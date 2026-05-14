package com.example.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.entity.BudgetEntity
import com.example.expensetracker.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(

    private val repository: BudgetRepository
) : ViewModel() {

    val budget: LiveData<BudgetEntity?> =
        repository.getBudget()
    fun deleteBudget() {

        viewModelScope.launch {

            repository.deleteBudget()
        }
    }
    fun saveBudget(
        amount: Double
    ) {

        viewModelScope.launch {

            repository.insertBudget(
                BudgetEntity(
                    monthlyBudget = amount
                )
            )
        }
    }
}