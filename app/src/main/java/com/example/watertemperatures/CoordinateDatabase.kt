package com.example.watertemperatures

import androidx.room.Database
import androidx.room.RoomDatabase

//Room Database for location coordinates. Used for getting DAO
@Database(entities = [Coordinate::class], version = 1)
abstract class CoordinateDatabase : RoomDatabase(){
    abstract fun coordinateDAO(): CoordinateDAO
}