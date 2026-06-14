package com.calfteam.petcare.data.model

data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val age: String,
    val status: String, // "Available" atau "Missing"
    val distance: String,
    val imageUrl: String, // Nanti untuk URL gambar dari Appwrite Storage
    val tags: List<String> = emptyList(),
    val lastSeen: String? = null, // 👇 TAMBAH KOMA DI SINI 👇
    val description: String = "",
    val contact: String = ""
)