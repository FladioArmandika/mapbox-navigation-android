package com.mapbox.navigation.ui.instruction

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.mapbox.api.directions.v5.models.BannerComponents
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.navigation.utils.internal.ifNonNull
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception
import java.lang.ref.WeakReference
import okhttp3.Interceptor
import okhttp3.OkHttpClient

/**
 * The class serves as a medium to emit bitmaps for the respective guidance view URL embedded in
 * [BannerInstructions]
 * @property callback OnImageDownload Callback that is triggered based on appropriate state of image downloading
 * @property context WeakReference<Context> [Context]
 * @property target Target
 * @constructor
 */
class GuidanceViewImageProvider(ctx: Context, val callback: OnImageDownload) {

    private val context: WeakReference<Context> = WeakReference(ctx)

    private val target: Target = object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            callback.onFailure(e?.message)
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            ifNonNull(bitmap) {
                callback.onImageReady(it)
            } ?: callback.onFailure("Something went wrong. Bitmap not received")
        }
    }

    companion object {
        private val USER_AGENT_KEY = "User-Agent"
        private val USER_AGENT_VALUE = "MapboxJava/"
    }

    /**
     * The API reads the bannerInstruction and renders a guidance view if one is available
     * @param bannerInstructions [BannerInstructions]
     */
    fun renderGuidanceView(bannerInstructions: BannerInstructions) {
        ifNonNull(context.get()) { cont ->
            val bannerView = bannerInstructions.view()
            ifNonNull(bannerView) { view ->
                val bannerComponents = view.components()
                ifNonNull(bannerComponents) { components ->
                    components.forEachIndexed { _, component ->
                        component.takeIf { c -> c.type() == BannerComponents.GUIDANCE_VIEW }?.let {
                            Picasso.Builder(cont).downloader(OkHttp3Downloader(getClient())).build()
                                .load(it.imageUrl()).into(target)
                        }
                    }
                } ?: callback.onNoGuidanceImageUrl()
            } ?: callback.onNoGuidanceImageUrl()
        }
    }

    private fun getClient(): OkHttpClient? {
        return OkHttpClient.Builder().addInterceptor { chain: Interceptor.Chain ->
            chain.proceed(
                chain.request().newBuilder().addHeader(USER_AGENT_KEY, USER_AGENT_VALUE).build()
            )
        }.build()
    }

    /**
     * Callback that is triggered based on appropriate state of image downloading
     */
    interface OnImageDownload {
        /**
         * Triggered when the image has been downloaded and is ready to be used.
         * @param bitmap Bitmap
         */
        fun onImageReady(bitmap: Bitmap)

        /**
         * Triggered when their is no URL to render
         */
        fun onNoGuidanceImageUrl()

        /**
         * Triggered when there is a failure to download the image
         * @param message String?
         */
        fun onFailure(message: String?)
    }
}
