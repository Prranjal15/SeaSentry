package com.seasentry.app.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.ui.components.SeaSentryPrimaryButton
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.OceanDark
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.PeachSquare
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate

/**
 * Register screen for creating new local vessel commander accounts.
 */
@Composable
fun RegisterScreen(
    onRegisterClick: (
        vesselName: String,
        registrationNumber: String,
        captainName: String,
        homePort: String,
        pin: String
    ) -> Unit,
    onNavigateToLogin: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var vesselName by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var captainName by remember { mutableStateOf("") }
    var homePort by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkyBlueBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Registration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PeachSquare),
                        contentAlignment = Alignment.Center
                    ) {
                        VesselLineArtIcon(
                            modifier = Modifier.size(36.dp),
                            color = OceanDark,
                            strokeWidth = 2.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = "Register Vessel",
                        color = TextDarkNavy,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Create local offline vessel profile",
                        color = TextSecondarySlate,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Demo Badge
                    DemoBadge()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Alert Banner (if server/repo or local validation error)
                    val activeError = localValidationError ?: errorMessage
                    if (activeError != null) {
                        AuthErrorBanner(
                            errorMessage = activeError,
                            onDismiss = {
                                localValidationError = null
                                onDismissError()
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 1. Vessel Name Field
                    AuthTextField(
                        value = vesselName,
                        onValueChange = {
                            vesselName = it
                            localValidationError = null
                        },
                        label = "Vessel Name",
                        placeholder = "e.g. Sea Falcon, Ocean Queen",
                        leadingIconText = "⚓"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Registration Number Field
                    AuthTextField(
                        value = registrationNumber,
                        onValueChange = {
                            registrationNumber = it
                            localValidationError = null
                        },
                        label = "Registration / Call Sign",
                        placeholder = "e.g. TN-12345 or KL-07-882",
                        leadingIconText = "📋"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Captain Name Field
                    AuthTextField(
                        value = captainName,
                        onValueChange = {
                            captainName = it
                            localValidationError = null
                        },
                        label = "Captain / Master Name",
                        placeholder = "e.g. Capt. Rajesh Kumar",
                        leadingIconText = "🧑‍✈️"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Home Port Field
                    AuthTextField(
                        value = homePort,
                        onValueChange = {
                            homePort = it
                            localValidationError = null
                        },
                        label = "Home Port / Harbor",
                        placeholder = "e.g. Chennai Harbor, Cochin",
                        leadingIconText = "📍"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. PIN Field
                    AuthTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                pin = it
                                localValidationError = null
                            }
                        },
                        label = "Security PIN (4 to 6 digits)",
                        placeholder = "••••",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIconText = "🔒"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. Confirm PIN Field
                    AuthTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                confirmPin = it
                                localValidationError = null
                            }
                        },
                        label = "Confirm Security PIN",
                        placeholder = "••••",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIconText = "🔑"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Register Button
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangePrimary, strokeWidth = 3.dp)
                        }
                    } else {
                        SeaSentryPrimaryButton(
                            text = "REGISTER & ACTIVATE",
                            onClick = {
                                if (vesselName.isBlank()) {
                                    localValidationError = "Please enter the vessel name"
                                    return@SeaSentryPrimaryButton
                                }
                                if (registrationNumber.isBlank()) {
                                    localValidationError = "Please enter the registration number"
                                    return@SeaSentryPrimaryButton
                                }
                                if (captainName.isBlank()) {
                                    localValidationError = "Please enter the captain name"
                                    return@SeaSentryPrimaryButton
                                }
                                if (homePort.isBlank()) {
                                    localValidationError = "Please enter the home port"
                                    return@SeaSentryPrimaryButton
                                }
                                if (pin.length < 4 || pin.length > 6) {
                                    localValidationError = "PIN must be between 4 and 6 numeric digits"
                                    return@SeaSentryPrimaryButton
                                }
                                if (pin != confirmPin) {
                                    localValidationError = "PINs do not match"
                                    return@SeaSentryPrimaryButton
                                }
                                onRegisterClick(
                                    vesselName,
                                    registrationNumber,
                                    captainName,
                                    homePort,
                                    pin
                                )
                            },
                            backgroundColor = OrangePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigate to Login
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Already registered? ",
                            color = TextSecondarySlate,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Sign In",
                            color = OrangePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onNavigateToLogin() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
