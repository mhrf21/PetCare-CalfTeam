package com.calfteam.petcare.ui.screens.post

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.LocationRepository
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.utils.LocationUtils
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
    var address by remember { mutableStateOf(pet.address) }
    var listingType by remember { mutableStateOf(pet.status) }

    // State gambar
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }

    // State untuk Map Picker overlay
    var showMapPicker by remember { mutableStateOf(false) }

    // Reverse geocode sekali jika post lama punya koordinat tapi belum ada address
    LaunchedEffect(Unit) {
        if (address.isBlank() && location.contains(",")) {
            val parts = location.split(",")
            val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
            val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val resolved = LocationUtils.reverseGeocode(context, lat, lng)
                if (resolved != null) address = resolved
            }
        }
    }

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
                    val resolvedAddress = LocationUtils.reverseGeocode(context, lat, lng)
                    address = resolvedAddress ?: ""
                    Toast.makeText(
                        context,
                        if (resolvedAddress != null) "Lokasi & alamat didapat"
                        else "Lokasi didapat (alamat tidak tersedia)",
                        Toast.LENGTH_SHORT
                    ).show()
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

    fun requestGps() {
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
                    val resolvedAddress = LocationUtils.reverseGeocode(context, lat, lng)
                    address = resolvedAddress ?: ""
                    Toast.makeText(
                        context,
                        if (resolvedAddress != null) "Lokasi & alamat didapat"
                        else "Lokasi didapat (alamat tidak tersedia)",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Gagal: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun resetLocation() {
        location = ""
        address = ""
    }

    // Map picker overlay (full-screen) — tampil di atas form saat aktif
    if (showMapPicker) {
        val initialCoords = location.split(",").let { parts ->
            val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
            val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (lat != null && lng != null) Pair(lat, lng) else null
        }
        MapPickerScreen(
            initialLat = initialCoords?.first,
            initialLng = initialCoords?.second,
            onConfirm = { lat, lng, addr ->
                location = "$lat,$lng"
                address = addr
                showMapPicker = false
            },
            onCancel = { showMapPicker = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    "Edit Postingan",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundColor
            )
        )

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Image picker (fallback ke gambar existing)
            SectionHeader(title = "Foto Hewan")
            Spacer(modifier = Modifier.height(12.dp))
            PostImagePicker(
                imageUri = selectedImageUri,
                fallbackUrl = pet.imageUrl,
                onPick = { galleryLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Listing type
            SectionHeader(title = "Tipe Listing")
            Spacer(modifier = Modifier.height(12.dp))
            ListingTypeSelector(
                selected = listingType,
                onSelect = { listingType = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Detail form
            SectionHeader(title = "Detail Hewan")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = "Nama Hewan",
                    icon = Icons.Default.Pets,
                    placeholder = "Cth: Mochi"
                )
                PetTypeDropdown(
                    value = breed,
                    onValueChange = { breed = it },
                    modifier = Modifier.fillMaxWidth()
                )
                IconTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = "Umur",
                    icon = Icons.Default.CalendarToday,
                    placeholder = "Cth: 2 Bulan"
                )
                IconTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = "Kontak (No HP)",
                    icon = Icons.Default.Phone,
                    placeholder = "08xxxxxxxxxx",
                    keyboardType = KeyboardType.Phone
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Location
            SectionHeader(
                title = if (listingType.equals("Adoption", true)) "Lokasi Hewan" else "Lokasi Terakhir Dilihat"
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (listingType.equals("Adoption", true)) {
                AdoptionLocationSection(
                    location = location,
                    address = address,
                    isGettingLocation = isGettingLocation,
                    onGetLocation = { requestGps() },
                    onReset = { resetLocation() },
                    onOpenMapPicker = { showMapPicker = true }
                )
            } else {
                MissingLocationSection(
                    address = address,
                    location = location,
                    isGettingLocation = isGettingLocation,
                    onAddressChange = { address = it },
                    onGetLocation = { requestGps() },
                    onReset = { resetLocation() },
                    onOpenMapPicker = { showMapPicker = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Description
            SectionHeader(title = "Cerita & Deskripsi")
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    placeholder = {
                        Text(
                            "Cth: Kucing jinak, suka dipeluk, sudah vaksin...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandTeal,
                        focusedLabelColor = BrandTeal,
                        cursorColor = BrandTeal,
                        unfocusedBorderColor = DividerColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Tombol Update
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
                                location = location,
                                address = address
                            )

                            if (updateResult.isSuccess) {
                                Toast.makeText(
                                    context,
                                    "Postingan berhasil diupdate!",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Simpan Perubahan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}