package com.example.expensetracker.ui.addexpense

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.BottomSheetAddExpenseBinding
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddExpenseBottomSheet(
    private val expense: ExpenseEntity? = null
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddExpenseBinding? = null

    private val binding get() = _binding!!

    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomSheetAddExpenseBinding.inflate(
            inflater,
            container,
            false
        )

        setupCategoryDropdown()

        setupDefaultValues()

        setupOldExpenseData()

        setupDatePicker()

        saveExpense()

        return binding.root
    }

    private fun setupDefaultValues() {

        // Only for ADD MODE
        if (expense == null) {

            // Default category
            binding.autoCategory.setText(
                "Others",
                false
            )

            // Today's date
            val currentDate = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Calendar.getInstance().time)

            binding.etDate.setText(currentDate)
        }
    }

    private fun setupOldExpenseData() {

        expense?.let {

            binding.etTitle.setText(it.title)

            binding.etAmount.setText(
                it.amount.toString()
            )

            binding.autoCategory.setText(
                it.category,
                false
            )

            binding.etDate.setText(it.date)

            binding.btnSaveExpense.text =
                "Update Expense"
        }
    }

    private fun setupCategoryDropdown() {

        val categories = listOf(

            "Food",
            "Shopping",
            "Transport",
            "Bills",
            "Health",
            "Others"
        )

        val adapter = ArrayAdapter(

            requireContext(),

            android.R.layout.simple_dropdown_item_1line,

            categories
        )

        binding.autoCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {

        binding.etDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)

            val month = calendar.get(Calendar.MONTH)

            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(

                requireContext(),

                { _, selectedYear, selectedMonth, selectedDay ->

                    val formattedDate =
                        "$selectedDay/${selectedMonth + 1}/$selectedYear"

                    binding.etDate.setText(formattedDate)
                },

                year,
                month,
                day
            )

            datePickerDialog.show()
        }
    }

    private fun saveExpense() {

        binding.btnSaveExpense.setOnClickListener {

            val title =
                binding.etTitle.text.toString().trim()

            val amount =
                binding.etAmount.text.toString().trim()

            val category =
                binding.autoCategory.text.toString().trim()

            val date =
                binding.etDate.text.toString().trim()

            // TITLE VALIDATION
            if (!title.matches(Regex("^[a-zA-Z ]+$"))) {

                binding.etTitle.error =
                    "Only letters allowed"

                return@setOnClickListener
            }

            if (
                title.isNotEmpty() &&
                amount.isNotEmpty() &&
                category.isNotEmpty() &&
                date.isNotEmpty()
            ) {

                // ADD MODE
                if (expense == null) {

                    val expenseEntity = ExpenseEntity(

                        title = title,

                        amount = amount.toDouble(),

                        category = category,

                        date = date,

                        note = ""
                    )

                    expenseViewModel.insertExpense(
                        expenseEntity
                    )

                    Toast.makeText(
                        requireContext(),
                        "Expense Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // UPDATE MODE
                else {

                    val updatedExpense =
                        expense.copy(

                            title = title,

                            amount = amount.toDouble(),

                            category = category,

                            date = date
                        )

                    expenseViewModel.updateExpense(
                        updatedExpense
                    )

                    Toast.makeText(
                        requireContext(),
                        "Expense Updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                dismiss()

            } else {

                Toast.makeText(
                    requireContext(),
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}