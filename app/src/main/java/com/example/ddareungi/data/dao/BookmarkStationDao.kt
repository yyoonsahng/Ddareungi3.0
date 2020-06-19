package com.example.ddareungi.data.dao

import androidx.room.*
import com.example.ddareungi.data.BookmarkStation

@Dao
interface BookmarkStationDao {

    @Query("SELECT * FROM BookmarkStations")
    fun getAllStations(): List<BookmarkStation>

    @Query("SELECT * FROM BookmarkStations WHERE stationId = :stationId")
    fun getStationById(stationId: String): BookmarkStation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: BookmarkStation)

    @Query("DELETE FROM BookmarkStations WHERE stationId = :stationId")
    suspend fun deleteStation(stationId: String)

}