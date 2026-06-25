package com.calfteam.petcare.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Utility untuk konversi antara koordinat (lat,lng) dan alamat terbaca.
 * Geocoder butuh koneksi internet & opsional layanan Google Play di device.
 */
object LocationUtils {

    /**
     * Reverse geocoding: lat,lng → alamat terbaca (contoh: "Jl. Sudirman, Jakarta Selatan").
     * Return null jika Geocoder tidak tersedia, jaringan gagal, atau hasil kosong.
     */
    suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null

            val geocoder = Geocoder(context, Locale.forLanguageTag("id-ID"))
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<List<Address>?> { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { result ->
                        cont.resume(result)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }

            addresses?.firstOrNull()?.let { formatAddress(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Memformat object Address menjadi string yang ringkas dan terbaca.
     * Menghindari duplikasi (mis. "Jakarta" sebagai locality & admin area)
     * dan menghindari nama gedung/nomor yang terlalu panjang.
     */
    private fun formatAddress(address: Address): String? {
        // Kumpulkan kandidat bagian alamat
        val rawParts = listOf(
            address.subLocality,           // kelurahan/desa (mis. "Kebayoran Baru")
            address.locality,              // kota (mis. "Jakarta Selatan")
            address.subAdminArea,          // kabupaten (mis. "DKI Jakarta")
            address.adminArea              // provinsi
        ).filter { !it.isNullOrBlank() }

        // Hapus duplikat sambil mempertahankan urutan
        val parts = rawParts.distinct()

        // Hindari alamat yang cuma berisi angka (fallback ke getAddressLine)
        if (parts.isEmpty() || parts.all { it.matches(Regex("\\d+")) }) {
            val line = address.getAddressLine(0)
            return line?.takeIf { it.isNotBlank() }
        }

        // Batasi max 3 bagian agar tidak kepanjangan
        val limited = parts.take(3).joinToString(", ")
        return limited.ifBlank { address.getAddressLine(0) }
    }
}