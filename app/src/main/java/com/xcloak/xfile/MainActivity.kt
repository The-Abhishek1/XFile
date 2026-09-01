package com.xcloak.xfile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.xcloak.xfile.ui.MainScreen
import com.xcloak.xfile.ui.theme.XFileTheme
import com.xcloak.xfile.ui.theme.ThemeViewModel
import com.xcloak.xfile.core.preferences.AppTheme
import com.xcloak.xfile.core.billing.BillingManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject lateinit var billingManager: BillingManager
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by themeViewModel.themeState.collectAsState()
            val darkTheme = when (themeState) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            XFileTheme(darkTheme = darkTheme) {
                MainScreen(
                    onUpgradeClick = { activity ->
                        billingManager.purchasePro(activity)
                    }
                )
            }
        }
    }
}
