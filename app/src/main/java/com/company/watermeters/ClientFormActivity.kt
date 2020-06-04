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
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.company.watermeters.model.Client
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.content_client_form.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ClientFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private lateinit var root: RelativeLayout
    private lateinit var textView: TextView
    private var storageRef: StorageReference? = null
    private val pickImageRequest = 112
    private lateinit var progressBar: ProgressBar

    //    private var filePath: Uri? = null
    private var allPath = ArrayList<Uri>()
    private var imageUUIDs = ArrayList<String>()
    private var imageURLs = Collections.synchronizedList(ArrayList<String>())
    private var outputFileUri: Uri? = null
    private lateinit var photo: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_form)
        setBackButton()
        val items = listOf("Горячая", "Холодная")
        val adapter = ArrayAdapter(this@ClientFormActivity, R.layout.dropdown_item, items)
        dropdown_text.setAdapter(adapter)
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
        clearButton.setOnClickListener {
            allPath.clear()
            addPhoto.text = getString(R.string.add_photo)
            clearButton.visibility = View.INVISIBLE
        }
        storageRef = FirebaseStorage.getInstance().reference
        addPhoto.setOnClickListener { chooseImageIntent() }
    }

    private fun saveClient() {
        //OPTIMIZE

//            imageURLs = withContext(Dispatchers.Default) { uploadImage() }
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
//        fullNameLayout.error = if (fullName == "") getString(R.string.requiredField) else null
//        addressLayout.error = if (address == "") getString(R.string.requiredField) else null
//        registryNumberLayout.error =
//            if (registryNumber == "") getString(R.string.requiredField) else null
//        numberLayout.error = if (number == "") getString(R.string.requiredField) else null
//        dateLayout.error = if (date == "") getString(R.string.requiredField) else null
//        endDateLayout.error = if (endDate == "") getString(R.string.requiredField) else null
//        waterTypeLayout.error = if (waterType == "") getString(R.string.requiredField) else null
//        certificateNumberLayout.error =
//            if (certificateNumber == "") getString(R.string.requiredField) else null
//        if (fullName == "" || address == "" || registryNumber == "" ||
//            number == "" || date == "" || endDate == "" || waterType == "" || certificateNumber == ""
//        ) return
        save_button.isEnabled = false
        //FIXME imageURLs is null, multithreading
//            uploadImageJob.await()
        var client: Client
        GlobalScope.launch {
            uploadImages()
            client = Client(
                fullName,
                address,
                registryNumber,
                number,
                date,
                endDate,
                waterType,
                certificateNumber,
                imageURLs,
                email
            )
            db = FirebaseDatabase.getInstance("https://clients-a1b6a.firebaseio.com/")
            myRef = db?.getReference("Clients")
            myRef?.push()?.setValue(client)
                ?.addOnCompleteListener {
                    if (allPath.isEmpty()) {
                        val intent = Intent()
                        intent.putExtra("customerIsAdded", true)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
                ?.addOnFailureListener {
                    Snackbar.make(root, "Ошибка! Клиент не добавлен", Snackbar.LENGTH_SHORT)
                        .show()
                    save_button.isEnabled = true
                }
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

    private suspend fun uploadImages() {
        if (allPath.isNotEmpty()) {
            return suspendCoroutine { continuation ->
                progressBar = findViewById(R.id.progress_bar)
                for (filePath in allPath) {
                    val randomUUID = UUID.randomUUID().toString()
                    imageUUIDs.add(randomUUID)
                    val imageRef = storageRef!!.child("images/$randomUUID")
                    imageRef.putFile(filePath)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val downloadUrl: Uri? = task.result
                                    imageURLs.add(downloadUrl.toString())
                                    if (filePath == allPath.last()) {
                                        continuation.resume(Unit)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            continuation.resume(Unit)
                            // Handle unsuccessful uploads
                            // ...
                        }
                        .addOnProgressListener {
                            //FIXME: при нескольких изображениях прогресс идёт неправильно
                            progressBar.visibility = View.VISIBLE
                            progressBar.run {
                                val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                                progressBar.progress = progress.toInt()
                                if (progress.toInt() == 100) {
                                    val intent = Intent()
                                    intent.putExtra("customerIsAdded", true)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                }
                            }
                        }
                }
            }
        }
    }

    private fun chooseImageIntent() {
        val root =
            File(getExternalFilesDir(null).toString() + File.separator.toString() + "images" + File.separator)
        root.mkdirs()
        val randomUUID = UUID.randomUUID().toString()
        photo = File(root, randomUUID)
        outputFileUri = Uri.fromFile(photo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
                intent.putExtra("isCamera", true)
                cameraIntents.add(intent)
            }

            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            galleryIntent.type = "image/*"

            val chooser = Intent.createChooser(galleryIntent, "Выберите приложение")
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray())
            startActivityForResult(chooser, pickImageRequest);
        } else {
            //TODO Action для версии ниже кит кат (либо get_content, либо без allow_multiple)
            val intent = Intent()
            intent.type = "image/*"
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture")
                , pickImageRequest
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        addPhoto.text = "Добавлено 1 фото"
        clearButton.visibility = View.VISIBLE
        if (resultCode == RESULT_OK && requestCode == pickImageRequest) {
            if (photo.exists()) {
                allPath.add(outputFileUri!!)
            } else if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                addPhoto.text = "Добавлено $count фото"
                clearButton.visibility = View.VISIBLE
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    allPath.add(imageUri)
                }
            }
        }
    }
}