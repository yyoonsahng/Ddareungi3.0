package com.example.ddareungi.data

import android.content.Context
import androidx.room.*
import com.example.ddareungi.data.dao.BookmarkStationDao

@Database(entities = [BookmarkStation::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun bookmarkStationDao() : BookmarkStationDao

    companion object {
        private var sInstance: AppDatabase? = null

        private const val DATABASE_NAME = "bookmark-db"

        fun getInstance(context: Context): AppDatabase {
            return sInstance ?: synchronized(this) {
                sInstance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                    .build()
            }
        }
    }


}