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
            "education"     -> R.drawable.ic_education
            "entertainment"     -> R.drawable.ic_entertainment
            "coffee"     -> R.drawable.ic_coffee
            else        -> R.drawable.ic_other
        }
    }

    fun getCategoryColor(category: String): String {

        return when (category.lowercase()) {
            "food"      -> "#FCEBE2"   // warm orange tint
            "shopping"  -> "#F5E9F4"   // soft purple tint
            "transport" -> "#EBEEF2"   // calm blue tint
            "health"    -> "#E9F3E8"   // fresh green tint
            "bills"     -> "#EEECE8"   // light grey tint
            "coffee"     -> "#F6EDDF"   // light peach tint
            "entertainment"     -> "#FCE8E9"   // light pink tint
            "education"     -> "#E8F2EF"   // light peach
            "others"    -> "#EFECE6"   // iOS system gray
            else        -> "#8A5F71"   // iOS system gray
        }
    }
}