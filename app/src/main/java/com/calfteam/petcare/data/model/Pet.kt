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
    val userId: String = "",
    val resolved: Boolean = false,
    /**
     * Alamat terbaca (contoh: "Kebayoran Baru, Jakarta Selatan").
     * Berasal dari reverse geocoding saat user mengaktifkan GPS,
     * atau input manual untuk post Missing.
     * Kosong artinya user belum pernah menyimpan alamat / post lama sebelum fitur ini ada.
     */
    val address: String = ""
)