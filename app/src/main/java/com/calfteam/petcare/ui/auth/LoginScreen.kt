package com.calfteam.petcare.ui.auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calfteam.petcare.MainActivity
import com.calfteam.petcare.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val loginStatus by viewModel.loginStatus.collectAsState()

    // Pengecekan otomatis saat aplikasi dibuka
    LaunchedEffect(Unit) {
        viewModel.checkActiveSession()
    }

    LaunchedEffect(loginStatus) {
        loginStatus?.let { status ->
            if (status.startsWith("Success:")) {
                // Di AuthRepository baru, ini bakal nangkep NAMA USER (contoh: "Alex Johnson")
                val userName = status.substringAfter("Success:")
                Toast.makeText(context, "Selamat datang kembali, $userName!", Toast.LENGTH_SHORT).show()

                // Langsung arahkan ke MainActivity dan bawa data nama user
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("userName", userName) // Ganti key dari "role" jadi "userName"
                }
                context.startActivity(intent)
                (context as? Activity)?.finish()
            } else {
                Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            }
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
            title = "Selamat Datang Kembali"
        )

        // Form section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = Icons.Default.Email,
                placeholder = "nama@email.com",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )

            AuthPasswordField(
                value = password,
                onValueChange = { password = it }
            )

            ForgotPasswordLink(onClick = {
                Toast.makeText(context, "Hubungi developer untuk reset password", Toast.LENGTH_SHORT).show()
            })

            Spacer(modifier = Modifier.height(8.dp))

            AuthPrimaryButton(
                text = "Masuk",
                icon = Icons.AutoMirrored.Filled.Login,
                onClick = { viewModel.signIn(email, password) },
                isLoading = loginStatus == "Loading..."
            )
        }

        // Footer link
        AuthFooterLink(
            prefix = "Belum punya akun?",
            actionText = "Daftar di sini",
            onClick = {
                val intent = Intent(context, SignUpActivity::class.java)
                context.startActivity(intent)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}