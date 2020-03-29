package com.company.watermeters.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.company.watermeters.model.WaterMeter

@Database(entities = [WaterMeter::class], version = DBContract.DATABASE_VERSION)
abstract class WaterMeterDatabase : RoomDatabase() {
    abstract fun dao(): WaterMeterDAO
}