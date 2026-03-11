package com.xiehe.spine.data.cache

import com.xiehe.spine.core.store.KeyValueStore
import com.xiehe.spine.data.image.ImageFileSummary
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class ImageCacheRepository(
    private val store: KeyValueStore,
    private val json: Json,
    private val binaryStore: ImageBinaryStore,
) {
    suspend fun getImageBytes(fileId: Int): ByteArray? {
        return binaryStore.read(fileId)
    }

    suspend fun putImageBytes(
        fileId: Int,
        bytes: ByteArray,
        mimeType: String?,
        fileName: String?,
    ) {
        binaryStore.write(
            fileId = fileId,
            bytes = bytes,
            mimeType = mimeType,
            fileName = fileName,
        )
    }

    suspend fun removeImage(fileId: Int) {
        binaryStore.delete(fileId)
    }

    fun getImageListSnapshot(): List<ImageFileSummary>? {
        val raw = store.getString(KEY_IMAGE_LIST) ?: return null
        return runCatching {
            json.decodeFromString(ListSerializer(ImageFileSummary.serializer()), raw)
        }.getOrNull()
    }

    fun putImageListSnapshot(items: List<ImageFileSummary>) {
        val encoded = json.encodeToString(
            ListSerializer(ImageFileSummary.serializer()),
            items,
        )
        store.putString(KEY_IMAGE_LIST, encoded)
    }

    fun mergeImageItems(items: List<ImageFileSummary>) {
        if (items.isEmpty()) {
            return
        }
        val merged = linkedMapOf<Int, ImageFileSummary>()
        getImageListSnapshot().orEmpty().forEach { existing ->
            merged[existing.id] = existing
        }
        items.forEach { incoming ->
            merged[incoming.id] = incoming
        }
        putImageListSnapshot(merged.values.toList())
    }

    fun removeImageItem(fileId: Int) {
        val current = getImageListSnapshot().orEmpty()
        putImageListSnapshot(current.filterNot { it.id == fileId })
    }

    fun getPatientNameById(patientId: Int): String? {
        return getPatientNameMap()[patientId.toString()]
    }

    fun putPatientNameMap(map: Map<Int, String>) {
        if (map.isEmpty()) {
            return
        }
        val current = getPatientNameMap().toMutableMap()
        map.forEach { (id, name) ->
            if (name.isNotBlank()) {
                current[id.toString()] = name
            }
        }
        val encoded = json.encodeToString(
            MapSerializer(String.serializer(), String.serializer()),
            current,
        )
        store.putString(KEY_PATIENT_NAME_MAP, encoded)
    }

    private fun getPatientNameMap(): Map<String, String> {
        val raw = store.getString(KEY_PATIENT_NAME_MAP) ?: return emptyMap()
        return runCatching {
            json.decodeFromString(
                MapSerializer(String.serializer(), String.serializer()),
                raw,
            )
        }.getOrDefault(emptyMap())
    }

    private companion object {
        const val KEY_IMAGE_LIST = "image_cache.list.v1"
        const val KEY_PATIENT_NAME_MAP = "image_cache.patient_name_map.v1"
    }
}
