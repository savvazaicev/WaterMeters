package com.company.watermeters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.company.watermeters.databinding.ActivityMainBinding
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.model.WaterMeter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private const val ADD_CLIENT_REQUEST = 111
    }

    private var waterMeters: MutableList<WaterMeter> = ArrayList()
    private var database: WaterMeterDatabase? = null
    private var listAdapter: WaterMeterListAdapter? = null
    private lateinit var binding: ActivityMainBinding
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolbar)
        setRoomDatabase()
        setRecyclerView()
        button_first.setOnClickListener {
            val intent = Intent(this, ClientFormActivity::class.java)
            startActivityForResult(intent, ADD_CLIENT_REQUEST)
        }
        launch { authWithSavedData() }
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
            exitItem.isVisible = true
            false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_CLIENT_REQUEST && resultCode == RESULT_OK && data != null) {
            val customerIsAdded: Boolean = data.getBooleanExtra("customerIsAdded", false)
            if (customerIsAdded) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.clientAdded),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.clientNotAdded),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (R.id.exit_item == item.itemId) {
            val intent = Intent(this, AuthActivity::class.java)
            intent.putExtra("actionExit", true)
            startActivity(intent)
            true
        } else super.onOptionsItemSelected(item)
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
        val recyclerView = binding.contentMain.list
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
                            date = formatDate(it.date)
                        })
                    }
                }
                listAdapter?.updateData(waterMeters)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun formatDate(date: String?): String? {
        var formattedDate: String? = date
        if (formattedDate != null) {
            try {
                val formattedString = SimpleDateFormat("d/M/yy", Locale("ru")).parse(formattedDate)
                if (formattedString != null) {
                    formattedDate =
                        SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(formattedString)
                }
            } finally {
                return formattedDate
            }
        }
        return formattedDate
    }

    private suspend fun authWithSavedData() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val sharedPref = getSharedPreferences("SaveData", Context.MODE_PRIVATE)
        val email = sharedPref?.getString(AuthActivity.EMAIL, null)
        val password = sharedPref?.getString(AuthActivity.PASSWORD, null)
        if (email != null && password != null) {
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnFailureListener {
                        startActivity(Intent(this, AuthActivity::class.java))
                        finish()
                    }
                    .await()
            } catch (e: Exception) {
                throw e
            }
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private suspend fun retrieveItems() = database?.dao()?.getAll()

    private suspend fun updateDatabaseData() = database?.dao()?.updateAll(waterMeters)
}