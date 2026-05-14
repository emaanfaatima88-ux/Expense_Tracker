package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteAllExpensesUseCase @Inject constructor(

    private val repository: ExpenseRepository

) {

    suspend operator fun invoke() {

        repository.deleteAllExpenses()
    }
}