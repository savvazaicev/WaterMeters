package com.company.watermeters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.company.watermeters.MainActivity.AddTaskAsyncTask
import com.company.watermeters.MainActivity.Companion.listAdapter
import com.company.watermeters.MainActivity.Companion.listView
import com.company.watermeters.MainActivity.Companion.selectedItem
import com.company.watermeters.MainActivity.Companion.toolBar
import com.company.watermeters.MainActivity.Companion.waterMeters
import com.company.watermeters.MainActivity.UpdateTaskAsyncTask
import com.company.watermeters.db.WaterMeterDatabase
import com.company.watermeters.model.WaterMeter
import kotlinx.android.synthetic.main.fragment_second.*

class SecondFragment : Fragment() {
    private var database: WaterMeterDatabase? = null

    companion object {
        var action = "newItem"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listView?.visibility = View.INVISIBLE
        toolBar?.visibility = View.INVISIBLE
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        view.findViewById<Button>(R.id.сancel_button).setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }
//        if (action == "editItem") {
//            registry_number.setText(waterMeters[selectedItem].name)
//            date.setText(waterMeters[selectedItem].registryNumber)
//            number.setText(waterMeters[selectedItem].producer)
//            address.setText(waterMeters[selectedItem].type)
//        }
//        save_button.setOnClickListener { saveWaterMeter(action) }
//    }

//    private fun saveWaterMeter(tag: String) {
//        val mark = view?.findViewById<EditText>(R.id.registry_number)?.text?.toString()
//        val model = view?.findViewById<EditText>(R.id.date)?.text?.toString()
//        val number = view?.findViewById<EditText>(R.id.number)?.text?.toString()
//        val address = view?.findViewById<EditText>(R.id.address)?.text?.toString()
//        when (tag) {
//            "newItem" -> {
//                if (mark != "" || model != "" || number != "" || address != "") {
//                    val newWaterMeter = WaterMeter(null, mark, model, number, address)
//                    newWaterMeter.id = AddTaskAsyncTask(database, newWaterMeter).execute().get()
//                    if (newWaterMeter != null) {
//                        waterMeters.add(newWaterMeter)
//                    }
//                }
//            }
//            "editItem" -> {
//                waterMeters[selectedItem].name = mark
//                waterMeters[selectedItem].registryNumber = model
//                waterMeters[selectedItem].producer = number
//                waterMeters[selectedItem].type = address
//                UpdateTaskAsyncTask(database, waterMeters[selectedItem]).execute()
//            }
//        }
//        listAdapter?.notifyDataSetChanged()
//        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
