package com.example.expensetracker.ui.alltransactions

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.adapter.GroupedExpenseAdapter
import com.example.expensetracker.model.GroupedExpense
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.FragmentTransactionHistoryBinding
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionHistoryFragment : Fragment() {

    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseAdapter: GroupedExpenseAdapter

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private var activeFilterCategory = "All"
    private var activeSortOrder = "newest"
    private var activeDateRange = "all"
    private var allExpensesList = emptyList<ExpenseEntity>()

    private val categoryList = listOf(
        "All",
        "Food",
        "Bills",
        "Shopping",
        "Transport",
        "Health",
        "Education",
        "Entertainment",
        "Coffee",
        "Others"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTransactionHistoryBinding.inflate(
            inflater,
            container,
            false
        )

        setupRecyclerView()

        observeExpenses()

        setupFilter()

        setupSearch()

        return binding.root
    }

    private fun setupRecyclerView() {

        expenseAdapter = GroupedExpenseAdapter(

            onItemClick = { expense ->

                val bottomSheet =
                    AddExpenseBottomSheet(expense)

                bottomSheet.show(
                    parentFragmentManager,
                    "UpdateExpense"
                )
            }
        )

        binding.recyclerViewAllTransactions.apply {

            adapter = expenseAdapter

            layoutManager =
                LinearLayoutManager(requireContext())

            setHasFixedSize(false)

            itemAnimator = null
        }
    }



    private fun observeExpenses() {

        expenseViewModel.allExpenses.observe(
            viewLifecycleOwner
        ) { expenses ->

            allExpensesList = expenses

            val groupedExpenses =
                groupExpensesByDate(expenses)

            expenseAdapter.setData(groupedExpenses)

            updateTotal(expenses)

            updateEmptyState(expenses)
        }
    }
    private fun groupExpensesByDate(
        expenses: List<ExpenseEntity>
    ): List<GroupedExpense> {

        val sortedExpenses =
            expenses.sortedByDescending {

                getDatePriority(it.date)
            }

        val groupedMap =
            sortedExpenses.groupBy {

                formatDayTitle(it.date)
            }

        return groupedMap.map {

            GroupedExpense(
                title = it.key,
                expenses = it.value
            )
        }
    }
    private fun getDatePriority(
        date: String
    ): Long {

        return when (date.lowercase()) {

            "today" ->
                System.currentTimeMillis()

            "yesterday" ->
                System.currentTimeMillis() - 86400000L

            else -> {

                try {

                    val parts =
                        date.split("/")

                    if (parts.size == 3) {

                        val day =
                            parts[0].toInt()

                        val month =
                            parts[1].toInt() - 1

                        val year =
                            parts[2].toInt()

                        val calendar =
                            java.util.Calendar.getInstance()

                        calendar.set(
                            year,
                            month,
                            day
                        )

                        calendar.timeInMillis

                    } else {

                        0L
                    }

                } catch (e: Exception) {

                    0L
                }
            }
        }
    }
    private fun formatDayTitle(
        date: String
    ): String {

        try {

            val today =
                java.util.Calendar.getInstance()

            val todayDay =
                today.get(java.util.Calendar.DAY_OF_YEAR)

            val todayYear =
                today.get(java.util.Calendar.YEAR)

            val calendar =
                java.util.Calendar.getInstance()

            if (date.lowercase() == "today") {

                return "TODAY"
            }

            if (date.lowercase() == "yesterday") {

                return "YESTERDAY"
            }

            val parts =
                date.split("/")

            if (parts.size != 3) {

                return date.uppercase()
            }

            val day =
                parts[0].toIntOrNull()
                    ?: return date.uppercase()

            val month =
                parts[1].toIntOrNull()
                    ?: return date.uppercase()

            val year =
                parts[2].toIntOrNull()
                    ?: return date.uppercase()

            calendar.set(
                year,
                month - 1,
                day
            )

            val expenseDay =
                calendar.get(java.util.Calendar.DAY_OF_YEAR)

            val expenseYear =
                calendar.get(java.util.Calendar.YEAR)

            val difference =
                todayDay - expenseDay

            /*
                TODAY
             */

            if (
                difference == 0 &&
                todayYear == expenseYear
            ) {

                return "TODAY"
            }

            /*
                YESTERDAY
             */

            if (
                difference == 1 &&
                todayYear == expenseYear
            ) {

                return "YESTERDAY"
            }

            /*
                LAST 7 DAYS
             */

            if (
                difference in 2..7 &&
                todayYear == expenseYear
            ) {

                return when (
                    calendar.get(
                        java.util.Calendar.DAY_OF_WEEK
                    )
                ) {

                    java.util.Calendar.SUNDAY ->
                        "SUNDAY"

                    java.util.Calendar.MONDAY ->
                        "MONDAY"

                    java.util.Calendar.TUESDAY ->
                        "TUESDAY"

                    java.util.Calendar.WEDNESDAY ->
                        "WEDNESDAY"

                    java.util.Calendar.THURSDAY ->
                        "THURSDAY"

                    java.util.Calendar.FRIDAY ->
                        "FRIDAY"

                    else ->
                        "SATURDAY"
                }
            }

            /*
                OLDER DATES
             */

            val monthName =
                java.text.SimpleDateFormat(
                    "MMMM",
                    java.util.Locale.getDefault()
                ).format(calendar.time)

            return "$monthName $day".uppercase()

        } catch (e: Exception) {

            return date.uppercase()
        }
    }
    private fun updateTotal(
        expenses: List<ExpenseEntity>
    ) {

        val total =
            expenses.sumOf { it.amount }

        binding.txtTotalAmount.text =
            "Rs. ${
                String.format(
                    "%,.0f",
                    total
                )
            }"
    }

    private fun updateEmptyState(
        list: List<ExpenseEntity>
    ) {

        if (list.isEmpty()) {

            binding.txtNoResult.visibility =
                View.VISIBLE

            binding.recyclerViewAllTransactions.visibility =
                View.GONE

        } else {

            binding.txtNoResult.visibility =
                View.GONE

            binding.recyclerViewAllTransactions.visibility =
                View.VISIBLE
        }
    }


    // 2. Replace your setupFilter() implementation with this clean logical flow
    private fun setupFilter() {
        binding.btnFilter.setOnClickListener {
            val filterSheet = FilterBottomSheet(
                currentCategory = activeFilterCategory,
                currentSortOrder = activeSortOrder,
                currentDateRange = activeDateRange
            ) { selectedCategory, sortOrder, dateRange ->

                activeFilterCategory = selectedCategory
                activeSortOrder = sortOrder
                activeDateRange = dateRange

                val cleanCategory = if (selectedCategory.contains("&")) selectedCategory.split("&")[0].trim() else selectedCategory

                // A. Filter by Category
                var filteredList = if (cleanCategory.equals("All", ignoreCase = true)) {
                    allExpensesList
                } else {
                    allExpensesList.filter { it.category.contains(cleanCategory, ignoreCase = true) }
                }

                // B. Filter by Date Range
                val currentTime = System.currentTimeMillis()
                filteredList = when (dateRange) {
                    "month" -> {
                        val cal = java.util.Calendar.getInstance()
                        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        val startOfMonth = cal.timeInMillis
                        filteredList.filter { getDatePriority(it.date) >= startOfMonth }
                    }
                    "30days" -> {
                        val thirtyDaysAgo = currentTime - (30L * 24 * 60 * 60 * 1000)
                        filteredList.filter { getDatePriority(it.date) >= thirtyDaysAgo }
                    }
                    "7days" -> {
                        val sevenDaysAgo = currentTime - (7L * 24 * 60 * 60 * 1000)
                        filteredList.filter { getDatePriority(it.date) >= sevenDaysAgo }
                    }
                    else -> filteredList // All time
                }

                // C. Sort the data
                filteredList = when (sortOrder) {
                    "oldest" -> filteredList.sortedBy { getDatePriority(it.date) }
                    "highest" -> filteredList.sortedByDescending { it.amount }
                    "lowest" -> filteredList.sortedBy { it.amount }
                    else -> filteredList.sortedByDescending { getDatePriority(it.date) } // Newest
                }

                // Push updates straight onto UI Layer
                expenseAdapter.setData(groupExpensesByDate(filteredList))
                updateTotal(filteredList)
                updateEmptyState(filteredList)
            }

            filterSheet.show(parentFragmentManager, "FilterBottomSheet")
        }
    }
    private fun setupSearch() {

        binding.etSearch.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val searchText =
                        s.toString().trim()

                    val filteredList =
                        if (searchText.isEmpty()) {

                            allExpensesList

                        } else {

                            allExpensesList.filter {

                                it.title.contains(
                                    searchText,
                                    ignoreCase = true
                                )
                            }
                        }

                    expenseAdapter.setData(
                        groupExpensesByDate(filteredList)
                    )

                    updateEmptyState(filteredList)
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }
    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}