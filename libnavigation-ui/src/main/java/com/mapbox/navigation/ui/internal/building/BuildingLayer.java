package com.mapbox.navigation.ui.internal.building;

import android.graphics.Color;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.expressions.Expression;

import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.distance;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.id;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;

/**
 * This class handles shared code for the {@link BuildingExtrusionHighlightLayer}
 * and {@link BuildingFootprintHighlightLayer} classes which show extruded 3d building
 * and {@link com.mapbox.mapboxsdk.style.layers.FillLayer} polygons respectively.
 * For now, this class is only compatible with the Mapbox Streets v7 vector tile source
 * because that's what the default Navigation UI SDK styles use.
 */
abstract class BuildingLayer {

  public static final String COMPOSITE_SOURCE_ID = "composite";
  public static final String BUILDING_LAYER_ID = "building";
  public static final Integer DEFAULT_HIGHLIGHT_COLOR = Color.RED;
  public static final Float DEFAULT_HIGHLIGHT_OPACITY = 1f;
  public static final Float QUERY_DISTANCE_MAX_METERS = 20f;
  private final MapboxMap mapboxMap;

  BuildingLayer(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
  }

  /**
   * Gets the correct {@link Expression#all(Expression...)} expression to show the building
   * extrusion associated with the query {@link LatLng}.
   *
   * @param queryLatLng the {@link LatLng} to use in determining the building ID to use in the
   *                   expression and which building is closest to the coordinate
   * @return an {@link Expression#all(Expression...)} expression
   */
  protected Expression getBuildingFilterExpression(LatLng queryLatLng) {
    return all(
        eq(get("extrude"), "true"),
        eq(get("type"), "building"),
        eq(get("underground"), "false"),
        eq(id(), literal(getBuildingId(queryLatLng))),
        lt(distance(Point.fromLngLat(queryLatLng.getLongitude(),
            queryLatLng.getLatitude())),
            literal(QUERY_DISTANCE_MAX_METERS))
    );
  }

  /**
   * Gets the specific ID from the building layer {@link Feature} that the
   * queryLatLng is within.
   *
   * @param queryLatLng the {@link LatLng} to use in determining the building ID to
   *                    use in the expression and which building is closest to the
   *                    coordinate.
   * @return the building ID as an integer
   */
  protected Integer getBuildingId(LatLng queryLatLng) {
    List<Feature> queryRenderedFeaturesList = mapboxMap.queryRenderedFeatures(
        mapboxMap.getProjection().toScreenLocation(queryLatLng), BUILDING_LAYER_ID);
    if (queryRenderedFeaturesList.size() > 0) {
      String buildingId = queryRenderedFeaturesList.get(0).id();
      if (buildingId != null) {
        return Integer.valueOf(buildingId);
      }
    }
    return 0;
  }
}
