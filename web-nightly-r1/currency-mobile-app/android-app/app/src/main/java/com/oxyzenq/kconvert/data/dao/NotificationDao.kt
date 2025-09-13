package com.oxyzenq.kconvert.data.dao

import androidx.room.*
import com.oxyzenq.kconvert.data.model.NotificationMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    
    @Query("SELECT * FROM notification_messages ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationMessage>>
    
    @Query("SELECT COUNT(*) FROM notification_messages WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationMessage): Long
    
    @Delete
    suspend fun deleteNotification(notification: NotificationMessage)
    
    @Query("DELETE FROM notification_messages WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)
    
    @Query("UPDATE notification_messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
    
    @Query("DELETE FROM notification_messages")
    suspend fun deleteAllNotifications()
    
    @Query("SELECT * FROM notification_messages WHERE id = :id")
    suspend fun getNotificationById(id: Long): NotificationMessage?
}