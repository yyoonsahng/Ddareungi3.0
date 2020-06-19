package com.example.ddareungi.bookmark

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ddareungi.R
import com.example.ddareungi.viewmodel.BikeStationViewModel


class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback {

    private val adapter: BookmarksAdapter

    private val viewModel: BikeStationViewModel

    lateinit var icon: Drawable

    lateinit var background: ColorDrawable

    constructor(viewModel: BikeStationViewModel, adapter: BookmarksAdapter, dragsDir: Int, swipeDirs: Int)
            : super(dragsDir, swipeDirs) {
        this.adapter = adapter
        this.viewModel = viewModel
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        viewModel.deleteBookmarkStation(position)
        adapter.submitList(viewModel.bikeStations.value)
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

        icon = viewHolder.itemView.context.getDrawable(R.drawable.ic_delete_white_24dp)!!
        background = ColorDrawable(Color.parseColor("#fa315b"))
        val itemView = viewHolder.itemView

        val backgroundCornerOffset = 20
        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        if (dX < 0) {
            val iconLeft = itemView.right - iconMargin - icon.intrinsicHeight
            val iconRight = itemView.right - iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            background.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top, itemView.right, itemView.bottom)
        } else {
            background.setBounds(0, 0, 0, 0)
        }
        background.draw(c)
        icon.draw(c)
    }
}