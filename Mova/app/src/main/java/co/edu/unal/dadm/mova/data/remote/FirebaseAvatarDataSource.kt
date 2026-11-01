package co.edu.unal.dadm.mova.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseAvatarDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun upload(userId: String, uri: Uri, contentType: String): String {
        val reference = storage.reference.child("users/$userId/avatar")
        reference.putFile(uri, com.google.firebase.storage.StorageMetadata.Builder()
            .setContentType(contentType)
            .build()).await()
        return reference.downloadUrl.await().toString()
    }
}
