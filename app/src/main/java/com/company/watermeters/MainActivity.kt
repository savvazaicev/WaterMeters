package com.company.watermeters

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import com.company.watermeters.db.DBContract.DATABASE_NAME
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.model.WaterMeter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

//TODO Дизайн
//TODO Подгружать из локальной БД полседнее состояние, если нет интернета или изменений в бд
//FIXME Не показывать экран регистрации, если аутентификация успешна
// - Авторизироваться из MainActivity и при ошибке стартовать AuthActivity

//FIXME неправильное отображение SecondFragment в альбомной ориентации (и в обычной криво)
// - Сделать отдельный UI для альбомной ориентации
// - Добавить ScrollView
// - Заменить ConstraintLayout на RelativeLayout
//TODO DatePickerDialog в SecondFragment
//TODO Шифрование данных авторизации
//TODO Оповещение об обновлении приложения/автоматическео обновление приложения
//TODO Material Design
//TODO Кнопка выхода
//OPTIMIZE FirstFragment становиться невидимым
// - лучше его заменять вторым через FragmentManager
//OPTIMIZE BackUp_Descriptor in Manifest
// - узнать что это, удалить или сделать
//OPTIMIZE Передача данных между Activity и Fragments
//OPTIMIZE Перенести запрос к бд из главного (UI) потока в побочный
//OPTIMIZE Почитать по сохраненной в вк ссылке про SOLID и остальное, затем внедрить
//OPTIMIZE Убрать костыли типо тегов и visibility, избыточный код
//OPTIMIZE Заменить listView на RecyclerView
//OPTIMIZE Использовать DiffUtils для списка
//OPTIMIZE Попросить кого-нибудь сделать CodeReview
class MainActivity : AppCompatActivity() {

    //    private var showMenuItems = false
    private var database: WaterMeterDatabase? = null
    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null

    companion object {
        var selectedItemRegistryNumber: String? = null
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
        val refreshItem = menu.findItem(R.id.refresh_item)
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnSearchClickListener {
            refreshItem.isVisible = false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                listAdapter?.filter?.filter(newText)
                return false
            }
        })
        searchView.setOnCloseListener {
            listAdapter?.notifyDataSetChanged()
            selectedItemRegistryNumber = null
            refreshItem.isVisible = true
            false
        }
        return true
    }

    private fun populateListView() {
        listAdapter = WaterMeterListAdapter(this, waterMeters)
        listView?.adapter = listAdapter
        waterMeters = RetrieveItemsAsyncTask(database).execute().get() as ArrayList<WaterMeter>
        listAdapter?.notifyDataSetChanged()
        if (waterMeters.isEmpty()) updateListView()
    }

    private fun updateListView() {
        myRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                waterMeters.clear()
                val t = object : GenericTypeIndicator<WaterMeter>() {}
                dataSnapshot.children.forEach {
                    it.getValue(t)?.let { it1 -> waterMeters.add(it1) }
                }
                listAdapter?.notifyDataSetChanged()
                UpdateAllAsyncTask(database, waterMeters).execute()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun showUpdateTaskUI(selected: Int) {
        selectedItemRegistryNumber = listAdapter?.getList()?.get(selected)?.registryNumber
        invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.refresh_item == item.itemId) {
            updateListView()
            selectedItemRegistryNumber = null
        }
        return super.onOptionsItemSelected(item)
    }

    private class RetrieveItemsAsyncTask(private val database: WaterMeterDatabase?) :
        AsyncTask<Void, Void, List<WaterMeter>>() {
        override fun doInBackground(vararg params: Void): List<WaterMeter>? {
            return database?.dao()?.retrieveItemList()
        }
    }

    class AddTaskAsyncTask(
        private val database: WaterMeterDatabase?, private val newWaterMeter:
        WaterMeter
    ) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void): Long? {
            return database?.dao()?.addItem(newWaterMeter)
        }
    }

    class UpdateTaskAsyncTask(
        private val database: WaterMeterDatabase?, private val
        selected: WaterMeter
    ) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void): Unit? {
            return database?.dao()?.updateItem(selected)
        }
    }

    class UpdateAllAsyncTask(
        private val database: WaterMeterDatabase?, private val
        selected: ArrayList<WaterMeter>
    ) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void): Unit? {
            return database?.dao()?.updateAll(selected)
        }
    }

    private class DeleteTaskAsyncTask(
        private val database: WaterMeterDatabase?, private val
        selected: WaterMeter
    ) : AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void): Unit? {
            return database?.dao()?.deleteItem(selected)
        }
    }
}