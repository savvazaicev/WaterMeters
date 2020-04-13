package com.company.watermeters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.company.watermeters.MainActivity.Companion.listAdapter
import com.company.watermeters.MainActivity.Companion.listView
import com.company.watermeters.MainActivity.Companion.selectedItemRegistryNumber
import com.company.watermeters.MainActivity.Companion.toolBar
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.model.Client
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_second.*

class SecondFragment : Fragment() {
    private var database: WaterMeterDatabase? = null
    private var db: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null

    companion object {
        var action = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listView?.visibility = View.INVISIBLE
        toolBar?.visibility = View.INVISIBLE
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.сancel_button).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        if (action == "newItem") {
            registry_number.setText(selectedItemRegistryNumber)
        }
        save_button.setOnClickListener { saveClient(action) }
    }

    private fun saveClient(tag: String) {
        val fullName = view?.findViewById<EditText>(R.id.full_name)?.text?.toString()
        val address = view?.findViewById<EditText>(R.id.address)?.text?.toString()
        val registryNumber = view?.findViewById<EditText>(R.id.registry_number)?.text?.toString()
        val number = view?.findViewById<EditText>(R.id.number)?.text?.toString()
        val date = view?.findViewById<EditText>(R.id.date)?.text?.toString()
        val endDate = view?.findViewById<EditText>(R.id.end_date)?.text?.toString()
        if (fullName != "" || address != "" || number != "" || address != "") {
            val client = Client(fullName, address, registryNumber, number, date, endDate)
//                    newWaterMeter.id = AddTaskAsyncTask(database, newWaterMeter).execute().get()
            db = FirebaseDatabase.getInstance("https://clients-a1b6a.firebaseio.com/")
            myRef = db?.getReference("Clients")
            myRef?.setValue(client)
        }
//            "editItem" -> {
//                waterMeters[selectedItem].name = mark
//                waterMeters[selectedItem].registryNumber = model
//                waterMeters[selectedItem].producer = number
//                waterMeters[selectedItem].type = address
//                UpdateTaskAsyncTask(database, waterMeters[selectedItem]).execute()
//            }
        listAdapter?.notifyDataSetChanged()
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }
//
//    override fun onDestroy() {
//        action
//        super.onDestroy()
//    }
}
