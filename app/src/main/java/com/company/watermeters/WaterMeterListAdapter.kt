package com.company.watermeters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.company.watermeters.model.WaterMeter

class WaterMeterListAdapter(private val context: Context, private val waterMeterList: ArrayList<WaterMeter>): BaseAdapter() {

    private var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var view = convertView
        val viewHolder: ViewHolder?
        if (view == null) {
            view =  inflater.inflate(R.layout.list_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.registryNumberTextView = view.findViewById(R.id.registry_number)
            viewHolder.nameTextView = view.findViewById(R.id.name)
            viewHolder.typeTextView = view.findViewById(R.id.type)
            viewHolder.producerTextView = view.findViewById(R.id.producer)
            viewHolder.dateTextView = view.findViewById(R.id.date)
            viewHolder.methodologyTextView = view.findViewById(R.id.methodology)
            viewHolder.coldTextView = view.findViewById(R.id.cold)
            viewHolder.hotTextView = view.findViewById(R.id.hot)

            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder?
        }

        val registryNumberTextView = viewHolder?.registryNumberTextView
        val nameTextView = viewHolder?.nameTextView
        val typeTextView = viewHolder?.typeTextView
        val producerTextView = viewHolder?.producerTextView
        val dateTextView = viewHolder?.dateTextView
        val methodologyTextView = viewHolder?.methodologyTextView
        val coldTextView = viewHolder?.coldTextView
        val hotTextView = viewHolder?.hotTextView

        val waterMeter = getItem(position) as WaterMeter

        registryNumberTextView?.text = waterMeter.registryNumber
        nameTextView?.text = waterMeter.name
        typeTextView?.text = waterMeter.type
        producerTextView?.text = waterMeter.producer
        dateTextView?.text = waterMeter.date.toString()
        methodologyTextView?.text = waterMeter.methodology
        coldTextView?.text = waterMeter.coldWater
        hotTextView?.text = waterMeter.hotWater
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
        var registryNumberTextView: TextView? = null
        var nameTextView: TextView? = null
        var typeTextView: TextView? = null
        var producerTextView: TextView? = null
        var dateTextView: TextView? = null
        var methodologyTextView: TextView? = null
        var coldTextView: TextView? = null
        var hotTextView: TextView? = null
    }
}