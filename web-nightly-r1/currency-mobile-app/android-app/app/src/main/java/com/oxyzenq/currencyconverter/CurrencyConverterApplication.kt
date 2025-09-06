package com.oxyzenq.currencyconverter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CurrencyConverterApplication : Application() {
    
    companion object {
        lateinit var instance: CurrencyConverterApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
