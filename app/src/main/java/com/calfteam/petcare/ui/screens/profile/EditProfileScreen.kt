package com.calfteam.petcare.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calfteam.petcare.data.repository.AuthRepository
import kotlinx.coroutines.launch

// Brand & semantic colors (konsisten dengan screen lain)
private val BrandTeal = Color(0xFF00666E)
private val BrandTealDeep = Color(0xFF008B95)
private val BrandTealLight = Color(0xFFE0F2F1)
private val BrandTealSoft = Color(0xFFF0F9FA)
private val DangerRed = Color(0xFFD32F2F)
private val WarnAmber = Color(0xFFFFA000)
private val WarnAmberSoft = Color(0xFFFFF3E0)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B7280)
private val Surface = Color(0xFFFBF9F8)
private val DividerColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authRepository: AuthRepository,
    currentName: String,
    currentEmail: String = "",
    onBack: () -> Unit,
    onProfileUpdated: (newName: String, newEmail: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var originalEmail by remember { mutableStateOf(currentEmail) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Prefill email dari Appwrite kalau pemanggil tidak menyediakannya
    LaunchedEffect(Unit) {
        if (originalEmail.isBlank()) {
            authRepository.getCurrentUser()?.let {
                email = it.email
                originalEmail = it.email
            }
        }
    }

    val trimmedName = name.trim()
    val trimmedEmail = email.trim()
    val nameHasError = trimmedName.isEmpty()
    val emailHasError = trimmedEmail.isEmpty() || !trimmedEmail.contains("@")
    val emailChanged = trimmedEmail != originalEmail.trim()
    val passwordMissingForEmail = emailChanged && password.isBlank()
    val canSave = !nameHasError && !emailHasError && !passwordMissingForEmail && !isSaving

    BackHandler { onBack() }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profil",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                color = Surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, DividerColor)
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                val failed = mutableListOf<String>()

                                if (trimmedName != currentName) {
                                    authRepository.updateName(trimmedName)
                                        .onFailure { failed += "nama: ${it.message ?: "gagal"}" }
                                }
                                if (emailChanged) {
                                    authRepository.updateEmail(trimmedEmail, password)
                                        .onFailure { failed += "email: ${it.message ?: "gagal"}" }
                                }

                                isSaving = false

                                if (failed.isEmpty()) {
                                    Toast.makeText(context, "Profil diperbarui ✓", Toast.LENGTH_SHORT).show()
                                    onProfileUpdated(trimmedName, trimmedEmail)
                                } else {
                                    Toast.makeText(context, failed.joinToString("\n"), Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandTeal,
                            disabledContainerColor = BrandTeal.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ===== HEADER: Avatar + Subtitle (dengan gradient soft) =====
            ProfileHeader(name = trimmedName)

            Spacer(modifier = Modifier.height(8.dp))

            // ===== SECTION: Informasi Akun =====
            SectionCard(emoji = "👤", title = "Informasi Akun") {
                LabeledField(
                    icon = Icons.Default.Person,
                    label = "Nama Lengkap",
                    value = name,
                    onValueChange = { name = it },
                    isError = nameHasError,
                    errorText = "Nama tidak boleh kosong",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(20.dp))

                LabeledField(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    isError = emailHasError,
                    errorText = "Format email tidak valid",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )

                // Info box animasi untuk password requirement
                AnimatedVisibility(
                    visible = emailChanged,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoBox(
                            text = "Untuk mengubah email, masukkan password saat ini sebagai konfirmasi keamanan.",
                            color = WarnAmber,
                            background = WarnAmberSoft
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LabeledField(
                            icon = Icons.Default.Lock,
                            label = "Password Saat Ini",
                            value = password,
                            onValueChange = { password = it },
                            isError = passwordMissingForEmail && password.isNotEmpty() == false && emailChanged,
                            errorText = "Password wajib diisi untuk mengubah email",
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== SECTION: Tips =====
            SectionCard(emoji = "🔒", title = "Tips Keamanan") {
                TipRow(
                    icon = Icons.Default.CheckCircle,
                    text = "Perubahan nama akan terlihat di semua postinganmu."
                )
                Spacer(modifier = Modifier.height(10.dp))
                TipRow(
                    icon = Icons.Default.CheckCircle,
                    text = "Email baru harus diverifikasi setelah disimpan."
                )
                Spacer(modifier = Modifier.height(10.dp))
                TipRow(
                    icon = Icons.Default.Security,
                    text = "Password tidak pernah ditampilkan di layar demi keamananmu."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Header profil dengan gradient soft, avatar elevated, dan subtitle.
 */
@Composable
private fun ProfileHeader(name: String) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "P"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandTealLight,
                        BrandTealSoft,
                        Surface
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar dengan gradient + shadow + edit badge
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandTealDeep, BrandTeal)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            // Edit foto badge (visual only)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit foto",
                    tint = BrandTeal,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Perbarui informasi akunmu",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionCard(
    emoji: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, DividerColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun LabeledField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorText: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = isError,
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isError) DangerRed else BrandTeal,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandTeal,
                unfocusedBorderColor = DividerColor,
                errorBorderColor = DangerRed,
                focusedLeadingIconColor = BrandTeal,
                unfocusedLeadingIconColor = TextSecondary,
                focusedLabelColor = BrandTeal,
                cursorColor = BrandTeal
            )
        )
        if (isError) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(DangerRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorText,
                    fontSize = 12.sp,
                    color = DangerRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TipRow(
    icon: ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(BrandTealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextPrimary,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun InfoBox(text: String, color: Color, background: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = color.copy(alpha = 0.9f),
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}