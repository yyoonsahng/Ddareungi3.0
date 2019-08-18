package com.example.ddareungi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.ddareungi.bookmark.BookmarkFragment
import com.example.ddareungi.bookmark.BookmarkPresenter
import com.example.ddareungi.data.*
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.map.MapFragment
import com.example.ddareungi.util.replaceFragmentInActivity
import com.example.ddareungi.util.setupActionBar
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(){


    companion object {
        val courseList = ArrayList<Course>()
        val courseInfoList = ArrayList<CourseInfo>()
        var timerMin=59
        var selectHour=false
        var ringFlag=true
        var activitystate=false

        const val DATA_REPOSITORY_ID = "DATA_REPOSITORY_ID"
        const val LOCATION_PERMISSION_ID = "LOCATION_PERMISSION_ID"
    }

    //channel 생성 (createNotificationChannel)

    private fun createNotificationChannel(context: Context,importance:Int,showBadge:Boolean,name:String,description:String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelId="${context.packageName}-$name"
            val channel=NotificationChannel(channelId,name,importance)
            channel.description=description
            channel.setShowBadge(showBadge)

            val notificationManager=context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    //헤드업 알림(builder 생셩)
    val CHANNEL_ID="TimerChannel"

    var builder= NotificationCompat.Builder(this,CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_ddareungi_logo)
        .setContentTitle("반납 시간 15분 전입니다.")
        .setContentText("15분 이내 근처 대여소에 따릉이를 반납해주세요.")
        .setPriority(NotificationCompat.PRIORITY_MAX)


    fun createNotificationBuilder(){
        val NOTIFICATION_ID=1001;
        val notificationManager=NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID,builder.build())
    }

    val mapFragment = MapFragment()
    val timerFragment = TimerFragment()
    val courseFragment = CourseFragment()

    var locationPermissionGranted: Boolean = false
    lateinit var dataRepository: DataRepository
    lateinit var bookmarkPresenter: BookmarkPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.v("noti","${this.packageName}")
        selectHour=false
        ringFlag=true
        readFile()

        //툴바 설정
        setupActionBar(R.id.toolbar) {
            setDisplayShowTitleEnabled(false)
        }

        //따릉이 웹 홈페이지 버튼 클릭 리스너 설정
        ddraeungi_home_button.setOnClickListener {
            val ddareungiHome = Uri.parse("https://www.bikeseoul.com")
            val webIntent = Intent(Intent.ACTION_VIEW, ddareungiHome)
            startActivity(webIntent)
        }

        //구글맵 로드 속도 개선 위해서 dummy map 실행
        val dummyMapsInitializer = SupportMapFragment()
        supportFragmentManager.beginTransaction().attach(dummyMapsInitializer).commit()
        dummyMapsInitializer.getMapAsync {}

        //로딩 해온 데이터 및 위치 권한 받음
        val holderId = intent.getStringExtra(DATA_REPOSITORY_ID)
        dataRepository = DataRepositoryHolder.popDataRepository(holderId)
        locationPermissionGranted = intent.getBooleanExtra(LOCATION_PERMISSION_ID, false)

        setUpBottomNav()

        //기본 fragment로 bookmarkFragment 생성
        val bookmarkFragment = BookmarkFragment.newInstance().also { fragment ->
            replaceFragmentInActivity(fragment, R.id.fragment_container)
        }
        bookmarkPresenter = BookmarkPresenter(dataRepository, bookmarkFragment, locationPermissionGranted)

    }

    fun readFile() {
        val scan = Scanner(resources.openRawResource(R.raw.courseinfo))
        while (scan.hasNextLine()) {

            val title = scan.nextLine()
            val subtitle = scan.nextLine()
            val bikestop = scan.nextLine()
            val location = scan.nextLine()
            val open = scan.nextLine()
            val tel = scan.nextLine()
            val data = CourseInfo(title, subtitle, bikestop, location, tel, open)
            courseInfoList.add(data)
        }

        val scan0 = Scanner(resources.openRawResource(R.raw.coursename))
        while (scan0.hasNextLine()) {

            val subtitle = scan0.nextLine()
            val title = scan0.nextLine()
            val length = scan0.nextLine()
            val time = scan0.nextLine()
            val data = Course(title, subtitle, length, time)
            courseList.add(data)
        }
    }

    //GPS나 네트워크 켜져 있는지 broadcast receiver로 확인
    //상태 바뀌면 플래그 변수 값 바꿔서 프래그먼트로 넘겨줌
//    fun checkUserState() {
//        val gpsIntentFilter = IntentFilter()
//        gpsIntentFilter.addAction("android.location.PROVIDERS_CHANGED")
//        val gpsReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                val lm = getSystemService(LOCATION_SERVICE) as LocationManager
//                enabledGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                mapFragment.mEnableGPS = enabledGPS
//                bookmarkFragment.enableGPS = enabledGPS
//                mapFragment.setGPSWidget()
//            }
//        }
//        registerReceiver(networkReceiver, intentFilter)
//        registerReceiver(gpsReceiver, gpsIntentFilter)
//
//        //처음 어플을 실행 했을 때 네트워크 및 GPS 상태를 확인
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connectivityManager.activeNetworkInfo
//        networkState = networkInfo != null && networkInfo.isConnected
//        mapFragment.networkState = networkState
//        bookmarkFragment.networkState = networkState
//
//        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
//        enabledGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//        mapFragment.mEnableGPS = enabledGPS
//        bookmarkFragment.enableGPS = enabledGPS
//    }

    fun setUpBottomNav() {
        bottom_nav_view.setOnNavigationItemSelectedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (it.itemId) {
                R.id.bookmark -> {
                    if(fragment !is BookmarkFragment) {
                        val bookmarkFragment = BookmarkFragment.newInstance().also { bookmarkFragment ->
                            replaceFragmentInActivity(bookmarkFragment, R.id.fragment_container)
                        }
                        bookmarkPresenter = BookmarkPresenter(dataRepository, bookmarkFragment, locationPermissionGranted)
                    }
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
}
