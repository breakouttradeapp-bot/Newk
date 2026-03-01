package com.kundaliai.app.language

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import com.kundaliai.app.utils.Constants
import java.util.*

class LanguageManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun getSavedLanguage(): String {
        return prefs.getString(Constants.PREF_LANGUAGE, Constants.LANG_ENGLISH)
            ?: Constants.LANG_ENGLISH
    }

    fun saveLanguage(language: String) {
        prefs.edit().putString(Constants.PREF_LANGUAGE, language).apply()
    }

    fun applyLanguage(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getLanguageDisplayName(code: String): String = when (code) {
        Constants.LANG_HINDI -> "हिंदी"
        Constants.LANG_MARATHI -> "मराठी"
        else -> "English"
    }

    fun getLanguageOptions(): List<Pair<String, String>> = listOf(
        Constants.LANG_ENGLISH to "English",
        Constants.LANG_HINDI to "हिंदी",
        Constants.LANG_MARATHI to "मराठी"
    )
}
