package com.calfteam.petcare.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.calfteam.petcare.data.repository.AuthRepository
import com.calfteam.petcare.viewmodel.AuthViewModel

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Repository dan ViewModel
        val repository = AuthRepository(applicationContext)
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repository) as T
            }
        })[AuthViewModel::class.java]

        setContent {
            SignUpScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    finish()
                }
            )
        }
    }
}