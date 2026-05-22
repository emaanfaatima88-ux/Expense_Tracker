package com.example.expensetracker.utils

import com.example.expensetracker.R

object ExpenseCategoryHelper {

    fun getCategoryIcon(category: String): Int {

        return when (category.lowercase()) {

            "food" -> R.drawable.ic_food
            "shopping" -> R.drawable.ic_shopping
            "transport" -> R.drawable.ic_transport
            "health" -> R.drawable.ic_health
            "bills" -> R.drawable.ic_bills
            "education" -> R.drawable.ic_education
            "entertainment" -> R.drawable.ic_entertainment
            "coffee" -> R.drawable.ic_coffee

            else -> R.drawable.ic_other
        }
    }

    // SOFT COLORS → HOME & HISTORY

    fun getCategoryColor(category: String): String {

        return when (category.lowercase()) {

            "food" -> "#FCEBE2"
            "shopping" -> "#F5E9F4"
            "transport" -> "#EBEEF2"
            "health" -> "#E9F3E8"
            "bills" -> "#EEECE8"
            "coffee" -> "#F6EDDF"
            "entertainment" -> "#FCE8E9"
            "education" -> "#E8F2EF"
            "others" -> "#EFECE6"

            else -> "#EFECE6"
        }
    }

    // DARK PREMIUM COLORS → STATISTICS

    fun getStatisticsColor(category: String): String {

        return when (category.lowercase()) {

            "food" -> "#D47216"
            "shopping" -> "#8E44AD"
            "transport" -> "#2F6FE4"
            "health" -> "#1FAA59"
            "bills" -> "#5D6678"
            "coffee" -> "#B7791F"
            "entertainment" -> "#D63384"
            "education" -> "#149ECA"
            "others" -> "#7A6F62"

            else -> "#7A6F62"
        }
    }
}