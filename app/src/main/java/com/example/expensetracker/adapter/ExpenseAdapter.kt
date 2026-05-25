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

        // 1. Clean up category text capitalization format rules
        val displayCategory = currentExpense.category.replaceFirstChar { it.uppercase() }

        // 2. Safely split the date and time strings apart
        val fullDateString = currentExpense.date // Expects "dd/MM/yyyy hh:mm a"
        var datePart = fullDateString
        var timePart = ""

        if (fullDateString.contains(" ")) {
            // Splits text dynamically at the first space barrier separation index
            val splitIndex = fullDateString.indexOf(" ")
            datePart = fullDateString.substring(0, splitIndex).trim()
            timePart = fullDateString.substring(splitIndex).trim()
        }

        // 3. Assign text values cleanly into your modified layout IDs
        holder.binding.txtTitle.text = currentExpense.title
        holder.binding.txtCategory.text = "$displayCategory  ·  $datePart"
        holder.binding.txtTime.text = timePart // Binds "10:32 am" explicitly onto row line 3

        // 4. Bind the amount centered vertically
        holder.binding.txtAmount.text =
            "- $currencySymbol ${AmountFormatter.formatAmount(currentExpense.amount)}"

        // 5. Handle category icon and background tints
        holder.binding.imgCategory.setImageResource(
            ExpenseCategoryHelper.getCategoryIcon(currentExpense.category)
        )

        val bgHex = ExpenseCategoryHelper.getCategoryColor(currentExpense.category)
        val parentCard = holder.binding.imgCategory.parent as ViewGroup
        parentCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor(bgHex))

        // ✅ Hide divider on last item
        holder.binding.divider.visibility = if (position == expenseList.size - 1) {
            View.GONE
        } else {
            View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            onItemClick(currentExpense)
        }

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