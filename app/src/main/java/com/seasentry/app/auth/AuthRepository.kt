package com.seasentry.app.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Local repository for offline authentication, vessel account registration,
 * and persistent session management using SharedPreferences and JSON (No Room).
 */
class AuthRepository(
    private val sharedPreferences: SharedPreferences
) {

    constructor(context: Context, prefsName: String = PREFS_NAME) : this(
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _activeSession = MutableStateFlow<VesselAccount?>(null)
    val activeSession: StateFlow<VesselAccount?> = _activeSession.asStateFlow()

    init {
        restoreSession()
    }

    /**
     * Reads all registered accounts from local storage.
     */
    fun getAllAccounts(): List<VesselAccount> {
        val rawJson = sharedPreferences.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<VesselAccount>>(rawJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Finds an account by registration number (case-insensitive).
     */
    fun findAccountByRegNumber(registrationNumber: String): VesselAccount? {
        val normalized = registrationNumber.trim().uppercase()
        return getAllAccounts().find { it.registrationNumber.trim().uppercase() == normalized }
    }

    /**
     * Registers a new vessel account with validation, salt generation, and SHA-256 PIN hashing.
     * On success, sets the active session and persists the account locally.
     */
    fun register(
        vesselName: String,
        registrationNumber: String,
        captainName: String,
        homePort: String,
        pin: String
    ): Result<VesselAccount> {
        val cleanVesselName = vesselName.trim()
        val cleanReg = registrationNumber.trim().uppercase()
        val cleanCaptain = captainName.trim()
        val cleanPort = homePort.trim()
        val cleanPin = pin.trim()

        if (cleanVesselName.isBlank()) {
            return Result.failure(IllegalArgumentException("Vessel name cannot be empty"))
        }
        if (cleanReg.isBlank()) {
            return Result.failure(IllegalArgumentException("Registration number cannot be empty"))
        }
        if (cleanCaptain.isBlank()) {
            return Result.failure(IllegalArgumentException("Captain name cannot be empty"))
        }
        if (cleanPort.isBlank()) {
            return Result.failure(IllegalArgumentException("Home port cannot be empty"))
        }
        if (!AuthCrypto.isValidPin(cleanPin)) {
            return Result.failure(IllegalArgumentException("PIN must be 4 to 6 numeric digits"))
        }

        val existingAccounts = getAllAccounts().toMutableList()
        if (existingAccounts.any { it.registrationNumber.trim().uppercase() == cleanReg }) {
            return Result.failure(IllegalArgumentException("Registration number '$cleanReg' is already registered"))
        }

        val salt = AuthCrypto.generateSalt()
        val pinHash = AuthCrypto.hashPin(cleanPin, salt)

        val newAccount = VesselAccount(
            vesselName = cleanVesselName,
            registrationNumber = cleanReg,
            captainName = cleanCaptain,
            homePort = cleanPort,
            pinHash = pinHash,
            salt = salt
        )

        existingAccounts.add(newAccount)
        saveAccounts(existingAccounts)
        setActiveSession(newAccount)

        return Result.success(newAccount)
    }

    /**
     * Authenticates an account using registration number and PIN.
     * On success, persists the active session.
     */
    fun login(registrationNumber: String, pin: String): Result<VesselAccount> {
        val cleanReg = registrationNumber.trim().uppercase()
        val cleanPin = pin.trim()

        if (cleanReg.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your vessel registration number"))
        }
        if (cleanPin.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your PIN"))
        }

        val account = findAccountByRegNumber(cleanReg)
            ?: return Result.failure(IllegalArgumentException("Unknown registration number: $cleanReg"))

        if (!AuthCrypto.verifyPin(cleanPin, account.pinHash, account.salt)) {
            return Result.failure(IllegalArgumentException("Incorrect PIN. Please try again."))
        }

        setActiveSession(account)
        return Result.success(account)
    }

    /**
     * One-tap login/registration for the standard demo vessel.
     */
    fun loginDemoVessel(): Result<VesselAccount> {
        val demoReg = DEMO_REG_NUMBER
        val existing = findAccountByRegNumber(demoReg)
        return if (existing != null) {
            setActiveSession(existing)
            Result.success(existing)
        } else {
            register(
                vesselName = DEMO_VESSEL_NAME,
                registrationNumber = DEMO_REG_NUMBER,
                captainName = DEMO_CAPTAIN_NAME,
                homePort = DEMO_HOME_PORT,
                pin = DEMO_DEFAULT_PIN
            )
        }
    }

    /**
     * Clears the current active session (Logout).
     */
    fun logout() {
        sharedPreferences.edit()
            .remove(KEY_ACTIVE_SESSION_REG)
            .apply()
        _activeSession.value = null
    }

    /**
     * Returns true if there is an active session currently saved.
     */
    fun isLoggedIn(): Boolean = _activeSession.value != null

    private fun restoreSession() {
        val activeReg = sharedPreferences.getString(KEY_ACTIVE_SESSION_REG, null) ?: return
        val account = findAccountByRegNumber(activeReg)
        if (account != null) {
            _activeSession.value = account
        } else {
            sharedPreferences.edit().remove(KEY_ACTIVE_SESSION_REG).apply()
        }
    }

    private fun setActiveSession(account: VesselAccount) {
        sharedPreferences.edit()
            .putString(KEY_ACTIVE_SESSION_REG, account.registrationNumber)
            .apply()
        _activeSession.value = account
    }

    private fun saveAccounts(accounts: List<VesselAccount>) {
        val rawJson = json.encodeToString(accounts)
        sharedPreferences.edit()
            .putString(KEY_ACCOUNTS, rawJson)
            .apply()
    }

    companion object {
        const val PREFS_NAME = "seasentry_auth_prefs"
        private const val KEY_ACCOUNTS = "registered_accounts_json"
        private const val KEY_ACTIVE_SESSION_REG = "active_session_reg_number"

        // Default Demo Vessel sample credentials
        const val DEMO_VESSEL_NAME = "Sea Falcon"
        const val DEMO_REG_NUMBER = "TN-12345"
        const val DEMO_CAPTAIN_NAME = "Capt. Rajesh Kumar"
        const val DEMO_HOME_PORT = "Chennai"
        const val DEMO_DEFAULT_PIN = "1234"
    }
}
