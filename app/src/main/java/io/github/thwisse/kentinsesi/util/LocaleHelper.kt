package io.github.thwisse.kentinsesi.util

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper class for managing app locale/language preferences.
 * Supports Turkish, English and System default.
 */
object LocaleHelper {

    private const val PREF_NAME = "language_pref"
    private const val KEY_LANGUAGE = "app_language"
    
    // Language codes
    const val LANGUAGE_TURKISH = "tr"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_SYSTEM = "system"

    /**
     * Get the persisted language preference.
     * Returns "system" if no preference is set.
     */
    fun getPersistedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    /**
     * Persist the selected language preference.
     */
    fun setPersistedLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).commit() // commit() for synchronous write before restart
    }

    /**
     * Get the effective locale to use.
     * If "system" is selected, returns the system's default locale.
     * Otherwise returns the persisted language locale.
     */
    fun getEffectiveLocale(context: Context): Locale {
        val language = getPersistedLanguage(context)
        return when (language) {
            LANGUAGE_SYSTEM -> getSystemLocale()
            LANGUAGE_TURKISH -> Locale("tr")
            LANGUAGE_ENGLISH -> Locale("en")
            else -> getSystemLocale()
        }
    }

    /**
     * Get system locale, defaulting to English if not Turkish.
     * This ensures we fall back to English for any non-Turkish locale.
     */
    private fun getSystemLocale(): Locale {
        val systemLocale = Locale.getDefault()
        return if (systemLocale.language == "tr") {
            Locale("tr")
        } else {
            Locale("en")
        }
    }

    /**
     * Apply locale to the context.
     * This should be called in attachBaseContext of Activity.
     */
    fun applyLocale(context: Context): Context {
        val locale = getEffectiveLocale(context)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Update locale and restart the app.
     * Call this when user changes language preference.
     */
    fun setLocaleAndRestart(context: Context, language: String) {
        setPersistedLanguage(context, language)
        restartApp(context)
    }

    /**
     * Restart the application to apply new locale.
     */
    private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        
        // Kill current process
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    /**
     * Get current language display name.
     */
    fun getCurrentLanguageDisplayName(context: Context): String {
        return when (getPersistedLanguage(context)) {
            LANGUAGE_TURKISH -> context.getString(io.github.thwisse.kentinsesi.R.string.language_turkish)
            LANGUAGE_ENGLISH -> context.getString(io.github.thwisse.kentinsesi.R.string.language_english)
            else -> context.getString(io.github.thwisse.kentinsesi.R.string.language_system)
        }
    }
}
