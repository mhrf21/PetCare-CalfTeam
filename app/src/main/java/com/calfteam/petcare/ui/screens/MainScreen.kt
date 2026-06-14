package com.calfteam.petcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.AuthRepository
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.ui.screens.detail.PetDetailScreen
import com.calfteam.petcare.ui.screens.home.HomeScreen
import com.calfteam.petcare.ui.screens.post.AddPostScreen
import com.calfteam.petcare.ui.screens.profile.ProfileScreen
import com.calfteam.petcare.ui.screens.search.SearchScreen
import com.calfteam.petcare.utils.AppwriteConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userName: String) {
    // 1. Inisialisasi Context & Repository
    val context = LocalContext.current
    val client = remember { AppwriteConfig.getClient(context) }
    val petRepository = remember { PetRepository(client) }
    val authRepository = remember { AuthRepository(context) }

    // 2. State Navigasi Bawah & Detail
    var selectedItem by remember { mutableStateOf(0) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }

    val bottomNavItems = listOf("Home", "Search", "Post", "Profile")
    val bottomNavIcons = listOf(Icons.Filled.Home, Icons.Filled.Search, Icons.Filled.AddCircle, Icons.Filled.Person)

    // 3. Logika Tampilan (Detail vs Navigasi Utama)
    if (selectedPet != null) {
        // TAMPILKAN LAYAR DETAIL (FULL SCREEN)
        PetDetailScreen(
            pet = selectedPet!!,
            onBack = { selectedPet = null } // Kalau di-back, hapus state hewannya
        )
    } else {
        // JIKA TIDAK ADA HEWAN YANG DIPILIH, TAMPILKAN LAYAR UTAMA
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
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
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
            floatingActionButton = {
                if (selectedItem == 0) {
                    FloatingActionButton(
                        onClick = { selectedItem = 2 }, // Pindah ke Post
                        containerColor = Color(0xFF00666E),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (selectedItem) {
                    0 -> HomeScreen(
                        petRepository = petRepository,
                        onPetSelected = { pet -> selectedPet = pet } // TANGKAP KLIK DARI HOME
                    )
                    1 -> SearchScreen(petRepository = petRepository)
                    2 -> AddPostScreen(
                        petRepository = petRepository,
                        onNavigateToHome = { selectedItem = 0 }
                    )
                    3 -> ProfileScreen(
                        userName = userName,
                        authRepository = authRepository
                    )
                }
            }
        }
    }
}