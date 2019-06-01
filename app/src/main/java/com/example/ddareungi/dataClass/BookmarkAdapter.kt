package com.example.ddareungi.dataClass

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.ddareungi.R

class BookmarkAdapter(val items: ArrayList<Bookmark>) : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {


    interface OnItemClickListener {
        fun OnItemClick(holder: ViewHolder, view: View, data: Bookmark, position: Int)
    }
    var itemClickListener: OnItemClickListener? = null

    fun removeItem(pos: Int) {//position정보, swipe하면 없어지게
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): BookmarkAdapter.ViewHolder {
        val v = LayoutInflater.from(p0.context)
            .inflate(R.layout.bookmarklist_layout, p0, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(p0: BookmarkAdapter.ViewHolder, p1: Int) {
        p0.rentalName.text = items[p1].rentalOffice
        val leftBike = items[p1].leftBike.toString() + "대"
        p0.leftBike.text = leftBike
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rentalName: TextView
        var leftBike: TextView
        var viewForeground:LinearLayout
        var viewBackground:LinearLayout

        init {
            rentalName = itemView.findViewById(R.id.rental_name)
            leftBike = itemView.findViewById(R.id.num_of_bike)
            viewForeground = itemView.findViewById(R.id.foregroundView)
            viewBackground = itemView.findViewById(R.id.backgroundView)
            itemView.setOnClickListener{
                val position = adapterPosition
                itemClickListener?.OnItemClick(this, it, items[position], position)
            }

        }
    }

}