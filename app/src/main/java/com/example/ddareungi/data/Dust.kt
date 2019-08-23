package com.example.ddareungi.data

import android.os.AsyncTask
import com.example.ddareungi.NetworkTask
import com.example.ddareungi.data.source.DataFilterType
import com.example.ddareungi.data.source.DataSource

/*
* pm10: 미세먼지 농도
* pm25: 초미세먼지 농도
* idex_nm: 통합대기환경등급 ex. 좋음, 보통 , ...
* idex_mval: 통합대기환경지수
* */

data class Dust(var pm10:Double, var pm25:Double, var idex_nm:String,var idex_mvl:Double, var code: String, var locality:String) {
    companion object {
        const val baseUrl = "http://openapi.seoul.go.kr:8088/6d71556a42627a7437377549426e67/json/RealtimeCityAir/1/1/"

        fun loadDust(dust: Dust, callback: DataSource.ApiListener) {
            val dustTask = NetworkTask(dust, DataFilterType.DUST, baseUrl + dust.code + "/" + dust.locality, callback)
            dustTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }
}