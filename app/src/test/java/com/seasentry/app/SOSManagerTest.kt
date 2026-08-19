package com.seasentry.app

import com.seasentry.app.sos.SOSManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SOSManagerTest {

    @Test
    fun testSOSTriggerAndResolution() = runTest {
        val testScope = TestScope()
        val sosManager = SOSManager(testScope)

        assertFalse(sosManager.sosState.value.isDistressActive)

        // Trigger SOS
        sosManager.triggerSOS(
            senderId = "SEASENTRY-TEST-1",
            latitude = 18.9220,
            longitude = 72.8347,
            message = "Distress test beacon"
        )

        val triggered = sosManager.sosState.value
        assertTrue(triggered.isDistressActive)
        assertNotNull(triggered.currentSOSEvent)
        assertEquals("SEASENTRY-TEST-1", triggered.currentSOSEvent?.senderId)

        // Resolve SOS
        sosManager.resolveSOS()
        val resolved = sosManager.sosState.value
        assertFalse(resolved.isDistressActive)
    }
}
