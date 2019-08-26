package com.example.ddareungi.data.source

import android.content.Context
import android.util.Log
import com.example.ddareungi.data.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

class DataRepository(
    val bikeList: ArrayList<Bike>, val toiletList: ArrayList<Toilet>, val parkList: ArrayList<Park>,
    var dust: Dust, var weather: Weather, val bookmarkDataBase: BookmarkDatabase
) : DataSource {

    var isReposInit = false
    var isWeatherInit=false
    var isBikeInit=false
    var isDustInit=false
    var isParkInit=false
    var isToiletInit=false

    override fun refreshWeather(callback: DataSource.LoadDataCallback) {
        class ApiListener : DataSource.ApiListener {
            private var networkState = true

            override fun onDataLoaded(dataFilterType: DataFilterType) {
                isWeatherInit = true
                if(isBikeInit && isToiletInit && isParkInit && isWeatherInit && isDustInit) {
                    isReposInit = true
                    callback.onDataLoaded()
                }
            }
            override fun onFailure(dataFilterType: DataFilterType) {
                //요청한 데이터 중 하나라도 실패하면 연결 실패로 간주
                //onFailure 호출이 여러번 되도 onNetworkNotAvailable은 한번만 호출
                if(networkState) {
                    networkState = false
                    callback.onNetworkNotAvailable()
                }
            }
        }
        val apiListener = ApiListener()

        Weather.loadWeather(weather, apiListener)
    }

    //외부로부터 받아와야 하는 데이터 모두 불러올 때
    override fun initRepository(callback: DataSource.LoadDataCallback) {
        class ApiListener: DataSource.ApiListener {
//            private var bikeLoaded = false
//            private var toiletLoaded = false
//            private var parkLoaded = false
//            private var weatherLoaded = false
//            private var dustLoaded = false
            private var bikeCallCount = 0
            private var toiletCallCount = 0
            private var networkState = true

            //각각 데이터가 성공적으로 불러와 질 때마다 callback 실행
            override fun onDataLoaded(dataFilterType: DataFilterType) {
                when(dataFilterType) {
                    DataFilterType.PARK ->  isParkInit= true
                    DataFilterType.DUST -> isDustInit = true
                    DataFilterType.BIKE -> {
                        bikeCallCount--
                        if(bikeCallCount == 0)  isBikeInit = true
                    }
                    DataFilterType.TOILET -> {
                        toiletCallCount--
                        if(toiletCallCount == 0) isToiletInit = true
                    }
                    DataFilterType.BIKE_NUM -> bikeCallCount++
                    DataFilterType.TOILET_NUM -> toiletCallCount++
                }
                
                if(isBikeInit && isToiletInit && isParkInit && isWeatherInit && isDustInit) {
                    isReposInit = true
                    callback.onDataLoaded()
                }
            }

            override fun onFailure(dataFilterType: DataFilterType) {
                //요청한 데이터 중 하나라도 실패하면 연결 실패로 간주
                //onFailure 호출이 여러번 되도 onNetworkNotAvailable은 한번만 호출
                if(networkState) {
                    networkState = false
                    callback.onNetworkNotAvailable()
                }
            }
        }
        val apiListener = ApiListener()

        Bike.loadBike(bikeList, apiListener)
        Park.loadPark(parkList, apiListener)
        Toilet.loadToilet(toiletList, apiListener)
        Dust.loadDust(dust, apiListener)
    }

    //MapFragment에서 자전거 관련 정보 받아와야 할 때
    override fun refreshBike(callback: DataSource.LoadDataCallback) {
        class ApiListener : DataSource.ApiListener {
            private var bikeLoaded = false
            private var bikeCallCount = 0
            private var networkState = true

            override fun onDataLoaded(dataFilterType: DataFilterType) {
                if (dataFilterType == DataFilterType.BIKE) {
                    bikeCallCount--
                    if(bikeCallCount == 0)  bikeLoaded = true
                }
                else if (dataFilterType == DataFilterType.BIKE_NUM) bikeCallCount++

                if(bikeLoaded)
                    callback.onDataLoaded()
            }

            override fun onFailure(dataFilterType: DataFilterType) {
                //요청한 데이터 중 하나라도 실패하면 연결 실패로 간주
                //onFailure 호출이 여러번 되도 onNetworkNotAvailable은 한번만 호출
                if(networkState) {
                    networkState = false
                    callback.onNetworkNotAvailable()
                }
            }
        }
        val apiListener = ApiListener()

        Bike.loadBike(bikeList, apiListener)
    }



    //BookmarkFragment에서 날씨, 자전거 관련 정보 다시 받아올 때
    override fun refreshForBookmarkFrag(callback: DataSource.LoadDataCallback) {
        class ApiListener : DataSource.ApiListener {
            private var bikeLoaded = false
            private var bikeCallCount = 0
            private var weatherLoaded = true
            private var dustLoaded = false
            private var networkState = true

            override fun onDataLoaded(dataFilterType: DataFilterType) {
                if (dataFilterType == DataFilterType.BIKE) {
                    bikeCallCount--
                    if(bikeCallCount == 0)  bikeLoaded = true
                }
                else if (dataFilterType == DataFilterType.BIKE_NUM) bikeCallCount++
                else if (dataFilterType == DataFilterType.WEATHER) weatherLoaded = true
                else if (dataFilterType == DataFilterType.DUST) dustLoaded = true

                if(weatherLoaded && dustLoaded && bikeLoaded)
                    callback.onDataLoaded()
            }

            override fun onFailure(dataFilterType: DataFilterType) {
                //요청한 데이터 중 하나라도 실패하면 연결 실패로 간주
                //onFailure 호출이 여러번 되도 onNetworkNotAvailable은 한번만 호출
                if(networkState) {
                    networkState = false
                    callback.onNetworkNotAvailable()
                }
            }
        }
        val apiListener = ApiListener()

        Bike.loadBike(bikeList, apiListener)
        Dust.loadDust(dust, apiListener)
        //Weather.loadWeather(weather, apiListener)
    }

    //날씨 api / 구에 따른 지역코드 파싱
    private fun loadWeatherFile(weatherFile: Scanner): String {
        var result = ""
        while (weatherFile.hasNextLine()) {
            val line = weatherFile.nextLine()
            result += line
        }
        return result
    }

    //날씨, 미세먼지 api
    //dust, weather 객체에 각각 지역 정보 업데이트, 지역 코드 업데이트
    fun initLocationCode(weatherFile: Scanner, dustFile: Scanner, locality: String, neighborhood: String) {
        dust.locality = locality
        weather.neighborhood = neighborhood

        val wResult = loadWeatherFile(weatherFile)
        val weatherArr = JSONArray(wResult)

        val dResult = loadWeatherFile(dustFile)
        val dustArr = JSONArray(dResult)

        for(i in 0 until weatherArr.length()) {
            val wValue = weatherArr.getJSONObject(i).getString("value")
            if(locality == wValue) {
                weather.code = weatherArr.getJSONObject(i).getString("code")
                dust.code = dustArr.getJSONObject(i).getString("code")
                break
            }
        }
    }

    fun findBookmarkInDatabase(name: String): Boolean {
        return bookmarkDataBase.findOffice(name) != 0
    }

    fun deleteBookmarkInDatabase(position: Int) {
        val name = bookmarkDataBase.findOfficeWithRow(position)
        deleteBookmarkInDatabase(name)
    }

    fun updateBookmarkInDatabase(name: String) {
        if(findBookmarkInDatabase(name)) {
            addBookmarkToDatabase(name)
        } else {
            deleteBookmarkInDatabase(name)
        }
    }

    fun deleteBookmarkInDatabase(name: String) {
        bookmarkDataBase.deleteUser(Bookmark("", 0, 1, name))
    }

    fun addBookmarkToDatabase(name: String) {
        bookmarkDataBase.addUser(Bookmark(name, 0, 1, ""))
    }

    interface ToiletNumApiListener: DataSource.ApiListener {
        override fun onDataLoaded(dataFilterType: DataFilterType)

        override fun onFailure(dataFilterType: DataFilterType)

        fun onDataLoaded(dataFilterType: DataFilterType, toiletNum: Int)
    }

    interface BikeNumApiListener: DataSource.ApiListener {

        override fun onDataLoaded(dataFilterType: DataFilterType)

        override fun onFailure(dataFilterType: DataFilterType)

        fun onDataLoaded(dataFilterType: DataFilterType, bikeNum: Int)
    }

    interface LocationCodeApiListener: DataSource.ApiListener {
        override fun onDataLoaded(dataFilterType: DataFilterType)

        override fun onFailure(dataFilterType: DataFilterType)

        fun onDataLoaded(dataFilterType: DataFilterType, locationCode: String)
    }

    companion object {
        fun newInstance(context: Context): DataRepository {
            val dust = Dust(0.0, 0.0, "", 0.0, "", "광진구")
            val weather = Weather(0, 0, 0, "", 0, "", "화양동")

            return DataRepository(ArrayList(), ArrayList(), ArrayList(), dust, weather, BookmarkDatabase.getInstance(context))
        }
    }
}