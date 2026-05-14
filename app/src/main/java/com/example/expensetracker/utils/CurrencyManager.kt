package com.example.expensetracker.utils

import android.content.Context

class CurrencyManager(
    context: Context
) {

    private val sharedPreferences =
        context.getSharedPreferences(
            "settings",
            Context.MODE_PRIVATE
        )

    fun saveCurrency(
        currency: String
    ) {

        sharedPreferences.edit()
            .putString(
                "currency",
                currency
            )
            .apply()
    }
    fun getCurrencySymbol(): String {

        return when (getCurrency()) {

            "Pakistani Rupee (Rs)" -> "Rs."

            "US Dollar ($)" -> "$"

            "Euro (€)" -> "€"

            "British Pound (£)" -> "£"

            else -> "Rs."
        }
    }
    fun getCurrency(): String {

        return sharedPreferences.getString(
            "currency",
            "Rs"
        ) ?: "Rs"
    }
}