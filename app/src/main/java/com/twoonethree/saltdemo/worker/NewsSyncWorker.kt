package com.twoonethree.saltdemo.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.twoonethree.saltdemo.data.remote.network.NetworkResult
import com.twoonethree.saltdemo.data.repository.NewsRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

open class NewsSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val newsRepository: NewsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "NewsSyncWorker"
        const val CHANNEL_ID = "breaking_news_channel"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return when (val result = newsRepository.fetchTopHeadlines(category = "general", page = 1)) {
            is NetworkResult.Success -> {
                if (result.data > 0) {
                    showBreakingNewsNotification()
                }
                Result.success()
            }
            is NetworkResult.Failure -> {
                Result.retry()
            }
        }
    }

    internal open fun showBreakingNewsNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Breaking News",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily updates for latest news articles"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val encodedUrl = URLEncoder.encode("https://newsapi.org", StandardCharsets.UTF_8.toString())
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("newsapp://article?articleUrl=$encodedUrl")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Articles Available")
            .setContentText("Check out the latest headlines in Discover feed")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
