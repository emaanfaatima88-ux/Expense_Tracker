package com.example.expensetracker.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinancialTipsProvider(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("financial_tips_prefs", Context.MODE_PRIVATE)

    private val tips = listOf(
        "Track every small expense. Coffee and snacks add up quicker than you think!",
        "Try the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings.",
        "Review your 'Recent Expenses' at the end of every week to see where you can cut back.",
        "Set a realistic monthly budget and stick to it. Consistency is key!",
        "Before making an impulse purchase, wait 24 hours to see if you still really need it.",
        "Categorize your expenses accurately to get a clear picture of your spending habits.",
        "Small savings daily lead to big balances monthly. Start small!",
        "Always prioritize your fixed expenses like bills and rent before spending on wants.",
        "Check your remaining budget balance before planning a weekend hangout.",
        "Try to cook at home more often. Food delivery apps eat up a huge chunk of your budget!",
        "Emergency funds keep you secure. Try to save a small portion of your allowance or income.",
        "Distinguish between your 'Needs' and 'Wants' before hitting that buy button.",
        "An unmonitored budget is just a wish list. Keep tracking daily!",
        "Look at your Statistics fragment weekly to catch unexpected spending spikes early.",
        "Be mindful of subscription services. Cancel the ones you haven't used this month."
    )

    // Helper to get today's date identifier string (e.g., "2026-05-23")
    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /**
     * Gets today's tip message string natively. Rotates index only if it's a completely new calendar day.
     */
    fun getDailyTip(): String {
        val lastSavedDate = sharedPreferences.getString("last_tip_date", "")
        var currentTipIndex = sharedPreferences.getInt("current_tip_index", 0)

        if (lastSavedDate != todayDateString) {
            // It's a brand new day! Increment tip index and reset dialog visibility flag
            currentTipIndex = (currentTipIndex + 1) % tips.size
            sharedPreferences.edit()
                .putInt("current_tip_index", currentTipIndex)
                .putString("last_tip_date", todayDateString)
                .putBoolean("dialog_shown_today", false) // Reset for the new day
                .apply()
        }

        return tips[currentTipIndex]
    }

    /**
     * Checks if the dialog was already popped today.
     */
    fun shouldShowDialogToday(): Boolean {
        // Run verification to ensure date index is correct before checking boolean flags
        getDailyTip()
        return !sharedPreferences.getBoolean("dialog_shown_today", false)
    }

    /**
     * Call this when user clicks "Got it" to dismiss the startup dialog box.
     */
    fun markDialogAsShownToday() {
        sharedPreferences.edit().putBoolean("dialog_shown_today", true).apply()
    }
}