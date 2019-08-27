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

    fun refreshForBookmarkFrag(callback: LoadDataCallback)

    fun refreshBike(callback: LoadDataCallback)
    fun initWeather(callback: LoadDataCallback)
    fun refreshWeather(callback: LoadDataCallback)
}