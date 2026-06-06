package com.calfteam.petcare.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.calfteam.petcare.ui.auth.LoginScreen // Import screen yang barusan dibikin

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Cukup panggil LoginScreen(), Compose akan otomatis menyediakan ViewModel-nya
            LoginScreen()
        }
    }
}