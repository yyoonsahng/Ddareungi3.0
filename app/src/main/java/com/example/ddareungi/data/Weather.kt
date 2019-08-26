package com.example.ddareungi.data

import android.os.AsyncTask
import android.util.Log
import com.example.ddareungi.NetworkTask
import com.example.ddareungi.R
import com.example.ddareungi.data.source.DataFilterType
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource

/*
* temp: 현재 시간 온도
* sky: 하늘상태코드(1: 맑음, 2: 구름조금 , 3: 구름 많음, 4: 흐림
* pty: 강수상태코드(0: 없음 1: 비 , 2: 비/눈 , 3: 눈)
* wfKor:날씨 한국어 ex. 흐림, 눈/비, 눈 , 등등등
* pop: 강수 확률 (퍼센트기준임)
* */

data class Weather(var temp: Int, var sky: Int, var pty: Int, var wfKor: String, var pop: Int, var code: String, var neighborhood: String) {
    companion object {
        const val codeUrl = "http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf."
        const val weatherUrl = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone="

        fun loadWeather(weather: Weather, callback: DataSource.ApiListener) {
            val locationTask = NetworkTask(weather, DataFilterType.LOCATION_CODE, codeUrl + weather.code + ".json.txt",
                object : DataRepository.LocationCodeApiListener {

                    override fun onDataLoaded(dataFilterType: DataFilterType, locationCode: String) {
                        if(locationCode != "") {
                            weather.code = locationCode
                        }
                        val url = weatherUrl + weather.code
                        val weatherTask = NetworkTask(weather, DataFilterType.WEATHER, url, callback)

                        weatherTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }

                    override fun onFailure(dataFilterType: DataFilterType) {
                        callback.onFailure(dataFilterType)
                    }

                    override fun onDataLoaded(dataFilterType: DataFilterType) {}
                })
            locationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    fun matchImage(): Int {
        var weatherId: Int
        when (wfKor) {
            "맑음" -> {
                weatherId = R.drawable.ic_sunny
            }
            "구름 조금" -> {
                weatherId = R.drawable.ic_partialy_cloudy
            }
            "구름 많음" -> {
                weatherId = R.drawable.ic_cloudy
            }
            "흐림" -> {
                weatherId = R.drawable.ic_overcast
            }
            "비" -> {
                weatherId = R.drawable.ic_rainy_day
            }
            "눈/비" -> {
                weatherId = R.drawable.ic_sleet
            }
            "눈" -> {
                weatherId = R.drawable.ic_snow
            }
            else -> {
                weatherId = R.drawable.ic_sunny
            }
        }
        return weatherId
    }
}