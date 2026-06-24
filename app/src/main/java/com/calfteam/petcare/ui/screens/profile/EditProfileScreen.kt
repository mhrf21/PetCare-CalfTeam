package com.calfteam.petcare.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private val Brand = Color(0xFF00666E)
private val BrandSoft = Color(0xFFE0F2F1)
private val DangerRed = Color(0xFFD32F2F)
private val WarnAmber = Color(0xFFFFA000)
private val WarnAmberSoft = Color(0xFFFFF3E0)
private val Surface = Color(0xFFFBF9F8)

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
                title = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
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
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold, color = Color.Black)
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
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand,
                            disabledContainerColor = Brand.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
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
            // ===== HEADER: Avatar + Subtitle =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Brand, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trimmedName.firstOrNull()?.uppercase() ?: "P",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Perbarui informasi akunmu",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== SECTION: Informasi Akun =====
            SectionCard(title = "Informasi Akun") {
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
            SectionCard(title = "Tips Keamanan") {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Brand,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Perubahan nama akan terlihat di semua postinganmu.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Brand,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Email baru harus diverifikasi setelah disimpan.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun LabeledField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            color = Color.Black,
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
                Icon(icon, contentDescription = null, tint = if (isError) DangerRed else Brand)
            },
            trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan",
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brand,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = DangerRed,
                focusedLeadingIconColor = Brand,
                unfocusedLeadingIconColor = Color.Gray,
                cursorColor = Brand
            )
        )
        if (isError) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorText,
                fontSize = 12.sp,
                color = DangerRed,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun InfoBox(text: String, color: Color, background: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF6D4C00),
            lineHeight = 19.sp
        )
    }
}
