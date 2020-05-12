package com.company.watermeters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.watermeters.MainActivity.Companion.waterMeters
import com.company.watermeters.model.WaterMeter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//OPTIMIZE Не нужно передавать context в адаптер, его можно получить из View
class WaterMeterListAdapter(
//    private val context: Context,
    private var waterMeterList: ArrayList<WaterMeter>
//    private var itemView: View
) : RecyclerView.Adapter<WaterMeterListAdapter.ViewHolder>(), Filterable {

    private var resultList = ArrayList<WaterMeter>()
    private var noFilterResults = true
//    private var inflater =
//        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
//
//        var itemView = convertView
//        val viewHolder: ViewHolder?
//        if (itemView == null) {
//            itemView = inflater.inflate(R.layout.list_item, parent, false)
//            viewHolder = ViewHolder()
//            viewHolder.registryNumberTextView = itemView.findViewById(R.id.registry_number)
//            viewHolder.nameTextView = itemView.findViewById(R.id.name)
//            viewHolder.typeTextView = itemView.findViewById(R.id.type)
//            viewHolder.producerTextView = itemView.findViewById(R.id.producer)
//            viewHolder.dateTextView = itemView.findViewById(R.id.date)
//            viewHolder.methodologyTextView = itemView.findViewById(R.id.methodology)
//            viewHolder.coldTextView = itemView.findViewById(R.id.cold)
//            viewHolder.hotTextView = itemView.findViewById(R.id.hot)
//
//            itemView.tag = viewHolder
//        } else {
//            viewHolder = itemView.tag as ViewHolder?
//        }
//
//        val registryNumberTextView = viewHolder?.registryNumberTextView
//        val nameTextView = viewHolder?.nameTextView
//        val typeTextView = viewHolder?.typeTextView
//        val producerTextView = viewHolder?.producerTextView
//        val dateTextView = viewHolder?.dateTextView
//        val methodologyTextView = viewHolder?.methodologyTextView
//        val coldTextView = viewHolder?.coldTextView
//        val hotTextView = viewHolder?.hotTextView
//
//        val waterMeter = getItem(position) as WaterMeter
//
//        registryNumberTextView?.text = waterMeter.registryNumber
//        nameTextView?.text = waterMeter.name
//        typeTextView?.text = waterMeter.type
//        producerTextView?.text = waterMeter.producer
//        dateTextView?.text = formatDate(waterMeter)
//        methodologyTextView?.text = waterMeter.methodology
//        coldTextView?.text = "Хол. " + waterMeter.coldWater
//        hotTextView?.text = "Гор. " + waterMeter.hotWater
//        return itemView
//    }

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

    fun setItems() {
        if (noFilterResults) waterMeters.forEach { resultList.add(it.clone() as WaterMeter) }
        Log.d("notify, wM size: ", waterMeters.size.toString())
    }

//    fun updateData(waterMeterList: ArrayList<WaterMeter>) {
//        this.waterMeterList = waterMeterList
//        waterMeters.forEach { resultList.add(it.clone() as WaterMeter) }
//        notifyDataSetChanged()
//    }

    fun getList() = resultList
//
//    override fun getItem(position: Int): Any {
//        return resultList[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }
//
//    override fun getCount(): Int {
//        return resultList.size
//    }

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

    override fun getFilter(): Filter? {
        return exampleFilter
    }

    private val exampleFilter: Filter = object : Filter() {
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
            setItems()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        //здесь можно добавить OnClickListener
        return ViewHolder(view)
    }

    override fun getItemCount() = resultList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listItem = resultList[position]
        holder.registryNumberTextView?.text =listItem.registryNumber
        holder.nameTextView?.text =listItem.name
        holder.typeTextView?.text =listItem.type
        holder.producerTextView?.text =listItem.producer
        holder.dateTextView?.text = formatDate(listItem)
        holder.methodologyTextView?.text =listItem.methodology
        holder.coldTextView?.text = "Хол. " + listItem.coldWater
        holder.hotTextView?.text = "Гор. " + listItem.hotWater
    }
}