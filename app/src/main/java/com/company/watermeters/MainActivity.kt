package com.company.watermeters

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
//FIXME Дублироание счётчиков в бд
//FIXME Неправильно отображается дата в списке
//FIXME Выводить сообщение об ошибке, если нет интернета

//TODO DatePickerDialog в SecondFragment
//TODO Кнопка выхода
//TODO фрагмент загрузки счётчиков
//OPTIMIZE Если данные в форме не изменились, не разрешать повторную отправку формы
//OPTIMIZE BackUp_Descriptor in Manifest
// - узнать что это, удалить или сделать
//OPTIMIZE Перенести все запросы к бд и Firebase бд и Авторизацию из главного (UI) потока в побочный
//OPTIMIZE Почитать по сохраненной в вк ссылке про SOLID и остальное, затем внедрить
//OPTIMIZE Заменить listView на RecyclerView
//OPTIMIZE Использовать DiffUtils для списка
//OPTIMIZE Попросить кого-нибудь сделать CodeReview
class MainActivity : AppCompatActivity() {

    private var database: WaterMeterDatabase? = null
    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private lateinit var root: CoordinatorLayout

    companion object {
        //OPTIMIZE Передать в интенте
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
            selectedItemRegistryNumber = listAdapter?.getList()?.get(position)?.registryNumber
            startActivityForResult(Intent(this, ClientFormActivity::class.java), 111)
        }
        button_first.setOnClickListener {
            startActivityForResult(Intent(this, ClientFormActivity::class.java), 111)
        }
        root = findViewById(R.id.root_element)
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
        myRef?.addValueEventListener(object : ValueEventListener {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            R.id.refresh_item == item.itemId -> {
                updateListView()
                selectedItemRegistryNumber = null
            }
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