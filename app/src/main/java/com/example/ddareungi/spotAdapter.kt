package com.example.ddareungi

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.ddareungi.data.Spot

class spotAdapter (val items:MutableList<Spot>)
    : RecyclerView.Adapter<spotAdapter.ViewHolder>(){
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v= LayoutInflater.from(p0.context).inflate(R.layout.fragment_spot_detail,p0,false)
        //p0에 레이아웃 카드레이아웃를 연결.
        return ViewHolder(v)
        //반환타입에 맞게 v객체로 viewHolder 만듬 레이아웃 객체만들고 viewholder로
    }

    fun moveItem( position1:Int,  position2:Int){
        val item1=items.get(position1)

        //지우고 다시 넣어주자
        items.removeAt(position1)
        items.add(position2,item1)
        //item data set 변화
        notifyItemMoved(position1,position2)//pos1 에서 pos2로


    }
    fun removeItem(position:Int){
        items.removeAt(position)
        notifyItemRemoved(position)
    }
    override fun getItemCount(): Int {
        return items.size
    }
/*
    private fun findClosestBikeStation(lat: Double, lng: Double): Bike {
        val dest = Location("dest")
        dest.latitude = lat
        dest.longitude = lng

        var closetBikeStation = Bike.newInstance()
        var dist = Float.MAX_VALUE

        for(bike in dataRepository.bikeList) {
            val bikeStation = Location("bike")
            bikeStation.latitude = bike.stationLatitude
            bikeStation.longitude = bike.stationLongitude
            var tempDist: Float
            tempDist = dest.distanceTo(bikeStation)
            if(dist > tempDist) {
                dist = tempDist
                closetBikeStation = bike
            }
        }
        return closetBikeStation
    }
*/
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        //데이터와 뷰를 연결

        p0.spotTitle.text=items.get(p1).title
        if(items.get(p1).tel.length>4)
        {
            p0.spotTelTxt.visibility = View.VISIBLE
            p0.spotTelTxt.text = items.get(p1).tel
        }
        else {
            p0.spotTelTxt.visibility=View.GONE
        }
        p0.spotHomeTxt.text=items.get(p1).homepage
/*
        Glide.with()
            .load(items.get(p1).imgOrigin)
            .apply(RequestOptions().placeholder(R.drawable.ic_directions_bike_black_24dp))
            .into(p0.spotImgView)
        */





    }

    inner class ViewHolder(itemView: View)
        :RecyclerView.ViewHolder(itemView) {

        var spotTitle: TextView
        var spotTelTxt: TextView
        var spotHomeTxt: TextView
        var spotImgView: ImageView
        var spotBikeTxt: TextView

        init {
            //init 블록에서 초기화 시켜주장. primary 생성자는 명시적으로 초기화 안 되니까 init블록 이용,, 멤버구성 후 위젯과 연결
            spotTitle = itemView.findViewById(R.id.spotTitle)
            spotTelTxt = itemView.findViewById(R.id.spotTelTxt)
            spotHomeTxt = itemView.findViewById(R.id.spotHomeTxt)
            spotBikeTxt = itemView.findViewById(R.id.spotBikeTxt)
            spotImgView = itemView.findViewById(R.id.spotImgView)
        }

    }




    }





