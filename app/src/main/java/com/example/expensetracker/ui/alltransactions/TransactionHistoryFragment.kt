package com.example.expensetracker.ui.alltransactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.adapter.ExpenseAdapter
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.FragmentTransactionHistoryBinding
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionHistoryFragment : Fragment() {

    private var _binding: FragmentTransactionHistoryBinding? = null

    private val binding get() = _binding!!

    private lateinit var expenseAdapter: ExpenseAdapter

    private val expenseViewModel: ExpenseViewModel by viewModels()

    private var allExpensesList =
        emptyList<ExpenseEntity>()

    private val categoryList = listOf(

        "All",
        "Food",
        "Bills",
        "Shopping",
        "Transport",
        "Health",
        "Transport",
        "Others"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentTransactionHistoryBinding.inflate(
                inflater,
                container,
                false
            )

        setupRecyclerView()

        setupSwipeToDelete()

        observeExpenses()

        setupFilter()

        setupBackButton()

        return binding.root
    }

    private fun setupRecyclerView() {

        expenseAdapter = ExpenseAdapter(

            onItemClick = { expense ->

                val bottomSheet =
                    AddExpenseBottomSheet(expense)

                bottomSheet.show(
                    parentFragmentManager,
                    "UpdateExpense"
                )
            },

            onLongClick = { }
        )

        binding.recyclerViewAllTransactions.apply {

            adapter = expenseAdapter

            layoutManager =
                LinearLayoutManager(requireContext())

            setHasFixedSize(false)

            isNestedScrollingEnabled = false

            itemAnimator = null
        }
    }

    private fun setupSwipeToDelete() {

        val swipeGesture =
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {

                    val position =
                        viewHolder.adapterPosition

                    val expense =
                        expenseAdapter.currentList[position]

                    expenseViewModel.deleteExpense(expense)
                }
            }

        val itemTouchHelper =
            ItemTouchHelper(swipeGesture)

        itemTouchHelper.attachToRecyclerView(
            binding.recyclerViewAllTransactions
        )
    }

    private fun observeExpenses() {

        expenseViewModel.allExpenses.observe(
            viewLifecycleOwner
        ) { expenses ->

            allExpensesList = expenses

            expenseAdapter.setData(expenses)

            if (expenses.isEmpty()) {

                binding.txtNoResult.visibility =
                    View.VISIBLE

                binding.recyclerViewAllTransactions.visibility =
                    View.GONE

            } else {

                binding.txtNoResult.visibility =
                    View.GONE

                binding.recyclerViewAllTransactions.visibility =
                    View.VISIBLE
            }
        }
    }

    private fun setupFilter() {

        binding.btnFilter.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())

                .setTitle("Select Category")

                .setItems(
                    categoryList.toTypedArray()
                ) { _, which ->

                    val selectedCategory =
                        categoryList[which]

                    val filteredList = if (
                        selectedCategory == "All"
                    ) {

                        allExpensesList

                    } else {

                        allExpensesList.filter {

                            it.category.equals(
                                selectedCategory,
                                ignoreCase = true
                            )
                        }
                    }

                    expenseAdapter.setData(filteredList)

                    if (filteredList.isEmpty()) {

                        binding.txtNoResult.visibility =
                            View.VISIBLE

                        binding.recyclerViewAllTransactions.visibility =
                            View.GONE

                    } else {

                        binding.txtNoResult.visibility =
                            View.GONE

                        binding.recyclerViewAllTransactions.visibility =
                            View.VISIBLE
                    }
                }

                .show()
        }
    }

    private fun setupBackButton() {

        binding.btnBack.setOnClickListener {

            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}