package com.calfteam.petcare.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.*

class LocationRepository(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Get current user location (Lat, Lng)
     * Returns Pair<latitude, longitude>
     */
    suspend fun getCurrentLocation(): Result<Pair<Double, Double>> {
        return try {
            // Check permission
            if (!hasLocationPermission()) {
                return Result.failure(Exception("Location permission not granted"))
            }

            // Try GPS first, then NETWORK provider
            val location = getLastKnownLocation() ?: getLocationFromProvider()

            if (location != null) {
                Log.d("LocationRepo", "✓ Location found: ${location.latitude}, ${location.longitude}")
                Result.success(Pair(location.latitude, location.longitude))
            } else {
                Result.failure(Exception("Unable to get location"))
            }
        } catch (e: Exception) {
            Log.e("LocationRepo", "❌ Error getting location: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get last known location dari GPS atau NETWORK provider
     */
    private fun getLastKnownLocation(): Location? {
        return try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                // Return GPS jika tersedia, otherwise NETWORK
                gpsLocation ?: networkLocation
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("LocationRepo", "Error getting last known location: ${e.message}")
            null
        }
    }

    /**
     * Request location dari provider (fallback)
     */
    @Suppress("MissingPermission")
    private fun getLocationFromProvider(): Location? {
        return try {
            val hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            return when {
                hasGPS -> locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                hasNetwork -> locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                else -> null
            }
        } catch (e: Exception) {
            Log.w("LocationRepo", "Error requesting location: ${e.message}")
            null
        }
    }

    /**
     * Check jika location permission sudah granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Calculate distance antara 2 points menggunakan Haversine formula
     * Returns distance dalam kilometer
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Format distance ke format yang lebih readable
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 0.1 -> "< 100m"
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()}m"
            else -> String.format("%.1f km", distanceKm)
        }
    }
}
