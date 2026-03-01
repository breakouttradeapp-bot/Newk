package com.kundaliai.app.ads

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import com.google.android.gms.ads.*
import com.kundaliai.app.BuildConfig

class AdManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: AdManager? = null

        fun getInstance(): AdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdManager().also { INSTANCE = it }
            }
        }
    }

    private var isInitialized = false

    // ── Initialize AdMob SDK ─────────────────────────────────────────────────
    fun initialize(context: Context, onComplete: (() -> Unit)? = null) {
        if (isInitialized) {
            onComplete?.invoke()
            return
        }

        MobileAds.initialize(context) {
            isInitialized = true
            // Configure test devices if in debug
            if (BuildConfig.DEBUG) {
                val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
                val configuration = RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
                MobileAds.setRequestConfiguration(configuration)
            }
            onComplete?.invoke()
        }
    }

    // ── Load Banner Ad ───────────────────────────────────────────────────────
    fun loadBannerAd(activity: Activity, container: LinearLayout) {
        if (!isInitialized) return

        val adView = AdView(activity).apply {
            adUnitId = BuildConfig.ADMOB_BANNER_ID
            setAdSize(AdSize.BANNER)
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    // Silently fail - don't disturb UX
                    container.removeAllViews()
                }
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    // Ad loaded successfully
                }
            }
        }

        container.removeAllViews()
        container.addView(adView)
        adView.loadAd(buildAdRequest())
    }

    // ── Build Ad Request ─────────────────────────────────────────────────────
    private fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
