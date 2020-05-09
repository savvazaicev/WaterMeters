package com.company.watermeters

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.company.watermeters.model.Client
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.content_client_form.*
import java.text.SimpleDateFormat
import java.util.*

class ClientFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private lateinit var root: RelativeLayout
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_form)
        setBackButton()
        registry_number?.setText(MainActivity.selectedItemRegistryNumber)
        MainActivity.selectedItemRegistryNumber = null
        save_button.setOnClickListener { saveClient() }
        root = findViewById(R.id.root_element)
        date.setOnClickListener {
            textView = date
            val datePicker: DialogFragment = DatePickerFragment()
            datePicker.show(supportFragmentManager, "date picker")
        }
        end_date.setOnClickListener {
            textView = end_date
            val datePicker: DialogFragment = DatePickerFragment()
            datePicker.show(supportFragmentManager, "date picker")
        }
    }

    private fun saveClient() {
        //OPTIMIZE
        val fullName = findViewById<TextInputEditText>(R.id.full_name)?.text?.toString()
        val address = findViewById<TextInputEditText>(R.id.address)?.text?.toString()
        val registryNumber = findViewById<TextInputEditText>(R.id.registry_number)?.text?.toString()
        val number = findViewById<TextInputEditText>(R.id.number)?.text?.toString()
        val date = findViewById<TextInputEditText>(R.id.date)?.text?.toString()
        val endDate = findViewById<TextInputEditText>(R.id.end_date)?.text?.toString()
        val sharedPref = getSharedPreferences("SaveData", Context.MODE_PRIVATE)
        val email = sharedPref?.getString("email", null)
        fullNameLayout.error = if (fullName == "") getString(R.string.requiredField) else null
        addressLayout.error = if (address == "") getString(R.string.requiredField) else null
        registryNumberLayout.error = if (registryNumber == "") getString(R.string.requiredField) else null
        numberLayout.error = if (number == "") getString(R.string.requiredField) else null
        dateLayout.error = if (date == "") getString(R.string.requiredField) else null
        endDateLayout.error = if (endDate == "") getString(R.string.requiredField) else null
        if (fullName == "" || address == "" || registryNumber == "" ||
            number == "" || date == "" || endDate == ""
        ) return
        save_button.isEnabled = false
        val client = Client(fullName, address, registryNumber, number, date, endDate, email)
        db = FirebaseDatabase.getInstance("https://clients-a1b6a.firebaseio.com/")
        myRef = db?.getReference("Clients")
        myRef?.push()?.setValue(client)
            ?.addOnCompleteListener {
                val intent = Intent()
                //OPTIMIZE лишние строки
                intent.putExtra("customerIsAdded", true)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            ?.addOnFailureListener {
                Snackbar.make(root, "Ошибка! Клиент не добавлен", Snackbar.LENGTH_SHORT).show()
                save_button.isEnabled = true
            }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c: Calendar = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val format = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        val currentDateString: String = format.format(c.time)
        textView.text = currentDateString
    }

    private fun setBackButton() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.left_to_right)
    }
}
