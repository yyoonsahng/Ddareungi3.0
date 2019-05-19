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

        stopMarkerOptionList.add(StopMarkerItem(0, "정류소1", KONKUK_UNIV, 10))
        stopMarkerOptionList.add(StopMarkerItem(1, "정류소2", LatLng(37.5405, 127.07), 8))
        stopMarkerOptionList.add(StopMarkerItem(2, "정류소3", LatLng(37.5396, 127.07), 0))

        for(i in stopMarkerOptionList.indices) {
            val marker = createMarker(stopMarkerOptionList[i])
            stopMarkerList.add(mMap.addMarker(marker))
            stopMarkerList[i].tag = stopMarkerOptionList[i]
        }

    }

    fun createMarker(StopMarkerItem: StopMarkerItem): MarkerOptions {
        val marker = MarkerOptions()
        marker.position(StopMarkerItem.latLng)
        val markerView = LayoutInflater.from(mContext).inflate(R.layout.custom_marker, null)
        markerView.stop_marker.text = StopMarkerItem.bikeNum.toString()
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