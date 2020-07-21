package com.mapbox.navigation.core.test

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.base.common.logger.Logger
import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.navigation.base.TimeFormat.TWENTY_FOUR_HOURS
import com.mapbox.navigation.base.internal.VoiceUnit.METRIC
import com.mapbox.navigation.base.internal.extensions.inferDeviceLocale
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.trip.notification.NotificationAction
import com.mapbox.navigation.core.R
import com.mapbox.navigation.core.Rounding
import com.mapbox.navigation.core.internal.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.service.MapboxTripService
import com.mapbox.navigation.trip.notification.internal.MapboxTripNotification
import com.mapbox.navigation.utils.internal.ThreadController
import com.mapbox.navigation.utils.internal.monitorChannelWithException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TripServiceActivity : AppCompatActivity() {

    private var mainJobController = ThreadController.getMainScopeAndRootJob()
    private lateinit var mapboxTripNotification: MapboxTripNotification
    private lateinit var mapboxTripService: MapboxTripService
    private var textUpdateJob: Job = Job()
    private val dummyLogger = object : Logger {
        override fun d(tag: Tag?, msg: Message, tr: Throwable?) {}

        override fun e(tag: Tag?, msg: Message, tr: Throwable?) {}

        override fun i(tag: Tag?, msg: Message, tr: Throwable?) {}

        override fun v(tag: Tag?, msg: Message, tr: Throwable?) {}

        override fun w(tag: Tag?, msg: Message, tr: Throwable?) {}
    }
    private lateinit var toggleNotification: Button
    private lateinit var notifyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_service)

        toggleNotification = findViewById(R.id.toggleNotification)
        notifyTextView = findViewById(R.id.notifyTextView)

        val formatter = MapboxDistanceFormatter.builder()
            .withRoundingIncrement(Rounding.INCREMENT_FIFTY)
            .withUnitType(METRIC)
            .withLocale(this.inferDeviceLocale())
            .build(this)

        mapboxTripNotification = MapboxTripNotification(
            NavigationOptions.Builder(applicationContext)
                .distanceFormatter(formatter)
                .timeFormatType(TWENTY_FOUR_HOURS)
                .build()
        )

        mapboxTripService =
            MapboxTripService(applicationContext, mapboxTripNotification, dummyLogger)

        toggleNotification.setOnClickListener {
            when (mapboxTripService.hasServiceStarted()) {
                true -> {
                    stopService()
                }
                false -> {
                    mapboxTripService.startService()
                    changeText()
                    toggleNotification.text = "Stop"
                    monitorNotificationActionButton(MapboxTripNotification.notificationActionButtonChannel)
                }
            }
        }
    }

    private fun monitorNotificationActionButton(channel: ReceiveChannel<NotificationAction>) {
        mainJobController.scope.monitorChannelWithException(channel, { notificationAction ->
            when (notificationAction) {
                NotificationAction.END_NAVIGATION -> stopService()
            }
        })
    }

    private fun stopService() {
        textUpdateJob.cancel()
        mapboxTripService.stopService()
        toggleNotification.text = "Start"
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxTripService.stopService()
        ThreadController.cancelAllNonUICoroutines()
        ThreadController.cancelAllUICoroutines()
    }

    private fun changeText() {
        textUpdateJob = mainJobController.scope.launch {
            while (isActive) {
                val text = "Time elapsed: + ${SystemClock.elapsedRealtime()}"
                notifyTextView.text = text
                mapboxTripService.updateNotification(null)
                delay(1000L)
            }
        }
    }
}
