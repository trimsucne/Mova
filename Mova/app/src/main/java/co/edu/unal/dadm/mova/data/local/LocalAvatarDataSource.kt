package co.edu.unal.dadm.mova.data.local

import android.content.Context
import android.net.Uri
import java.io.File

class LocalAvatarDataSource(context: Context) {
    private val appContext = context.applicationContext
    private val avatarFile = File(appContext.filesDir, "profile_avatar")

    fun save(uri: Uri, contentType: String): String {
        require(contentType == "image/jpeg" || contentType == "image/png") {
            "Avatar must be JPG or PNG"
        }
        val input = requireNotNull(appContext.contentResolver.openInputStream(uri)) {
            "Avatar file could not be opened"
        }
        val tempFile = File.createTempFile("avatar_", ".tmp", appContext.cacheDir)
        try {
            var bytes = 0L
            input.use { source ->
                tempFile.outputStream().use { target ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var read = source.read(buffer)
                    while (read != -1) {
                        bytes += read
                        require(bytes <= MAX_BYTES) { "Avatar must be 5 MB or smaller" }
                        target.write(buffer, 0, read)
                        read = source.read(buffer)
                    }
                }
            }
            require(tempFile.renameTo(avatarFile)) { "Avatar could not be saved" }
        } finally {
            tempFile.delete()
        }
        return avatarFile.absolutePath
    }

    private companion object {
        const val MAX_BYTES = 5 * 1024 * 1024
    }
}
