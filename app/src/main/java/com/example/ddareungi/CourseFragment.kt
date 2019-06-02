package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.course.myPagerAdapter

class CourseFragment : Fragment() {


 companion object {
     private val ARG_CAUGHT="fragment_caught"

     fun newInstance():CourseFragment{
         val args=Bundle()
         val frag=CourseFragment()
         frag.arguments=args
         return frag
     }
 }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course, container, false)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    /*    data.add(0, Course("추천 코스 2: 양재 시민의 숲 코스", "거리: 7.83 km", "예상 소요 시간 : 30-40 분"))
        data.add(0, Course("추천 코스 1 : 여의도 ~ 반포 코스", "거리: 11.05 km", "예상 소요 시간 : 44 분"))*/
        val pagerAdapter= myPagerAdapter(activity!!.supportFragmentManager)
        val pager=activity!!.findViewById<ViewPager>(R.id.crsViewPager)
        pager.adapter=pagerAdapter

    }

}