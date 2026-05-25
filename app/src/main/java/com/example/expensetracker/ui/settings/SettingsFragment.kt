package com.example.expensetracker.ui.settings

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController // Import needed for Jetpack Navigation
import com.example.expensetracker.R
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
        // 🛠️ BUDGET CARD NAVIGATION
        // Replace R.id.budgetFragment with your exact navigation destination action/ID if different
        binding.btnSettingsBudget.setOnClickListener {
            findNavController().navigate(R.id.budgetFragment)
        }

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
        val currencyManager = CurrencyManager(requireContext())
        binding.txtSelectedCurrency.text = currencyManager.getCurrency()

        binding.btnCurrencySelector.setOnClickListener {
            val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_currency, null)
            bottomSheet.setContentView(view)

            val layoutCurrencies = view.findViewById<LinearLayout>(R.id.layoutCurrencies)

            val currencies = listOf(
                Triple("Pakistani Rupee", "PKR", "Rs"),
                Triple("US Dollar", "USD", "$"),
                Triple("Euro", "EUR", "€"),
                Triple("British Pound", "GBP", "£"),
                Triple("Indian Rupee", "INR", "₹"),
                Triple("UAE Dirham", "AED", "AED"),
                Triple("Japanese Yen", "JPY", "¥")
            )

            val savedCurrency = currencyManager.getCurrency()

            currencies.forEach { currency ->
                val itemView = layoutInflater.inflate(R.layout.item_currency, layoutCurrencies, false)

                val txtSymbol = itemView.findViewById<TextView>(R.id.txtSymbol)
                val txtName = itemView.findViewById<TextView>(R.id.txtCurrencyName)
                val txtCode = itemView.findViewById<TextView>(R.id.txtCurrencyCode)
                val imgCheck = itemView.findViewById<ImageView>(R.id.imgCheck)

                txtSymbol.text = currency.third
                txtName.text = currency.first
                txtCode.text = currency.second

                val isSelected = savedCurrency.contains(currency.first)

                if (isSelected) {
                    imgCheck.visibility = View.VISIBLE
                    itemView.setBackgroundResource(R.drawable.bg_currency_item)
                    itemView.background?.setTint(Color.parseColor("#FBEFDF"))

                    txtName.setTextColor(Color.parseColor("#D47216"))
                    txtSymbol.setTextColor(Color.parseColor("#D47216"))
                } else {
                    imgCheck.visibility = View.GONE
                    itemView.background = null

                    txtName.setTextColor(Color.parseColor("#1A1612"))
                    txtSymbol.setTextColor(Color.parseColor("#8D8578"))
                }

                itemView.setOnClickListener {
                    val selected = "${currency.first} (${currency.third})"
                    currencyManager.saveCurrency(selected)
                    binding.txtSelectedCurrency.text = selected

                    Toast.makeText(
                        requireContext(),
                        "Currency Updated",
                        Toast.LENGTH_SHORT
                    ).show()

                    bottomSheet.dismiss()
                }

                layoutCurrencies.addView(itemView)
            }
            bottomSheet.show()
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