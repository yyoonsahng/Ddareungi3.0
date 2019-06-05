package com.example.ddareungi

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ddareungi.dataClass.MyBike
import com.example.ddareungi.dataClass.MyPark
import com.example.ddareungi.dataClass.MyRestroom
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import kotlinx.android.synthetic.main.bike_marker.view.*

class MarkerController(
    context: Context,
    googleMap: GoogleMap,
    visibleMarkers: MutableMap<String, Marker>
) {
    private val mContext = context
    private val mMap = googleMap
    private val mVisibleMarkers = visibleMarkers

    fun addBikeMarker(bike: MyBike): Marker {
        val markerOptions = MarkerOptions()
        //marker의 위치 정보 설정
        markerOptions.position(LatLng(bike.stationLatitude, bike.stationLongitude))

        //marker에 해당하는 뷰 객체 얻어옴
        val markerView = LayoutInflater.from(mContext).inflate(R.layout.bike_marker, null)

        //markerView의 textView에 자전거 수 표시
        markerView.bike_marker_num_textView.text = bike.parkingBikeTotCnt.toString()

        //자전거 수 별 다른 아이콘 이미지 설정, 자전거가 많을 수록 zIndex 값을 높게 설정해서 우선적으로 보이게 함
        if (bike.parkingBikeTotCnt == 0) {
            markerView.bike_marker_num_textView.setBackgroundResource(R.drawable.ic_marker_zero)
        } else if (bike.parkingBikeTotCnt < 10) {
            markerView.bike_marker_num_textView.setBackgroundResource(R.drawable.ic_marker_low)
            markerOptions.zIndex(1.0f)
        } else {
            markerOptions.zIndex(2.0f)
        }
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(mContext, markerView)))
        return mMap.addMarker(markerOptions)
    }

    fun addToiletMarker(toilet: MyRestroom): Marker {
        val markerOptions = MarkerOptions()
        markerOptions.position(LatLng(toilet.wgs84_y, toilet.wgs84_x))
        markerOptions.icon(bitmapDescriptorFromVector(mContext, R.drawable.ic_toilet_marker))

        return mMap.addMarker(markerOptions)
    }

    fun addParkMarker(park: MyPark): Marker {
        val markerOptions = MarkerOptions()
        markerOptions.position(LatLng(park.latitude, park.longitude))
        markerOptions.icon(bitmapDescriptorFromVector(mContext, R.drawable.ic_marker_park))

        return mMap.addMarker(markerOptions)
    }

    fun addSearchMarker(place: Place): Marker {
        val markerOptions = MarkerOptions()
        markerOptions.position(place.latLng!!)

        return mMap.addMarker(markerOptions)
    }

    fun removeMarker(markerKey: String) {
        mVisibleMarkers[markerKey]!!.remove()
        mVisibleMarkers.remove(markerKey)
    }

    /*
        marker를 View 형태로 만들어서 textView에 자전거 수를 표시하게 함.
        marker 아이콘은 비트맵 형태로 사용해야 되므로 View를 비트맵으로 바꿔줘야 함
     */
    fun createDrawableFromView(context: Context, view: View): Bitmap {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}
