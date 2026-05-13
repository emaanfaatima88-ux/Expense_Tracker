package com.example.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.example.expensetracker.domain.usecase.GetAllExpensesUseCase
import com.example.expensetracker.domain.usecase.InsertExpenseUseCase
import com.example.expensetracker.domain.usecase.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(

    private val insertExpenseUseCase: InsertExpenseUseCase,

    private val deleteExpenseUseCase: DeleteExpenseUseCase,

    private val getAllExpensesUseCase: GetAllExpensesUseCase,

    private val updateExpenseUseCase: UpdateExpenseUseCase

) : ViewModel() {

    val allExpenses: LiveData<List<ExpenseEntity>> =
        getAllExpensesUseCase().asLiveData()

    val totalExpense: LiveData<Double> =
        allExpenses.map { expenseList ->

            expenseList.sumOf { it.amount }
        }

    fun insertExpense(expense: ExpenseEntity) {

        viewModelScope.launch {

            insertExpenseUseCase(expense)
        }
    }
    fun updateExpense(expense: ExpenseEntity) {

        viewModelScope.launch {

            updateExpenseUseCase(expense)
        }
    }
    fun deleteExpense(expense: ExpenseEntity) {

        viewModelScope.launch {

            deleteExpenseUseCase(expense)
        }
    }
}