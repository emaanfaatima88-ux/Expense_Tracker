package com.example.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_table")
data class BudgetEntity(

    @PrimaryKey
    val id: Int = 1,

    val monthlyBudget: Double
)