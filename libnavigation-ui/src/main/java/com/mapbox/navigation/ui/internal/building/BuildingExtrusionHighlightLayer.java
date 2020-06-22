package com.mapbox.navigation.ui.internal.building;

import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;

import androidx.annotation.NonNull;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;
import static com.mapbox.navigation.ui.internal.building.BuildingFootprintHighlightLayer.BUILDING_HIGHLIGHTED_FOOTPRINT_LAYER_ID;

/**
 * This layer handles the creation and customization of a {@link FillExtrusionLayer}
 * to show 3D buildings. For now, this layer is only compatible with the Mapbox Streets
 * v7 vector tile source because that's what the default Navigation UI SDK styles use.
 */
public class BuildingExtrusionHighlightLayer extends BuildingLayer {

  public static final String BUILDING_HIGHLIGHTED_EXTRUSION_LAYER_ID = "building-extrusion-highlighted-layer-id";
  private LatLng queryLatLng;
  private Integer color = DEFAULT_HIGHLIGHT_COLOR;
  private Float opacity = DEFAULT_HIGHLIGHT_OPACITY;
  private MapboxMap mapboxMap;

  public BuildingExtrusionHighlightLayer(MapboxMap mapboxMap) {
    super(mapboxMap);
    this.mapboxMap = mapboxMap;
  }

  /**
   * Toggles the visibility of the highlighted extrusion layer.
   *
   * @param visible true if the layer should be displayed. False if it should be hidden.
   */
  public void updateVisibility(boolean visible) {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        FillExtrusionLayer buildingExtrusionLayer = style.getLayerAs(BUILDING_HIGHLIGHTED_EXTRUSION_LAYER_ID);
        if (buildingExtrusionLayer == null && visible) {
          addHighlightExtrusionLayerToMap();
        } else if (buildingExtrusionLayer != null) {
          buildingExtrusionLayer.setProperties(visibility(visible ? VISIBLE : NONE));
        }
      }
    });
  }

  /**
   * Set the {@link LatLng} location of the building extrusion highlight layer.
   * The {@link LatLng} passed through this method is used to see whether its within the
   * footprint of a specific building. If so, that building's footprint is used for the 3D
   * highlighted extrusion.
   *
   * @param queryLatLng the new coordinates to use in querying the building layer
   *                    to get the associated {@link Polygon} to eventually highlight.
   */
  public void setQueryLatLng(LatLng queryLatLng) {
    this.queryLatLng = queryLatLng;
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        FillExtrusionLayer buildingExtrusionLayer = style.getLayerAs(BUILDING_HIGHLIGHTED_EXTRUSION_LAYER_ID);
        if (buildingExtrusionLayer != null) {
          buildingExtrusionLayer.setFilter(getBuildingFilterExpression(queryLatLng));
        }
      }
    });
  }

  /**
   * Set the color of the highlighted extrusion layer.
   *
   * @param newColor the new color value
   */
  public void setColor(int newColor) {
    color = newColor;
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        FillExtrusionLayer buildingFillExtrusionLayer = style.getLayerAs(BUILDING_HIGHLIGHTED_EXTRUSION_LAYER_ID);
        if (buildingFillExtrusionLayer != null) {
          buildingFillExtrusionLayer.withProperties(fillExtrusionColor(newColor));
        }
      }
    });
  }

  /**
   * Set the opacity of the highlighted extrusion layer.
   *
   * @param newOpacity the new opacity value
   */
  public void setOpacity(Float newOpacity) {
    opacity = newOpacity;
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        FillExtrusionLayer buildingFillExtrusionLayer = style.getLayerAs(BUILDING_HIGHLIGHTED_EXTRUSION_LAYER_ID);
        if (buildingFillExtrusionLayer != null) {
          buildingFillExtrusionLayer.withProperties(fillExtrusionOpacity(newOpacity));
        }
      }
    });
  }

  /**
   * Retrieve the latest set color of the highlighted extrusion layer.
   *
   * @return the color Integer
   */
  public Integer getColor() {
    return color;
  }

  /**
   * Retrieve the latest set opacity of the highlighted extrusion layer.
   *
   * @return the opacity Float
   */
  public Float getOpacity() {
    return opacity;
  }

  /**
   * Retrieve the latest set opacity of the highlighted extrusion layer.
   *
   * @return the opacity Float
   */
  public LatLng getQueryLatLng() {
    return queryLatLng;
  }

  /**
   * Customize and add a {@link FillExtrusionLayer} to the map to show a
   * highlighted building extrusion.
   */
  private void addHighlightExtrusionLayerToMap() {
    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        FillExtrusionLayer fillExtrusionLayer = new FillExtrusionLayer(
            BUILDING_HIGHLIGHTED_EXTRUSION_LAYER_ID, COMPOSITE_SOURCE_ID);
        fillExtrusionLayer.setSourceLayer(BUILDING_LAYER_ID);
        if (queryLatLng != null) {
          fillExtrusionLayer.setFilter(getBuildingFilterExpression(queryLatLng));
        }
        fillExtrusionLayer.withProperties(
            fillExtrusionColor(color == null ? DEFAULT_HIGHLIGHT_COLOR : color),
            fillExtrusionOpacity(opacity == null ? DEFAULT_HIGHLIGHT_OPACITY : opacity),
            fillExtrusionHeight(get("height"))
        );
        if (style.getLayerAs(BUILDING_HIGHLIGHTED_FOOTPRINT_LAYER_ID) != null) {
          style.addLayerAbove(fillExtrusionLayer, BUILDING_HIGHLIGHTED_FOOTPRINT_LAYER_ID);
        } else {
          style.addLayer(fillExtrusionLayer);
        }
      }
    });
  }
}
