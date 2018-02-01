package com.mapfit.mapfitsdk

import android.view.View
import com.mapfit.mapfitsdk.annotations.Marker
import com.mapfit.mapfitsdk.annotations.Polygon
import com.mapfit.mapfitsdk.annotations.Polyline
import com.mapfit.mapfitsdk.annotations.callback.OnMarkerAddedCallback
import com.mapfit.mapfitsdk.annotations.callback.OnMarkerClickListener
import com.mapfit.mapfitsdk.annotations.callback.OnPolygonClickListener
import com.mapfit.mapfitsdk.annotations.callback.OnPolylineClickListener
import com.mapfit.mapfitsdk.geometry.LatLng
import com.mapfit.mapfitsdk.geometry.LatLngBounds


/**
 * Controller for the map.
 *
 * Created by dogangulcan on 12/19/17.
 */
abstract class MapfitMap {

    /**
     * Sets the center of the map.
     *
     * @param latLng coordinates
     */
    abstract fun setCenter(latLng: LatLng)

    /**
     * Sets the center of the map.
     *
     * @param latLng coordinates
     * @param duration optional, the camera will move with ease.
     */
    abstract fun setCenter(latLng: LatLng, duration: Long = 0)

    /**
     * Sets the center of the map accordingly to the layer.
     *
     * @param layer the map will center accordingly to
     */
    protected abstract fun setCenterWithLayer(
        layer: Layer,
        duration: Long = 0,
        paddingPercentage: Float
    )

    /**
     * Adds the given layer to the map.
     *
     * @param layer to add to the map
     */
    abstract fun addLayer(layer: Layer)


    /**
     * Returns the layers of the map.
     *
     * @return layer list
     */
    abstract fun getLayers(): List<Layer>

    /**
     * @return center of the visible map
     */
    abstract fun getCenter(): LatLng

    /**
     * Adds a marker on the default layer to the given coordinates.
     *
     * @return marker
     */
    abstract fun addMarker(latLng: LatLng): Marker

    /**
     * Adds a marker on the default layer for the given address. Marker will be placed at the
     * coordinates of main entrance if the address belongs to a building. If there are no entrance
     * information for given address, marker will be placed to the most accurate coordinates.
     *
     * @param address an address such as "119w 24th st NY" venue names are shouldn't be given
     * @param onMarkerAddedCallback for response and errors
     */
    abstract fun addMarker(address: String, onMarkerAddedCallback: OnMarkerAddedCallback)

    /**
     * Removes the given marker from the map.
     *
     * @param marker to be removed
     * @return isRemoved
     */
    abstract fun removeMarker(marker: Marker): Boolean

    /**
     * Adds a polyline to default layer.
     *
     * @return polyline
     */
    protected abstract fun addPolyline(): Polyline

    /**
     * Removes given [Polyline] from the [MapView].
     * @param polyline to be removed
     */
    protected abstract fun removePolyline(polyline: Polyline)

    /**
     * Adds a polygon to default layer.
     *
     * @return polygon
     */
    protected abstract fun addPolygon(polygon: List<List<LatLng>>): Polygon

    /**
     * Removes given [Polygon] from the [MapView].
     *
     * @param polygon to be removed
     */
    protected abstract fun removePolygon(polygon: Polygon)

    /**
     * Removes given [Layer] from the [MapView].
     *
     * @param layer to be removed
     */
    abstract fun removeLayer(layer: Layer)

    /**
     * Sets zoom level of the map.
     *
     * @param zoomLevel Zoom level for the view
     * @param duration optional duration for zooming in milliseconds
     */
    abstract fun setZoom(zoomLevel: Float, duration: Int = 0)

    /**
     * @return current zoom level of the map
     */
    abstract fun getZoom(): Float

    /**
     * Sets the visible map bounds to given [LatLngBounds]. Zoom level and center will be set from
     * given [LatLngBounds].
     *
     * @param bounds
     * @param padding between map and bounds as percentage. For 10% padding, you can pass 0.1f.
     */
    abstract fun setBounds(bounds: LatLngBounds, padding: Float)

    /**
     * @return latLngBounds for currently visible [MapView]
     */
    abstract fun getBounds(): LatLngBounds

    /**
     * Sets [OnMapClickListener] for [MapView] that single click events will be passed to.
     */
    abstract fun setOnMapClickListener(listener: OnMapClickListener)

    /**
     * Sets [OnMapDoubleClickListener] for [MapView] that double click events will be passed to.
     */
    abstract fun setOnMapDoubleClickListener(listener: OnMapDoubleClickListener)

    /**
     * Sets [OnMapLongClickListener] for [MapView] that long click events will be passed to.
     */
    abstract fun setOnMapLongClickListener(listener: OnMapLongClickListener)

    /**
     * Sets [OnMarkerClickListener] for [MapView] that marker click events will be passed to.
     */
    abstract fun setOnMarkerClickListener(listener: OnMarkerClickListener)

    /**
     * Sets [OnMapPanListener] for [MapView] that pan events will be passed to.x
     */
    abstract fun setOnMapPanListener(listener: OnMapPanListener)

    /**
     * Sets [OnMapPinchListener] for [MapView] that pan events will be passed to.x

     * @param listener
     */
    abstract fun setOnMapPinchListener(listener: OnMapPinchListener)

    /**
     * Used to set custom views for place info.
     * When the callback is invoked, you can return the view you want to be shown as place info.
     *
     * @param adapter the callback to be invoked to obtain your custom place info.
     */
    abstract fun setPlaceInfoAdapter(adapter: PlaceInfoAdapter)

    /**
     * Sets a callback that's invoked when the user clicks on an info window.
     *
     * @param listener The callback that's invoked when the user clicks on an info window.
     *                 To unset the callback, use null.
     */
    abstract fun setOnPlaceInfoClickListener(listener: OnPlaceInfoClickListener)

    protected abstract fun setOnPolylineClickListener(listener: OnPolylineClickListener)

    protected abstract fun setOnPolygonClickListener(listener: OnPolygonClickListener)

    /**
     * MapOptions can be used to changing options for the map. For instance, setting maximum zoom
     * level or turning zoom controls off.
     */
    abstract fun getMapOptions(): MapOptions

    protected abstract fun getDirectionsOptions(): DirectionsOptions

    protected abstract fun setTilt(angle: Float)

    protected abstract fun getTilt(): Float

    protected abstract fun setRotation(angle: Float)

    protected abstract fun getRotation(): Float

    /**
     * Will reCenter the map.
     */
    abstract fun reCenter()

    interface PlaceInfoAdapter {

        /**
         * Called when a place info will be shown after a marker click.
         *
         * @param marker The marker the user clicked on.
         * @return View to be shown as a place info. If null is returned the default
         * info window will be shown.
         */
        fun getPlaceInfoView(marker: Marker): View
    }

    interface OnPlaceInfoClickListener {

        /**
         * Called when the user clicks on a place info.
         *
         * @param marker The marker of the place info that is clicked on.
         */
        fun onPlaceInfoClicked(marker: Marker)
    }

}