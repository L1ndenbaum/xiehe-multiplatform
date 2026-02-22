package com.xiehe.spine.data.cache

interface ImageBinaryStore {
    suspend fun read(fileId: Int): ByteArray?
    suspend fun write(
        fileId: Int,
        bytes: ByteArray,
        mimeType: String? = null,
        fileName: String? = null,
    )

    suspend fun delete(fileId: Int)
}

class InMemoryImageBinaryStore : ImageBinaryStore {
    private val bytesById = mutableMapOf<Int, ByteArray>()

    override suspend fun read(fileId: Int): ByteArray? = bytesById[fileId]

    override suspend fun write(
        fileId: Int,
        bytes: ByteArray,
        mimeType: String?,
        fileName: String?,
    ) {
        bytesById[fileId] = bytes.copyOf()
    }

    override suspend fun delete(fileId: Int) {
        bytesById.remove(fileId)
    }
}
