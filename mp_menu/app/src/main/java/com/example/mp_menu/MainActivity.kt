package com.example.mp_menu

import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val fragmentmanager = supportFragmentManager.findFragmentById(R.id.container)
    val bookmarkFragment = BookmarkFragment()
    val mapFragment = MapFragment()
    val timerFragment = TimerFragment()
    val courseFragment = CourseFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment()
        init()
    }

    fun init() {
        bottom_navigation.setOnNavigationItemSelectedListener {
            val transaction = supportFragmentManager.beginTransaction()
            when (it.itemId) {
                R.id.bookmark -> {
                    transaction.replace(R.id.container, bookmarkFragment)
                    transaction.commit()
                }
                R.id.map -> {
                    transaction.replace(R.id.container, mapFragment)
                    transaction.commit()
                }
                R.id.timer -> {
                    transaction.replace(R.id.container, timerFragment)
                    transaction.commit()
                }
                R.id.course -> {
                    transaction.replace(R.id.container, courseFragment)
                    transaction.commit()
                }

            }
            true
        }
    }

    fun fragment() {
        val bottomNavigationView = findViewById<BottomNavigationItemView>(R.id.bottom_navigation)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, bookmarkFragment)
        transaction.commit()
    }
}
