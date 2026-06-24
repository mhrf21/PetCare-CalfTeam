package com.calfteam.petcare.ui.screens.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
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
import coil.compose.AsyncImage
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.PetRepository
import kotlinx.coroutines.launch

@Composable
fun PetDetailScreen(
    pet: Pet,
    petRepository: PetRepository,
    currentUserId: String,
    onBack: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 👇 State untuk memunculkan dialog konfirmasi hapus
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Tangani tombol "Back" bawaan HP
    BackHandler {
        onBack()
    }

    // 👇 Pop-up Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Postingan") },
            text = { Text("Yakin mau hapus data ini? Tindakan ini nggak bisa dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            val result = petRepository.deletePetWithLog(pet.id)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Postingan berhasil dihapus ✓", Toast.LENGTH_SHORT).show()
                                onDeleteSuccess() // Kembali ke Home setelah dihapus
                            } else {
                                val errorMsg = result.exceptionOrNull()?.message ?: "Error tidak diketahui"
                                Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 1. Foto Hewan
        AsyncImage(
            model = pet.imageUrl,
            contentDescription = "Foto ${pet.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentScale = ContentScale.Crop
        )

        // 👇 Header Tombol Back & Delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tombol Back
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // 👇 Tombol Delete (HANYA MUNCUL JIKA ID USER COCOK) 👇
            if (currentUserId.isNotEmpty() && currentUserId == pet.userId) {
                IconButton(
                    onClick = { showDeleteDialog = true }, // Munculin dialog
                    modifier = Modifier.background(Color.Red.copy(alpha = 0.8f), shape = CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        }

        // 2. Card Info (Melengkung)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 320.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFFFBF9F8))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header: Nama & Tipe Listing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pet.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Badge Status
                val badgeColor = if (pet.status.equals("Adoption", ignoreCase = true)) Color(0xFF00666E) else Color(0xFFB06A26)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeColor.copy(alpha = 0.1f),
                    contentColor = badgeColor
                ) {
                    Text(
                        text = pet.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ras/Breed
            Text(text = "Ras: ${pet.breed}", fontSize = 16.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))

            // Uploader Name
            Text(
                text = "Diposting oleh: ${pet.uploaderName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00666E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info Lokasi
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF00666E))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = pet.distance, fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Deskripsi
            Text(text = "Tentang ${pet.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pet.description.ifEmpty { "Tidak ada deskripsi tambahan dari pemilik." },
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Kontak Pemilik
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${pet.contact}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00666E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Hubungi", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hubungi Pemilik", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}