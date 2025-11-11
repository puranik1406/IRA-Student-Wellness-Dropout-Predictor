package com.example.ira.data.local.dao

import androidx.room.*
import com.example.ira.data.local.entities.Meeting
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getMeetingsByStudent(studentId: Long): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE counselorId = :counselorId ORDER BY createdAt DESC")
    fun getMeetingsByCounselor(counselorId: Long): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE status = :status ORDER BY scheduledAt DESC")
    fun getMeetingsByStatus(status: String): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE id = :id")
    suspend fun getMeetingById(id: Long): Meeting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting): Long

    @Update
    suspend fun updateMeeting(meeting: Meeting)

    @Delete
    suspend fun deleteMeeting(meeting: Meeting)

    @Query("UPDATE meetings SET status = :status WHERE id = :meetingId")
    suspend fun updateMeetingStatus(meetingId: Long, status: String)
}
