package com.example.ddareungi.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import com.example.ddareungi.R
import com.example.ddareungi.data.Bike
import com.example.ddareungi.data.Park
import com.example.ddareungi.data.Toilet
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.bike_marker.view.*

class MarkerController(val context: Context, val googleMap: GoogleMap) {
    val visibleMarkers = mutableMapOf<String, Marker>()


    fun addBikeMarker(markersToShow: MutableMap<String, Bike>, showKeyList: MutableList<String>, removeKeyList: MutableList<String>) {
        val iconGenerator = IconGenerator(context)

        for(key in showKeyList) {
            val bike = markersToShow[key]
            val markerOptions = MarkerOptions()
            val bikeMarkerView = LayoutInflater.from(context).inflate(R.layout.bike_marker, null)

            bikeMarkerView!!.amu_text.text = bike!!.parkingBikeTotCnt.toString()
            if(bike.parkingBikeTotCnt == 0) {
                iconGenerator.setBackground(context.getDrawable(R.drawable.ic_simple_marker_low))
            } else if(bike.parkingBikeTotCnt < 10) {
                iconGenerator.setBackground(context.getDrawable(R.drawable.ic_simple_marker_middle))
            } else {
                iconGenerator.setBackground(context.getDrawable(R.drawable.ic_simple_marker_high))
            }
            iconGenerator.setContentView(bikeMarkerView)
            //marker의 위치 정보 설정
            markerOptions.position(LatLng(bike.stationLatitude, bike.stationLongitude))

            //자전거 수 별 다른 아이콘 이미지 설정, 자전거가 많을 수록 zIndex 값을 높게 설정해서 우선적으로 보이게 함
            if (bike.parkingBikeTotCnt < 10) {
                markerOptions.zIndex(1.0f)
            } else {
                markerOptions.zIndex(2.0f)
            }
            //뷰를 drawable로 만들어서 마커의 아이콘으로 설정
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))

            visibleMarkers[key] = (googleMap.addMarker(markerOptions))
            visibleMarkers[key]!!.tag = bike
        }

        for(key in removeKeyList) {
            removeMarker(key)
        }
    }

    fun addToiletMarker(markersToShow: MutableMap<String, Toilet>, keyList: MutableList<String>) {
        for(key in keyList) {
            val toilet = markersToShow[key]
            val markerOptions = MarkerOptions()

            markerOptions.position(LatLng(toilet!!.wgs84_y, toilet.wgs84_x))
            markerOptions.icon(bitmapDescriptorFromVector(context, R.drawable.ic_toilet_marker))

            visibleMarkers[key] = googleMap.addMarker(markerOptions)
            visibleMarkers[key]!!.tag = toilet
            visibleMarkers[key]!!.title = toilet.fName
        }
    }

    fun addParkMarker(markersToShow: MutableMap<String, Park>, keyList: MutableList<String>) {
        for(key in keyList) {
            val park = markersToShow[key]
            val markerOptions = MarkerOptions()

            markerOptions.position(LatLng(park!!.latitude, park.longitude))
            markerOptions.icon(bitmapDescriptorFromVector(context, R.drawable.ic_marker_park))

            visibleMarkers[key] = googleMap.addMarker(markerOptions)
            visibleMarkers[key]!!.tag = park
        }
    }

    fun addSearchMarker(place: Place): Marker {
        val markerOptions = MarkerOptions()
        markerOptions.position(place.latLng!!)

        val marker = googleMap.addMarker(markerOptions)
        marker!!.tag = place
        marker.title = place.name
        visibleMarkers[place.name!!] = marker
        return marker
    }

    fun removeMarker(markerKey: String) {
        visibleMarkers[markerKey]!!.remove()
        visibleMarkers.remove(markerKey)
    }

    fun findBikeMarker(key: String, bike: Bike): Marker {
        if(visibleMarkers[key] == null) {
            addBikeMarker(mutableMapOf(key to bike), mutableListOf(key), mutableListOf())
        }
        return visibleMarkers[key]!!
    }

    /*
        marker를 View 형태로 만들어서 textView에 자전거 수를 표시하게 함.
        marker 아이콘은 비트맵 형태로 사용해야 되므로 View를 비트맵으로 바꿔줘야 함
     */
//    private fun createDrawableFromView(context: Context, view: View): Bitmap {
//        val displayMetrics = DisplayMetrics()
//        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
//        view.layoutParams =
//            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
//        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
//        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
//
//        val canvas = Canvas(bitmap)
//        view.draw(canvas)
//
//        return bitmap
//    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}
