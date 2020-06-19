package com.example.ddareungi.bookmark

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ddareungi.R
import com.example.ddareungi.data.BikeStation


@BindingAdapter("app:items")
fun setItems(listView: RecyclerView, items: List<BikeStation>) {
    ((listView.adapter) as BookmarksAdapter).submitList(items)
}

@BindingAdapter("app:setBookmarkImgRes")
fun setBookmarkImgRes(button: ImageButton, bookmarked: Boolean) {
    if(bookmarked) {
        button.setImageResource(R.drawable.ic_star_filled)
    } else {
        button.setImageResource(R.drawable.ic_star_border)
    }
}

@BindingAdapter("wDataLoading", "bDataLoading")
fun setVisibility(layout: FrameLayout, wDataLoading: Boolean, bDataLoading: Boolean) {
    if(!wDataLoading && !bDataLoading) {
        layout.visibility = View.VISIBLE
    } else {
        layout.visibility = View.GONE
    }
}

@BindingAdapter("wDataLoading", "bDataLoading")
fun setPbVisibility(progressBar: ProgressBar, wDataLoading: Boolean, bDataLoading: Boolean) {
    if(wDataLoading || bDataLoading) {
        progressBar.visibility = View.VISIBLE
    } else {
        progressBar.visibility = View.GONE
    }
}

@BindingAdapter("app:setWeatherImgRes")
fun setWeatherImgRes(imageView: ImageView, wfKor: String) {
    when(wfKor) {
        "맑음" -> {
            imageView.setImageResource(R.drawable.ic_sunnny)
        }
        "구름 조금" -> {
            imageView.setImageResource(R.drawable.ic_partialy_cloudy)
        }
        "구름 많음" -> {
            imageView.setImageResource(R.drawable.ic_cloudy)
        }
        "흐림" -> {
            imageView.setImageResource(R.drawable.ic_overcast)
        }
        "비" -> {
            imageView.setImageResource(R.drawable.ic_rainy)
        }
        "눈/비" -> {
            imageView.setImageResource(R.drawable.ic_sleet)
        }
        "눈" -> {
            imageView.setImageResource(R.drawable.ic_snow)
        }
        else -> {
            imageView.setImageResource(R.drawable.ic_sunnny)
        }
    }
}