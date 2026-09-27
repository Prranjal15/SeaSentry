package com.seasentry.app.location

import com.seasentry.app.geofence.AlertLevel

/**
 * Represents the current operational state of the standalone hardware GPS receiver.
 * Offline-capable: Uses direct GPS_PROVIDER with zero internet/A-GPS dependencies.
 */
enum class GpsFixState {
    /** GPS receiver is inactive or stopped */
    IDLE,

    /** ACCESS_FINE_LOCATION permission has not been granted by user */
    PERMISSION_DENIED,

    /** Device GPS hardware sensor is disabled in system settings */
    GPS_DISABLED,

    /** Hardware receiver searching for satellite lock under open sky (cold start ~30s-3min) */
    ACQUIRING,

    /** Active satellite lock with valid live coordinates */
    FIXED
}

/**
 * Operating mode for vessel position tracking.
 * DEMO: Uses synthetic waypoint playback (MockLocationProvider) for demonstrations.
 * LIVE_GPS: Uses real standalone hardware GPS_PROVIDER directly from satellites.
 */
enum class TrackingMode {
    DEMO,
    LIVE_GPS
}

/**
 * Raw location snapshot emitted by [RealLocationProvider].
 */
data class RealLocationState(
    val fixState: GpsFixState = GpsFixState.IDLE,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speedKnots: Float? = null,
    val headingDegrees: Float? = null,
    val accuracyMeters: Float? = null,
    val seedFixAgeMs: Long? = null,
    val lastFixTimestamp: Long? = null
) {
    val hasCoordinates: Boolean
        get() = latitude != null && longitude != null

    val isSeedFix: Boolean
        get() = seedFixAgeMs != null
}

/**
 * High-level UI state for live GPS tracking, evaluated against maritime boundary geofences.
 */
data class LiveTrackingUiState(
    val fixState: GpsFixState = GpsFixState.IDLE,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val speedKnots: Double = 0.0,
    val headingDegrees: Double = 0.0,
    val accuracyMeters: Float? = null,
    val seedFixAgeMs: Long? = null,
    val lastFixTimestamp: Long? = null,
    val distanceMeters: Double = 0.0,
    val formattedDistance: String = "--",
    val alertLevel: AlertLevel = AlertLevel.SAFE,
    val isBreached: Boolean = false,
    val actionRequired: String = "Maintain standard navigation course.",
    val isTracking: Boolean = false,
    val isCriticalScreenActive: Boolean = false,
    val hasMutedCriticalAlert: Boolean = false
) {
    val hasValidFix: Boolean
        get() = fixState == GpsFixState.FIXED && latitude != null && longitude != null
}
