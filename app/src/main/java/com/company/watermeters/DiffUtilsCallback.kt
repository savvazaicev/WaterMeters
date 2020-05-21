package com.company.watermeters

import androidx.recyclerview.widget.DiffUtil
import com.company.watermeters.model.WaterMeter

class DiffUtilsCallback(
    var oldList: ArrayList<WaterMeter>,
    var newList: ArrayList<WaterMeter>
) :
    DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}