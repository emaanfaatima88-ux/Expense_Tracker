package com.example.expensetracker.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.MainActivity
import com.example.expensetracker.R
import com.example.expensetracker.adapter.CategorySummaryAdapter
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
    private val dbDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var isShowingLast7Days = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        setupPieChart()
        setupCategoryRecyclerView()
        setupFilterToggles()

        updateToggleUI()
        observeExpenses()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ ADDED: Scroll Hook tracking the screen's main NestedScrollView view variable layout layer container
        binding.statisticsContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val dy = scrollY - oldScrollY
            if (dy > 10) {
                // User scrolls down -> Slide down navigation bars and move FAB to corner bounds
                (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = false)
            } else if (dy < -10) {
                // User scrolls up -> Restore dashboard control modules layout states
                (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = true)
            }
        })
    }

    private fun setupFilterToggles() {
        binding.btnToggleList.setOnClickListener {
            isShowingLast7Days = true
            updateToggleUI()
            observeExpenses()
        }
        binding.btnToggleDial.setOnClickListener {
            isShowingLast7Days = false
            updateToggleUI()
            observeExpenses()
        }
    }

    private fun updateToggleUI() {
        if (isShowingLast7Days) {
            binding.btnToggleList.setBackgroundResource(R.drawable.bg_segmented_active)
            binding.btnToggleList.setTextColor(Color.parseColor("#FFFFFF"))
            binding.btnToggleDial.setBackgroundResource(R.drawable.bg_segmented_inactive)
            binding.btnToggleDial.setTextColor(Color.parseColor("#7A6F62"))
        } else {
            binding.btnToggleDial.setBackgroundResource(R.drawable.bg_segmented_active)
            binding.btnToggleDial.setTextColor(Color.parseColor("#FFFFFF"))
            binding.btnToggleList.setBackgroundResource(R.drawable.bg_segmented_inactive)
            binding.btnToggleList.setTextColor(Color.parseColor("#7A6F62"))
        }
    }

    private fun observeExpenses() {
        val currencyManager = CurrencyManager(requireContext())
        val currencySymbol = currencyManager.getCurrencySymbol()
        println("DEBUG: Observing expenses. isShowingLast7Days is: $isShowingLast7Days")
        expenseViewModel.allExpenses.removeObservers(viewLifecycleOwner)
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { allExpenses ->
            val filtered = if (isShowingLast7Days) {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                allExpenses.filter { parseDatabaseDate(it.date)?.after(cal.time) == true }
            } else {
                val cal = Calendar.getInstance()
                allExpenses.filter {
                    val d = parseDatabaseDate(it.date)
                    val c = Calendar.getInstance().apply { if (d != null) time = d }
                    d != null && c.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                            c.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                }
            }

            if (filtered.isEmpty()) {
                showEmptyState(currencySymbol)
            } else {
                hideEmptyState()
                updateDashboard(filtered, currencySymbol)
            }
        }
    }

    private fun updateDashboard(expenses: List<ExpenseEntity>, currencySymbol: String) {
        val totalSpent = expenses.sumOf { it.amount }
        binding.txtTotalSpent.text = "$currencySymbol ${AmountFormatter.formatAmount(totalSpent)}"
        binding.txtTransactionCount.text = "${expenses.size} transactions"

        val categoryMap = expenses.groupBy {
            when (it.category.trim().lowercase()) {
                "food", "food & drink" -> "Food"
                "other", "others" -> "Other"
                else -> it.category.trim().replaceFirstChar { c -> c.uppercase() }
            }
        }

        binding.pieChart.centerText = "CATS\n${categoryMap.size}"
        val entries = categoryMap.map { PieEntry(it.value.sumOf { e -> e.amount }.toFloat(), it.key) }
        val summaryList = categoryMap.map {
            CategorySummary(it.key, it.value.sumOf { e -> e.amount }, Color.parseColor(ExpenseCategoryHelper.getStatisticsColor(it.key)), currencySymbol)
        }

        binding.pieChart.data = PieData(PieDataSet(entries, "").apply {
            colors = summaryList.map { it.color }
            setDrawValues(false)
            sliceSpace = 3f
        })
        binding.pieChart.invalidate()
        categoryAdapter.setData(ArrayList(summaryList))

        val daysToDraw = if (isShowingLast7Days) 7 else 30
        populateRealBarChart(expenses, daysToDraw)
    }

    private fun populateRealBarChart(expenses: List<ExpenseEntity>, days: Int) {
        binding.barChartContainer.removeAllViews()
        val density = resources.displayMetrics.density
        val compareFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val totals = DoubleArray(days)
        for (i in (days - 1) downTo 0) {
            val targetCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = compareFormat.format(targetCal.time)
            totals[(days - 1) - i] = expenses.filter {
                parseDatabaseDate(it.date)?.let { d -> compareFormat.format(d) } == key
            }.sumOf { it.amount }
        }

        binding.txtBarChartStartDate.text = displayDateFormat.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(days - 1)) }.time)
        binding.txtBarChartEndDate.text = displayDateFormat.format(Calendar.getInstance().time)

        val max = totals.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
        val currencyManager = CurrencyManager(requireContext())
        binding.txtBarChartPeak.text = "peak ${currencyManager.getCurrencySymbol()} ${AmountFormatter.formatAmount(max)}"

        totals.forEachIndexed { index, dayAmount ->
            val barView = View(requireContext()).apply {
                val heightPx = (((dayAmount / max) * 100).coerceAtLeast(4.0) * density).toInt()
                layoutParams = LinearLayout.LayoutParams(0, heightPx, 1f).apply { setMargins(4, 0, 4, 0) }
                setBackgroundColor(Color.parseColor("#1A1612"))
                scaleY = 0f
                pivotY = heightPx.toFloat()
            }
            binding.barChartContainer.addView(barView)
            barView.animate().scaleY(1f).setDuration(500).setStartDelay((index * 20).toLong()).start()
        }
    }

    private fun showEmptyState(currencySymbol: String) {
        binding.pieChart.data = PieData(PieDataSet(listOf(PieEntry(1f, "")), "").apply { colors = listOf(Color.parseColor("#EEEBE6")) })
        binding.pieChart.invalidate()
        binding.txtTotalSpent.text = "$currencySymbol 0"
        binding.txtTransactionCount.text = "0 transactions"
        binding.txtEmptyPie.visibility = View.VISIBLE
        populateEmptyBarChart()
        binding.txtEmptyBar.visibility = View.VISIBLE
        categoryAdapter.setData(ArrayList())
        binding.txtEmptyCategory.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.txtEmptyPie.visibility = View.GONE
        binding.txtEmptyBar.visibility = View.GONE
        binding.txtEmptyCategory.visibility = View.GONE
    }

    private fun populateEmptyBarChart() {
        binding.barChartContainer.removeAllViews()
        val density = resources.displayMetrics.density
        for (i in 0 until 14) {
            val barView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, (8 * density).toInt(), 1f).apply { setMargins(4, 0, 4, 0) }
                setBackgroundColor(Color.parseColor("#DEDAD4"))
            }
            binding.barChartContainer.addView(barView)
        }
    }

    private fun parseDatabaseDate(dateStr: String): Date? = try { dbDateFormat.parse(dateStr.trim()) } catch (e: Exception) { null }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}