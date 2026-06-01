package com.example.expensetracker.ui.home

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import com.example.expensetracker.MainActivity
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

    // ✅ Flag to prevent scroll listener from overriding hide when navigating to budget
    private var isNavigatingAway = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        tipsProvider = FinancialTipsProvider(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        observeExpenses()
        setupClickListeners()
        checkAndShowFirstOpenDialog()

        binding.nestedScrollViewHome.setOnScrollChangeListener(androidx.core.widget.NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // ✅ Block scroll from overriding visibility when navigating away
            if (isNavigatingAway) return@OnScrollChangeListener
            val dy = scrollY - oldScrollY
            if (dy > 10) {
                (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = false)
            } else if (dy < -10) {
                (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = true)
            }
        })
    }

    // ✅ Reset flag when user returns back to HomeFragment
    override fun onResume() {
        super.onResume()
        isNavigatingAway = false
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
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            itemAnimator = null
        }
    }

    private fun setupSwipeToDelete() {
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
                        expenseViewModel.deleteExpense(currentList[position])
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
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                val itemView = viewHolder.itemView
                val density = resources.displayMetrics.density
                val cornerRadius = 20f * density

                val cardLeft = itemView.left.toFloat()
                val cardTop = itemView.top.toFloat()
                val cardRight = itemView.right.toFloat()
                val cardBottom = itemView.bottom.toFloat()

                val cardPath = android.graphics.Path()
                val cardRect = android.graphics.RectF(cardLeft, cardTop, cardRight, cardBottom)
                cardPath.addRoundRect(cardRect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)

                c.save()
                c.clipPath(cardPath)

                val swipePaint = Paint().apply {
                    color = Color.parseColor("#d97706")
                    isAntiAlias = true
                }
                val swipeRect = android.graphics.RectF(
                    cardRight + dX,
                    cardTop,
                    cardRight,
                    cardBottom
                )
                c.drawRect(swipeRect, swipePaint)

                if (deleteIcon != null && dX < -deleteIcon.intrinsicWidth) {
                    val iconSize = (24 * density).toInt()
                    val iconMargin = (20 * density).toInt()
                    val iconTop = itemView.top + (itemView.height - iconSize) / 2
                    val iconLeft = itemView.right - iconMargin - iconSize
                    deleteIcon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                    deleteIcon.setTint(Color.WHITE)
                    deleteIcon.draw(c)
                }

                c.restore()

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

            expenseAdapter.setData(sortedExpenses.take(10).toMutableList())

            if (nonNullExpenses.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewExpenses.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewExpenses.visibility = View.VISIBLE
            }

            val totalSpentThisMonth = nonNullExpenses.sumOf { it.amount }
            binding.txtTotalExpense.text = "$currencySymbol ${AmountFormatter.formatAmount(totalSpentThisMonth)}"

            val totalTxnsCount = nonNullExpenses.size
            binding.txtTxnCount.text = totalTxnsCount.toString()

            val uniqueCategoriesCount = nonNullExpenses.map { it.category }.distinct().size
            binding.txtCategoryCount.text = uniqueCategoriesCount.toString()

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

            val calendar = Calendar.getInstance()
            val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val dailyAvg = totalSpentThisMonth / currentDayOfMonth
            binding.txtDailyAvg.text = "$currencySymbol ${AmountFormatter.formatAmount(dailyAvg)}"

            val weeklyTotal = nonNullExpenses.takeLast(7).sumOf { it.amount }
            binding.txtWeeklyStats.text = "$currencySymbol ${AmountFormatter.formatAmount(weeklyTotal)}"
        }
    }

    private fun setupClickListeners() {
        binding.btnQuickAddExpense.setOnClickListener {
            val bottomSheet = AddExpenseBottomSheet(null)
            bottomSheet.show(parentFragmentManager, "AddExpenseFromSummary")
        }

        binding.btnNotification.setOnClickListener {
            showTipBottomSheet()
        }

        binding.txtSeeAll.setOnClickListener {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                .selectedItemId = R.id.transactionHistoryFragment
        }

        // ✅ Set flag + hide nav before navigating to budget
        binding.cardBudget.setOnClickListener {
            isNavigatingAway = true
            (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = false)
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