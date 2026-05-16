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
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.components.Legend
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

        _binding =
            FragmentStatisticsBinding.inflate(
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
                //Makes donut-style chart.
            isDrawHoleEnabled = true

            setHoleColor(Color.WHITE)

            setTransparentCircleAlpha(0)

            holeRadius = 60f

            transparentCircleRadius = 65f

            setDrawEntryLabels(false)

            centerText = "Expenses"

            setCenterTextSize(18f)

            setCenterTextColor(Color.BLACK)

            animateY(1000)

            setUsePercentValues(false)
            setMinAngleForSlices(5f)
            setExtraOffsets(
                16f,
                16f,
                16f,
                24f
            )
//Shows category labels/colors
            legend.apply {

                isEnabled = true

                textSize = 12f

                textColor = Color.DKGRAY
//items arranged horizontally
                orientation =
                    Legend.LegendOrientation.HORIZONTAL

                horizontalAlignment =
                    Legend.LegendHorizontalAlignment.CENTER

                verticalAlignment =
                    Legend.LegendVerticalAlignment.BOTTOM

                isWordWrapEnabled = true

                xEntrySpace = 10f

                yEntrySpace = 8f

                form = Legend.LegendForm.CIRCLE
            }
        }
    }

    private fun setupRecyclerView() {

        categoryAdapter = CategorySummaryAdapter()
        binding.recyclerCategorySummary.isNestedScrollingEnabled = false
        binding.recyclerCategorySummary.apply {

            adapter = categoryAdapter

            layoutManager =
                LinearLayoutManager(requireContext())
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

            val categoryMap =
                expenses.groupBy { it.category }

            val entries =
                ArrayList<PieEntry>()

            val summaryList =
                ArrayList<CategorySummary>()

            categoryMap.forEach { (category, expenseList) ->

                val total =
                    expenseList.sumOf { it.amount }

                val predefinedColors = listOf(

                    "#FF6384",
                    "#36A2EB",
                    "#FFCE56",
                    "#4BC0C0",
                    "#9966FF",
                    "#FF9F40",
                    "#8BC34A",
                    "#E91E63",
                    "#009688",
                    "#673AB7"
                )

                val color =
                    Color.parseColor(
                        predefinedColors[
                            summaryList.size % predefinedColors.size
                        ]
                    )

                entries.add(
                    PieEntry(
                        total.toFloat(),
                        category
                    )
                )

                summaryList.add(
                    CategorySummary(
                        category,
                        total,
                        color,
                        currencySymbol
                    )
                )
            }

            val dataSet =
                PieDataSet(entries, "")

            dataSet.colors =
                summaryList.map { it.color }

            dataSet.setDrawValues(false)

            dataSet.sliceSpace = 3f

            dataSet.selectionShift = 8f

            val pieData =
                PieData(dataSet)

            binding.pieChart.data =
                pieData

            binding.pieChart.invalidate()

            categoryAdapter.setData(summaryList)
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}