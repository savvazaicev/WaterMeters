package com.company.watermeters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.company.watermeters.model.WaterMeter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WaterMeterListAdapter(private var waterMeters: MutableList<WaterMeter>) :
    RecyclerView.Adapter<WaterMeterListAdapter.ViewHolder>(), Filterable {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.list_item, parent, false)

        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                MainActivity.selectedItemRegistryNumber =
                    waterMeters[viewHolder.adapterPosition].registryNumber
                (context as Activity).startActivityForResult(
                    Intent(
                        context as Activity,
                        ClientFormActivity::class.java
                    ), 111
                )
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listItem = waterMeters[holder.adapterPosition]
        holder.registryNumberTextView?.text = listItem.registryNumber
        holder.nameTextView?.text = listItem.name
        holder.typeTextView?.text = listItem.type
        holder.producerTextView?.text = listItem.producer
        holder.dateTextView?.text = formatDate(listItem.date)
        holder.methodologyTextView?.text = listItem.methodology
        holder.coldTextView?.text = context.getString(R.string.cold, listItem.coldWater)
        holder.hotTextView?.text = context.getString(R.string.hot, listItem.hotWater)
    }

    fun updateData(newList: MutableList<WaterMeter>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(waterMeters, newList))
        waterMeters = newList
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    override fun getItemCount() = waterMeters.size

    override fun getFilter() = filter

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val registryNumberTextView: TextView? = itemView.findViewById(R.id.registry_number)
        val nameTextView: TextView? = itemView.findViewById(R.id.name)
        val typeTextView: TextView? = itemView.findViewById(R.id.type)
        val producerTextView: TextView? = itemView.findViewById(R.id.producer)
        val dateTextView: TextView? = itemView.findViewById(R.id.date)
        val methodologyTextView: TextView? = itemView.findViewById(R.id.methodology)
        val coldTextView: TextView? = itemView.findViewById(R.id.cold)
        val hotTextView: TextView? = itemView.findViewById(R.id.hot)
    }

    private val filter = object : Filter() {
        val originalList = ArrayList<WaterMeter>()

        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            waterMeters.forEach { originalList.add(it.copy()) }
            val filteredList: MutableList<WaterMeter> = ArrayList()

            if (constraint == null || constraint.isEmpty()) {
                originalList.forEach { filteredList.add(it.copy()) }
            } else {
                val filterPattern =
                    constraint.toString().toLowerCase().trim()
                for (item in originalList) {
                    if (item.registryNumber?.toLowerCase()?.contains(filterPattern) == true ||
                        item.name?.toLowerCase()?.contains(filterPattern) == true ||
                        item.producer?.toLowerCase()?.contains(filterPattern) == true ||
                        item.date?.toLowerCase()?.contains(filterPattern) == true ||
                        item.methodology?.toLowerCase()?.contains(filterPattern) == true ||
                        item.type?.toLowerCase()?.contains(filterPattern) == true
                    ) {
                        filteredList.add(item.copy())
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults
        ) {
            waterMeters.clear()
            waterMeters.addAll(results.values as ArrayList<WaterMeter>)
            notifyDataSetChanged()
        }
    }

    //OPTIMIZE Перенести метод в класс DateConverter
    private fun formatDate(date: String?): String? {
        var formattedDate: String? = date
        if (formattedDate != null) {
            try {
                val formattedString = SimpleDateFormat("d/M/yy", Locale("ru")).parse(formattedDate)
                if (formattedString != null) {
                    formattedDate =
                        SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(formattedString)
                }
            } finally {
                return formattedDate
            }
        }
        return formattedDate
    }
}