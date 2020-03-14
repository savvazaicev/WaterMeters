package com.example.watermeters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.watermeters.model.WaterMeter

class WaterMeterListAdapter(private val context: Context, private val waterMeterList: ArrayList<WaterMeter>): BaseAdapter() {

    private var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var view = convertView
        val viewHolder: ViewHolder?
        if (view == null) {
            view =  inflater.inflate(R.layout.list_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.markTextView = view.findViewById(R.id.task_item_mark)
            viewHolder.modelTextView = view.findViewById(R.id.task_item_model)
            viewHolder.numberTextView = view.findViewById(R.id.task_item_number)
            viewHolder.addressTextView = view.findViewById(R.id.task_item_address)

            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder?
        }

        val markTextView = viewHolder?.markTextView
        val modelTextView = viewHolder?.modelTextView
        val numberTextView = viewHolder?.numberTextView
        val addressTextView = viewHolder?.addressTextView

        val waterMeter = getItem(position) as WaterMeter

        markTextView?.text = waterMeter.mark
        modelTextView?.text = waterMeter.model
        numberTextView?.text = waterMeter.number
        addressTextView?.text = waterMeter.address
        return view
    }

    override fun getItem(position: Int): Any {
        return waterMeterList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return waterMeterList.size
    }

    private class ViewHolder {
        var markTextView: TextView? = null
        var modelTextView: TextView? = null
        var numberTextView: TextView? = null
        var addressTextView: TextView? = null
    }
}