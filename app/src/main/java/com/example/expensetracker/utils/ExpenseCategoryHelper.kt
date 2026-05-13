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
            "food"      -> "#D97A2B"   // warm orange tint
            "shopping"  -> "#E03F4F"   // soft purple tint
            "transport" -> "#0D0B61"   // calm blue tint
            "health"    -> "#5B7E3C"   // fresh green tint
            "bills"     -> "#FF61F8"   // amber tint
            "others"    -> "#e5de00"   // iOS system gray
            else        -> "#8A5F71"   // iOS system gray
        }
    }
}