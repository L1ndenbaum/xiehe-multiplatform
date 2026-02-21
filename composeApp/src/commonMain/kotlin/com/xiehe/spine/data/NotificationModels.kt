package com.xiehe.spine.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationMessagePageData(
    val items: List<NotificationMessage>,
    val pagination: Pagination,
)

@Serializable
data class NotificationMessage(
    val id: Int,
    val title: String,
    val content: String,
    @SerialName("message_type") val messageType: String? = null,
    val priority: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("action_url") val actionUrl: String? = null,
    @SerialName("action_text") val actionText: String? = null,
    @SerialName("sender_name") val senderName: String? = null,
)
