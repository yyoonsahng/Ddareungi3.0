package com.example.ddareungi

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.a190306app.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*




class MainActivity : AppCompatActivity(), MenuItem.OnMenuItemClickListener, BookmarkFragment.BookmarkToMapListener {
    val MY_LOCATION_REQUEST = 99
    var locationPermissionGranted = false
    lateinit var bookmarkFragment: BookmarkFragment
    lateinit var mapFragment: MapFragment
    lateinit var timerFragment: TimerFragment
    lateinit var courseFragment: CourseFragment

    var mLocation: Location = Location("initLocation")
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var bList = mutableListOf<MyBike>()
    var dList = mutableListOf<MyDust>()
    var rList = mutableListOf<MyRestroom>()
    var pList = mutableListOf<MyPark>()
    var dParse = dataParser(bList, dList, rList, pList)
    lateinit var localty: String
    var enabledGPS = false

    var urlStr = arrayOf(
        "http://openapi.seoul.go.kr:8088/746c776f61627a7437376b49567a68/json/bikeList/", //대여소 1531개 있음 , 1000씩 나눠서 호출해야함
        "http://openapi.seoul.go.kr:8088/6d71556a42627a7437377549426e67/json/RealtimeCityAir/1/25/",
        "http://openapi.seoul.go.kr:8088/694b534943627a7434307364586868/json/SearchPublicToiletPOIService/", //4938개나 있음
        "http://openapi.seoul.go.kr:8088/527a4a4b47627a74363558734a7658/json/SearchParkInfoService/1/132/"
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermission()
         checkNetwork()
         init()
    }

    override fun changeBookmarkToMap(rentalOffice: String) {
        mapFragment.setData(locationPermissionGranted, enabledGPS, bList, rList, pList, rentalOffice)
        bottom_navigation.menu.findItem(R.id.map).setChecked(true)
        loadFragment(mapFragment)

    }

    fun checkNetwork(){
        var connectvityManager=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo=connectvityManager.activeNetworkInfo
        if(networkInfo!=null && networkInfo.isConnected){

            Toast.makeText(this,"네트워크연결됨",Toast.LENGTH_SHORT).show()
            initData()
        }
        else{
            //네트워크에연결안되어있으면 일단그냥종료
            //어떻게처리할지 고민해봐야겠음
            Toast.makeText(this,"네트워크연결안됨",Toast.LENGTH_SHORT).show()
            logo_layout.visibility = View.GONE
        }
    }
    fun  initData() {
        val networkTask0 = NetworkTask(0, urlStr[0],dParse,null)
        networkTask0.execute()

        val networkTask2 = NetworkTask(2, urlStr[2], dParse, this)
        networkTask2.execute()

        val networkTask1 = NetworkTask(1, urlStr[1], dParse, null)
        networkTask1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        val networkTask3 = NetworkTask(3, urlStr[3], dParse, null)
        networkTask3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


    }

    fun init() {
        Log.i("location init", "init실행")
        bookmarkFragment = BookmarkFragment()
        mapFragment = MapFragment()
        timerFragment = TimerFragment()
        courseFragment = CourseFragment()
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
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

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            R.id.action_homepage -> {
                Toast.makeText(this, "action button clicked", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
                return false
            }
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
            if (enabledGPS) {
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        mLocation = it
                    } else {
                        mLocation.latitude = 37.540
                        mLocation.longitude = 127.07
                    }
                }
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
                    //finish()
                }
            }
        }
    }

    fun initPermission() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        mLocation.latitude = 37.540
        mLocation.longitude = 127.07

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            enabledGPS = true
        if (enabledGPS)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        else
            localty = "광진구"

        if (checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
        } else {
            askPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_LOCATION_REQUEST)
        }
    }

    class NetworkTask(val type: Int, val url: String, val dParse: dataParser, activity: MainActivity?) :
        AsyncTask<Unit, Unit, List<String>>() { //void 대신 unit
        // doInBackground, onProgressUpdate, onPostExecute의 매개변수 자료형

        enum class Data(val type: Int) {
            BIKE(0),
            DUST(1),
            RESTROOM(2),
            PARK(3)
        }

        val mActivity = activity

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
            }
            else if(type==Data.RESTROOM.type){
                val rStr=url+"1/1000"
                val result=RequestHttpURLConnection().request(rStr)
                var rNum=JSONObject(result).getJSONObject("SearchPublicToiletPOIService").optInt("list_total_count")-1000
                rList.add(result)
                var count=2
                while(true){
                    val rStr=url+(1+1000*(count-1)).toString()+"/"+(1000*count).toString()
                    val result=RequestHttpURLConnection().request(rStr) // 해당 URL로 부터 결과물을 얻어온다.
                    rList.add(result)
                    rNum -= 1000
                    if (rNum < 1)
                        break
                    count++
                }
                return rList
            }
            val result = RequestHttpURLConnection().request(url) // 해당 URL로 부터 결과물을 얻어온다.
            rList.add(result)

            return rList
        }

        override fun onPostExecute(result: List<String>) {
            super.onPostExecute(result)
            for (i in result)
                dParse.parse(type, i)
            if (mActivity != null) {
                initLocation()
                Toast.makeText(
                    mActivity.applicationContext,
                    "Data parsing done" + mActivity!!.localty,
                    Toast.LENGTH_SHORT
                ).show()
                mActivity.logo_layout.visibility = View.GONE
                mActivity.window.statusBarColor = mActivity.resources.getColor(R.color.white, null)
                mActivity.window.decorView.background = mActivity.resources.getDrawable(R.color.white, null)
                mActivity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                Toast.makeText(mActivity.applicationContext, "Data parsing done"+mActivity.localty, Toast.LENGTH_SHORT).show()
                mActivity.loadFragment(mActivity.bookmarkFragment)
                mActivity.bookmarkFragment.getData(dParse.bList, dParse.dList)

                mActivity.mapFragment.setData(
                    mActivity.locationPermissionGranted,
                    mActivity.enabledGPS,
                    dParse.bList,
                    dParse.rList,
                    dParse.pList,
                    null
                )
            }
        }

        fun initLocation(){
            if(mActivity!!.enabledGPS){
                var geocoder= Geocoder(mActivity, Locale.KOREA)
                var addrList=geocoder.getFromLocation(mActivity.mLocation.latitude,mActivity!!.mLocation.longitude,1)
                var addr=addrList.first().getAddressLine(0).split(" ")
                mActivity.localty=addr[2]
            }
//            if (mActivity!!.checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))){
//                val lm=mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//                if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)
//                    fusedLocationClient.lastLocation.addOnSuccessListener {
//
//                    }
//                }
//            }
        }
    }
}


