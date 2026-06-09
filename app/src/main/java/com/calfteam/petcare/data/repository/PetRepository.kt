package com.calfteam.petcare.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.calfteam.petcare.data.model.Pet
import com.calfteam.petcare.utils.Constants
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import java.io.File

class PetRepository(client: Client) {

    private val storage = Storage(client)
    private val databases = Databases(client)

    // 1. UPLOAD GAMBAR
    suspend fun uploadPetImage(file: File): Result<String> {
        return try {
            val response = storage.createFile(
                bucketId = Constants.BUCKET_PET_IMAGES_ID,
                fileId = ID.unique(),
                file = InputFile.fromFile(file)
            )
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. SIMPAN POSTINGAN (Disesuaikan dengan Attribute Database lo)
    suspend fun savePetPost(
        name: String,
        breed: String,
        age: String,
        desc: String,
        type: String,
        fileId: String,
        tags: List<String> = emptyList(),
        contact: String = "",
        location: String = "Nearby"
    ): Result<Unit> {
        return try {
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_PETS_ID,
                documentId = ID.unique(),
                data = mapOf(
                    "petName" to name,
                    "type" to type,
                    "age" to age,
                    "description" to desc,
                    "status" to type,
                    "imageId" to fileId,
                    "contact" to contact,
                    "location" to location
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. AMBIL SEMUA DATA (Bagian paling krusial buat gambar)
    suspend fun getAllPets(): Result<List<Pet>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_PETS_ID
            )

            val pets = response.documents.map { doc ->
                // Ambil ID gambar dari database
                val imageId = doc.data["imageId"]?.toString() ?: ""

                // Generate URL lengkap pake helper di Constants
                val fullImageUrl = if (imageId.isNotEmpty()) {
                    Constants.getImageUrl(imageId)
                } else {
                    ""
                }

                // LOG UNTUK CEK DI LOGCAT: Klik link ini nanti di Logcat!
                Log.d("PET_DEBUG", "Hewan: ${doc.data["petName"]}, Link Gambar: $fullImageUrl")

                Pet(
                    id = doc.id,
                    name = doc.data["petName"]?.toString() ?: "No Name",
                    breed = doc.data["type"]?.toString() ?: "Unknown",
                    age = doc.data["age"]?.toString() ?: "",
                    status = doc.data["status"]?.toString() ?: "Available",
                    distance = doc.data["location"]?.toString() ?: "Nearby",
                    imageUrl = fullImageUrl, // URL ini yang dipake AsyncImage
                    tags = emptyList(),
                    lastSeen = doc.data["description"]?.toString()
                )
            }
            Result.success(pets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // HELPER: URI ke FILE
    fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val tempFile = File.createTempFile("pet_image", ".jpg", context.cacheDir)
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = tempFile.outputStream()
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}