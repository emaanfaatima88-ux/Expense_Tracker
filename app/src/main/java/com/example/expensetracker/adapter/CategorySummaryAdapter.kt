package com.example.expensetracker.adapter

import android.content.res.ColorStateList
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.expensetracker.databinding.ItemCategorySummaryBinding
import com.example.expensetracker.ui.statistics.CategorySummary

class CategorySummaryAdapter :
    RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

    private var summaryList = emptyList<CategorySummary>()

    inner class ViewHolder(
        val binding: ItemCategorySummaryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemCategorySummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = summaryList[position]

        holder.binding.txtCategory.text =
            item.category

        holder.binding.txtAmount.text =
            "Rs. %, .0f".format(item.total)

        holder.binding.viewColor.backgroundTintList =
            ColorStateList.valueOf(item.color)
    }

    override fun getItemCount() =
        summaryList.size

    fun setData(list: List<CategorySummary>) {

        summaryList = list

        notifyDataSetChanged()
    }
}