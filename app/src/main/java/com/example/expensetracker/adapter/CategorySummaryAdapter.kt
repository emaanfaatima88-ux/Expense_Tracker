package com.example.expensetracker.adapter

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.ItemCategorySummaryBinding
import com.example.expensetracker.ui.statistics.CategorySummary
import com.example.expensetracker.utils.AmountFormatter

class CategorySummaryAdapter : RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

    private val items = ArrayList<CategorySummary>()
    private var totalAmountSum: Double = 1.0

    fun setData(newItems: List<CategorySummary>) {
        items.clear()
        items.addAll(newItems.sortedByDescending { it.total })
        totalAmountSum = items.sumOf { it.total }.coerceAtLeast(1.0)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategorySummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCategorySummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySummary) {
            binding.txtCategoryName.text = item.category
            binding.txtCategoryAmount.text = "${item.currencySymbol} ${AmountFormatter.formatAmount(item.total)}"

            // Fixed double-to-int percentage formula calculation
            val pct = ((item.total / totalAmountSum) * 100).toInt()
            binding.txtCategoryPercentage.text = "$pct%"
            binding.progressCategory.progress = pct

            val dotBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(item.color)
            }
            binding.viewColorDot.background = dotBackground
            binding.progressCategory.progressTintList = ColorStateList.valueOf(item.color)
        }
    }
}