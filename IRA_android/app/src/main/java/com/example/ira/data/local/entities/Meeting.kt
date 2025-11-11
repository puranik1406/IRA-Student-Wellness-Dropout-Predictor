package com.example.ira.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "meetings",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Counselor::class,
            parentColumns = ["id"],
            childColumns = ["counselorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Meeting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val counselorId: Long? = null,
    val status: String = "scheduled", // scheduled, completed, cancelled
    val scheduledAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
