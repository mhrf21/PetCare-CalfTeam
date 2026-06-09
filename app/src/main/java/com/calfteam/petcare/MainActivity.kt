package com.calfteam.petcare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calfteam.petcare.data.repository.AuthRepository
import com.calfteam.petcare.ui.auth.LoginActivity
import kotlinx.coroutines.launch
import com.calfteam.petcare.data.model.Pet
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.calfteam.petcare.utils.AppwriteConfig
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.utils.Constants // Pastikan ini juga diimport

// Nanti di onClick tinggal pakai petRepo.uploadPetImage(file)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tangkap nama user yang dikirim dari LoginScreen
        val userName = intent.getStringExtra("userName") ?: "Pecinta Hewan"

        setContent {
            MainScreenUI(userName = userName)
        }
    }
}

// PetCard Composable
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetCard(pet: Pet, onClick: () -> Unit) {
    val isMissing = pet.status == "Missing"
    val badgeColor = if (isMissing) Color(0xFFBA1A1A) else Color(0xFF00666E)
    val buttonColor = if (isMissing) Color(0xFF8C4A23) else Color(0xFF00666E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Bagian Gambar & Badge Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Tampilkan gambar dari Appwrite Storage
                if (pet.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = pet.imageUrl,
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.ImageNotSupported, contentDescription = "No image", tint = Color.Gray)
                }

                // Teks status di pojok kanan atas
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(badgeColor, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = pet.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bagian Informasi Teks
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = pet.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(text = pet.distance, color = Color.Gray, fontSize = 11.sp)
                }

                Text(text = "${pet.breed} • ${pet.age}", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Jika hewan Hilang, tampilkan info lokasi terakhir
                if (isMissing && pet.lastSeen != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFE8DC), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Text(text = pet.lastSeen, color = Color(0xFF783912), fontSize = 10.sp, lineHeight = 14.sp)
                    }
                } else {
                    // Jika hewan Tersedia, tampilkan tag/fitur
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        pet.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = tag, color = Color.DarkGray, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Aksi Utama
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isMissing) "I've Seen ${pet.name}" else "View Profile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenUI(userName: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AuthRepository(context) }

    var selectedItem by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Pets") }
    val client = remember { AppwriteConfig.getClient(context) }
    val authRepository = remember { AuthRepository(context) }
    val petRepository = remember { PetRepository(client) }

    // State untuk menyimpan pets dari database
    var petsList by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoadingPets by remember { mutableStateOf(false) }

    // Fetch pets saat screen pertama kali ditampilkan atau saat kembali ke Home
    LaunchedEffect(selectedItem) {
        if (selectedItem == 0) { // Home screen
            isLoadingPets = true
            petRepository.getAllPets().onSuccess { pets ->
                petsList = pets
                isLoadingPets = false
            }.onFailure { error ->
                Toast.makeText(context, "Gagal load data: ${error.message}", Toast.LENGTH_SHORT).show()
                isLoadingPets = false
            }
        }
    }

    val bottomNavItems = listOf("Home", "Search", "Post", "Profile")
    val bottomNavIcons = listOf(Icons.Filled.Home, Icons.Filled.Search, Icons.Filled.AddCircle, Icons.Filled.Person)
    val categories = listOf("🐾 All Pets", "🐶 Dogs", "🐱 Cats", "🚨 Missing")

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

// Launcher Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Pets, contentDescription = "Logo", tint = Color(0xFF00666E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "PetCare", fontWeight = FontWeight.Bold, color = Color(0xFF00666E))
                    }
                },
                actions = {
                    IconButton(onClick = { /* NOTIFIKASI */ }) {
                        Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notifikasi", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = bottomNavIcons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00666E),
                            selectedTextColor = Color(0xFF00666E),
                            indicatorColor = Color(0xFFE0F2F1)
                        )
                    )
                }
            }
        },
        // Sesuai Solusi 1: FAB (+) sekarang muncul untuk SIAPAPUN selama berada di Home
        floatingActionButton = {
            if (selectedItem == 0) {
                FloatingActionButton(
                    onClick = { selectedItem = 2 }, // Pindah ke menu Post saat diklik
                    containerColor = Color(0xFF00666E),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        when (selectedItem) {
            0 -> { // --- HOME FEED ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFFBF9F8))
                ) {
                    // Urgent Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFA676))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "URGENT: Missing Golden Retriever \"Buddy\" seen near Park Avenue.",
                            color = Color(0xFF783912),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text("Search for breeds, locations, or names...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00666E),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF5F3F3),
                            unfocusedContainerColor = Color(0xFFF5F3F3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00666E),
                                    selectedLabelColor = Color.White,
                                    disabledContainerColor = Color(0xFFEAE8E7)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Nearby Community",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Black
                    )

                    // Grid List Hewan dari Database
                    if (isLoadingPets) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00666E))
                        }
                    } else if (petsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada listing hewan", color = Color.Gray)
                        }
                    } else {
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(petsList.size) { index ->
                                val pet = petsList[index]
                                PetCard(pet = pet, onClick = {
                                    Toast.makeText(context, "Membuka profil ${pet.name}", Toast.LENGTH_SHORT).show()
                                })
                            }
                        }
                    }
                }
            }
            2 -> { // --- HALAMAN TAMBAH POST HEWAN (Mockup Baru) ---
                var listingType by remember { mutableStateOf("Adoption") }
                var petName by remember { mutableStateOf("") }
                var petBreed by remember { mutableStateOf("") }
                var petAge by remember { mutableStateOf("") }
                var petDescription by remember { mutableStateOf("") }
                var petTagsText by remember { mutableStateOf("") }
                var petContact by remember { mutableStateOf("") }
                var petLocation by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color.White)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- BAGIAN 1: FOTO HEWAN ---

                    Text("Pet Photos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                            .clickable { galleryLauncher.launch("image/*") }, // Buka galeri pas diklik
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            // Tampilkan gambar yang dipilih
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Pet Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Add", tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- BAGIAN 2: LISTING TYPE ---
                    Text("Listing Type", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAdoption = listingType == "Adoption"
                        Button(
                            onClick = { listingType = "Adoption" },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAdoption) Color(0xFF00666E) else Color(0xFFF5F3F3)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Pets, contentDescription = null, tint = if (isAdoption) Color.White else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adoption", color = if (isAdoption) Color.White else Color.DarkGray)
                        }

                        val isMissing = listingType == "Missing"
                        Button(
                            onClick = { listingType = "Missing" },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMissing) Color(0xFF00666E) else Color(0xFFF5F3F3)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Campaign, contentDescription = null, tint = if (isMissing) Color.White else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Missing", color = if (isMissing) Color.White else Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- BAGIAN 3: FORM INPUT ---
                    Text("Pet Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        placeholder = { Text("e.g. Buddy") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Breed", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = petBreed,
                                onValueChange = { petBreed = it },
                                placeholder = { Text("e.g. Persian") }, // Ganti placeholder jadi contoh
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Age", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = petAge,
                                onValueChange = { petAge = it },
                                placeholder = { Text("e.g. 2 years") }, // Ganti placeholder jadi contoh
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Description", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = petDescription,
                        onValueChange = { petDescription = it },
                        placeholder = {
                            if (listingType == "Adoption") {
                                Text("Describe Buddy's personality, medical history, or habits...")
                            } else {
                                Text("Where was Buddy last seen? Any distinct marks or collar?")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tags (Optional - pisah dengan koma)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = petTagsText,
                        onValueChange = { petTagsText = it },
                        placeholder = { Text("e.g. Vaccinated, Friendly, Needs Garden") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Contact Info", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = petContact,
                        onValueChange = { petContact = it },
                        placeholder = { Text("e.g. +62 812 3456 7890 or email@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Location", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = petLocation,
                        onValueChange = { petLocation = it },
                        placeholder = { Text("e.g. Jakarta Pusat, Kota Tua") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                selectedImageUri?.let { uri ->
                                    try {
                                        // Validasi input
                                        if (petName.isBlank()) {
                                            Toast.makeText(context, "Nama hewan wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        if (petBreed.isBlank()) {
                                            Toast.makeText(context, "Breed wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        if (petAge.isBlank()) {
                                            Toast.makeText(context, "Umur wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        if (petContact.isBlank()) {
                                            Toast.makeText(context, "Kontak wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        if (petLocation.isBlank()) {
                                            Toast.makeText(context, "Lokasi wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }

                                        // 1. Convert URI ke File
                                        val file = petRepository.uriToFile(context, uri)

                                        // 2. Upload gambar
                                        val uploadResult = petRepository.uploadPetImage(file)

                                        uploadResult.onSuccess { fileId ->
                                            // 3. Simpan ke database
                                            val tagsList = petTagsText
                                                .split(",")
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                            
                                            val dbResult = petRepository.savePetPost(
                                                name = petName,
                                                breed = petBreed,
                                                age = petAge,
                                                desc = petDescription,
                                                type = listingType,
                                                fileId = fileId,
                                                tags = tagsList,
                                                contact = petContact,
                                                location = petLocation
                                            )

                                            dbResult.onSuccess {
                                                Toast.makeText(context, "Postingan berhasil dibuat!", Toast.LENGTH_LONG).show()
                                                // Reset form
                                                petName = ""
                                                petBreed = ""
                                                petAge = ""
                                                petDescription = ""
                                                petTagsText = ""
                                                petContact = ""
                                                petLocation = ""
                                                selectedImageUri = null
                                                listingType = "Adoption"
                                                selectedItem = 0 // Kembali ke Home
                                            }.onFailure { error ->
                                                Toast.makeText(context, "Gagal simpan: ${error.message ?: "Error tidak diketahui"}", Toast.LENGTH_LONG).show()
                                                android.util.Log.e("PostError", "DB Error: ${error.stackTraceToString()}")
                                            }
                                        }.onFailure { error ->
                                            Toast.makeText(context, "Gagal upload gambar: ${error.message ?: "Error tidak diketahui"}", Toast.LENGTH_LONG).show()
                                            android.util.Log.e("PostError", "Upload Error: ${error.stackTraceToString()}")
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        android.util.Log.e("PostError", "Catch Error: ${e.stackTraceToString()}")
                                    }
                                } ?: Toast.makeText(context, "Mohon pilih foto terlebih dahulu", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00666E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Post to Community", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            3 -> { // --- HALAMAN USER PROFILE (Sesuai image_6a8cdf.png) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Color(0xFFFBF9F8))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(120.dp),
                        tint = Color(0xFF00666E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Menampilkan Nama Asli User secara dinamis
                    Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)

                    Spacer(modifier = Modifier.height(40.dp))

                    // SIGN OUT BUTTON
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = repository.logout()
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Berhasil Keluar Akun", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    context.startActivity(intent)
                                    (context as? Activity)?.finish()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = "Sign Out")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Halaman ${bottomNavItems[selectedItem]} Segera Datang")
                }
            }
        }
    }
}