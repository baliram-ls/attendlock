package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String = "",
    val teacher: String = "",
    val targetAttendance: Int = 75,
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis()
)
