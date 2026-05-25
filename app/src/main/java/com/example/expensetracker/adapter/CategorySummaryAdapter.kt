package com.example.expensetracker.adapter

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.ItemCategorySummaryBinding
import com.example.expensetracker.ui.statistics.CategorySummary
import com.example.expensetracker.utils.AmountFormatter
import java.util.ArrayList
import kotlin.math.roundToInt

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

            // 🛠️ RECALCULATE DYNAMIC VALUES WITH A 1% SAFETYSIGNAL CEILING
            val rawPercentage = (item.total / totalAmountSum) * 100

            val displayedPercentage = if (item.total > 0 && rawPercentage < 1.0) {
                1 // Give it a visible 1% floor fallback if an expense exists
            } else {
                rawPercentage.roundToInt()
            }

            // Set your text using the corrected value
            binding.txtCategoryPercentage.text = "$displayedPercentage%"

            val dotBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(item.color)
            }
            binding.viewColorDot.background = dotBackground
            binding.progressCategory.progressTintList = ColorStateList.valueOf(item.color)

            // 🛠️ ANIMATE PROGRESS BAR UP TO THE DISPLAYED PERCENTAGE VALUE
            binding.progressCategory.progress = 0 // Clear view cache state

            ObjectAnimator.ofInt(binding.progressCategory, "progress", 0, displayedPercentage).apply {
                duration = 900
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }
}