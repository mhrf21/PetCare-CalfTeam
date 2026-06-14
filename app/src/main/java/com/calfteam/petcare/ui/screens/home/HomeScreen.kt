package com.calfteam.petcare.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.ui.components.PetCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(petRepository: PetRepository, onPetSelected: (Pet) -> Unit) {
    val context = LocalContext.current
    var petsList by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoadingPets by remember { mutableStateOf(true) }

    // State untuk Filter & Search
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Pets") }
    val categories = listOf("All Pets", "Adoption", "Missing") // Disesuaikan dengan status di database lu

    // Fetch pets saat screen dibuka
    LaunchedEffect(Unit) {
        isLoadingPets = true
        petRepository.getAllPets().onSuccess { pets ->
            petsList = pets
            isLoadingPets = false
        }.onFailure { error ->
            Toast.makeText(context, "Error load data: ${error.message}", Toast.LENGTH_SHORT).show()
            isLoadingPets = false
        }
    }

    // Filter logika (Gabungan Search text & Kategori)
    val filteredPets = petsList.filter { pet ->
        val matchesSearch = pet.name.contains(searchText, ignoreCase = true) ||
                pet.breed.contains(searchText, ignoreCase = true)
        val matchesCategory = if (selectedCategory == "All Pets") true else pet.status.equals(selectedCategory, ignoreCase = true)

        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F8))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search for breeds, names...") },
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

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00666E),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Grid Daftar Hewan
        if (isLoadingPets) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00666E))
            }
        } else if (filteredPets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada hewan yang ditemukan 🐾", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredPets) { pet ->
                    PetCard(
                        pet = pet,
                        onClick = { onPetSelected(pet) } // TAMBAHKAN INI BIAR BISA DIKLIK
                    )
                }
            }
        }
    }
}