package com.example.ira.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val rollNumber: String,
    val department: String,
    val semester: Int,
    val cgpa: Double = 7.0,
    val feePending: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
