package com.xcloak.xfile

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class XFileApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Mobile Ads SDK
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@XFileApplication) {}
        }
    }
}
