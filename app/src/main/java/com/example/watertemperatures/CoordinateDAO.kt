package com.example.watertemperatures

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CoordinateDAO {
    @Query("SELECT*FROM coordinate")
    suspend fun getAll(): List<Coordinate>

    @Query("SELECT * FROM coordinate WHERE cid IN (:coordinateIds)")
    suspend fun getByIds(coordinateIds: IntArray): List<Coordinate>

    @Insert
    suspend fun insertAll(vararg coordinate: Coordinate)

    @Delete
    suspend fun delete(coordinate: Coordinate)

    @Query("DELETE FROM coordinate")
    suspend fun deleteAll()
}