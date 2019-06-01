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
import com.example.a190306app.MyBike
import com.example.a190306app.MyDust
import com.example.ddareungi.dataClass.Bookmark
import com.example.ddareungi.dataClass.BookmarkAdapter
import com.example.ddareungi.dataClass.Rental
import kotlinx.android.synthetic.main.fragment_bookmark.*

class BookmarkFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
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

//    fun initSwipe() {//swipe 기능을 넣는다
//        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, LEFT) {
//            val listener: RecyclerItemTouchHelperListener
//
//            fun RecyclerItemTouchHelper(dragDirs: Int, swipeDirs: Int, listener: RecyclerItemTouchHelperListener) {
//                super(dragDirs, swipeDirs)
//                this.listener = listener
//            }
//
//            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
//                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                 return true
//            }
//            override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
//                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                dbHandler = MyDB(context!!)
//                val rental: Rental = Rental("", "", 1)
//                var delete_name = dbHandler!!.findOfficeWithRow(p0.adapterPosition)
//                rental.delete = delete_name
//                dbHandler!!.deleteUser(rental)
//                adapter.removeItem(p0.adapterPosition)
//
//            }
//            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
//                super.onSelectedChanged(viewHolder, actionState)
//                if(viewHolder != null){
//                    val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
//                    getDefaultUIUtil().onSelected(foregroundView)
//                }
//
//            }
//
//
//            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
//                super.clearView(recyclerView, viewHolder)
//                val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
//                getDefaultUIUtil().clearView(foregroundView)
//            }
//            override fun onChildDrawOver(
//                c: Canvas,
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder?,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//                super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
//                getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
//                    actionState, isCurrentlyActive)
//            }
//            override fun onChildDraw(
//                c: Canvas,
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
//                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
//                    actionState, isCurrentlyActive)
//            }
//
//            override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
//                return super.convertToAbsoluteDirection(flags, layoutDirection)
//            }
//
//        }//여기까지가 객체 생성
//        //helper 객체를 하나 만든다
//        var itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
//        itemTouchHelper.attachToRecyclerView(bookmark)
//    }


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
        val dividerItemDecoration = DividerItemDecoration(context!!, layoutManager.orientation)
        bookmark.addItemDecoration(dividerItemDecoration)

        adapter.itemClickListener = object : BookmarkAdapter.OnItemClickListener {
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
        val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,this, dbHandler, adapter, context!!)
        val item = ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(bookmark)
        //initSwipe()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if(viewHolder is BookmarkAdapter.ViewHolder) {
            dbHandler = MyDB(context!!)
            val rental: Rental = Rental("", "", 1)
            var delete_name = dbHandler!!.findOfficeWithRow(viewHolder.adapterPosition)
            rental.delete = delete_name
            dbHandler!!.deleteUser(rental)
            adapter.removeItem(viewHolder.adapterPosition)
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
        showRentalOffice()
    }
}


