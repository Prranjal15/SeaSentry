package com.seasentry.app.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate

/**
 * Authentication Gate:
 * 1. Shows animated splash screen on startup.
 * 2. If session exists -> renders the existing app content unchanged with an onLogout callback.
 * 3. If not logged in -> displays the Login / Register flow.
 */
@Composable
fun AuthGate(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel? = null,
    content: @Composable (onLogout: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val vm = authViewModel ?: remember {
        AuthViewModel(AuthRepository(context.applicationContext))
    }

    val state by vm.uiState.collectAsState()

    Crossfade(
        targetState = when {
            state.isSplashShowing -> "SPLASH"
            state.isLoggedIn -> "APP"
            state.currentScreen == AuthScreen.LOGIN -> "LOGIN"
            else -> "REGISTER"
        },
        label = "AuthGateTransition",
        modifier = modifier.fillMaxSize()
    ) { target ->
        when (target) {
            "SPLASH" -> {
                SplashScreen(
                    onSplashComplete = { vm.onSplashFinished() }
                )
            }

            "APP" -> {
                // Render EXISTING app content completely uncovered and unchanged
                content {
                    vm.logout()
                }
            }

            "LOGIN" -> {
                LoginScreen(
                    onLoginClick = { reg, pin -> vm.login(reg, pin) },
                    onContinueDemoClick = { vm.loginDemoVessel() },
                    onNavigateToRegister = { vm.showRegister() },
                    errorMessage = state.errorMessage,
                    onDismissError = { vm.clearError() },
                    isLoading = state.isLoading
                )
            }

            "REGISTER" -> {
                RegisterScreen(
                    onRegisterClick = { name, reg, cap, port, pin ->
                        vm.register(name, reg, cap, port, pin)
                    },
                    onNavigateToLogin = { vm.showLogin() },
                    errorMessage = state.errorMessage,
                    onDismissError = { vm.clearError() },
                    isLoading = state.isLoading
                )
            }
        }
    }
}

/**
 * Reusable confirmation dialog for vessel logout.
 */
@Composable
fun LogoutConfirmationDialog(
    vesselName: String = "Active Vessel",
    vesselId: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Out Active Vessel?",
                fontWeight = FontWeight.Bold,
                color = TextDarkNavy,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = if (vesselId.isNotBlank()) {
                    "Are you sure you want to log out $vesselName ($vesselId)? You will return to the vessel commander login screen."
                } else {
                    "Are you sure you want to log out of the active session? You will return to the vessel commander login screen."
                },
                color = TextSecondarySlate,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "LOGOUT",
                    color = EmergencyRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "CANCEL",
                    color = TextDarkNavy,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
