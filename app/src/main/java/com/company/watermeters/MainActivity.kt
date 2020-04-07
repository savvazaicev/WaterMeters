package com.company.watermeters

import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.room.Room
import com.company.watermeters.SecondFragment.Companion.action
import com.company.watermeters.db.DBContract.DATABASE_NAME
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.model.WaterMeter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

//TODO Дизайн
//TODO Поиск водосчётчиков
//TODO Удалить кнопку регистрации
//TODO Подгружать из локальной БД полседнее состояние, если нет интернета или изменений в бд
//FIXME Не показывать экран регистрации, если аутентификация успешна
// - Авторизироваться из MainActivity и при ошибке стартовать AuthActivity

//FIXME неправильное отображение SecondFragment в альбомной ориентации (и в обычной криво)
// - Сделать отдельный UI для альбомной ориентации
// - Добавить ScrollView
// - Заменить ConstraintLayout на RelativeLayout
//FIXME Неправильно еотображение горячей воды
//TODO Шифрование данных авторизации
//TODO Оповещение об обновлении приложения/автоматическео обновление приложения
//TODO Добавить роль Гость
//TODO Material Design
//TODO Кнопка выхода
//TODO Поменять цвет ActionButton с зелёного на белый
//OPTIMIZE FirstFragment становиться невидимым
// - лучше его заменять вторым через FragmentManager
//OPTIMIZE BackUp_Descriptor in Manifest
// - узнать что это, удалить или сделать
//OPTIMIZE Передача данных между Activity и Fragments
//OPTIMIZE Хранить данные входа в Shared Preferences
//OPTIMIZE Перенести запрос к бд из главного (UI) потока в побочный
//OPTIMIZE Почитать по сохраненной в вк ссылке про SOLID и остальное, затем внедрить
//OPTIMIZE Попросить кого-нибудь сделать CodeReview
class MainActivity : AppCompatActivity() {

    private var showMenuItems = false
    private var database: WaterMeterDatabase? = null
    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null

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
        database = Room.databaseBuilder(
            applicationContext, WaterMeterDatabase::class.java,
            DATABASE_NAME
        ).build()
        db = FirebaseDatabase.getInstance("https://watermeters.firebaseio.com/")
        myRef = db?.getReference("WaterMeters")
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
        val searchItem = menu.findItem(R.id.search_item)
        val searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                listAdapter?.getFilter()?.filter(newText)
                return false
            }
        })
        searchView.setOnCloseListener {
            searchView.onActionViewCollapsed()
            populateListView()
            selectedItem = -1
            false
        }
        searchView.isIconifiedByDefault = true
//        if (showMenuItems) {
//            menu.findItem(R.id.edit_item).isVisible = true
//            menu.findItem(R.id.delete_item).isVisible = true
//        }
        return true
    }

    private fun populateListView() {
        myRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t = object : GenericTypeIndicator<WaterMeter>() {}
                dataSnapshot.children.forEach {
                    it.getValue(t)?.let { it1 -> waterMeters.add(it1) }
                }
                listAdapter?.notifyDataSetChanged()
            }
            override fun onCancelled(p0: DatabaseError) {
                // Failed to read value
            }
        })
        listAdapter = WaterMeterListAdapter(this, waterMeters)
        listView?.adapter = listAdapter
//        waterMeters =  RetrieveTasksAsyncTask(database).execute().get() as ArrayList<WaterMeter>
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

    class AddTaskAsyncTask(
        private val database: WaterMeterDatabase?, private val newWaterMeter:
        WaterMeter
    ) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void): Long? {
            return database?.dao()?.addWaterMeter(newWaterMeter)
        }
    }

    class UpdateTaskAsyncTask(
        private val database: WaterMeterDatabase?, private val
        selected: WaterMeter
    ) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void): Unit? {
            return database?.dao()?.updateWaterMeter(selected)
        }
    }

    private class DeleteTaskAsyncTask(
        private val database: WaterMeterDatabase?, private val
        selected: WaterMeter
    ) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void): Unit? {
            return database?.dao()?.deleteWaterMeter(selected)
        }
    }
}