package com.example.ira.data.local.dao

import androidx.room.*
import com.example.ira.data.local.entities.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE studentId = :studentId ORDER BY date DESC")
    fun getActivitiesByStudent(studentId: Long): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE studentId = :studentId ORDER BY date DESC LIMIT :limit")
    fun getRecentActivities(studentId: Long, limit: Int = 7): Flow<List<Activity>>
    
    @Query("SELECT AVG(steps) FROM activities WHERE studentId = :studentId AND date >= :startDate")
    suspend fun getAverageStepsSince(studentId: Long, startDate: Long): Double?
    
    @Query("SELECT AVG(sleepHours) FROM activities WHERE studentId = :studentId AND date >= :startDate")
    suspend fun getAverageSleepSince(studentId: Long, startDate: Long): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity): Long
    
    @Update
    suspend fun updateActivity(activity: Activity)
    
    @Delete
    suspend fun deleteActivity(activity: Activity)
    
    @Query("DELETE FROM activities WHERE studentId = :studentId")
    suspend fun deleteActivitiesByStudent(studentId: Long)
}
