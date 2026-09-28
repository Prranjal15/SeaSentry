package com.seasentry.app.audio

import com.seasentry.app.R
import com.seasentry.app.auth.FakeSharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AlertLanguage mapping, AlertLanguageStore persistence,
 * and AlertSoundController state handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlertAudioTest {

    // ==========================================
    // 1. AlertLanguage Enum & Resource Mapping Tests
    // ==========================================

    @Test
    fun testAlertLanguageDefault() {
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.DEFAULT)
    }

    @Test
    fun testAlertLanguageRawResourceMapping() {
        assertEquals(R.raw.sos_alert_en, AlertLanguage.ENGLISH.rawResId)
        assertEquals(R.raw.sos_alert_hi, AlertLanguage.HINDI.rawResId)
        assertEquals(R.raw.sos_alert_mr, AlertLanguage.MARATHI.rawResId)
        assertEquals(R.raw.sos_alert_ta, AlertLanguage.TAMIL.rawResId)
    }

    @Test
    fun testAlertLanguageLookupHelpers() {
        // fromCode
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromCode("en"))
        assertEquals(AlertLanguage.HINDI, AlertLanguage.fromCode("HI"))
        assertEquals(AlertLanguage.MARATHI, AlertLanguage.fromCode("mr"))
        assertEquals(AlertLanguage.TAMIL, AlertLanguage.fromCode("ta"))
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromCode("unknown_code"))
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromCode(null))

        // fromDisplayName
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromDisplayName("English"))
        assertEquals(AlertLanguage.HINDI, AlertLanguage.fromDisplayName("हिंदी (Hindi)"))
        assertEquals(AlertLanguage.MARATHI, AlertLanguage.fromDisplayName("मराठी (Marathi)"))
        assertEquals(AlertLanguage.TAMIL, AlertLanguage.fromDisplayName("தமிழ் (Tamil)"))
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromDisplayName("NonExistent"))
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromDisplayName(null))

        // fromName
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromName("ENGLISH"))
        assertEquals(AlertLanguage.HINDI, AlertLanguage.fromName("hindi"))
        assertEquals(AlertLanguage.ENGLISH, AlertLanguage.fromName("UNKNOWN"))
    }

    // ==========================================
    // 2. AlertLanguageStore Persistence Tests
    // ==========================================

    @Test
    fun testAlertLanguageStoreDefault() {
        val fakePrefs = FakeSharedPreferences()
        val store = AlertLanguageStore(fakePrefs)

        assertEquals(AlertLanguage.ENGLISH, store.getLanguage())
        assertEquals(AlertLanguage.ENGLISH, store.selectedLanguage.value)
    }

    @Test
    fun testAlertLanguageStorePersistenceAndRestart() {
        val fakePrefs = FakeSharedPreferences()
        val store = AlertLanguageStore(fakePrefs)

        // Set to Marathi
        store.setLanguage(AlertLanguage.MARATHI)
        assertEquals(AlertLanguage.MARATHI, store.getLanguage())
        assertEquals(AlertLanguage.MARATHI, store.selectedLanguage.value)
        assertEquals("mr", fakePrefs.getString(AlertLanguageStore.KEY_ALERT_LANGUAGE, null))

        // Simulate app restart by constructing a new AlertLanguageStore with same SharedPreferences
        val restartedStore = AlertLanguageStore(fakePrefs)
        assertEquals(AlertLanguage.MARATHI, restartedStore.getLanguage())
        assertEquals(AlertLanguage.MARATHI, restartedStore.selectedLanguage.value)

        // Switch to Tamil
        restartedStore.setLanguage(AlertLanguage.TAMIL)
        assertEquals(AlertLanguage.TAMIL, restartedStore.getLanguage())
        assertEquals("ta", fakePrefs.getString(AlertLanguageStore.KEY_ALERT_LANGUAGE, null))
    }

    // ==========================================
    // 3. AlertSoundController Lifecycle & State Tests
    // ==========================================

    private class FakeAudioPlayerEngine : AudioPlayerEngine {
        var audioFocusRequested = false
        var audioFocusAbandoned = false
        var activeClipStopped = false
        var playedClips = mutableListOf<Pair<Int, Boolean>>()
        var shouldThrowOnPlay = false

        override fun requestAudioFocus(): Boolean {
            audioFocusRequested = true
            audioFocusAbandoned = false
            return true
        }

        override fun abandonAudioFocus() {
            audioFocusAbandoned = true
        }

        override suspend fun playClip(resId: Int, isSpeech: Boolean, maxDurationMs: Long?) {
            if (shouldThrowOnPlay) {
                throw RuntimeException("Simulated audio playback failure")
            }
            playedClips.add(Pair(resId, isSpeech))
        }

        override fun stopActiveClip() {
            activeClipStopped = true
        }

        override fun release() {
            stopActiveClip()
            abandonAudioFocus()
        }
    }

    @Test
    fun testControllerStartAndStop() = runTest {
        val fakeEngine = FakeAudioPlayerEngine()
        val controller = AlertSoundController(engine = fakeEngine, scope = backgroundScope)

        assertFalse(controller.isPlaying.value)

        // Start alert in Hindi
        controller.start(AlertLanguage.HINDI)
        assertTrue(controller.isPlaying.value)
        assertTrue(fakeEngine.audioFocusRequested)

        // Advance coroutine to execute voice clip and siren clip
        advanceTimeBy(1000)
        assertTrue(fakeEngine.playedClips.any { it.first == R.raw.sos_alert_hi && it.second })

        // Stop alert
        controller.stop()
        advanceTimeBy(100)
        assertFalse(controller.isPlaying.value)
        assertTrue(fakeEngine.audioFocusAbandoned)
        assertTrue(fakeEngine.activeClipStopped)
    }

    @Test
    fun testControllerStartTwiceDoesNotStack() = runTest {
        val fakeEngine = FakeAudioPlayerEngine()
        val controller = AlertSoundController(engine = fakeEngine, scope = backgroundScope)

        controller.start(AlertLanguage.TAMIL)
        assertTrue(controller.isPlaying.value)

        // Second call while already running should be a no-op / ignored
        controller.start(AlertLanguage.MARATHI)
        assertTrue(controller.isPlaying.value)

        advanceTimeBy(1000)
        // Only Tamil clip was scheduled as first voice alert
        assertTrue(fakeEngine.playedClips.any { it.first == R.raw.sos_alert_ta })

        controller.stop()
        advanceTimeBy(100)
        assertFalse(controller.isPlaying.value)
    }

    @Test
    fun testControllerResilienceOnError() = runTest {
        val fakeEngine = FakeAudioPlayerEngine().apply { shouldThrowOnPlay = true }
        val controller = AlertSoundController(engine = fakeEngine, scope = backgroundScope)

        // Should not crash even when player engine throws
        controller.start(AlertLanguage.ENGLISH)
        assertTrue(controller.isPlaying.value)

        advanceTimeBy(1000)
        controller.stop()
        advanceTimeBy(100)
        assertFalse(controller.isPlaying.value)
    }
}
