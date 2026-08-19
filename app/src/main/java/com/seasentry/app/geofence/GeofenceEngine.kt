package com.seasentry.app.geofence

import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeofenceResult(
    val alertLevel: AlertLevel,
    val distanceMeters: Double,
    val isBreached: Boolean,
    val actionRequired: String,
    val boundaryName: String,
    val targetLat: Double,
    val targetLon: Double,
    val bearingDegrees: Double = 0.0
) {
    val formattedDistance: String
        get() = if (distanceMeters >= 1000) {
            String.format(Locale.US, "%.2f km", distanceMeters / 1000.0)
        } else {
            String.format(Locale.US, "%d m", distanceMeters.toInt())
        }
}

class GeofenceEngine(
    private val boundary: MaritimeBoundary = GeofenceConfig.DEFAULT_BOUNDARY,
    private val advisoryThreshold: Double = GeofenceConfig.ADVISORY_THRESHOLD_METERS,
    private val warningThreshold: Double = GeofenceConfig.WARNING_THRESHOLD_METERS,
    private val criticalThreshold: Double = GeofenceConfig.CRITICAL_THRESHOLD_METERS
) {
    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0 // WGS84 mean spherical earth radius
    }

    /**
     * Calculates great-circle distance between two GPS coordinates using the Haversine formula.
     * Returns distance in meters.
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates initial bearing (heading) in degrees (0..360) from point 1 to point 2.
     */
    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        return (Math.toDegrees(theta) + 360.0) % 360.0
    }

    /**
     * Evaluates a vessel's current position against the maritime boundary.
     */
    fun evaluatePosition(
        vesselLat: Double,
        vesselLon: Double,
        customBoundary: MaritimeBoundary? = null
    ): GeofenceResult {
        val target = customBoundary ?: boundary
        val distance = calculateDistance(vesselLat, vesselLon, target.latitude, target.longitude)
        val bearing = calculateBearing(vesselLat, vesselLon, target.latitude, target.longitude)

        val alertLevel = when {
            distance <= criticalThreshold -> AlertLevel.CRITICAL
            distance <= warningThreshold -> AlertLevel.WARNING
            distance <= advisoryThreshold -> AlertLevel.ADVISORY
            else -> AlertLevel.SAFE
        }

        val isBreached = distance <= 50.0 // Near zero boundary breach threshold

        val action = when (alertLevel) {
            AlertLevel.CRITICAL -> "TURN BOAT 180° SOUTH IMMEDIATELY"
            AlertLevel.WARNING -> "WARNING: APPROACHING IMBL (~500M) - REDUCE SPEED & ALTER HEADING"
            AlertLevel.ADVISORY -> "ADVISORY: MARITIME BOUNDARY IN PROXIMITY (~1KM) - MONITOR NAVIC RADAR"
            AlertLevel.SAFE -> "NORMAL OPERATIONS - VESSEL IN AUTHORIZED WATERS"
        }

        return GeofenceResult(
            alertLevel = alertLevel,
            distanceMeters = distance,
            isBreached = isBreached,
            actionRequired = action,
            boundaryName = target.name,
            targetLat = target.latitude,
            targetLon = target.longitude,
            bearingDegrees = bearing
        )
    }
}
