package com.example.ddareungi

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log

class courseInfoPager : AppCompatActivity() {


     var index=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index=intent.getIntExtra("index",-1)
        Log.v("index",index.toString())
        setContentView(R.layout.activity_course_info_pager)
        init()
    }
    fun init(){

        val pagerAdapter=crsViewPager(supportFragmentManager,index)
        val pager=findViewById<ViewPager>(R.id.container)
        pager.adapter=pagerAdapter

    }
}
