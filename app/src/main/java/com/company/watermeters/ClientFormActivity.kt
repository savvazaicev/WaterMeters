package com.company.watermeters

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.company.watermeters.model.Client
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.content_client_form.*

class ClientFormActivity : AppCompatActivity() {

    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_form)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        registry_number?.setText(MainActivity.selectedItemRegistryNumber)
        save_button.setOnClickListener { saveClient() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.left_to_right)
    }

    private fun saveClient() {
        val fullName = findViewById<TextInputEditText>(R.id.full_name)?.text?.toString()
        val address = findViewById<TextInputEditText>(R.id.address)?.text?.toString()
        val registryNumber = findViewById<TextInputEditText>(R.id.registry_number)?.text?.toString()
        val number = findViewById<TextInputEditText>(R.id.number)?.text?.toString()
        val date = findViewById<TextInputEditText>(R.id.date)?.text?.toString()
        val endDate = findViewById<TextInputEditText>(R.id.end_date)?.text?.toString()
        if (fullName != "" || address != "" || number != "" || address != "") {
            val client = Client(fullName, address, registryNumber, number, date, endDate)
            db = FirebaseDatabase.getInstance("https://clients-a1b6a.firebaseio.com/")
            myRef = db?.getReference("Clients")
            myRef?.setValue(client)
        }
    }
}
