package com.example.ira.data.local.dao

import androidx.room.*
import com.example.ira.data.local.entities.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE userId = :userId AND userType = :userType ORDER BY createdAt DESC")
    fun getNotificationsByUser(userId: Long, userType: String): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND userType = :userType AND isRead = 0")
    fun getUnreadCount(userId: Long, userType: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Update
    suspend fun updateNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId AND userType = :userType")
    suspend fun markAllAsRead(userId: Long, userType: String)

    @Delete
    suspend fun deleteNotification(notification: Notification)

    @Query("DELETE FROM notifications WHERE userId = :userId AND userType = :userType")
    suspend fun deleteAllByUser(userId: Long, userType: String)
}
