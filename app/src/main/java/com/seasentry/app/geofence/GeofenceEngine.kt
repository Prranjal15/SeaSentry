package com.seasentry.app.geofence

import com.seasentry.app.boundary.LatLng
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
    val boundaryLines: List<List<LatLng>> = GeofenceConfig.DEFAULT_BOUNDARY_LINES,
    private val advisoryThreshold: Double = GeofenceConfig.ADVISORY_THRESHOLD_METERS,
    private val warningThreshold: Double = GeofenceConfig.WARNING_THRESHOLD_METERS,
    private val criticalThreshold: Double = GeofenceConfig.CRITICAL_THRESHOLD_METERS
) {
    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0 // WGS84 mean spherical earth radius
        private const val METERS_PER_LAT_DEGREE_APPROX = 110000.0
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
     * Finds the nearest boundary point to the vessel coordinates across all boundary polylines.
     * Returns a Pair of the nearest LatLng point and the distance in meters.
     */
    fun findNearestPoint(
        vesselLat: Double,
        vesselLon: Double,
        lines: List<List<LatLng>> = boundaryLines
    ): Pair<LatLng, Double> {
        var minDistance = Double.MAX_VALUE
        var nearest: LatLng? = null

        for (line in lines) {
            for (point in line) {
                // Fast bounding-box check to avoid costly trigonometric calls on distant points
                if (minDistance != Double.MAX_VALUE &&
                    Math.abs(vesselLat - point.latitude) * METERS_PER_LAT_DEGREE_APPROX > minDistance
                ) {
                    continue
                }

                val dist = calculateDistance(vesselLat, vesselLon, point.latitude, point.longitude)
                if (dist < minDistance) {
                    minDistance = dist
                    nearest = point
                }
            }
        }

        val point = nearest ?: LatLng(GeofenceConfig.DEFAULT_IMBL_LAT, GeofenceConfig.DEFAULT_IMBL_LON)
        val distance = if (minDistance == Double.MAX_VALUE) {
            calculateDistance(vesselLat, vesselLon, point.latitude, point.longitude)
        } else {
            minDistance
        }

        return Pair(point, distance)
    }

    /**
     * Evaluates a vessel's current position against the maritime boundary lines or a custom boundary.
     */
    fun evaluatePosition(
        vesselLat: Double,
        vesselLon: Double,
        customBoundary: MaritimeBoundary? = null
    ): GeofenceResult {
        val targetLat: Double
        val targetLon: Double
        val boundaryName: String
        val distance: Double

        if (customBoundary != null) {
            targetLat = customBoundary.latitude
            targetLon = customBoundary.longitude
            boundaryName = customBoundary.name
            distance = calculateDistance(vesselLat, vesselLon, targetLat, targetLon)
        } else {
            val (nearestPoint, dist) = findNearestPoint(vesselLat, vesselLon, boundaryLines)
            targetLat = nearestPoint.latitude
            targetLon = nearestPoint.longitude
            boundaryName = GeofenceConfig.DEFAULT_IMBL_NAME
            distance = dist
        }

        val bearing = calculateBearing(vesselLat, vesselLon, targetLat, targetLon)

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
            boundaryName = boundaryName,
            targetLat = targetLat,
            targetLon = targetLon,
            bearingDegrees = bearing
        )
    }
}
