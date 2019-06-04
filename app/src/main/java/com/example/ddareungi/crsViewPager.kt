package com.example.ddareungi

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class crsViewPager(fm:FragmentManager,var index:Int):FragmentPagerAdapter(fm) {


    val PAGE_CNT=5
    override fun getCount(): Int {
        return PAGE_CNT
    }

    override fun getItem(p0: Int): Fragment {

        val bundle=Bundle()
        bundle.putInt("index",index)
        val fragment:Fragment=when(p0){

            1->{crsVP1Fragment()}
            2->{crsVP2Fragment() }
            3->{crsVP3Fragment()}
            4->{crsVP4Fragment()}
            else->{crsVP_title()}//제목 페이지
        }
        fragment.arguments=bundle
        return fragment
    }
}