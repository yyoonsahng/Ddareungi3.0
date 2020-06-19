package com.example.ddareungi.utils.parse

import com.example.ddareungi.data.BikeStation
import org.json.JSONObject

class BikeStationJsonUtils {

    companion object {

        fun parseBikeStationList(jsonStr: String): MutableList<BikeStation> {
            val jsonObject = JSONObject(jsonStr)
            val jsonArr = jsonObject.getJSONArray("realtimeList")
            val bikeStationList = mutableListOf<BikeStation>()

            for (i in 0 until jsonArr.length()) {
                val stationObj = jsonArr.getJSONObject(i)

                bikeStationList.add(
                    BikeStation(
                        stationId = stationObj.optString("stationId"),
                        stationName = stationObj.optString("stationName"),
                        stationLatitude = stationObj.optDouble("stationLatitude"),
                        stationLongitude = stationObj.optDouble("stationLongitude"),
                        rackTotCnt = stationObj.optInt("rackTotCnt"),
                        parkingBikeTotCnt = stationObj.optInt("parkingBikeTotCnt")
                                + stationObj.optInt("parkingQRBikeCnt"),
                        bookmarked = false
                    )
                )
            }
            return bikeStationList
        }
    }

}