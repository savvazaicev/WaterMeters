package com.company.watermeters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.company.watermeters.MainActivity.Companion.waterMeters
import com.company.watermeters.model.WaterMeter

class WaterMeterListAdapter(
    private val context: Context,
    private var waterMeterList: ArrayList<WaterMeter>
) : BaseAdapter(), Filterable {

    private var resultList = ArrayList<WaterMeter>()
    private var noFilterResults = false
    private var inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var view = convertView
        val viewHolder: ViewHolder?
        if (view == null) {
            view = inflater.inflate(R.layout.list_item, parent, false)
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
        dateTextView?.text = waterMeter.date
        methodologyTextView?.text = waterMeter.methodology
        coldTextView?.text = "Хол. " + waterMeter.coldWater
        hotTextView?.text = "Гор. " + waterMeter.hotWater
        return view
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        if (!noFilterResults) waterMeters.forEach { resultList.add(it.clone() as WaterMeter) }
    }

//    fun updateData(waterMeterList: ArrayList<WaterMeter>) {
//        this.waterMeterList = waterMeterList
//        waterMeters.forEach { resultList.add(it.clone() as WaterMeter) }
//        notifyDataSetChanged()
//    }

    fun getList(): ArrayList<WaterMeter> {
        return resultList
    }

    override fun getItem(position: Int): Any {
        return resultList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return resultList.size
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

    override fun getFilter(): Filter? {
        return exampleFilter
    }

    private val exampleFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            val filteredList: MutableList<WaterMeter> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                waterMeters.forEach { filteredList.add(it.clone() as WaterMeter) }
            } else {
                val filterPattern =
                    constraint.toString().toLowerCase().trim()
                for (item in waterMeters) {
                    if (item.registryNumber?.toLowerCase()?.contains(filterPattern) == true ||
                        item.name?.toLowerCase()?.contains(filterPattern) == true ||
                        item.producer?.toLowerCase()?.contains(filterPattern) == true ||
                        item.date?.toLowerCase()?.contains(filterPattern) == true ||
                        item.methodology?.toLowerCase()?.contains(filterPattern) == true ||
                        item.type?.toLowerCase()?.contains(filterPattern) == true
                    ) {
                        filteredList.add(item.clone() as WaterMeter)
                    }
                }
            }
            if (filteredList.isEmpty()) noFilterResults = true
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults
        ) {
            resultList.clear()
            resultList.addAll(results.values as ArrayList<WaterMeter>)
            notifyDataSetChanged()
        }
    }
}