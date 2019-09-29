package com.example.ddareungi.data.source

interface DataSource {

    interface LoadDataCallback {
        fun onDataLoaded()

        fun onNetworkNotAvailable()
    }

    interface ApiListener {
        fun onDataLoaded(dataFilterType: DataFilterType)

        fun onFailure(dataFilterType: DataFilterType)
    }
    fun initRepository(callback: LoadDataCallback)
    fun initRepositoryForBookmarkFrag(callback: LoadDataCallback)
    fun refreshBike(callback: LoadDataCallback)
    fun initWeather(isGps:Boolean,callback: LoadDataCallback)
    fun refreshWeather(callback: LoadDataCallback)
}