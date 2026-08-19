package com.seasentry.app.demo

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

data class DemoUiState(
    val latitude: Double = 18.9220,
    val longitude: Double = 72.8347,
    val speedKnots: Double = 14.2,
    val headingDegrees: Double = 0.0,
    val distanceMeters: Double = 1889.0,
    val formattedDistance: String = "1.89 km",
    val alertLevel: AlertLevel = AlertLevel.SAFE,
    val isBreached: Boolean = false,
    val actionRequired: String = "Maintain standard navigation course.",
    val currentStep: Int = 0,
    val totalSteps: Int = 9,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val isCriticalScreenActive: Boolean = false,
    val hasMutedCriticalAlert: Boolean = false
)

class DemoController(
    private val scope: CoroutineScope,
    private val alertDao: AlertDao? = null,
    val geofenceEngine: GeofenceEngine = GeofenceEngine(),
    stepIntervalMs: Long = 1500L
) {
    val locationProvider = MockLocationProvider(scope, stepIntervalMs)

    private var previousAlertLevel: AlertLevel = AlertLevel.SAFE
    private val _isCriticalScreenActive = MutableStateFlow(false)
    private val _hasMutedCriticalAlert = MutableStateFlow(false)

    private val _demoUiState = MutableStateFlow(buildCurrentUiState())
    val demoUiState: StateFlow<DemoUiState> = _demoUiState.asStateFlow()

    init {
        scope.launch {
            locationProvider.locationState.collect { locState ->
                val geoResult = geofenceEngine.evaluatePosition(locState.latitude, locState.longitude)
                checkAlertTransition(geoResult, locState.latitude, locState.longitude)
                refreshUiState()
            }
        }
    }

    private fun buildCurrentUiState(): DemoUiState {
        val loc = locationProvider.locationState.value
        val geoResult = geofenceEngine.evaluatePosition(loc.latitude, loc.longitude)

        return DemoUiState(
            latitude = loc.latitude,
            longitude = loc.longitude,
            speedKnots = loc.speedKnots,
            headingDegrees = loc.headingDegrees,
            distanceMeters = geoResult.distanceMeters,
            formattedDistance = geoResult.formattedDistance,
            alertLevel = geoResult.alertLevel,
            isBreached = geoResult.isBreached,
            actionRequired = geoResult.actionRequired,
            currentStep = loc.currentStep,
            totalSteps = loc.totalSteps,
            isRunning = loc.isRunning,
            isCompleted = loc.isCompleted,
            isCriticalScreenActive = _isCriticalScreenActive.value,
            hasMutedCriticalAlert = _hasMutedCriticalAlert.value
        )
    }

    private fun refreshUiState() {
        _demoUiState.value = buildCurrentUiState()
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
            details = "Simulated vessel approached within threshold: ${tier.title}"
        )

        alertDao?.let { dao ->
            scope.launch {
                dao.insertAlert(event)
            }
        }
    }

    fun startDemo() {
        _hasMutedCriticalAlert.value = false
        refreshUiState()
        locationProvider.start()
    }

    fun pauseDemo() {
        locationProvider.pause()
        refreshUiState()
    }

    fun restartDemo() {
        _isCriticalScreenActive.value = false
        _hasMutedCriticalAlert.value = false
        previousAlertLevel = AlertLevel.SAFE
        locationProvider.reset()
        refreshUiState()
        locationProvider.start()
    }

    fun resetDemo() {
        _isCriticalScreenActive.value = false
        _hasMutedCriticalAlert.value = false
        previousAlertLevel = AlertLevel.SAFE
        locationProvider.reset()
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
}
