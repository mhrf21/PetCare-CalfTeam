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
import com.calfteam.petcare.data.repository.LocationRepository
import com.calfteam.petcare.data.repository.PetRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    petRepository: PetRepository,
    userName: String,
    userId: String,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationRepository = remember { LocationRepository(context) }

    // State form
    var petName by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var listingType by remember { mutableStateOf("Adoption") }

    // State gambar
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Buat Postingan Baru", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00666E))
        Spacer(modifier = Modifier.height(16.dp))

        // Pemilih Gambar
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
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("Klik untuk pilih foto", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pilihan Tipe Listing (Adoption / Missing)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = listingType == "Adoption",
                onClick = { listingType = "Adoption" },
                label = { Text("Adopsi") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00666E), selectedLabelColor = Color.White)
            )
            FilterChip(
                selected = listingType == "Missing",
                onClick = { listingType = "Missing" },
                label = { Text("Hilang (Missing)") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFB06A26), selectedLabelColor = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Input
        OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Nama Hewan") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Ras / Jenis") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Umur (cth: 2 Bulan)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Kontak (No HP)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        // Location Section dengan Hybrid GPS
        if (listingType == "Adoption") {
            // ADOPTION: GPS Auto-fill
            Text("📍 Lokasi Hewan (Auto GPS):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (location.isEmpty()) {
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

        // Tombol Submit
        Button(
            onClick = {
                if (selectedImageUri == null || petName.isEmpty() || breed.isEmpty()) {
                    Toast.makeText(context, "Lengkapi data dan foto!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                coroutineScope.launch {
                    isUploading = true
                    try {
                        // 1. Convert Uri ke File
                        val imageFile = petRepository.uriToFile(context, selectedImageUri!!)

                        // 2. Upload Gambar
                        val uploadResult = petRepository.uploadPetImage(imageFile)

                        if (uploadResult.isSuccess) {
                            val fileId = uploadResult.getOrNull() ?: ""
                            // 3. Simpan Postingan ke DB
                            val dbResult = petRepository.savePetPost(
                                name = petName,
                                breed = breed,
                                age = age,
                                desc = description,
                                type = listingType,
                                fileId = fileId,
                                contact = contact,
                                location = location,
                                uploaderName = userName,
                                userId = userId
                            )
                            if (dbResult.isSuccess) {
                                Toast.makeText(context, "Berhasil Posting!", Toast.LENGTH_SHORT).show()
                                onNavigateToHome() // Pindah ke tab Home
                            } else {
                                Toast.makeText(context, "Gagal simpan data", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Gagal upload gambar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isUploading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00666E))
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Posting Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(80.dp)) // Ruang untuk bottom nav
    }
}