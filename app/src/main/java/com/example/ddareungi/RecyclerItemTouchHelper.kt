package com.example.ddareungi

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.example.ddareungi.dataClass.BookmarkAdapter

class RecyclerItemTouchHelper: ItemTouchHelper.SimpleCallback {
    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int)
    }

    val listener: RecyclerItemTouchHelperListener

    constructor(dragDirs: Int, swipeDirs: Int, listener: RecyclerItemTouchHelperListener) : super(dragDirs, swipeDirs) {
        this.listener = listener
    }

    override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return true
    }
    override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        listener.onSwiped(p0, p1, p0.adapterPosition)

    }
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if(viewHolder != null){
            val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
            getDefaultUIUtil().onSelected(foregroundView)
        }

    }


    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
        getDefaultUIUtil().clearView(foregroundView)
    }
    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive)
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
        val foregroundView = (viewHolder as BookmarkAdapter.ViewHolder).viewForeground
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive)
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }






}