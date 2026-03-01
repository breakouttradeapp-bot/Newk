package com.kundaliai.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.RotateAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.kundaliai.app.ads.AdManager
import com.kundaliai.app.databinding.ActivitySplashBinding
import com.kundaliai.app.ui.home.HomeActivity
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        initAdMob()

        scope.launch {
            delay(2500)
            navigateToHome()
        }
    }

    private fun setupAnimations() {
        // Fade in app name
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1200
            fillAfter = true
        }
        binding.tvAppName.startAnimation(fadeIn)

        // Delayed tagline fade
        val taglineFade = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 600
            fillAfter = true
        }
        binding.tvTagline.startAnimation(taglineFade)

        // Rotate zodiac ring
        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 4000
            repeatCount = Animation.INFINITE
            fillAfter = true
        }
        binding.ivZodiacRing.startAnimation(rotate)
    }

    private fun initAdMob() {
        AdManager.getInstance().initialize(this)
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
