package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timetableEntryId: Long? = null,
    val subjectId: Long,
    val date: String, // "YYYY-MM-DD"
    val status: AttendanceStatus,
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
