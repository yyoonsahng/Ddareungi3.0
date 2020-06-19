package com.example.ddareungi

import android.app.Application
import com.example.ddareungi.data.AppDatabase

class BasicApp : Application() {

    private fun getDatabase() : AppDatabase {
        return AppDatabase.getInstance(this)
    }

    fun getDataRepository() : DataRepository {
        return DataRepository.getInstance(getDatabase().bookmarkStationDao())!!
    }
}