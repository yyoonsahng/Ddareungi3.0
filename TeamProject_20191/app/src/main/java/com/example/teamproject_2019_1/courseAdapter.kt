package com.example.teamproject_2019_1

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class courseAdapter(var items:ArrayList<course>)
    :RecyclerView.Adapter<courseAdapter.ViewHolder>(){
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): courseAdapter.ViewHolder {
val v=LayoutInflater.from(p0.context).inflate(R.layout.crscard,p0,false)
        return  ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(p0: courseAdapter.ViewHolder, p1: Int) {
        p0.title.text=items.get(p1).title
        p0.time.text=items.get(p1).time
        p0.length.text=items.get(p1).length
        when(p1){
            0->{
                p0.crsImage.setImageResource(R.drawable.bicycle)

            }
            1->{
                p0.crsImage.setImageResource(R.drawable.cyclist)
            }
            2->{
            p0.crsImage.setImageResource(R.drawable.course2)
        }
            3->{
                p0.crsImage.setImageResource(R.drawable.course)
            }
        }

    }

    inner class ViewHolder(itemView: View)
        :RecyclerView.ViewHolder(itemView){
        var title:TextView
        var length:TextView
        var crsImage:ImageView
        var icon:ImageView
        var time:TextView
        var click:Boolean

        var cardInfo:LinearLayout
        init{
            cardInfo=itemView.findViewById(R.id.crsInfo)
            title=itemView.findViewById(R.id.crsTitle)
            length=itemView.findViewById(R.id.crsLength)
            time=itemView.findViewById(R.id.crsTime)
            crsImage=itemView.findViewById(R.id.crsImage)
            icon=itemView.findViewById(R.id.crsIcon)
            click=false
            icon.setOnClickListener {
                if(click){
                      icon.setImageResource(R.drawable.ic_expand_more_black_24dp)
                       cardInfo.visibility=LinearLayout.GONE
                    click=!click
                }else{
                    icon.setImageResource(R.drawable.ic_expand_less_black_24dp)
                    cardInfo.visibility=LinearLayout.VISIBLE
                    click=!click
                }

            }
        }
    }
}