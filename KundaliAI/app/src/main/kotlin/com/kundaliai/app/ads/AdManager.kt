package com.kundaliai.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
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

    // ─────────────────────────────────────────────────────────────
    // Initialize AdMob (Crash Safe)
    // ─────────────────────────────────────────────────────────────
    fun initialize(context: Context, onComplete: (() -> Unit)? = null) {
        try {
            if (isInitialized) {
                onComplete?.invoke()
                return
            }

            MobileAds.initialize(context) { _: InitializationStatus ->
                isInitialized = true
                onComplete?.invoke()
            }

        } catch (e: Exception) {
            Log.e("AdManager", "AdMob initialization failed", e)
            onComplete?.invoke()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Load Banner Ad (Fully Crash Safe)
    // ─────────────────────────────────────────────────────────────
    fun loadBannerAd(activity: Activity, container: LinearLayout) {

        // Prevent crash if not initialized
        if (!isInitialized) return

        // Prevent crash if banner ID not defined
        if (BuildConfig.ADMOB_BANNER_ID.isBlank()) {
            Log.w("AdManager", "ADMOB_BANNER_ID is blank. Skipping ad load.")
            return
        }

        try {
            val adView = AdView(activity).apply {
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                setAdSize(AdSize.BANNER)
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdManager", "Ad failed to load: ${error.message}")
                        container.removeAllViews()
                    }

                    override fun onAdLoaded() {
                        Log.d("AdManager", "Ad loaded successfully")
                    }
                }
            }

            container.removeAllViews()
            container.addView(adView)
            adView.loadAd(AdRequest.Builder().build())

        } catch (e: Exception) {
            Log.e("AdManager", "Error loading banner ad", e)
        }
    }
}
