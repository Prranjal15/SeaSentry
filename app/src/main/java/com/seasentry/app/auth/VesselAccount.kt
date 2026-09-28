package com.seasentry.app.auth

import kotlinx.serialization.Serializable

/**
 * Data class representing a registered vessel account.
 * Persisted locally on device (offline, no backend, no Room).
 */
@Serializable
data class VesselAccount(
    val vesselName: String,
    val registrationNumber: String,
    val captainName: String,
    val homePort: String,
    val pinHash: String,
    val salt: String
)
