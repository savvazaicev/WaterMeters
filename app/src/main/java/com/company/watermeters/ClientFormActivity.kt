package com.company.watermeters

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.*


class ClientFormActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private lateinit var root: RelativeLayout
    private lateinit var textView: TextView
    private var storageRef: StorageReference? = null
    private val pickImageRequest = 112

    //    private var filePath: Uri? = null
    private var allPath = ArrayList<Uri>()
    private var imageUUIDs = ArrayList<String>()

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
        addPhoto.setOnClickListener { chooseImage() }
    }

    private fun saveClient() {
        //OPTIMIZE
        uploadImage()
        val fullName = findViewById<TextInputEditText>(R.id.full_name)?.text?.toString()
        val address = findViewById<TextInputEditText>(R.id.address)?.text?.toString()
        val registryNumber = findViewById<TextInputEditText>(R.id.registry_number)?.text?.toString()
        val number = findViewById<TextInputEditText>(R.id.number)?.text?.toString()
        val date = findViewById<TextInputEditText>(R.id.date)?.text?.toString()
        val endDate = findViewById<TextInputEditText>(R.id.end_date)?.text?.toString()
        val waterType = findViewById<AutoCompleteTextView>(R.id.dropdown_text)?.text?.toString()
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
        val client = Client(
            fullName,
            address,
            registryNumber,
            number,
            date,
            endDate,
            waterType,
            certificateNumber,
            imageUUIDs,
            email
        )
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

    private fun uploadImage() {
        if (allPath.isNotEmpty()) {
//        val file: Uri = Uri.fromFile(File("path/to/images/rivers.jpg"))
            for (filePath in allPath) {
                val randomUUID = UUID.randomUUID().toString()
                imageUUIDs.add(randomUUID)
                val riversRef = storageRef!!.child("images/$randomUUID")

                riversRef.putFile(filePath)
                    .addOnSuccessListener { taskSnapshot -> // Get a URL to the uploaded content
                        val downloadUrl: Uri? = taskSnapshot.uploadSessionUri
//                        imageURLs.add(downloadUrl.toString())
                    }
                    .addOnFailureListener {
                        // Handle unsuccessful uploads
                        // ...
                    }
                    .addOnProgressListener {
                        var progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                        var uploaded = "Uploaded ${progress.toInt()}%"
                    }
            }
        }
    }

    private fun chooseImage() {
        //Выбирается несколько фото, их пути сохраняются
        //Можно удалить выбранные фото
        //При отправке формы фото загружаются, показывается прогресс загрузки фото
//        val intent = Intent()
//        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, pickImageRequest);
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            intent.action = Intent.ACTION_PICK
        } else {
            //TODO Action для версии ниже кит кат
            var intent = Intent()
            intent.type = "image/*"
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture")
                , pickImageRequest
            )
        }
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickImageRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequest && resultCode == Activity.RESULT_OK
            && data != null
        ) {
//            allPath = ImageCho
//            filePath = data.data
            val count = data.clipData!!.itemCount
            addPhoto.text = "Добавлено $count фото"
            clearButton.visibility = View.VISIBLE
//            try {
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePAth)
////                imageView.setImageBitmap(bitmap)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
            if (data.clipData != null) {
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
//                    getPathFromURI(imageUri)
                    allPath.add(imageUri)
                }
            } else if (data.data != null) {
                val imagePath: String = data.data!!.path!!
                Log.e("imagePath", imagePath);
            }
        }
    }

//    private fun getPathFromURI(uri: Uri) {
//        var path: String = uri.path!! // uri = any content Uri
//
//        val databaseUri: Uri
//        val selection: String?
//        val selectionArgs: Array<String>?
//        if (path.contains("/document/image:")) { // files selected from "Documents"
//            databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            selection = "_id=?"
//            selectionArgs = arrayOf(DocumentsContract.getDocumentId(uri).split(":")[1])
//        } else { // files selected from all other sources, especially on Samsung devices
//            databaseUri = uri
//            selection = null
//            selectionArgs = null
//        }
//        try {
//            val projection = arrayOf(
//                MediaStore.Images.Media.DATA,
//                MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.ORIENTATION,
//                MediaStore.Images.Media.DATE_TAKEN
//            ) // some example data you can query
//            val cursor = contentResolver.query(
//                databaseUri,
//                projection, selection, selectionArgs, null
//            )
//            if (cursor.moveToFirst()) {
//                val columnIndex = cursor.getColumnIndex(projection[0])
//                imagePath = cursor.getString(columnIndex)
//                // Log.e("path", imagePath);
//                imagesPathList.add(imagePath)
//            }
//            cursor.close()
//        } catch (e: Exception) {
//            Log.e(TAG, e.message, e)
//        }
//    }
}
