package com.example.ddareungi

import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.a190306app.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val MY_LOCATION_REQUEST = 99
    var locationPermissionGranted = false
    lateinit var bookmarkFragment: BookmarkFragment
    lateinit var mapFragment: MapFragment
    lateinit var timerFragment: TimerFragment
    lateinit var courseFragment: CourseFragment
    var urlStr = arrayOf(
        "http://openapi.seoul.go.kr:8088/746c776f61627a7437376b49567a68/json/bikeList/1/1000/", //대여소 1525개 있음 , 1000씩 나눠서 호출해야함
        "http://openapi.seoul.go.kr:8088/6d71556a42627a7437377549426e67/json/RealtimeCityAir/1/15/",
        "http://openapi.seoul.go.kr:8088/694b534943627a7434307364586868/json/SearchPublicToiletPOIService/1/5/",
        "http://openapi.seoul.go.kr:8088/527a4a4b47627a74363558734a7658/json/SearchParkInfoService/1/15/"
    )
    var bList = mutableListOf<MyBike>()
    var dList = mutableListOf<MyDust>()
    var rList = mutableListOf<MyRestroom>()
    var pList = mutableListOf<MyPark>()
    var dParse = dataParser(bList, dList, rList, pList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermission()
        initData()
        init()
    }

    fun initData() {
        //api url 연결해서 데이터 받아와서 파싱하여 리스트로 저장
        val networkTask0 = NetworkTask(0, urlStr[0], dParse, null)
        networkTask0.execute()
        val networkTask1 = NetworkTask(1, urlStr[1], dParse, null)
        networkTask1.execute()
        val networkTask2 = NetworkTask(2, urlStr[2], dParse, null)
        networkTask2.execute()
        val networkTask3 = NetworkTask(3, urlStr[3], dParse, this)
        networkTask3.execute()
    }

    fun init() {

        bookmarkFragment = BookmarkFragment()
        mapFragment = MapFragment()
        timerFragment = TimerFragment()
        courseFragment = CourseFragment()

        //바텀 메뉴 클릭했을 때 메뉴 별 fragment 생성
        bottom_navigation.setOnNavigationItemSelectedListener {
            var fragment: Fragment? = null
            when (it.itemId) {
                R.id.bookmark -> {
                    loadFragment(bookmarkFragment)
                }
                R.id.map -> {
                    loadFragment(mapFragment)
                }
                R.id.timer -> {
                    loadFragment(timerFragment)
                }
                R.id.course -> {
                    loadFragment(courseFragment)
                }
            }
            true
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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
            locationPermissionGranted = true
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
                    locationPermissionGranted = true
                } else {
                    finish()
                }
            }
        }
    }

    fun initPermission() {
        if (checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
        } else {
            askPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_LOCATION_REQUEST)
        }
    }

    class NetworkTask(val type: Int, val url: String, val dParse: dataParser, activity: MainActivity?) :
        AsyncTask<Unit, Unit, String>() { //void 대신 unit

        enum class Data(val type: Int) {
            BIKE(0),
            DUST(1),
            RESTROOM(2),
            PARK(3)
        }

        val mActivity = activity

        override fun doInBackground(vararg params: Unit?): String {
            val result: String // 요청 결과를 저장할 변수.
            val requestHttpURLConnection = RequestHttpURLConnection()
            result = requestHttpURLConnection.request(url) // 해당 URL로 부터 결과물을 얻어온다.
            return result
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //tview.text = result
            dParse.parse(type, result!!)
            if (mActivity != null) {
                Toast.makeText(mActivity.applicationContext, "Data parsing done", Toast.LENGTH_SHORT).show()
                dParse.bList.removeAt(999)
                mActivity.loadFragment(mActivity.bookmarkFragment)
                mActivity.mapFragment.setData(mActivity.locationPermissionGranted, dParse.bList)

            }

//
//            when (type) {
//                Data.BIKE.type ->{
//                    Log.i("asdf",dParse.bList.first().stationName)
//                }
//                Data.DUST.type ->{
//                    var list= dParse.getList(type) as MutableList<MyDust>
//                    Log.i("asdf",list.first().idex_nm)
//                }
//                Data.RESTROOM.type ->{
//                    var list= dParse.getList(type) as MutableList<MyRestroom>
//                    Log.i("asdf",list.first().fName)
//                }
//                Data.PARK.type ->{
//                    var list= dParse.getList(type) as MutableList<MyPark>
//                    Log.i("asdf",list.first().name)
//                }
//            }


        }
    }
}
