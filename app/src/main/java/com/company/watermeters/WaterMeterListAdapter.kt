package com.company.watermeters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.company.watermeters.model.WaterMeter
import java.util.*
import kotlin.collections.ArrayList

class WaterMeterListAdapter(private var waterMeters: MutableList<WaterMeter>) :
    RecyclerView.Adapter<WaterMeterListAdapter.ViewHolder>(), Filterable {

    private lateinit var context: Context
    //    private var isFiltering = false
    val originalList: MutableList<WaterMeter> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.list_item, parent, false)

        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val intent = Intent(context as Activity, ClientFormActivity::class.java)
                    .putExtra(
                        "registryNumber",
                        waterMeters[viewHolder.adapterPosition].registryNumber
                    )
                (context as Activity).startActivityForResult(
                    intent, 111
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
        holder.dateTextView?.text = listItem.date
        holder.methodologyTextView?.text = listItem.methodology
        holder.coldTextView?.text = context.getString(R.string.cold, listItem.coldWater)
        holder.hotTextView?.text = context.getString(R.string.hot, listItem.hotWater)
    }

    fun updateData(newList: MutableList<WaterMeter>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(waterMeters, newList))
        Log.d("mytag", "updateData waterMeters size: ${waterMeters.size}")
        Log.d("mytag", "updateData newList size: ${newList.size}")
        waterMeters = newList
        waterMeters.forEach { originalList.add(it.copy()) }
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
        Log.d("mytag", "notifyDataSetChanged waterMeters size: ${waterMeters.size}")
        Log.d("mytag", "notifyDataSetChanged newList size: ${newList.size}")
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
//        val originalList: MutableList<WaterMeter> = ArrayList()
//            .also {
//            waterMeters.forEach { it1 -> it.add(it1.copy()) }
//        }

        override fun performFiltering(constraint: CharSequence?): FilterResults? {
//            if (!isFiltering) {
//            waterMeters.forEach { originalList.add(it.copy()) }
//                isFiltering = true
//            }
            Log.d("mytag", "filter originalList size: ${originalList.size}")
            Log.d("mytag", "filter waterMeters size: ${waterMeters.size}")
            val filteredList: MutableList<WaterMeter> = ArrayList()

            if (constraint == null || constraint.isEmpty()) {
                originalList.forEach { filteredList.add(it.copy()) }
                Log.d("mytag", "filter if filteredList size: ${filteredList.size}")
//                isFiltering = false
            } else {
                val filterPattern =
                    constraint.toString().toLowerCase(Locale.ROOT).trim()
                for (item in originalList) {
                    if (item.registryNumber?.toLowerCase(Locale.ROOT)
                            ?.contains(filterPattern) == true ||
                        item.name?.toLowerCase(Locale.ROOT)?.contains(filterPattern) == true ||
                        item.producer?.toLowerCase(Locale.ROOT)?.contains(filterPattern) == true ||
                        item.date?.toLowerCase(Locale.ROOT)?.contains(filterPattern) == true ||
                        item.methodology?.toLowerCase(Locale.ROOT)
                            ?.contains(filterPattern) == true ||
                        item.type?.toLowerCase(Locale.ROOT)?.contains(filterPattern) == true
                    ) {
                        filteredList.add(item.copy())
                    }
                }
                Log.d("mytag", "filter else filteredList size: ${filteredList.size}")
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
            @Suppress("UNCHECKED_CAST")
            if (results.values != null) {
                waterMeters.addAll(results.values as ArrayList<WaterMeter>)
                Log.d("mytag", "results waterMeters size: ${waterMeters.size}")
            }
            notifyDataSetChanged()
        }
    }
}