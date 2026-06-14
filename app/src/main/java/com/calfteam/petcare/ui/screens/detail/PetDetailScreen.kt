package com.calfteam.petcare.ui.screens.detail

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun PetDetailScreen(pet: Pet, onBack: () -> Unit) {
    val context = LocalContext.current

    // Tangani tombol "Back" bawaan HP (Hardware Back Button)
    BackHandler {
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 1. Foto Hewan (Full lebar, di bagian atas)
        AsyncImage(
            model = pet.imageUrl,
            contentDescription = "Foto ${pet.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentScale = ContentScale.Crop
        )

        // Tombol Back di pojok kiri atas foto
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // 2. Card Info (Melengkung naik nutupin bawah foto sedikit)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 320.dp) // Offset biar melengkungnya tumpang tindih sama foto
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

                // Badge Status (Adopsi / Hilang)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Info Lokasi & Umur
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
                    // Buka aplikasi Telepon/Dialer bawaan HP
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${pet.contact}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
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