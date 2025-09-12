/*
 * Notification Message Entity for Room Database
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notify_messages")
data class NotifyMessage(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val title: String? = null,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val releaseUrl: String? = null,
    val messageType: String = "UPDATE" // UPDATE, INFO, ERROR
)
