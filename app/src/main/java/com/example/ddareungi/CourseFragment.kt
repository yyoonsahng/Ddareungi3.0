package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ddareungi.MainActivity.Companion.courseList
import kotlinx.android.synthetic.main.activity_main.*


class CourseFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.v("doing","cardviewList")
        super.onActivityCreated(savedInstanceState)
        val layoutManager= GridLayoutManager(activity,2)
        if(layoutManager!=null)
            Log.v("doing","layoutmanager")
        val coursemenu=activity!!.findViewById<RecyclerView>(R.id.CoursemenuList)
        coursemenu.layoutManager=layoutManager

        val adapter=crsmenucardAdapter(courseList,this.requireActivity())
        if(adapter!=null)
            Log.v("doing","adapter")
        coursemenu.adapter=adapter
        if(coursemenu.adapter!=null)
            Log.v("doing","cousemenuadapter")





    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.appbar_title.text = "추천 코스"
    }

}