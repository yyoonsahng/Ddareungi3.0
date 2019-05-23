package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teamproject_2019_1.Course
import com.example.teamproject_2019_1.CourseAdapter


class CourseFragment : Fragment() {

    var data: ArrayList<Course> = ArrayList()
    lateinit var adapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    fun init() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val crsCards = activity!!.findViewById<RecyclerView>(R.id.crsCards)
        crsCards.layoutManager = layoutManager
        adapter = CourseAdapter(data, activity!!)
        crsCards.adapter = adapter
        data.add(0, Course("추천 코스 2: 양재 시민의 숲 코스", "거리: 7.83 km", "예상 소요 시간 : 30-40 분"))
        data.add(0, Course("추천 코스 1 : 여의도 ~ 반포 코스", "거리: 11.05 km", "예상 소요 시간 : 44 분"))

        adapter.notifyDataSetChanged()

    }
}
