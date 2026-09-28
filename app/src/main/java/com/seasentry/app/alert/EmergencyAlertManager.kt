package com.seasentry.app.alert

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Manages haptic vibration for critical maritime emergency events.
 * Audible alerts are coordinated through [com.seasentry.app.audio.AlertSoundController].
 */
class EmergencyAlertManager(private val context: Context) {

    companion object {
        private const val TAG = "EmergencyAlertManager"

        // Emergency vibration waveform pattern:
        // Delay (0ms) -> Short Buzz (180ms) -> Pause (120ms) -> Strong Pulse (450ms) -> Pause (120ms) -> Short Buzz (180ms) -> Cycle Gap (500ms)
        private val VIBRATION_TIMINGS = longArrayOf(0, 180, 120, 450, 120, 180, 500)
        private val VIBRATION_AMPLITUDES = intArrayOf(0, 255, 0, 255, 0, 255, 0)
        private const val REPEAT_INDEX = 0 // Repeat continuously from beginning
    }

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                @Suppress("DEPRECATION")
                manager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Vibrator", e)
            null
        }
    }

    private var isVibrationActive: Boolean = false
    private val lock = Any()

    /**
     * Starts the emergency strong vibration pattern.
     * Idempotent: will not restart or create duplicate instances if already active.
     */
    fun startEmergencyAlert() {
        startVibration()
    }

    /**
     * Stops and silences all active emergency vibration.
     */
    fun stopEmergencyAlert() {
        stopVibration()
    }

    fun startVibration() {
        synchronized(lock) {
            if (isVibrationActive) {
                Log.d(TAG, "Emergency vibration already active, ignoring duplicate start request.")
                return
            }
            isVibrationActive = true
            Log.i(TAG, "🚨 Triggering CRITICAL emergency vibration pattern")

            val v = vibrator ?: return
            if (!v.hasVibrator()) {
                Log.w(TAG, "Device does not have vibration hardware.")
                return
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = if (v.hasAmplitudeControl()) {
                        VibrationEffect.createWaveform(VIBRATION_TIMINGS, VIBRATION_AMPLITUDES, REPEAT_INDEX)
                    } else {
                        VibrationEffect.createWaveform(VIBRATION_TIMINGS, REPEAT_INDEX)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val attributes = VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_ALARM)
                            .build()
                        v.vibrate(effect, attributes)
                    } else {
                        @Suppress("DEPRECATION")
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        @Suppress("DEPRECATION")
                        v.vibrate(effect, audioAttributes)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(VIBRATION_TIMINGS, REPEAT_INDEX)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to trigger vibration", e)
            }
        }
    }

    fun stopVibration() {
        synchronized(lock) {
            if (!isVibrationActive) {
                return
            }
            Log.i(TAG, "Halting emergency vibration (Mute / Acknowledge / Reset)")
            isVibrationActive = false
            try {
                vibrator?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "Error canceling vibration", e)
            }
        }
    }

    /**
     * Cleans up all resources.
     */
    fun release() {
        stopEmergencyAlert()
    }
}
