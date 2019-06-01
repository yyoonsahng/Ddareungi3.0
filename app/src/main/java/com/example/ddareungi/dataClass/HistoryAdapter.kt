package com.example.ddareungi.dataClass

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.ddareungi.R

class HistoryAdapter(val items: ArrayList<History>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun OnItemClick(holder: ViewHolder, view: View, data: History, position: Int)
    }

    var itemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HistoryAdapter.ViewHolder {
        val v = LayoutInflater.from(p0.context)
            .inflate(R.layout.previouslist_layout, p0, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(p0: HistoryAdapter.ViewHolder, p1: Int) {
        p0.recentHistory.text = items[p1].recent.toString()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recentHistory: TextView

        init {
            recentHistory = itemView.findViewById(R.id.rental_name)
            itemView.setOnClickListener {
                val position = adapterPosition
                itemClickListener?.OnItemClick(this, it, items[position], position)
            }

        }
    }

}
