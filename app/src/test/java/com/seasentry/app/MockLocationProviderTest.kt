package com.seasentry.app

import com.seasentry.app.demo.MockLocationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockLocationProviderTest {

    @Test
    fun testWaypointsProgressionAndReset() = runTest {
        val testScope = TestScope()
        val provider = MockLocationProvider(testScope, stepIntervalMs = 10L)

        // Initial state
        val initial = provider.locationState.value
        assertEquals(0, initial.currentStep)
        assertEquals(18.9220, initial.latitude, 0.0001)
        assertEquals(72.8347, initial.longitude, 0.0001)
        assertFalse(initial.isRunning)
        assertFalse(initial.isCompleted)

        // Jump to step
        provider.jumpToStep(4)
        val step4 = provider.locationState.value
        assertEquals(4, step4.currentStep)
        assertEquals(18.9348, step4.latitude, 0.0001)

        // Reset
        provider.reset()
        val resetState = provider.locationState.value
        assertEquals(0, resetState.currentStep)
        assertEquals(18.9220, resetState.latitude, 0.0001)
    }

    @Test
    fun testDeterministicWaypointsCoverAllTiers() {
        val waypoints = MockLocationProvider.WAYPOINTS
        assertTrue("Should have at least 8 waypoints", waypoints.size >= 8)

        val first = waypoints.first()
        val last = waypoints.last()

        assertEquals(18.9220, first.latitude, 0.0001)
        assertEquals(18.9390, last.latitude, 0.0001)
    }
}
