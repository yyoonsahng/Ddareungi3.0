package com.example.ddareungi

import android.os.AsyncTask
import android.util.Log
import com.example.ddareungi.util.RequestHttpURLConnection
import com.example.ddareungi.data.*
import com.example.ddareungi.data.source.DataFilterType
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class NetworkTask(val dataType: DataFilterType, var url: String, val callback: DataSource.ApiListener)
    : AsyncTask<Unit, Unit, String>() {
    lateinit var bikeList: ArrayList<Bike>
    lateinit var parkList: ArrayList<Park>
    lateinit var toiletList: ArrayList<Toilet>
    lateinit var dust: Dust
    lateinit var weather: Weather

    constructor(dataList: ArrayList<*>, dataType: DataFilterType, url: String, callback: DataSource.ApiListener)
            : this(dataType, url, callback) {
        if (dataType == DataFilterType.BIKE) bikeList = dataList as ArrayList<Bike>
        else if (dataType == DataFilterType.PARK) parkList = dataList as ArrayList<Park>
        else if (dataType == DataFilterType.TOILET) toiletList = dataList as ArrayList<Toilet>
    }

    constructor(dust: Dust, dataType: DataFilterType, url: String, callback: DataSource.ApiListener): this(dataType, url, callback) {
        this.dust = dust
    }

    constructor(weather: Weather, dataType: DataFilterType, url: String, callback: DataSource.ApiListener): this(dataType, url, callback) {
        this.weather = weather
    }

    override fun doInBackground(vararg params: Unit?): String {
        return RequestHttpURLConnection().request(url)
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        if (result == "ERROR") {
            callback.onFailure(dataType)
        } else {
            when (dataType) {
                DataFilterType.BIKE -> {
                    try {
                        val jsonArray: JSONArray =
                            JSONObject(result).getJSONObject("rentBikeStatus").getJSONArray("row")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            val stationId: String = jsonObj.optString("stationId")
                            val stationName: String = jsonObj.optString("stationName")
                            val rackTotCnt: Int = jsonObj.optInt("rackTotCnt")
                            val parkingBikeTotCnt: Int = jsonObj.optInt("parkingBikeTotCnt")
                            val shared: Int = jsonObj.optInt("shared")
                            val stationLatitude: Double = jsonObj.optDouble("stationLatitude")
                            val stationLongitude: Double = jsonObj.optDouble("stationLongitude")
                            val bookmarked: Int = 0
                            bikeList.add(Bike(stationId, stationName, rackTotCnt, parkingBikeTotCnt, shared, stationLatitude, stationLongitude, bookmarked))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                DataFilterType.PARK -> {
                    try {
                        val jsonArray: JSONArray =
                            JSONObject(result).getJSONObject("SearchParkInfoService").getJSONArray("row")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            val id: Int = jsonObj.optInt("P_IDX")
                            val name: String = jsonObj.optString("P_PARK")
                            val zone: String = jsonObj.optString("P_ZONE")
                            val addr: String = jsonObj.optString("P_ADDR")
                            val g_longitude: Double = jsonObj.optDouble("G_LONGITUDE")
                            val g_latitude: Double = jsonObj.optDouble("G_LATITUDE")
                            val longitude: Double = jsonObj.optDouble("LONGITUDE")
                            val latitude: Double = jsonObj.optDouble("LATITUDE")
                            parkList.add(Park(id, name, zone, addr, g_longitude, g_latitude, longitude, latitude))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                DataFilterType.TOILET -> {
                    try {
                        val jsonArray: JSONArray =
                            JSONObject(result).getJSONObject("SearchPublicToiletPOIService").getJSONArray("row")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            val id: String = jsonObj.optString("POI_ID")
                            val fName: String = jsonObj.optString("FNAME")
                            val aName: String = jsonObj.optString("ANAME")
                            val c_x: Double = jsonObj.optDouble("CENTER_X1")
                            val c_y: Double = jsonObj.optDouble("CENTER_Y1")
                            val wgs84_x: Double = jsonObj.optDouble("X_WGS84")
                            val wgs84_y: Double = jsonObj.optDouble("Y_WGS84")

                            toiletList.add(Toilet(id, fName, aName, c_x, c_y, wgs84_x, wgs84_y))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                DataFilterType.DUST -> {
                    try {
                        val jsonArray: JSONArray =
                            JSONObject(result).getJSONObject("RealtimeCityAir").getJSONArray("row")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            dust.pm10 = jsonObj.optDouble("PM10")
                            dust.pm25 = jsonObj.optDouble("PM25")
                            dust.idex_nm = jsonObj.optString("IDEX_NM")
                            dust.idex_mvl = jsonObj.optDouble("IDEX_MVL")
                            dust.locality = jsonObj.optString("MSRSTE_NM")
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                DataFilterType.LOCATION_CODE -> {
                    try {
                        val jsonArray = JSONArray(result)
                        var code = ""
                        for (i in 0 until jsonArray.length()) {
                            val value = jsonArray.getJSONObject(i).getString("value")
                            if (weather.neighborhood == value) {
                                code = jsonArray.getJSONObject(i).getString("code")
                                break
                            }
                        }
                        (callback as DataRepository.LocationCodeApiListener).onDataLoaded(dataType, code)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                DataFilterType.WEATHER -> {
                    var factory: XmlPullParserFactory?
                    factory = XmlPullParserFactory.newInstance()
                    factory!!.isNamespaceAware = true
                    val xpp = factory!!.newPullParser()
                    xpp.setInput(StringReader(result))
                    var dataSet = false
                    var tag_name: String? = null
                    var eventType = xpp.getEventType()
                    var isEnd = false
                    while (eventType != XmlPullParser.END_DOCUMENT && !isEnd) {
                        when (eventType) {
                            XmlPullParser.START_DOCUMENT -> {
                            }
                            XmlPullParser.START_TAG -> {
                                tag_name = xpp.getName()
                                if (tag_name == "temp" || tag_name == "sky" || tag_name == "pty" || tag_name == "wfKor" || tag_name == "pop") {
                                    dataSet = true
                                }
                            }
                            XmlPullParser.TEXT -> {
                                if (dataSet) {
                                    val data = xpp.getText()
                                    when (tag_name) {
                                        "temp" -> weather.temp = data.toDouble().toInt()
                                        "sky" -> weather.sky = data.toInt()
                                        "pty" -> weather.pty = data.toInt()
                                        "wfKor" -> weather.wfKor = data
                                        "pop" -> {
                                            weather.pop = data.toInt()
                                            isEnd = true
                                        }
                                    }
                                }
                                dataSet = false
                            }
                            XmlPullParser.END_TAG -> {
                            }
                        }
                        eventType = xpp.next()
                    }
                }

                DataFilterType.BIKE_NUM -> {
                    try {
                        val jsonObj = JSONObject(result).getJSONObject("rentBikeStatus")
                        val bikeNum = jsonObj.optInt("list_total_count")
                        val bStatus = jsonObj.getJSONObject("RESULT").optString("CODE")
                        if (bStatus != "INFO-000")
                            callback.onFailure(dataType)
                        else {
                            (callback as DataRepository.BikeNumApiListener).onDataLoaded(dataType, bikeNum)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        callback.onFailure(dataType)
                    }
                }

                DataFilterType.TOILET_NUM -> {
                    try {
                        val jsonObject = JSONObject(result).getJSONObject("SearchPublicToiletPOIService")
                        val listTotalCount = jsonObject.optInt("list_total_count")
                        (callback as DataRepository.ToiletNumApiListener).onDataLoaded(dataType, listTotalCount)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            callback.onDataLoaded(dataType)
        }
    }
}