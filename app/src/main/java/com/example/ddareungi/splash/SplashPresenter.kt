package com.example.ddareungi.splash

import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import java.util.*

class SplashPresenter(val dataRepository: DataRepository, val splashView: SplashContract.View) :
    SplashContract.Presenter {

    override fun initDataRepository() {

        dataRepository.initRepository(object : DataSource.LoadDataCallback {
            override fun onDataLoaded() {
                splashView.showBookmarkActivity(dataRepository)
            }

            override fun onNetworkNotAvailable() {
                splashView.showBookmarkActivity(dataRepository)
            }
        })
    }

    override fun initWeatherRepository() {
        splashView.initLocation(dataRepository)
    }

    override fun processLocation(locality: String, neighborhood: String, weatherFile: Scanner, dustFile: Scanner) {
        dataRepository.initLocationCode(weatherFile, dustFile, locality, neighborhood)
    }


}