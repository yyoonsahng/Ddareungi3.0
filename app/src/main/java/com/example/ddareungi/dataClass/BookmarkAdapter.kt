package com.example.ddareungi.dataClass

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    fun moveItem(pos1: Int, pos2: Int) {
        val item1 = items[pos1]//position
        val item2 = items[pos2]
        //지우고 다시 넣어준다.
        items.removeAt(pos1)//옮기려는걸 먼저 지운다
        items.add(pos2, item1)//그리고 원하는 pos에 추가하면 알아서 밑에 있는것은 밀리게됨
        //position이 바뀌었음을 알려준다.
        notifyItemMoved(pos1, pos2)
    }



    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): BookmarkAdapter.ViewHolder {
        val v = LayoutInflater.from(p0.context)
            .inflate(R.layout.bookmarklist_layout, p0, false)
        return ViewHolder(v)

        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        return items.size
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun onBindViewHolder(p0: BookmarkAdapter.ViewHolder, p1: Int) {
        p0.rentalName.text = items.get(p1).rentalOffice.toString()
        p0.leftBike.text = items.get(p1).leftBike.toString()
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rentalName: TextView
        var leftBike: TextView

        init {
            rentalName = itemView.findViewById(R.id.rental_name)
            leftBike = itemView.findViewById(R.id.num_of_bike)
            itemView.setOnClickListener{
                val position = adapterPosition
                itemClickListener?.OnItemClick(this, it, items[position], position)
            }

        }
    }

}