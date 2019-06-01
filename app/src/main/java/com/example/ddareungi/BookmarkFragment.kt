package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.a190306app.MyBike
import com.example.a190306app.MyDust
import com.example.ddareungi.dataClass.Bookmark
import com.example.ddareungi.dataClass.BookmarkAdapter
import kotlinx.android.synthetic.main.fragment_bookmark.*


class BookmarkFragment : Fragment() {
    var dbHandler: MyDB? = null
    var mBikeList: MutableList<MyBike> = mutableListOf()
    var mDust: MutableList<MyDust> = mutableListOf()
    lateinit var bookmarkArray: ArrayList<Bookmark>
    lateinit var bookmarkMap: MutableMap<String, Bookmark>

    interface BookmarkToMapListener {
        //이미지를 터치하면 changeTextFrag 호출
        fun changeBookmarkToMap(rentalOffice: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    fun getData(bikeList: MutableList<MyBike>, dustList: MutableList<MyDust>) {
        mBikeList = bikeList
        mDust = dustList
    }

    fun showRentalOffice() {
        dbHandler = MyDB(context!!)
        bookmarkMap = mutableMapOf()
        var Db = MyDB
        bookmarkArray = dbHandler!!.getAllUser()
        for (bookmark in bookmarkArray) {
            bookmarkMap[bookmark.rentalOffice] = bookmark
        }

        for (bike in mBikeList) {
            if (bookmarkMap.containsKey(bike.stationName)) {
                bookmarkMap[bike.stationName]!!.leftBike = bike.parkingBikeTotCnt
            }
        }

        for (bookmark in bookmarkArray) {
            bookmark.leftBike = bookmarkMap[bookmark.rentalOffice]!!.leftBike
        }
    }

    fun initLayout() {
        val adapter = BookmarkAdapter(bookmarkArray)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bookmark.layoutManager = layoutManager
        bookmark.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(context!!, layoutManager.orientation)
        bookmark.addItemDecoration(dividerItemDecoration)

        adapter.itemClickListener = object : BookmarkAdapter.OnItemClickListener {
            override fun OnItemClick(
                holder: BookmarkAdapter.ViewHolder,
                view: View,
                data: Bookmark,
                position: Int
            ) {
                Toast.makeText(context, data.rentalOffice, Toast.LENGTH_SHORT).show()
                if (activity is BookmarkFragment.BookmarkToMapListener) {//구현하고있는 activity인 경우에만 이 기능이 수행된다.
                    val bookmarkListener = activity as BookmarkFragment.BookmarkToMapListener
                    bookmarkListener.changeBookmarkToMap(data.rentalOffice)
                }

            }

        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showRentalOffice()
        initLayout()
    }
}


