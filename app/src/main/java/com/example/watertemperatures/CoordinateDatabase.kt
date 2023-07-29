package com.example.watertemperatures

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Coordinate::class], version = 1)
abstract class CoordinateDatabase : RoomDatabase(){
    abstract fun coordinateDAO(): CoordinateDAO
}