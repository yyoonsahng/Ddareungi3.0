package com.example.ddareungi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.example.ddareungi.RequestHttpURLConnection
import com.example.ddareungi.dataClass.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.Math.floor
import java.lang.Math.pow
import java.util.*
import kotlin.math.*


class MainActivity : AppCompatActivity(), BookmarkFragment.BookmarkToMapListener {
    val MY_LOCATION_REQUEST = 99
    var locationPermissionGranted = false
    lateinit var bookmarkFragment: BookmarkFragment
    lateinit var mapFragment: MapFragment
    lateinit var timerFragment: TimerFragment
    lateinit var courseFragment: CourseFragment

    var mLocation: Location = Location("initLocation")
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var bList = mutableListOf<MyBike>()
    //var dList = mutableListOf<MyDust>()
    var rList = mutableListOf<MyRestroom>()
    var pList = mutableListOf<MyPark>()
    var mWeather=MyWeather(-1,-1,-1,"",-1)
    var mDust=MyDust(0.0,0.0,"",0.0,"")
    var dParse = dataParser(bList, mDust, rList, pList,mWeather)
    lateinit var localty: String
    lateinit var neighborhood:String
    var enabledGPS = false

    var urlStr = arrayOf(
        "http://openapi.seoul.go.kr:8088/746c776f61627a7437376b49567a68/json/bikeList/", //대여소 1531개 있음 , 1000씩 나눠서 호출해야함
        "http://openapi.seoul.go.kr:8088/6d71556a42627a7437377549426e67/json/RealtimeCityAir/1/1/",
        "http://openapi.seoul.go.kr:8088/694b534943627a7434307364586868/json/SearchPublicToiletPOIService/", //4938개나 있음
        "http://openapi.seoul.go.kr:8088/527a4a4b47627a74363558734a7658/json/SearchParkInfoService/1/132/"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermission()
        checkNetwork()
        initFragment()
    }

    fun initLocation() {
        if (enabledGPS) {
            var geocoder = Geocoder(this, Locale.KOREA)
            var addrList = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 1)
            var addr = addrList.first().getAddressLine(0).split(" ")
            localty = addr[2]
            neighborhood=addr[3]
        }
    }

    override fun changeBookmarkToMap(rentalOffice: String) {
        mapFragment.setData(locationPermissionGranted, enabledGPS, bList, rList, pList, rentalOffice)
        bottom_navigation.menu.findItem(R.id.map).setChecked(true)
        loadFragment(mapFragment)
    }

    fun checkNetwork() {
        var connectvityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectvityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {

            Toast.makeText(this, "네트워크연결됨", Toast.LENGTH_SHORT).show()
            initData()
        } else {
            //네트워크에연결안되어있으면 일단그냥종료
            //어떻게처리할지 고민해봐야겠음
            Toast.makeText(this, "네트워크연결안됨", Toast.LENGTH_SHORT).show()
            logo_layout.visibility = View.GONE
        }
    }

    fun initData() {
        val networkTask0 = NetworkTask(0, urlStr[0], dParse, null)
        networkTask0.execute()

        val networkTask2 = NetworkTask(2, urlStr[2], dParse, this)
        networkTask2.execute()


        val networkTask3 = NetworkTask(3, urlStr[3], dParse, null)
        networkTask3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    }

    fun initFragment() {
        bookmarkFragment = BookmarkFragment()
        mapFragment = MapFragment()
        timerFragment = TimerFragment()
        courseFragment = CourseFragment()
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        ddraeungi_home_button.setOnClickListener {
            val ddareungiHome = Uri.parse("https://www.bikeseoul.com")
            val webIntent = Intent(Intent.ACTION_VIEW, ddareungiHome)
            startActivity(webIntent)
        }

        val dummyMapsInitializer = SupportMapFragment()
        supportFragmentManager.beginTransaction().attach(dummyMapsInitializer).commit()
        dummyMapsInitializer.getMapAsync {
        }

        //바텀 메뉴 클릭했을 때 메뉴 별 fragment 생성
        bottom_navigation.setOnNavigationItemSelectedListener {
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
    fun loadWeatherFile(id:Int):String{ //날씨 api / 구에 따른 지역코드 파싱
        val scan= Scanner(resources.openRawResource(id)) //json 파일
        var result = ""
        while (scan.hasNextLine()) {
            val line = scan.nextLine()
            result += line
        }
     return result
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

        }
        if (enabledGPS) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    mLocation = it
                    initLocation()

                } else {
                    mLocation.latitude = 37.540
                    mLocation.longitude = 127.07
                }
                //날씨


            }
        }
        val wResult=loadWeatherFile(R.raw.weather)
        val wArray = JSONObject(wResult).getJSONArray("data")
        //미세먼지
        val dResult=loadWeatherFile(R.raw.dust)
        val dArray = JSONObject(dResult).getJSONArray("data")

        var wCode=""
        var dCode=""
        for (i in 0 until wArray.length()) {
            val wValue = wArray.getJSONObject(i).getString("value")
            if (localty == wValue) {
                wCode=wArray.getJSONObject(i).getString("code")
                dCode=dArray.getJSONObject(i).getString("code")
                break
            }
        }
        val networkTask1 = NetworkTask(1, urlStr[1]+dCode+"/"+localty, dParse, null)
        networkTask1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        val networkTask4 = NetworkTask(4, wCode, dParse, this)
        networkTask4.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        locationPermissionGranted = true
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
                    //finish()
                }
            }
        }
    }

    fun initPermission() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        mLocation.latitude = 37.540
        mLocation.longitude = 127.07
        localty = "광진구"
        neighborhood="화양동"
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            enabledGPS = true
        if (enabledGPS) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }
        if (checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
        } else {
            askPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_LOCATION_REQUEST)
        }
    }


    class NetworkTask(
        val type: Int,
        var url: String,
        val dParse: dataParser?,
        var mActivity: MainActivity?,
        var onMapUpdate: Boolean? = null
    ) :
        AsyncTask<Unit, Unit, List<String>>() { //void 대신 unit
        // doInBackground, onProgressUpdate, onPostExecute의 매개변수 자료형
        enum class Data(val type: Int) {
            BIKE(0),
            DUST(1),
            RESTROOM(2),
            PARK(3),
            WEATHER(4)
        }


        override fun doInBackground(vararg params: Unit?): List<String> {
            var rList = mutableListOf<String>()

            if (type == Data.BIKE.type) {
                var count = 1
                while (true) {
                    val bStr = url + (1 + 1000 * (count - 1)).toString() + "/" + (1000 * count).toString()
                    val result = RequestHttpURLConnection().request(bStr) // 해당 URL로 부터 결과물을 얻어온다.
                    val jObject = JSONObject(result).getJSONObject("rentBikeStatus")
                    val bNum = jObject.optInt("list_total_count")
                    val bStatus = jObject.getJSONObject("RESULT").optString("CODE")
                    if (bStatus != "INFO-000")
                        break
                    rList.add(result)
                    if (bNum % 1000 != 0) //더이상 검색안해두될 떄 !
                        break
                    count++
                }
                return rList
            } else if (type == Data.RESTROOM.type) {
                val rStr = url + "1/1000"
                val result = RequestHttpURLConnection().request(rStr)
                var rNum =
                    JSONObject(result).getJSONObject("SearchPublicToiletPOIService").optInt("list_total_count") - 1000
                rList.add(result)
                var count = 2
                while (true) {
                    val rStr = url + (1 + 1000 * (count - 1)).toString() + "/" + (1000 * count).toString()
                    val result = RequestHttpURLConnection().request(rStr) // 해당 URL로 부터 결과물을 얻어온다.
                    rList.add(result)
                    rNum -= 1000
                    if (rNum < 1)
                        break
                    count++
                }
                return rList
            }
            if (type == Data.WEATHER.type) {
                val wStr = "http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf."+url+".json.txt" //url :지역구 코드
                val result = RequestHttpURLConnection().request(wStr)
                val array = JSONArray(result)
                var code=""
                for (i in 0 until array.length()) {
                    val value = array.getJSONObject(i).getString("value")
                    if (mActivity!!.neighborhood == value) {
                        code=array.getJSONObject(i).getString("code")
                        break
                    }
                }
                url="http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone="+code
            }
            val result = RequestHttpURLConnection().request(url) // 해당 URL로 부터 결과물을 얻어온다.
            rList.add(result)

            return rList
        }

        override fun onPostExecute(result: List<String>) {
            super.onPostExecute(result)
            if (onMapUpdate != null) {
                var mCount = 0
                for (i in result) {
                    try {
                        var jarray: JSONArray = JSONObject(i).getJSONObject("rentBikeStatus").getJSONArray("row")
                        for (j in 0..jarray.length()) {
                            val mParkingBikeTotCnt: Int = jarray.getJSONObject(j).optInt("parkingBikeTotCnt")
                            if (mActivity!!.dParse.bList[mCount].parkingBikeTotCnt != mParkingBikeTotCnt) {
                                mActivity!!.dParse.bList[mCount].parkingBikeTotCnt = mParkingBikeTotCnt
                            }
                            mCount++
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                if (onMapUpdate!!) {
                    mActivity!!.mapFragment.currentMarkerType = MapFragment.PlaceType.BIKE
                    mActivity!!.mapFragment.updateMarker(mActivity!!.mapFragment.currentMarkerType, true)
                } else {
                    mActivity!!.bookmarkFragment.upDate(true)
                }
            } else {
                for (i in result)
                    dParse!!.parse(type, i)
                if (mActivity != null && type == Data.RESTROOM.type) {
                    //    initLocation()
                    Toast.makeText(
                        mActivity!!.applicationContext,
                        "Data parsing done" + mActivity!!.localty+"의 날씨는 "+dParse!!.mWeather.wfKor,
                        Toast.LENGTH_SHORT
                    ).show()
                    mActivity!!.logo_layout.visibility = View.GONE
                    mActivity!!.window.statusBarColor = mActivity!!.resources.getColor(R.color.white, null)
                    mActivity!!.window.decorView.background = mActivity!!.resources.getDrawable(R.color.white, null)
                    mActivity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                    mActivity!!.loadFragment(mActivity!!.bookmarkFragment)
                    mActivity!!.bookmarkFragment.setData(dParse!!.bList, dParse.mDust)

                    mActivity!!.mapFragment.setData(
                        mActivity!!.locationPermissionGranted,
                        mActivity!!.enabledGPS,
                        dParse!!.bList,
                        dParse!!.rList,
                        dParse!!.pList,
                        null
                    )
                }
            }

        }


    }
}

