package com.xcloak.xfile.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_WORK = "tool_work_completed"
        const val CHANNEL_ID_DOWNLOAD = "downloads"
        const val NOTIFICATION_ID_WORK = 101
        const val NOTIFICATION_ID_DOWNLOAD = 102
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameWork = "Tool Work"
            val descriptionWork = "Notifications for tool completion"
            val importanceWork = NotificationManager.IMPORTANCE_DEFAULT
            val channelWork = NotificationChannel(CHANNEL_ID_WORK, nameWork, importanceWork).apply {
                description = descriptionWork
            }

            val nameDownload = "Downloads"
            val descriptionDownload = "Notifications for app updates and downloads"
            val importanceDownload = NotificationManager.IMPORTANCE_DEFAULT
            val channelDownload = NotificationChannel(CHANNEL_ID_DOWNLOAD, nameDownload, importanceDownload).apply {
                description = descriptionDownload
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelWork)
            notificationManager.createNotificationChannel(channelDownload)
        }
    }

    fun showWorkCompletedNotification(toolName: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_WORK)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Tool Work Completed")
            .setContentText("Your $toolName is ready!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_WORK, builder.build())
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }

    fun showDownloadNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOAD)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_DOWNLOAD, builder.build())
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }
}
