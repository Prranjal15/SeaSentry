package com.seasentry.app.demo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class Waypoint(
    val stepIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val speedKnots: Double,
    val headingDegrees: Double,
    val note: String
)

data class MockLocationState(
    val currentStep: Int = 0,
    val totalSteps: Int = 9,
    val latitude: Double = 18.9220,
    val longitude: Double = 72.8347,
    val speedKnots: Double = 14.2,
    val headingDegrees: Double = 0.0,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false
)

class MockLocationProvider(
    private val scope: CoroutineScope,
    private val stepIntervalMs: Long = 1500L
) {
    companion object {
        val WAYPOINTS = listOf(
            Waypoint(0, 18.9220, 72.8347, 14.2, 0.0, "Safe Zone - Starting Port Departure (~1.89 km)"),
            Waypoint(1, 18.9270, 72.8347, 14.5, 0.0, "Safe Zone - Cruising Northbound (~1.33 km)"),
            Waypoint(2, 18.9302, 72.8347, 14.8, 0.0, "Entering Advisory Zone (~980 m)"),
            Waypoint(3, 18.9328, 72.8347, 15.0, 0.0, "Advisory Zone - Approaching Boundary (~690 m)"),
            Waypoint(4, 18.9348, 72.8347, 15.1, 0.0, "Entering Warning Zone (~470 m)"),
            Waypoint(5, 18.9365, 72.8347, 15.3, 0.0, "Warning Zone - Extreme Proximity (~280 m)"),
            Waypoint(6, 18.9374, 72.8347, 15.4, 0.0, "Critical Zone - Immediate Action Required (~180 m)"),
            Waypoint(7, 18.9385, 72.8347, 15.5, 0.0, "Critical Zone - Breaching Boundary (~55 m)"),
            Waypoint(8, 18.9390, 72.8347, 15.5, 0.0, "IMBL Maritime Boundary Breached (0 m)")
        )
    }

    private val _locationState = MutableStateFlow(
        MockLocationState(
            currentStep = 0,
            totalSteps = WAYPOINTS.size,
            latitude = WAYPOINTS.first().latitude,
            longitude = WAYPOINTS.first().longitude,
            speedKnots = WAYPOINTS.first().speedKnots,
            headingDegrees = WAYPOINTS.first().headingDegrees
        )
    )
    val locationState: StateFlow<MockLocationState> = _locationState.asStateFlow()

    private var simulationJob: Job? = null

    fun start() {
        if (_locationState.value.isRunning) return
        if (_locationState.value.isCompleted) {
            reset()
        }

        simulationJob?.cancel()
        simulationJob = scope.launch {
            _locationState.value = _locationState.value.copy(isRunning = true)

            var currentIndex = _locationState.value.currentStep
            while (isActive && currentIndex < WAYPOINTS.size) {
                val wp = WAYPOINTS[currentIndex]
                _locationState.value = _locationState.value.copy(
                    currentStep = currentIndex,
                    latitude = wp.latitude,
                    longitude = wp.longitude,
                    speedKnots = wp.speedKnots,
                    headingDegrees = wp.headingDegrees,
                    isRunning = true,
                    isCompleted = currentIndex == WAYPOINTS.size - 1
                )

                if (currentIndex == WAYPOINTS.size - 1) {
                    break
                }

                delay(stepIntervalMs)
                currentIndex++
            }

            _locationState.value = _locationState.value.copy(
                isRunning = false,
                isCompleted = true
            )
        }
    }

    fun pause() {
        simulationJob?.cancel()
        simulationJob = null
        _locationState.value = _locationState.value.copy(isRunning = false)
    }

    fun reset() {
        simulationJob?.cancel()
        simulationJob = null
        val first = WAYPOINTS.first()
        _locationState.value = MockLocationState(
            currentStep = 0,
            totalSteps = WAYPOINTS.size,
            latitude = first.latitude,
            longitude = first.longitude,
            speedKnots = first.speedKnots,
            headingDegrees = first.headingDegrees,
            isRunning = false,
            isCompleted = false
        )
    }

    fun jumpToStep(step: Int) {
        val index = step.coerceIn(0, WAYPOINTS.size - 1)
        val wp = WAYPOINTS[index]
        _locationState.value = _locationState.value.copy(
            currentStep = index,
            latitude = wp.latitude,
            longitude = wp.longitude,
            speedKnots = wp.speedKnots,
            headingDegrees = wp.headingDegrees,
            isCompleted = index == WAYPOINTS.size - 1
        )
    }
}
