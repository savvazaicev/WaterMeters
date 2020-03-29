package com.company.watermeters.db

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
    @TypeConverter
    fun timesstampToDate(time: Long?): Date? = time?.let { Date(it) }
}