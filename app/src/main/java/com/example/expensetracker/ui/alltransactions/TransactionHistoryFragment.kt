package com.example.expensetracker.ui.alltransactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.adapter.ExpenseAdapter
import com.example.expensetracker.databinding.FragmentTransactionHistoryBinding
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionHistoryFragment : Fragment() {

    private var _binding: FragmentTransactionHistoryBinding? = null

    private val binding get() = _binding!!

    private lateinit var expenseAdapter: ExpenseAdapter

    private val expenseViewModel: ExpenseViewModel by viewModels()

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

            expenseAdapter.setData(expenses)
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