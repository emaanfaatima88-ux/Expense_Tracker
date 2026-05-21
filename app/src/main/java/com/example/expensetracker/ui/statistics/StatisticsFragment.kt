package com.example.expensetracker.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.adapter.CategorySummaryAdapter
import com.example.expensetracker.databinding.FragmentStatisticsBinding
import com.example.expensetracker.utils.AmountFormatter
import com.example.expensetracker.utils.CurrencyManager
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStatisticsBinding.inflate(
            inflater,
            container,
            false
        )

        setupPieChart()

        setupRecyclerView()

        observeExpenses()

        return binding.root
    }

    private fun setupPieChart() {

        binding.pieChart.apply {

            description.isEnabled = false

            isDrawHoleEnabled = true

            setHoleColor(Color.TRANSPARENT)

            setTransparentCircleAlpha(0)

            holeRadius = 72f

            transparentCircleRadius = 76f

            setDrawEntryLabels(false)

            centerText = "CATS\n0"

            setCenterTextSize(18f)

            setCenterTextColor(
                Color.parseColor("#1A1612")
            )

            animateY(
                1400,
                Easing.EaseInOutQuad
            )

            setUsePercentValues(false)

            rotationAngle = 0f

            isRotationEnabled = false

            setExtraOffsets(
                0f,
                0f,
                0f,
                0f
            )

            legend.isEnabled = false
        }
    }

    private fun setupRecyclerView() {

        categoryAdapter = CategorySummaryAdapter()

        binding.recyclerCategorySummary.apply {

            adapter = categoryAdapter

            layoutManager =
                LinearLayoutManager(requireContext())

            isNestedScrollingEnabled = false
        }
    }

    private fun observeExpenses() {

        val currencyManager =
            CurrencyManager(requireContext())

        val currencySymbol =
            currencyManager.getCurrencySymbol()

        expenseViewModel.allExpenses.observe(
            viewLifecycleOwner
        ) { expenses ->

            if (expenses.isEmpty()) {

                binding.layoutEmptyState.visibility =
                    View.VISIBLE

                binding.statisticsContent.visibility =
                    View.GONE

                return@observe
            }

            binding.layoutEmptyState.visibility =
                View.GONE

            binding.statisticsContent.visibility =
                View.VISIBLE

            // TOTAL SPENDING
            val totalSpent =
                expenses.sumOf { it.amount }

            binding.txtTotalSpent.text =
                "$currencySymbol ${
                    AmountFormatter.formatAmount(totalSpent)
                }"

            // TRANSACTION COUNT
            binding.txtTransactionCount.text =
                "${expenses.size} transactions"

            // GROUP BY CATEGORY
            val categoryMap =
                expenses.groupBy { it.category }

            // CENTER TEXT
            binding.pieChart.centerText =
                "CATS\n${categoryMap.size}"

            val entries =
                ArrayList<PieEntry>()

            val summaryList =
                ArrayList<CategorySummary>()

            // PREMIUM COLORS
            val predefinedColors = listOf(

                "#4E5D78",
                "#E4572E",
                "#8E3FE8",
                "#2E6BE6",
                "#1FAA59",
                "#E12D8A",
                "#149ECA",
                "#B7791F"
            )

            categoryMap.entries.forEachIndexed { index, entry ->

                val category = entry.key

                val expenseList = entry.value

                val total =
                    expenseList.sumOf { expense ->
                        expense.amount
                    }

                val color =
                    Color.parseColor(
                        predefinedColors[
                            index % predefinedColors.size
                        ]
                    )

                // PIE ENTRY
                entries.add(
                    PieEntry(
                        total.toFloat(),
                        category
                    )
                )

                // SUMMARY ITEM
                summaryList.add(
                    CategorySummary(
                        category,
                        total,
                        color,
                        currencySymbol
                    )
                )
            }

            // DATASET
            val dataSet =
                PieDataSet(entries, "")

            dataSet.colors =
                summaryList.map { it.color }

            dataSet.setDrawValues(false)

            dataSet.sliceSpace = 2f

            dataSet.selectionShift = 6f

            // PIE DATA
            val pieData =
                PieData(dataSet)

            binding.pieChart.data =
                pieData

            binding.pieChart.invalidate()

            // RECYCLER DATA
            categoryAdapter.setData(summaryList)
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}