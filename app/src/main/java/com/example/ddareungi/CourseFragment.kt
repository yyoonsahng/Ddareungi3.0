package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ddareungi.dataClass.Course
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

        adapter.notifyDataSetChanged()
    }

}