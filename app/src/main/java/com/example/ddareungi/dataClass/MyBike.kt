package com.example.a190306app

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/*
* stationId: 대여소 id
* stationName: 대여소 이름
* rackTotCnt: 거치대 갯수
* parkingBikeTotCnt: 주차된 자전거 총 갯수(연결거치 포함)
* shared: 거치율( parkingBikeTotCnt/rackTotCnt)
* stationLatitude : 위도
* stationLongitude : 경도
* */

data class MyBike(val stationId:String, val stationName:String, val rackTotCnt:Int, val parkingBikeTotCnt:Int,
                  val shared:Int, val stationLatitude:Double, val stationLongitude:Double, var bookmarked:Int ): ClusterItem {
    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return LatLng(stationLatitude, stationLongitude)
    }
}
