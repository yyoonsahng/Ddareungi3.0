package com.example.ddareungi.timer

import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("app:setText")
fun setText(button: Button, isRunning: Boolean) {
    if(isRunning) {
        button.text = "반납 완료"
    }
    else {
        button.text = "대여 시작"
    }
}

@BindingAdapter("isRunning", "countHour")
fun setDefaultTimerText(textView: TextView, isRunning: Boolean, countHour: Int) {
    if(!isRunning) {
        if(countHour == 1) {
            textView.text = "0:55:00"
        }
        else if(countHour == 2) {
            textView.text = "1:55:00"
        }
    }
}