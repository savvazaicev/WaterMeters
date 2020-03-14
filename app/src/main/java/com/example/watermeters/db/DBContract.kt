package com.example.watermeters.db

import android.provider.BaseColumns

object DBContract {
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "database"
    class WaterMeterItem: BaseColumns {
        companion object {
            const val TABLE_NAME = "WaterMeters"
            const val COLUMN_NAME_MARK = "mark"
            const val COLUMN_NAME_MODEL = "model"
            const val COLUMN_NAME_NUMBER = "number"
            const val COLUMN_NAME_ADDRESS = "address"
        }
    }

}