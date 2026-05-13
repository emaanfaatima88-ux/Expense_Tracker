package com.example.expensetracker.domain.usecase

import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class InsertExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {

    suspend operator fun invoke(expense: ExpenseEntity) {
        repository.insertExpense(expense)
    }
}