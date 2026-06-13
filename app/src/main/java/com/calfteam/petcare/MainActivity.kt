package com.calfteam.petcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.calfteam.petcare.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tangkap nama user yang dikirim dari LoginScreen
        val userName = intent.getStringExtra("userName") ?: "Pecinta Hewan"

        setContent {
            // Langsung panggil cangkang utamanya
            MainScreen(userName = userName)
        }
    }
}