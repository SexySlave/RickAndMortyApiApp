package org.sexyslave.app
import android.app.Application
import cafe.adriel.voyager.core.registry.ScreenRegistry

import cafe.adriel.voyager.koin.getScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.sexyslave.app.di.appModule


import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        println("КОИН ИНИЦИАЛИЗИРУЕТСЯ!")
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}