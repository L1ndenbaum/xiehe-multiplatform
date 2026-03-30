package com.xiehe.spine.data.image

private val embeddedCategoryRegex = Regex("""影像类别[:：]\s*(正面|侧面|左侧曲位|右侧曲位|体态照片)""")

enum class ImageCategory(val label: String) {
    FRONT("正面"),
    SIDE("侧面"),
    LEFT_BENDING("左侧曲位"),
    RIGHT_BENDING("右侧曲位"),
    POSTURE_PHOTO("体态照片"),
    ;

    companion object {
        val optionLabels: List<String> = entries.map(ImageCategory::label)

        fun fromLabel(label: String?): ImageCategory? {
            return entries.firstOrNull { it.label == label?.trim() }
        }

        fun fromRaw(raw: String?): ImageCategory? {
            val value = raw?.trim().orEmpty()
            if (value.isBlank()) {
                return null
            }

            embeddedCategoryRegex.find(value)
                ?.groupValues
                ?.getOrNull(1)
                ?.let(::fromLabel)
                ?.let { return it }

            return when {
                value.contains("体态", ignoreCase = true) || value.contains("posture", ignoreCase = true) -> POSTURE_PHOTO
                value.contains("左侧曲位", ignoreCase = true) || (value.contains("左", ignoreCase = true) && value.contains("曲", ignoreCase = true)) -> LEFT_BENDING
                value.contains("右侧曲位", ignoreCase = true) || (value.contains("右", ignoreCase = true) && value.contains("曲", ignoreCase = true)) -> RIGHT_BENDING
                value.contains("侧面", ignoreCase = true) || value.contains("侧位", ignoreCase = true) || value.contains("side", ignoreCase = true) -> SIDE
                value.contains("正面", ignoreCase = true) || value.contains("正位", ignoreCase = true) || value.contains("front", ignoreCase = true) || value.contains("x光", ignoreCase = true) || value.contains("xray", ignoreCase = true) -> FRONT
                else -> null
            }
        }
    }
}

fun resolveImageCategory(item: ImageFileSummary): ImageCategory {
    return ImageCategory.fromRaw(item.description)
        ?: ImageCategory.fromRaw(item.bodyPart)
        ?: ImageCategory.fromRaw(item.modality)
        ?: ImageCategory.FRONT
}

fun encodeImageUploadDescription(category: ImageCategory, note: String?): String {
    val safeNote = note?.trim().orEmpty()
    return if (safeNote.isBlank()) {
        category.label
    } else {
        "影像类别:${category.label}；备注:${safeNote}"
    }
}
