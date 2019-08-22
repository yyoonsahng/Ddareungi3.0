package com.example.ddareungi.data

import android.os.AsyncTask
import com.example.ddareungi.NetworkTask
import com.example.ddareungi.data.source.DataFilterType
import com.example.ddareungi.data.source.DataSource

/*
* id: 공원 id
* name: 공원 이름
* zone: 위치한 지역구
* addr: 공원 주소
* g_longitude, g_latitude: X, Y좌표(GRS80TM)
* longitude, latitude: X, Y좌표(WGS84)
* */

data class Park(val id:Int, val name:String,val zone:String, val addr:String, val g_longitude: Double, val g_latitude: Double, val longitude:Double, val latitude:Double) {
    companion object {
        const val baseUrl = "http://openapi.seoul.go.kr:8088/527a4a4b47627a74363558734a7658/json/SearchParkInfoService/1/132/"

        fun loadPark(parkList: ArrayList<Park>, callback: DataSource.ApiListener) {
            val parkTask = NetworkTask(parkList, DataFilterType.PARK, baseUrl, callback)
            parkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }
}
