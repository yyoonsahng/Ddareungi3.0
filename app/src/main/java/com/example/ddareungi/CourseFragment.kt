package com.example.ddareungi


import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.ddareungi.data.Spot
import com.example.ddareungi.util.RequestHttpURLConnection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.temp_courslayout.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
/*
*  구 선택 -> 해당 구ㅇ에 있는 관광지 목록 파싱 -> 첫 번쨰 관광지에 대한 내용 파싱 -> 보여주기 형식
* 현재 프레그먼트는 강남구/ 강동구 두 개를 예시로 해두었웁니다. 실제로 레이아웃짜시면 함수만 가져다가 쓰시면 됨니다
* 구현한 함수
* 1) 관광지 정보를 얻기 위해 구를 선택할 때
* 2) 원하는 구를 선택한 이후 다음 정보를 얻기 위해 옆으로 넘길 때
*
* */


class CourseFragment : Fragment() {

    val zone= mutableMapOf<Int,String>(1 to "강남구",2 to "강동구", 3 to "강북구", 4 to "강서구", 5 to "관악구", 6 to "광진구", 7 to "구로구", 8 to "금천구", 9 to "노원구", 10 to "도봉구", 11 to "동대문구", 12 to "동작구", 13 to " 마포구", 14 to "서대문구", 15  to "서초구", 16 to "성동구", 17 to "성북구", 18 to "송파구", 19 to "양천구", 20 to "영등포구", 21 to "용산구", 22 to "은평구", 23 to "종로구", 24 to "중구", 25 to "중랑구")
    var sList=mutableListOf<Spot>()
    lateinit var jarray:JSONArray
    var num:Int=0
    /*numOfRows*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.temp_courslayout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btn_gu1.setOnClickListener {

            sList.clear()
            num=0 //현재 보여주는 스팟 번호(지역구를 새로 선택했으니 0으로 초기화)
            val networkTask0 = NetworkTask(1,sList,num,activity) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...
            networkTask0.execute()
            if(networkTask0.getStatus() != AsyncTask.Status.FINISHED)
            num++
            if(num==sList.size) {
                num = 0
            }
        }
        btn_gu2.setOnClickListener {
            sList.clear()
            num=0
            val networkTask0 = NetworkTask(2,sList,num,activity) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...
            networkTask0.execute()
            num++
            if(num==sList.size) {
                num = 0
            }
        }
        btnNext.setOnClickListener { //옆으로 넘겼다는건 이미 관광 게시물을 하나 봤다는 의미니까 sList에 데이터 존재함
            val networkTask0 = NetworkTask(-1,sList,num,activity) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...
            networkTask0.execute()
            num++
            if(num==sList.size) {
                num = 0
            }
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.appbar_title.text = "추천 코스"
    }
    class NetworkTask(var code:Int, var sList:MutableList<Spot>, var num:Int, var mActivity:FragmentActivity?) : AsyncTask<Unit, Unit, String>(){

        var url=arrayOf(
            "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?ServiceKey=cBjFR2LOycFyN6y%2FoSOb9jqx0YXqt1UBL5cjKWV8zPmenCK%2BfBuboT88uCRofXgQbKdQNC5yMBed%2FYHA7j9JNw%3D%3D&areaCode=1&contentTypeId=12&MobileOS=AND&MobileApp=Ddareungi3.0&_type=json&sigunguCode=",
            "http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailCommon?ServiceKey=cBjFR2LOycFyN6y%2FoSOb9jqx0YXqt1UBL5cjKWV8zPmenCK%2BfBuboT88uCRofXgQbKdQNC5yMBed%2FYHA7j9JNw%3D%3D&defaultYN=Y&overviewYN=Y&MobileOS=AND&MobileApp=TestApp&_type=json&contentId="
            )

        override fun doInBackground(vararg params: Unit?): String {
            var spotData=""
            if(sList.size==0) { //처음 클릭할 때
                var res = RequestHttpURLConnection().request(url[0] + code.toString()+"&numOfRows=0")
                Log.i("api",res)
                var jobj = JSONObject(res).getJSONObject("response").getJSONObject("body")
                var totalCount = jobj.optInt("totalCount")

                var result = RequestHttpURLConnection().request(url[0] + code.toString() + "&numOfRows=" + totalCount.toString())
                parsingSpotList(result)
            }
            Log.i("api",num.toString())
            spotData= RequestHttpURLConnection().request(url[1]+sList[num].contentid)

            return spotData
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            var jobj=JSONObject(result).getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONObject("item")
            try {
                sList[num].tel = jobj.optString("tel")
            }
            catch (e: JSONException)
            {
                sList[num].tel = "정보없음"
            }
            try {
                sList[num].homepage = jobj.optString("homepage")
            }
            catch (e: JSONException)
            {
                sList[num].homepage = "정보없음"
            }
            sList[num].overview=jobj.optString("overview")

            showSpot()
        }

        fun parsingSpotList(data:String){
            var jarray= JSONObject(data).getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item")

            for (i in 0..jarray.length()-1) {
                var jObject = jarray.getJSONObject(i)
                val contentid: Int = jObject.optInt("contentid")
                var imgOrigin:String=jObject.optString("firstimage")
                var imgThumb:String=jObject.optString("firstimage2")
                val mapX:Double=jObject.optDouble("mapx")
                val mapY:Double=jObject.optDouble("mapy")
                val title:String=jObject.optString("title")
                sList.add(
                    Spot(contentid,imgOrigin,imgThumb,mapX,mapY,title,"tel","homepage","overview")
                )

            }
        }

        fun showSpot(){
            val spotText=mActivity!!.findViewById<TextView>(R.id.spotText)
            spotText.text=sList[num].title+"\n"+sList[num].tel+"\n"+sList[num].overview
        }
    }
}