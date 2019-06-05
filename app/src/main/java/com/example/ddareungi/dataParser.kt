package com.example.ddareungi.dataClass

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/*
* bList: 대여소 객체를 관리하는 컬렉션
* mDust: 미세먼지 정보 객체
* rList: 화장실 객체를 관리하는 컬렉션
* pList: 공원 객체를 관리하는 컬렉션
* mWeather: 날씨 객체
* bikeParse(), dustParse(), restroomParse(), parkParse(), weatherParse() : 각 데이터를 파싱하는 함수
* */
class dataParser(var bList:MutableList<MyBike>,var mDust:MyDust,var rList:MutableList<MyRestroom>,var pList:MutableList<MyPark>, var mWeather:MyWeather){

    enum class Data(val type: Int) {
        BIKE(0),
        DUST(1),
        RESTROOM(2),
        PARK(3),
        WEATHER(4)
    }

    fun parse(type: Int, jsonString: String) {

        when (type) {
            Data.BIKE.type ->bikeParse(jsonString)
            Data.DUST.type ->dustParse(jsonString)
            Data.RESTROOM.type ->restroomParse(jsonString)
            Data.PARK.type ->parkParse(jsonString)
            Data.WEATHER.type->weatherParse(jsonString)
        }
    }
    fun bikeParse(jsonString: String){
        try {
            var jarray: JSONArray = JSONObject(jsonString).getJSONObject("rentBikeStatus").getJSONArray("row")
            for (i in 0..jarray.length()) {
                var jObject = jarray.getJSONObject(i)
                var stationId: String = jObject.optString("stationId")
                var stationName: String = jObject.optString("stationName")
                var rackTotCnt: Int = jObject.optInt("rackTotCnt")
                var parkingBikeTotCnt: Int = jObject.optInt("parkingBikeTotCnt")
                var shared: Int = jObject.optInt("shared")
                var stationLatitude: Double = jObject.optDouble("stationLatitude")
                var stationLongitude: Double = jObject.optDouble("stationLongitude")
                var bookmarked:Int = 0

                bList.add(
                    MyBike(
                        stationId,
                        stationName,
                        rackTotCnt,
                        parkingBikeTotCnt,
                        shared,
                        stationLatitude,
                        stationLongitude,
                        bookmarked
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    fun dustParse(jsonString: String){
        //미세먼지는 위치정보 받아오면, 그 위치가 속한 구를 url에 붙여서 파싱해야하는데, 아직은 없으므로 row로 지정
        try {
            var jarray: JSONArray = JSONObject(jsonString).getJSONObject("RealtimeCityAir").getJSONArray("row")
            for (i in 0..jarray.length()) {
                var jObject = jarray.getJSONObject(i)
                mDust.pm10 = jObject.optDouble("PM10")
                mDust.pm25= jObject.optDouble("PM25")
                mDust.idex_nm = jObject.optString("IDEX_NM")
                mDust.idex_mvl = jObject.optDouble("IDEX_MVL")
                mDust.localty=jObject.optString("MSRSTE_NM")

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }
    fun restroomParse(jsonString: String){
        try {
            var jarray: JSONArray = JSONObject(jsonString).getJSONObject("SearchPublicToiletPOIService").getJSONArray("row")
            for (i in 0..jarray.length()) {
                var jObject = jarray.getJSONObject(i)
                var id: Int = jObject.optInt("POI_ID")
                var fName: String = jObject.optString("FNAME")
                var aName: String = jObject.optString("ANAME")
                var c_x: Double = jObject.optDouble("CENTER_X1")
                var c_y: Double = jObject.optDouble("CENTER_Y1")
                var wgs84_x: Double = jObject.optDouble("X_WGS84")
                var wgs84_y: Double = jObject.optDouble("Y_WGS84")
                rList.add(
                    MyRestroom(id,fName,aName,c_x,c_y,wgs84_x,wgs84_y)
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    fun parkParse(jsonString: String){
        // 공원은 제공하는 정보가 많아서, 다같이 필요한 데이터 확인할 필요 있을 듯
        try {
            var jarray: JSONArray = JSONObject(jsonString).getJSONObject("SearchParkInfoService").getJSONArray("row")
            for (i in 0..jarray.length()) {
                var jObject = jarray.getJSONObject(i)
                var id: Int = jObject.optInt("P_IDX")
                var name: String = jObject.optString("P_PARK")
                var zone: String = jObject.optString("P_ZONE")
                var addr: String = jObject.optString("P_ADDR")
                var g_longitude: Double = jObject.optDouble("G_LONGITUDE")
                var g_latitude: Double = jObject.optDouble("G_LATITUDE")
                var longitude: Double = jObject.optDouble("LONGITUDE")
                var latitude: Double = jObject.optDouble("LATITUDE")
                pList.add(
                    MyPark(id,name,zone,addr,g_longitude,g_latitude,longitude,latitude)
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()

        }
    }
    fun weatherParse(xmlString: String){
        var factory: XmlPullParserFactory? = null
        factory = XmlPullParserFactory.newInstance()
        factory!!.setNamespaceAware(true)
        val xpp = factory!!.newPullParser()
        xpp.setInput(StringReader(xmlString))
        var dataSet = false
        var tag_name:String?=null
        var eventType = xpp.getEventType()
        var isEnd=false
        while (eventType != XmlPullParser.END_DOCUMENT && !isEnd) {
            when (eventType) {
                XmlPullParser.START_DOCUMENT -> { }
                XmlPullParser.START_TAG -> {
                    tag_name = xpp.getName()
                    if (tag_name == "temp"||tag_name == "sky"||tag_name == "pty"||tag_name == "wfKor"||tag_name == "pop"){
                        dataSet=true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (dataSet) {
                        val data = xpp.getText()
                        when(tag_name) {
                            "temp"-> mWeather.temp= data.toDouble().toInt()
                            "sky"-> mWeather.sky= data.toInt()
                            "pty"->  mWeather.pty= data.toInt()
                            "wfKor"-> mWeather.wfKor=data
                            "pop"->{
                                mWeather.pop=data.toInt()
                                isEnd=true
                            }
                        }
                    }
                    dataSet = false
                }
                XmlPullParser.END_TAG -> { }
            }
            eventType = xpp.next()
        }
    }

}