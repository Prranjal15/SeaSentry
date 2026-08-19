package com.seasentry.app

import com.seasentry.app.geofence.AlertLevel
import com.seasentry.app.geofence.GeofenceConfig
import com.seasentry.app.geofence.GeofenceEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeofenceEngineTest {

    private lateinit var engine: GeofenceEngine

    @Before
    fun setUp() {
        engine = GeofenceEngine()
    }

    @Test
    fun testHaversineDistanceCalculationAccuracy() {
        // Known distance between Mumbai (18.9220, 72.8347) and Boundary point (18.9390, 72.8347)
        // delta lat = 0.017 degrees ≈ 1,890 meters
        val distance = engine.calculateDistance(18.9220, 72.8347, 18.9390, 72.8347)
        assertTrue("Distance should be approx 1890m, was $distance", distance in 1850.0..1920.0)
    }

    @Test
    fun testAlertLevelSafeZone() {
        // Vessel at start location (> 1.8km away)
        val result = engine.evaluatePosition(18.9220, 72.8347)
        assertEquals(AlertLevel.SAFE, result.alertLevel)
        assertFalse(result.isBreached)
        assertTrue(result.distanceMeters > 1000.0)
    }

    @Test
    fun testAlertLevelAdvisoryZone() {
        // Vessel at ~980m away from 18.9390 (delta = 0.0088 deg)
        val result = engine.evaluatePosition(18.9302, 72.8347)
        assertEquals(AlertLevel.ADVISORY, result.alertLevel)
        assertTrue(result.distanceMeters in 500.0..1000.0)
    }

    @Test
    fun testAlertLevelWarningZone() {
        // Vessel at ~470m away from 18.9390 (delta = 0.0042 deg)
        val result = engine.evaluatePosition(18.9348, 72.8347)
        assertEquals(AlertLevel.WARNING, result.alertLevel)
        assertTrue(result.distanceMeters in 200.0..500.0)
    }

    @Test
    fun testAlertLevelCriticalZone() {
        // Vessel at ~180m away from 18.9390 (delta = 0.0016 deg)
        val result = engine.evaluatePosition(18.9374, 72.8347)
        assertEquals(AlertLevel.CRITICAL, result.alertLevel)
        assertTrue(result.distanceMeters <= 200.0)
        assertEquals("TURN BOAT 180° SOUTH IMMEDIATELY", result.actionRequired)
    }

    @Test
    fun testBoundaryBreachDetection() {
        // Vessel right at boundary point
        val result = engine.evaluatePosition(18.9390, 72.8347)
        assertEquals(AlertLevel.CRITICAL, result.alertLevel)
        assertTrue(result.isBreached)
        assertTrue(result.distanceMeters <= 50.0)
    }
}
