package com.calfteam.petcare.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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

@Composable
fun HomeScreen(petRepository: PetRepository) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Pets") }
    var petsList by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoadingPets by remember { mutableStateOf(false) }
    val categories = listOf("🐾 All Pets", "🐶 Dogs", "🐱 Cats", "🚨 Missing")

    // Fetch pets saat screen pertama kali ditampilkan
    LaunchedEffect(Unit) {
        isLoadingPets = true
        petRepository.getAllPets().onSuccess { pets ->
            petsList = pets
            isLoadingPets = false
        }.onFailure { error ->
            Toast.makeText(context, "Gagal load data: ${error.message}", Toast.LENGTH_SHORT).show()
            isLoadingPets = false
        }
    }

    // Pindahkan isi dari `0 -> { ... }` (Home Feed) di MainActivity lu ke sini.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F8))
    ) {
        // Urgent Banner, Search Bar, Filter Chips... (Copy paste dari MainActivity)
        // LazyVerticalGrid untuk nampilin petsList...
    }
}