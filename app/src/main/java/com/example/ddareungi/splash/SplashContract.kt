package com.example.ddareungi.splash

import com.example.ddareungi.data.source.DataRepository
import java.util.*

interface SplashContract {

    interface View {

        fun showBookmarkActivity(dataRepository: DataRepository)

        //gps 정보로 현재 구, 동 위치 구함
        fun initLocation(isGps: Boolean,dataRepository: DataRepository)
    }

    interface Presenter {

        fun initDataRepository()
        fun initWeatherRepository(isGps:Boolean)
        //dataRepos에 날씨, 미세먼지 지역구 코드 raw파일, 현재 구, 동 업데이트
        fun processLocation(locality: String, neighborhood: String, weatherFile: Scanner, dustFile: Scanner)
    }
}
