package com.calfteam.petcare.ui.screens.post

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.LocationRepository
import com.calfteam.petcare.data.repository.PetRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    pet: Pet,
    petRepository: PetRepository,
    onBack: () -> Unit,
    onEditSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationRepository = remember { LocationRepository(context) }

    // State form
    var petName by remember { mutableStateOf(pet.name) }
    var breed by remember { mutableStateOf(pet.breed) }
    var age by remember { mutableStateOf(pet.age) }
    var description by remember { mutableStateOf(pet.description) }
    var contact by remember { mutableStateOf(pet.contact) }
    var location by remember { mutableStateOf(pet.distance) }
    var listingType by remember { mutableStateOf(pet.status) }

    // State gambar
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }

    // Permission launcher untuk location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            coroutineScope.launch {
                isGettingLocation = true
                val result = locationRepository.getCurrentLocation()
                isGettingLocation = false

                if (result.isSuccess) {
                    val (lat, lng) = result.getOrNull() ?: return@launch
                    location = "$lat,$lng"
                    Toast.makeText(context, "✓ Lokasi didapat", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Gagal mendapat lokasi: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(context, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Edit Postingan", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Pemilih Gambar (Opsional - untuk ganti foto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0F0F0))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "New Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = pet.imageUrl,
                            contentDescription = "Current Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Text(
                "Tap untuk ganti foto (opsional)",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pilihan Tipe Listing
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = listingType == "Adoption",
                    onClick = { listingType = "Adoption" },
                    label = { Text("Adopsi") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00666E),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = listingType == "Missing",
                    onClick = { listingType = "Missing" },
                    label = { Text("Hilang (Missing)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFB06A26),
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form Input
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Nama Hewan") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            PetTypeDropdown(
                value = breed,
                onValueChange = { breed = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Umur (cth: 2 Bulan)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Kontak (No HP)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Location Section dengan Hybrid GPS
            if (listingType == "Adoption") {
                // ADOPTION: GPS Auto-fill
                Text("📍 Lokasi Hewan (Auto GPS):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (location.isEmpty() || location == pet.distance) {
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
                                    isGettingLocation = true
                                    val result = locationRepository.getCurrentLocation()
                                    isGettingLocation = false

                                    if (result.isSuccess) {
                                        val (lat, lng) = result.getOrNull() ?: return@launch
                                        location = "$lat,$lng"
                                        Toast.makeText(context, "✓ Lokasi didapat", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Gagal: ${result.exceptionOrNull()?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00666E)),
                        enabled = !isGettingLocation,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Get Location", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isGettingLocation) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Gunakan Lokasi Saya", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0F0F0)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("✓ Lokasi Terisi", fontSize = 12.sp, color = Color.Gray)
                            Text(location, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00666E))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { location = "" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                    ) {
                        Text("Ubah Lokasi", fontSize = 12.sp)
                    }
                }
            } else {
                // MISSING: Manual Input + GPS Helper
                Text("📍 Lokasi Terakhir Dilihat:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Misal: Jakarta Barat atau -6.2088,106.8456") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Masukkan lokasi terakhir dilihat", fontSize = 12.sp) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
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
                                isGettingLocation = true
                                val result = locationRepository.getCurrentLocation()
                                isGettingLocation = false

                                if (result.isSuccess) {
                                    val (lat, lng) = result.getOrNull() ?: return@launch
                                    location = "$lat,$lng"
                                    Toast.makeText(context, "✓ Lokasi helper diterapkan (bisa diedit)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Gagal: ${result.exceptionOrNull()?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isGettingLocation,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Get Location", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isGettingLocation) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Helper: Lokasi Saya", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Singkat") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Update
            Button(
                onClick = {
                    if (petName.isEmpty() || breed.isEmpty()) {
                        Toast.makeText(context, "Lengkapi data!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isUpdating = true
                        try {
                            // 1. Jika ada gambar baru, upload dulu
                            if (selectedImageUri != null) {
                                val imageFile = petRepository.uriToFile(context, selectedImageUri!!)
                                val imageResult = petRepository.updatePetImage(
                                    documentId = pet.id,
                                    oldImageId = pet.imageUrl, // Simplified - ideally store imageId
                                    newImageFile = imageFile
                                )
                                if (!imageResult.isSuccess) {
                                    Toast.makeText(context, "Gagal update gambar", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                            }

                            // 2. Update data postingan
                            val updateResult = petRepository.updatePetPost(
                                documentId = pet.id,
                                name = petName,
                                breed = breed,
                                age = age,
                                desc = description,
                                type = listingType,
                                contact = contact,
                                location = location
                            )

                            if (updateResult.isSuccess) {
                                Toast.makeText(context, "Postingan berhasil diupdate! ✓", Toast.LENGTH_SHORT).show()
                                onEditSuccess()
                            } else {
                                val errorMsg = updateResult.exceptionOrNull()?.message ?: "Error tidak diketahui"
                                Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isUpdating = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00666E))
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
