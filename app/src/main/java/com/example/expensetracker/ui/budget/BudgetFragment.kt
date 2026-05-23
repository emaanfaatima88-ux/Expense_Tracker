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
        val dialog = BottomSheetDialog(
            requireContext(),
            com.google.android.material.R.style.Theme_Design_Light_BottomSheetDialog
        )
        val dialogView = layoutInflater.inflate(R.layout.layout_bottom_sheet_budget, null)
        dialog.setContentView(dialogView)

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

        if (!hasAnimated) {
            hasAnimated = true
            // ✅ Reset to 0 instantly, then animate to actual value after fragment is drawn
            binding.progressBudgetRing.setProgressCompat(0, false)
            binding.root.postDelayed({
                if (_binding == null) return@postDelayed
                binding.progressBudgetRing.setProgressCompat(progressPercent, true)
            }, 400)
        } else {
            // ✅ Budget updated via bottom sheet — just update instantly, no animation
            binding.progressBudgetRing.setProgressCompat(progressPercent, false)
        }

        // Pacing
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
            binding.txtPacingStatusLabel.text = "Over pace"
            binding.txtPacingDifference.text =
                "+$currencySymbol ${AmountFormatter.formatAmount(delta)}"
            binding.txtPacingDifference.setTextColor(Color.parseColor("#D47216"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasAnimated = false  // ✅ Reset so every fresh open animates correctly
        _binding = null
    }
}