package com.seasentry.app

import com.seasentry.app.demo.DemoController
import com.seasentry.app.geofence.AlertLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoControllerTest {

    @Test
    fun testCriticalTransitionAndMuteFlow() = runTest {
        val controller = DemoController(scope = backgroundScope, stepIntervalMs = 10L)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.demoUiState.collect()
        }
        testScheduler.advanceUntilIdle()

        // Initially at step 0: SAFE
        val state0 = controller.demoUiState.value
        assertEquals(AlertLevel.SAFE, state0.alertLevel)
        assertFalse(state0.isCriticalScreenActive)
        assertFalse(state0.hasMutedCriticalAlert)

        // Jump to Step 4 (Warning Zone)
        controller.locationProvider.jumpToStep(4)
        yield()
        testScheduler.advanceTimeBy(100)
        val state4 = controller.demoUiState.value
        assertEquals(AlertLevel.WARNING, state4.alertLevel)
        assertFalse(state4.isCriticalScreenActive)

        // Jump to Step 6 (Critical Zone)
        controller.locationProvider.jumpToStep(6)
        yield()
        testScheduler.advanceTimeBy(100)
        val state6 = controller.demoUiState.value
        assertEquals(AlertLevel.CRITICAL, state6.alertLevel)
        assertTrue("Critical screen must activate when reaching CRITICAL", state6.isCriticalScreenActive)
        assertFalse(state6.hasMutedCriticalAlert)

        // Dismiss / Mute critical alert
        controller.dismissCriticalAlert()
        testScheduler.advanceUntilIdle()
        val stateMuted = controller.demoUiState.value
        assertFalse("Critical screen should be deactivated on dismiss", stateMuted.isCriticalScreenActive)
        assertTrue("Critical alert should be marked as muted", stateMuted.hasMutedCriticalAlert)

        // Re-open critical screen
        controller.openCriticalScreen()
        testScheduler.advanceUntilIdle()
        val stateReopened = controller.demoUiState.value
        assertTrue("Critical screen should be active after openCriticalScreen", stateReopened.isCriticalScreenActive)
        assertFalse("Muted flag should be cleared", stateReopened.hasMutedCriticalAlert)

        // Reset demo
        controller.resetDemo()
        testScheduler.advanceUntilIdle()
        val stateReset = controller.demoUiState.value
        assertEquals(AlertLevel.SAFE, stateReset.alertLevel)
        assertFalse(stateReset.isCriticalScreenActive)
        assertFalse(stateReset.hasMutedCriticalAlert)

        // Restart demo resets to SAFE initial state and starts
        controller.restartDemo()
        val stateRestarted = controller.demoUiState.value
        assertEquals(AlertLevel.SAFE, stateRestarted.alertLevel)
        assertFalse(stateRestarted.isCriticalScreenActive)
        assertFalse(stateRestarted.hasMutedCriticalAlert)

        // Progress to CRITICAL again after restart
        controller.locationProvider.jumpToStep(6)
        yield()
        testScheduler.advanceTimeBy(100)
        val stateCriticalAgain = controller.demoUiState.value
        assertEquals(AlertLevel.CRITICAL, stateCriticalAgain.alertLevel)
        assertTrue("Critical screen must re-trigger when reaching CRITICAL again", stateCriticalAgain.isCriticalScreenActive)
        assertFalse(stateCriticalAgain.hasMutedCriticalAlert)

        job.cancel()
    }
}

