package com.example.ddareungi


import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ddareungi.MainActivity.Companion.dataRepository
import com.example.ddareungi.data.Bike
import com.example.ddareungi.data.Spot
import com.example.ddareungi.util.OnSwipeTouchListener
import com.example.ddareungi.util.RequestHttpURLConnection
import com.example.ddareungi.util.checkCallPermission
import com.example.ddareungi.util.requestCallPermission
import kotlinx.android.synthetic.main.fragment_spot_detail.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder


class SpotDetailFragment : Fragment() {

    var sList = mutableListOf<Spot>()
    var num: Int = 0
    var index = 0
    var preclk=false

    fun showPathInNaverMap(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val list: MutableList<ResolveInfo> =
            activity!!.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list.isEmpty()) {
            //네이버 지도 앱이 깔려있지 않은 경우에 플레이 스토어로 연결
            context!!.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap"))
            )
        } else {
            context!!.startActivity(intent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        index = this.arguments!!.getInt("index")

        return inflater.inflate(R.layout.fragment_spot_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init(index)
    }

    fun init(code: Int) {


        sList.clear()
        num = 0 //현재 보여주는 스팟 번호(지역구를 새로 선택했으니 0으로 초기화)
        val networkTask0 =
            SpotDetailFragment.NetworkTask(code, sList, num, this) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...

        networkTask0.execute()
        if (networkTask0.getStatus() != AsyncTask.Status.FINISHED)
            num++
        if (num == sList.size) {
            num = 0
        }
       spotTitle.isSelected=true
        // ***따릉이 경로 버튼***
        spotPathBtn.setOnClickListener {
            val networkTask0 =
                SpotDetailFragment.NetworkTask(-1, sList, num, this) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...

            //목적지
            val dlat=sList[num].mapY
            val dlng=sList[num].mapX
            val dname=URLEncoder.encode(sList[num].title,"UTF-8")
            //경유지
            val closetBikeStation =networkTask0.findClsBikeStp(sList[num].mapX, sList[num].mapY)
            val vlat = closetBikeStation.stationLatitude
            val vlng = closetBikeStation.stationLongitude
            val vname = URLEncoder.encode(closetBikeStation.stationName, "UTF-8")
            val url =
                "nmap://route/bicycle?dlat=$dlat&dlng=$dlng&dname=$dname&v1lat=$vlat&v1lng=$vlng&v1name=$vname&appname=com.example.ddareungi"
            showPathInNaverMap(url)
        }


        spotHomeTxt.setOnClickListener {
            if(spotHomeTxt.length()>0){
                val webpage= Uri.parse("http://"+spotHomeTxt.text.toString())
                val webIntent= Intent(Intent.ACTION_VIEW,webpage)
                startActivity(webIntent)

            }
        }
        spotTelTxt.setOnClickListener {
            if(spotTelTxt.text != "정보 없음"){
                if(checkCallPermission()) {
                    val number = Uri.parse("tel:" + spotTelTxt.text.toString())
                    val callIntent = Intent(Intent.ACTION_DIAL, number)
                    startActivity(callIntent)
                } else {
                    (activity as AppCompatActivity).requestCallPermission()
                }
            }
        }
        spotPreBtn.setOnClickListener {


        }
        spotNextBtn.setOnClickListener {
            //옆으로 넘겼다는건 이미 관광 게시물을 하나 봤다는 의미니까 sList에 데이터 존재함

        }

        spotMoreBtn.setOnClickListener {

            if (num > 0 && !preclk) {
                num--
            }
            else if(num<sList.size&&preclk){
                num++
            }
            val networkTask0 =
                SpotDetailFragment.NetworkTask(-1, sList, num, this) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...
            networkTask0.execute()
            val str = sList[num].overview
            popup(str)

            if (!preclk) {
                num++
             }
            else{
                num--
             }


        }

       frameView.setOnTouchListener (object :OnSwipeTouchListener(this){

           override fun onSwipeLeft() {//다음 화면
             Log.i("touch","touch1:$num")

               preclk=false

               num++
               if (num == sList.size) {
                   num = 0
               }

               val networkTask0 =
                   SpotDetailFragment.NetworkTask(-1, sList, num, this@SpotDetailFragment) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...
               networkTask0.execute()


           }

           override fun onSwipeRight() {
               Log.i("touch","touch2:$num")

               num--
               if (num < 0) {

                   val fragment=CourseFragment()
                   activity!!.supportFragmentManager.beginTransaction()
                       .replace(R.id.fragment_container, fragment)
                       .commit()

                   num=0
               }

               preclk=true


               val networkTask0 =
                   SpotDetailFragment.NetworkTask(-1, sList, num, this@SpotDetailFragment) //선택된 구에 따라서 구 코드 달라짐 ex. 강남구 1  강동구 2 ,...
               networkTask0.execute()


           }
       })

    }

    fun popup(n: String) {
        val popupView = layoutInflater.inflate(R.layout.spotdetailpopup, null)
        val popupWindow =
            PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        val spotDetailTxt = popupView.findViewById<TextView>(R.id.spotDetailTxt)
        //if(sList.isNotEmpty()){
        spotDetailTxt.text = n
        spotDetailTxt.movementMethod=ScrollingMovementMethod()

        //}
        val closeBtn = popupView.findViewById<Button>(R.id.spotClsBtn)
        closeBtn.setOnClickListener {
            popupWindow.dismiss()
        }
    }


    class NetworkTask(var code: Int, var sList: MutableList<Spot>, var num: Int, var mFrag: SpotDetailFragment) :
        AsyncTask<Unit, Unit, String>() {

        var url = arrayOf(
            "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?ServiceKey=cBjFR2LOycFyN6y%2FoSOb9jqx0YXqt1UBL5cjKWV8zPmenCK%2BfBuboT88uCRofXgQbKdQNC5yMBed%2FYHA7j9JNw%3D%3D&areaCode=1&contentTypeId=12&MobileOS=AND&MobileApp=Ddareungi3.0&_type=json&sigunguCode=",
            "http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailCommon?ServiceKey=cBjFR2LOycFyN6y%2FoSOb9jqx0YXqt1UBL5cjKWV8zPmenCK%2BfBuboT88uCRofXgQbKdQNC5yMBed%2FYHA7j9JNw%3D%3D&defaultYN=Y&overviewYN=Y&MobileOS=AND&MobileApp=TestApp&_type=json&contentId="
        )

        override fun doInBackground(vararg params: Unit?): String {
            var spotData = ""
            if (sList.size == 0) { //처음 클릭할 때
                var res = RequestHttpURLConnection().request(url[0] + code.toString() + "&numOfRows=0")

                var jobj = JSONObject(res).getJSONObject("response").getJSONObject("body")
                var totalCount = jobj.optInt("totalCount")

                var result =
                    RequestHttpURLConnection().request(url[0] + code.toString() + "&numOfRows=" + totalCount.toString())
                parsingSpotList(result)
            }

            spotData = RequestHttpURLConnection().request(url[1] + sList[num].contentid)

            return spotData
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            var jobj = JSONObject(result).getJSONObject("response").getJSONObject("body").getJSONObject("items")
                .getJSONObject("item")
            try {
                sList[num].tel = jobj.optString("tel")
            } catch (e: JSONException) {
                sList[num].tel = "정보 없음"
            }

            try {
                   sList[num].homepage =
                    jobj.optString("homepage").substringAfterLast("http://").substringBefore("&").substringBefore("<").substringBefore("/","정보 없음")
            } catch (e: JSONException) {
                sList[num].homepage = "정보없음"
            }

            sList[num].overview=jobj.optString("overview")
            sList[num].deleteTag()
            showSpot()

        }

        fun parsingSpotList(data: String) {
            var jarray = JSONObject(data).getJSONObject("response").getJSONObject("body").getJSONObject("items")
                .getJSONArray("item")

            for (i in 0..jarray.length() - 1) {
                var jObject = jarray.getJSONObject(i)
                val contentid: Int = jObject.optInt("contentid")
                var imgOrigin: String = jObject.optString("firstimage")
                var imgThumb: String = jObject.optString("firstimage2")
                val mapX: Double = jObject.optDouble("mapx")
                val mapY: Double = jObject.optDouble("mapy")
                val title: String = jObject.optString("title")

                sList.add(
                    Spot(contentid, imgOrigin, imgThumb, mapX, mapY, title, "tel", "homepage", "overview")
                )

            }
        }

        fun showSpot() {
            //어댑터 연결

            if(mFrag.isAdded) {
                val spotImgView = mFrag.activity!!.findViewById<ImageView>(R.id.spotImgView)
                val spotTitle = mFrag.activity!!.findViewById<TextView>(R.id.spotTitle)
                val spotTelTxt = mFrag.activity!!.findViewById<TextView>(R.id.spotTelTxt)
                val spotHomeTxt = mFrag.activity!!.findViewById<TextView>(R.id.spotHomeTxt)
                val spotBikeTxt = mFrag.activity!!.findViewById<TextView>(R.id.spotBikeTxt)

                Glide.with(mFrag.activity!!.applicationContext)
                    .load(sList[num].imgOrigin)
                    .apply(RequestOptions().placeholder(R.drawable.ic_action_img))
                    .into(spotImgView)


                spotTitle.text = sList[num].title

                //***근처 따릉이 대여소!!***

                spotBikeTxt.text=findClsBikeStp(sList[num].mapX,sList[num].mapY).stationName


                spotHomeTxt.text = sList[num].homepage
                if (sList[num].tel.length > 4) {
                    spotTelTxt.visibility = View.VISIBLE
                    spotTelTxt.text = sList[num].tel
                } else {
                    spotTelTxt.visibility = View.GONE
                }
            }

        }
        fun findClsBikeStp(mapX:Double,mapY:Double):Bike{


            val dest = Location("dest")
            dest.latitude = mapY
            dest.longitude = mapX

            var closetBikeStation = Bike.newInstance()
            var dist =Float.MAX_VALUE

            for(bike in dataRepository.bikeList) {
                Log.i("dest4",bike.stationName)
                var bikeStation = Location("bike")
                bikeStation.latitude = bike.stationLatitude
                bikeStation.longitude = bike.stationLongitude
                var tempDist: Float
                tempDist = dest.distanceTo(bikeStation)
                if (dist > tempDist) {
                    dist = tempDist
                    closetBikeStation = bike
                }
            }

            return closetBikeStation
        }


    }
}

