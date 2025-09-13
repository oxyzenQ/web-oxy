package com.oxyzenq.kconvert.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_messages")
data class NotificationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType = NotificationType.INFO,
    val isRead: Boolean = false,
    val actionData: String? = null // For storing update version info, etc.
)

enum class NotificationType {
    UPDATE_AVAILABLE,
    UPDATE_LATEST,
    NO_INTERNET,
    INFO,
    ERROR
}
