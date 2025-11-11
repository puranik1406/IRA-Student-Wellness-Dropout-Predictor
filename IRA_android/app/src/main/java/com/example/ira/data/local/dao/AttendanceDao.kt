package com.example.ira.data.local.dao

import androidx.room.*
import com.example.ira.data.local.entities.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY year DESC, month DESC")
    fun getAttendanceByStudent(studentId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY year DESC, month DESC LIMIT :limit")
    fun getRecentAttendance(studentId: Long, limit: Int = 4): Flow<List<Attendance>>

    @Query("SELECT AVG(attendancePercentage) FROM attendance WHERE studentId = :studentId")
    suspend fun getAverageAttendance(studentId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteAttendanceByStudent(studentId: Long)
}
