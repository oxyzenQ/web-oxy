/*
 * Notification DAO for Room Database
 * Creativity Authored by oxyzenq 2025
 */

package com.oxyzenq.kconvert.data.local.dao

import androidx.room.*
import com.oxyzenq.kconvert.data.local.entity.NotifyMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface NotifyDao {
    @Query("SELECT * FROM notify_messages ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<NotifyMessage>>

    @Query("SELECT COUNT(*) FROM notify_messages")
    fun getUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(msg: NotifyMessage)

    @Update
    suspend fun update(msg: NotifyMessage)

    @Query("DELETE FROM notify_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notify_messages")
    suspend fun deleteAll()

    @Query("UPDATE notify_messages SET read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notify_messages SET read = 1")
    suspend fun markAllAsRead()

    @Query("SELECT * FROM notify_messages WHERE id = :id")
    suspend fun getById(id: Long): NotifyMessage?
}
