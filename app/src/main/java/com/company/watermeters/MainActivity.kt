package com.company.watermeters

import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.room.Room
import com.company.watermeters.SecondFragment.Companion.action
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.db.DBContract.DATABASE_NAME
import com.company.watermeters.model.WaterMeter
import kotlinx.android.synthetic.main.activity_main.*

//FIXME Автоматический вход с неправильными данными (проверить для auth.currentUser)
//FIXME Не показывать экран регистрации, если аутентификация успешна
// - Авторизироваться из MainActivity и при ошибке стартовать AuthActivity
//FIXME В базе данных перепутан email и имя
//FIXME неправильное отображение SecondFragment в альбомной ориентации
// - Сделать отдельный UI для альбомной ориентации
// - Добавить ScrollView
// - Заменить ConstraintLayout на RelativeLayout
//TODO Шифрование данных авторизации
//TODO Изменить поля в БД
// - Изменить поля в fragment_second
// - Изменить поля в list_item
// - Мигрировать на Room для упрощения жизни
// - - БД для Счётчиков
// - - Firebase БД для Клиентов

//TODO База данных Firebase
// - Сделать БД по видео
// - - Подгружать из локальной БД полседнее состояние, если нет интернета

//TODO Добавить роль Гость
//OPTIMIZE FirstFragment становиться невидимым
// - лучше его заменять вторым через FragmentManager
//OPTIMIZE BackUp_Descriptor in Manifest
// - узнать что это, удалить или сделать
//OPTIMIZE Передача данных между Activity и Fragments
//OPTIMIZE Хранить данные входа в Shared Preferences
class MainActivity : AppCompatActivity() {

    private var showMenuItems = false
    private var database: WaterMeterDatabase? = null

    companion object {
        var selectedItem = -1
        var listView: ListView? = null
        var toolBar: Toolbar? = null
        var waterMeters = ArrayList<WaterMeter>()
        var listAdapter: WaterMeterListAdapter? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        database = Room.databaseBuilder(applicationContext, WaterMeterDatabase::class.java,
                DATABASE_NAME).build()
        listView = findViewById(R.id.list_view)
        toolBar = findViewById(R.id.toolbar)
        populateListView()
        listView?.onItemClickListener = AdapterView.OnItemClickListener { _, _,
                                                                          position, _ ->
            showUpdateTaskUI(position)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (showMenuItems) {
            menu.findItem(R.id.edit_item).isVisible = true
            menu.findItem(R.id.delete_item).isVisible = true
        }
        return true
    }

    private fun populateListView() {
        waterMeters =  RetrieveTasksAsyncTask(database).execute().get() as ArrayList<WaterMeter>
        listAdapter = WaterMeterListAdapter(this, waterMeters)
        listView?.adapter = listAdapter
    }

    private fun showUpdateTaskUI(selected: Int) {
        selectedItem = selected
        showMenuItems = true
        invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (-1 != selectedItem) {
            when {
                R.id.edit_item == item.itemId -> {
                    action = "editItem"
                    listView?.visibility = View.INVISIBLE
                    toolBar?.visibility = View.INVISIBLE
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
                R.id.delete_item == item.itemId -> {
                    val selectedWaterMeter = waterMeters[selectedItem]
                    DeleteTaskAsyncTask(database, selectedWaterMeter).execute()
                    waterMeters.removeAt(selectedItem)
                    listAdapter?.notifyDataSetChanged()
                    selectedItem = -1
                }
            }
        }
        if (R.id.refresh_item == item.itemId) {
            populateListView()
            listAdapter?.notifyDataSetChanged()
            selectedItem = -1
        }
        return super.onOptionsItemSelected(item)
    }

    private class RetrieveTasksAsyncTask(private val database: WaterMeterDatabase?) :
        AsyncTask<Void, Void, List<WaterMeter>>() {
        override fun doInBackground(vararg params: Void): List<WaterMeter>? {
            return database?.dao()?.retrieveWaterMeterList()
        }
    }

    class AddTaskAsyncTask(private val database: WaterMeterDatabase?, private val newWaterMeter:
    WaterMeter) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void): Long? {
            return database?.dao()?.addWaterMeter(newWaterMeter)
        }
    }

    class UpdateTaskAsyncTask(private val database: WaterMeterDatabase?, private val
    selected: WaterMeter) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void): Unit? {
            return database?.dao()?.updateWaterMeter(selected)
        }
    }

    private class DeleteTaskAsyncTask(private val database: WaterMeterDatabase?, private val
    selected: WaterMeter) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void): Unit? {
            return database?.dao()?.deleteWaterMeter(selected)
        }
    }
}