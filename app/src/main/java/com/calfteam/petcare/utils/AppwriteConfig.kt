package com.calfteam.petcare.utils
import android.content.Context
import io.appwrite.Client

object AppwriteConfig {

    // Variabel client dibuat private agar hanya bisa diakses lewat fungsi getClient
    private var client: Client? = null

    /**
     * Fungsi untuk mendapatkan instance Appwrite Client.
     * Jika client belum ada, maka akan dibuat baru. Jika sudah ada, gunakan yang lama.
     */
    fun getClient(context: Context): Client {
        if (client == null) {
            client = Client(context)
                .setEndpoint(Constants.APPWRITE_ENDPOINT)
                .setProject(Constants.PROJECT_ID)
            // Jika Anda menggunakan self-signed certificate (biasanya untuk local dev),
            // uncomment baris di bawah ini:
            // .setSelfSigned(status = true)
        }
        return client!!
    }
}