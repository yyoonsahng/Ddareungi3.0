package com.example.ddareungi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.ddareungi.bookmark.BookmarkFragment
import com.example.ddareungi.bookmark.BookmarkPresenter
import com.example.ddareungi.data.DataRepositoryHolder
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.map.MapFragment
import com.example.ddareungi.map.MapPresenter
import com.example.ddareungi.util.replaceFragmentInActivity
import com.example.ddareungi.util.setupActionBar
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){


    companion object {

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

    var locationPermissionGranted: Boolean = false
    lateinit var dataRepository: DataRepository
    lateinit var bookmarkPresenter: BookmarkPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.v("noti","${this.packageName}")
        selectHour=false
        ringFlag=true

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

        val dummyMapInitializer = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction().attach(dummyMapInitializer).commit()
        dummyMapInitializer.getMapAsync {  }

        //로딩 해온 데이터 및 위치 권한 받음
        val holderId = intent.getStringExtra(DATA_REPOSITORY_ID)
        dataRepository = DataRepositoryHolder.popDataRepository(holderId)
        locationPermissionGranted = intent.getBooleanExtra(LOCATION_PERMISSION_ID, false)

        setUpBottomNav()

        //기본 fragment로 bookmarkFragment 생성
        val bookmarkFragment = BookmarkFragment.newInstance().also { fragment ->
            replaceFragmentInActivity(fragment, R.id.fragment_container, "즐겨찾기")
        }
        bookmarkPresenter = BookmarkPresenter(dataRepository, bookmarkFragment, locationPermissionGranted)

    }

    fun setUpBottomNav() {
        bottom_nav_view.setOnNavigationItemSelectedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (it.itemId) {
                R.id.bookmark -> {
                    if(fragment !is BookmarkFragment) {
                        val bookmarkFragment = BookmarkFragment.newInstance().also {
                            replaceFragmentInActivity(it, R.id.fragment_container, "즐겨찾기")
                        }
                        bookmarkPresenter = BookmarkPresenter(dataRepository, bookmarkFragment, locationPermissionGranted)
                    }
                }
                R.id.map -> {
                    if(fragment !is MapFragment) {
                        val mapFragment = MapFragment().also {
                            replaceFragmentInActivity(it, R.id.fragment_container, "")
                        }
                        val mapPresenter = MapPresenter(dataRepository, mapFragment, false ,"")
                    }
                }
                R.id.timer -> {
                    val timerFragment = TimerFragment().also {
                        replaceFragmentInActivity(it, R.id.fragment_container, "타이머")
                    }
                }
                R.id.course -> {
                    val courseFragment = CourseFragment().also {
                        replaceFragmentInActivity(it, R.id.fragment_container, "추천 관광지")
                    }
                }
            }
            true
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(fragment is MapFragment) {
            if(!fragment.onBackButtonPressed())
                super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }
}
