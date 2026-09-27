package com.seasentry.app.location

import android.content.Context
import com.seasentry.app.data.AlertDao
import com.seasentry.app.data.AlertEvent
import com.seasentry.app.geofence.AlertLevel
import com.seasentry.app.geofence.GeofenceEngine
import com.seasentry.app.geofence.GeofenceResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Live GPS tracking controller.
 *
 * Sourced directly from standalone hardware [RealLocationProvider] (LocationManager.GPS_PROVIDER)
 * with zero network/cellular/Wi-Fi dependencies for true offline maritime operation.
 *
 * Evaluates live coordinates in real-time against maritime geofences via [GeofenceEngine]
 * and logs boundary alert transitions to [AlertDao].
 */
class LiveTrackingController(
    private val scope: CoroutineScope,
    val locationProvider: RealLocationProvider,
    private val alertDao: AlertDao? = null,
    val geofenceEngine: GeofenceEngine = GeofenceEngine()
) {
    /**
     * Secondary constructor for standard Android lifecycle instantiation with [Context].
     */
    constructor(
        context: Context,
        scope: CoroutineScope,
        alertDao: AlertDao? = null,
        geofenceEngine: GeofenceEngine = GeofenceEngine()
    ) : this(
        scope = scope,
        locationProvider = RealLocationProvider(context),
        alertDao = alertDao,
        geofenceEngine = geofenceEngine
    )

    private var previousAlertLevel: AlertLevel = AlertLevel.SAFE
    private val _isCriticalScreenActive = MutableStateFlow(false)
    private val _hasMutedCriticalAlert = MutableStateFlow(false)
    private val _isTracking = MutableStateFlow(false)

    private val _liveTrackingUiState = MutableStateFlow(buildCurrentUiState())
    val liveTrackingUiState: StateFlow<LiveTrackingUiState> = _liveTrackingUiState.asStateFlow()

    init {
        scope.launch {
            locationProvider.locationState.collect { locState ->
                val lat = locState.latitude
                val lon = locState.longitude
                if (lat != null && lon != null) {
                    val geoResult = geofenceEngine.evaluatePosition(lat, lon)
                    checkAlertTransition(geoResult, lat, lon)
                }
                refreshUiState()
            }
        }
    }

    private fun buildCurrentUiState(): LiveTrackingUiState {
        val loc = locationProvider.locationState.value
        val lat = loc.latitude
        val lon = loc.longitude

        val geoResult = if (lat != null && lon != null) {
            geofenceEngine.evaluatePosition(lat, lon)
        } else {
            null
        }

        val actionRequired = when {
            geoResult != null -> geoResult.actionRequired
            loc.fixState == GpsFixState.PERMISSION_DENIED -> "Location permission required for live satellite tracking."
            loc.fixState == GpsFixState.GPS_DISABLED -> "Enable GPS in system settings for direct satellite navigation."
            loc.fixState == GpsFixState.ACQUIRING -> "Acquiring GPS satellite signal under open sky..."
            loc.fixState == GpsFixState.IDLE -> "GPS tracking is on standby."
            else -> "Maintain standard navigation course."
        }

        return LiveTrackingUiState(
            fixState = loc.fixState,
            latitude = lat,
            longitude = lon,
            speedKnots = loc.speedKnots?.toDouble() ?: 0.0,
            headingDegrees = loc.headingDegrees?.toDouble() ?: 0.0,
            accuracyMeters = loc.accuracyMeters,
            seedFixAgeMs = loc.seedFixAgeMs,
            lastFixTimestamp = loc.lastFixTimestamp,
            distanceMeters = geoResult?.distanceMeters ?: 0.0,
            formattedDistance = geoResult?.formattedDistance ?: "--",
            alertLevel = geoResult?.alertLevel ?: AlertLevel.SAFE,
            isBreached = geoResult?.isBreached ?: false,
            actionRequired = actionRequired,
            isTracking = _isTracking.value,
            isCriticalScreenActive = _isCriticalScreenActive.value,
            hasMutedCriticalAlert = _hasMutedCriticalAlert.value
        )
    }

    private fun refreshUiState() {
        _liveTrackingUiState.value = buildCurrentUiState()
    }

    private fun checkAlertTransition(result: GeofenceResult, lat: Double, lon: Double) {
        val currentLevel = result.alertLevel
        if (currentLevel != previousAlertLevel) {
            val oldLevel = previousAlertLevel
            previousAlertLevel = currentLevel

            // Only log if progressing to an advisory/warning/critical alert tier
            if (currentLevel != AlertLevel.SAFE && currentLevel.isMoreSevereThan(oldLevel)) {
                logAlertEvent(currentLevel, lat, lon)
            }

            // If entering critical for the first time in this run, show critical screen
            if (currentLevel == AlertLevel.CRITICAL && !_hasMutedCriticalAlert.value) {
                _isCriticalScreenActive.value = true
            }
        }
    }

    private fun logAlertEvent(tier: AlertLevel, lat: Double, lon: Double) {
        val title = when (tier) {
            AlertLevel.CRITICAL -> "IMBL Border Breached"
            AlertLevel.WARNING -> "Boundary Approach Warning"
            AlertLevel.ADVISORY -> "Boundary Proximity Advisory"
            AlertLevel.SAFE -> "Zone Normal"
        }

        val event = AlertEvent(
            tier = tier.severityTag,
            latitude = lat,
            longitude = lon,
            timestamp = System.currentTimeMillis(),
            title = title,
            details = "Live vessel approached within threshold: ${tier.title}"
        )

        alertDao?.let { dao ->
            scope.launch {
                dao.insertAlert(event)
            }
        }
    }

    fun startTracking(minIntervalMs: Long = 1000L, minDistanceMeters: Float = 0f) {
        _isTracking.value = true
        _hasMutedCriticalAlert.value = false
        locationProvider.start(minIntervalMs, minDistanceMeters)
        refreshUiState()
    }

    fun stopTracking() {
        _isTracking.value = false
        locationProvider.stop()
        refreshUiState()
    }

    fun refreshGpsStatus() {
        locationProvider.refreshAvailability()
        refreshUiState()
    }

    fun dismissCriticalAlert() {
        _isCriticalScreenActive.value = false
        _hasMutedCriticalAlert.value = true
        refreshUiState()
    }

    fun openCriticalScreen() {
        _hasMutedCriticalAlert.value = false
        _isCriticalScreenActive.value = true
        refreshUiState()
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    fun isGpsEnabled(): Boolean = locationProvider.isGpsEnabled()
}
