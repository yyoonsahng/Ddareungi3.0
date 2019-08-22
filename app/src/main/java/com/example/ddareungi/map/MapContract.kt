package com.example.ddareungi.map

import android.location.Location
import android.support.annotation.IdRes
import com.example.ddareungi.bookmark.PlaceType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.model.Place

interface MapContract {

    interface View {
        var presenter: Presenter

        interface GpsStateListener {
            fun onGpsTurnOn()

            fun onGpsTurnOff()
        }

        fun showMarkerOnCurrentMap(markersToShow: MutableMap<String, Any>, showKeyList: MutableList<String>, removeKeyList: MutableList<String>)

        fun showUpdatedBikeMarker()

        fun showBikeCardView(stationName: String, leftBikeNum: String, bookmarked: Boolean)

        fun showParkCardView(name: String, dist: String)

        fun showDestCardView(name: String?, dist: String)

        fun changeBookmarkState(bookmarked: Int)

        fun showPathInNaverMap(url: String)

        fun moveCameraToUser()

        fun moveCameraByPos(lat: Double, lng: Double)

        fun getUserLocation(callback: MapPresenter.GetUserLocationCallback)

        fun showLoadingDataFailedDialog()

        fun showNoGpsDialog()
    
   }

    interface Presenter {

        var currentPlaceType: PlaceType

        var currentClickedMarker: Marker?

        var currentClickedMarkerType: PlaceType

        fun start()

        fun initCameraPosition()

        fun updateMarkers(bounds: LatLngBounds, zoomLevel: Float, clearAll: Boolean)

        fun addSearchMarker(place: Place)

        fun updateClickedMarkerCardView(marker: Marker)

        fun updateBookmarkState()

        fun processUserLocation(location: Location?)

        fun requestBikeDataUpdate()

        fun getUrlForNaverMap(@IdRes buttonId: Int)
    }
}