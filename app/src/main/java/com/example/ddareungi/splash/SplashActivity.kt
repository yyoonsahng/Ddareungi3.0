package com.example.ddareungi.splash

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.ddareungi.MainActivity
import com.example.ddareungi.R
import com.example.ddareungi.data.DataRepositoryHolder
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class SplashActivity : AppCompatActivity(), SplashContract.View {
    lateinit var splashPresenter: SplashPresenter

    var mLocation: Location = Location("initLocation")
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var locationPermissionGranted = false


    companion object {
        const val MY_LOCATION_REQUEST = 99
        const val CALL_REQUEST = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val dataRepository = DataRepository.newInstance(this)
        splashPresenter = SplashPresenter(dataRepository, this)

        mLocation.latitude = 37.566414
        mLocation.longitude = 126.977912

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initPermission()
    }

    override fun showBookmarkActivity(dataRepository: DataRepository) {
        val intent = Intent(this, MainActivity::class.java).apply {
            val holderId = DataRepositoryHolder.putDataRepository(dataRepository)
            putExtra(MainActivity.DATA_REPOSITORY_ID, holderId)
            putExtra(MainActivity.LOCATION_PERMISSION_ID, locationPermissionGranted)
        }
        startActivity(intent)
    }

    override fun initLocation(isGps:Boolean,dataRepository: DataRepository) {

        try {
            val geocoder = Geocoder(this, Locale.KOREA)
            val addrList = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 5)
            var address: List<String> = listOf("대한민국", "서울특별시", "중구", "명동")
            for(addr in addrList) {
                val splitedArr = addr.getAddressLine(0).split(" ")
                if(splitedArr[2].endsWith("구") ){
                    address = splitedArr
                    break
                }
            }
            splashPresenter.processLocation(
                address[2],
                address[3],
                Scanner(resources.openRawResource(R.raw.weather)),
                Scanner(resources.openRawResource(R.raw.dust))
            )

        } catch (e: Exception) {
        }
        dataRepository.initWeather(isGps, object : DataSource.LoadDataCallback {
            override fun onDataLoaded() {
                showBookmarkActivity(dataRepository)
            }

            override fun onNetworkNotAvailable() {
                showBookmarkActivity(dataRepository)
            }
        })
    }

    private fun checkAppPermission(requestPermission: Array<String>, isLocation: Boolean): Boolean {
        val requestResult = BooleanArray(requestPermission.size)
        for (i in requestResult.indices) {
            requestResult[i] =
                ContextCompat.checkSelfPermission(
                    this,
                    requestPermission[i]
                ) == PackageManager.PERMISSION_GRANTED
            if (!requestResult[i]) {
                return false
            }
        }
        if (isLocation) {
            val lm =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val locationListener = object : LocationListener {
                var isGpsProvider = false
                var isNetworkProvider = false
                var isLoaded = false
                var isGpsProviderEnabled = true
                var isNetworkProviderEnabled = true
                override fun onLocationChanged(location: Location) {
                    lm.removeUpdates(this)
                    if (location.provider == LocationManager.GPS_PROVIDER) isGpsProvider = true
                    if (location.provider == LocationManager.NETWORK_PROVIDER) isNetworkProvider =true
                    if ((!(isGpsProvider && isNetworkProvider)) && (!isLoaded)) {
                        isLoaded = true
                        mLocation.latitude = location.latitude
                        mLocation.longitude = location.longitude
                        lm.removeUpdates(this)
                        splashPresenter.initWeatherRepository(true)
                    }
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                }

                override fun onProviderEnabled(provider: String) {

                }

                override fun onProviderDisabled(provider: String) {
                    lm.removeUpdates(this)
                    Toast.makeText(applicationContext,"위치 환경 설정을 켜주세요.",Toast.LENGTH_SHORT).show()

                    if (provider == LocationManager.GPS_PROVIDER) isGpsProviderEnabled = false
                    if (provider == LocationManager.NETWORK_PROVIDER) isNetworkProviderEnabled =false
                    if (!isGpsProviderEnabled && !isNetworkProviderEnabled) {
                        splashPresenter.initWeatherRepository(false)
                    }


                }
            }

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000L, 10f, locationListener)
            lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                60000L,
                10f,
                locationListener
            )
            splashPresenter.initDataRepository()
        }
        return true
    }


    private fun askPermission(requestPermission: Array<String>, REQ_PERMISSION: Int) {
        ActivityCompat.requestPermissions(this, requestPermission, REQ_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_LOCATION_REQUEST -> {
                if (checkAppPermission(permissions, true)) {
                    //
                } else {
                    locationPermissionGranted = false
                }
            }

        }
    }

    private fun initPermission() {
        if (checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), true)) {
        } else {
            askPermission(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MY_LOCATION_REQUEST
            )
        }
        if (checkAppPermission(arrayOf(android.Manifest.permission.CALL_PHONE), false)) {

        } else {
            askPermission(arrayOf(android.Manifest.permission.CALL_PHONE), CALL_REQUEST)
        }
    }

}
