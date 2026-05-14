package com.example.expensetracker.ui.statistics

data class CategorySummary(

    val category: String,

    val total: Double,

    val color: Int,

    val currencySymbol: String
)