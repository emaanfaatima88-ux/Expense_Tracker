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
import com.example.expensetracker.R
import com.example.expensetracker.adapter.CategorySummaryAdapter
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

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private lateinit var categoryAdapter: CategorySummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        setupPieChart()
        setupRecyclerView()
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
            centerText = "CATS\n8"
            setCenterTextSize(14f)
            setCenterTextColor(Color.parseColor("#1A1612"))

            animateY(1000, Easing.EaseInOutQuad)
            rotationAngle = -90f
            isRotationEnabled = false
            legend.isEnabled = false
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategorySummaryAdapter()
        binding.recyclerCategorySummary.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
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
            if (expenses.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.statisticsContent.visibility = View.GONE
                return@observe
            }

            binding.layoutEmptyState.visibility = View.GONE
            binding.statisticsContent.visibility = View.VISIBLE

            val totalSpent = expenses.sumOf { it.amount }
            binding.txtTotalSpent.text = "$currencySymbol ${AmountFormatter.formatAmount(totalSpent)}"
            binding.txtTransactionCount.text = "${expenses.size} transactions"

            val categoryMap = expenses.groupBy { it.category }
            binding.pieChart.centerText = "CATS\n${categoryMap.size}"

            val entries = ArrayList<PieEntry>()
            val summaryList = ArrayList<CategorySummary>()

            // Loop through categories and match colors dynamically from ExpenseCategoryHelper
            categoryMap.entries.forEach { entry ->
                val categoryName = entry.key
                val totalSpentInCategory = entry.value.sumOf { it.amount }

                // Get the unified hex color string from your helper class
                val colorHex =
                    ExpenseCategoryHelper.getStatisticsColor(categoryName)
                val colorParsed = Color.parseColor(colorHex)

                entries.add(PieEntry(totalSpentInCategory.toFloat(), categoryName))

                // Pass the unified, persistent color property down to the adapter list data
                summaryList.add(CategorySummary(categoryName, totalSpentInCategory, colorParsed, currencySymbol))
            }

            val dataSet = PieDataSet(entries, "").apply {
                colors = summaryList.map { it.color } // Pie slices now completely sync with summary list colors!
                setDrawValues(false)
                sliceSpace = 3f
                selectionShift = 0f
            }

            binding.pieChart.data = PieData(dataSet)
            binding.pieChart.invalidate()

            categoryAdapter.setData(summaryList)

            populateBarChart()
        }
    }

    private fun populateBarChart() {
        binding.barChartContainer.removeAllViews()

        val sampleHeightsDp = listOf(25, 10, 15, 55, 25, 8, 12, 22, 35, 18, 10, 85, 60, 30)
        val density = resources.displayMetrics.density

        sampleHeightsDp.forEachIndexed { idx, heightDp ->
            val barView = View(requireContext()).apply {
                val heightPx = (heightDp * density).toInt()

                layoutParams = LinearLayout.LayoutParams(0, heightPx, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }

                if (idx == sampleHeightsDp.lastIndex) {
                    setBackgroundColor(Color.parseColor("#D47216"))
                } else {
                    setBackgroundColor(Color.parseColor("#EAE5DC"))
                }
            }
            binding.barChartContainer.addView(barView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}