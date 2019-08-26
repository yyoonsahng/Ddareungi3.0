package com.example.ddareungi.data

import android.os.AsyncTask
import com.example.ddareungi.NetworkTask
import com.example.ddareungi.data.source.DataFilterType
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource


/*
* stationId: 대여소 id
* stationName: 대여소 이름
* rackTotCnt: 거치대 갯수
* parkingBikeTotCnt: 주차된 자전거 총 갯수(연결거치 포함)
* shared: 거치율( parkingBikeTotCnt/rackTotCnt)
* stationLatitude : 위도
* stationLongitude : 경도
* */

data class Bike(val stationId:String, val stationName:String, val rackTotCnt:Int, var parkingBikeTotCnt:Int,
                val shared:Int, val stationLatitude:Double, val stationLongitude:Double, var bookmarked:Int) {

    companion object {
        private const val baseUrl = "http://openapi.seoul.go.kr:8088/746c776f61627a7437376b49567a68/json/bikeList/"

        fun newInstance() = Bike("", "", 0, 0, 0, 0.0, 0.0, 0)

        fun loadBike(bikeList: ArrayList<Bike>, callback: DataSource.ApiListener){
            var totalBikeNum = 0
            lateinit var bikeNumCallback: DataRepository.BikeNumApiListener

            class LoadBikeList(var url: String): DataRepository.BikeNumApiListener {
                var bikeCallCount = 1

                override fun onDataLoaded(dataFilterType: DataFilterType, bikeNum: Int) {
                    callback.onDataLoaded(DataFilterType.BIKE_NUM)
                    totalBikeNum += bikeNum

                    if(totalBikeNum % 1000 == 0) {
                        bikeCallCount++
                        val startIdx = (1 + 1000 * (bikeCallCount - 1)).toString()
                        val endIdx = (1000 * bikeCallCount).toString()
                        url = baseUrl + startIdx + "/" + endIdx
                        val bikeNumTask = NetworkTask(DataFilterType.BIKE_NUM, url, bikeNumCallback)
                        bikeNumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    } else {
                        for(i in 0 until bikeCallCount) {
                            val startIdx = (1 + 1000 * i).toString()
                            val endIdx = (1000 * (i + 1)).toString()
                            url = baseUrl + startIdx + "/" + endIdx
                            val bikeTask = NetworkTask(bikeList, DataFilterType.BIKE, url, callback)
                            bikeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                        }
                    }
                }

                override fun onFailure(dataFilterType: DataFilterType) {
                    callback.onFailure(dataFilterType)
                }

                override fun onDataLoaded(dataFilterType: DataFilterType) {}
            }
            val url = baseUrl + "1/1000"
            bikeNumCallback = LoadBikeList(url)
            val bikeNumTask = NetworkTask(DataFilterType.BIKE_NUM, url, bikeNumCallback)
            bikeNumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

    }
}