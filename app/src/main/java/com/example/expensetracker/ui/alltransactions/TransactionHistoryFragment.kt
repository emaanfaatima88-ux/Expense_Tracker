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
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.MainActivity
import com.example.expensetracker.adapter.GroupedExpenseAdapter
import com.example.expensetracker.model.GroupedExpense
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.FragmentTransactionHistoryBinding
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import com.example.expensetracker.viewmodel.ExpenseViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeExpenses()
        setupFilter()
        setupSearch()

        // ✅ ADDED: Smooth Scroll Animation Hook pointing directly to recyclerViewAllTransactions
        binding.recyclerViewAllTransactions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 10) {
                    (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = false)
                } else if (dy < -10) {
                    (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = true)
                }
            }
        })
    }

    private fun setupRecyclerView() {
        expenseAdapter = GroupedExpenseAdapter(
            onItemClick = { expense ->
                if (parentFragmentManager.findFragmentByTag("UpdateExpense") == null) {
                    val bottomSheet = AddExpenseBottomSheet(expense)
                    bottomSheet.show(parentFragmentManager, "UpdateExpense")
                }
            }
        )

        binding.recyclerViewAllTransactions.apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            itemAnimator = null
        }
    }

    private fun observeExpenses() {
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            allExpensesList = expenses
            val groupedExpenses = groupExpensesByDate(expenses)
            expenseAdapter.setData(groupedExpenses)
            updateTotal(expenses)
            updateEmptyState(expenses)
        }
    }

    private fun groupExpensesByDate(expenses: List<ExpenseEntity>): List<GroupedExpense> {
        if (expenses.isEmpty()) return emptyList()

        val groupedMap = expenses.groupBy { formatDayTitle(it.date) }

        val groupedList = groupedMap.map { entry ->
            GroupedExpense(
                title = entry.key,
                expenses = entry.value.sortedByDescending { getDatePriority(it.date) }
            )
        }

        return groupedList.sortedByDescending { groupedGroup ->
            groupedGroup.expenses.maxOfOrNull { getDatePriority(it.date) } ?: 0L
        }
    }

    private fun getDatePriority(date: String): Long {
        return when (date.lowercase()) {
            "today" -> System.currentTimeMillis()
            "yesterday" -> System.currentTimeMillis() - 86400000L
            else -> {
                try {
                    val cleanDate = date.split(" ")[0]
                    val parts = cleanDate.split("/")
                    if (parts.size == 3) {
                        val day = parts[0].toInt()
                        val month = parts[1].toInt() - 1
                        val year = parts[2].toInt()

                        val calendar = java.util.Calendar.getInstance()
                        calendar.set(year, month, day, 0, 0, 0)
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

    private fun formatDayTitle(date: String): String {
        try {
            val today = java.util.Calendar.getInstance()
            val todayDay = today.get(java.util.Calendar.DAY_OF_YEAR)
            val todayYear = today.get(java.util.Calendar.YEAR)
            val calendar = java.util.Calendar.getInstance()

            if (date.lowercase().contains("today")) return "TODAY"
            if (date.lowercase().contains("yesterday")) return "YESTERDAY"

            val cleanDate = date.split(" ")[0]
            val parts = cleanDate.split("/")
            if (parts.size != 3) return date.uppercase()

            val day = parts[0].toIntOrNull() ?: return date.uppercase()
            val month = parts[1].toIntOrNull() ?: return date.uppercase()
            val year = parts[2].toIntOrNull() ?: return date.uppercase()

            calendar.set(year, month - 1, day)

            val expenseDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            val expenseYear = calendar.get(java.util.Calendar.YEAR)

            val difference = if (todayYear == expenseYear) {
                todayDay - expenseDay
            } else {
                -1
            }

            if (difference == 0) return "TODAY"
            if (difference == 1) return "YESTERDAY"

            if (difference in 2..7) {
                return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                    java.util.Calendar.SUNDAY -> "SUNDAY"
                    java.util.Calendar.MONDAY -> "MONDAY"
                    java.util.Calendar.TUESDAY -> "TUESDAY"
                    java.util.Calendar.WEDNESDAY -> "WEDNESDAY"
                    java.util.Calendar.THURSDAY -> "THURSDAY"
                    java.util.Calendar.FRIDAY -> "FRIDAY"
                    else -> "SATURDAY"
                }
            }

            val monthName = java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(calendar.time)
            return "$monthName $day".uppercase()

        } catch (e: Exception) {
            return date.uppercase()
        }
    }

    private fun updateTotal(expenses: List<ExpenseEntity>) {
        val total = expenses.sumOf { it.amount }
        binding.txtTotalAmount.text = "Rs. ${String.format("%,.0f", total)}"
    }

    private fun updateEmptyState(list: List<ExpenseEntity>) {
        if (list.isEmpty()) {
            binding.txtNoResult.visibility = View.VISIBLE
            binding.recyclerViewAllTransactions.visibility = View.GONE
        } else {
            binding.txtNoResult.visibility = View.GONE
            binding.recyclerViewAllTransactions.visibility = View.VISIBLE
        }
    }

    private fun setupFilter() {
        binding.btnFilter.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag("FilterBottomSheet") != null) return@setOnClickListener

            val filterSheet = FilterBottomSheet(
                currentCategory = activeFilterCategory,
                currentSortOrder = activeSortOrder,
                currentDateRange = activeDateRange
            ) { selectedCategory, sortOrder, dateRange ->

                activeFilterCategory = selectedCategory
                activeSortOrder = sortOrder
                activeDateRange = dateRange

                val cleanCategory = if (selectedCategory.contains("&")) selectedCategory.split("&")[0].trim() else selectedCategory

                var filteredList = if (cleanCategory.equals("All", ignoreCase = true)) {
                    allExpensesList
                } else {
                    allExpensesList.filter { it.category.contains(cleanCategory, ignoreCase = true) }
                }

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
                    else -> filteredList
                }

                filteredList = when (sortOrder) {
                    "oldest" -> filteredList.sortedBy { getDatePriority(it.date) }
                    "highest" -> filteredList.sortedByDescending { it.amount }
                    "lowest" -> filteredList.sortedBy { it.amount }
                    else -> filteredList.sortedByDescending { getDatePriority(it.date) }
                }

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
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val searchText = s.toString().trim()
                    val filteredList = if (searchText.isEmpty()) {
                        allExpensesList
                    } else {
                        allExpensesList.filter {
                            it.title.contains(searchText, ignoreCase = true)
                        }
                    }
                    expenseAdapter.setData(groupExpensesByDate(filteredList))
                    updateEmptyState(filteredList)
                }
                override fun afterTextChanged(s: Editable?) {}
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}