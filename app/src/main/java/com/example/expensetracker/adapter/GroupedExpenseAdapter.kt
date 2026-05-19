package com.example.expensetracker.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.ItemTransactionGroupBinding
import com.example.expensetracker.model.GroupedExpense
import com.example.expensetracker.utils.AmountFormatter
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.utils.ExpenseCategoryHelper

class GroupedExpenseAdapter(
    private val onItemClick: (ExpenseEntity) -> Unit
) : RecyclerView.Adapter<GroupedExpenseAdapter.GroupViewHolder>() {

    private var groupList =
        emptyList<GroupedExpense>()

    inner class GroupViewHolder(
        val binding: ItemTransactionGroupBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupViewHolder {

        val binding =
            ItemTransactionGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: GroupViewHolder,
        position: Int
    ) {

        val group =
            groupList[position]

        holder.binding.txtDayTitle.text =
            group.title.uppercase()

        holder.binding.layoutTransactions.removeAllViews()

        group.expenses.forEachIndexed { index, expense ->

            val view =
                LayoutInflater.from(holder.itemView.context)
                    .inflate(
                        R.layout.item_transaction_row,
                        holder.binding.layoutTransactions,
                        false
                    )

            val txtTitle =
                view.findViewById<TextView>(
                    R.id.txtTitle
                )

            val txtSubtitle =
                view.findViewById<TextView>(
                    R.id.txtSubtitle
                )

            val txtAmount =
                view.findViewById<TextView>(
                    R.id.txtAmount
                )

            val imgCategory =
                view.findViewById<ImageView>(
                    R.id.imgCategory
                )

            val iconContainer =
                view.findViewById<FrameLayout>(
                    R.id.iconContainer
                )

            val divider =
                view.findViewById<View>(
                    R.id.divider
                )

            txtTitle.text =
                expense.title

            txtSubtitle.text =
                "${expense.category} • ${expense.date}"

            val currency =
                CurrencyManager(
                    holder.itemView.context
                ).getCurrencySymbol()

            txtAmount.text =
                "- $currency ${
                    AmountFormatter.formatAmount(
                        expense.amount
                    )
                }"

            imgCategory.setImageResource(
                ExpenseCategoryHelper.getCategoryIcon(
                    expense.category
                )
            )

            val bgColor =
                ExpenseCategoryHelper.getCategoryColor(
                    expense.category
                )

            iconContainer.backgroundTintList =
                ColorStateList.valueOf(
                    Color.parseColor(bgColor)
                )

            divider.visibility =
                if (
                    index == group.expenses.lastIndex
                ) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            view.setOnClickListener {

                onItemClick(expense)
            }

            holder.binding.layoutTransactions
                .addView(view)
        }
    }

    override fun getItemCount(): Int {

        return groupList.size
    }

    fun setData(
        data: List<GroupedExpense>
    ) {

        groupList = data

        notifyDataSetChanged()
    }
}