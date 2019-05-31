package com.example.ddareungi

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.a190306app.MyBike
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.bike_marker.view.*

class Markers(context: Context, googleMap: GoogleMap) {
    private val mContext = context
    private val mMap = googleMap

    fun setMarker(bikeStop: MyBike): Marker {
        val markerOptions = setMarkerOption(bikeStop)
        return mMap.addMarker(markerOptions)
    }


    fun setMarkerOption (bikeStop : MyBike): MarkerOptions {
        val marker = MarkerOptions()

        //marker의 위치 정보 설정
        marker.position(LatLng(bikeStop.stationLatitude, bikeStop.stationLongitude))

        //marker에 해당하는 뷰 객체 얻어옴
        val markerView = LayoutInflater.from(mContext).inflate(R.layout.bike_marker, null)

        //markerView의 textView에 자전거 수 표시
        markerView.bike_marker_num_textView.text = bikeStop.parkingBikeTotCnt.toString()

        //자전거 수 별 다른 아이콘 이미지 설정, 자전거가 많을 수록 zIndex 값을 높게 설정해서 우선적으로 보이게 함
        if(bikeStop.parkingBikeTotCnt == 0) {
            markerView.bike_marker_num_textView.setBackgroundResource(R.drawable.ic_marker_zero)
        } else if (bikeStop.parkingBikeTotCnt < 10){
            markerView.bike_marker_num_textView.setBackgroundResource(R.drawable.ic_marker_low)
            marker.zIndex(1.0f)
        } else {
            marker.zIndex(2.0f)
        }
        marker.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(mContext, markerView)))

        return marker
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

}
