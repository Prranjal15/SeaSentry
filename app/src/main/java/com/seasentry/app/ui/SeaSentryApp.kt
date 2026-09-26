package com.seasentry.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.seasentry.app.alert.EmergencyAlertManager
import com.seasentry.app.data.AppDatabase
import com.seasentry.app.demo.DemoController
import com.seasentry.app.navarea.NavAreaRepository
import com.seasentry.app.sos.SOSManager
import com.seasentry.app.ui.screens.CoastGuardScreen
import com.seasentry.app.ui.screens.CriticalWarningScreen
import com.seasentry.app.ui.screens.DemoScreen
import com.seasentry.app.ui.screens.HubScreen
import com.seasentry.app.ui.screens.LoginScreen
import com.seasentry.app.ui.screens.SafetyHistoryScreen
import com.seasentry.app.ui.screens.SettingsScreen
import com.seasentry.app.ui.screens.SurvivalGuideScreen
import kotlinx.coroutines.launch

enum class ScreenDestination {
    LOGIN,
    HUB,
    DEMO,
    SAFETY_HISTORY,
    SURVIVAL_GUIDE,
    COAST_GUARD,
    SETTINGS
}

@Composable
fun SeaSentryApp(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize Room Database & DAOs
    val database = remember { AppDatabase.getDatabase(context) }
    val alertDao = remember { database.alertDao() }
    val sosDao = remember { database.sosDao() }

    // Initialize Emergency Alert Manager (Audio + Vibration)
    val emergencyAlertManager = remember { EmergencyAlertManager(context.applicationContext) }

    DisposableEffect(Unit) {
        onDispose {
            emergencyAlertManager.release()
        }
    }

    // Initialize Controllers & Repositories
    val demoController = remember {
        DemoController(
            scope = coroutineScope,
            alertDao = alertDao
        )
    }

    val sosManager = remember {
        SOSManager(
            scope = coroutineScope,
            sosDao = sosDao
        )
    }

    val navAreaRepository = remember {
        NavAreaRepository()
    }

    var currentScreen by remember { mutableStateOf(ScreenDestination.HUB) }
    var userVesselId by remember { mutableStateOf("SEASENTRY-PRO-2026") }

    val demoState by demoController.demoUiState.collectAsState()
    val sosState by sosManager.sosState.collectAsState()
    val relayTelemetry by sosManager.relayManager.telemetry.collectAsState()
    val navWarnings by navAreaRepository.warnings.collectAsState()

    val alertEventsFlow = remember { alertDao.getAllAlerts() }

    // Trigger Emergency Sound & Vibration on transition to CRITICAL, and stop when muted/dismissed/reset
    LaunchedEffect(demoState.isCriticalScreenActive) {
        if (demoState.isCriticalScreenActive) {
            emergencyAlertManager.startEmergencyAlert()
        } else {
            emergencyAlertManager.stopEmergencyAlert()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main Screen Navigation
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { destination ->
            when (destination) {
                ScreenDestination.LOGIN -> {
                    LoginScreen(
                        onLoginSuccess = { vesselId ->
                            userVesselId = vesselId
                            currentScreen = ScreenDestination.HUB
                        }
                    )
                }

                ScreenDestination.HUB -> {
                    HubScreen(
                        currentLat = demoState.latitude,
                        currentLon = demoState.longitude,
                        isSOSActive = sosState.isDistressActive,
                        onSendSOS = {
                            sosManager.triggerSOS(
                                senderId = userVesselId,
                                latitude = demoState.latitude,
                                longitude = demoState.longitude
                            )
                        },
                        onNavigateToDemo = { currentScreen = ScreenDestination.DEMO },
                        onNavigateToHistory = { currentScreen = ScreenDestination.SAFETY_HISTORY },
                        onNavigateToSurvival = { currentScreen = ScreenDestination.SURVIVAL_GUIDE },
                        onNavigateToCoastGuard = { currentScreen = ScreenDestination.COAST_GUARD },
                        onNavigateToSettings = { currentScreen = ScreenDestination.SETTINGS },
                        navWarnings = navWarnings,
                        onSimulateHazardWarning = { navAreaRepository.injectSimulatedWarning() },
                        onDismissNavWarning = { warningId -> navAreaRepository.removeWarning(warningId) }
                    )
                }

                ScreenDestination.DEMO -> {
                    DemoScreen(
                        demoState = demoState,
                        onStartDemo = { demoController.startDemo() },
                        onRestartDemo = { demoController.restartDemo() },
                        onPauseDemo = { demoController.pauseDemo() },
                        onTriggerSOS = {
                            sosManager.triggerSOS(
                                senderId = userVesselId,
                                latitude = demoState.latitude,
                                longitude = demoState.longitude
                            )
                        },
                        onBackClick = { currentScreen = ScreenDestination.HUB },
                        onOpenCriticalScreen = {
                            demoController.openCriticalScreen()
                        }
                    )
                }

                ScreenDestination.SAFETY_HISTORY -> {
                    SafetyHistoryScreen(
                        alertEventsFlow = alertEventsFlow,
                        onClearLogs = {
                            coroutineScope.launch {
                                alertDao.clearAllAlerts()
                            }
                        },
                        onBackClick = { currentScreen = ScreenDestination.HUB }
                    )
                }

                ScreenDestination.SURVIVAL_GUIDE -> {
                    SurvivalGuideScreen(
                        onBackClick = { currentScreen = ScreenDestination.HUB }
                    )
                }

                ScreenDestination.COAST_GUARD -> {
                    CoastGuardScreen(
                        sosState = sosState,
                        relayTelemetry = relayTelemetry,
                        onAcknowledgeSOS = {
                            sosManager.resolveSOS()
                        },
                        onBackClick = { currentScreen = ScreenDestination.HUB }
                    )
                }

                ScreenDestination.SETTINGS -> {
                    SettingsScreen(
                        onBackClick = { currentScreen = ScreenDestination.HUB }
                    )
                }
            }
        }

        // Full Screen Critical Warning Modal Overlay (Image 1)
        if (demoState.isCriticalScreenActive) {
            CriticalWarningScreen(
                onDismiss = {
                    demoController.dismissCriticalAlert()
                }
            )
        }
    }
}
