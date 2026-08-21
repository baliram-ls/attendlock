package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val defaultTarget: Int = 75,
    val reminderMinutesBefore: Int = 10,
    val reminderNotificationsEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val themeMode: String = "dark", // "system", "light", "dark"
    val dynamicColors: Boolean = false,
    val geminiApiKey: String = ""
)
