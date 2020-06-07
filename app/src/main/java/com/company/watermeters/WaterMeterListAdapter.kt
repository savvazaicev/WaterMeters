package com.company.watermeters

import android.app.Activity
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
import com.company.watermeters.MainActivity.Companion.waterMeters
import com.company.watermeters.model.WaterMeter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//FIXME неправильная логика в адаптере, в конструктор не передаются данные
class WaterMeterListAdapter(
    waterMeterss: MutableCollection<WaterMeter>
) : RecyclerView.Adapter<WaterMeterListAdapter.ViewHolder>(), Filterable {

    private var waterMeterList: MutableCollection<WaterMeter>

    init {
        waterMeterList = waterMeterss
    }

    private var resultList = ArrayList<WaterMeter>()
    private var noFilterResults = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)

        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                MainActivity.selectedItemRegistryNumber =
                    resultList[viewHolder.adapterPosition].registryNumber
                (parent.context as Activity).startActivityForResult(
                    Intent(parent.context as Activity,
                        ClientFormActivity::class.java), 111)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listItem = resultList[holder.adapterPosition]
        holder.registryNumberTextView?.text = listItem.registryNumber
        holder.nameTextView?.text = listItem.name
        holder.typeTextView?.text = listItem.type
        holder.producerTextView?.text = listItem.producer
        holder.dateTextView?.text = formatDate(listItem)
        holder.methodologyTextView?.text = listItem.methodology
        holder.coldTextView?.text = "Хол. " + listItem.coldWater
        holder.hotTextView?.text = "Гор. " + listItem.hotWater
    }

    fun setData() {
        //FIXME Подумать над правильной логикой
        if (noFilterResults) waterMeters.forEach { resultList.add(it.clone() as WaterMeter) }
        Log.d("wMList size", waterMeterList.size.toString())
    }

    fun updateList(newList: ArrayList<WaterMeter>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(resultList, newList))
        diffResult.dispatchUpdatesTo(this)
    }

    fun getList() = resultList

    override fun getItemCount() = resultList.size

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
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            val filteredList: MutableList<WaterMeter> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                waterMeters.forEach { filteredList.add(it.clone() as WaterMeter) }
                Log.d("con null, em, wM size: ", waterMeters.size.toString())
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
                Log.d("con not empt, fL size: ", filteredList.size.toString())
            }
            noFilterResults = filteredList.isEmpty()
            Log.d("wM noResults: ", noFilterResults.toString())
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
            Log.d("con not empt, rL size: ", resultList.size.toString())
            setData()
            notifyDataSetChanged()
        }
    }

    //FIXME Перенести метод к запросу к бд
    private fun formatDate(waterMeter: WaterMeter): String? {
        var formattedDate: String? = waterMeter.date
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