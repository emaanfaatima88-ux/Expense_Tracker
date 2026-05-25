package com.example.expensetracker.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.ItemExpenseBinding
import com.example.expensetracker.utils.AmountFormatter
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.utils.ExpenseCategoryHelper

class ExpenseAdapter(
    private val onItemClick: (ExpenseEntity) -> Unit,
    private val onLongClick: (ExpenseEntity) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenseList = emptyList<ExpenseEntity>()

    val currentList: List<ExpenseEntity>
        get() = expenseList

    inner class ExpenseViewHolder(
        val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {

        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ExpenseViewHolder,
        position: Int
    ) {

        val currentExpense = expenseList[position]

        val currencyManager = CurrencyManager(holder.itemView.context)
        val currencySymbol = currencyManager.getCurrencySymbol()

        // CATEGORY FORMAT
        val displayCategory = currentExpense.category
            .trim()
            .replaceFirstChar {
                it.uppercase()
            }

        // DATE & TIME SPLIT
        val fullDateString = currentExpense.date

        var datePart = fullDateString
        var timePart = ""

        try {

            val parts = fullDateString.split(" ")

            if (parts.size >= 3) {

                datePart = parts[0]

                timePart = parts
                    .subList(1, parts.size)
                    .joinToString(" ")
            }

        } catch (_: Exception) {
        }

        // TITLE
        holder.binding.txtTitle.text =
            currentExpense.title

        // CATEGORY + DATE
        holder.binding.txtCategory.text =
            "$displayCategory  ·  $datePart"

        // TIME
        holder.binding.txtTime.text =
            timePart

        // AMOUNT
        holder.binding.txtAmount.text =
            "- $currencySymbol ${
                AmountFormatter.formatAmount(currentExpense.amount)
            }"

        // CATEGORY ICON
        holder.binding.imgCategory.setImageResource(
            ExpenseCategoryHelper.getCategoryIcon(
                currentExpense.category
            )
        )

        // CATEGORY BACKGROUND COLOR
        val bgColor =
            ExpenseCategoryHelper.getCategoryColor(
                currentExpense.category
            )

        holder.binding.iconBackground.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor(bgColor)
            )

        // DIVIDER
        holder.binding.divider.visibility =
            if (position == expenseList.lastIndex) {
                View.GONE
            } else {
                View.VISIBLE
            }

        // CLICK
        holder.itemView.setOnClickListener {
            onItemClick(currentExpense)
        }

        // LONG CLICK
        holder.itemView.setOnLongClickListener {
            onLongClick(currentExpense)
            true
        }
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    fun setData(expenses: List<ExpenseEntity>) {

        expenseList = expenses.toList()

        notifyDataSetChanged()
    }
}