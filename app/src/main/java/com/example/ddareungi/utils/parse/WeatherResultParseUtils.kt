package com.example.ddareungi.utils.parse

import com.example.ddareungi.data.Weather
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

class WeatherResultParseUtils {

    companion object {

        fun parseZoneCode(result: ByteArray, neighborhood: String): String {
            var flag = true

            // euc-kr 인코딩을 UTF-8로 변환
            val jsonArray = JSONArray(String(result, Charsets.UTF_8))
            var zoneCode = ""

            for (i in 0 until jsonArray.length()) {
                val value = jsonArray.getJSONObject(i).getString("value")

                if (neighborhood == value) {
                    zoneCode = jsonArray.getJSONObject(i).getString("code")
                    flag = false
                    break
                }
            }
            if (flag) {
                zoneCode = jsonArray.getJSONObject(0).getString("code")
            }

            return zoneCode
        }

        fun parseWeatherStateXml(result: String): Weather {
            val factory: XmlPullParserFactory? = XmlPullParserFactory.newInstance()
            factory!!.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(StringReader(result))
            var dataSet = false
            var tagName: String? = null
            var eventType = xpp.getEventType()
            var isEnd = false

            val weather = Weather(0, 0, 0, "", 0, "", "")

            while (eventType != XmlPullParser.END_DOCUMENT && !isEnd) {
                when (eventType) {
                    XmlPullParser.START_DOCUMENT -> {
                    }
                    XmlPullParser.START_TAG -> {
                        tagName = xpp.getName()
                        if (tagName == "temp" || tagName == "sky" || tagName == "pty" || tagName == "wfKor" || tagName == "pop") {
                            dataSet = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (dataSet) {
                            val data = xpp.text
                            when (tagName) {
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

            return weather
        }

        fun parseDustJson(result: String): String {
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONObject("RealtimeCityAir").getJSONArray("row")

            var idex_nm = ""
            for(i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                idex_nm = jsonObj.optString("IDEX_NM")
            }
            return idex_nm
        }
    }
}