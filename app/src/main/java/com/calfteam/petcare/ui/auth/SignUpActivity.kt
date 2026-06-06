package com.calfteam.petcare.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Memanggil UI layar pendaftaran yang barusan kita buat
            SignUpScreen()
        }
    }
}