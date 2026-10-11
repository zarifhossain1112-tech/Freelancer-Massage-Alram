package com.example.data.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.db.GigAlarmDatabase
import com.example.data.model.CustomSound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object CustomSoundManager {

    suspend fun importAudioFile(
        context: Context,
        uri: Uri,
        nameOverride: String? = null
    ): CustomSound? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val originalName = nameOverride?.takeIf { it.isNotBlank() } ?: queryFileName(context, uri) ?: "কাস্টম সাউন্ড"
            val id = UUID.randomUUID().toString()

            val soundsDir = File(context.filesDir, "alarm_sounds").apply {
                if (!exists()) mkdirs()
            }

            val extension = originalName.substringAfterLast('.', "mp3")
            val targetFile = File(soundsDir, "sound_${id}.$extension")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            val sound = CustomSound(
                id = id,
                name = originalName.substringBeforeLast('.'),
                filePath = targetFile.absolutePath,
                fileSize = targetFile.length(),
                createdAt = System.currentTimeMillis()
            )

            GigAlarmDatabase.getDatabase(context).customSoundDao().insert(sound)
            sound
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteCustomSound(context: Context, sound: CustomSound) = withContext(Dispatchers.IO) {
        try {
            val file = File(sound.filePath)
            if (file.exists()) {
                file.delete()
            }
            GigAlarmDatabase.getDatabase(context).customSoundDao().delete(sound)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (colIndex != -1) {
                            result = cursor.getString(colIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
