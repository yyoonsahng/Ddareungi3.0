package com.example.ddareungi

import android.util.Log
import com.example.ddareungi.data.*
import com.example.ddareungi.data.dao.BookmarkStationDao
import com.example.ddareungi.utils.NetworkUtils
import com.example.ddareungi.utils.parse.BikeStationJsonUtils
import com.example.ddareungi.utils.parse.ParkJsonUtils
import com.example.ddareungi.utils.parse.WeatherResultParseUtils
import com.example.ddareungi.data.Result.Success
import com.example.ddareungi.data.Result.Error
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException


class DataRepository(private val mBookmarkStationDao: BookmarkStationDao) {

    /**
     * 따릉이 정류소 실시간 정보를 가져오기 위한 메소드
     * 정류소 별 정보를 파싱한 [BikeStation] 객체 [List]를 리턴
     */
    suspend fun getBikeStations(): Result<List<BikeStation>> {
        val baseUrl = "https://www.bikeseoul.com/app/station/getStationRealtimeStatus.do"
        val querys = hashMapOf("stationGrpSeq" to "ALL")

        val url = NetworkUtils.buildUrl(baseUrl, querys)
        val okHttpClient = OkHttpClient()

        return withContext(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.Default) {
                        okHttpClient.newCall(Request.Builder().url(url).build()).execute()
                    }

                if (!response.isSuccessful) {
                    Log.e("NETWORK_ERROR", "Error while getting bike station info")
                    return@withContext Error(Exception("Failed at loading bike stations"))
                }
                else {
                    val bikeStationList =
                        BikeStationJsonUtils.parseBikeStationList(response.body!!.string())

                    return@withContext Success(bikeStationList)
                }
            }
            catch (e: IOException) {
                Log.e("NETWORK_ERROR", "Error while getting bike station info : $e")
                return@withContext Error(Exception(e))
            }
        }
    }

    suspend fun getBookmarkStations(): Result<List<BookmarkStation>> = withContext(Dispatchers.IO){
        return@withContext try {
            Success(mBookmarkStationDao.getAllStations())
        } catch (e: Exception) {
            Error(e)
        }
    }

    suspend fun addBookmarkStation(stationId: String, stationName: String) {
        mBookmarkStationDao.insertStation(BookmarkStation(stationId, stationName))
    }

    suspend fun deleteBookmarkStation(stationId: String) {
        mBookmarkStationDao.deleteStation(stationId)
    }

    private fun getBookmarkStation(stationId: String): BookmarkStation? {
        return mBookmarkStationDao.getStationById(stationId)
    }

    /**
     * 지역 구([code])와 동([neighborhood])에 해당하는 zone code를 가져오는 메소드
     */
    suspend fun getZoneCode(code: String, neighborhood: String): Result<String> {
        val url = "https://www.kma.go.kr/DFSROOT/POINT/DATA/leaf.${code}.json.txt"

        val okHttpClient = OkHttpClient()

        return withContext(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.Default) {
                    okHttpClient.newCall(Request.Builder().url(url).build()).execute()
                }
                if(!response.isSuccessful) {
                    Log.e(this.javaClass.name, "Error while getting zone code")
                    return@withContext Error(Exception("Failed at loading zone code"))
                } else {
                    val zoneCode
                            = WeatherResultParseUtils.parseZoneCode(response.body!!.bytes(), neighborhood)

                    return@withContext Success(zoneCode)
                }
            } catch (e: IOException) {
                Log.e(this.javaClass.name, "Error while getting zone code : $e")
                return@withContext Error(Exception(e))
            }
        }
    }

    /**
     * [zoneCode]에 해당하는 날씨 정보를 가져오기 위한 메소드
     */
    suspend fun getWeatherState(zoneCode: String): Result<Weather> {
        val baseUrl = "https://www.kma.go.kr/wid/queryDFSRSS.jsp"
        val querys = hashMapOf("zone" to zoneCode)

        val url = NetworkUtils.buildUrl(baseUrl, querys)
        val okHttpClient = OkHttpClient()

        return withContext(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.Default) {
                    okHttpClient.newCall(Request.Builder().url(url).build()).execute()
                }
                if(!response.isSuccessful) {
                    Log.i(this.javaClass.name, "Error while getting weather state")
                    return@withContext Error(Exception("Failed at loading weather state"))
                }
                else {
                    val weather =
                        WeatherResultParseUtils.parseWeatherStateXml(response.body!!.string())

                    return@withContext Success(weather)
                }
            }
            catch (e: IOException) {
                Log.e(this.javaClass.name, "Error while getting weather state : $e")
                return@withContext Error(Exception(e))
            }
        }
    }

    suspend fun getDustState(code: String, locality: String): Result<String> {
        val url = "http://openapi.seoul.go.kr:8088/6d71556a42627a7437377549426e67/json/RealtimeCityAir/1/1/${code}/${locality}"
        val okHttpClient = OkHttpClient()

        return withContext(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.Default) {
                    okHttpClient.newCall(Request.Builder().url(url).build()).execute()
                }
                if(!response.isSuccessful) {
                    Log.i(this.javaClass.name, "Error while getting dust state")
                    return@withContext Error(Exception("Failed at loading dust state"))
                }
                else {
                    val dust = WeatherResultParseUtils.parseDustJson(response.body!!.string())

                    return@withContext Success(dust)
                }
            }
            catch (e: IOException) {
                Log.e("DataRepository", "Error while getting dust state : $e")
                return@withContext Error(Exception(e))
            }
        }
    }

    suspend fun getParks(): Result<List<Park>> {
        val url = "http://openapi.seoul.go.kr:8088/527a4a4b47627a74363558734a7658/json/SearchParkInfoService/1/132/"
        val okHttpClient = OkHttpClient()

        return withContext(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.Default) {
                    okHttpClient.newCall(Request.Builder().url(url).build()).execute()
                }
                if(!response.isSuccessful) {
                    Log.i("DataRepositry", "Error while getting parks data")
                    return@withContext Error(Exception("Failed at getting parks data"))
                }
                else {
                    val parkList = ParkJsonUtils.parseParkList(response.body!!.string())

                    return@withContext Success(parkList)
                }
            }
            catch (e: IOException) {
                Log.e("DataRepository", "Error while getting parks data : $e")
                return@withContext Error(Exception(e))
            }
        }
    }

    companion object {
        private var sInstance: DataRepository? = null

        fun getInstance(bookmarkStationDao: BookmarkStationDao): DataRepository? =
            sInstance ?: synchronized(this) {
                sInstance ?: DataRepository(bookmarkStationDao).also { sInstance = it }
            }
    }
}