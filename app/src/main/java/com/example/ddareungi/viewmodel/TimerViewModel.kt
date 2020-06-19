package com.example.ddareungi.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ddareungi.Event


class TimerViewModel: ViewModel() {

    private val mCountHour = MutableLiveData<Int>().apply { value = 1 }
    val countHour: LiveData<Int> = mCountHour

    private val mIsRunning = MutableLiveData<Boolean>().apply { value = false }
    val isRunning: LiveData<Boolean> = mIsRunning

    // [SnackBar] 메세지를 포함한 [Event]
    private val mSnackbarText = MutableLiveData<Event<String>>()
    val snackbarText: LiveData<Event<String>> = mSnackbarText


    fun setCountHour(hour: Int) {
        mCountHour.postValue(hour)
    }

    fun toggleIsRunning() {
        mIsRunning.value = !mIsRunning.value!!
    }

    fun setIsRunning(state: Boolean) {
        mIsRunning.value = state
    }

    fun showSnackBarMessage(message: String) {
        mSnackbarText.value = Event(message)
    }

    companion object {

        class Factory(application: Application)
            : ViewModelProvider.NewInstanceFactory() {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TimerViewModel() as T
            }
        }
    }
}