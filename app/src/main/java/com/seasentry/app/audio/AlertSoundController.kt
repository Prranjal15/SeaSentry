package com.seasentry.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RawRes
import com.seasentry.app.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Safe logging helper that outputs to android.util.Log in Android runtime
 * and falls back to console in JVM unit tests without throwing unmocked exceptions.
 */
internal object AudioLogger {
    fun d(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (_: Throwable) {
            println("DEBUG: [$tag] $msg")
        }
    }

    fun i(tag: String, msg: String) {
        try {
            Log.i(tag, msg)
        } catch (_: Throwable) {
            println("INFO: [$tag] $msg")
        }
    }

    fun w(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) Log.w(tag, msg, tr) else Log.w(tag, msg)
        } catch (_: Throwable) {
            println("WARN: [$tag] $msg ${tr?.message ?: ""}")
        }
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) Log.e(tag, msg, tr) else Log.e(tag, msg)
        } catch (_: Throwable) {
            System.err.println("ERROR: [$tag] $msg ${tr?.message ?: ""}")
        }
    }
}

/**
 * Interface abstraction for audio playback and focus management.
 * Enables zero-dependency JVM unit testing.
 */
interface AudioPlayerEngine {
    fun requestAudioFocus(): Boolean
    fun abandonAudioFocus()
    suspend fun playClip(@RawRes resId: Int, isSpeech: Boolean, maxDurationMs: Long? = null)
    fun stopActiveClip()
    fun release()
}

/**
 * Android system implementation of [AudioPlayerEngine] utilizing [MediaPlayer] and [AudioManager].
 */
class AndroidAudioPlayerEngine(private val context: Context) : AudioPlayerEngine {

    companion object {
        private const val TAG = "AndroidAudioEngine"
    }

    private val audioManager: AudioManager? by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    private var focusRequest: AudioFocusRequest? = null
    private var activePlayer: MediaPlayer? = null
    private val playerLock = Any()

    override fun requestAudioFocus(): Boolean {
        return try {
            val am = audioManager ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { /* Handled gracefully */ }
                    .build()
                focusRequest = request
                am.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Throwable) {
            AudioLogger.e(TAG, "Error requesting audio focus", e)
            false
        }
    }

    override fun abandonAudioFocus() {
        try {
            val am = audioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
                focusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (e: Throwable) {
            AudioLogger.w(TAG, "Error abandoning audio focus", e)
        }
    }

    override suspend fun playClip(
        @RawRes resId: Int,
        isSpeech: Boolean,
        maxDurationMs: Long?
    ) {
        val contentType = if (isSpeech) {
            AudioAttributes.CONTENT_TYPE_SPEECH
        } else {
            AudioAttributes.CONTENT_TYPE_SONIFICATION
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(contentType)
            .build()

        var player: MediaPlayer? = null
        try {
            player = try {
                MediaPlayer.create(context, resId, audioAttributes, 0)
                    ?: MediaPlayer.create(context, resId)
            } catch (e: Throwable) {
                AudioLogger.e(TAG, "Failed to instantiate MediaPlayer for resId=$resId", e)
                null
            }

            if (player == null) {
                AudioLogger.e(TAG, "Unable to load audio resource $resId - skipping clip")
                return
            }

            synchronized(playerLock) {
                activePlayer = player
            }

            val playSuspend = suspendCancellableCoroutine<Unit> { continuation ->
                player.setOnCompletionListener {
                    if (continuation.isActive) continuation.resume(Unit)
                }
                player.setOnErrorListener { mp, what, extra ->
                    AudioLogger.e(TAG, "MediaPlayer error on resId=$resId: what=$what extra=$extra")
                    if (continuation.isActive) continuation.resume(Unit)
                    true
                }
                continuation.invokeOnCancellation {
                    try {
                        if (player.isPlaying) player.stop()
                        player.release()
                    } catch (_: Throwable) {}
                }
                try {
                    player.setVolume(1.0f, 1.0f)
                    player.isLooping = false
                    player.start()
                } catch (e: Throwable) {
                    AudioLogger.e(TAG, "Error starting MediaPlayer for resId=$resId", e)
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }

            if (maxDurationMs != null && maxDurationMs > 0) {
                withTimeoutOrNull(maxDurationMs) {
                    playSuspend
                }
            } else {
                playSuspend
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            AudioLogger.e(TAG, "Exception during playClip for resId=$resId", e)
        } finally {
            synchronized(playerLock) {
                if (activePlayer == player) {
                    activePlayer = null
                }
            }
            try {
                if (player?.isPlaying == true) {
                    player.stop()
                }
                player?.release()
            } catch (_: Throwable) {}
        }
    }

    override fun stopActiveClip() {
        synchronized(playerLock) {
            activePlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                } catch (e: Throwable) {
                    AudioLogger.w(TAG, "Error stopping active clip player", e)
                }
            }
            activePlayer = null
        }
    }

    override fun release() {
        stopActiveClip()
        abandonAudioFocus()
    }
}

/**
 * Controls the alert cycle for critical boundary warnings:
 * Plays the spoken voice alert (in the selected language) -> short gap -> siren alarm (emergency_alert.wav, max 4s) -> short gap -> repeat.
 *
 * Guarantees:
 * - Single active playback at any time (no overlapping sounds or stacked loops).
 * - Immediate mid-clip termination on stop()/release()/acknowledge.
 * - Audio focus acquisition and USAGE_ALARM attributes for maximum audibility.
 * - Resilient error handling (never crashes or gets stuck silent on bad clips).
 */
class AlertSoundController(
    private val engine: AudioPlayerEngine,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    companion object {
        private const val TAG = "AlertSoundController"
        const val INTER_CLIP_GAP_MS = 750L
        const val EMERGENCY_ALERT_MAX_DURATION_MS = 4000L
    }

    constructor(context: Context) : this(
        engine = AndroidAudioPlayerEngine(context.applicationContext),
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    )

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var cycleJob: Job? = null
    private val lock = Any()

    /**
     * Starts the alert cycle with the requested spoken voice language.
     * Idempotent: will not restart or stack players if already playing.
     */
    fun start(language: AlertLanguage = AlertLanguage.DEFAULT) {
        synchronized(lock) {
            if (_isPlaying.value) {
                AudioLogger.d(TAG, "AlertSoundController is already running, ignoring duplicate start request.")
                return
            }
            _isPlaying.value = true
            engine.requestAudioFocus()

            cycleJob = scope.launch {
                try {
                    while (isActive) {
                        // 1. Spoken voice alert in the selected language (played completely)
                        try {
                            engine.playClip(
                                resId = language.rawResId,
                                isSpeech = true,
                                maxDurationMs = null
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            AudioLogger.e(TAG, "Error playing voice clip for ${language.code}", e)
                        }

                        if (!isActive) break
                        delay(INTER_CLIP_GAP_MS)
                        if (!isActive) break

                        // 2. Siren / TING emergency alert (max 4.0s)
                        try {
                            engine.playClip(
                                resId = R.raw.emergency_alert,
                                isSpeech = false,
                                maxDurationMs = EMERGENCY_ALERT_MAX_DURATION_MS
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            AudioLogger.e(TAG, "Error playing emergency siren", e)
                        }

                        if (!isActive) break
                        delay(INTER_CLIP_GAP_MS)
                    }
                } catch (e: CancellationException) {
                    // Expected on stop() or cancellation
                } catch (e: Throwable) {
                    AudioLogger.e(TAG, "Unexpected error in alert sound cycle", e)
                } finally {
                    engine.stopActiveClip()
                    engine.abandonAudioFocus()
                    _isPlaying.value = false
                }
            }
        }
    }

    /**
     * Immediately stops audio playback mid-clip, cancels the cycle, and abandons audio focus.
     */
    fun stop() {
        synchronized(lock) {
            if (!_isPlaying.value && cycleJob == null) {
                return
            }
            _isPlaying.value = false
            cycleJob?.cancel()
            cycleJob = null
            engine.stopActiveClip()
            engine.abandonAudioFocus()
        }
    }

    /**
     * Releases all media and coroutine resources.
     */
    fun release() {
        stop()
        engine.release()
        try {
            scope.cancel()
        } catch (_: Throwable) {}
    }
}
