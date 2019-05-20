package com.example.teamproject_2019_1

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.cards.*

class MainActivity : AppCompatActivity() {

    var data:ArrayList<course> = ArrayList()
    lateinit var adapter:courseAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cards)
        init()
    }
    fun init(){
        val layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        crsCards.layoutManager=layoutManager
        adapter= courseAdapter(data)
        crsCards.adapter=adapter
        data.add(0,course("추천 코스 4: 광화문 코스","거리: 7km","예상 소요 시간 : 50-60 분"))
        data.add(0,course("추천 코스 3: 상암동 코스","거리: 9.7 km","예상 소요 시간 : 50-60 분"))
        data.add(0,course("추천 코스 2: 양재 시민의 숲 코스","거리: 7.83 km","예상 소요 시간 : 30-40 분"))
        data.add(0,course("추천 코스 1 : 여의도 ~ 반포 코스","거리: 11.05 km","예상 소요 시간 : 44 분"))

        adapter.notifyDataSetChanged()
    }

}
