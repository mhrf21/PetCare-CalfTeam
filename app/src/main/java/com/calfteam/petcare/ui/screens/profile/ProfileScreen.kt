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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pets
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

private val Brand = Color(0xFF00666E)
private val BrandSoft = Color(0xFFE0F2F1)
private val DangerRed = Color(0xFFD32F2F)
private val Surface = Color(0xFFFBF9F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    authRepository: AuthRepository,
    petRepository: PetRepository,
    currentUserId: String,
    onPetSelected: (Pet) -> Unit,
    onEditProfile: () -> Unit,
    onNameChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var myPosts by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Brand)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Profil") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEditProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Keluar", color = DangerRed) },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = DangerRed) },
                                onClick = {
                                    menuExpanded = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== HEADER PROFIL (sticky visual di atas) =====
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar dengan inisial
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .background(Brand, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "P",
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (email.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = email, fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // ===== STATISTIK SAYA (kartu 3 kolom) =====
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatColumn(
                            value = myPosts.size.toString(),
                            label = "Postingan",
                            accent = Brand
                        )
                        VerticalDivider(color = Color(0xFFEEEEEE))
                        StatColumn(
                            value = myPosts.count { it.status.equals("Adoption", true) }.toString(),
                            label = "Adopsi",
                            accent = Brand
                        )
                        VerticalDivider(color = Color(0xFFEEEEEE))
                        StatColumn(
                            value = myPosts.count { it.status.equals("Missing", true) }.toString(),
                            label = "Hilang",
                            accent = Color(0xFFB06A26)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // ===== SECTION: Akun =====
            item {
                SectionTitle("Akun")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        AccountRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = email.ifEmpty { "Belum dimuat" },
                            valueColor = if (email.isEmpty()) Color.Gray else Color.Black
                        )
                        Divider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = Color(0xFFEEEEEE)
                        )
                        AccountRow(
                            icon = Icons.Default.Edit,
                            label = "Nama",
                            value = userName,
                            valueColor = Color.Black
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // ===== SECTION: Postingan Saya =====
            item {
                SectionTitleWithAction(
                    title = "Postingan Saya",
                    actionText = if (myPosts.isNotEmpty()) "${myPosts.size} total" else null
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Brand)
                    }
                }
            } else if (myPosts.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                items(myPosts, key = { it.id }) { pet ->
                    Box(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        PetCard(pet = pet, onClick = { onPetSelected(pet) })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialog konfirmasi logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Keluar dari akun?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Kamu perlu login lagi untuk masuk ke akun ini.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
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
                }) {
                    Text("Keluar", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun StatColumn(value: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun VerticalDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(color)
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 0.8.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, bottom = 8.dp, end = 24.dp)
    )
}

@Composable
private fun SectionTitleWithAction(title: String, actionText: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 0.8.sp
        )
        if (actionText != null) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                color = Brand,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AccountRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BrandSoft, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Brand, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(BrandSoft, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Pets,
                    contentDescription = null,
                    tint = Brand,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Belum ada postingan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Postingan yang kamu buat akan muncul di sini",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
