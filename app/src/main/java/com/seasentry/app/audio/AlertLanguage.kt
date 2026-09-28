package com.seasentry.app.audio

import androidx.annotation.RawRes
import com.seasentry.app.R

/**
 * Supported spoken voice alert languages for critical boundary warnings.
 * Maps each language to its bundled offline raw audio resource.
 */
enum class AlertLanguage(
    val code: String,
    val displayName: String,
    @get:RawRes val rawResId: Int
) {
    ENGLISH("en", "English", R.raw.sos_alert_en),
    HINDI("hi", "हिंदी (Hindi)", R.raw.sos_alert_hi),
    MARATHI("mr", "मराठी (Marathi)", R.raw.sos_alert_mr),
    TAMIL("ta", "தமிழ் (Tamil)", R.raw.sos_alert_ta);

    companion object {
        val DEFAULT = ENGLISH

        fun fromCode(code: String?): AlertLanguage {
            return entries.firstOrNull { it.code.equalsIgnoreCase(code) } ?: DEFAULT
        }

        fun fromDisplayName(name: String?): AlertLanguage {
            return entries.firstOrNull { it.displayName.equalsIgnoreCase(name) } ?: DEFAULT
        }

        fun fromName(name: String?): AlertLanguage {
            return entries.firstOrNull { it.name.equalsIgnoreCase(name) } ?: DEFAULT
        }

        private fun String.equalsIgnoreCase(other: String?): Boolean {
            return this.equals(other, ignoreCase = true)
        }
    }
}
