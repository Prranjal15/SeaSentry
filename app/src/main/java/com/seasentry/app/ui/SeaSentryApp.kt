package com.seasentry.app.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.seasentry.app.alert.EmergencyAlertManager
import com.seasentry.app.audio.AlertLanguageStore
import com.seasentry.app.audio.AlertSoundController
import com.seasentry.app.auth.AuthGate
import com.seasentry.app.data.AppDatabase
import com.seasentry.app.demo.DemoController
import com.seasentry.app.location.LiveTrackingController
import com.seasentry.app.location.TrackingMode
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
    AuthGate(modifier = modifier) { onLogout ->
        SeaSentryAppContent(onLogout = onLogout)
    }
}

@Composable
private fun SeaSentryAppContent(
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize Room Database & DAOs
    val database = remember { AppDatabase.getDatabase(context) }
    val alertDao = remember { database.alertDao() }
    val sosDao = remember { database.sosDao() }

    // Initialize Audio & Emergency Alert Controllers
    val alertLanguageStore = remember { AlertLanguageStore(context.applicationContext) }
    val alertSoundController = remember { AlertSoundController(context.applicationContext) }
    val emergencyAlertManager = remember { EmergencyAlertManager(context.applicationContext) }

    DisposableEffect(Unit) {
        onDispose {
            emergencyAlertManager.release()
            alertSoundController.release()
        }
    }

    // Initialize Simulated Demo Controller (Intact Mock Location Provider)
    val demoController = remember {
        DemoController(
            scope = coroutineScope,
            alertDao = alertDao
        )
    }

    // Initialize Live Standalone Hardware GPS Tracking Controller (100% Offline Satellite Location)
    val liveTrackingController = remember {
        LiveTrackingController(
            context = context.applicationContext,
            scope = coroutineScope,
            alertDao = alertDao
        )
    }

    // Tracking Mode Toggle State (DEMO vs LIVE_GPS)
    var trackingMode by remember { mutableStateOf(TrackingMode.DEMO) }

    // Runtime Permission Launcher for direct ACCESS_FINE_LOCATION
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            liveTrackingController.startTracking()
        } else {
            liveTrackingController.refreshGpsStatus()
        }
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
    val liveState by liveTrackingController.liveTrackingUiState.collectAsState()
    val sosState by sosManager.sosState.collectAsState()
    val relayTelemetry by sosManager.relayManager.telemetry.collectAsState()
    val navWarnings by navAreaRepository.warnings.collectAsState()

    val alertEventsFlow = remember { alertDao.getAllAlerts() }

    // Active position coordinates sourced dynamically from whichever tracking controller is active
    val activeLat = if (trackingMode == TrackingMode.LIVE_GPS) {
        liveState.latitude ?: demoState.latitude
    } else {
        demoState.latitude
    }

    val activeLon = if (trackingMode == TrackingMode.LIVE_GPS) {
        liveState.longitude ?: demoState.longitude
    } else {
        demoState.longitude
    }

    val isCriticalScreenActive = if (trackingMode == TrackingMode.LIVE_GPS) {
        liveState.isCriticalScreenActive
    } else {
        demoState.isCriticalScreenActive
    }

    // Trigger Emergency Sound & Vibration on transition to CRITICAL, and stop when muted/dismissed/reset
    LaunchedEffect(isCriticalScreenActive, trackingMode) {
        if (isCriticalScreenActive) {
            val language = alertLanguageStore.getLanguage()
            alertSoundController.start(language)
            emergencyAlertManager.startEmergencyAlert()
        } else {
            alertSoundController.stop()
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
                        currentLat = activeLat,
                        currentLon = activeLon,
                        isSOSActive = sosState.isDistressActive,
                        onSendSOS = {
                            sosManager.triggerSOS(
                                senderId = userVesselId,
                                latitude = activeLat,
                                longitude = activeLon
                            )
                        },
                        onNavigateToDemo = { currentScreen = ScreenDestination.DEMO },
                        onNavigateToHistory = { currentScreen = ScreenDestination.SAFETY_HISTORY },
                        onNavigateToSurvival = { currentScreen = ScreenDestination.SURVIVAL_GUIDE },
                        onNavigateToCoastGuard = { currentScreen = ScreenDestination.COAST_GUARD },
                        onNavigateToSettings = { currentScreen = ScreenDestination.SETTINGS },
                        navWarnings = navWarnings,
                        onSimulateHazardWarning = { navAreaRepository.injectSimulatedWarning() },
                        onDismissNavWarning = { warningId -> navAreaRepository.removeWarning(warningId) },
                        trackingMode = trackingMode,
                        onToggleTrackingMode = { mode ->
                            trackingMode = mode
                            if (mode == TrackingMode.LIVE_GPS) {
                                if (liveTrackingController.hasLocationPermission()) {
                                    liveTrackingController.startTracking()
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            } else {
                                liveTrackingController.stopTracking()
                            }
                        },
                        gpsFixState = liveState.fixState,
                        liveTrackingUiState = liveState,
                        onRequestLocationPermission = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
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
                        onBackClick = { currentScreen = ScreenDestination.HUB },
                        onLogout = onLogout,
                        alertLanguageStore = alertLanguageStore
                    )
                }
            }
        }

        // Full Screen Critical Warning Modal Overlay
        if (isCriticalScreenActive) {
            CriticalWarningScreen(
                onDismiss = {
                    if (trackingMode == TrackingMode.LIVE_GPS) {
                        liveTrackingController.dismissCriticalAlert()
                    } else {
                        demoController.dismissCriticalAlert()
                    }
                }
            )
        }
    }
}
