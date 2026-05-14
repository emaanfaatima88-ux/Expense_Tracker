package com.example.expensetracker.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.ItemCategorySummaryBinding
import com.example.expensetracker.ui.statistics.CategorySummary
import com.example.expensetracker.utils.CurrencyManager

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

        val currencyManager =
            CurrencyManager(holder.itemView.context)

        val currency =
            currencyManager.getCurrencySymbol()

        val formattedAmount =
            String.format("%,.0f", item.total)

        holder.binding.txtAmount.text =
            "$currency $formattedAmount"

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