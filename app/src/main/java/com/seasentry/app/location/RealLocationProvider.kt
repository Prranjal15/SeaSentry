package com.seasentry.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Offline-capable GPS Location Provider.
 *
 * Direct standalone satellite positioning using android.location.LocationManager with
 * LocationManager.GPS_PROVIDER ONLY.
 *
 * Strictly zero dependencies on Google Play Services, FusedLocationProviderClient,
 * or LocationManager.NETWORK_PROVIDER. This ensures full operational capability in the middle of
 * the ocean without SIM, cellular towers, Wi-Fi networks, or internet-based A-GPS assistance data.
 */
class RealLocationProvider(
    private val context: Context? = null,
    private val locationManager: LocationManager? = context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
) {
    companion object {
        /** Conversion multiplier from meters per second to knots (nautical miles per hour) */
        const val MPS_TO_KNOTS = 1.9438445f
    }

    private val _locationState = MutableStateFlow(RealLocationState(fixState = GpsFixState.IDLE))
    val locationState: StateFlow<RealLocationState> = _locationState.asStateFlow()

    private var activeListener: LocationListener? = null
    private var isListening: Boolean = false
    private var lastRequestedIntervalMs: Long = 1000L
    private var lastRequestedDistanceM: Float = 0f

    /**
     * Checks if runtime ACCESS_FINE_LOCATION permission is granted.
     */
    fun hasLocationPermission(): Boolean {
        val ctx = context ?: return false
        return try {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if the device's hardware GPS provider is enabled in system settings.
     */
    fun isGpsEnabled(): Boolean {
        return try {
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Starts listening for raw hardware GPS satellite updates.
     * Seeds initial state with getLastKnownLocation() so the UI isn't blank during cold start.
     *
     * @param minIntervalMs Minimum time interval between location updates in milliseconds (default: 1000ms)
     * @param minDistanceMeters Minimum distance between location updates in meters (default: 0m)
     */
    fun start(minIntervalMs: Long = 1000L, minDistanceMeters: Float = 0f) {
        lastRequestedIntervalMs = minIntervalMs
        lastRequestedDistanceM = minDistanceMeters

        if (!hasLocationPermission()) {
            _locationState.value = RealLocationState(fixState = GpsFixState.PERMISSION_DENIED)
            return
        }

        if (!isGpsEnabled()) {
            _locationState.value = RealLocationState(fixState = GpsFixState.GPS_DISABLED)
            return
        }

        stop() // Clean up any active listener before registering a new one

        // Seed initial position from last known GPS fix to prevent a blank UI during satellite acquisition
        val seedState = seedFromLastKnownFix()
        _locationState.value = seedState

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val speedKnots = if (location.hasSpeed()) location.speed * MPS_TO_KNOTS else 0.0f
                val heading = if (location.hasBearing()) location.bearing else 0.0f
                val accuracy = if (location.hasAccuracy()) location.accuracy else null

                _locationState.value = RealLocationState(
                    fixState = GpsFixState.FIXED,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    speedKnots = speedKnots,
                    headingDegrees = heading,
                    accuracyMeters = accuracy,
                    seedFixAgeMs = null,
                    lastFixTimestamp = location.time
                )
            }

            override fun onProviderDisabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    _locationState.value = _locationState.value.copy(
                        fixState = GpsFixState.GPS_DISABLED
                    )
                }
            }

            override fun onProviderEnabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    if (_locationState.value.fixState == GpsFixState.GPS_DISABLED) {
                        _locationState.value = _locationState.value.copy(
                            fixState = GpsFixState.ACQUIRING
                        )
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // Maintained for backward compatibility on older API levels
            }
        }

        activeListener = listener
        isListening = true

        try {
            val looper = Looper.getMainLooper() ?: Looper.myLooper()
            if (looper != null) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minIntervalMs,
                    minDistanceMeters,
                    listener,
                    looper
                )
            } else {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minIntervalMs,
                    minDistanceMeters,
                    listener
                )
            }
        } catch (e: SecurityException) {
            _locationState.value = RealLocationState(fixState = GpsFixState.PERMISSION_DENIED)
            isListening = false
            activeListener = null
        } catch (e: Exception) {
            // In case GPS hardware cannot be accessed
            _locationState.value = RealLocationState(fixState = GpsFixState.GPS_DISABLED)
            isListening = false
            activeListener = null
        }
    }

    /**
     * Attempts to read the last known GPS location from hardware cache as a seed fix.
     */
    private fun seedFromLastKnownFix(): RealLocationState {
        val lastKnown = try {
            locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }

        return if (lastKnown != null) {
            val ageMs = (System.currentTimeMillis() - lastKnown.time).coerceAtLeast(0L)
            val speedKnots = if (lastKnown.hasSpeed()) lastKnown.speed * MPS_TO_KNOTS else 0.0f
            val heading = if (lastKnown.hasBearing()) lastKnown.bearing else 0.0f
            val accuracy = if (lastKnown.hasAccuracy()) lastKnown.accuracy else null

            RealLocationState(
                fixState = GpsFixState.ACQUIRING,
                latitude = lastKnown.latitude,
                longitude = lastKnown.longitude,
                speedKnots = speedKnots,
                headingDegrees = heading,
                accuracyMeters = accuracy,
                seedFixAgeMs = ageMs,
                lastFixTimestamp = lastKnown.time
            )
        } else {
            RealLocationState(fixState = GpsFixState.ACQUIRING)
        }
    }

    /**
     * Stops listening for GPS location updates and releases hardware resources.
     */
    fun stop() {
        activeListener?.let { listener ->
            try {
                locationManager?.removeUpdates(listener)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
        activeListener = null
        isListening = false
        _locationState.value = _locationState.value.copy(fixState = GpsFixState.IDLE)
    }

    /**
     * Re-evaluates permission and GPS hardware status, resuming tracking if conditions are met.
     */
    fun refreshAvailability() {
        val hasPerm = hasLocationPermission()
        val isGpsOn = isGpsEnabled()

        when {
            !hasPerm -> {
                if (isListening) stop()
                _locationState.value = RealLocationState(fixState = GpsFixState.PERMISSION_DENIED)
            }
            !isGpsOn -> {
                if (isListening) stop()
                _locationState.value = RealLocationState(fixState = GpsFixState.GPS_DISABLED)
            }
            _locationState.value.fixState == GpsFixState.PERMISSION_DENIED ||
            _locationState.value.fixState == GpsFixState.GPS_DISABLED -> {
                // Permission or GPS was restored; start listening
                start(lastRequestedIntervalMs, lastRequestedDistanceM)
            }
        }
    }

    /**
     * Helper for unit testing and fake injections.
     */
    fun updateStateForTesting(state: RealLocationState) {
        _locationState.value = state
    }
}
