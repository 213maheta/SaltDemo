package com.twoonethree.saltdemo

import android.app.Application
import androidx.work.*
import com.twoonethree.saltdemo.di.genericModule
import com.twoonethree.saltdemo.di.networkModule
import com.twoonethree.saltdemo.di.roomModule
import com.twoonethree.saltdemo.worker.NewsSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class SaltApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SaltApp)
            workManagerFactory()
            modules(networkModule, roomModule, genericModule)
        }

        scheduleBackgroundSync()
    }

    private fun scheduleBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<NewsSyncWorker>(
            repeatInterval = 6,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NewsSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
