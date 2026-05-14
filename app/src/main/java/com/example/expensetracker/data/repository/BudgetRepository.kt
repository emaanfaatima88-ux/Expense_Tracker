package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.dao.BudgetDao
import com.example.expensetracker.data.local.entity.BudgetEntity
import javax.inject.Inject

class BudgetRepository @Inject constructor(

    private val budgetDao: BudgetDao
) {

    suspend fun insertBudget(
        budgetEntity: BudgetEntity
    ) {

        budgetDao.insertBudget(
            budgetEntity
        )
    }
    suspend fun deleteBudget() {

        budgetDao.deleteBudget()
    }
    fun getBudget() =
        budgetDao.getBudget()
}