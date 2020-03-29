package com.company.watermeters.db

import androidx.room.*
import com.company.watermeters.model.WaterMeter

@Dao
interface WaterMeterDAO {
    @Query("SELECT * FROM " + DBContract.WaterMeterItem.TABLE_NAME)
    fun retrieveWaterMeterList(): List<WaterMeter>
    @Insert
    fun addWaterMeter(task: WaterMeter): Long
    @Update
    fun updateWaterMeter(task: WaterMeter)
    @Delete
    fun deleteWaterMeter(task: WaterMeter)
}
