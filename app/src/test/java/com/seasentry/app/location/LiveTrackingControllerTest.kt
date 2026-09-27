package com.seasentry.app.location

import com.seasentry.app.geofence.AlertLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiveTrackingControllerTest {

    @Test
    fun testInitialState() = runTest {
        val locationProvider = RealLocationProvider()
        val controller = LiveTrackingController(scope = backgroundScope, locationProvider = locationProvider)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.liveTrackingUiState.collect()
        }
        testScheduler.advanceUntilIdle()

        val initialState = controller.liveTrackingUiState.value
        assertEquals(GpsFixState.IDLE, initialState.fixState)
        assertNull(initialState.latitude)
        assertNull(initialState.longitude)
        assertEquals(AlertLevel.SAFE, initialState.alertLevel)
        assertFalse(initialState.isCriticalScreenActive)
        assertFalse(initialState.hasMutedCriticalAlert)
        assertFalse(initialState.isTracking)

        job.cancel()
    }

    @Test
    fun testGpsFixStateTransitions() = runTest {
        val locationProvider = RealLocationProvider()
        val controller = LiveTrackingController(scope = backgroundScope, locationProvider = locationProvider)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.liveTrackingUiState.collect()
        }
        testScheduler.advanceUntilIdle()

        // 1. Permission Denied State
        locationProvider.updateStateForTesting(RealLocationState(fixState = GpsFixState.PERMISSION_DENIED))
        yield()
        val permDeniedState = controller.liveTrackingUiState.value
        assertEquals(GpsFixState.PERMISSION_DENIED, permDeniedState.fixState)
        assertTrue(permDeniedState.actionRequired.contains("permission", ignoreCase = true))

        // 2. GPS Disabled State
        locationProvider.updateStateForTesting(RealLocationState(fixState = GpsFixState.GPS_DISABLED))
        yield()
        val gpsDisabledState = controller.liveTrackingUiState.value
        assertEquals(GpsFixState.GPS_DISABLED, gpsDisabledState.fixState)
        assertTrue(gpsDisabledState.actionRequired.contains("GPS", ignoreCase = true))

        // 3. Acquiring State with Seed Fix
        locationProvider.updateStateForTesting(
            RealLocationState(
                fixState = GpsFixState.ACQUIRING,
                latitude = 18.9220,
                longitude = 72.8347,
                speedKnots = 12.0f,
                headingDegrees = 45.0f,
                accuracyMeters = 8.0f,
                seedFixAgeMs = 5000L
            )
        )
        yield()
        val acquiringState = controller.liveTrackingUiState.value
        assertEquals(GpsFixState.ACQUIRING, acquiringState.fixState)
        assertEquals(18.9220, acquiringState.latitude!!, 0.0001)
        assertEquals(72.8347, acquiringState.longitude!!, 0.0001)
        assertEquals(5000L, acquiringState.seedFixAgeMs)
        assertEquals(AlertLevel.SAFE, acquiringState.alertLevel)

        // 4. Fixed State
        locationProvider.updateStateForTesting(
            RealLocationState(
                fixState = GpsFixState.FIXED,
                latitude = 18.9270,
                longitude = 72.8347,
                speedKnots = 14.5f,
                headingDegrees = 0.0f,
                accuracyMeters = 3.5f,
                seedFixAgeMs = null,
                lastFixTimestamp = 1700000000000L
            )
        )
        yield()
        val fixedState = controller.liveTrackingUiState.value
        assertEquals(GpsFixState.FIXED, fixedState.fixState)
        assertTrue(fixedState.hasValidFix)
        assertEquals(18.9270, fixedState.latitude!!, 0.0001)
        assertNull(fixedState.seedFixAgeMs)
        assertEquals(3.5f, fixedState.accuracyMeters)

        job.cancel()
    }

    @Test
    fun testGeofenceEvaluationAndCriticalTransition() = runTest {
        val locationProvider = RealLocationProvider()
        val controller = LiveTrackingController(scope = backgroundScope, locationProvider = locationProvider)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.liveTrackingUiState.collect()
        }
        testScheduler.advanceUntilIdle()

        // 1. Safe Zone
        locationProvider.updateStateForTesting(
            RealLocationState(
                fixState = GpsFixState.FIXED,
                latitude = 18.9220,
                longitude = 72.8347
            )
        )
        yield()
        assertEquals(AlertLevel.SAFE, controller.liveTrackingUiState.value.alertLevel)
        assertFalse(controller.liveTrackingUiState.value.isCriticalScreenActive)

        // 2. Warning Zone (~470m from IMBL at 18.9348)
        locationProvider.updateStateForTesting(
            RealLocationState(
                fixState = GpsFixState.FIXED,
                latitude = 18.9348,
                longitude = 72.8347
            )
        )
        yield()
        assertEquals(AlertLevel.WARNING, controller.liveTrackingUiState.value.alertLevel)
        assertFalse(controller.liveTrackingUiState.value.isCriticalScreenActive)

        // 3. Critical Zone (18.9390, 72.8347 - breach at IMBL)
        locationProvider.updateStateForTesting(
            RealLocationState(
                fixState = GpsFixState.FIXED,
                latitude = 18.9390,
                longitude = 72.8347
            )
        )
        yield()
        val criticalState = controller.liveTrackingUiState.value
        assertEquals(AlertLevel.CRITICAL, criticalState.alertLevel)
        assertTrue("Critical screen should activate on boundary breach", criticalState.isCriticalScreenActive)
        assertTrue("isBreached must be true when near boundary line", criticalState.isBreached)
        assertFalse(criticalState.hasMutedCriticalAlert)

        // 4. Mute / Dismiss Critical Alert
        controller.dismissCriticalAlert()
        testScheduler.advanceUntilIdle()
        val mutedState = controller.liveTrackingUiState.value
        assertFalse("Critical screen should deactivate after dismiss", mutedState.isCriticalScreenActive)
        assertTrue("Alert should be recorded as muted", mutedState.hasMutedCriticalAlert)

        // 5. Open Critical Screen
        controller.openCriticalScreen()
        testScheduler.advanceUntilIdle()
        val reopenedState = controller.liveTrackingUiState.value
        assertTrue("Critical screen should re-open", reopenedState.isCriticalScreenActive)
        assertFalse(reopenedState.hasMutedCriticalAlert)

        job.cancel()
    }

    @Test
    fun testTrackingControls() = runTest {
        val locationProvider = RealLocationProvider()
        val controller = LiveTrackingController(scope = backgroundScope, locationProvider = locationProvider)

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.liveTrackingUiState.collect()
        }
        testScheduler.advanceUntilIdle()

        controller.startTracking()
        yield()
        assertTrue(controller.liveTrackingUiState.value.isTracking)

        controller.stopTracking()
        yield()
        assertFalse(controller.liveTrackingUiState.value.isTracking)

        job.cancel()
    }
}
