package com.example.ddareungi.splash

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.ddareungi.MainActivity
import com.example.ddareungi.R
import com.example.ddareungi.data.DataRepositoryHolder
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.lang.Exception
import java.util.*

class SplashActivity : AppCompatActivity(), SplashContract.View {
    lateinit var splashPresenter: SplashPresenter

    var mLocation: Location = Location("initLocation")
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var locationPermissionGranted = false

    companion object {
        const val MY_LOCATION_REQUEST = 99
        const val CALL_REQUEST=1234
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataRepository = DataRepository.newInstance(this)

        splashPresenter = SplashPresenter(dataRepository, this)

        mLocation.latitude = 37.540
        mLocation.longitude = 127.07

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

    override fun initLocation(dataRepository: DataRepository) {

        try {
            val geocoder = Geocoder(this, Locale.KOREA)
            val addrList = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 1)
            val addr = addrList.first().getAddressLine(0).split(" ")
            splashPresenter.processLocation(addr[2], addr[3], Scanner(resources.openRawResource(R.raw.weather)), Scanner(resources.openRawResource(R.raw.dust)))


        }
        catch(e:Exception){  }
        dataRepository.initWeather(object: DataSource.LoadDataCallback{
                override fun onDataLoaded() {
                    showBookmarkActivity(dataRepository)
                }

                override fun onNetworkNotAvailable() {
                    showBookmarkActivity(dataRepository)
                }
        })
    }

    private fun checkAppPermission(requestPermission: Array<String>,isLocation:Boolean): Boolean {
        val requestResult = BooleanArray(requestPermission.size)
        for (i in requestResult.indices) {
            requestResult[i] =
                ContextCompat.checkSelfPermission(this, requestPermission[i]) == PackageManager.PERMISSION_GRANTED
            if (!requestResult[i]) {
                return false
            }
        }
        if(isLocation) {
            locationPermissionGranted = true
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    mLocation = it
                }
                splashPresenter.initWeatherRepository()
            }
            splashPresenter.initDataRepository() //위치정보 파싱이 끝난 후에 데이터 파싱
                        //사용자가 권한 체크한 후에 데이터 파싱
        }

        return true
    }

    private fun askPermission(requestPermission: Array<String>, REQ_PERMISSION: Int) {
        ActivityCompat.requestPermissions(this, requestPermission, REQ_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_LOCATION_REQUEST -> {
                if (checkAppPermission(permissions,true)) {
                    //
                } else {
                    locationPermissionGranted = false
                }
            }

        }
    }

    private fun initPermission() {
        if (checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),true)) {
        } else {
            askPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_LOCATION_REQUEST)
        }
        if(checkAppPermission(arrayOf(android.Manifest.permission.CALL_PHONE),false)){

        }else{
            askPermission(arrayOf(android.Manifest.permission.CALL_PHONE), CALL_REQUEST)
        }
    }

}
