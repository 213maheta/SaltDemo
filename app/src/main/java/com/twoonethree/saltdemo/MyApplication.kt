package com.twoonethree.saltdemo

import android.app.Application
import com.twoonethree.saltdemo.koinsetup.genericModule
import com.twoonethree.saltdemo.koinsetup.networkModule
import com.twoonethree.saltdemo.koinsetup.roomModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(
                networkModule,
                roomModule,
                genericModule,
            )
        }
    }
}