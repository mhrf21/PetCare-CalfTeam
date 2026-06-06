package com.calfteam.petcare.data.repository

import android.content.Context
import com.calfteam.petcare.utils.AppwriteConfig // Pastikan package AppwriteConfig lu bener
import io.appwrite.Client
import io.appwrite.services.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {

    // Ambil client dari config yang sudah kita bikin sebelumnya
    private val client: Client = AppwriteConfig.getClient(context)
    private val account = Account(client)

    // Fungsi untuk Registrasi (Sign Up)
    suspend fun signUp(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            // Appwrite butuh ID unik, kita generate pakai ID.unique()
            account.create(
                userId = io.appwrite.ID.unique(),
                email = email,
                password = password,
                name = name
            )
            Result.success("Akun berhasil dibuat!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fungsi untuk Login (Sign In)
    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            account.createEmailPasswordSession(
                email = email,
                password = password
            )
            Result.success("Login berhasil!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}