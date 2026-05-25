package com.example.expensetracker.ui.home

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.adapter.ExpenseAdapter
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.example.expensetracker.utils.AmountFormatter
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.utils.FinancialTipsProvider
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

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

    private fun showTipBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_tip_bottom_sheet, null)

        val txtTip = view.findViewById<TextView>(R.id.txtBottomSheetTip)
        val txtStat = view.findViewById<TextView>(R.id.txtNotificationStat)
        val btnClose = view.findViewById<ImageView>(R.id.btnCloseBottomSheet)

        txtTip.text = tipsProvider.getDailyTip()

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
        val background = ColorDrawable()
        val backgroundColor = Color.parseColor("#d97706")
        val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_sweep)

        val swipeGesture = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

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

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val isCanceled = dX == 0f && !isCurrentlyActive

                if (isCanceled) {
                    c.drawRect(
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        clearPaint
                    )
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                background.color = backgroundColor
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                if (deleteIcon != null) {
                    val intrinsicWidth = deleteIcon.intrinsicWidth
                    val intrinsicHeight = deleteIcon.intrinsicHeight

                    val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                    val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                    val deleteIconRight = itemView.right - deleteIconMargin
                    val deleteIconBottom = deleteIconTop + intrinsicHeight

                    deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                    deleteIcon.setTint(Color.WHITE)
                    deleteIcon.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeGesture).attachToRecyclerView(binding.recyclerViewExpenses)
    }

    private fun observeExpenses() {
        val currencyManager = CurrencyManager(requireContext())
        val currencySymbol = currencyManager.getCurrencySymbol()

        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            val nonNullExpenses = expenses ?: emptyList()

            // 🛠️ DATE SORTING ENGINE (Newest to Oldest)
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

            val sortedExpenses = nonNullExpenses.sortedWith { item1, item2 ->
                try {
                    val date1 = dateFormatter.parse(item1.date)
                    val date2 = dateFormatter.parse(item2.date)
                    date2?.compareTo(date1) ?: 0
                } catch (e: Exception) {
                    0
                }
            }

            // Take top 10 for recent view container
            expenseAdapter.setData(sortedExpenses.take(10).toMutableList())

            if (nonNullExpenses.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewExpenses.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewExpenses.visibility = View.VISIBLE
            }

            // 🛠️ FIX: COMPUTE & INJECT REAL-TIME CALCULATIONS INTO THE SUMMARY CARD

            // 1. Calculations for total spent and counts
            val totalSpentThisMonth = nonNullExpenses.sumOf { it.amount }
            binding.txtTotalExpense.text = "$currencySymbol ${AmountFormatter.formatAmount(totalSpentThisMonth)}"

            val totalTxnsCount = nonNullExpenses.size
            binding.txtTransactionCount.text = "$totalTxnsCount txns"
            binding.txtTxnCount.text = totalTxnsCount.toString()

            val uniqueCategoriesCount = nonNullExpenses.map { it.category }.distinct().size
            binding.txtCategoryCount.text = uniqueCategoriesCount.toString()

            // 2. Budget and remaining parameters calculation
            val budgetPercent = if (monthlyBudget > 0) ((totalSpentThisMonth / monthlyBudget) * 100).roundToInt() else 0
            binding.progressMonthly.progress = budgetPercent.coerceAtMost(100)

            val formattedBudget = AmountFormatter.formatAmount(monthlyBudget)
            binding.txtBudgetPercent.text = "$budgetPercent% of $currencySymbol $formattedBudget"
            binding.txtBudgetMini.text = "$currencySymbol $formattedBudget"

            val remainingAmount = monthlyBudget - totalSpentThisMonth
            if (remainingAmount >= 0) {
                binding.txtRemainingAmount.text = "$currencySymbol ${AmountFormatter.formatAmount(remainingAmount)} left"
            } else {
                binding.txtRemainingAmount.text = "$currencySymbol ${AmountFormatter.formatAmount(Math.abs(remainingAmount))} over"
            }

            // 3. Dynamic Daily Average Calculation Based on Days Gone by in Current Month
            val calendar = Calendar.getInstance()
            val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dailyAvg = totalSpentThisMonth / currentDayOfMonth
            binding.txtDailyAvg.text = "$currencySymbol ${AmountFormatter.formatAmount(dailyAvg)}"

            // 4. Set weekly footer navigation status indicator preview balance text
            val weeklyTotal = nonNullExpenses.takeLast(7).sumOf { it.amount }
            binding.txtWeeklyStats.text = "$currencySymbol ${AmountFormatter.formatAmount(weeklyTotal)}"
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