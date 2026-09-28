package com.seasentry.app.audio

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the user's preferred alert voice language in SharedPreferences.
 * Exposes a reactive [StateFlow] and guarantees persistence across application restarts.
 */
class AlertLanguageStore(
    private val prefs: SharedPreferences
) {
    companion object {
        const val PREFS_NAME = "seasentry_audio_prefs"
        const val KEY_ALERT_LANGUAGE = "alert_language"
    }

    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _selectedLanguage = MutableStateFlow(loadPersistedLanguage())
    val selectedLanguage: StateFlow<AlertLanguage> = _selectedLanguage.asStateFlow()

    private fun loadPersistedLanguage(): AlertLanguage {
        val code = prefs.getString(KEY_ALERT_LANGUAGE, null)
        return AlertLanguage.fromCode(code)
    }

    fun getLanguage(): AlertLanguage = _selectedLanguage.value

    fun setLanguage(language: AlertLanguage) {
        _selectedLanguage.value = language
        prefs.edit().putString(KEY_ALERT_LANGUAGE, language.code).apply()
    }
}
