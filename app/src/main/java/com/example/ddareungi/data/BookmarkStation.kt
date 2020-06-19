package com.example.ddareungi.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "BookmarkStations")
data class BookmarkStation(
    @PrimaryKey var stationId: String,
    var stationName: String
    )