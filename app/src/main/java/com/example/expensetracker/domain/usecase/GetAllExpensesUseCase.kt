package com.example.expensetracker.domain.usecase

import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {

    operator fun invoke(): Flow<List<ExpenseEntity>> {
        return repository.getAllExpenses()
    }
}