package com.example.expensetracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.adapter.ExpenseAdapter
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.example.expensetracker.utils.CurrencyManager
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var expenseAdapter: ExpenseAdapter

    private val expenseViewModel: ExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentHomeBinding.inflate(
                inflater,
                container,
                false
            )

        setupRecyclerView()
        setupSwipeToDelete()
        observeExpenses()

        setupClickListeners()

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

        binding.recyclerViewExpenses.apply {

            adapter = expenseAdapter

            layoutManager =
                LinearLayoutManager(requireContext())

            setHasFixedSize(false)

            isNestedScrollingEnabled = true

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
                        viewHolder.bindingAdapterPosition

                    if (position != RecyclerView.NO_POSITION) {

                        val currentList =
                            expenseAdapter.currentList

                        if (position < currentList.size) {

                            val expense =
                                currentList[position]

                            expenseViewModel.deleteExpense(expense)
                        }
                    }
                }
            }

        val itemTouchHelper =
            ItemTouchHelper(swipeGesture)

        itemTouchHelper.attachToRecyclerView(
            binding.recyclerViewExpenses
        )
    }
    private fun observeExpenses() {

        val currencyManager =
            CurrencyManager(requireContext())

        val currencySymbol =
            currencyManager.getCurrencySymbol()

        expenseViewModel.allExpenses.observe(
            viewLifecycleOwner
        ) { expenses ->

            // SHOW ONLY RECENT 10

            expenseAdapter.setData(
                expenses.take(10).toMutableList()
            )

            if (expenses.isEmpty()) {

                binding.layoutEmptyState.visibility =
                    View.VISIBLE

                binding.recyclerViewExpenses.visibility =
                    View.GONE
            }

            else {

                binding.layoutEmptyState.visibility =
                    View.GONE

                binding.recyclerViewExpenses.visibility =
                    View.VISIBLE
            }

            binding.txtTransactionCount.text =
                expenses.size.toString()

            binding.txtCategoryCount.text =
                expenses.map { it.category }
                    .distinct()
                    .size
                    .toString()
        }

        expenseViewModel.totalExpense.observe(
            viewLifecycleOwner
        ) { total ->

            binding.txtTotalExpense.text =
                "$currencySymbol %.0f".format(total)

            val dailyAverage =
                total / 30

            binding.txtDailyAvg.text =
                "$currencySymbol %.0f".format(dailyAverage)
        }
    }

    private fun setupClickListeners() {

        binding.txtSeeAll.setOnClickListener {

            findNavController().navigate(
                R.id.transactionHistoryFragment
            )
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}