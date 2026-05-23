package com.example.expensetracker

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.expensetracker.databinding.ActivityMainBinding
import com.example.expensetracker.ui.addexpense.AddExpenseBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupFab()
        setupInsets()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun setupFab() {
        binding.fabAddExpense.setOnClickListener {
            // SAFE CHECK: Only show the bottom sheet if it isn't already added/visible on screen
            if (supportFragmentManager.findFragmentByTag("AddExpenseBottomSheet") == null) {
                AddExpenseBottomSheet().show(
                    supportFragmentManager,
                    "AddExpenseBottomSheet"
                )
            }
        }
    }

    private fun setupInsets() {
        // Fixes bottom system bar overlapping across all Android versions (API 9 to 16)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationView) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // Adjust the layout margins safely without shrinking the inner menu text size
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = dpToPx(14) + navigationBars.bottom
            view.layoutParams = layoutParams

            // Adjust FAB cleanly alongside the bottom bar changes
            val fabParams = binding.fabAddExpense.layoutParams as ViewGroup.MarginLayoutParams
            fabParams.bottomMargin = dpToPx(100) + navigationBars.bottom
            binding.fabAddExpense.layoutParams = fabParams

            insets
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}