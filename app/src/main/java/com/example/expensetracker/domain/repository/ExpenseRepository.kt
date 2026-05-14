package com.example.expensetracker.domain.repository

import com.example.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    suspend fun insertExpense(
        expense: ExpenseEntity
    )

    suspend fun deleteExpense(
        expense: ExpenseEntity
    )

    suspend fun updateExpense(
        expense: ExpenseEntity
    )

    suspend fun deleteAllExpenses()

    fun getAllExpenses(): Flow<List<ExpenseEntity>>
}