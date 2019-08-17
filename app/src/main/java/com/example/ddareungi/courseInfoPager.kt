package com.example.ddareungi

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View

class courseInfoPager : AppCompatActivity() {


    var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = intent.getIntExtra("index", -1)
        setContentView(R.layout.activity_course_info_pager)
        init()
    }

    fun init() {
        window.statusBarColor = resources.getColor(R.color.white, null)
        window.decorView.background = resources.getDrawable(R.color.white, null)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val pagerAdapter = crsViewPager(supportFragmentManager, index)
        val pager = findViewById<ViewPager>(R.id.container)
        pager.adapter = pagerAdapter

    }
}