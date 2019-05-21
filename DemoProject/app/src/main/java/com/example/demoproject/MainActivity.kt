package com.example.demoproject

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    val MY_LOCATION_REQUEST = 99
    var mLocationPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermission()
        //MapFragment 생성 및 GPS 권환 획득 유무 확인하는 flag 값을 전달
        val mapFragment = MapFragment()
        val args = Bundle()
        args.putBoolean("mLocationPermissionGranted", mLocationPermissionGranted)
        mapFragment.arguments = args
        supportFragmentManager.beginTransaction().add(R.id.main_container, mapFragment).commit()

    }

    fun checkAppPermission(requestPermission: Array<String>): Boolean {
        val requestResult = BooleanArray(requestPermission.size)
        for (i in requestResult.indices) {
            requestResult[i] = ContextCompat.checkSelfPermission(
                this,
                requestPermission[i]
            ) == PackageManager.PERMISSION_GRANTED
            if (!requestResult[i]) {
                return false
            }
            mLocationPermissionGranted = true
        }
        return true
    }

    fun askPermission(requestPermission: Array<String>, REQ_PERMISSION: Int) {
        ActivityCompat.requestPermissions(this, requestPermission, REQ_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_LOCATION_REQUEST -> {
                if (checkAppPermission(permissions)) {
                    mLocationPermissionGranted = true
                } else {
                    finish()
                }
            }
        }
    }
    fun initPermission() {
        if(checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
        } else {
            askPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_LOCATION_REQUEST)
        }
    }

}
