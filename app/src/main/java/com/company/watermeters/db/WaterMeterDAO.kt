package com.company.watermeters.db

import androidx.room.*
import com.company.watermeters.model.WaterMeter

@Dao
interface WaterMeterDAO {
    @Query("SELECT * from " + "waterMeters")
    suspend fun getAll(): List<WaterMeter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(items: MutableList<WaterMeter>): List<Long>

    @Query("DELETE from waterMeters WHERE id NOT IN (:itemsId)")
    suspend fun deleteAll(itemsId: List<Int?>)

    @Transaction
    suspend fun updateAll(items: MutableList<WaterMeter>) {
        addAll(items)
        deleteAll(items.map { it.id })
    }
}
