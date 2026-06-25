package com.calfteam.petcare.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.calfteam.petcare.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val signUpStatus by viewModel.signUpStatus.collectAsState()

    LaunchedEffect(signUpStatus) {
        if (signUpStatus == "Success") {
            Toast.makeText(context, "Daftar Berhasil! Silakan Login", Toast.LENGTH_SHORT).show()
            onNavigateToLogin()
        } else if (signUpStatus.startsWith("Error")) {
            Toast.makeText(context, signUpStatus, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Header dengan logo + gradient
        AuthHeader(
            title = "Daftar PetCare"
        )

        // Form section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nama Lengkap",
                icon = Icons.Default.Person,
                placeholder = "Cth: Alex Johnson"
            )

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = Icons.Default.Email,
                placeholder = "nama@email.com",
                keyboardType = KeyboardType.Email
            )

            AuthPasswordField(
                value = password,
                onValueChange = { password = it },
                imeAction = ImeAction.Done
            )

            PasswordStrengthIndicator(password = password)

            Spacer(modifier = Modifier.height(8.dp))

            AuthPrimaryButton(
                text = "Daftar Sekarang",
                icon = Icons.Default.PersonAdd,
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.length >= 8) {
                        viewModel.signUp(name, email, password)
                    } else {
                        Toast.makeText(
                            context,
                            "Isi semua data & password min 8 karakter",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                isLoading = signUpStatus == "Loading..."
            )
        }

        // Footer link
        AuthFooterLink(
            prefix = "Sudah punya akun?",
            actionText = "Masuk di sini",
            onClick = onNavigateToLogin
        )
    }
}