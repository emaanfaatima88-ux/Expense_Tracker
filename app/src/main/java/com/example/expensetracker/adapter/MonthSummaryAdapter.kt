package com.example.expensetracker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import java.util.Locale

class MonthSummaryAdapter : RecyclerView.Adapter<MonthSummaryAdapter.MonthViewHolder>() {

    private var monthDataList: List<Pair<String, Double>> = emptyList()
    private var maxMonthValue: Double = 1.0

    fun updateData(newList: List<Pair<String, Double>>, newMax: Double) {
        this.monthDataList = newList
        this.maxMonthValue = newMax
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_statistics_month_bar, parent, false)

        val displayMetrics = parent.context.resources.displayMetrics

        val totalPadding = (40 * displayMetrics.density).toInt()
        val itemWidth = (displayMetrics.widthPixels - totalPadding) / 2

        view.layoutParams = ViewGroup.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bind(monthDataList[position], maxMonthValue)
    }

    override fun getItemCount(): Int = monthDataList.size

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMonthLabel = itemView.findViewById<TextView>(R.id.txtMonthLabelName)
        private val txtAmountIndicator = itemView.findViewById<TextView>(R.id.txtMonthAmountIndicator)
        private val barVisualElement = itemView.findViewById<View>(R.id.viewMonthBarVisual)

        fun bind(data: Pair<String, Double>, maxMonthValue: Double) {
            val monthName = data.first
            val monthAmount = data.second
            val density = itemView.context.resources.displayMetrics.density

            txtMonthLabel.text = monthName

            txtAmountIndicator.text = when {
                monthAmount >= 1000000 -> String.format(Locale.US, "%.1fM", monthAmount / 1000000.0)
                monthAmount >= 1000 -> "${(monthAmount / 1000).toInt()}k"
                else -> monthAmount.toInt().toString()
            }

            val maxBarHeightDp = 90
            val scaledHeightDp = ((monthAmount / maxMonthValue) * maxBarHeightDp).coerceAtLeast(4.0).toFloat()
            val calculatedHeightPx = (scaledHeightDp * density).toInt()

            barVisualElement.layoutParams.height = calculatedHeightPx
            barVisualElement.requestLayout()

            // 🛠️ COLOR FIX: Every month is explicitly rendered in solid black now
            barVisualElement.setBackgroundColor(Color.parseColor("#1A1612"))

            // Ensure the view scales from its bottom edge upward
            barVisualElement.post {
                barVisualElement.pivotY = calculatedHeightPx.toFloat() // Anchor pivot to the bottom edge
                barVisualElement.scaleY = 0f
                barVisualElement.animate()
                    .scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(OvershootInterpolator(1.0f))
                    .start()
            }
        }
    }
}