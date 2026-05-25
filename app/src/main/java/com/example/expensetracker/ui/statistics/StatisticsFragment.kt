package com.example.expensetracker.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.expensetracker.R
import com.example.expensetracker.adapter.CategorySummaryAdapter
import com.example.expensetracker.adapter.MonthSummaryAdapter
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.FragmentStatisticsBinding
import com.example.expensetracker.utils.AmountFormatter
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.utils.ExpenseCategoryHelper
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val expenseViewModel: ExpenseViewModel by viewModels()

    private lateinit var categoryAdapter: CategorySummaryAdapter
    private lateinit var monthAdapter: MonthSummaryAdapter // Keep a global reference

    private var globalExpensesList: List<ExpenseEntity> = emptyList()
    private val dbDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        setupPieChart()
        setupCategoryRecyclerView()
        setupMonthOverviewRecyclerView() // Initialize Layout Managers ONCE with new layout fixes
        setupFilterToggles()
        observeExpenses()

        return binding.root
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.parseColor("#FFFCF6"))
            setTransparentCircleAlpha(0)
            holeRadius = 78f
            setDrawEntryLabels(false)
            centerText = "CATS\n0"
            setCenterTextSize(14f)
            setCenterTextColor(Color.parseColor("#1A1612"))
            animateY(1000, Easing.EaseInOutQuad)
            rotationAngle = -90f
            isRotationEnabled = false
            legend.isEnabled = false
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategorySummaryAdapter()
        binding.recyclerCategorySummary.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    // 🛠️ PERFORMANCE & SCROLL FIX: Initialized once with explicit padding boundaries and variable tracking
    private fun setupMonthOverviewRecyclerView() {
        monthAdapter = MonthSummaryAdapter()

        binding.recyclerMonthOverview.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = monthAdapter
            setHasFixedSize(false) // Restores dynamic layout boundary tracking for scrolling fluidity
            itemAnimator = null   // Prevents data refresh layout flicker animations

            // Provides visual margins when reaching list end boundaries
            setPadding(16, 0, 16, 0)
            clipToPadding = false
        }

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerMonthOverview)
    }

    private fun setupFilterToggles() {
        binding.btnToggleList.setOnClickListener {
            binding.btnToggleList.setBackgroundResource(R.drawable.bg_segmented_active)
            binding.btnToggleList.setTextColor(Color.parseColor("#FFFFFF"))
            binding.btnToggleDial.setBackgroundResource(R.drawable.bg_segmented_inactive)
            binding.btnToggleDial.setTextColor(Color.parseColor("#7A6F62"))
        }

        binding.btnToggleDial.setOnClickListener {
            binding.btnToggleDial.setBackgroundResource(R.drawable.bg_segmented_active)
            binding.btnToggleDial.setTextColor(Color.parseColor("#FFFFFF"))
            binding.btnToggleList.setBackgroundResource(R.drawable.bg_segmented_inactive)
            binding.btnToggleList.setTextColor(Color.parseColor("#7A6F62"))
        }
    }

    private fun observeExpenses() {
        val currencyManager = CurrencyManager(requireContext())
        val currencySymbol = currencyManager.getCurrencySymbol()

        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            if (expenses == null || expenses.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.statisticsContent.visibility = View.GONE
                return@observe
            }

            binding.layoutEmptyState.visibility = View.GONE
            binding.statisticsContent.visibility = View.VISIBLE

            globalExpensesList = expenses

            val totalSpent = expenses.sumOf { it.amount }
            binding.txtTotalSpent.text = "$currencySymbol ${AmountFormatter.formatAmount(totalSpent)}"
            binding.txtTransactionCount.text = "${expenses.size} transactions"

            // 🛠️ FIX: Clean and normalize strings during grouping to catch variants like "food", "Others", or "Other"
            val categoryMap = expenses.groupBy { expense ->
                when (val rawCategory = expense.category.trim().lowercase()) {
                    "food", "food & drink" -> "Food"
                    "other", "others" -> "Other"
                    else -> expense.category.trim().replaceFirstChar { it.uppercase() }
                }
            }

            binding.pieChart.centerText = "CATS\n${categoryMap.size}"

            val entries = ArrayList<PieEntry>()
            val summaryList = ArrayList<CategorySummary>()

            categoryMap.entries.forEach { entry ->
                val categoryName = entry.key
                val totalSpentInCategory = entry.value.sumOf { it.amount }

                // Now it's guaranteed to be exactly "Food" or "Other", matching ExpenseCategoryHelper perfectly!
                val colorHex = ExpenseCategoryHelper.getStatisticsColor(categoryName)
                val colorParsed = Color.parseColor(colorHex)

                entries.add(PieEntry(totalSpentInCategory.toFloat(), categoryName))
                summaryList.add(CategorySummary(categoryName, totalSpentInCategory, colorParsed, currencySymbol))
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = summaryList.map { it.color }
                setDrawValues(false)
                sliceSpace = 3f
                selectionShift = 0f
            }

            binding.pieChart.data = PieData(dataSet)
            binding.pieChart.invalidate()
            categoryAdapter.setData(summaryList)

            populateRealBarChart(expenses)
            calculateAndAnimateMonthOverMonth(expenses)
        }
    }

    private fun populateRealBarChart(expenses: List<ExpenseEntity>) {
        binding.barChartContainer.removeAllViews()
        val density = resources.displayMetrics.density
        val maxGraphHeightDp = 100

        val dailyTotals = DoubleArray(14)
        val compareFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        var startDateLabel = ""
        var endDateLabel = ""

        for (i in 13 downTo 0) {
            val targetCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dayKey = compareFormat.format(targetCal.time)

            if (i == 13) startDateLabel = displayDateFormat.format(targetCal.time)
            if (i == 0) endDateLabel = displayDateFormat.format(targetCal.time)

            val daySum = expenses.filter {
                val expDate = parseDatabaseDate(it.date)
                if (expDate != null) compareFormat.format(expDate) == dayKey else false
            }.sumOf { it.amount }

            dailyTotals[13 - i] = daySum
        }

        binding.txtBarChartStartDate.text = startDateLabel
        binding.txtBarChartEndDate.text = endDateLabel

        val peakAmount = dailyTotals.maxOrNull() ?: 0.0
        val currencyManager = CurrencyManager(requireContext())
        binding.txtBarChartPeak.text = "peak ${currencyManager.getCurrencySymbol()} ${AmountFormatter.formatAmount(peakAmount)}"

        val highestValue = dailyTotals.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

        dailyTotals.forEachIndexed { idx, dayAmount ->
            val calculatedHeightDp = ((dayAmount / highestValue) * maxGraphHeightDp).coerceAtLeast(4.0).toInt()

            val barView = View(requireContext()).apply {
                val heightPx = (calculatedHeightDp * density).toInt()
                layoutParams = LinearLayout.LayoutParams(0, heightPx, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }

                if (idx == dailyTotals.lastIndex) {
                    setBackgroundColor(Color.parseColor("#D47216"))
                } else {
                    setBackgroundColor(Color.parseColor("#EAE5DC"))
                }

                scaleY = 0f
                pivotY = heightPx.toFloat()
            }
            binding.barChartContainer.addView(barView)

            barView.animate()
                .scaleY(1f)
                .setDuration(750)
                .setStartDelay(idx * 30L)
                .setInterpolator(android.view.animation.OvershootInterpolator(1.1f))
                .start()
        }
    }

    private fun calculateAndAnimateMonthOverMonth(expenses: List<ExpenseEntity>) {
        val totalMonthsToDisplay = 12
        val monthDataList = ArrayList<Pair<String, Double>>()

        // HIGH PERFORMANCE PARSING ENGINE: Map target data into memory once to prevent layout stutter
        val parsedExpensesWithDates = expenses.mapNotNull { expense ->
            parseDatabaseDate(expense.date)?.let { parsedDate ->
                val tempCal = Calendar.getInstance().apply { time = parsedDate }
                val monthYearKey = "${tempCal.get(Calendar.YEAR)}_${tempCal.get(Calendar.MONTH)}"
                monthYearKey to expense.amount
            }
        }.groupBy({ it.first }, { it.second })

        // Build the lookup array chronologically
        for (i in (totalMonthsToDisplay - 1) downTo 0) {
            val targetMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val targetMonth = targetMonthCal.get(Calendar.MONTH)
            val targetYear = targetMonthCal.get(Calendar.YEAR)
            val monthLabel = targetMonthCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""

            val lookupKey = "${targetYear}_${targetMonth}"
            val monthSum = parsedExpensesWithDates[lookupKey]?.sum() ?: 0.0

            monthDataList.add(Pair(monthLabel, monthSum))
        }

        val maxMonthValue = monthDataList.maxOf { it.second }.coerceAtLeast(1.0)

        // Only look up total items before data insertion to determine if it's the initial baseline synchronicity load
        val checkScroll = monthAdapter.itemCount == 0
        monthAdapter.updateData(monthDataList, maxMonthValue)

        // Only scroll to position during initial data load to prevent snapping back while the user is actively swiping
        if (checkScroll && monthDataList.isNotEmpty()) {
            binding.recyclerMonthOverview.scrollToPosition(monthDataList.lastIndex)
        }
    }

    private fun parseDatabaseDate(dateStr: String): Date? {
        return try {
            dbDateFormat.parse(dateStr.trim())
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}