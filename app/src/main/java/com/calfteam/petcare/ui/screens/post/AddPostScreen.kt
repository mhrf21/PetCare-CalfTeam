package com.calfteam.petcare.ui.screens.post

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.calfteam.petcare.data.repository.PetRepository
import kotlinx.coroutines.launch

@Composable
fun AddPostScreen(
    petRepository: PetRepository,
    onNavigateToHome: () -> Unit // Callback untuk kembali ke Home setelah sukses post
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    // State Form
    var listingType by remember { mutableStateOf("Adoption") }
    // ... Copy paste semua state petName, petBreed, dll ...

    // Pindahkan isi dari `2 -> { ... }` (Form Input) di MainActivity lu ke sini.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // UI Foto, Listing Type, Form Input...

        // Di bagian dbResult.onSuccess { ... }, panggil onNavigateToHome()
        // menggantikan `selectedItem = 0` yang lama
    }
}