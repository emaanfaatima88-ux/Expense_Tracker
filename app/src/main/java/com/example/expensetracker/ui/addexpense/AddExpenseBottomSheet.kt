package com.example.expensetracker.ui.addexpense

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View // Added explicit view framework import for safety
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.BottomSheetAddExpenseBinding
import com.example.expensetracker.utils.ExpenseCategoryHelper
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

    // Tracking the currently selected category item
    private var selectedCategory: String = "Others"
    private val categoryViewsList = ArrayList<Pair<String, LinearLayout>>()

    // Define categories
    private val categoriesData = listOf(
        "Food & Drink", "Transport", "Health", "Shopping",
        "Bills", "Entertainment", "Education", "Coffee", "Others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddExpenseBinding.inflate(inflater, container, false)

        setupCategoryGrid()
        setupDefaultValues()
        setupOldExpenseData()
        setupDatePicker()

        binding.etAmount.filters = arrayOf(InputFilter.LengthFilter(12))
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        saveExpense()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
        val bottomSheet =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let { sheet ->

            sheet.setBackgroundColor(Color.TRANSPARENT)

            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)

            // same as filter sheet behavior
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

            sheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }
    private fun setupCategoryGrid() {

        binding.categoryGrid.removeAllViews()

        categoryViewsList.clear()

        val displayMetrics =
            resources.displayMetrics

        val screenWidth =
            displayMetrics.widthPixels

        // TOTAL horizontal padding/margins
        val totalMargin =
            dpToPx(72f)

        // PERFECT 3 COLUMN WIDTH
        val itemWidth =
            (screenWidth - totalMargin) / 3

        categoriesData.forEach { catName ->

            val cellView =
                layoutInflater.inflate(
                    R.layout.item_category_grid,
                    binding.categoryGrid,
                    false
                ) as LinearLayout

            val params =
                GridLayout.LayoutParams().apply {

                    width = itemWidth

                    height = dpToPx(58f)

                    setMargins(
                        dpToPx(4f),
                        dpToPx(4f),
                        dpToPx(4f),
                        dpToPx(4f)
                    )
                }

            cellView.layoutParams = params

            val titleTxt =
                cellView.findViewById<TextView>(
                    R.id.txtCategoryName
                )

            val iconImg =
                cellView.findViewById<ImageView>(
                    R.id.imgCategoryIcon
                )

            titleTxt.text = catName

            when (catName) {

                "Food & Drink" ->
                    iconImg.setImageResource(R.drawable.ic_food)

                "Transport" ->
                    iconImg.setImageResource(R.drawable.ic_transport)

                "Health" ->
                    iconImg.setImageResource(R.drawable.ic_health)

                "Shopping" ->
                    iconImg.setImageResource(R.drawable.ic_shopping)

                "Bills" ->
                    iconImg.setImageResource(R.drawable.ic_bills)

                "Entertainment" ->
                    iconImg.setImageResource(R.drawable.ic_entertainment)

                "Education" ->
                    iconImg.setImageResource(R.drawable.ic_education)

                "Coffee" ->
                    iconImg.setImageResource(R.drawable.ic_coffee)

                else ->
                    iconImg.setImageResource(R.drawable.ic_other)
            }

            cellView.setOnClickListener {

                selectCategoryItem(catName)
            }

            binding.categoryGrid.addView(cellView)

            categoryViewsList.add(
                Pair(catName, cellView)
            )
        }

        selectCategoryItem(selectedCategory)
    }

    private fun selectCategoryItem(categoryName: String) {
        selectedCategory = categoryName

        categoryViewsList.forEach { viewPair ->
            val currentCatName = viewPair.first
            val catView = viewPair.second
            val titleTxt = catView.findViewById<TextView>(R.id.txtCategoryName)
            val iconImg = catView.findViewById<ImageView>(R.id.imgCategoryIcon)

            // Normalize strings to match your helper's conditions (e.g. "Food & Drink" -> pass "food")
            val cleanKey = if (currentCatName.contains("&")) {
                currentCatName.split("&")[0].trim()
            } else {
                currentCatName
            }

            val baseColorStr = ExpenseCategoryHelper.getCategoryColor(cleanKey)
            val baseColor = Color.parseColor(baseColorStr)

            if (currentCatName == selectedCategory) {
                // 1. ACTIVE STATE: Generate custom background and stroke programmatically
                val activeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = getCornerRadiusPx()
                    setColor(baseColor)

                    val hsv = FloatArray(3)
                    Color.colorToHSV(baseColor, hsv)
                    hsv[2] *= 0.7f
                    val strokeColor = Color.HSVToColor(hsv)

                    setStroke(dpToPx(1.5f), strokeColor)
                }
                catView.background = activeDrawable

                val hsv = FloatArray(3)
                Color.colorToHSV(baseColor, hsv)
                hsv[2] *= 0.5f
                val deepContrastColor = Color.HSVToColor(hsv)

                titleTxt.setTextColor(deepContrastColor)
                iconImg.imageTintList = ColorStateList.valueOf(deepContrastColor)

            } else {
                // 2. INACTIVE STATE: Clean background fallback
                val inactiveDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = getCornerRadiusPx()
                    setColor(Color.parseColor("#FFFDF9"))
                }
                catView.background = inactiveDrawable

                val defaultDarkColor = Color.parseColor("#1A1612")
                titleTxt.setTextColor(defaultDarkColor)
                iconImg.imageTintList = ColorStateList.valueOf(defaultDarkColor)
            }
        }
    }

    private fun dpToPx(dp: Float): Int {
        val metrics = context?.resources?.displayMetrics ?: return dp.toInt()
        return (dp * metrics.density).toInt()
    }

    private fun getCornerRadiusPx(): Float {
        val metrics = context?.resources?.displayMetrics ?: return 20f
        return 20f * metrics.density
    }

    private fun setupDefaultValues() {
        if (expense == null) {
            selectCategoryItem("Others")

            val currentDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time)
            binding.etDate.text = currentDate
        }
    }

    private fun setupOldExpenseData() {
        expense?.let {
            binding.txtSheetTitle.text = "Edit expense"
            binding.etTitle.setText(it.title)
            binding.etAmount.setText(it.amount.toString())
            binding.etDate.text = it.date
            binding.btnSaveExpense.text = "Update Expense"
            selectCategoryItem(it.category)
        }
    }

    private fun setupDatePicker() {
        binding.btnDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val displayCalendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                    }
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(displayCalendar.time)
                    binding.etDate.text = formattedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun saveExpense() {
        binding.btnSaveExpense.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val amount = binding.etAmount.text.toString().trim()
            val date = binding.etDate.text.toString().trim()

            if (title.isEmpty() || amount.isEmpty() || date.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expense == null) {
                val expenseEntity = ExpenseEntity(
                    title = title,
                    amount = amount.toDouble(),
                    category = selectedCategory,
                    date = date,
                    note = ""
                )
                expenseViewModel.insertExpense(expenseEntity)
                Toast.makeText(requireContext(), "Expense Saved", Toast.LENGTH_SHORT).show()
            } else {
                val updatedExpense = expense.copy(
                    title = title,
                    amount = amount.toDouble(),
                    category = selectedCategory,
                    date = date
                )
                expenseViewModel.updateExpense(updatedExpense)
                Toast.makeText(requireContext(), "Expense Updated", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}