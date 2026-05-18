package com.example.expensetracker.utils

import com.example.expensetracker.R

object ExpenseCategoryHelper {

    fun getCategoryIcon(category: String): Int {

        return when (category.lowercase()) {
            "food"      -> R.drawable.ic_food
            "shopping"  -> R.drawable.ic_shopping
            "transport" -> R.drawable.ic_transport
            "health"    -> R.drawable.ic_health
            "bills"     -> R.drawable.ic_bills
            else        -> R.drawable.ic_other
        }
    }

    fun getCategoryColor(category: String): String {

        return when (category.lowercase()) {
            "food"      -> "#fcebe2"   // warm orange tint
            "shopping"  -> "#f2e6f1"   // soft purple tint
            "transport" -> "#ebeef2"   // calm blue tint
            "health"    -> "#e9f3e8"   // fresh green tint
            "bills"     -> "#FF61F8"   // amber tint
            "others"    -> "#e5de00"   // iOS system gray
            else        -> "#8A5F71"   // iOS system gray
        }
    }
}