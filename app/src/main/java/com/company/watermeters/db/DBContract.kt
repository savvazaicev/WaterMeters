package com.company.watermeters.db

import android.provider.BaseColumns

object DBContract {
    const val DATABASE_VERSION = 2
    const val DATABASE_NAME = "waterMeters"
    class WaterMeterItem: BaseColumns {
        companion object {
            const val TABLE_NAME = "waterMeters"
            const val COLUMN_NAME_REGISTRY_NUMBER = "registryNumber"
            const val COLUMN_NAME_NAME = "name"
            const val COLUMN_NAME_TYPE = "type"
            const val COLUMN_NAME_PRODUCER = "producer"
            const val COLUMN_NAME_DATE = "date"
            const val COLUMN_NAME_END_METHODOLOGY = "methodology"
            const val COLUMN_NAME_END_COLD_WATER = "coldWater"
            const val COLUMN_NAME_END_HOT_WATER = "hotWater"
        }
    }
}