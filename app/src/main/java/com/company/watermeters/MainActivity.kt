package com.company.watermeters

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.company.watermeters.db.DBContract.DATABASE_NAME
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.model.WaterMeter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

//FIXME Не показывать экран регистрации, если аутентификация успешна
// - Заменить все активности на фрагменты

//FIXME Изменить цвет DateTimePicker'a
//FIXME Текст "Добавить фото" находится не по центру
//FIXME На текст "Добавить фото" тяжело нажать, как и на иконку, увеличить область нажатия
//FIXME Отдельный класс с онитемкликером заменить на обычный вызов, нет анимации клика
//FIXME ВЫлетает из-за UpdateAll
//FIXME Данные не сохраняются локально, все время загружаются из интернета
//TODO фрагмент загрузки счётчиков
//OPTIMIZE BackUp_Descriptor in Manifest
// - узнать что это, удалить или сделать
//OPTIMIZE Перенести все запросы к бд и Firebase бд и Авторизацию из главного (UI) потока в побочный
//OPTIMIZE Почитать по сохраненной в вк ссылке про SOLID и остальное, затем внедрить
//OPTIMIZE Попросить кого-нибудь сделать CodeReview
//OPTIMIZE Использовать ViewBinding
//OPTIMIZE Попробовать фабричный метод
//OPTIMIZE Заменить где нужно match_parent на 0dp, почитать про это
class MainActivity : AppCompatActivity() {

    private var database: WaterMeterDatabase? = null
    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private lateinit var root: CoordinatorLayout

    companion object {
        //OPTIMIZE Передать в интенте
        var selectedItemRegistryNumber: String? = null
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
        toolBar = findViewById(R.id.toolbar)
        populateRecyclerView()
//        recyclerView?.onItemClickListener = AdapterView.OnItemClickListener { _, _,
//                                                                              position, _ ->
//            selectedItemRegistryNumber = listAdapter?.getList()?.get(position)?.registryNumber
//            startActivityForResult(Intent(this, ClientFormActivity::class.java), 111)
//        }
        button_first.setOnClickListener {
            startActivityForResult(Intent(this, ClientFormActivity::class.java), 111)
        }
        root = findViewById(R.id.root_element)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu.findItem(R.id.search_item)
        val searchView = searchItem.actionView as SearchView
//        val refreshItem = menu.findItem(R.id.refresh_item)
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
//        searchView.setOnSearchClickListener {
//            refreshItem.isVisible = false
//        }
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
            listAdapter?.setData()
            listAdapter?.notifyDataSetChanged()
            selectedItemRegistryNumber = null
//            refreshItem.isVisible = true
            false
        }
        return true
    }

    private fun populateRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        listAdapter = WaterMeterListAdapter(waterMeters)
        recyclerView.adapter = listAdapter
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.setOnItemClickListener { position ->
            selectedItemRegistryNumber = listAdapter?.getList()?.get(position)?.registryNumber
            startActivityForResult(Intent(this, ClientFormActivity::class.java), 111)
        }
        Log.d("wM before retriv size: ", waterMeters.size.toString())
        waterMeters = RetrieveItemsAsyncTask(database).execute().get() as ArrayList<WaterMeter>
        Log.d("wM after retriv size: ", waterMeters.size.toString())
        listAdapter?.setData()
        listAdapter?.notifyDataSetChanged()
        if (waterMeters.isEmpty()) updateListView()
    }

    private fun updateListView() {
        myRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                waterMeters.clear()
                Log.d("wM after clear size: ", waterMeters.size.toString())
                val t = object : GenericTypeIndicator<WaterMeter>() {}
                dataSnapshot.children.forEach {
                    it.getValue(t)?.let { it1 -> waterMeters.add(it1) }
                }
                Log.d("wM after upd size: ", waterMeters.size.toString())
                listAdapter?.setData()
                listAdapter?.notifyDataSetChanged()
//                Log.d("wM before update size: ", waterMeters.size.toString())
                UpdateAllAsyncTask(database, waterMeters).execute()
//                Log.d("wM after update size: ", waterMeters.size.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
//            R.id.refresh_item == item.itemId -> {
//                updateListView()
//                selectedItemRegistryNumber = null
//            }
            R.id.exit_item == item.itemId -> {
                val intent = Intent(this, AuthActivity::class.java)
                intent.putExtra("actionExit", true)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        val customerIsAdded: Boolean = data.getBooleanExtra("customerIsAdded", false)
        if (customerIsAdded) {
            Snackbar.make(root, "Клиент успешно добавлен", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(root, "Ошибка! Клиент не добавлен", Snackbar.LENGTH_SHORT).show()
        }
    }

    private inline fun RecyclerView.setOnItemClickListener(crossinline listener: (position: Int) -> Unit) {
        addOnItemTouchListener(
            RecyclerItemClickListener(this,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        listener(position)
                    }
                })
        )
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