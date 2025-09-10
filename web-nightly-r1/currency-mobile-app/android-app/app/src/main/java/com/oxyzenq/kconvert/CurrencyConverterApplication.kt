/*
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KconvertApplication : Application() {
    
    companion object {
        lateinit var instance: KconvertApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
