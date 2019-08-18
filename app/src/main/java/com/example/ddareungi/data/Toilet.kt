package com.example.ddareungi.data

import android.os.AsyncTask
import com.example.ddareungi.NetworkTask
import com.example.ddareungi.data.source.DataFilterType
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource


/*
* id: 공중화장실 id
* fName: 대명칭 ex. 건물이름
* aName: 중명칭 ex. 민간개방화장실
* c_x, c_y: 중앙좌표
* wgs84_x,wgs84_y: WGS84 좌표
* */
data class Toilet(val id: String, val fName: String, val aName: String, val c_x: Double, val c_y: Double, val wgs84_x: Double, val wgs84_y: Double) {
    companion object {
        const val baseUrl = "http://openapi.seoul.go.kr:8088/694b534943627a7434307364586868/json/SearchPublicToiletPOIService/"

        fun loadToilet(toiletList: ArrayList<Toilet>, callback: DataSource.ApiListener) {
            var url = baseUrl + "1/5"
            val toiletNumTask = NetworkTask(DataFilterType.TOILET_NUM, url, object : DataRepository.ToiletNumApiListener {
                    override fun onDataLoaded(dataFilterType: DataFilterType, toiletNum: Int) {
                        val toiletCallCount = toiletNum / 1000

                        for (i in 0..toiletCallCount) {
                            callback.onDataLoaded(DataFilterType.TOILET_NUM)

                            val startIdx = i * 1000 + 1
                            val endIdx = (i + 1) * 1000
                            url = baseUrl + startIdx.toString() + "/" + endIdx.toString()
                            val toiletTask =
                                NetworkTask(toiletList, DataFilterType.TOILET, url, callback)
                            toiletTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                        }
                    }

                    override fun onFailure(dataFilterType: DataFilterType) {
                        callback.onFailure(dataFilterType)
                    }

                    override fun onDataLoaded(dataFilterType: DataFilterType) {}
                })

            toiletNumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }
}