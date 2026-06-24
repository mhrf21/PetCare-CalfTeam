package com.calfteam.petcare.ui.screens.profile

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
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
import com.calfteam.petcare.data.repository.AuthRepository
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.ui.auth.LoginActivity
import com.calfteam.petcare.ui.components.PetCard
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userName: String,
    authRepository: AuthRepository,
    petRepository: PetRepository,
    currentUserId: String,
    onPetSelected: (Pet) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var myPosts by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Ambil email + postingan milik user. Re-run saat currentUserId terisi
    // (MainScreen mengisinya secara asinkron setelah Profil pertama kali tampil).
    LaunchedEffect(currentUserId) {
        authRepository.getCurrentUser()?.let { email = it.email }

        if (currentUserId.isBlank()) return@LaunchedEffect

        isLoading = true
        petRepository.getAllPets()
            .onSuccess { pets ->
                myPosts = pets.filter { it.userId == currentUserId }
                isLoading = false
            }
            .onFailure { error ->
                Toast.makeText(context, "Gagal memuat postingan: ${error.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F8)),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ===== Header Profil =====
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF00666E), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if (email.isNotEmpty()) {
                Text(text = email, fontSize = 14.sp, color = Color.Gray)
            }
            Text(
                text = "${myPosts.size} Postingan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00666E),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ===== Judul Section =====
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Postingan Saya",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ===== Daftar Postingan =====
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00666E))
                }
            }
        } else if (myPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada postingan 🐾", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            items(myPosts) { pet ->
                Box(modifier = Modifier.padding(bottom = 12.dp)) {
                    PetCard(pet = pet, onClick = { onPetSelected(pet) })
                }
            }
        }

        // ===== Tombol Logout =====
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = authRepository.logout()
                        if (result.isSuccess) {
                            Toast.makeText(context, "Berhasil Keluar", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        } else {
                            Toast.makeText(context, "Gagal Logout", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar (Logout)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
