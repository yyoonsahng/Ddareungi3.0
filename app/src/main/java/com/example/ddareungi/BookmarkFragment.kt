package com.example.ddareungi


import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.a190306app.MyBike
import com.example.a190306app.MyDust
import com.example.ddareungi.dataClass.Bookmark
import com.example.ddareungi.dataClass.BookmarkAdapter
import kotlinx.android.synthetic.main.fragment_bookmark.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class BookmarkFragment : Fragment() {
    var dbHandler: MyDB? = null
    var mBikeList: MutableList<MyBike> = mutableListOf()
    var mDust: MutableList<MyDust> = mutableListOf()
    lateinit var bookmarkArray: ArrayList<Bookmark>
    lateinit var bookmarkMap: MutableMap<String, Bookmark>
    lateinit var adapter:BookmarkAdapter
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
    fun initSwipe() {//swipe 기능을 넣는다
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(UP or DOWN, RIGHT) {

            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                //View의 위치를 바꾼다. 두개의 배열의 내용이 서로 바뀌어야함
                //adapter.moveItem(p1.adapterPosition, p2.adapterPosition)//바꿀 position정보 viewholder가 가지고 있음
                return true
            }

            override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                adapter.removeItem(p0.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }//여기까지가 객체 생성

        //helper 객체를 하나 만든다
        var itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)

        itemTouchHelper.attachToRecyclerView(bookmark)
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
        adapter = BookmarkAdapter(bookmarkArray)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bookmark.layoutManager = layoutManager
        bookmark.adapter = adapter
        var mapfragment: MapFragment = MapFragment()
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


                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showRentalOffice()
        initLayout()
    }

    override fun onStop() {
        super.onStop()
        Log.i("stop","Stoped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("destroy","destroyed")
    }
}


