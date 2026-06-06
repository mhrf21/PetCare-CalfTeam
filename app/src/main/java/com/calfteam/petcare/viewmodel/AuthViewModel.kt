package com.calfteam.petcare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calfteam.petcare.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)

    private val _loginStatus = MutableStateFlow<String?>(null)
    val loginStatus = _loginStatus.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.signIn(email, password)
            if (result.isSuccess) {
                _loginStatus.value = "Berhasil Login!"
            } else {
                _loginStatus.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}