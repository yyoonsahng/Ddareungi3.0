package com.example.ddareungi

import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.ddareungi.dataClass.MySpot

class spotCardAdapter (var items: MutableList<MySpot>, val activity: FragmentActivity?) : RecyclerView.Adapter<spotCardAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): spotCardAdapter.ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.spotcard, p0, false)
        return ViewHolder(v)    }
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        p0.spotTitle.text=items.get(p1).title
        //spotImg
        p0.spotDetail.text=items.get(p1).overview
        p0.spotTel.text=items.get(p1).tel
        p0.spotHome.text=items.get(p1).homepage

        //image
        Glide.with(activity!!.applicationContext)
            .load(items.get(p1).imgOrigin)
            .into(p0.spotImg)

        /*
        val url="http://tong.visitkorea.or.kr/cms/resource/57/2031357_image2_1.jpg"
        Glide.with(this.applicationContext).load(url).into(testImg)
        */
    }

    override fun getItemCount(): Int {
        return items.size    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var spotTitle: TextView
        var spotMoreBtn:ImageButton
        var spotImg:ImageView
        var spotDetail:TextView
        var spotBStop:TextView
        var spotTel:TextView
        var spotHome:TextView


        init {
            spotTitle=itemView.findViewById(R.id.spotTitle)
            spotMoreBtn=itemView.findViewById(R.id.spotMoreBtn)
            spotImg=itemView.findViewById(R.id.spotImg)
            spotDetail=itemView.findViewById(R.id.spotDetail)
            spotBStop=itemView.findViewById(R.id.spotBStop)
            spotTel=itemView.findViewById(R.id.spotTel)
            spotHome=itemView.findViewById(R.id.spotHome)
            spotMoreBtn.setOnClickListener {

                if(spotDetail.visibility==GONE){//moreBtn
                    spotMoreBtn.setBackgroundResource(R.drawable.ic_expand_less_black_24dp)
                    spotDetail.visibility= VISIBLE

                }else{
                    spotMoreBtn.setBackgroundResource(R.drawable.ic_expand_more_black_24dp)
                    spotDetail.visibility= GONE
                }
            }
        }

    }

}