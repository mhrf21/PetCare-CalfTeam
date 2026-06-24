package com.calfteam.petcare.data.model

data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val age: String,
    val status: String,
    val distance: String,
    val imageUrl: String,
    val tags: List<String> = emptyList(),
    val lastSeen: String? = null,
    val description: String = "",
    val contact: String = "",
    val uploaderName: String = "Anonim",
    val userId: String = ""
)