package com.example.ddareungi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.ddareungi.bookmark.BookmarkFragment
import com.example.ddareungi.bookmark.BookmarkPresenter
import com.example.ddareungi.data.DataRepositoryHolder
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.map.MapFragment
import com.example.ddareungi.map.MapPresenter
import com.example.ddareungi.timer.TimerFragment
import com.example.ddareungi.util.replaceFragmentInActivity
import com.example.ddareungi.util.setupActionBar
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    interface BackButtonListener {
        fun onBackPressed()
    }


    var backButtonListener: BackButtonListener? = null
    var locationPermissionGranted: Boolean = false
    //lateinit var dataRepository: DataRepository
    lateinit var bookmarkPresenter: BookmarkPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    private fun setUpBottomNav() {
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

    fun setBackButtonPressedListener(listener: BackButtonListener) {
        this.backButtonListener = listener
    }

    override fun onBackPressed() {
        if(backButtonListener != null)
            backButtonListener!!.onBackPressed()
        else
            super.onBackPressed()
    }

    companion object {
        lateinit var dataRepository: DataRepository

        const val DATA_REPOSITORY_ID = "DATA_REPOSITORY_ID"
        const val LOCATION_PERMISSION_ID = "LOCATION_PERMISSION_ID"
    }
}