package com.example.expensetracker

import android.graphics.Color
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
    private var isNavigationHidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#f6f1e8")
        window.navigationBarColor = Color.TRANSPARENT

        // Dark icons on light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Draw behind system bars + hide only nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }

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

    private fun hideSystemNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setEnterAnim(R.anim.nav_enter)
                .setExitAnim(R.anim.nav_exit)
                .setPopEnterAnim(R.anim.nav_enter)
                .setPopExitAnim(R.anim.nav_exit)
                .setLaunchSingleTop(true)
                .build()

            when (item.itemId) {
                R.id.homeFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                else -> {
                    navController.navigate(item.itemId, null, navOptions)
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

            if (destination.id == R.id.budgetFragment) {
                setNavigationAndFabVisibility(visible = false)
            } else {
                setNavigationAndFabVisibility(visible = true)

                when (destination.id) {
                    R.id.homeFragment -> {
                        menu.findItem(R.id.homeFragment).setIcon(R.drawable.ic_home_filled)
                    }
                    R.id.transactionHistoryFragment -> {
                        menu.findItem(R.id.transactionHistoryFragment).setIcon(R.drawable.ic_history)
                    }
                    R.id.statisticsFragment -> {
                        menu.findItem(R.id.statisticsFragment).setIcon(R.drawable.ic_stat_filled)
                    }
                    R.id.settingsFragment -> {
                        menu.findItem(R.id.settingsFragment).setIcon(R.drawable.ic_settings_filled)
                    }
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAddExpense.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag("AddExpenseBottomSheet") == null) {
                AddExpenseBottomSheet().show(supportFragmentManager, "AddExpenseBottomSheet")
            }
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val fragmentParams = binding.navHostFragment.layoutParams as ViewGroup.MarginLayoutParams
            if (fragmentParams.topMargin != statusBarInsets.top) {
                fragmentParams.topMargin = statusBarInsets.top
                binding.navHostFragment.layoutParams = fragmentParams
            }
            insets
        }
    }

    fun setNavigationAndFabVisibility(visible: Boolean) {
        if (isNavigationHidden == !visible) return
        isNavigationHidden = !visible

        val screenWidth = resources.displayMetrics.widthPixels
        val fabTargetTranslationX = (screenWidth / 2f) - dpToPx(30) - dpToPx(24)
        val fabTargetTranslationY = dpToPx(16).toFloat()

        if (visible) {
            // Slide bottom nav back up
            binding.bottomNavigationView.animate()
                .translationY(0f)
                .setDuration(250)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()

            // Return FAB to center
            binding.fabAddExpense.animate()
                .translationX(0f)
                .translationY(0f)
                .setDuration(250)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        } else {
            // Slide bottom nav down off screen
            binding.bottomNavigationView.animate()
                .translationY(binding.bottomNavigationView.height.toFloat())
                .setDuration(250)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .start()

            // Translate FAB to bottom-right corner
            binding.fabAddExpense.animate()
                .translationX(fabTargetTranslationX)
                .translationY(fabTargetTranslationY)
                .setDuration(250)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .start()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}