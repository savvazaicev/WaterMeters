package com.example.watermeters

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.watermeters.MainActivity.Companion.listAdapter
import com.example.watermeters.MainActivity.Companion.listView
import com.example.watermeters.MainActivity.Companion.selectedItem
import com.example.watermeters.MainActivity.Companion.waterMeters
import com.example.watermeters.db.DBHelper
import com.example.watermeters.model.WaterMeter
import kotlinx.android.synthetic.main.fragment_second.*

class SecondFragment : Fragment() {
    private var dbHelper: DBHelper? = null

    companion object {
        var action = "newItem"
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        dbHelper = DBHelper(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.сancel_button).setOnClickListener {
            listView?.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        if (action == "editItem") {
            mark.setText(waterMeters[selectedItem].mark)
            model.setText(waterMeters[selectedItem].model)
            number.setText(waterMeters[selectedItem].number)
            address.setText(waterMeters[selectedItem].address)
        }
        save_button.setOnClickListener { saveWaterMeter(action) }
    }

    private fun saveWaterMeter(tag: String) {
        when (tag) {
            "newItem" -> {
                val mark = view?.findViewById<EditText>(R.id.mark)?.text?.toString()
                val model = view?.findViewById<EditText>(R.id.model)?.text?.toString()
                val number = view?.findViewById<EditText>(R.id.number)?.text?.toString()
                val address = view?.findViewById<EditText>(R.id.address)?.text?.toString()
                if (mark != "" || model != "" || number != "" || address != "") {
                    val newWaterMeter =
                        dbHelper?.addNewWaterMeter(WaterMeter(null, mark, model, number, address))
                    if (newWaterMeter != null) {
                        waterMeters.add(newWaterMeter)
                    }
                    listAdapter?.notifyDataSetChanged()
                }
                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                listView?.visibility = View.VISIBLE
            }
            "editItem" -> {
                val mark = view?.findViewById<EditText>(R.id.mark)?.text?.toString()
                val model = view?.findViewById<EditText>(R.id.model)?.text?.toString()
                val number = view?.findViewById<EditText>(R.id.number)?.text?.toString()
                val address = view?.findViewById<EditText>(R.id.address)?.text?.toString()
                waterMeters[selectedItem].mark = mark
                waterMeters[selectedItem].model = model
                waterMeters[selectedItem].number = number
                waterMeters[selectedItem].address = address
                dbHelper?.updateWaterMeter(waterMeters[selectedItem])
                listAdapter?.notifyDataSetChanged()
                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                listView?.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        dbHelper?.close()
        super.onDestroy()
    }
}
