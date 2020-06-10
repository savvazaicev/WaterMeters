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
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.company.watermeters.databinding.ActivityClientFormBinding
import com.company.watermeters.model.Client
import com.google.android.material.snackbar.Snackbar
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

    private var allPath: MutableList<Uri> = ArrayList()
    private var imageURLs: MutableList<String> =
        Collections.synchronizedList(ArrayList<String>())
    private lateinit var imageUri: Uri
    private lateinit var photo: File
    private var format = SimpleDateFormat("dd-MM-yyyy", Locale("ru"))
    private lateinit var binding: ActivityClientFormBinding
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBackButton()
        setClickListeners()
        setDropdownTextAdapter()
        binding.contentClientForm.registryNumber.setText(intent.getStringExtra("registryNumber"))
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
        val c: Calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        val currentDateString: String = format.format(c.time)
        textView.text = currentDateString
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.left_to_right)
    }

    private fun setClickListeners() {
        save_button.setOnClickListener { saveClient() }
        with(binding.contentClientForm) {
            date.setOnClickListener {
                textView = date
                format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale("ru"))
                DatePickerFragment().show(supportFragmentManager, "date picker")
            }
            endDate.setOnClickListener {
                textView = end_date
                format = SimpleDateFormat("dd-MM-yyyy", Locale("ru"))
                DatePickerFragment().show(supportFragmentManager, "date picker")
            }
            clearButton.setOnClickListener {
                allPath.clear()
                addPhoto.text = getString(R.string.add_photo)
                clearButton.visibility = View.INVISIBLE
            }
        }
        addPhoto.setOnClickListener { chooseImageIntent() }
    }

    private fun setBackButton() {
        val toolbar = binding.toolbar
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
        binding.contentClientForm.dropdownText.setAdapter(adapter)
    }

    private fun showPhotosSelected(count: Int) {
        binding.contentClientForm.addPhoto.text = getString(R.string.photoAdded, count)
        binding.contentClientForm.clearButton.visibility = View.VISIBLE
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
            val intent = Intent(captureIntent).apply {
                component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                setPackage(packageName)
                putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                putExtra("isCamera", true)
            }
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
        with(binding.contentClientForm) {
            val fullName = fullName.text?.toString()
            val address = address.text?.toString()
            val registryNumber = registryNumber.text?.toString()
            val number = number.text?.toString()
            val date = date.text?.toString()
            val endDate = endDate.text?.toString()
            val waterType = dropdownText.text?.toString()
            val certificateNumber = certificateNumber.text?.toString()
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

            saveButton.isEnabled = false
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
                        Snackbar.make(
                            binding.root,
                            "Ошибка! Клиент не добавлен",
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                        save_button.isEnabled = true
                    }
            }
        }
    }

    private suspend fun uploadImage(filePath: Uri) {
        return suspendCoroutine { continuation ->
            val progressBar = binding.contentClientForm.progressBar
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
                    progressBar.visibility = View.VISIBLE
                    progressBar.run {
                        val alreadyDownloadedPart = 100.0 * imageURLs.size / allPath.size
                        val currentPartCoefficient = 100.0 * 1 / allPath.size
                        val progress =
                            (alreadyDownloadedPart + currentPartCoefficient * it.bytesTransferred / it.totalByteCount)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar.setProgress(progress.toInt(), true)
                        } else {
                            progressBar.progress = progress.toInt()
                        }
                    }
                }
        }
    }
}