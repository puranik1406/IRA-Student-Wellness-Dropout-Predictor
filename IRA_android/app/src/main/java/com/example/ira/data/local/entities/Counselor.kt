package com.example.ira.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "counselors")
data class Counselor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val employeeId: String,
    val licenseNumber: String,
    val specialization: String,
    val qualifications: String,
    val experienceYears: Int,
    val department: String,
    val createdAt: Long = System.currentTimeMillis()
)
