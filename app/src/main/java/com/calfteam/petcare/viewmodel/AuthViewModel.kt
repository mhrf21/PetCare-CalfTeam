package com.calfteam.petcare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calfteam.petcare.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _signUpStatus = MutableStateFlow("")
    val signUpStatus: StateFlow<String> = _signUpStatus

    private val _loginStatus = MutableStateFlow<String?>(null)
    val loginStatus: StateFlow<String?> = _loginStatus

    // REGISTER: Tanpa parameter role
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _signUpStatus.value = "Loading"
            val result = repository.signUp(name, email, password)
            if (result.isSuccess) {
                _signUpStatus.value = "Success"
            } else {
                _signUpStatus.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // LOGIN: Mengirim status berupa "Success:NamaUser" ke LoginScreen
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = "Loading"
            val result = repository.signIn(email, password)
            if (result.isSuccess) {
                val userName = result.getOrNull() ?: "User"
                _loginStatus.value = "Success:$userName"
            } else {
                _loginStatus.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // CEK SESI: Otomatis login jika sesi Appwrite masih ada
    fun checkActiveSession() {
        viewModelScope.launch {
            val result = repository.checkSession()
            if (result.isSuccess) {
                val userName = result.getOrNull() ?: "User"
                _loginStatus.value = "Success:$userName"
            }
        }
    }
}