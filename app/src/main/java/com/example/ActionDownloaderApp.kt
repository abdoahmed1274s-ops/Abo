package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.repository.DownloadRepository

class ActionDownloaderApp : Application() {

    lateinit var repository: DownloadRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = DownloadRepository(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Action Downloader Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for active and completed media downloads"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "action_downloader_channel"
    }
}
