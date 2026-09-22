package com.xcloak.xfile.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingManager: com.xcloak.xfile.core.billing.BillingManager
) {
    companion object {
        private val IS_DEBUG = com.xcloak.xfile.BuildConfig.DEBUG

        // Production Ad Units from User
        private const val PROD_BANNER = "ca-app-pub-5043960817552570/8058963118"
        private const val PROD_INTERSTITIAL = "ca-app-pub-5043960817552570/6441639432"
        private const val PROD_REWARDED = "ca-app-pub-5043960817552570/2949116988"

        // Google Test Ad Units
        private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

        val BANNER_ID = if (IS_DEBUG) TEST_BANNER else PROD_BANNER
        val INTERSTITIAL_ID = if (IS_DEBUG) TEST_INTERSTITIAL else PROD_INTERSTITIAL
    }

    private var interstitialAd: InterstitialAd? = null

    fun loadInterstitial(activity: Activity) {
        if (billingManager.isPro.value) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onComplete: () -> Unit) {
        if (billingManager.isPro.value) {
            onComplete()
            return
        }

        if (interstitialAd != null) {
            interstitialAd?.show(activity)
            interstitialAd = null
            onComplete()
        } else {
            onComplete()
        }
    }
}
