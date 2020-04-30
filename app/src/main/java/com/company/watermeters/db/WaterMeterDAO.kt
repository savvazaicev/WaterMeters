package com.company.watermeters.db

import androidx.room.*
import com.company.watermeters.model.WaterMeter

@Dao
interface WaterMeterDAO {
    @Query("SELECT * FROM " + DBContract.WaterMeterItem.TABLE_NAME)
    fun retrieveItemList(): List<WaterMeter>
    @Insert
    fun addItem(item: WaterMeter): Long
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(items: ArrayList<WaterMeter>)
    @Update
    fun updateItem(item: WaterMeter)
    @Delete
    fun deleteItem(item: WaterMeter)
}
