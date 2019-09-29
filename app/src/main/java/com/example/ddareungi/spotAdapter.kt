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

    override fun getItemCount(): Int {
        return items.size
    }

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





