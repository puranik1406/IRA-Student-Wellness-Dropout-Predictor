package com.example.ira.data.local.dao

import androidx.room.*
import com.example.ira.data.local.entities.Counselor
import kotlinx.coroutines.flow.Flow

@Dao
interface CounselorDao {
    @Query("SELECT * FROM counselors WHERE id = :id")
    fun getCounselorById(id: Long): Flow<Counselor?>

    @Query("SELECT * FROM counselors WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Counselor?

    @Query("SELECT * FROM counselors ORDER BY name ASC")
    fun getAllCounselors(): Flow<List<Counselor>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCounselor(counselor: Counselor): Long

    @Update
    suspend fun updateCounselor(counselor: Counselor)

    @Delete
    suspend fun deleteCounselor(counselor: Counselor)
}
