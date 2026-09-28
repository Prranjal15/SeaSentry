package com.seasentry.app.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        repository = AuthRepository(fakePrefs)
    }

    // ==========================================
    // 1. PIN Hashing & Cryptography Tests
    // ==========================================

    @Test
    fun pinHashing_isDeterministic_forSamePinAndSalt() {
        val pin = "1234"
        val salt = "abcdef0123456789"

        val hash1 = AuthCrypto.hashPin(pin, salt)
        val hash2 = AuthCrypto.hashPin(pin, salt)

        assertEquals("Same PIN and salt must produce exact same SHA-256 hash", hash1, hash2)
        assertTrue("Hash must be non-empty hex string", hash1.isNotBlank())
        assertEquals(64, hash1.length) // SHA-256 hex length is 64
    }

    @Test
    fun pinHashing_differs_forDifferentSalts() {
        val pin = "1234"
        val salt1 = AuthCrypto.generateSalt()
        val salt2 = AuthCrypto.generateSalt()

        val hash1 = AuthCrypto.hashPin(pin, salt1)
        val hash2 = AuthCrypto.hashPin(pin, salt2)

        assertFalse("Different salts must produce different hashes for same PIN", hash1 == hash2)
    }

    @Test
    fun pinHashing_differs_forDifferentPins() {
        val salt = "common_salt_12345"
        val hash1 = AuthCrypto.hashPin("1234", salt)
        val hash2 = AuthCrypto.hashPin("5678", salt)

        assertFalse("Different PINs with same salt must produce different hashes", hash1 == hash2)
    }

    @Test
    fun verifyPin_returnsTrueForCorrectPin_andFalseForIncorrect() {
        val pin = "9876"
        val salt = AuthCrypto.generateSalt()
        val hash = AuthCrypto.hashPin(pin, salt)

        assertTrue(AuthCrypto.verifyPin("9876", hash, salt))
        assertFalse(AuthCrypto.verifyPin("1234", hash, salt))
        assertFalse(AuthCrypto.verifyPin("98765", hash, salt))
        assertFalse(AuthCrypto.verifyPin("", hash, salt))
    }

    @Test
    fun pinValidation_requires4To6Digits() {
        assertTrue("4 digits valid", AuthCrypto.isValidPin("1234"))
        assertTrue("5 digits valid", AuthCrypto.isValidPin("12345"))
        assertTrue("6 digits valid", AuthCrypto.isValidPin("123456"))

        assertFalse("3 digits invalid", AuthCrypto.isValidPin("123"))
        assertFalse("7 digits invalid", AuthCrypto.isValidPin("1234567"))
        assertFalse("Empty invalid", AuthCrypto.isValidPin(""))
        assertFalse("Alphabetic characters invalid", AuthCrypto.isValidPin("12ab"))
        assertFalse("Special characters invalid", AuthCrypto.isValidPin("12#4"))
    }

    // ==========================================
    // 2. Registration Tests
    // ==========================================

    @Test
    fun register_successfulRegistration_persistsAccountAndSetsActiveSession() {
        val result = repository.register(
            vesselName = "Ocean Sentinel",
            registrationNumber = "IND-9988",
            captainName = "Capt. Vikram",
            homePort = "Kochi Harbor",
            pin = "4321"
        )

        assertTrue("Registration should succeed", result.isSuccess)
        val account = result.getOrNull()
        assertNotNull(account)
        assertEquals("Ocean Sentinel", account?.vesselName)
        assertEquals("IND-9988", account?.registrationNumber)
        assertEquals("Capt. Vikram", account?.captainName)
        assertEquals("Kochi Harbor", account?.homePort)

        // PIN should never be stored in plaintext
        assertFalse(account!!.pinHash.contains("4321"))
        assertTrue(account.salt.isNotBlank())

        // Active session must be set
        assertTrue(repository.isLoggedIn())
        assertEquals("IND-9988", repository.activeSession.value?.registrationNumber)
    }

    @Test
    fun register_rejectsDuplicateRegistrationNumber() {
        val firstResult = repository.register(
            vesselName = "Sea Hawk",
            registrationNumber = "TN-5555",
            captainName = "Capt. John",
            homePort = "Chennai",
            pin = "1122"
        )
        assertTrue(firstResult.isSuccess)

        // Attempt second registration with same registration number
        val duplicateResult = repository.register(
            vesselName = "Another Boat",
            registrationNumber = "TN-5555",
            captainName = "Capt. Doe",
            homePort = "Mumbai",
            pin = "3344"
        )

        assertTrue("Duplicate registration must fail", duplicateResult.isFailure)
        assertTrue(
            duplicateResult.exceptionOrNull()?.message?.contains("already registered") == true
        )

        // Case-insensitive duplicate check
        val lowerCaseDuplicate = repository.register(
            vesselName = "Another Boat 2",
            registrationNumber = "tn-5555",
            captainName = "Capt. Smith",
            homePort = "Kolkata",
            pin = "5566"
        )
        assertTrue("Case-insensitive duplicate must also fail", lowerCaseDuplicate.isFailure)
    }

    @Test
    fun register_rejectsBlankInputs() {
        val blankName = repository.register("", "REG-1", "Cap", "Port", "1234")
        assertTrue(blankName.isFailure)

        val blankReg = repository.register("Boat", "", "Cap", "Port", "1234")
        assertTrue(blankReg.isFailure)

        val blankCaptain = repository.register("Boat", "REG-2", "", "Port", "1234")
        assertTrue(blankCaptain.isFailure)

        val blankPort = repository.register("Boat", "REG-3", "Cap", "", "1234")
        assertTrue(blankPort.isFailure)

        val invalidPin = repository.register("Boat", "REG-4", "Cap", "Port", "12")
        assertTrue(invalidPin.isFailure)
    }

    // ==========================================
    // 3. Login Tests
    // ==========================================

    @Test
    fun login_successWithCorrectCredentials() {
        // First register
        repository.register(
            vesselName = "Mariner One",
            registrationNumber = "KL-1234",
            captainName = "Capt. Thomas",
            homePort = "Vizhinjam",
            pin = "5678"
        )
        repository.logout()
        assertFalse(repository.isLoggedIn())

        // Login
        val loginResult = repository.login("KL-1234", "5678")
        assertTrue("Login with correct credentials should succeed", loginResult.isSuccess)
        assertEquals("Mariner One", loginResult.getOrNull()?.vesselName)
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun login_failureOnWrongPin() {
        repository.register(
            vesselName = "Mariner One",
            registrationNumber = "KL-1234",
            captainName = "Capt. Thomas",
            homePort = "Vizhinjam",
            pin = "5678"
        )
        repository.logout()

        val wrongPinResult = repository.login("KL-1234", "0000")
        assertTrue("Login with wrong PIN must fail", wrongPinResult.isFailure)
        assertTrue(wrongPinResult.exceptionOrNull()?.message?.contains("Incorrect PIN") == true)
        assertFalse(repository.isLoggedIn())
    }

    @Test
    fun login_failureOnUnknownRegistrationNumber() {
        val unknownRegResult = repository.login("NONEXISTENT-999", "1234")
        assertTrue("Login with unknown registration must fail", unknownRegResult.isFailure)
        assertTrue(
            unknownRegResult.exceptionOrNull()?.message?.contains("Unknown registration number") == true
        )
        assertFalse(repository.isLoggedIn())
    }

    // ==========================================
    // 4. Demo Vessel & Session Lifecycle Tests
    // ==========================================

    @Test
    fun loginDemoVessel_initializesAndLogsInSampleAccount() {
        val demoResult = repository.loginDemoVessel()
        assertTrue(demoResult.isSuccess)

        val account = demoResult.getOrNull()
        assertNotNull(account)
        assertEquals(AuthRepository.DEMO_VESSEL_NAME, account?.vesselName)
        assertEquals(AuthRepository.DEMO_REG_NUMBER, account?.registrationNumber)
        assertTrue(repository.isLoggedIn())

        // Second call should log in existing demo vessel smoothly
        repository.logout()
        assertFalse(repository.isLoggedIn())

        val secondDemoResult = repository.loginDemoVessel()
        assertTrue(secondDemoResult.isSuccess)
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun logout_clearsActiveSession() {
        repository.loginDemoVessel()
        assertTrue(repository.isLoggedIn())

        repository.logout()
        assertFalse(repository.isLoggedIn())
        assertNull(repository.activeSession.value)
    }

    @Test
    fun sessionPersistence_restoresSessionAcrossAppRestarts() {
        // User registers and logs in
        repository.register(
            vesselName = "Polaris",
            registrationNumber = "POL-001",
            captainName = "Capt. Arun",
            homePort = "Tuticorin",
            pin = "2468"
        )
        assertTrue(repository.isLoggedIn())

        // Simulate app kill and new launch with same SharedPreferences
        val newRepositoryInstance = AuthRepository(fakePrefs)
        assertTrue("Session should be restored across instances", newRepositoryInstance.isLoggedIn())
        assertEquals("POL-001", newRepositoryInstance.activeSession.value?.registrationNumber)
        assertEquals("Polaris", newRepositoryInstance.activeSession.value?.vesselName)
    }
}
