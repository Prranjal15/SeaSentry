package com.seasentry.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealLocationProviderTest {

    @Test
    fun testMpsToKnotsConversionConstant() {
        val mps = 10.0f
        val knots = mps * RealLocationProvider.MPS_TO_KNOTS
        assertEquals(19.438445f, knots, 0.0001f)
    }

    @Test
    fun testRealLocationStateProperties() {
        val emptyState = RealLocationState()
        assertEquals(GpsFixState.IDLE, emptyState.fixState)
        assertFalse(emptyState.hasCoordinates)
        assertFalse(emptyState.isSeedFix)
        assertNull(emptyState.latitude)
        assertNull(emptyState.longitude)

        val seededState = RealLocationState(
            fixState = GpsFixState.ACQUIRING,
            latitude = 18.9220,
            longitude = 72.8347,
            seedFixAgeMs = 15000L
        )
        assertTrue(seededState.hasCoordinates)
        assertTrue(seededState.isSeedFix)
        assertEquals(15000L, seededState.seedFixAgeMs)

        val fixedState = RealLocationState(
            fixState = GpsFixState.FIXED,
            latitude = 18.9220,
            longitude = 72.8347,
            speedKnots = 14.2f,
            headingDegrees = 180f,
            accuracyMeters = 4.0f,
            lastFixTimestamp = 1700000000000L
        )
        assertTrue(fixedState.hasCoordinates)
        assertFalse(fixedState.isSeedFix)
        assertEquals(14.2f, fixedState.speedKnots!!, 0.001f)
        assertEquals(180f, fixedState.headingDegrees!!, 0.001f)
        assertEquals(4.0f, fixedState.accuracyMeters!!, 0.001f)
    }

    @Test
    fun testLiveTrackingUiStateHasValidFix() {
        val idleUiState = LiveTrackingUiState(fixState = GpsFixState.IDLE)
        assertFalse(idleUiState.hasValidFix)

        val acquiringUiState = LiveTrackingUiState(
            fixState = GpsFixState.ACQUIRING,
            latitude = 18.9220,
            longitude = 72.8347
        )
        assertFalse(acquiringUiState.hasValidFix)

        val fixedUiState = LiveTrackingUiState(
            fixState = GpsFixState.FIXED,
            latitude = 18.9220,
            longitude = 72.8347
        )
        assertTrue(fixedUiState.hasValidFix)
    }
}
