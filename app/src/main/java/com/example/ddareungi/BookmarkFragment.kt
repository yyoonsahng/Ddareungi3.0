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
import kotlinx.android.synthetic.main.fragment_bookmark.*

class BookmarkFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener,
    RecyclerItemTouchHelper2.RecyclerItemTouchHelperListener {
    var dbHandler: MyDB? = null
    var mBikeList: MutableList<MyBike> = mutableListOf()
    var mDust: MutableList<MyDust> = mutableListOf()
    lateinit var bookmarkArray: ArrayList<Bookmark>
    lateinit var bookmarkMap: MutableMap<String, Bookmark>
    lateinit var bookmarkAdapter:BookmarkAdapter
    lateinit var historyAdapter:HistoryAdapter
    lateinit var historyArray:ArrayList<History>
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

    fun getData(bikeList: MutableList<MyBike>, dustList: MutableList<MyDust>) {
        mBikeList = bikeList
        mDust = dustList
    }


    fun rentalOfficeData() {
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
        historyArray = dbHandler!!.getAllHistory()
    }

    fun initLayout() {
        val progressBar = activity!!.findViewById<ProgressBar>(R.id.progress_circular)
        if(progressBar != null)
            progressBar.visibility = View.GONE

        bookmarkAdapter = BookmarkAdapter(bookmarkArray)
        historyAdapter = HistoryAdapter(historyArray)
        val layoutManager_bookmark = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val layoutManager_history = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bookmark.layoutManager = layoutManager_bookmark
        recent_path.layoutManager = layoutManager_history
        bookmark.adapter = bookmarkAdapter
        recent_path.adapter = historyAdapter
        val dividerItemDecoration1 = DividerItemDecoration(context!!, layoutManager_bookmark.orientation)
        val dividerItemDecoration2 = DividerItemDecoration(context!!, layoutManager_history.orientation)
        bookmark.addItemDecoration(dividerItemDecoration1)
        recent_path.addItemDecoration(dividerItemDecoration2)

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

        historyAdapter.itemClickListener = object : HistoryAdapter.OnItemClickListener {
            override fun OnItemClick(
                holder: HistoryAdapter.ViewHolder,
                view: View,
                data: History,
                position: Int
            ) {
                /*if (activity is BookmarkFragment.BookmarkToMapListener) {//구현하고있는 activity인 경우에만 이 기능이 수행된다.
                    val bookmarkListener = activity as BookmarkFragment.BookmarkToMapListener
                    bookmarkListener.changeBookmarkToMap(data.rentalOffice)
                }*/

            }

        }


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLayout()
        val itemTouchHelperCallback_1: ItemTouchHelper.SimpleCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        val itemTouchHelperCallback_2: ItemTouchHelper.SimpleCallback = RecyclerItemTouchHelper2(0, ItemTouchHelper.LEFT, this)
        //val item = ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(bookmark)
        object: ItemTouchHelper(itemTouchHelperCallback_1){}.attachToRecyclerView(bookmark)
        object: ItemTouchHelper(itemTouchHelperCallback_2){}.attachToRecyclerView(recent_path)

    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        dbHandler = MyDB(context!!)
        if(viewHolder is BookmarkAdapter.ViewHolder) {
            val rental: Rental = Rental("", "", 1)
            val delete_name = dbHandler!!.findOfficeWithRow(viewHolder.adapterPosition)
            rental.delete = delete_name
            dbHandler!!.deleteUser(rental)
            bookmarkAdapter.removeItem(viewHolder.adapterPosition)
        }
        else{
            val history = History("")
            val delete_name = dbHandler!!.findHistoryWithRow(viewHolder.adapterPosition)
            history.recent = delete_name
            dbHandler!!.deleteHistory(history)
            historyAdapter.removeItem(viewHolder.adapterPosition)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i("stop","Stoped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("destroy","destroyed")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rentalOfficeData()
    }
}
