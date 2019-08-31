package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.ddareungi.data.Spot
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray

/*
*  구 선택 -> 해당 구ㅇ에 있는 관광지 목록 파싱 -> 첫 번쨰 관광지에 대한 내용 파싱 -> 보여주기 형식
* 현재 프레그먼트는 강남구/ 강동구 두 개를 예시로 해두었웁니다. 실제로 레이아웃짜시면 함수만 가져다가 쓰시면 됨니다
* 구현한 함수
* 1) 관광지 정보를 얻기 위해 구를 선택할 때
* 2) 원하는 구를 선택한 이후 다음 정보를 얻기 위해 옆으로 넘길 때
*
* */


class CourseFragment() : Fragment() {

    val zone= mutableMapOf<Int,String>(1 to "강남구",2 to "강동구", 3 to "강북구", 4 to "강서구", 5 to "관악구", 6 to "광진구", 7 to "구로구", 8 to "금천구", 9 to "노원구", 10 to "도봉구", 11 to "동대문구", 12 to "동작구", 13 to " 마포구", 14 to "서대문구", 15  to "서초구", 16 to "성동구", 17 to "성북구", 18 to "송파구", 19 to "양천구", 20 to "영등포구", 21 to "용산구", 22 to "은평구", 23 to "종로구", 24 to "중구", 25 to "중랑구")
    var sList=mutableListOf<Spot>()
    lateinit var jarray:JSONArray
    var num:Int=0
    val btnArray=arrayListOf<Button>()

    /*numOfRows*/


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.spotmenu_gu, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.appbar_title.text = "추천 관광지"
    }

    var index=0
    var datarepoid=0

    fun init(){
        val idTypeArray= resources.obtainTypedArray(R.array.button)

        for(i in 0..24){

            var view=activity!!.findViewById<Button>(idTypeArray.getResourceId(i,0))
            view.setOnClickListener {
                val bundle=Bundle()
                index=i+1
                bundle.putInt("index",index)
                bundle.putInt("id",datarepoid)
                val fragment = spotDetailFragment()
                fragment.arguments=bundle
                loadFragment(fragment)
            }
        }

    }


        fun loadFragment(fragment: Fragment) {
            activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

}