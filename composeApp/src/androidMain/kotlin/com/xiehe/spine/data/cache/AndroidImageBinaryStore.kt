package com.xiehe.spine.data.cache

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidImageBinaryStore(
    context: Context,
) : ImageBinaryStore {
    private val rootDir = File(context.filesDir, "image_cache").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    override suspend fun read(fileId: Int): ByteArray? = withContext(Dispatchers.IO) {
        val target = imageFile(fileId)
        if (!target.exists() || !target.isFile) {
            return@withContext null
        }
        runCatching { target.readBytes() }.getOrNull()
    }

    override suspend fun write(
        fileId: Int,
        bytes: ByteArray,
        mimeType: String?,
        fileName: String?,
    ) {
        withContext(Dispatchers.IO) {
            val target = imageFile(fileId)
            val tmp = File(target.absolutePath + ".tmp")
            runCatching {
                tmp.outputStream().use { output ->
                    output.write(bytes)
                output.flush()
            }
            if (target.exists()) {
                target.delete()
            }
            tmp.renameTo(target)
            }.onFailure {
                tmp.delete()
            }
        }
    }

    override suspend fun delete(fileId: Int) {
        withContext(Dispatchers.IO) {
            imageFile(fileId).delete()
        }
    }

    private fun imageFile(fileId: Int): File {
        return File(rootDir, "image_$fileId.bin")
    }
}
