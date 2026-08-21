package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.repository.AttendanceRepository

class AttendMateApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        AttendanceRepository(
            studentProfileDao = database.studentProfileDao(),
            subjectDao = database.subjectDao(),
            timetableDao = database.timetableDao(),
            attendanceDao = database.attendanceDao(),
            appSettingsDao = database.appSettingsDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_CLASS_REMINDERS,
                "Class Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you before college classes start"
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_CLASS_REMINDERS = "channel_class_reminders"
    }
}
