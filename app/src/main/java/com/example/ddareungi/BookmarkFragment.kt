package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.example.ddareungi.dataClass.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_bookmark.*

class BookmarkFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    var dbHandler: MyDB? = null
    var mBikeList: MutableList<MyBike> = mutableListOf()
    var mDust: MutableList<MyDust> = mutableListOf()
    lateinit var bookmarkArray: ArrayList<Bookmark>
    lateinit var bookmarkMap: MutableMap<String, Bookmark>
    lateinit var bookmarkAdapter: BookmarkAdapter

    interface BookmarkToMapListener {
        fun changeBookmarkToMap(rentalOffice: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    fun setData(bikeList: MutableList<MyBike>, dustList: MutableList<MyDust>) {
        mBikeList = bikeList
        mDust = dustList
    }


    fun rentalOfficeData() {
        dbHandler = MyDB(context!!)
        bookmarkMap = mutableMapOf()
        var Db = MyDB
        bookmarkArray = dbHandler!!.getAllUser()
        upDate(false)
    }

    fun upDate(onUpdate: Boolean) {
        for (bookmark in bookmarkArray) {
            for (bike in mBikeList) {
                if (bookmark.rentalOffice == bike.stationName) {
                    bookmark.leftBike = bike.parkingBikeTotCnt
                    break
                }
            }
        }
        if (onUpdate)
            bookmarkAdapter.notifyDataSetChanged()
    }

    fun initLayout() {
        val progressBar = activity!!.findViewById<ProgressBar>(R.id.progress_circular)
        if (progressBar != null)
            progressBar.visibility = View.GONE

        bookmarkAdapter = BookmarkAdapter(bookmarkArray)
        val layoutManager_bookmark = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bookmark.layoutManager = layoutManager_bookmark
        bookmark.adapter = bookmarkAdapter
        val dividerItemDecoration1 = DividerItemDecoration(context!!, layoutManager_bookmark.orientation)
        bookmark.addItemDecoration(dividerItemDecoration1)

        if(bookmark!!.adapter!!.itemCount == 0) {
            bookmark.visibility = View.GONE
            no_bookmark_image.visibility = View.VISIBLE
            no_bookmark_text.visibility = View.VISIBLE
            activity!!.bookmark_refresh_fab.hide()
        } else {
            bookmark.visibility = View.VISIBLE
            no_bookmark_image.visibility = View.GONE
            no_bookmark_text.visibility = View.GONE
            activity!!.bookmark_refresh_fab.show()
        }

        bookmarkAdapter.itemClickListener = object : BookmarkAdapter.OnItemClickListener {
            override fun OnItemClick(
                holder: BookmarkAdapter.ViewHolder,
                view: View,
                data: Bookmark,
                position: Int
            ) {
                if (activity is BookmarkFragment.BookmarkToMapListener) {//구현하고있는 activity인 경우에만 이 기능이 수행된다.
                    val bookmarkListener = activity as BookmarkFragment.BookmarkToMapListener
                    bookmarkListener.changeBookmarkToMap(data.rentalOffice)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLayout()
        bookmark_refresh_fab.setOnClickListener {
            val url = "http://openapi.seoul.go.kr:8088/746c776f61627a7437376b49567a68/json/bikeList/"
            val networkTask = MainActivity.NetworkTask(0, url, null, activity as MainActivity, false)
            networkTask.execute()
        }
        val itemTouchHelperCallback_1: ItemTouchHelper.SimpleCallback =
            RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        object : ItemTouchHelper(itemTouchHelperCallback_1) {}.attachToRecyclerView(bookmark)

    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        dbHandler = MyDB(context!!)
        val rental: Rental = Rental("", "", 1)
        val delete_name = dbHandler!!.findOfficeWithRow(viewHolder.adapterPosition)
        rental.delete = delete_name
        dbHandler!!.deleteUser(rental)
        bookmarkAdapter.removeItem(viewHolder.adapterPosition)

        if(bookmark!!.adapter!!.itemCount == 0) {
            bookmark.visibility = View.GONE
            no_bookmark_image.visibility = View.VISIBLE
            no_bookmark_text.visibility = View.VISIBLE
            activity!!.bookmark_refresh_fab.hide()
        } else {
            bookmark.visibility = View.VISIBLE
            no_bookmark_image.visibility = View.GONE
            no_bookmark_text.visibility = View.GONE
            activity!!.bookmark_refresh_fab.show()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i("stop", "Stoped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("destroy", "destroyed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.appbar_title.text = "즐겨찾기"
        rentalOfficeData()
    }
}
