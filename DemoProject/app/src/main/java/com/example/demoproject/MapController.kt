package com.example.demoproject

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.custom_marker.view.*


class MapController(context: Context, googleMap: GoogleMap) {

    private val mContext = context
    private val mMap = googleMap

    val KONKUK_UNIV = LatLng(37.540, 127.07)
    var stopMarkerOptionList = mutableListOf<StopMarkerItem>()
    var stopMarkerList = mutableListOf<Marker>()

    fun setMarker() {

        //따릉이 정류소에 대한 임시 데이터. 필요한 데이터 값 : id, 정류소 이름, 위치 정보(latitude, longitude), 자전거 수)
        stopMarkerOptionList.add(StopMarkerItem(0, "정류소1", KONKUK_UNIV, 10))
        stopMarkerOptionList.add(StopMarkerItem(1, "정류소2", LatLng(37.5405, 127.07), 8))
        stopMarkerOptionList.add(StopMarkerItem(2, "정류소3", LatLng(37.5396, 127.07), 0))

        for(i in stopMarkerOptionList.indices) {
            val markerOption = createMarker(stopMarkerOptionList[i])
            //설정한 Option 값으로 지도 상에 marker를 찍음
            stopMarkerList.add(mMap.addMarker(markerOption))
            //marker를 클릭했을 때 bottom sheet에 보여줄 데이터를 저장하기 위해 tag 값으로 저장
            val marker = createMarker(stopMarkerOptionList[i])
            stopMarkerList.add(mMap.addMarker(marker))
            stopMarkerList[i].tag = stopMarkerOptionList[i]
        }

    }

    //marker 별 해당하는 Option 값을 설정
    fun createMarker(StopMarkerItem: StopMarkerItem): MarkerOptions {
        val marker = MarkerOptions()

        //marker의 위치 정보 설정
        marker.position(StopMarkerItem.latLng)

        //marker에 해당하는 뷰 객체 얻어옴
        val markerView = LayoutInflater.from(mContext).inflate(R.layout.custom_marker, null)

        //markerView의 textView에 자전거 수 표시
        markerView.stop_marker.text = StopMarkerItem.bikeNum.toString()

        //자전거 수 별 다른 아이콘 이미지 설정, 자전거가 많을 수록 zIndex 값을 높게 설정해서 우선적으로 보이게 함
        if(StopMarkerItem.bikeNum == 0) {
            markerView.stop_marker.setBackgroundResource(R.drawable.ic_marker_zero)
        } else if (StopMarkerItem.bikeNum < 10){
            markerView.stop_marker.setBackgroundResource(R.drawable.ic_marker_low)
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