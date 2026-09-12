package com.xcloak.xfile.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.core.preferences.AppTheme
import com.xcloak.xfile.ui.components.AdvancedGlassCard
import com.xcloak.xfile.ui.components.GlassCard
import com.xcloak.xfile.ui.theme.ThemeViewModel
import java.io.File

import com.xcloak.xfile.ui.components.ProBadge
import com.xcloak.xfile.core.billing.BillingManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPro: () -> Unit,
    billingManager: BillingManager,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val currentTheme by viewModel.themeState.collectAsState()
    val isPro by billingManager.isPro.collectAsState()
    val context = LocalContext.current

    // FIX: "Clear Cache" showed a hardcoded "12.4 MB" and its onClick was empty — it
    // never measured anything and never actually cleared anything. This reads the real
    // cacheDir size and recomputes it after an actual delete.
    var cacheSize by remember { mutableLongStateOf(dirSize(context.cacheDir)) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
        } catch (e: Exception) {
            "—"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (isPro) {
                Spacer(modifier = Modifier.width(16.dp))
                ProBadge()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pro Banner
        if (!isPro) {
            AdvancedGlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToPro
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Go Pro", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp)
                        Text("Remove ads & unlock all tools", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Pro Status Active", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Lifetime access unlocked", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        SettingsSectionHeader("General")

        // NOTE: a true selectable Light theme isn't wired up yet — most screens in this
        // app hardcode Color.White text on dark glass, so switching the color scheme
        // alone would make text unreadable on a light background. Cycles Dark <-> Follow
        // System until per-screen colors are pulled from MaterialTheme.colorScheme.
        SettingsRow(
            icon = Icons.Default.Palette,
            title = "Appearance",
            subtitle = when (currentTheme) {
                AppTheme.DARK -> "Dark"
                AppTheme.SYSTEM -> "Follow System"
                AppTheme.LIGHT -> "Light"
            },
            onClick = { showThemeDialog = true }
        )

        // FIX: was a fake, unclickable "/Documents/XFile" — doesn't match where Save
        // actually writes (Downloads/XFile via MediaStore on Android 10+, or app storage
        // on older versions — see FileActions.save). Shown as informational text now,
        // not a dead button.
        SettingsRow(
            icon = Icons.Default.Folder,
            title = "Default Save Location",
            subtitle = "Downloads/XFile",
            interactive = false
        )

        SettingsRow(
            icon = Icons.Default.DeleteSweep,
            title = "Clear Cache",
            subtitle = formatSize(cacheSize),
            onClick = { showClearCacheDialog = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        SettingsSectionHeader("Support")

        SettingsRow(
            icon = Icons.Default.Info,
            title = "About XFile",
            subtitle = "Version $versionName",
            onClick = { showAboutDialog = true }
        )
        SettingsRow(
            icon = Icons.Default.PrivacyTip,
            title = "Privacy Policy",
            subtitle = "Your data stays on your device",
            onClick = { showPrivacyDialog = true }
        )

        SettingsRow(
            icon = Icons.Default.Share,
            title = "Share XFile",
            subtitle = "Spread the word",
            onClick = { shareApp(context) }
        )

        SettingsRow(
            icon = Icons.Default.RateReview,
            title = "Rate XFile",
            subtitle = "Support us on the Play Store",
            onClick = { rateApp(context) }
        )

        SettingsRow(
            icon = Icons.Default.Email,
            title = "Send Feedback",
            subtitle = "Report bugs or suggest features",
            onClick = { sendFeedback(context) }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeOption("Dark", currentTheme == AppTheme.DARK) {
                        viewModel.setTheme(AppTheme.DARK)
                        showThemeDialog = false
                    }
                    ThemeOption("Light", currentTheme == AppTheme.LIGHT) {
                        viewModel.setTheme(AppTheme.LIGHT)
                        showThemeDialog = false
                    }
                    ThemeOption("Follow System", currentTheme == AppTheme.SYSTEM) {
                        viewModel.setTheme(AppTheme.SYSTEM)
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear cache?") },
            text = { Text("This deletes ${formatSize(cacheSize)} of temporary processing files. Anything already saved or shared is unaffected.") },
            confirmButton = {
                TextButton(onClick = {
                    clearDir(context.cacheDir)
                    cacheSize = dirSize(context.cacheDir)
                    showClearCacheDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("XFile") },
            text = { Text("Version $versionName\n\nLocal PDF and image tools by XCloak. Every tool runs entirely on your device — files are never uploaded to a server.") },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy") },
            text = {
                Text(
                    "XFile processes every file entirely on your device. Files you select are " +
                            "never uploaded, transmitted, or sent to any server — merging, splitting, " +
                            "compressing, resizing, and metadata removal all happen locally. Processed " +
                            "files are kept in this app's storage until you delete them from the Files tab " +
                            "or clear the cache in Settings."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    interactive: Boolean = true,
    onClick: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = if (interactive) onClick else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                }
            }
            // FIX: every row used to render this chevron regardless of whether tapping
            // it did anything — "Default Save Location" looked exactly as tappable as
            // "Appearance" even though nothing happened when you tapped it.
            if (interactive) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun shareApp(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "XFile - Local PDF & Image Tools")
        putExtra(Intent.EXTRA_TEXT, "Check out XFile, a powerful tool to process your files entirely on your device: https://play.google.com/store/apps/details?id=${context.packageName}")
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun rateApp(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback for browser
        context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()))
    }
}

private fun sendFeedback(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf("idiot63666@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "XFile Feedback")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // No email client
    }
}

private fun dirSize(dir: File): Long {
    if (!dir.exists()) return 0L
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

private fun clearDir(dir: File) {
    dir.listFiles()?.forEach { it.deleteRecursively() }
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}