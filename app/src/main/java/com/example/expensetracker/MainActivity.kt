package com.example.expensetracker

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
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
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        setupNavigation()
        setupFab()
        setupInsets()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigation()
        }
    }

    /**
     * Requirement 2: Immersive Sticky Hide Mode for system navigation buttons
     */
    private fun hideSystemNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    )
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        // ✅ FIX: Pop back to home when home tab is tapped
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menu = binding.bottomNavigationView.menu

            menu.findItem(R.id.homeFragment).setIcon(R.drawable.ic_home)
            menu.findItem(R.id.transactionHistoryFragment).setIcon(R.drawable.ic_history)
            menu.findItem(R.id.statisticsFragment).setIcon(R.drawable.ic_stat)
            menu.findItem(R.id.settingsFragment).setIcon(R.drawable.ic_settings)

            when (destination.id) {
                R.id.homeFragment -> menu.findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_filled)
                R.id.transactionHistoryFragment -> menu.findItem(R.id.transactionHistoryFragment).setIcon(R.drawable.ic_history)
                R.id.statisticsFragment -> menu.findItem(R.id.statisticsFragment).setIcon(R.drawable.ic_stat_filled)
                R.id.settingsFragment -> menu.findItem(R.id.settingsFragment).setIcon(R.drawable.ic_settings_filled)
            }
        }
    }

    private fun setupFab() {
        binding.fabAddExpense.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag("AddExpenseBottomSheet") == null) {
                AddExpenseBottomSheet().show(
                    supportFragmentManager,
                    "AddExpenseBottomSheet"
                )
            }
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            // TOP STATUS BAR FIX
            val fragmentParams =
                binding.navHostFragment.layoutParams as ViewGroup.MarginLayoutParams
            fragmentParams.topMargin = statusBarInsets.top
            binding.navHostFragment.layoutParams = fragmentParams

            // ✅ REMOVED: binding.bottomNavigationContainer (no longer exists in XML)
            // The nav bar handles its own padding now

            insets
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}