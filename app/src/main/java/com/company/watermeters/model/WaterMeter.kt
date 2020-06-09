package com.company.watermeters.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.company.watermeters.db.DateConverter

@Entity(tableName = "waterMeters")
data class WaterMeter(

    @PrimaryKey
    var id: Int?,

    @ColumnInfo(name = "registryNumber")
    val registryNumber: String?,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "type")
    val type: String?,

    @ColumnInfo(name = "producer")
    val producer: String?,

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = "date")
    val date: String?,

    @ColumnInfo(name = "methodology")
    val methodology: String?,

    @ColumnInfo(name = "coldWater")
    val coldWater: String?,

    @ColumnInfo(name = "hotWater")
    val hotWater: String?
) {
    constructor() : this(
        null, "", "", "", "",
        "", "", "", ""
    )
}