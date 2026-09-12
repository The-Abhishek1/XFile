package com.xcloak.xfile.core.update

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.xcloak.xfile.core.notification.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            _updateState.value = UpdateState.Downloaded
            notificationHelper.showDownloadNotification(
                "Update Downloaded",
                "Restart the app to apply the latest update."
            )
        } else if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            _updateState.value = UpdateState.Downloading(bytesDownloaded, totalBytesToDownload)
        }
    }

    init {
        appUpdateManager.registerListener(listener)
    }

    fun checkForUpdates(activity: Activity) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("UpdateManager", "Update flow failed", e)
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _updateState.value = UpdateState.Downloaded
            }
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(listener)
    }

    companion object {
        const val UPDATE_REQUEST_CODE = 1234
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    data class Downloading(val progress: Long, val total: Long) : UpdateState()
    object Downloaded : UpdateState()
}
