package com.company.watermeters

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.company.watermeters.model.Client
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.content_client_form.*
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ClientFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, CoroutineScope {

    companion object {
        private const val PICK_IMAGE_REQUEST = 112
    }

    private lateinit var textView: TextView

    private var allPath: MutableCollection<Uri> = ArrayList()
    private var imageURLs: MutableCollection<String> =
        Collections.synchronizedList(ArrayList<String>())
    private lateinit var imageUri: Uri
    private lateinit var photo: File
    private var format = SimpleDateFormat("dd-MM-yyyy", Locale("ru"))
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_form)
        setBackButton()
        setClickListeners()
        setDropdownTextAdapter()

        registry_number?.setText(MainActivity.selectedItemRegistryNumber)
        MainActivity.selectedItemRegistryNumber = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            if (photo.exists()) {
                allPath.add(imageUri)
                showPhotosSelected(1)
            } else if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    allPath.add(imageUri)
                }
                showPhotosSelected(count)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c: Calendar = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val currentDateString: String = format.format(c.time)
        textView.text = currentDateString
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.left_to_right)
    }

    private fun setClickListeners() {
        save_button.setOnClickListener { saveClient() }
        date.setOnClickListener {
            textView = date
            format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale("ru"))
            DatePickerFragment().show(supportFragmentManager, "date picker")
        }
        end_date.setOnClickListener {
            textView = end_date
            format = SimpleDateFormat("dd-MM-yyyy", Locale("ru"))
            DatePickerFragment().show(supportFragmentManager, "date picker")
        }
        clearButton.setOnClickListener {
            allPath.clear()
            addPhoto.text = getString(R.string.add_photo)
            clearButton.visibility = View.INVISIBLE
        }
        addPhoto.setOnClickListener { chooseImageIntent() }
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

    private fun setDropdownTextAdapter() {
        val items = listOf(getString(R.string.hotWater), getString(R.string.coldWater))
        val adapter = ArrayAdapter(this@ClientFormActivity, R.layout.dropdown_item, items)
        dropdown_text.setAdapter(adapter)
    }

    private fun showPhotosSelected(count: Int) {
        addPhoto.text = getString(R.string.photoAdded, count)
        clearButton.visibility = View.VISIBLE
    }

    private fun chooseImageIntent() {
        val root =
            File(getExternalFilesDir(null).toString() + File.separator.toString() + "images" + File.separator)
        root.mkdirs()
        val randomUUID = UUID.randomUUID().toString()
        photo = File(root, randomUUID)
        imageUri = Uri.fromFile(photo)

        val cameraIntents = ArrayList<Intent>()
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager: PackageManager = packageManager
        val listCam: List<ResolveInfo> = packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val packageName: String = res.activityInfo.packageName
            val intent = Intent(captureIntent)
            intent.component = ComponentName(
                res.activityInfo.packageName,
                res.activityInfo.name
            )
            intent.setPackage(packageName)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.putExtra("isCamera", true)
            cameraIntents.add(intent)
        }

        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        val chooser = Intent.createChooser(galleryIntent, "Выберите приложение")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray())
        startActivityForResult(chooser, PICK_IMAGE_REQUEST)
    }

    private fun saveClient() {
        val fullName = findViewById<TextInputEditText>(R.id.full_name)?.text?.toString()
        val address = findViewById<TextInputEditText>(R.id.address)?.text?.toString()
        val registryNumber =
            findViewById<TextInputEditText>(R.id.registry_number)?.text?.toString()
        val number = findViewById<TextInputEditText>(R.id.number)?.text?.toString()
        val date = findViewById<TextInputEditText>(R.id.date)?.text?.toString()
        val endDate = findViewById<TextInputEditText>(R.id.end_date)?.text?.toString()
        val waterType =
            findViewById<AutoCompleteTextView>(R.id.dropdown_text)?.text?.toString()
        val certificateNumber =
            findViewById<TextInputEditText>(R.id.certificateNumber)?.text?.toString()
        val sharedPref = getSharedPreferences("SaveData", Context.MODE_PRIVATE)
        val email = sharedPref?.getString("email", null)

        fullNameLayout.error = if (fullName == "") getString(R.string.requiredField) else null
        addressLayout.error = if (address == "") getString(R.string.requiredField) else null
        registryNumberLayout.error =
            if (registryNumber == "") getString(R.string.requiredField) else null
        numberLayout.error = if (number == "") getString(R.string.requiredField) else null
        dateLayout.error = if (date == "") getString(R.string.requiredField) else null
        endDateLayout.error = if (endDate == "") getString(R.string.requiredField) else null
        waterTypeLayout.error = if (waterType == "") getString(R.string.requiredField) else null
        certificateNumberLayout.error =
            if (certificateNumber == "") getString(R.string.requiredField) else null
        if (fullName == "" || address == "" || registryNumber == "" ||
            number == "" || date == "" || endDate == "" || waterType == "" || certificateNumber == ""
        ) return

        save_button.isEnabled = false
        launch {
            for (filePath in allPath) {
                uploadImage(filePath)
            }
            val client = Client(
                fullName, address, registryNumber, number,
                endDate, waterType, certificateNumber, imageURLs, email
            )
            val db = FirebaseDatabase.getInstance("https://clients-a1b6a.firebaseio.com/")
            val myRef = db.getReference("Clients/$date/")
            myRef.setValue(client)
                .addOnCompleteListener {
                    val intent = Intent()
                    intent.putExtra("customerIsAdded", true)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                .addOnFailureListener {
                    val root = findViewById<RelativeLayout>(R.id.root_element)
                    Snackbar.make(root, "Ошибка! Клиент не добавлен", Snackbar.LENGTH_SHORT)
                        .show()
                    save_button.isEnabled = true
                }
        }
    }

    private suspend fun uploadImage(filePath: Uri) {
        return suspendCoroutine { continuation ->
            val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
            val randomUUID = UUID.randomUUID().toString()
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/$randomUUID")
            imageRef.putFile(filePath)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUrl: Uri? = task.result
                            imageURLs.add(downloadUrl.toString())
                            continuation.resume(Unit)
                        }
                    }
                }
                .addOnFailureListener {
                    continuation.resume(Unit)
                }
                .addOnProgressListener {
                    //FIXME: при нескольких изображениях прогресс идёт неправильно
                    progressBar.visibility = View.VISIBLE
                    progressBar.run {
                        val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                        progressBar.progress = progress.toInt()
                    }
                }
        }
    }
}