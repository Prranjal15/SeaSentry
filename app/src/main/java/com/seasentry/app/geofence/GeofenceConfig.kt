package com.seasentry.app.geofence

data class MaritimeBoundary(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String = "International Maritime Boundary Line (IMBL)"
)

object GeofenceConfig {
    // Default reference boundary point in the Arabian Sea sector
    const val DEFAULT_IMBL_LAT = 18.9390
    const val DEFAULT_IMBL_LON = 72.8347
    const val DEFAULT_IMBL_NAME = "Sector Alpha IMBL"

    // Distance thresholds in meters
    const val ADVISORY_THRESHOLD_METERS = 1000.0  // ~1 km
    const val WARNING_THRESHOLD_METERS = 500.0    // ~500 m
    const val CRITICAL_THRESHOLD_METERS = 200.0   // ~200 m

    val DEFAULT_BOUNDARY = MaritimeBoundary(
        id = "IMBL-SEC-01",
        name = DEFAULT_IMBL_NAME,
        latitude = DEFAULT_IMBL_LAT,
        longitude = DEFAULT_IMBL_LON
    )
}
