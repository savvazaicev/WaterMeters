package com.company.watermeters

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.model.WaterMeter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        //OPTIMIZE Передать в интенте в bundle (размер bundle ограничен)
        var selectedItemRegistryNumber: String? = null
        private const val ADD_CLIENT_REQUEST = 111
    }

    private var waterMeters: MutableList<WaterMeter> = ArrayList()
    private var database: WaterMeterDatabase? = null
    private var listAdapter: WaterMeterListAdapter? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setRoomDatabase()
        setRecyclerView()

        button_first.setOnClickListener {
            val intent = Intent(this, ClientFormActivity::class.java)
            startActivityForResult(intent, ADD_CLIENT_REQUEST)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.search_item)
        val exitItem = menu.findItem(R.id.exit_item)
        val searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnSearchClickListener {
            exitItem.isVisible = false
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
            exitItem.isVisible = true
            false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_CLIENT_REQUEST && resultCode == RESULT_OK && data != null) {
            val root = findViewById<CoordinatorLayout>(R.id.root_element)
            val customerIsAdded: Boolean = data.getBooleanExtra("customerIsAdded", false)
            if (customerIsAdded) {
                Snackbar.make(root, getString(R.string.clientAdded), Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(root, getString(R.string.clientNotAdded), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.exit_item == item.itemId) {
            val intent = Intent(this, AuthActivity::class.java)
            intent.putExtra("actionExit", true)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    private fun setRoomDatabase() {
        database = Room.databaseBuilder(
            applicationContext, WaterMeterDatabase::class.java,
            "waterMeters"
        ).build()
    }

    private fun setRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.list)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        listAdapter = WaterMeterListAdapter(waterMeters)
        recyclerView.adapter = listAdapter
        recyclerView.setHasFixedSize(true)
        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        launch {
            waterMeters = retrieveItems() as ArrayList<WaterMeter>
            Log.d("wM after retriv size", waterMeters.size.toString())
            listAdapter?.updateData(waterMeters)
            updateWaterMeterList()
        }
    }

    private fun updateWaterMeterList() {
        val db = FirebaseDatabase.getInstance("https://watermeters.firebaseio.com/")
        val ref = db.getReference("WaterMeters")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                updateWaterMetersData(dataSnapshot)
                launch { updateDatabaseData() }
            }

            private fun updateWaterMetersData(dataSnapshot: DataSnapshot) {
                waterMeters.clear()
                val t = object : GenericTypeIndicator<WaterMeter>() {}
                dataSnapshot.children.forEachIndexed { index, child ->
                    child.getValue(t)?.let { it ->
                        waterMeters.add(it.apply {
                            id = index
                        })
                    }
                }
                Log.d("wM after firebase size", waterMeters.size.toString())
                listAdapter?.updateData(waterMeters)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private suspend fun retrieveItems() = database?.dao()?.getAll()

    private suspend fun updateDatabaseData() = database?.dao()?.updateAll(waterMeters)
}