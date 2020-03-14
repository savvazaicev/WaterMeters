package com.example.watermeters.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.example.watermeters.db.DBContract.DATABASE_NAME
import com.example.watermeters.db.DBContract.DATABASE_VERSION
import com.example.watermeters.model.WaterMeter

class DBHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME,
    null, DATABASE_VERSION
) {
    private val SQL_CREATE_ENTRIES = "CREATE TABLE " +
            DBContract.WaterMeterItem.TABLE_NAME + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            DBContract.WaterMeterItem.COLUMN_NAME_MARK + " TEXT, " +
            DBContract.WaterMeterItem.COLUMN_NAME_MODEL + " TEXT, " +
            DBContract.WaterMeterItem.COLUMN_NAME_NUMBER + " TEXT, " +
            DBContract.WaterMeterItem.COLUMN_NAME_ADDRESS + " TEXT)"
    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " +
            DBContract.WaterMeterItem.TABLE_NAME

    fun addNewWaterMeter(waterMeter: WaterMeter): WaterMeter {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DBContract.WaterMeterItem.COLUMN_NAME_MARK, waterMeter.mark)
        values.put(DBContract.WaterMeterItem.COLUMN_NAME_MODEL, waterMeter.model)
        values.put(DBContract.WaterMeterItem.COLUMN_NAME_NUMBER, waterMeter.number)
        values.put(DBContract.WaterMeterItem.COLUMN_NAME_ADDRESS, waterMeter.address)
        val taskId = db.insert(DBContract.WaterMeterItem.TABLE_NAME, null, values)
        waterMeter.Id = taskId
        return waterMeter
    }

    fun retrieveWaterMetersList(): ArrayList<WaterMeter> {
        val db = this.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            DBContract.WaterMeterItem.COLUMN_NAME_MARK,
            DBContract.WaterMeterItem.COLUMN_NAME_MODEL,
            DBContract.WaterMeterItem.COLUMN_NAME_NUMBER,
            DBContract.WaterMeterItem.COLUMN_NAME_ADDRESS
        )
        val cursor = db.query(
            DBContract.WaterMeterItem.TABLE_NAME, projection,
            null, null, null, null, null
        )
        val waterMeters = ArrayList<WaterMeter>()
        while (cursor.moveToNext()) {
            val waterMeter = WaterMeter(
                cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.WaterMeterItem.COLUMN_NAME_MARK)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.WaterMeterItem.COLUMN_NAME_MODEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.WaterMeterItem.COLUMN_NAME_NUMBER)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.WaterMeterItem.COLUMN_NAME_ADDRESS))
            )
            waterMeters.add(waterMeter)
        }
        cursor.close()
        return waterMeters
    }

    fun updateWaterMeter(waterMeter: WaterMeter) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DBContract.WaterMeterItem.COLUMN_NAME_MARK, waterMeter.mark)
        values.put(
            DBContract.WaterMeterItem.COLUMN_NAME_MODEL,
            waterMeter.model
        )
        values.put(
            DBContract.WaterMeterItem.COLUMN_NAME_NUMBER,
            waterMeter.number
        )
        values.put(
            DBContract.WaterMeterItem.COLUMN_NAME_ADDRESS,
            waterMeter.address
        )
        val selection = BaseColumns._ID + " = ?"
        val selectionArgs = arrayOf(waterMeter.Id.toString())
        db.update(
            DBContract.WaterMeterItem.TABLE_NAME, values, selection,
            selectionArgs
        )
    }

    fun deleteWaterMeter(waterMeter: WaterMeter) {
        val db = this.writableDatabase
        val selection = BaseColumns._ID + " = ?"
        val selectionArgs = arrayOf(waterMeter.Id.toString())
        db.delete(DBContract.WaterMeterItem.TABLE_NAME, selection, selectionArgs)
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}