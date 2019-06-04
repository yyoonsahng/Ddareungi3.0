package com.example.ddareungi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager

class GPSChecker : BroadcastReceiver() {
    interface GPSCallback {
        fun turnedon()
        fun turnedoff()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val locationManager = context!!.getSystemService(LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            
        } else {
        }
    }
}