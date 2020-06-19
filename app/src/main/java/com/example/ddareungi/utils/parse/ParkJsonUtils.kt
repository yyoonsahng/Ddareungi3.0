package com.example.ddareungi.utils.parse

import com.example.ddareungi.data.Park
import org.json.JSONArray
import org.json.JSONObject

class ParkJsonUtils {

    companion object {

        fun parseParkList(result: String): List<Park> {
            val jsonArray: JSONArray =
                JSONObject(result).getJSONObject("SearchParkInfoService").getJSONArray("row")

            val parkList = ArrayList<Park>()
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
                parkList.add(Park(id, name, zone, addr, g_longitude, g_latitude, longitude, latitude, 0.0f)
                )
            }
            return parkList
        }
    }
}