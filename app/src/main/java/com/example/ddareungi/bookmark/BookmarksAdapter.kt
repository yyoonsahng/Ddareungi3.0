package com.example.ddareungi.bookmark

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ddareungi.data.BikeStation
import com.example.ddareungi.databinding.BookmarkSingleItemBinding
import com.example.ddareungi.viewmodel.BikeStationViewModel


class BookmarksAdapter(private val viewModel: BikeStationViewModel)
    : ListAdapter<BikeStation, BookmarksAdapter.BookmarkViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        return BookmarkViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(viewModel, item)
    }


    class BookmarkViewHolder(private val binding: BookmarkSingleItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: BikeStationViewModel, item: BikeStation) {
            binding.bikeStationVM = viewModel
            binding.bikeStation = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup?): BookmarkViewHolder {
                val layoutInflater = LayoutInflater.from(parent!!.context)
                val binding = BookmarkSingleItemBinding.inflate(layoutInflater, parent, false)

                return BookmarkViewHolder(binding)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<BikeStation>() {
        override fun areItemsTheSame(oldItem: BikeStation, newItem: BikeStation): Boolean {
            return oldItem.stationId == newItem.stationId
        }

        override fun areContentsTheSame(oldItem: BikeStation, newItem: BikeStation): Boolean {
            return oldItem == newItem
        }
    }
}