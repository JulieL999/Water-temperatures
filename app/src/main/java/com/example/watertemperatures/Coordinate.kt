package com.example.watertemperatures

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Entity class for location coordinates for getting/setting values
@Entity
data class Coordinate(
    @PrimaryKey(autoGenerate = true) var cid: Int,
    @ColumnInfo(name = "CoordinateName") var name: String,
    @ColumnInfo(name = "Latitude") var latitude: String,
    @ColumnInfo(name = "Longitude") var longitude: String,
    @ColumnInfo(name = "WaterTemperature") var waterTemp: Double
)
