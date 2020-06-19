package com.example.ddareungi.utils


import com.example.ddareungi.data.BikeStation
import com.example.ddareungi.data.Park
import com.google.android.libraries.places.api.model.Place
import java.net.URLEncoder

class NaverMapUtils {

    companion object {

        fun getUrlForBikeStation(station: BikeStation): String {
            var url = ""

            val dlat = station.stationLatitude
            val dlng = station.stationLongitude
            val dname = URLEncoder.encode(station.stationName, "UTF-8")
            url =
                "nmap://route/walk?dlat=$dlat&dlng=$dlng&dname=$dname&appname=com.example.ddareungi"

            return url
        }

        fun getUrlForPark(park: Park, closestStation: BikeStation): String {
            var url = ""

            val dlat = park.latitude
            val dlng = park.longitude
            val dname = URLEncoder.encode(park.name, "UTF-8")

            val vlat = closestStation.stationLatitude
            val vlng = closestStation.stationLongitude
            val vname = URLEncoder.encode(closestStation.stationName, "UTF-8")

            url =
                "nmap://route/bicycle?dlat=$dlat&dlng=$dlng&dname=$dname&v1lat=$vlat&v1lng=$vlng&v1name=$vname&appname=com.example.ddareungi"

            return url
        }

        fun getUrlForPlace(place: Place, closestStation: BikeStation): String {

            var url = ""

            val dlat = place.latLng!!.latitude
            val dlng = place.latLng!!.longitude
            val dname = URLEncoder.encode(place.name, "UTF-8")

            val vlat = closestStation.stationLatitude
            val vlng = closestStation.stationLongitude
            val vname = URLEncoder.encode(closestStation.stationName, "UTF-8")

            url =
                "nmap://route/bicycle?dlat=$dlat&dlng=$dlng&dname=$dname&v1lat=$vlat&v1lng=$vlng&v1name=$vname&appname=com.example.ddareungi"

            return url
        }
    }
}