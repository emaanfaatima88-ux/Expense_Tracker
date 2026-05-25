package com.example.expensetracker.utils

import androidx.annotation.DrawableRes
import com.example.expensetracker.R

object ExpenseCategoryHelper {

    data class CategoryStyle(
        @DrawableRes val icon: Int,
        val homeColor: String,
        val statisticsColor: String
    )

    private val categoryStyles = mapOf(

        // FOOD
        "food" to CategoryStyle(
            R.drawable.ic_food,
            "#FCEBE2",
            "#D47216"
        ),

        "food & drink" to CategoryStyle(
            R.drawable.ic_food,
            "#FCEBE2",
            "#D47216"
        ),

        // SHOPPING
        "shopping" to CategoryStyle(
            R.drawable.ic_shopping,
            "#F5E9F4",
            "#8E44AD"
        ),

        // TRANSPORT
        "transport" to CategoryStyle(
            R.drawable.ic_transport,
            "#EBEEF2",
            "#2F6FE4"
        ),

        // HEALTH
        "health" to CategoryStyle(
            R.drawable.ic_health,
            "#E9F3E8",
            "#1FAA59"
        ),

        // BILLS
        "bills" to CategoryStyle(
            R.drawable.ic_bills,
            "#EEECE8",
            "#5D6678"
        ),

        // EDUCATION
        "education" to CategoryStyle(
            R.drawable.ic_education,
            "#E8F2EF",
            "#149ECA"
        ),

        // ENTERTAINMENT
        "entertainment" to CategoryStyle(
            R.drawable.ic_entertainment,
            "#FCE8E9",
            "#D63384"
        ),

        // COFFEE
        "coffee" to CategoryStyle(
            R.drawable.ic_coffee,
            "#F6EDDF",
            "#B7791F"
        ),

        // OTHERS
        "other" to CategoryStyle(
            R.drawable.ic_other,
            "#EFECE6",
            "#7A6F62"
        ),

        "others" to CategoryStyle(
            R.drawable.ic_other,
            "#EFECE6",
            "#7A6F62"
        )
    )

    private fun normalize(category: String): String {

        return category
            .trim()
            .lowercase()
    }

    fun getCategoryIcon(category: String): Int {

        val normalized = normalize(category)

        return categoryStyles[normalized]?.icon
            ?: R.drawable.ic_other
    }

    fun getCategoryColor(category: String): String {

        val normalized = normalize(category)

        return categoryStyles[normalized]?.homeColor
            ?: "#EFECE6"
    }

    fun getStatisticsColor(category: String): String {

        val normalized = normalize(category)

        return categoryStyles[normalized]?.statisticsColor
            ?: "#7A6F62"
    }
}