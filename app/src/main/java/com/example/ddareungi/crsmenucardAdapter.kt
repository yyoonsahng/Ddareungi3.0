package com.example.ddareungi

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.ddareungi.data.Course

class crsmenucardAdapter( var items: ArrayList<Course>, val activity: FragmentActivity) : RecyclerView.Adapter<crsmenucardAdapter.ViewHolder>() {

 lateinit var intent:Intent
    var index=0
    val drawableTypeArray=activity!!.resources.obtainTypedArray(R.array.drawable)
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): crsmenucardAdapter.ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.coursemenucard, p0, false)
        return ViewHolder(v)    }

    override fun getItemCount(): Int {
        return items.size    }

    override fun onBindViewHolder(p0: crsmenucardAdapter.ViewHolder, p1: Int) {

        p0.crsmenucardtitle.text=items.get(p1).subtitle
        p0.crsmenucardsubtitle.text=items.get(p1).title
        try {
            p0.crsmenucardimg.setImageResource(drawableTypeArray.getResourceId(p1 * 5, -1))
        }
        catch(e:Exception){
            p0.crsmenucardimg.setImageResource(R.drawable.crs53)
        }

    }
    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var crsmenucardimg:ImageView
        var crsmenucardtitle:TextView
        var crsmenucardsubtitle:TextView
        init {
            crsmenucardimg=itemView.findViewById(R.id.crsmenucardimg)
            crsmenucardtitle=itemView.findViewById(R.id.crsmenucardsubtitle)
            crsmenucardsubtitle=itemView.findViewById(R.id.crsmenucardtitle)
            itemView.setOnClickListener {

                val i=adapterPosition

                //메뉴 카드뷰 레이아웃 인텐트 생성
                intent=Intent(activity,courseInfoPager::class.java)
                intent.putExtra("index",adapterPosition)//position찾기
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                startActivity(activity.applicationContext,intent,null)
            }
        }

    }

}