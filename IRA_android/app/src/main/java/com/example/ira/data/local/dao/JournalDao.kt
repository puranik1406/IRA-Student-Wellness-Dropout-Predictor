package com.example.ira.data.local.dao

import androidx.room.*
import com.example.ira.data.local.entities.Journal
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journals WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getJournalsByStudent(studentId: Long): Flow<List<Journal>>

    @Query("SELECT * FROM journals WHERE studentId = :studentId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentJournals(studentId: Long, limit: Int = 5): Flow<List<Journal>>

    @Query("SELECT * FROM journals WHERE id = :id")
    suspend fun getJournalById(id: Long): Journal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal): Long

    @Update
    suspend fun updateJournal(journal: Journal)

    @Delete
    suspend fun deleteJournal(journal: Journal)

    @Query("DELETE FROM journals WHERE studentId = :studentId")
    suspend fun deleteJournalsByStudent(studentId: Long)
}
