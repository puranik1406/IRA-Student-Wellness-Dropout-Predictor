package com.example.ira.data.local.dao

import androidx.room.*
import com.example.ira.data.local.entities.Mood
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM moods WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getMoodsByStudent(studentId: Long): Flow<List<Mood>>

    @Query("SELECT * FROM moods WHERE studentId = :studentId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentMoods(studentId: Long, limit: Int = 7): Flow<List<Mood>>

    @Query("SELECT AVG(moodScore) FROM moods WHERE studentId = :studentId AND createdAt >= :startTime")
    suspend fun getAverageMoodSince(studentId: Long, startTime: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: Mood): Long

    @Update
    suspend fun updateMood(mood: Mood)

    @Delete
    suspend fun deleteMood(mood: Mood)

    @Query("DELETE FROM moods WHERE studentId = :studentId")
    suspend fun deleteMoodsByStudent(studentId: Long)
}
