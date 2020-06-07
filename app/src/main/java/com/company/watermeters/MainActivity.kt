package com.company.watermeters

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
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
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        //OPTIMIZE Передать в интенте в bundle (размер bundle ограничен)
        var selectedItemRegistryNumber: String? = null
        var waterMeters: MutableCollection<WaterMeter> = ArrayList()
        private const val ADD_CLIENT_REQUEST = 111
        private const val STORAGE_PERMISSION_REQUEST = 211
    }

    private var database: WaterMeterDatabase? = null
    private var db: FirebaseDatabase? = null
    private var ref: DatabaseReference? = null
    private var listAdapter: WaterMeterListAdapter? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setFirebaseDatabase()
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
            listAdapter?.setData()
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
                Snackbar.make(root, getString(R.string.clientNotAdded), Snackbar.LENGTH_SHORT).show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSION_REQUEST && grantResults.isNotEmpty()){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("permissionGranted", "true")
                //TODO: onRequestPermissionsResult
            }
        } else {
            Log.d("permissionGranted", "false")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    private fun setFirebaseDatabase() {
        database = Room.databaseBuilder(
            applicationContext, WaterMeterDatabase::class.java,
            DATABASE_NAME
        ).build()
        db = FirebaseDatabase.getInstance("https://watermeters.firebaseio.com/")
        ref = db?.getReference("WaterMeters")
    }

    private fun setRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.list)

        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        listAdapter = WaterMeterListAdapter(waterMeters)
        recyclerView.adapter = listAdapter
        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        Log.d("wM before retriv size: ", waterMeters.size.toString())
        launch {
            waterMeters = retrieveItems(database) as ArrayList<WaterMeter>
        }
        Log.d("wM after retriv size: ", waterMeters.size.toString())
        listAdapter?.setData()
        listAdapter?.notifyDataSetChanged()
        if (waterMeters.isEmpty()) updateListView()
    }

    private fun updateListView() {
        ref?.addValueEventListener(object : ValueEventListener {
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
                launch {
                    updateAllItems(database, waterMeters)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    //FIXME Не работает Room, прологировать
    private suspend fun retrieveItems(database: WaterMeterDatabase?) =
        withContext(Dispatchers.IO) {
            database?.dao()?.retrieveItemList()
        }

    private suspend fun updateAllItems(
        database: WaterMeterDatabase?,
        selected: MutableCollection<WaterMeter>
    ) = withContext(Dispatchers.IO) {
        database?.dao()?.updateAll(selected)
    }
}