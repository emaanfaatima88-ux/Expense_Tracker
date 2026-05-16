package com.example.expensetracker.utils

import java.text.DecimalFormat

object AmountFormatter {

    fun formatAmount(amount: Double): String {

        return when {

            amount >= 1_000_000_000 -> {
                "${formatShort(amount / 1_000_000_000)}B"
            }

            amount >= 1_000_000 -> {
                "${formatShort(amount / 1_000_000)}M"
            }


            else -> {
                DecimalFormat("#,###").format(amount)
            }
        }
    }

    private fun formatShort(value: Double): String {

        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}