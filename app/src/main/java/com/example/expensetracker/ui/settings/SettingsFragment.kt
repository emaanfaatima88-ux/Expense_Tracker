package com.example.expensetracker.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.expensetracker.databinding.FragmentSettingsBinding
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.utils.PdfGenerator
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupClicks()
        setupCurrencyDropdown()

        return binding.root
    }

    private fun setupClicks() {
        // DELETE ALL EXPENSES IMPLEMENTATION
        binding.btnDeleteAllTransactions.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete All Expenses")
                .setMessage("Are you sure you want to delete all expenses?")
                .setPositiveButton("Delete") { _, _ ->
                    expenseViewModel.deleteAllExpenses()
                    Toast.makeText(
                        requireContext(),
                        "All expenses deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // EXPORT EXCEL/PDF REPORT DOCUMENT IMPLEMENTATION
        binding.btnExportPDF.setOnClickListener {
            expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
                if (expenses == null || expenses.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No expenses found to export",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val pdfUri = PdfGenerator.generateExpensePdf(requireContext(), expenses)

                    if (pdfUri != null) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("PDF Exported")
                            .setMessage("Expense report saved successfully.\n\nDo you want to open it?")
                            .setPositiveButton("Open") { _, _ ->
                                openPdf(pdfUri)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to export PDF",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupCurrencyDropdown() {
        val currencies = listOf(
            "Pakistani Rupee (Rs)",
            "US Dollar ($)",
            "Euro (€)",
            "British Pound (£)"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            currencies
        )
        binding.dropdownCurrency.setAdapter(adapter)

        val currencyManager = CurrencyManager(requireContext())

        // SHOW CURRENTLY SAVED PREFERENCE STATE
        val savedCurrency = currencyManager.getCurrency()
        binding.dropdownCurrency.setText(savedCurrency, false)

        // MUTATE & WRITE NEW CURRENCY STATE TO STORAGE
        binding.dropdownCurrency.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = currencies[position]
            binding.dropdownCurrency.setText(selectedCurrency, false)
            currencyManager.saveCurrency(selectedCurrency)

            Toast.makeText(
                requireContext(),
                "Currency Updated",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openPdf(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "No PDF viewer app found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}