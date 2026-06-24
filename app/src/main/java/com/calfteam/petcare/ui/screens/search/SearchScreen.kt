package com.calfteam.petcare.ui.screens.search

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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

    // State
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var nearbyPets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var isLoadingPets by remember { mutableStateOf(false) }
    var selectedBreed by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var userLocationText by remember { mutableStateOf("") }

    // Available filters
    val statusOptions = listOf("Adoption", "Missing")
    val breeds = listOf("Anjing", "Kucing", "Kelinci", "Hamster", "Burung")

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            coroutineScope.launch {
                isLoadingLocation = true
                val result = locationRepository.getCurrentLocation()
                isLoadingLocation = false

                if (result.isSuccess) {
                    val (lat, lng) = result.getOrNull() ?: return@launch
                    userLocation = Pair(lat, lng)
                    userLocationText = "📍 ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}"
                    
                    // Auto search nearby pets
                    isLoadingPets = true
                    val searchResult = petRepository.getNearbyPets(
                        userLat = lat,
                        userLng = lng,
                        radiusKm = 5,
                        breed = selectedBreed,
                        status = selectedStatus,
                        locationRepo = locationRepository
                    )
                    isLoadingPets = false

                    if (searchResult.isSuccess) {
                        nearbyPets = searchResult.getOrNull() ?: emptyList()
                        if (nearbyPets.isEmpty()) {
                            Toast.makeText(context, "Tidak ada hewan terdekat", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Error: ${searchResult.exceptionOrNull()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Gagal mendapat lokasi: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    // Function untuk trigger search dengan filter
    val triggerSearch = remember {
        {
            if (userLocation != null) {
                coroutineScope.launch {
                    isLoadingPets = true
                    val result = petRepository.getNearbyPets(
                        userLat = userLocation!!.first,
                        userLng = userLocation!!.second,
                        radiusKm = 5,
                        breed = selectedBreed,
                        status = selectedStatus,
                        locationRepo = locationRepository
                    )
                    isLoadingPets = false

                    if (result.isSuccess) {
                        nearbyPets = result.getOrNull() ?: emptyList()
                        if (nearbyPets.isEmpty()) {
                            Toast.makeText(context, "Tidak ada hewan terdekat", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Error: ${result.exceptionOrNull()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                "🔍 Cari Hewan Terdekat",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00666E)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
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
                        coroutineScope.launch {
                            isLoadingLocation = true
                            val result = locationRepository.getCurrentLocation()
                            isLoadingLocation = false

                            if (result.isSuccess) {
                                val (lat, lng) = result.getOrNull() ?: return@launch
                                userLocation = Pair(lat, lng)
                                userLocationText = "📍 ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}"
                                triggerSearch()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Gagal mendapat lokasi: ${result.exceptionOrNull()?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00666E)),
                enabled = !isLoadingLocation,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Get Location",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isLoadingLocation) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Cari Lokasi Saya", fontWeight = FontWeight.Bold)
                }
            }

            if (userLocationText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    userLocationText,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (userLocation == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Aktifkan Lokasi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "Tekan tombol di atas untuk\nmencari hewan terdekat",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Filter Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = {
                                    selectedStatus = null
                                    triggerSearch()
                                },
                                label = { Text("Semua") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00666E),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(statusOptions) { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = {
                                    selectedStatus = status
                                    triggerSearch()
                                },
                                label = { Text(status) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00666E),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    Text("Filter Breed:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedBreed == null,
                                onClick = {
                                    selectedBreed = null
                                    triggerSearch()
                                },
                                label = { Text("Semua") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00666E),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(breeds) { breed ->
                            FilterChip(
                                selected = selectedBreed == breed,
                                onClick = {
                                    selectedBreed = breed
                                    triggerSearch()
                                },
                                label = { Text(breed) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00666E),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (isLoadingPets) {
                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF00666E))
                        }
                    }
                } else if (nearbyPets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada hewan terdekat 🐾", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                } else {
                    item {
                        Text(
                            "Hewan Terdekat (${nearbyPets.size}):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    items(nearbyPets) { pet ->
                        PetCard(
                            pet = pet,
                            onClick = { onPetSelected(pet) }
                        )
                    }
                }
            }
        }
    }
}