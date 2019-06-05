package com.example.ddareungi


import android.os.AsyncTask
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
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

class BookmarkFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, View.OnClickListener {
    var dbHandler: MyDB? = null
    var mBikeList: MutableList<MyBike> = mutableListOf()
    lateinit var mDust: MyDust
    lateinit var bookmarkArray: ArrayList<Bookmark>
    lateinit var bookmarkMap: MutableMap<String, Bookmark>
    lateinit var mWeather: MyWeather
    lateinit var bookmarkAdapter: BookmarkAdapter
    var networkState = false
    var enableGPS = false

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

    fun setData(bikeList: MutableList<MyBike>, mDust: MyDust, mWeather: MyWeather) {
        mBikeList = bikeList
        this.mWeather = mWeather
        this.mDust = mDust
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
        if (onUpdate) {
            bookmarkAdapter.notifyDataSetChanged()
            dust_text.text = "오늘의 미세먼지는\n${mDust.idex_nm}입니다"
            weather_image.setImageResource(mWeather.matchImage())
        }
    }

    fun initLayout() {
        rentalOfficeData()
        val progressBar = activity!!.findViewById<ProgressBar>(R.id.progress_circular)
        if (progressBar != null)
            progressBar.visibility = View.GONE

        dust_text.text = "오늘의 미세먼지는\n${mDust.idex_nm}입니다"
        weather_image.setImageResource(mWeather.matchImage())

        bookmarkAdapter = BookmarkAdapter(bookmarkArray)
        val layoutManager_bookmark = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bookmark.layoutManager = layoutManager_bookmark
        bookmark.adapter = bookmarkAdapter
        val dividerItemDecoration1 = DividerItemDecoration(context!!, layoutManager_bookmark.orientation)
        bookmark.addItemDecoration(dividerItemDecoration1)

        if (bookmark!!.adapter!!.itemCount == 0 && networkState) {
            bookmark.visibility = View.GONE
            network_refresh_button.visibility = View.GONE
            no_bookmark_image.setImageResource(R.drawable.ic_no_bookmark)
            no_bookmark_text.text = "즐겨찾는 정류소를 추가해주세요"
            no_bookmark_image.visibility = View.VISIBLE
            no_bookmark_text.visibility = View.VISIBLE
            activity!!.bookmark_refresh_fab.hide()
        } else if (bookmark!!.adapter!!.itemCount > 0 && networkState) {
            bookmark.visibility = View.VISIBLE
            no_bookmark_image.visibility = View.GONE
            no_bookmark_text.visibility = View.GONE
            network_refresh_button.visibility = View.GONE
            activity!!.bookmark_refresh_fab.show()
        } else {
            activity!!.bookmark_refresh_fab.hide()
            weather_image.visibility = View.GONE
            dust_text.visibility = View.GONE
            bookmark.visibility = View.GONE
            no_bookmark_image.visibility = View.VISIBLE
            no_bookmark_text.visibility = View.VISIBLE
            no_bookmark_text.text = "네트워크 연결을 확인해주세요"
            network_refresh_button.visibility = View.VISIBLE
            no_bookmark_image.setImageResource(R.drawable.ic_wifi)
            network_refresh_button.setOnClickListener(this)
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

    override fun onClick(v: View?) {
        if (v!!.id == R.id.network_refresh_button) { //새로고침버튼
            val progressBar = activity!!.findViewById<ProgressBar>(R.id.progress_circular)
            if (progressBar != null)
                progressBar.visibility = View.VISIBLE
            if (networkState) {
                val mActivity=activity as MainActivity
                mActivity.initPermission()
                mActivity.checkNetwork()
                mActivity.isreLoad=true
            }
            else{
                if (progressBar != null)
                    progressBar.visibility = View.GONE
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLayout()
        bookmark_refresh_fab.setOnClickListener {
            val mActivity=activity as MainActivity
            val progressBar = activity!!.findViewById<ProgressBar>(R.id.progress_circular)
            if (progressBar != null)
                progressBar.visibility = View.VISIBLE
            if (networkState) {
                mActivity.initPermission()
                val url = "http://openapi.seoul.go.kr:8088/746c776f61627a7437376b49567a68/json/bikeList/"
                val networkTask = MainActivity.NetworkTask(0, url,mActivity.dParse , mActivity, false)
                networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
            else{
                initLayout()
                if (progressBar != null)
                    progressBar.visibility = View.GONE
            }
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

        if (bookmark!!.adapter!!.itemCount == 0) {
            bookmark.visibility = View.GONE
            no_bookmark_image.setImageResource(R.drawable.ic_no_bookmark)
            no_bookmark_text.text = "즐겨찾는 정류소를 추가해주세요"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.appbar_title.text = "즐겨찾기"
    }
}
