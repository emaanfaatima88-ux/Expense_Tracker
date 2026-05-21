package com.example.expensetracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.adapter.ExpenseAdapter
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.utils.AmountFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var expenseAdapter: ExpenseAdapter

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val monthlyBudget = 120000.0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentHomeBinding.inflate(
                inflater,
                container,
                false
            )

        setupRecyclerView()
        setupSwipeToDelete()
        observeExpenses()

        setupClickListeners()

        return binding.root
    }

    private fun setupRecyclerView() {

        expenseAdapter = ExpenseAdapter(

            onItemClick = { expense ->

                val bottomSheet =
                    AddExpenseBottomSheet(expense)

                bottomSheet.show(
                    parentFragmentManager,
                    "UpdateExpense"
                )
            },
            onLongClick = { }
        )

        binding.recyclerViewExpenses.apply {

            adapter = expenseAdapter

            layoutManager =
                LinearLayoutManager(requireContext())

            setHasFixedSize(false)

            isNestedScrollingEnabled = true

            itemAnimator = null
        }
    }
    private fun setupSwipeToDelete() {

        val swipeGesture =
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {

                    val position =
                        viewHolder.bindingAdapterPosition
//Get expense at swiped position
                    if (position != RecyclerView.NO_POSITION) {

                        val currentList =
                            expenseAdapter.currentList

                        if (position < currentList.size) {

                            val expense =
                                currentList[position]

                            expenseViewModel.deleteExpense(expense)
                        }
                    }
                }
            }

        val itemTouchHelper =
            ItemTouchHelper(swipeGesture)
//Connects swipe behavior to RecyclerView
        itemTouchHelper.attachToRecyclerView(
            binding.recyclerViewExpenses
        )
    }
    private fun observeExpenses() {

        // Gets selected currency settings
        val currencyManager =
            CurrencyManager(requireContext())

        val currencySymbol =
            currencyManager.getCurrencySymbol()

        expenseViewModel.allExpenses.observe(
            viewLifecycleOwner
        ) { expenses ->

            // SHOW ONLY RECENT 10
            expenseAdapter.setData(
                expenses.take(10).toMutableList()
            )

            if (expenses.isEmpty()) {

                // show empty state if no expenses
                binding.layoutEmptyState.visibility =
                    View.VISIBLE

                // hide recycler view if no expenses
                binding.recyclerViewExpenses.visibility =
                    View.GONE
            } else {

                // hide empty state if expenses exist
                binding.layoutEmptyState.visibility =
                    View.GONE

                // show recycler view if expenses exist
                binding.recyclerViewExpenses.visibility =
                    View.VISIBLE
            }

            // Transaction Count
            binding.txtTransactionCount.text =
                expenses.size.toString()

            // Category Count
            binding.txtCategoryCount.text =
                expenses.map { it.category }
                    .distinct()
                    .size
                    .toString()

            // Last 7 Transactions Total
            val weeklyTotal =
                expenses.takeLast(7).sumOf { it.amount }

            binding.txtWeeklyStats.text =
                "$currencySymbol ${
                    AmountFormatter.formatAmount(weeklyTotal)
                }"
        }

        // Observe total expense
        expenseViewModel.totalExpense.observe(
            viewLifecycleOwner
        ) { total ->

            // Total Expense
            binding.txtTotalExpense.text =
                "$currencySymbol ${
                    AmountFormatter.formatAmount(total)
                }"

            // Daily Average
            val dailyAverage = total / 30

            binding.txtDailyAvg.text =
                "$currencySymbol ${
                    AmountFormatter.formatAmount(dailyAverage)
                }"

            // Budget Calculation
            val percentage =
                ((total / monthlyBudget) * 100).toInt()

            val safePercentage =
                percentage.coerceIn(0, 100)

            // Progress Bar
            binding.progressMonthly.setProgress(
                safePercentage,
                true
            )

            // Remaining Amount
            val remaining =
                monthlyBudget - total

            // Budget Text
            binding.txtBudgetPercent.text =
                "$safePercentage% of $currencySymbol ${
                    AmountFormatter.formatAmount(monthlyBudget)
                }"

            // Remaining Text
            binding.txtRemainingAmount.text =
                "$currencySymbol ${
                    AmountFormatter.formatAmount(remaining)
                } left"

            // Budget Mini Card
            binding.txtBudgetMini.text =
                "$currencySymbol ${
                    AmountFormatter.formatAmount(monthlyBudget)
                }"
        }
    }

    private fun setupClickListeners() {

        // SEE ALL -> switch to History tab
        binding.txtSeeAll.setOnClickListener {

            requireActivity()
                .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottomNavigationView
                )
                .selectedItemId = R.id.transactionHistoryFragment
        }

        // BUDGET -> normal navigation
        binding.cardBudget.setOnClickListener {

            findNavController().navigate(
                R.id.budgetFragment
            )
        }

        // STATISTICS -> switch to Statistics tab
        binding.cardStats.setOnClickListener {

            requireActivity()
                .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    R.id.bottomNavigationView
                )
                .selectedItemId = R.id.statisticsFragment
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}