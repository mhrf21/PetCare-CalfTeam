package com.calfteam.petcare.ui.screens.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.data.repository.PetRepository
import com.calfteam.petcare.ui.screens.post.EditPostScreen
import kotlinx.coroutines.launch

// Brand & semantic colors (konsisten dengan screen lain)
private val BrandTeal = Color(0xFF00666E)
private val BrandTealDeep = Color(0xFF008B95)
private val BrandTealLight = Color(0xFFE0F2F1)
private val BrandTealSoft = Color(0xFFF0F9FA)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B7280)
private val BackgroundColor = Color(0xFFFBF9F8)
private val DividerColor = Color(0xFFE5E7EB)
private val MissingColor = Color(0xFFFF6B6B)
private val MissingColorLight = Color(0xFFFFE5E5)
private val ResolvedColor = Color(0xFF2E7D32)
private val ResolvedColorLight = Color(0xFFE8F5E9)

@Composable
fun PetDetailScreen(
    pet: Pet,
    petRepository: PetRepository,
    currentUserId: String,
    onBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    onEditSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 👇 State untuk memunculkan dialog konfirmasi hapus
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditScreen by remember { mutableStateOf(false) }

    // 👇 State status "selesai" (sudah ditemukan / diadopsi)
    var resolved by remember { mutableStateOf(pet.resolved) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var isUpdatingResolved by remember { mutableStateOf(false) }

    val isMissing = pet.status.equals("Missing", ignoreCase = true)
    val statusAccent = if (isMissing) MissingColor else BrandTeal
    val statusAccentLight = if (isMissing) MissingColorLight else BrandTealLight
    val resolvedLabel = if (isMissing) "Sudah Ditemukan" else "Sudah Diadopsi"
    val markActionLabel = if (isMissing) "Tandai Sudah Ditemukan" else "Tandai Sudah Diadopsi"
    val isOwner = currentUserId.isNotEmpty() && currentUserId == pet.userId

    fun setResolved(value: Boolean) {
        coroutineScope.launch {
            isUpdatingResolved = true
            val result = petRepository.setPetResolved(pet.id, value)
            isUpdatingResolved = false
            if (result.isSuccess) {
                resolved = value
                Toast.makeText(
                    context,
                    if (value) "Ditandai: $resolvedLabel ✓" else "Postingan dibuka kembali",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "Gagal: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Tangani tombol "Back" bawaan HP
    BackHandler {
        onBack()
    }

    // Jika edit screen terbuka, tampilkan EditPostScreen
    if (showEditScreen) {
        EditPostScreen(
            pet = pet,
            petRepository = petRepository,
            onBack = { showEditScreen = false },
            onEditSuccess = {
                showEditScreen = false
                onEditSuccess()
                onBack() // Kembali ke home dan refresh
            }
        )
        return
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

    // 👇 Pop-up Konfirmasi Tandai Selesai
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text(markActionLabel) },
            text = {
                Text(
                    if (isMissing)
                        "Tandai hewan ini sudah ditemukan? Postingan tetap tampil dengan label \"Selesai\" dan bisa dibuka kembali."
                    else
                        "Tandai hewan ini sudah diadopsi? Postingan tetap tampil dengan label \"Selesai\" dan bisa dibuka kembali."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResolveDialog = false
                    setResolved(true)
                }) {
                    Text("Ya, Tandai", color = ResolvedColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        // 1. Foto Hewan
        AsyncImage(
            model = pet.imageUrl,
            contentDescription = "Foto ${pet.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay bawah gambar biar teks di card lebih kebaca
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.0f),
                            Color.Black.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // 👇 Header Tombol Back & Actions (Edit & Delete)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Back
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .shadow(4.dp, CircleShape, clip = false)
                    .background(Color.White, shape = CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            // 👇 Tombol Edit & Delete (HANYA MUNCUL JIKA ID USER COCOK) 👇
            if (isOwner) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Tombol Edit
                    IconButton(
                        onClick = { showEditScreen = true },
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, clip = false)
                            .background(BrandTeal, shape = CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White
                        )
                    }

                    // Tombol Delete
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, clip = false)
                            .background(MissingColor, shape = CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 2. Card Info (Melengkung)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 330.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(BackgroundColor)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 22.dp, bottom = 32.dp)
        ) {
            // Header: Nama + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pet.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pet.breed,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Status Badge dengan shadow & background soft
                StatusBadge(
                    label = pet.status,
                    accentColor = statusAccent,
                    accentLight = statusAccentLight
                )
            }

            // Resolved badge
            if (resolved) {
                Spacer(modifier = Modifier.height(10.dp))
                ResolvedBadge(label = "✓ $resolvedLabel")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Info Grid (3 kolom: Ras, Umur, Lokasi)
            QuickInfoGrid(
                breed = pet.breed,
                age = pet.age,
                distance = pet.address.ifBlank { pet.distance }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Uploader info
            UploaderCard(uploaderName = pet.uploaderName)

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Tentang
            SectionTitle(
                emoji = "📝",
                title = "Tentang ${pet.name}"
            )
            Spacer(modifier = Modifier.height(10.dp))
            DescriptionCard(
                description = pet.description.ifEmpty { "Tidak ada deskripsi tambahan dari pemilik." }
            )

            // Tags (hanya tampil kalau ada)
            if (pet.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(emoji = "🏷️", title = "Tag")
                Spacer(modifier = Modifier.height(10.dp))
                TagsRow(tags = pet.tags)
            }

            Spacer(modifier = Modifier.height(28.dp))

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
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Hubungi", tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Hubungi Pemilik", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Tombol Tandai Selesai / Buka Kembali (HANYA PEMILIK)
            if (isOwner) {
                Spacer(modifier = Modifier.height(12.dp))
                if (!resolved) {
                    Button(
                        onClick = { showResolveDialog = true },
                        enabled = !isUpdatingResolved,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ResolvedColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isUpdatingResolved) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(markActionLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { setResolved(false) },
                        enabled = !isUpdatingResolved,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTeal),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BrandTeal)
                    ) {
                        if (isUpdatingResolved) {
                            CircularProgressIndicator(color = BrandTeal, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Buka Kembali Postingan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Badge status dengan shadow halus dan background soft.
 */
@Composable
private fun StatusBadge(
    label: String,
    accentColor: Color,
    accentLight: Color
) {
    Row(
        modifier = Modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(accentLight)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ResolvedBadge(label: String) {
    Row(
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(ResolvedColorLight)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ResolvedColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = ResolvedColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Quick info grid 3 kolom untuk Ras, Umur, Lokasi — sebelumnya cuma plain text.
 */
@Composable
private fun QuickInfoGrid(
    breed: String,
    age: String,
    distance: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickInfoCard(
            label = "Ras",
            value = breed.ifEmpty { "—" },
            icon = Icons.Default.Pets,
            accentColor = BrandTeal,
            modifier = Modifier.weight(1f)
        )
        QuickInfoCard(
            label = "Umur",
            value = age.ifEmpty { "—" },
            icon = Icons.Default.CalendarToday,
            accentColor = Color(0xFF8B5CF6),
            modifier = Modifier.weight(1f)
        )
        QuickInfoCard(
            label = "Jarak",
            value = distance.ifEmpty { "—" },
            icon = Icons.Default.LocationOn,
            accentColor = Color(0xFFEC4899),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickInfoCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                clip = false
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * Card untuk menampilkan info uploader.
 */
@Composable
private fun UploaderCard(uploaderName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                clip = false
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle dengan initial
        val initial = uploaderName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandTealLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTeal
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Diposting oleh",
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = uploaderName,
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionTitle(emoji: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun DescriptionCard(description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(14.dp),
                clip = false
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = description,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 20.sp
        )
    }
}

/**
 * Tags ditampilkan sebagai LazyRow chip — sebelumnya tidak pernah ditampilkan.
 */
@Composable
private fun TagsRow(tags: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        items(tags) { tag ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandTealSoft)
                    .border(1.dp, BrandTealLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = null,
                    tint = BrandTeal,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tag,
                    fontSize = 12.sp,
                    color = BrandTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}