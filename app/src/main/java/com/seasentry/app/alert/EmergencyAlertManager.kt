package com.seasentry.app.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.seasentry.app.R

/**
 * Manages audible alarms and haptic vibration for critical maritime emergency events.
 * Ensures bundled offline playback, continuous looping, and proper lifecycle management.
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

    private var mediaPlayer: MediaPlayer? = null
    private var isAlertActive: Boolean = false
    private val lock = Any()

    /**
     * Starts the emergency warning sound and strong vibration pattern.
     * Idempotent: will not restart or create duplicate instances if already active.
     */
    fun startEmergencyAlert() {
        synchronized(lock) {
            if (isAlertActive) {
                Log.d(TAG, "Emergency alert already active, ignoring duplicate start request.")
                return
            }
            isAlertActive = true
            Log.i(TAG, "🚨 Triggering CRITICAL emergency alert (Audio + Vibration)")

            startAudio()
            startVibration()
        }
    }

    /**
     * Stops and silences all emergency sounds and halts active vibration.
     */
    fun stopEmergencyAlert() {
        synchronized(lock) {
            if (!isAlertActive && mediaPlayer == null) {
                return
            }
            Log.i(TAG, "Silencing emergency alert (Mute / Acknowledge / Reset)")
            isAlertActive = false
            stopAudio()
            stopVibration()
        }
    }

    private fun startAudio() {
        try {
            stopAudio() // Ensure previous player is released

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val player = MediaPlayer.create(context, R.raw.emergency_alert, audioAttributes, 0)
                ?: MediaPlayer.create(context, R.raw.emergency_alert)

            player?.apply {
                isLooping = true
                setVolume(1.0f, 1.0f)
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    try {
                        mp.release()
                    } catch (_: Exception) {}
                    synchronized(lock) {
                        if (mediaPlayer == mp) mediaPlayer = null
                    }
                    true
                }
                start()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start emergency audio playback", e)
        }
    }

    private fun stopAudio() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing MediaPlayer", e)
            }
        }
        mediaPlayer = null
    }

    private fun startVibration() {
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

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Error canceling vibration", e)
        }
    }

    /**
     * Cleans up all resources.
     */
    fun release() {
        stopEmergencyAlert()
    }
}
