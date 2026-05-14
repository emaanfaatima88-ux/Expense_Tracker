package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.local.dao.BudgetDao
import com.example.expensetracker.data.local.dao.ExpenseDao
import com.example.expensetracker.data.repository.ExpenseRepositoryImpl
import com.example.expensetracker.domain.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {

        @Provides
        @Singleton
        fun provideExpenseDatabase(
            @ApplicationContext context: Context
        ): ExpenseDatabase {

            return Room.databaseBuilder(
                context,
                ExpenseDatabase::class.java,
                "expense_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        @Singleton
        fun provideExpenseDao(
            database: ExpenseDatabase
        ): ExpenseDao {

            return database.expenseDao()
        }

        @Provides
        @Singleton
        fun provideBudgetDao(
            database: ExpenseDatabase
        ): BudgetDao {

            return database.budgetDao()
        }
    }

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository
}