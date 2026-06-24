package com.calfteam.petcare.ui.screens.search

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.LocationRepository
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.ui.components.PetCard
import com.calfteam.petcare.ui.screens.post.commonPetTypes
import kotlinx.coroutines.launch

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F8))
    ) {
        // ===== Header: judul + search + lokasi =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("🔍 Cari Hewan", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00666E))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari nama, ras, deskripsi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF00666E),
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { requestLocation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00666E)),
                enabled = !isLoadingLocation,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Lokasi", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                if (isLoadingLocation) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(if (loc == null) "Gunakan Lokasi Saya" else "Perbarui Lokasi", fontWeight = FontWeight.Bold)
                }
            }
            if (loc != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "📍 ${String.format("%.4f", loc.first)}, ${String.format("%.4f", loc.second)}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00666E))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Filter jarak
                item {
                    Text("Jarak:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        item {
                            SearchFilterChip(selected = radiusKm == null, label = "Semua") { radiusKm = null }
                        }
                        items(radiusOptions) { r ->
                            SearchFilterChip(selected = radiusKm == r, label = "$r km") {
                                radiusKm = r
                                if (userLocation == null) requestLocation()
                            }
                        }
                    }
                }

                // Filter status
                item {
                    Text("Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        item {
                            SearchFilterChip(selected = selectedStatus == null, label = "Semua") { selectedStatus = null }
                        }
                        items(statusOptions) { status ->
                            SearchFilterChip(selected = selectedStatus == status, label = status) { selectedStatus = status }
                        }
                    }
                }

                // Filter jenis
                item {
                    Text("Jenis:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        item {
                            SearchFilterChip(selected = selectedType == null, label = "Semua") { selectedType = null }
                        }
                        items(commonPetTypes) { type ->
                            SearchFilterChip(selected = selectedType == type, label = type) { selectedType = type }
                        }
                    }
                }

                if (radiusKm != null && userLocation == null) {
                    item {
                        Text(
                            "Tekan \"Gunakan Lokasi Saya\" agar filter jarak aktif.",
                            fontSize = 12.sp,
                            color = Color(0xFFB06A26)
                        )
                    }
                }

                // Hasil
                if (filteredPets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada hewan ditemukan 🐾", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                } else {
                    item {
                        Text(
                            "Hasil (${filteredPets.size}):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    items(filteredPets) { pet ->
                        PetCard(pet = pet, onClick = { onPetSelected(pet) })
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF00666E),
            selectedLabelColor = Color.White
        )
    )
}
