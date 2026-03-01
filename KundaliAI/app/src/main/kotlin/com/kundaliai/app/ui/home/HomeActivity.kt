package com.kundaliai.app.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.kundaliai.app.R
import com.kundaliai.app.databinding.ActivityHomeBinding
import com.kundaliai.app.language.LanguageManager
import com.kundaliai.app.ui.form.KundaliFormActivity
import com.kundaliai.app.ui.webview.WebViewActivity
import com.kundaliai.app.utils.Constants

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        languageManager = LanguageManager(this)
        setupUI()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Generate Kundali button
        binding.btnGenerateKundali.setOnClickListener {
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                startActivity(Intent(this, KundaliFormActivity::class.java))
            }.start()
        }

        // Daily Rashifal button
        binding.btnDailyRashifal.setOnClickListener {
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                startActivity(Intent(this, KundaliFormActivity::class.java).apply {
                    putExtra("mode", "rashifal")
                })
            }.start()
        }

        // Premium PDF button
        binding.btnUnlockPremium.setOnClickListener {
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                // Launch form first, then billing on result screen
                startActivity(Intent(this, KundaliFormActivity::class.java).apply {
                    putExtra("auto_unlock", true)
                })
            }.start()
        }

        // Privacy policy
        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", Constants.PRIVACY_POLICY_URL)
                putExtra("title", getString(R.string.privacy_policy))
            })
        }

        // Terms
        binding.tvTerms.setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java).apply {
                putExtra("url", Constants.TERMS_URL)
                putExtra("title", getString(R.string.terms_conditions))
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language -> {
                showLanguageDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLanguageDialog() {
        val options = languageManager.getLanguageOptions()
        val displayNames = options.map { it.second }.toTypedArray()
        val currentLang = languageManager.getSavedLanguage()
        val currentIndex = options.indexOfFirst { it.first == currentLang }.coerceAtLeast(0)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(displayNames, currentIndex) { dialog, which ->
                val selected = options[which].first
                languageManager.saveLanguage(selected)
                dialog.dismiss()
                recreate()
            }
            .show()
    }
}
