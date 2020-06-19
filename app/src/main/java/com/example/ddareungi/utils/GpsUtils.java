package com.example.ddareungi.utils;

import android.content.Context;
import android.location.LocationManager;

public class GpsUtils {

    public static boolean isDeviceGpsTurnOn(Context context) {
        if(context == null) {
            return false;
        }
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
