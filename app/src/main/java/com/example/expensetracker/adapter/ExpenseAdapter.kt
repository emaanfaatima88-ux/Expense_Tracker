package com.example.expensetracker.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
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

    private var expenseList =
        emptyList<ExpenseEntity>()

    val currentList: List<ExpenseEntity>
        get() = expenseList

    inner class ExpenseViewHolder(
        val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {

        val binding =
            ItemExpenseBinding.inflate(
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

        val currentExpense =
            expenseList[position]

        val currencyManager =
            CurrencyManager(holder.itemView.context)

        val currencySymbol =
            currencyManager.getCurrencySymbol()

        holder.binding.txtTitle.text =
            currentExpense.title

        holder.binding.txtCategory.text =
            "${currentExpense.category}  ·  ${currentExpense.date}"

        holder.binding.txtAmount.text =
            "- $currencySymbol ${
                AmountFormatter.formatAmount(
                    currentExpense.amount
                )
            }"

        holder.binding.imgCategory.setImageResource(
            ExpenseCategoryHelper.getCategoryIcon(
                currentExpense.category
            )
        )

        val bgHex =
            ExpenseCategoryHelper.getCategoryColor(
                currentExpense.category
            )

        val parentCard =
            holder.binding.imgCategory.parent as ViewGroup

        parentCard.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor(bgHex)
            )

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

    fun setData(
        expenses: List<ExpenseEntity>
    ) {

        expenseList = expenses.toList()

        notifyDataSetChanged()
    }
}