package com.example.expensetracker.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.FragmentBudgetBinding
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null

    private val binding get() = _binding!!

    private val budgetViewModel: BudgetViewModel by viewModels()

    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBudgetBinding.inflate(
            inflater,
            container,
            false
        )

        setupClicks()

        observeBudget()
        return binding.root
    }

    private fun setupClicks() {

        binding.btnSetBudget.setOnClickListener {

            showBudgetDialog()
        }

        binding.btnEditBudget.setOnClickListener {

            showBudgetDialog()
        }
        binding.recyclerSwipeBudget.setOnLongClickListener {

            MaterialAlertDialogBuilder(requireContext())

                .setTitle("Delete Budget")

                .setMessage(
                    "Do you want to remove monthly budget?"
                )

                .setPositiveButton("Delete") { _, _ ->

                    budgetViewModel.deleteBudget()
                }

                .setNegativeButton("Cancel", null)

                .show()

            true
        }
    }

    private fun showBudgetDialog() {

        val input = TextInputEditText(requireContext())

        input.hint = "Enter Monthly Budget"

        // SHOW NUMERIC KEYBOARD
        input.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        input.requestFocus()

        MaterialAlertDialogBuilder(requireContext())

            .setTitle("Monthly Budget")

            .setView(input)

            .setPositiveButton("Save") { _, _ ->

                val amount =
                    input.text.toString().trim()

                val budgetAmount =
                    amount.toDoubleOrNull()

                if (
                    budgetAmount != null &&
                    budgetAmount > 0
                ) {

                    budgetViewModel.saveBudget(
                        budgetAmount
                    )

                } else {

                    android.widget.Toast.makeText(
                        requireContext(),
                        "Enter valid amount",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }

            .setNegativeButton("Cancel", null)

            .show()
    }

    private fun observeBudget() {

        val currencyManager =
            CurrencyManager(requireContext())

        val currencySymbol =
            currencyManager.getCurrencySymbol()

        budgetViewModel.budget.observe(
            viewLifecycleOwner
        ) { budget ->

            if (budget == null) {

                binding.layoutEmptyState.visibility =
                    View.VISIBLE

                binding.layoutBudgetContent.visibility =
                    View.GONE

                return@observe
            }

            binding.layoutEmptyState.visibility =
                View.GONE

            binding.layoutBudgetContent.visibility =
                View.VISIBLE

            val monthlyBudget =
                budget.monthlyBudget

            binding.txtMonthlyBudget.text =
                "$currencySymbol %.0f".format(monthlyBudget)

            expenseViewModel.totalExpense.observe(
                viewLifecycleOwner
            ) { totalSpent ->

                binding.txtSpent.text =
                    "$currencySymbol %.0f".format(totalSpent)

                val remaining =
                    monthlyBudget - totalSpent

                binding.txtRemaining.text =
                    "$currencySymbol %.0f".format(remaining)

                val progress = if (monthlyBudget > 0) {

                    ((totalSpent / monthlyBudget) * 100).toInt()

                } else {

                    0
                }

                binding.progressBudget.progress =
                    progress.coerceAtMost(100)

                when {

                    progress < 70 -> {

                        binding.txtRemaining.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                android.R.color.holo_green_dark
                            )
                        )
                    }

                    progress in 70..89 -> {

                        binding.txtRemaining.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                android.R.color.holo_orange_dark
                            )
                        )
                    }

                    else -> {

                        binding.txtRemaining.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                android.R.color.holo_red_dark
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}