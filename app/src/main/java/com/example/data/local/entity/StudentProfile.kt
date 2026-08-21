package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: Int = 1,
    val studentName: String = "",
    val collegeName: String = "",
    val semester: String = "",
    val section: String = "",
    val defaultTarget: Int = 75,
    val isSetupCompleted: Boolean = false
)
