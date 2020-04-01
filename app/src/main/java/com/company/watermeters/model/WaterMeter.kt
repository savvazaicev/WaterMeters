package com.company.watermeters.model

import android.provider.BaseColumns
import androidx.room.*
import com.company.watermeters.db.DBContract
import com.company.watermeters.db.DateConverter
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(tableName = DBContract.WaterMeterItem.TABLE_NAME)
class WaterMeter() {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    var id: Long? = null

    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_REGISTRY_NUMBER)
    var registryNumber: String? = null

    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_NAME)
    var name: String? = null

    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_TYPE)
    var type: String? = null

    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_PRODUCER)
    var producer: String? = null

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_DATE)
    var date: String? = null

    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_END_METHODOLOGY)
    var methodology: String? = null

    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_END_COLD_WATER)
    var coldWater: String? = null

    @ColumnInfo(name = DBContract.WaterMeterItem.COLUMN_NAME_END_HOT_WATER)
    var hotWater: String? = null

    constructor(
        coldWater: String?,
        date: String?,
        hotWater: String?,
        methodology: String?,
        name: String?,
        producer: String?,
        registryNumber: String?,
        type: String?
    ) : this() {
        this.registryNumber = registryNumber
        this.name = name
        this.type = type
        this.producer = producer
        this.date = date
        this.methodology = methodology
        this.coldWater = coldWater
        this.hotWater = hotWater
    }
}