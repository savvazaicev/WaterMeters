package com.example.watermeters

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.watermeters.SecondFragment.Companion.action
import com.example.watermeters.db.DBHelper
import com.example.watermeters.model.WaterMeter
import kotlinx.android.synthetic.main.activity_main.*

//FIXME Скрыть меню при переходе на SecondFragment
//TODO Авторизация Firebase
// - Сделать запоминание данных входа, чтобы каждый раз не вводить данные
//TODO База данных Firebase
// - Сделать БД по видео
// - Изменить логику локальной БД
// - - Подгружать из локальной БД полседнее состояние, если нет интернета
// - - Сохранять водосчётчик в локальную БД, если нет интернета/не удается подключиться,
// - - - Подсвечивать красным цветом незагруженный аодосчётчик
class MainActivity : AppCompatActivity() {

    private var showMenuItems = false
    private var dbHelper: DBHelper = DBHelper(this)

    companion object {
        var selectedItem = -1
        var listView: ListView? = null
        var waterMeters = ArrayList<WaterMeter>()
        var listAdapter: WaterMeterListAdapter? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        listView = findViewById(R.id.list_view)
        populateListView()
        listView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view,
                                                                          position, id ->
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
        waterMeters = dbHelper.retrieveWaterMetersList()
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
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
                R.id.delete_item == item.itemId -> {
                    val selectedWaterMeter = waterMeters[selectedItem]
                    dbHelper.deleteWaterMeter(selectedWaterMeter)
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
}