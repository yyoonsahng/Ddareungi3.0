package com.example.ddareungi.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.ddareungi.BasicApp
import com.example.ddareungi.DataRepository
import com.example.ddareungi.R
import com.example.ddareungi.data.Weather
import com.example.ddareungi.data.Result.Success
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.*

class WeatherViewModel(private val mRepository: DataRepository, private val mApplication: Application)
    : AndroidViewModel(mApplication) {

    private val mWeather = MutableLiveData<Weather>().apply {
        value = Weather(0, 0, 0, "", 0, "", "")
    }
    val weather: LiveData<Weather> = mWeather

    private val mNeighborhood = MutableLiveData<String>().apply { value = "" }
    val neighborhood: LiveData<String> = mNeighborhood

    private val mLocality = MutableLiveData<String>().apply { value = "" }
    val locality: LiveData<String> = mLocality

    // 지역 구 코드
    private var mWeatherCode: String = ""
    private var mDustCode: String = ""

    private val mDust = MutableLiveData<String>().apply { value = "" }
    val dust: LiveData<String> = mDust

    // 현재 시간 온도
    private val mTemp = MutableLiveData<Int>().apply { value = 0 }
    val temp: LiveData<Int> = mTemp

    // 날씨 한국어 표현 문자열
    private val mWfKor = MutableLiveData<String>().apply { value = "" }
    val wfKor: LiveData<String> = mWfKor

    private val mSky = MutableLiveData<Int>().apply { value = 0 }
    val sky: LiveData<Int> = mSky

    // 강수 상태 코드
    private val mPty = MutableLiveData<Int>().apply { value = 0 }
    val pty: LiveData<Int> = mPty

    // 강수 상태 코드
    private val mPop = MutableLiveData<Int>().apply { value = 0 }
    val pop: LiveData<Int> = mPop

    private val mLoadSucceed = MutableLiveData<Boolean>().apply { value = false }
    val loadSucceed: LiveData<Boolean> = mLoadSucceed

    private val mDataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = mDataLoading


    /**
     *  현재 유저 주소 중 구([locality]), 동([neighborhood])에 해당하는 날씨, 미세먼지 지역 코드
     */
    private fun initLocationCode(locality: String, neighborhood: String) {
        mLocality.value = locality
        mNeighborhood.value = neighborhood

        val wResult = loadRawFile("weather")
        val dResult = loadRawFile("dust")

        val weatherArr = JSONArray(wResult)
        val dustArr = JSONArray(dResult)

        for(i in 0 until weatherArr.length()) {
            val wValue = weatherArr.getJSONObject(i).getString("value")
            if(locality == wValue) {
                mWeatherCode = weatherArr.getJSONObject(i).getString("code")
                mDustCode = dustArr.getJSONObject(i).getString("code")
                break
            }
        }
    }

    fun loadWeather(locality: String, neighborhood: String) {
        mDataLoading.value = true

        initLocationCode(locality, neighborhood)

        viewModelScope.launch {
            val zoneCodeResult = mRepository.getZoneCode(mWeatherCode, mNeighborhood.value!!)
            if (zoneCodeResult is Success) {
                val weatherResult = mRepository.getWeatherState(zoneCodeResult.data)
                val dustResult = mRepository.getDustState(mDustCode, mLocality.value!!)

                if (weatherResult is Success && dustResult is Success) {
                    mTemp.value = weatherResult.data.temp
                    mSky.value = weatherResult.data.sky
                    mPty.value = weatherResult.data.pty
                    mWfKor.value = weatherResult.data.wfKor
                    mPop.value = weatherResult.data.pop

                    mDust.value = dustResult.data

                    mLoadSucceed.value = true
                    Log.i(this.javaClass.name, "succeeded at getting weather state, " + loadSucceed.value)
                }
                else if (weatherResult is Success){
                    mTemp.value = weatherResult.data.temp
                    mSky.value = weatherResult.data.sky
                    mPty.value = weatherResult.data.pty
                    mWfKor.value = weatherResult.data.wfKor
                    mPop.value = weatherResult.data.pop

                    mLoadSucceed.value = false
                    Log.i(this.javaClass.name, "failed at getting weather state, " + loadSucceed.value)
                }
                else if (dustResult is Success) {
                    mDust.value = dustResult.data

                    mLoadSucceed.value = false
                    Log.i(this.javaClass.name, "failed at getting dust state, " + loadSucceed.value)
                }
            }
            else {
                mLoadSucceed.value = false
                Log.i(this.javaClass.name, "failed at getting zone code, " + loadSucceed.value)
            }
            mDataLoading.value = false
        }
    }

    private fun loadRawFile(type: String): String {
        var scanner: Scanner? = null

        if(type == "weather") {
            scanner = Scanner(mApplication.resources.openRawResource(R.raw.weather))
        } else if(type == "dust") {
            scanner = Scanner(mApplication.resources.openRawResource(R.raw.dust))
        }
        var result = ""
        while (scanner!!.hasNextLine()) {
            val line = scanner.nextLine()
            result += line
        }
        return result
    }

    companion object {
        class Factory(application: Application)
            : ViewModelProvider.NewInstanceFactory() {

            private val mRepository = (application as BasicApp).getDataRepository()

            private val mApplication = application

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return WeatherViewModel(mRepository, mApplication) as T
            }
        }
    }
}