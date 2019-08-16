package com.example.ddareungi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.Ringtone
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.example.ddareungi.MainActivity.Companion.courseInfoList
import com.example.ddareungi.dataClass.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.internal.it
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_bookmark.*
import kotlinx.android.synthetic.main.fragment_timer.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity(), BookmarkFragment.BookmarkToMapListener {

    companion object {
        val courseList = ArrayList<Course>()
        val courseInfoList = ArrayList<CourseInfo>()
        var timerStr=""
            //지금 수정
        var timermin=60
        var timerStart=false
    }


    lateinit var timer:Timer


    val MY_LOCATION_REQUEST = 99
    var locationPermissionGranted = false
    val bookmarkFragment = BookmarkFragment()
    val mapFragment = MapFragment()
    val timerFragment = TimerFragment()
    val courseFragment = CourseFragment()

    var mLocation: Location = Location("initLocation")
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var bList = mutableListOf<MyBike>()
    var rList = mutableListOf<MyRestroom>()
    var pList = mutableListOf<MyPark>()
    var mWeather = MyWeather(-1, -1, -1, "", -1)
    var mDust = MyDust(0.0, 0.0, "", 0.0, "")
    var dParse = dataParser(bList, mDust, rList, pList, mWeather)
    lateinit var localty: String
    lateinit var neighborhood: String
    var tCount:Int=60
    var nowFragment="bookmark"
    var enabledGPS = false  //GPS 켜져 있는지
    var networkState = false       //네트워크 켜져 있는지
    var isreLoad = false //네트워크 재연결 검사 후 켜져 있을 때 true
    var isSchedule=false



    var urlStr = arrayOf(
        "http://openapi.seoul.go.kr:8088/746c776f61627a7437376b49567a68/json/bikeList/", //대여소 1531개 있음 , 1000씩 나눠서 호출해야함
        "http://openapi.seoul.go.kr:8088/6d71556a42627a7437377549426e67/json/RealtimeCityAir/1/1/",
        "http://openapi.seoul.go.kr:8088/694b534943627a7434307364586868/json/SearchPublicToiletPOIService/", //4938개나 있음
        "http://openapi.seoul.go.kr:8088/527a4a4b47627a74363558734a7658/json/SearchParkInfoService/1/132/"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkUserState()
        initPermission()
        checkNetwork()
        //init()
        readFile()


    }
    fun runTimer(flag:Boolean,count:Int){
        tCount=count

        if(flag){
            timer=Timer()
            timer.schedule(CustomerTimer(this), 2000, 2500) //60000 1분
            isSchedule=true
        }
        else{
            tCount=count
            if(isSchedule){
                timer.cancel() //아예 타이머를 처음킨 것 처럼
                timerFragment.setData(false,timerFragment.mode,"00")

            }
            timerFragment.isTimer=false
            Log.i("timer", "run timer에서 flag가 false일 때  실행")
            timerFragment.initLayout()
        }

    }
    class CustomerTimer(val context: Context):TimerTask(){

        override fun run() {

            var mCount=(context as MainActivity).tCount
            mCount--
            if(mCount<0){

            }
            (context as MainActivity).tCount=mCount
            Log.i("timer","run에서 tCount값"+(context as MainActivity).tCount.toString())
            if(mCount==0) { //타이머 종료
                Log.i("timer","타이머가 종료됩니다.")

            }

            if((context as MainActivity).nowFragment=="timer"){
                var s1=""
                var s2=""
                if(mCount<=60){
                    if(mCount==60){
                        s1="01"
                        s2="00"
                    }
                    else{
                        s1="00"
                        s2=mCount.toString()
                    }
                }
                else{
                    if(mCount==120){
                        s1="02"
                        s2="00"
                    }
                    else{
                        s1="01"
                        s2=(mCount-60).toString()
                    }
                }
                Log.i("timer", "customer timer 스케쥴해서 run에서  실행")
                (context as MainActivity).timerFragment.setData(false, s1,s2)
                (context as MainActivity).timerFragment.initLayout()
            }
        }


    }


    fun readFile() {
        val scan = Scanner(resources.openRawResource(R.raw.courseinfo))
        while (scan.hasNextLine()) {

            val title = scan.nextLine()
            Log.v("scan", title)
            val subtitle = scan.nextLine()
            Log.v("scan", subtitle)
            val bikestop = scan.nextLine()
            val location = scan.nextLine()
            val open = scan.nextLine()
            val tel = scan.nextLine()
            val data = CourseInfo(title, subtitle, bikestop, location, tel, open)
            courseInfoList.add(data)
            Log.v("scan", courseInfoList.size.toString())
        }

        val scan0 = Scanner(resources.openRawResource(R.raw.coursename))
        while (scan0.hasNextLine()) {

            val subtitle = scan0.nextLine()
            val title = scan0.nextLine()
            val length = scan0.nextLine()
            val time = scan0.nextLine()
            val data = Course(title, subtitle, length, time)
            courseList.add(data)
            Log.v("scan1", courseList.size.toString())
        }

        initFragment()
    }

    //GPS나 네트워크 켜져 있는지 broadcast receiver로 확인
    //상태 바뀌면 플래그 변수 값 바꿔서 프래그먼트로 넘겨줌
    fun checkUserState() {

        val intentFilter = IntentFilter()
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        val networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo
                networkState = networkInfo != null && networkInfo.isConnected
                mapFragment.networkState = networkState
                bookmarkFragment.networkState = networkState
            }
        }
        val gpsIntentFilter = IntentFilter()
        gpsIntentFilter.addAction("android.location.PROVIDERS_CHANGED")
        val gpsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val lm = getSystemService(LOCATION_SERVICE) as LocationManager
                enabledGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                mapFragment.mEnableGPS = enabledGPS
                bookmarkFragment.enableGPS = enabledGPS
                mapFragment.setGPSWidget()
            }
        }
        registerReceiver(networkReceiver, intentFilter)
        registerReceiver(gpsReceiver, gpsIntentFilter)

        //처음 어플을 실행 했을 때 네트워크 및 GPS 상태를 확인
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        networkState = networkInfo != null && networkInfo.isConnected
        mapFragment.networkState = networkState
        bookmarkFragment.networkState = networkState

        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        enabledGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        mapFragment.mEnableGPS = enabledGPS
        bookmarkFragment.enableGPS = enabledGPS
    }

    fun initLocation() {
        try {
            if (enabledGPS && networkState) {
                var geocoder = Geocoder(this, Locale.KOREA)
                var addrList = geocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 1)
                var addr = addrList.first().getAddressLine(0).split(" ")
                localty = addr[2]
                neighborhood = addr[3]
            }
        }
        catch(e:Exception){
            mLocation.latitude = 37.540
            mLocation.longitude = 127.07
            localty="광진구"
            neighborhood="화양동"

        }
    }

    override fun changeBookmarkToMap(rentalOffice: String) {
        mapFragment.setData(locationPermissionGranted, enabledGPS, bList, rList, pList, rentalOffice)
        bottom_navigation.menu.findItem(R.id.map).setChecked(true)
        loadFragment(mapFragment)
    }

    fun checkNetwork() {
        if (networkState) {
            initData()
        } else {
            logo_layout.visibility = View.GONE
            window.statusBarColor = resources.getColor(R.color.white, null)
            window.decorView.background = resources.getDrawable(R.color.white, null)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            bookmarkFragment.setData(dParse.bList, dParse.mDust, dParse.mWeather, neighborhood)
            loadFragment(bookmarkFragment)
        }
    }

    fun initData() {
        val networkTask0 = NetworkTask(0, urlStr[0], dParse, null)
        networkTask0.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        val networkTask2 = NetworkTask(2, urlStr[2], dParse, this)
        networkTask2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        val networkTask3 = NetworkTask(3, urlStr[3], dParse, null)
        networkTask3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    }

    fun initFragment() {
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
            var flag=false
            if(nowFragment=="timer" && timerFragment.isTimer==true) //타이머 프레그먼트에서 타이머가 돌아갔는지 확인
                flag=true
            when (it.itemId) {
                R.id.bookmark -> {
                    nowFragment="bookmark"
                    loadFragment(bookmarkFragment)
                }
                R.id.map -> {
                    nowFragment="map"
                    mapFragment.mLocationPermissionGranted = locationPermissionGranted
                    loadFragment(mapFragment)
                }
                R.id.timer -> {

//                    var s1=""
//                    var s2=""
//                    if(tCount>=60){
//                        if(tCount==60){
//                            s1="01"
//                            s2="00"
//                        }
//                        else{
//                            s1="00"
//                            s2=tCount.toString()
//                        }
//                    }
//                    else{
//                        if(tCount==120){
//                            s1="02"
//                            s2="00"
//                        }
//                        else{
//                            s1="01"
//                            s2=(tCount-60).toString()
//                        }
//                    }
//                    if(flag){ //타이머가 돌아간 상태에서 다른 프레그먼트를 눌렀다가 다시 타이머 프레그를 누른 경우
//                        timerFragment.setData(true,s1,s2)
//                    }
//                    else{
//                        timerFragment.setData(false,s1,s2)
//                    }

               //     loadFragment(timerFragment)
                    Toast.makeText(this, "준비중입니다",Toast.LENGTH_SHORT).show()
                    nowFragment="timer"
                }
                R.id.course -> {
                    nowFragment="course"
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

    fun loadWeatherFile(id: Int): String { //날씨 api / 구에 따른 지역코드 파싱
        val scan = Scanner(resources.openRawResource(id)) //json 파일
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
            }
        }

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
        mLocation.latitude = 37.540
        mLocation.longitude = 127.07
        localty = "광진구"
        neighborhood = "화양동"
        if (enabledGPS) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }
        if (checkAppPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
        } else {
            askPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_LOCATION_REQUEST)
        }

        val wResult = loadWeatherFile(R.raw.weather)
        val wArray = JSONArray(wResult)
        //미세먼지
        val dResult = loadWeatherFile(R.raw.dust)
        val dArray = JSONArray(dResult)

        var wCode = ""
        var dCode = ""
        for (i in 0 until wArray.length()) {
            val wValue = wArray.getJSONObject(i).getString("value")
            if (localty == wValue) {
                wCode = wArray.getJSONObject(i).getString("code")
                dCode = dArray.getJSONObject(i).getString("code")
                break
            }
        }

        if (networkState) {
            val networkTask1 = NetworkTask(1, urlStr[1] + dCode + "/" + localty, dParse, null)
            networkTask1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

            val networkTask4 = NetworkTask(4, wCode, dParse, this)
            networkTask4.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
                val wStr = "http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf." + url + ".json.txt" //url :지역구 코드
                val result = RequestHttpURLConnection().request(wStr)
                val array = JSONArray(result)
                var code = ""
                for (i in 0 until array.length()) {
                    val value = array.getJSONObject(i).getString("value")
                    if (mActivity!!.neighborhood == value) {
                        code = array.getJSONObject(i).getString("code")
                        break
                    }
                }
                url = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=" + code
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
                                if (dParse!!.bList[mCount].parkingBikeTotCnt != mParkingBikeTotCnt) {
                                    dParse!!.bList[mCount].parkingBikeTotCnt = mParkingBikeTotCnt
                                }
                                mCount++
                            }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                if (onMapUpdate!!) {
                    mActivity!!.mapFragment.currentMarkerType = MapFragment.PlaceType.BIKE
                    mActivity!!.mapFragment.setData(
                        mActivity!!.locationPermissionGranted,
                        mActivity!!.enabledGPS,
                        mActivity!!.dParse.bList,
                        mActivity!!.dParse.rList,
                        mActivity!!.dParse.pList,
                        null
                    )
                    mActivity!!.mapFragment.updateMarker(mActivity!!.mapFragment.currentMarkerType, true)

                } else {
                    mActivity!!.bookmarkFragment.setData(
                        mActivity!!.dParse.bList,
                        mActivity!!.dParse.mDust,
                        mActivity!!.dParse.mWeather,
                        mActivity!!.neighborhood
                    )
                    mActivity!!.bookmarkFragment.upDate(true)
                }
                val progressBar = mActivity!!.findViewById<ProgressBar>(R.id.progress_circular)
                if (progressBar != null)
                    progressBar.visibility = View.GONE
            } else {
                for (i in result)
                    dParse!!.parse(type, i)
                if (mActivity != null && type == Data.RESTROOM.type) {
                    mActivity!!.logo_layout.visibility = View.GONE
                    mActivity!!.window.statusBarColor = mActivity!!.resources.getColor(R.color.white, null)
                    mActivity!!.window.decorView.background = mActivity!!.resources.getDrawable(R.color.white, null)
                    mActivity!!.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                    mActivity!!.loadFragment(mActivity!!.bookmarkFragment)

                    mActivity!!.bookmarkFragment.setData(
                        dParse!!.bList,
                        dParse.mDust,
                        dParse.mWeather,
                        mActivity!!.neighborhood
                    )

                    if (mActivity!!.isreLoad) { //네트워크 연결 재시도로 호출한 파싱일 경우
                        mActivity!!.bookmarkFragment.weather_image.visibility = View.VISIBLE
                        mActivity!!.bookmarkFragment.dust_text.visibility = View.VISIBLE
                        mActivity!!.bookmarkFragment.initLayout()
                    }
                    mActivity!!.isreLoad = false
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

