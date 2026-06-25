package com.calfteam.petcare.ui.screens.search

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.LocationRepository
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.ui.components.PetCard
import com.calfteam.petcare.ui.screens.post.commonPetTypes
import kotlinx.coroutines.launch

// Brand & semantic colors — konsisten dengan screen lain
private val BrandTeal = Color(0xFF00666E)
private val BrandTealLight = Color(0xFFE0F2F1)
private val BackgroundColor = Color(0xFFFBF9F8)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    petRepository: PetRepository,
    onPetSelected: (Pet) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationRepository = remember { LocationRepository(context) }

    // Data
    var allPets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Filter state
    var searchText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var radiusKm by remember { mutableStateOf<Int?>(null) } // null = Semua jarak
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val statusOptions = listOf("Adoption", "Missing")
    val radiusOptions = listOf(1, 5, 10, 25)

    // Muat semua pets sekali; sisanya difilter in-memory (instan)
    LaunchedEffect(Unit) {
        isLoading = true
        petRepository.getAllPets()
            .onSuccess { allPets = it; isLoading = false }
            .onFailure {
                Toast.makeText(context, "Error load data: ${it.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    fun fetchLocation() {
        coroutineScope.launch {
            isLoadingLocation = true
            val result = locationRepository.getCurrentLocation()
            isLoadingLocation = false
            if (result.isSuccess) {
                val (lat, lng) = result.getOrNull() ?: return@launch
                userLocation = Pair(lat, lng)
                Toast.makeText(context, "✓ Lokasi diperbarui", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Gagal mendapat lokasi: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchLocation()
        } else {
            Toast.makeText(context, "Location permission diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            fetchLocation()
        }
    }

    // ===== Filtering (in-memory) =====
    val q = searchText.trim()
    val baseFiltered = allPets.filter { pet ->
        val matchesText = q.isEmpty() ||
                pet.name.contains(q, ignoreCase = true) ||
                pet.breed.contains(q, ignoreCase = true) ||
                pet.description.contains(q, ignoreCase = true)
        val matchesStatus = selectedStatus == null || pet.status.equals(selectedStatus, ignoreCase = true)
        val matchesType = selectedType == null || pet.breed.equals(selectedType, ignoreCase = true)
        matchesText && matchesStatus && matchesType
    }

    val loc = userLocation
    val activeRadius = radiusKm
    val filteredPets: List<Pet> = if (activeRadius != null && loc != null) {
        baseFiltered.mapNotNull { pet ->
            val parts = pet.distance.split(",")
            val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
            val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (lat == null || lng == null) return@mapNotNull null
            val km = locationRepository.calculateDistance(loc.first, loc.second, lat, lng)
            if (km > activeRadius) null
            else Pair(pet.copy(distance = locationRepository.formatDistance(km)), km)
        }.sortedBy { it.second }.map { it.first }
    } else {
        baseFiltered
    }

    // Count filter aktif (untuk badge)
    val activeFilterCount = listOfNotNull(
        selectedStatus,
        selectedType,
        radiusKm?.toString()
    ).size

    fun resetFilters() {
        selectedStatus = null
        selectedType = null
        radiusKm = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // ===== Header: search + lokasi ringkas =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundColor)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Title + subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cari Hewan",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Temukan hewan berdasarkan nama, ras, atau kategori",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search bar polished
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari nama, ras, deskripsi...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BrandTeal,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandTeal,
                    unfocusedBorderColor = DividerColor,
                    focusedLeadingIconColor = BrandTeal,
                    cursorColor = BrandTeal
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location row — compact, bukan tombol dominan
            LocationChip(
                location = loc,
                isLoading = isLoadingLocation,
                onActivate = { requestLocation() },
                onReset = { userLocation = null; radiusKm = null }
            )
        }

        // ===== Filter Card =====
        FilterCard(
            radiusKm = radiusKm,
            radiusOptions = radiusOptions,
            onRadiusSelect = {
                radiusKm = it
                if (userLocation == null) requestLocation()
            },
            selectedStatus = selectedStatus,
            statusOptions = statusOptions,
            onStatusSelect = { selectedStatus = it },
            selectedType = selectedType,
            onTypeSelect = { selectedType = it },
            activeCount = activeFilterCount,
            onReset = { resetFilters() }
        )

        // ===== Hasil =====
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = BrandTeal,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Memuat data...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hint jika radius dipilih tapi lokasi belum di-set
                if (radiusKm != null && userLocation == null) {
                    item {
                        LocationRequiredHint()
                    }
                }

                // Hasil header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (filteredPets.isEmpty()) "Tidak ada hasil"
                            else "Hasil (${filteredPets.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (activeFilterCount > 0) {
                            Text(
                                text = "$activeFilterCount filter aktif",
                                fontSize = 11.sp,
                                color = BrandTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (filteredPets.isEmpty()) {
                    item {
                        EmptySearchState(
                            searchText = q,
                            hasFilters = activeFilterCount > 0,
                            onReset = { resetFilters(); searchText = "" }
                        )
                    }
                } else {
                    items(filteredPets) { pet ->
                        PetCard(pet = pet, onClick = { onPetSelected(pet) })
                    }
                }
            }
        }
    }
}

/**
 * Chip lokasi yang compact — menggantikan tombol full-width yang terlalu dominan.
 */
@Composable
private fun LocationChip(
    location: Pair<Double, Double>?,
    isLoading: Boolean,
    onActivate: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (location != null) BrandTealLight
                else Color.White
            )
            .border(
                width = 1.dp,
                color = if (location != null) BrandTeal.copy(alpha = 0.3f)
                else DividerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isLoading) {
                if (location == null) onActivate() else onReset()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = BrandTeal,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = if (location != null) Icons.Default.LocationOn
                else Icons.Default.LocationOff,
                contentDescription = null,
                tint = if (location != null) BrandTeal else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (location != null) {
                Text(
                    text = "Lokasi aktif",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = String.format("%.4f, %.4f", location.first, location.second),
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Aktifkan lokasi saya",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Untuk filter berdasarkan jarak",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
        if (location != null && !isLoading) {
            Text(
                text = "Reset",
                fontSize = 12.sp,
                color = BrandTeal,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Card untuk semua filter (Jarak, Status, Jenis) dengan active count badge.
 */
@Composable
private fun FilterCard(
    radiusKm: Int?,
    radiusOptions: List<Int>,
    onRadiusSelect: (Int?) -> Unit,
    selectedStatus: String?,
    statusOptions: List<String>,
    onStatusSelect: (String?) -> Unit,
    selectedType: String?,
    onTypeSelect: (String?) -> Unit,
    activeCount: Int,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Header baris: judul + badge + reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = null,
                    tint = BrandTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (activeCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandTeal)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            AnimatedVisibility(visible = activeCount > 0) {
                Text(
                    text = "Reset",
                    fontSize = 12.sp,
                    color = BrandTeal,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onReset() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status filter
        FilterRow(
            label = "Status",
            options = listOf(null) + statusOptions.map { it as String? },
            selected = selectedStatus,
            onSelect = onStatusSelect,
            optionLabel = { it ?: "Semua" }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Jenis filter
        FilterRow(
            label = "Jenis",
            options = listOf(null) + commonPetTypes.map { it as String? },
            selected = selectedType,
            onSelect = onTypeSelect,
            optionLabel = { it ?: "Semua" }
        )

        // Jarak filter — hanya jika lokasi aktif
        if (radiusKm != null) {
            Spacer(modifier = Modifier.height(10.dp))
            FilterRow(
                label = "Jarak",
                options = listOf<Int?>(null) + radiusOptions.map { it },
                selected = radiusKm,
                onSelect = onRadiusSelect,
                optionLabel = { if (it == null) "Semua" else "$it km" }
            )
        }
    }
}

/**
 * Baris filter horizontal: label kecil di atas + LazyRow chip.
 */
@Composable
private fun <T> FilterRow(
    label: String,
    options: List<T?>,
    selected: T?,
    onSelect: (T?) -> Unit,
    optionLabel: (T?) -> String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(options) { option ->
                SearchFilterChip(
                    selected = selected == option,
                    label = optionLabel(option),
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun LocationRequiredHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF1E8))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MyLocation,
            contentDescription = null,
            tint = Color(0xFFFF6B35),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Tekan \"Aktifkan lokasi saya\" di atas untuk filter jarak.",
            fontSize = 12.sp,
            color = Color(0xFF8A4A1F),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptySearchState(
    searchText: String,
    hasFilters: Boolean,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BrandTealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (searchText.isNotEmpty()) "Tidak ada hasil untuk \"$searchText\""
            else "Tidak ada hewan ditemukan",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Coba kata kunci lain atau ubah filter",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (hasFilters) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTeal),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandTeal)
            ) {
                Text("Reset Filter", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SearchFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val containerColor = if (selected) BrandTeal else Color.White
    val contentColor = if (selected) Color.White else TextSecondary
    val borderColor = if (selected) BrandTeal else DividerColor

    Row(
        modifier = Modifier
            .shadow(
                elevation = if (selected) 2.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}