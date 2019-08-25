package com.example.ddareungi.map

import android.location.Location
import android.support.annotation.IdRes
import com.example.ddareungi.R
import com.example.ddareungi.bookmark.PlaceType
import com.example.ddareungi.data.Bike
import com.example.ddareungi.data.Park
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.model.Place
import java.net.URLEncoder

class MapPresenter(val dataRepository: DataRepository, val mapView: MapContract.View, val fromBookmarkFrag: Boolean, val clickedStationName: String)
    : MapContract.Presenter {

    override var currentPlaceType = PlaceType.BIKE
    override var currentClickedMarkerType: PlaceType = PlaceType.BIKE
    override var currentClickedMarker: Marker? = null
    val markersToShow = mutableMapOf<String, Any>()
    var userLocation: Location? = null

    init {
        mapView.presenter = this
    }

    override fun initCameraPosition() {
        if(fromBookmarkFrag) {
            var clickedStation = Bike.newInstance()

            for(bike in dataRepository.bikeList) {
                if(bike.stationName == clickedStationName) {
                    clickedStation = bike
                    break
                }
            }
            val name = clickedStation.stationName
            val bikeNumText = "${clickedStation.parkingBikeTotCnt}대 사용 가능"
            var bookmarked = false
            if (dataRepository.findBookmarkInDatabase(clickedStation.stationName)) {
                bookmarked = true
            }
            mapView.moveCameraByPos(clickedStation.stationLatitude, clickedStation.stationLongitude)
            mapView.showBikeCardView(name, bikeNumText, bookmarked)
        } else {
            mapView.moveCameraToUser(init = true)
        }
    }

    override fun updateMarkers(bounds: LatLngBounds, zoomLevel: Float, clearAll: Boolean) {
        val showKeyList = mutableListOf<String>()
        val removeKeyList = mutableListOf<String>()
        if (clearAll)
            markersToShow.clear()

        when (currentPlaceType) {
            PlaceType.BIKE -> {
                val bikeList = dataRepository.bikeList
                for (bike in bikeList) {
                    if (bounds.contains(LatLng(bike.stationLatitude, bike.stationLongitude))) {
                        if (markersToShow.containsKey(bike.stationId)) {
                            if (zoomLevel < 15f && bike.parkingBikeTotCnt == 0) {
                                markersToShow.remove(bike.stationId)
                                removeKeyList.add(bike.stationId)
                            }
                        } else {
                            if (zoomLevel >= 15f || (zoomLevel < 15f && bike.parkingBikeTotCnt > 0)) {
                                markersToShow[bike.stationId] = bike
                                showKeyList.add(bike.stationId)
                            }
                        }
                    }
                }
            }
            PlaceType.TOILET -> {
                val toiletList = dataRepository.toiletList
                for (toilet in toiletList) {
                    if (bounds.contains(LatLng(toilet.wgs84_y, toilet.wgs84_x)) && !markersToShow.containsKey(toilet.fName)) {
                        markersToShow[toilet.fName] = toilet
                        showKeyList.add(toilet.fName)
                    }
                }
            }
            PlaceType.PARK -> {
                val parkList = dataRepository.parkList
                for (park in parkList) {
                    if (bounds.contains(LatLng(park.latitude, park.longitude)) && !markersToShow.containsKey(park.id.toString())) {
                        markersToShow[park.id.toString()] = park
                        showKeyList.add(park.id.toString())
                    }
                }
            }
            else -> {}
        }
        mapView.showMarkerOnCurrentMap(markersToShow, showKeyList, removeKeyList)
    }

    override fun updateClickedMarkerCardView(marker: Marker) {
        when (currentClickedMarkerType) {
            PlaceType.BIKE -> {
                val bike = marker.tag as Bike
                val name = bike.stationName
                val bikeNumText = "${bike.parkingBikeTotCnt}대 사용 가능"
                var bookmarked = false
                if (dataRepository.findBookmarkInDatabase(bike.stationName)) {
                    bookmarked = true
                }
                mapView.showBikeCardView(name, bikeNumText, bookmarked)
            }
            PlaceType.TOILET -> { }
            PlaceType.PARK -> {
                val park = marker.tag as Park
                val name = park.name
                var dist: Float = -1f
                mapView.getUserLocation(object: GetUserLocationCallback {
                    override fun onSuccess(location: Location?) {
                        dist = getDistance(park.latitude, park.longitude, location)
                        var distStr = ""
                        if (dist != -1f) {
                            distStr = String.format("%.1fkm", dist)
                        }
                        mapView.showParkCardView(name, distStr)
                    }
                    override fun onFailure() {}
                })
            }
            PlaceType.SEARCH -> {
                val searchedPlace = marker.tag as Place
                val name = searchedPlace.name
                var dist = 0f
                mapView.getUserLocation(object: GetUserLocationCallback {
                    override fun onSuccess(location: Location?) {
                        dist = getDistance(searchedPlace.latLng!!.latitude, searchedPlace.latLng!!.longitude, location)
                        var distStr = ""
                        if(dist != -1f) {
                            distStr = String.format("%.1fkm", dist)
                        }
                        mapView.showDestCardView(name, distStr)
                    }
                    override fun onFailure() {}
                })
            }
        }
    }

    override fun getUrlForNaverMap(@IdRes buttonId: Int) {
        var url: String = ""
        val dname: String
        val dlat =currentClickedMarker!!.position.latitude
        val dlng = currentClickedMarker!!.position.longitude

        when(buttonId) {
            R.id.path_button -> {
                dname = URLEncoder.encode((currentClickedMarker!!.tag as Bike).stationName, "UTF-8")
                url =
                    "nmap://route/walk?dlat=$dlat&dlng=$dlng&dname=$dname&appname=com.example.ddareungi"
            }
            R.id.park_card_path_button -> {
                dname = URLEncoder.encode((currentClickedMarker!!.tag as Park).name, "UTF-8")
                val closetBikeStation = findClosestBikeStation(dlat, dlng)
                val vlat = closetBikeStation.stationLatitude
                val vlng = closetBikeStation.stationLongitude
                val vname = URLEncoder.encode(closetBikeStation.stationName, "UTF-8")
                url =
                    "nmap://route/bicycle?dlat=$dlat&dlng=$dlng&dname=$dname&v1lat=$vlat&v1lng=$vlng&v1name=$vname&appname=com.example.ddareungi"
            }
            R.id.dest_card_path_button -> {
                dname = URLEncoder.encode((currentClickedMarker!!.tag as Place).name, "UTF-8")
                val closetBikeStation = findClosestBikeStation(dlat, dlng)
                val vlat = closetBikeStation.stationLatitude
                val vlng = closetBikeStation.stationLongitude
                val vname = URLEncoder.encode(closetBikeStation.stationName, "UTF-8")
                url =
                    "nmap://route/bicycle?dlat=$dlat&dlng=$dlng&dname=$dname&v1lat=$vlat&v1lng=$vlng&v1name=$vname&appname=com.example.ddareungi"
            }
        }
        mapView.showPathInNaverMap(url)
    }

    private fun getDistance(destLat: Double, destLng: Double, location: Location?): Float {
        val dest = Location("dest")
        var dist = 0f
        dest.latitude = destLat
        dest.longitude = destLng
        if(location != null) {
            dist = location.distanceTo(dest) / 1000
        }
        return dist
    }

    override fun processUserLocation(location: Location?) {
        userLocation = location
    }

    private fun findClosestBikeStation(lat: Double, lng: Double): Bike {
        val dest = Location("dest")
        dest.latitude = lat
        dest.longitude = lng

        var closetBikeStation = Bike.newInstance()
        var dist = Float.MAX_VALUE

        for(bike in dataRepository.bikeList) {
            val bikeStation = Location("bike")
            bikeStation.latitude = bike.stationLatitude
            bikeStation.longitude = bike.stationLongitude
            var tempDist: Float
            tempDist = dest.distanceTo(bikeStation)
            if(dist > tempDist) {
                dist = tempDist
                closetBikeStation = bike
            }
        }
        return closetBikeStation
    }


    override fun updateBookmarkState() {
        val stationName = (currentClickedMarker!!.tag as Bike).stationName
        val bookmarked = (currentClickedMarker!!.tag as Bike).bookmarked
        if (bookmarked == 0) {
            (currentClickedMarker!!.tag as Bike).bookmarked = 1
            dataRepository.addBookmarkToDatabase(stationName)
        }
        else if (bookmarked == 1) {
            (currentClickedMarker!!.tag as Bike).bookmarked = 0
            dataRepository.deleteBookmarkInDatabase(stationName)
        }
        mapView.changeBookmarkState((currentClickedMarker!!.tag as Bike).bookmarked)
    }

    override fun requestBikeDataUpdate() {
        mapView.showLoadingIndicator(active = true)

        dataRepository.refreshBike(object : DataSource.LoadDataCallback {
            override fun onDataLoaded() {
                mapView.showLoadingIndicator(active = false)
                mapView.showUpdatedBikeMarker()
            }

            override fun onNetworkNotAvailable() {
                mapView.showLoadingIndicator(active = false)
                mapView.showLoadingDataFailedDialog()
            }

        })
    }

    interface GetUserLocationCallback {
        fun onSuccess(location: Location?)

        fun onFailure()
    }
}