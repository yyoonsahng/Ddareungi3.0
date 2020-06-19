package com.example.ddareungi.data

data class BikeStation(
    val stationId: String, // 정류소 Id
    val stationName: String, // 정류소 이름 (정류소 번호 + 이름 형식, ex) 1. XXX )
    val rackTotCnt: Int = 0, // 거치대 개수
    var parkingBikeTotCnt: Int = 0,  // 주차된 자전거 총 수
    val stationLatitude: Double = 0.0, val stationLongitude: Double = 0.0, // 정류소 위,경도
    var bookmarked:Boolean = false // 사용자 북마크 여부
)