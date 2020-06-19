package com.example.ddareungi.map

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.ddareungi.R
import com.example.ddareungi.data.BikeStation
import com.example.ddareungi.data.Park
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.bike_marker.view.*

class MarkerController(val context: Context, val mapViewModel: MapViewModel) {

    private val iconGen = IconGenerator(context)
    private val markerZeroDrawable = context.getDrawable(R.drawable.ic_simple_marker_low)
    private val markerMiddleDrawable = context.getDrawable(R.drawable.ic_simple_marker_middle)
    private val markerHighDrawable = context.getDrawable(R.drawable.ic_simple_marker_high)

    fun updateBikeStationMarkers(lifecycleOwner: LifecycleOwner, map: GoogleMap, bounds: LatLngBounds) {

        mapViewModel.updateVisibleStations(bounds)

        mapViewModel.updateStations.observe(lifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                for(station in it) {
                    val markerOptions = setBikeStationMarkerOptions(station)
                    // MarkerOption 추가 및 마커의 태그에 [station] 객체 설정
                    val marker = map.addMarker(markerOptions)
                    marker.tag = station
                }
            }
        })
    }

    fun updateParkMarkers(lifecycleOwner: LifecycleOwner, map: GoogleMap, bounds: LatLngBounds) {

        mapViewModel.updateVisibleParks(bounds)

        mapViewModel.updateParks.observe(lifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                for (park in it) {
                    val markerOption = setParkMarkerOptions(park)
                    val marker = map.addMarker(markerOption)
                    marker.tag = park
                }
            }
        })
    }

    fun addSearchedMarker(place: Place, map: GoogleMap): Marker {
        val markerOptions = MarkerOptions()
        markerOptions.position(place.latLng!!)

        val marker = map.addMarker(markerOptions)
        marker!!.tag = place
        marker.title = place.name

        return marker
    }

    private fun setBikeStationMarkerOptions(station: BikeStation): MarkerOptions {
        val markerOptions = MarkerOptions()
        val markerView = LayoutInflater.from(context).inflate(R.layout.bike_marker, null)

        // 거치된 자전거 수 표시
        markerView.amu_text.text = station.parkingBikeTotCnt.toString()

        // 자전거 수에 따라 마커 색상 설정
        if (station.parkingBikeTotCnt == 0) {
            iconGen.setBackground(markerZeroDrawable)
        } else if (station.parkingBikeTotCnt < 10) {
            iconGen.setBackground(markerMiddleDrawable)
        } else {
            iconGen.setBackground(markerHighDrawable)
        }

        // IconGenerator의 view 설정
        iconGen.setContentView(markerView)

        // 마커의 위치 정보 설정
        markerOptions.position(LatLng(station.stationLatitude, station.stationLongitude))

        // IconGenerator를 비트맵으로 생성해 markerOption의 icon으로 설정
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGen.makeIcon("${station.parkingBikeTotCnt}")))

        return markerOptions
    }

    private fun setParkMarkerOptions(park: Park): MarkerOptions {
        val markerOptions = MarkerOptions()
        val markerView = LayoutInflater.from(context).inflate(R.layout.bike_marker, null)

        iconGen.setBackground(context.getDrawable(R.drawable.ic_marker_park))

        iconGen.setContentView(markerView)

        markerOptions.position(LatLng(park.latitude, park.longitude))
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGen.makeIcon()))

        return markerOptions
    }

}