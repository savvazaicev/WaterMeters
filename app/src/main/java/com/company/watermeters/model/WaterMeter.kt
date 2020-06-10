package com.company.watermeters.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @ColumnInfo(name = "date")
    var date: String?,

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