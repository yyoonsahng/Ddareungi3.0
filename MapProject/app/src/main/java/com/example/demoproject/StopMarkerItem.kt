package com.example.demoproject

import com.google.android.gms.maps.model.LatLng

data class StopMarkerItem(val id: Int = 0, val name: String = "", val latLng: LatLng = LatLng(0.0, 0.0), var bikeNum: Int = 0) {
}