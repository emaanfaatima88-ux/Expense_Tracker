package com.example.expensetracker.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensetracker.data.local.entity.BudgetEntity

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(
        budgetEntity: BudgetEntity
    )
    @Query("DELETE FROM budget_table")
    suspend fun deleteBudget()
    @Query("SELECT * FROM budget_table LIMIT 1")
    fun getBudget(): LiveData<BudgetEntity?>
}