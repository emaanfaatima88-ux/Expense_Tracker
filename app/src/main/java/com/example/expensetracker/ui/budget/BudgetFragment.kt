package com.example.expensetracker.ui.budget

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.expensetracker.utils.AmountFormatter
import com.example.expensetracker.databinding.FragmentBudgetBinding
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.expensetracker.R
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel: BudgetViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()

    private var cachedBudgetLimit = 0.0
    private var cachedTotalSpent = 0.0
    private var hasAnimated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)

        // ✅ Force ring to 0 IMMEDIATELY after inflation, before anything renders
        binding.progressBudgetRing.setProgressCompat(0, false)

        setupActionClicks()
        observeBudgetDataStream()
        return binding.root
    }

    private fun setupActionClicks() {
        binding.btnBackNavigation.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.btnUpdateBudgetInline.setOnClickListener {
            showBudgetBottomSheet()
        }
    }

    private fun showBudgetBottomSheet() {
        // 1. Pass your custom style that sets backgroundTint and windowBackground to transparent
        val dialog = BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialogNoShadow
        )
        val dialogView = layoutInflater.inflate(R.layout.layout_bottom_sheet_budget, null)
        dialog.setContentView(dialogView)

        // 2. Clear the default container background colors directly via the window layer to kill the square corners
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val edtBudgetInput = dialogView.findViewById<android.widget.EditText>(R.id.edtBudgetInput)
        val btnCloseSheet = dialogView.findViewById<android.widget.ImageView>(R.id.btnClose)
        val btnCancelSheet = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelSheet)
        val btnSaveBudgetSheet = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveBudgetSheet)
        val chip50k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip50k)
        val chip80k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip80k)
        val chip120k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip120k)
        val chip200k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip200k)

        budgetViewModel.budget.value?.let { currentBudget ->
            edtBudgetInput.setText(currentBudget.monthlyBudget.toInt().toString())
        }

        chip50k?.setOnClickListener { edtBudgetInput?.setText("50000") }
        chip80k?.setOnClickListener { edtBudgetInput?.setText("80000") }
        chip120k?.setOnClickListener { edtBudgetInput?.setText("120000") }
        chip200k?.setOnClickListener { edtBudgetInput?.setText("200000") }

        btnCloseSheet?.setOnClickListener { dialog.dismiss() }
        btnCancelSheet?.setOnClickListener { dialog.dismiss() }

        btnSaveBudgetSheet?.setOnClickListener {
            val inputText = edtBudgetInput?.text.toString().trim()
            val parsedAmount = inputText.toDoubleOrNull()
            if (parsedAmount != null && parsedAmount > 0) {
                budgetViewModel.saveBudget(parsedAmount)
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please enter a valid amount",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    private fun observeBudgetDataStream() {
        val currencySymbol = CurrencyManager(requireContext()).getCurrencySymbol()
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.txtCurrentMonthYear.text = monthYearFormat.format(Calendar.getInstance().time)

        budgetViewModel.budget.observe(viewLifecycleOwner) { budget ->
            if (budget == null) {
                budgetViewModel.saveBudget(120000.0)
                return@observe
            }
            cachedBudgetLimit = budget.monthlyBudget
            updateUI(currencySymbol)
        }

        expenseViewModel.totalExpense.observe(viewLifecycleOwner) { totalSpent ->
            cachedTotalSpent = totalSpent
            updateUI(currencySymbol)
        }
    }

    private fun updateUI(currencySymbol: String) {
        if (cachedBudgetLimit <= 0) return

        val remainingBalance = cachedBudgetLimit - cachedTotalSpent
        binding.txtSpent.text = "$currencySymbol ${AmountFormatter.formatAmount(cachedTotalSpent)}"
        binding.txtRemaining.text = "$currencySymbol ${AmountFormatter.formatAmount(remainingBalance)}"
        binding.txtMonthlyBudget.text = "$currencySymbol ${AmountFormatter.formatAmount(cachedBudgetLimit)}"

        val progressPercent = ((cachedTotalSpent / cachedBudgetLimit) * 100)
            .toInt().coerceIn(0, 100)
        binding.txtProgressPercentage.text = "$progressPercent%"

        val indicatorColor = if (progressPercent >= 100) "#B05C1D" else "#D47216"
        binding.progressBudgetRing.setIndicatorColor(Color.parseColor(indicatorColor))

        // 🔥 FORCE RESET & ANIMATE FORWARD COLD
        // This removes all reliance on flags. It snaps the UI back to 0 instantly,
        // then handles a clean, smooth clockwise animation up to the target percent.
        binding.progressBudgetRing.setProgressCompat(0, false)
        binding.root.post {
            if (_binding != null) {
                binding.progressBudgetRing.setProgressCompat(progressPercent, true)
            }
        }

        // Pacing calculation logic remains perfectly untouched
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        binding.txtPacingExpectedTitle.text = "By day $currentDay of $totalDaysInMonth, expected"

        val expectedSpent = (cachedBudgetLimit / totalDaysInMonth) * currentDay
        binding.txtPacingExpectedAmount.text =
            "$currencySymbol ${AmountFormatter.formatAmount(expectedSpent)}"

        val delta = cachedTotalSpent - expectedSpent
        if (delta <= 0) {
            binding.txtPacingStatusLabel.text = "Under pace"
            binding.txtPacingDifference.text =
                "-$currencySymbol ${AmountFormatter.formatAmount(Math.abs(delta))}"
            binding.txtPacingDifference.setTextColor(Color.parseColor("#B05C1D"))
        } else {
            branchPacingOver(currencySymbol, delta)
        }
    }

    private fun branchPacingOver(currencySymbol: String, delta: Double) {
        binding.txtPacingStatusLabel.text = "Over pace"
        binding.txtPacingDifference.text =
            "+$currencySymbol ${AmountFormatter.formatAmount(delta)}"
        binding.txtPacingDifference.setTextColor(Color.parseColor("#D47216"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ✅ FIXED: Resets the flag every time the view layout breaks down, stopping backward rotation loops
        hasAnimated = false
        _binding = null
    }
}