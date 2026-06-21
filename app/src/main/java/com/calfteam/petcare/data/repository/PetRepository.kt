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

    // 2. SIMPAN POSTINGAN (Menyimpan ID Gambar saja biar gak kepanjangan)
    suspend fun savePetPost(
        name: String, breed: String, age: String, desc: String,
        type: String, fileId: String, contact: String, location: String,
        uploaderName: String,
        userId: String
    ): Result<Unit> {
        return try {
            val data = mapOf(
                "petName" to name,
                "type" to breed,
                "age" to age,
                "description" to desc,
                "status" to type,
                "imageId" to fileId, // 👈 Ganti jadi "imageId" dan simpan fileId-nya saja (bukan URL penuh)
                "contact" to contact,
                "location" to location,
                "uploaderName" to uploaderName,
                "userId" to userId
            )
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_PETS_ID,
                documentId = io.appwrite.ID.unique(),
                data = data
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. AMBIL SEMUA DATA (Generate URL pas data diambil)
    suspend fun getAllPets(): Result<List<Pet>> {
        return try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_PETS_ID
            )

            val pets = response.documents.map { doc ->
                // 👈 Ambil id gambar dari kolom "imageId"
                val imageId = doc.data["imageId"]?.toString() ?: ""

                // 👈 Ubah id gambar jadi URL lengkap menggunakan helper Constants
                val fullImageUrl = if (imageId.isNotEmpty()) {
                    Constants.getImageUrl(imageId)
                } else {
                    ""
                }

                Pet(
                    id = doc.id,
                    name = doc.data["petName"]?.toString() ?: "No Name",
                    breed = doc.data["type"]?.toString() ?: "Unknown",
                    age = doc.data["age"]?.toString() ?: "",
                    status = doc.data["status"]?.toString() ?: "Available",
                    distance = doc.data["location"]?.toString() ?: "Nearby",
                    imageUrl = fullImageUrl, // 👈 Masukkan URL hasil generate ke model Pet
                    tags = emptyList(),
                    description = doc.data["description"]?.toString() ?: "",
                    contact = doc.data["contact"]?.toString() ?: "",
                    uploaderName = doc.data["uploaderName"]?.toString() ?: "Anonim",
                    userId = doc.data["userId"]?.toString() ?: ""
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

    // 👇 TAMBAHKAN FUNGSI DELETE INI DI PALING BAWAH 👇
    suspend fun deletePet(documentId: String): Boolean {
        return try {
            databases.deleteDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COLLECTION_PETS_ID,
                documentId = documentId
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} // Ini kurung tutup terakhir milik class PetRepository
