package com.seasentry.app

import com.seasentry.app.boundary.BoundaryRepository
import com.seasentry.app.boundary.LatLng
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

    @Test
    fun testInlineGeoJsonNearestPointAndDistanceCalculation() {
        // Sample GeoJSON containing:
        // - A LineString feature with 2 points: [lon 80.0, lat 10.0] and [lon 80.0, lat 10.01]
        // - A LineString feature with 1 segment point: [lon 80.0, lat 10.05]
        // - A Maldives feature that should be filtered out by BoundaryRepository
        val sampleGeoJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [
                      [80.0, 10.0],
                      [80.0, 10.01]
                    ]
                  },
                  "properties": {
                    "line_id": 101,
                    "line_name": "Sri Lanka - India",
                    "length_km": 1.11
                  }
                },
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [
                      [80.0, 10.05]
                    ]
                  },
                  "properties": {
                    "line_id": 102,
                    "line_name": "India - Bangladesh",
                    "length_km": 5.56
                  }
                },
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [
                      [73.0, 3.0]
                    ]
                  },
                  "properties": {
                    "line_id": 103,
                    "line_name": "Maldives - India",
                    "length_km": 10.0
                  }
                }
              ]
            }
        """.trimIndent()

        val repository = BoundaryRepository()
        val polylines = repository.parseGeoJson(sampleGeoJson)

        // Verify Maldives was filtered out: exactly 2 polylines remain
        assertEquals(2, polylines.size)

        // Verify lat/lon are not swapped: GeoJSON [80.0, 10.0] -> LatLng(lat=10.0, lon=80.0)
        val line1 = polylines[0]
        assertEquals(2, line1.size)
        assertEquals(10.0, line1[0].latitude, 0.000001)
        assertEquals(80.0, line1[0].longitude, 0.000001)
        assertEquals(10.01, line1[1].latitude, 0.000001)
        assertEquals(80.0, line1[1].longitude, 0.000001)

        val line2 = polylines[1]
        assertEquals(1, line2.size)
        assertEquals(10.05, line2[0].latitude, 0.000001)
        assertEquals(80.0, line2[0].longitude, 0.000001)

        // Create GeofenceEngine with these parsed polylines
        val multiPointEngine = GeofenceEngine(boundaryLines = polylines)

        // Hand-verifiable test:
        // Vessel at (lat = 10.0090, lon = 80.0000)
        // Distance to (10.0000, 80.0) = delta 0.0090° ≈ 1000.75 m
        // Distance to (10.0100, 80.0) = delta 0.0010° ≈ 111.19 m (NEAREST POINT)
        // Distance to (10.0500, 80.0) = delta 0.0410° ≈ 4558.99 m
        val (nearestPoint, nearestDist) = multiPointEngine.findNearestPoint(10.0090, 80.0000)
        assertEquals(10.01, nearestPoint.latitude, 0.000001)
        assertEquals(80.00, nearestPoint.longitude, 0.000001)
        assertEquals(111.195, nearestDist, 0.1) // Hand-calculated: 0.001 * (pi/180) * 6371000 = 111.1949m

        // Evaluating position should trigger CRITICAL alert (distance 111.2m <= 200m)
        val evalResult = multiPointEngine.evaluatePosition(10.0090, 80.0000)
        assertEquals(AlertLevel.CRITICAL, evalResult.alertLevel)
        assertEquals(10.01, evalResult.targetLat, 0.000001)
        assertEquals(80.00, evalResult.targetLon, 0.000001)
        assertEquals(111.195, evalResult.distanceMeters, 0.1)
    }

    @Test
    fun testMultiLineStringGeoJsonParsing() {
        val multiLineStringJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "MultiLineString",
                    "coordinates": [
                      [
                        [79.5333, 9.1],
                        [79.5216, 9.0]
                      ],
                      [
                        [80.05, 10.0833],
                        [79.5833, 9.95]
                      ]
                    ]
                  },
                  "properties": {
                    "line_id": 1307,
                    "line_name": "Sri Lanka - India"
                  }
                }
              ]
            }
        """.trimIndent()

        val repository = BoundaryRepository()
        val polylines = repository.parseGeoJson(multiLineStringJson)

        assertEquals(2, polylines.size)
        assertEquals(2, polylines[0].size)
        assertEquals(2, polylines[1].size)

        // Check first point of line 0: [79.5333, 9.1] -> lat 9.1, lon 79.5333
        assertEquals(9.1, polylines[0][0].latitude, 0.0001)
        assertEquals(79.5333, polylines[0][0].longitude, 0.0001)

        // Check first point of line 1: [80.05, 10.0833] -> lat 10.0833, lon 80.05
        assertEquals(10.0833, polylines[1][0].latitude, 0.0001)
        assertEquals(80.05, polylines[1][0].longitude, 0.0001)
    }
}
