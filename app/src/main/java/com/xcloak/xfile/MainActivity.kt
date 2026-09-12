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

import com.xcloak.xfile.core.update.UpdateManager
import com.xcloak.xfile.core.update.UpdateState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.os.Build

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject lateinit var billingManager: BillingManager
    @Inject lateinit var updateManager: UpdateManager
    private val themeViewModel: ThemeViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        updateManager.checkForUpdates(this)
        checkNotificationPermission()

        setContent {
            val themeState by themeViewModel.themeState.collectAsState()
            val darkTheme = when (themeState) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            val updateState by updateManager.updateState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(updateState) {
                if (updateState is UpdateState.Downloaded) {
                    val result = snackbarHostState.showSnackbar(
                        message = "Update downloaded. Restart to install.",
                        actionLabel = "Restart",
                        duration = androidx.compose.material3.SnackbarDuration.Indefinite
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        updateManager.completeUpdate()
                    }
                }
            }

            XFileTheme(darkTheme = darkTheme) {
                MainScreen(
                    billingManager = billingManager,
                    snackbarHostState = snackbarHostState,
                    onUpgradeClick = { activity ->
                        billingManager.purchasePro(activity)
                    }
                )
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateManager.onDestroy()
    }
}
