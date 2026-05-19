package com.example.expensetracker.model

import com.example.expensetracker.data.local.entity.ExpenseEntity

data class GroupedExpense(

    val title: String,

    val expenses: List<ExpenseEntity>
)