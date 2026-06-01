package com.example.expensetracker.ui.addexpense

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.expensetracker.MainActivity
import com.example.expensetracker.R
import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.databinding.BottomSheetAddExpenseBinding
import com.example.expensetracker.utils.ExpenseCategoryHelper
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private var selectedCategory: String = "Other"
    private val categoryViewsList = ArrayList<Pair<String, LinearLayout>>()

    private val categoriesData = listOf(
        "Food", "Transport", "Health", "Shopping",
        "Bills", "Entertainment", "Education", "Coffee", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogNoShadow)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddExpenseBinding.inflate(inflater, container, false)

        setupCategoryGrid()
        setupDefaultValues()
        setupDatePicker()
        setupFieldsTextWatcher()

        binding.etAmount.filters = arrayOf(InputFilter.LengthFilter(12))
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        saveExpense()

        return binding.root
    }

    private fun setupFieldsTextWatcher() {
        val formWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSaveButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etAmount.addTextChangedListener(formWatcher)
        binding.etTitle.addTextChangedListener(formWatcher)
        updateSaveButtonState()
    }

    private fun updateSaveButtonState() {
        val amountInput = binding.etAmount.text.toString().trim()
        val titleInput = binding.etTitle.text.toString().trim()

        if (amountInput.isNotEmpty() && titleInput.isNotEmpty()) {
            binding.btnSaveExpense.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A1612"))
            binding.btnSaveExpense.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            binding.btnSaveExpense.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EAE4D3"))
            binding.btnSaveExpense.setTextColor(Color.parseColor("#9C968A"))
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        bottomSheet?.let { sheet ->
            sheet.setBackgroundColor(Color.TRANSPARENT)
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            sheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        (activity as? MainActivity)?.setNavigationAndFabVisibility(visible = true)
    }

    private fun setupCategoryGrid() {
        binding.categoryGrid.removeAllViews()
        categoryViewsList.clear()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val totalMargin = dpToPx(72f)
        val itemWidth = (screenWidth - totalMargin) / 3

        categoriesData.forEach { catName ->
            val cellView = layoutInflater.inflate(R.layout.item_category_grid, binding.categoryGrid, false) as LinearLayout
            val params = GridLayout.LayoutParams().apply {
                width = itemWidth
                height = dpToPx(58f)
                setMargins(dpToPx(4f), dpToPx(4f), dpToPx(4f), dpToPx(4f))
            }
            cellView.layoutParams = params

            val titleTxt = cellView.findViewById<TextView>(R.id.txtCategoryName)
            val iconImg = cellView.findViewById<ImageView>(R.id.imgCategoryIcon)
            titleTxt.text = catName

            when (catName) {
                "Food" -> iconImg.setImageResource(R.drawable.ic_food)
                "Transport" -> iconImg.setImageResource(R.drawable.ic_transport)
                "Health" -> iconImg.setImageResource(R.drawable.ic_health)
                "Shopping" -> iconImg.setImageResource(R.drawable.ic_shopping)
                "Bills" -> iconImg.setImageResource(R.drawable.ic_bills)
                "Entertainment" -> iconImg.setImageResource(R.drawable.ic_entertainment)
                "Education" -> iconImg.setImageResource(R.drawable.ic_education)
                "Coffee" -> iconImg.setImageResource(R.drawable.ic_coffee)
                else -> iconImg.setImageResource(R.drawable.ic_other)
            }
            cellView.setOnClickListener { selectCategoryItem(catName) }

            binding.categoryGrid.addView(cellView)
            categoryViewsList.add(Pair(catName, cellView))
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

            val cleanKey = if (currentCatName.contains("&")) {
                currentCatName.split("&")[0].trim()
            } else currentCatName

            val baseColorStr = ExpenseCategoryHelper.getCategoryColor(cleanKey)
            val baseColor = Color.parseColor(baseColorStr)

            if (currentCatName == selectedCategory) {
                val activeDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = getCornerRadiusPx()
                    setColor(baseColor)
                    val hsv = FloatArray(3)
                    Color.colorToHSV(baseColor, hsv)
                    hsv[2] *= 0.7f
                    setStroke(dpToPx(1.5f), Color.HSVToColor(hsv))
                }
                catView.background = activeDrawable
                val hsv = FloatArray(3)
                Color.colorToHSV(baseColor, hsv)
                hsv[2] *= 0.5f
                val deepColor = Color.HSVToColor(hsv)
                titleTxt.setTextColor(deepColor)
                iconImg.imageTintList = ColorStateList.valueOf(deepColor)
            } else {
                val inactiveDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = getCornerRadiusPx()
                    setColor(Color.parseColor("#FFFDF9"))
                }
                catView.background = inactiveDrawable
                titleTxt.setTextColor(Color.parseColor("#1A1612"))
                iconImg.imageTintList = ColorStateList.valueOf(Color.parseColor("#1A1612"))
            }
        }
    }

    private fun dpToPx(dp: Float): Int {
        val metrics = context?.resources?.displayMetrics ?: return dp.toInt()
        return (dp * metrics.density).toInt()
    }

    private fun getCornerRadiusPx(): Float {
        val metrics = context?.resources?.displayMetrics ?: return 12f
        return 12f * metrics.density
    }

    private fun setupDefaultValues() {
        if (expense == null) {
            // ── ADD MODE ──
            binding.txtSheetTitle.text = "Add expense"
            binding.btnSaveExpense.text = "Add expense"
            binding.btnDeleteExpense.visibility = View.GONE

            selectCategoryItem("Other")
            val currentDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                .format(Calendar.getInstance().time)
            binding.etDate.text = currentDate

        } else {
        binding.txtSheetTitle.text = "Edit expense"
        binding.btnSaveExpense.text = "Save"
        binding.etTitle.setText(expense.title)
        binding.etAmount.setText(expense.amount.toString())
        binding.etDate.text = expense.date

        binding.btnDeleteExpense.visibility = View.VISIBLE

        binding.btnDeleteExpense.setOnClickListener {
            expenseViewModel.deleteExpense(expense)
            Toast.makeText(requireContext(), "Expense Deleted", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        selectCategoryItem(expense.category)
    }
    }

    private fun setupDatePicker() {
        binding.btnDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                R.style.DarkSummaryDatePickerTheme,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val displayCalendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                    }
                    binding.etDate.text = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                        .format(displayCalendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
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
                expenseViewModel.insertExpense(
                    ExpenseEntity(
                        title = title,
                        amount = amount.toDouble(),
                        category = selectedCategory.trim().replaceFirstChar { it.uppercase() },
                        date = date,
                        note = ""
                    )
                )
                Toast.makeText(requireContext(), "Expense Saved", Toast.LENGTH_SHORT).show()
            } else {
                expenseViewModel.updateExpense(
                    expense.copy(
                        title = title,
                        amount = amount.toDouble(),
                        category = selectedCategory.trim().replaceFirstChar { it.uppercase() },
                        date = date
                    )
                )
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