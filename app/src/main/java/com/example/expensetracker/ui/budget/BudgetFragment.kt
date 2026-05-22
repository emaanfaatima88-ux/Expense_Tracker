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
import com.google.android.material.chip.Chip
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.button.MaterialButton
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

        // Inline text update indicator action triggers bottom sheet
        binding.btnUpdateBudgetInline.setOnClickListener {
            showBudgetBottomSheet()
        }
    }

    private fun showBudgetBottomSheet() {
        val dialog = BottomSheetDialog(requireContext(), com.google.android.material.R.style.Theme_Design_Light_BottomSheetDialog)

        // 1. Inflate the custom bottom sheet layout file
        val dialogView = layoutInflater.inflate(R.layout.layout_bottom_sheet_budget, null)
        dialog.setContentView(dialogView)

        // 2. CRITICAL FIX: Use 'dialogView.findViewById' so it looks inside the sheet layout!
        val edtBudgetInput = dialogView.findViewById<android.widget.EditText>(R.id.edtBudgetInput)
        val btnCloseSheet = dialogView.findViewById<android.widget.ImageView>(R.id.btnClose)
        val btnCancelSheet = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelSheet)
        val btnSaveBudgetSheet = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveBudgetSheet)

        // 3. Bind the quick choice chips from the sheet layout
        val chip50k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip50k)
        val chip80k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip80k)
        val chip120k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip120k)
        val chip200k = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chip200k)

        // Automatically set the text field to show the user's current budget amount
        budgetViewModel.budget.value?.let { currentBudget ->
            edtBudgetInput.setText(currentBudget.monthlyBudget.toInt().toString())
        }

        // Quick Select Chip Click Listeners
        chip50k?.setOnClickListener { edtBudgetInput?.setText("50000") }
        chip80k?.setOnClickListener { edtBudgetInput?.setText("80000") }
        chip120k?.setOnClickListener { edtBudgetInput?.setText("120000") }
        chip200k?.setOnClickListener { edtBudgetInput?.setText("200000") }

        // Dismiss Sheet Actions
        btnCloseSheet?.setOnClickListener { dialog.dismiss() }
        btnCancelSheet?.setOnClickListener { dialog.dismiss() }

        // Save Button Core Logic
        btnSaveBudgetSheet?.setOnClickListener {
            val inputText = edtBudgetInput?.text.toString().trim()
            val parsedAmount = inputText.toDoubleOrNull()

            if (parsedAmount != null && parsedAmount > 0) {
                budgetViewModel.saveBudget(parsedAmount)
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(requireContext(), "Please enter a valid amount", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
    private fun observeBudgetDataStream() {
        val currencySymbol = CurrencyManager(requireContext()).getCurrencySymbol()

        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.txtCurrentMonthYear.text = monthYearFormat.format(Calendar.getInstance().time)

        budgetViewModel.budget.observe(viewLifecycleOwner) { budget ->
            // Fallback initialization defaults strictly to 120,000 as explicitly defined
            if (budget == null) {
                budgetViewModel.saveBudget(120000.0)
                return@observe
            }

            val monthlyBudgetLimit = budget.monthlyBudget
            binding.txtMonthlyBudget.text = "$currencySymbol ${AmountFormatter.formatAmount(monthlyBudgetLimit)}"

            expenseViewModel.totalExpense.observe(viewLifecycleOwner) { totalSpent ->
                binding.txtSpent.text = "$currencySymbol ${AmountFormatter.formatAmount(totalSpent)}"

                val remainingBalance = monthlyBudgetLimit - totalSpent
                binding.txtRemaining.text = "$currencySymbol ${AmountFormatter.formatAmount(remainingBalance)}"

                // Center Ring Progress Calculation Label String
                val progressPercent = if (monthlyBudgetLimit > 0) {
                    ((totalSpent / monthlyBudgetLimit) * 100).toInt()
                } else {
                    0
                }

                binding.txtProgressPercentage.text = "$progressPercent%"
                binding.progressBudgetRing.setProgress(progressPercent.coerceAtMost(100))

                // Toggle Ring Indicator Tone Map
                if (progressPercent >= 100) {
                    binding.progressBudgetRing.setIndicatorColor(Color.parseColor("#B05C1D"))
                } else {
                    binding.progressBudgetRing.setIndicatorColor(Color.parseColor("#D47216"))
                }

                // Pacing System Updates
                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                binding.txtPacingExpectedTitle.text = "By day $currentDay of $totalDaysInMonth, expected"

                val expectedSpentAmount = (monthlyBudgetLimit / totalDaysInMonth) * currentDay
                binding.txtPacingExpectedAmount.text = "$currencySymbol ${AmountFormatter.formatAmount(expectedSpentAmount)}"

                val deltaDifference = totalSpent - expectedSpentAmount

                if (deltaDifference <= 0) {
                    binding.txtPacingStatusLabel.text = "Under pace"
                    binding.txtPacingDifference.text = "-$currencySymbol ${AmountFormatter.formatAmount(Math.abs(deltaDifference))}"
                    binding.txtPacingDifference.setTextColor(Color.parseColor("#B05C1D"))
                } else {
                    binding.txtPacingStatusLabel.text = "Over pace"
                    binding.txtPacingDifference.text = "+$currencySymbol ${AmountFormatter.formatAmount(deltaDifference)}"
                    binding.txtPacingDifference.setTextColor(Color.parseColor("#D47216"))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}