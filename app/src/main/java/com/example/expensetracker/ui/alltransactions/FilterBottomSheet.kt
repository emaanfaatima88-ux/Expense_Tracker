package com.example.expensetracker.ui.alltransactions

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.expensetracker.databinding.BottomSheetFilterBinding
import com.example.expensetracker.utils.ExpenseCategoryHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class FilterBottomSheet(
    private val currentCategory: String,
    private val currentSortOrder: String,
    private val currentDateRange: String,
    private val onFilterApplied: (category: String, sortOrder: String, dateRange: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory = currentCategory
    private var selectedSortOrder = currentSortOrder
    private var selectedDateRange = currentDateRange

    private val categories = listOf(
        "All",
        "Food & Drink",
        "Transport",
        "Health",
        "Shopping",
        "Bills",
        "Entertainment",
        "Education",
        "Coffee",
        "Others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let { sheet ->
            sheet.setBackgroundColor(Color.TRANSPARENT)
            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            sheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSortUi()
        setupDateRangeUi()
        buildCategoryChips()
        setupButtons()

        binding.btnCloseFilter.setOnClickListener {
            dismiss()
        }

        binding.btnResetFilter.setOnClickListener {
            onFilterApplied("All", "newest", "all")
            dismiss()
        }

        binding.btnApplyFilter.setOnClickListener {
            onFilterApplied(
                selectedCategory,
                selectedSortOrder,
                selectedDateRange
            )
            dismiss()
        }
    }

    /**
     * 🛠️ FIXED: Material Buttons modify properties safely to ensure
     * the Reset button honors its transparent layout attributes.
     */
    private fun setupButtons() {
        // Keeps the primary apply button matching your layout's core color profile
        binding.btnApplyFilter.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A1612"))

        // REMOVED the programmatic solid background initialization for btnResetFilter
        // so it successfully preserves its transparent XML configuration!
    }

    private fun setupSortUi() {
        val sortMap = mapOf(
            "newest" to binding.chipSortNewest,
            "oldest" to binding.chipSortOldest,
            "highest" to binding.chipSortHighest,
            "lowest" to binding.chipSortLowest
        )

        sortMap.forEach { (order, view) ->
            updatePillState(view, selectedSortOrder == order)

            view.setOnClickListener {
                selectedSortOrder = order
                sortMap.forEach { (key, tv) ->
                    updatePillState(tv, selectedSortOrder == key)
                }
            }
        }
    }

    private fun setupDateRangeUi() {
        val dateMap = mapOf(
            "all" to binding.chipDateAll,
            "month" to binding.chipDateMonth,
            "30days" to binding.chipDate30Days,
            "7days" to binding.chipDate7Days
        )

        dateMap.forEach { (range, view) ->
            updatePillState(view, selectedDateRange == range)

            view.setOnClickListener {
                selectedDateRange = range
                dateMap.forEach { (key, tv) ->
                    updatePillState(tv, selectedDateRange == key)
                }
            }
        }
    }

    private fun updatePillState(textView: TextView, isSelected: Boolean) {
        val density = resources.displayMetrics.density

        textView.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f * density

            if (isSelected) {
                setColor(Color.parseColor("#17120F"))
            } else {
                setColor(Color.parseColor("#FFFDF9"))
                setStroke((1f * density).toInt(), Color.parseColor("#E9E1D5"))
            }
        }

        textView.setTextColor(
            Color.parseColor(if (isSelected) "#FFFDF9" else "#1A1612")
        )
    }

    private fun buildCategoryChips() {
        binding.filterCategoryChipGroup.removeAllViews()

        val context = requireContext()
        val density = resources.displayMetrics.density

        categories.forEach { rawCategory ->
            val chip = Chip(context).apply {
                text = rawCategory
                isCheckable = true
                isCheckedIconVisible = false
                isCloseIconVisible = false
                textSize = 12f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                chipMinHeight = 36f * density
                chipCornerRadius = 100f * density
                setEnsureMinTouchTargetSize(false)
                chipStrokeWidth = 1f * density

                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#E9E1D5"))
                chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#FFFDF9"))
                setTextColor(Color.parseColor("#1A1612"))

                textStartPadding = 4f * density
                textEndPadding = 10f * density

                if (rawCategory != "All") {
                    val cleanKey = if (rawCategory.contains("&")) rawCategory.split("&")[0].trim() else rawCategory
                    val sharpColor = Color.parseColor(ExpenseCategoryHelper.getStatisticsColor(cleanKey))

                    val dotDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(sharpColor)
                        setSize((7 * density).toInt(), (7 * density).toInt())
                    }

                    chipIcon = dotDrawable
                    chipIconSize = 7f * density
                    iconStartPadding = 10f * density
                    textStartPadding = 6f * density
                }

                if (rawCategory.equals(selectedCategory, true)) {
                    isChecked = true
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#17120F"))
                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#17120F"))
                    setTextColor(Color.parseColor("#FFFDF9"))
                }

                setOnClickListener {
                    if (selectedCategory != rawCategory) {
                        selectedCategory = rawCategory
                        buildCategoryChips()
                    }
                }
            }
            binding.filterCategoryChipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}