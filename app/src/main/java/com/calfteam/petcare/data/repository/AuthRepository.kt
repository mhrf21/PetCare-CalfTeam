package com.calfteam.petcare.data.repository

import android.content.Context
import com.calfteam.petcare.utils.AppwriteConfig
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Storage
import io.appwrite.models.InputFile
import java.io.File

class AuthRepository(context: Context) {

    private val client: Client = AppwriteConfig.getClient(context)
    private val account = Account(client)

    // 1. SIGN UP: Hapus parameter role, langsung simpan nama aslinya
    suspend fun signUp(name: String, email: String, password: String): Result<String> {
        return try {
            account.create(
                userId = io.appwrite.ID.unique(),
                email = email,
                password = password,
                name = name // Langsung pakai nama asli tanpa pembatas "|"
            )
            Result.success("Pendaftaran berhasil! Silakan login.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. SIGN IN: Return nama asli user ke UI setelah sukses login
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            account.createEmailPasswordSession(email = email, password = password)
            val user = account.get()
            Result.success(user.name) // Mengembalikan nama asli user (bukan role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. CHECK SESSION: Return nama asli user kalau session masih aktif
    suspend fun checkSession(): Result<String> {
        return try {
            val user = account.get()
            Result.success(user.name) // Mengembalikan nama asli user
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            account.deleteSession(sessionId = "current")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}

