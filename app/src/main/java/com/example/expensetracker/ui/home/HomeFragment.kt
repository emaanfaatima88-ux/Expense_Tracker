package com.example.expensetracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.example.expensetracker.utils.FinancialTipsProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var tipsProvider: FinancialTipsProvider

    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val monthlyBudget = 120000.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        tipsProvider = FinancialTipsProvider(requireContext())

        setupRecyclerView()
        setupSwipeToDelete()
        observeExpenses()
        setupClickListeners()
        checkAndShowFirstOpenDialog()

        return binding.root
    }

    /**
     * Pops up the dialog window box only if the user is launching the app for the first time today.
     */
    private fun checkAndShowFirstOpenDialog() {
        if (tipsProvider.shouldShowDialogToday()) {
            val dailyTipMessage = tipsProvider.getDailyTip()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("💡 First Open Tip of the Day")
                .setMessage(dailyTipMessage)
                .setCancelable(false)
                .setPositiveButton("Got it") { dialog, _ ->
                    tipsProvider.markDialogAsShownToday()
                    dialog.dismiss()
                }
                .show()
        }
    }

    /**
     * Infills and pops up the custom styled Bottom Sheet layout cleanly.
     */
    private fun showTipBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_tip_bottom_sheet, null)

        val txtTip = view.findViewById<TextView>(R.id.txtBottomSheetTip)
        val txtStat = view.findViewById<TextView>(R.id.txtNotificationStat)
        // FIXED: Correctly matching the view definition type as ImageView to prevent class casting crash
        val btnClose = view.findViewById<ImageView>(R.id.btnCloseBottomSheet)

        // Set the dynamic daily financial tip text smoothly
        txtTip.text = tipsProvider.getDailyTip()

        // Dynamic Calculations: Grabbing your highest transaction for the "Biggest expense" card
        val currencyManager = CurrencyManager(requireContext())
        val currencySymbol = currencyManager.getCurrencySymbol()

        expenseViewModel.allExpenses.value?.let { expenses ->
            if (expenses.isNotEmpty()) {
                val maxExpense = expenses.maxByOrNull { it.amount }
                if (maxExpense != null) {
                    txtStat.text = "${maxExpense.title} · $currencySymbol ${AmountFormatter.formatAmount(maxExpense.amount)}"
                }
            } else {
                txtStat.text = "No expenses recorded yet"
            }
        }

        btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            onItemClick = { expense ->
                val bottomSheet = AddExpenseBottomSheet(expense)
                bottomSheet.show(parentFragmentManager, "UpdateExpense")
            },
            onLongClick = { }
        )

        binding.recyclerViewExpenses.apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            isNestedScrollingEnabled = true
            itemAnimator = null
        }
    }

    private fun setupSwipeToDelete() {
        val swipeGesture = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentList = expenseAdapter.currentList
                    if (position < currentList.size) {
                        val expense = currentList[position]
                        expenseViewModel.deleteExpense(expense)
                    }
                }
            }
        }

        ItemTouchHelper(swipeGesture).attachToRecyclerView(binding.recyclerViewExpenses)
    }

    private fun observeExpenses() {
        val currencyManager = CurrencyManager(requireContext())
        val currencySymbol = currencyManager.getCurrencySymbol()

        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.setData(expenses.take(10).toMutableList())

            if (expenses.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewExpenses.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewExpenses.visibility = View.VISIBLE
            }

            binding.txtTransactionCount.text = expenses.size.toString()
            binding.txtCategoryCount.text = expenses.map { it.category }.distinct().size.toString()

            val weeklyTotal = expenses.takeLast(7).sumOf { it.amount }
            binding.txtWeeklyStats.text = "$currencySymbol ${AmountFormatter.formatAmount(weeklyTotal)}"
        }

        expenseViewModel.totalExpense.observe(viewLifecycleOwner) { total ->
            binding.txtTotalExpense.text = "$currencySymbol ${AmountFormatter.formatAmount(total)}"

            val dailyAverage = total / 30
            binding.txtDailyAvg.text = "$currencySymbol ${AmountFormatter.formatAmount(dailyAverage)}"

            val percentage = ((total / monthlyBudget) * 100).toInt()
            val safePercentage = percentage.coerceIn(0, 100)
            binding.progressMonthly.setProgress(safePercentage, true)

            val remaining = monthlyBudget - total
            binding.txtBudgetPercent.text = "$safePercentage% of $currencySymbol ${AmountFormatter.formatAmount(monthlyBudget)}"
            binding.txtRemainingAmount.text = "$currencySymbol ${AmountFormatter.formatAmount(remaining)} left"
            binding.txtBudgetMini.text = "$currencySymbol ${AmountFormatter.formatAmount(monthlyBudget)}"
        }
    }

    private fun setupClickListeners() {
        binding.btnNotification.setOnClickListener {
            showTipBottomSheet()
        }

        binding.txtSeeAll.setOnClickListener {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                .selectedItemId = R.id.transactionHistoryFragment
        }

        binding.cardBudget.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_budget)
        }

        binding.cardStats.setOnClickListener {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                .selectedItemId = R.id.statisticsFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}