package com.calfteam.petcare.utils
import android.content.Context
import io.appwrite.Client

object AppwriteConfig {

    private var client: Client? = null

    fun getClient(context: Context): Client {
        if (client == null) {
            client = Client(context)
                .setEndpoint(Constants.APPWRITE_ENDPOINT)
                .setProject(Constants.PROJECT_ID)
        }
        return client!!
    }
}