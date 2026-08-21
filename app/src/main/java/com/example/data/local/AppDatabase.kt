package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.StudentProfileDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.entity.AppSettings
import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.StudentProfile
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry

@Database(
    entities = [
        StudentProfile::class,
        Subject::class,
        TimetableEntry::class,
        AttendanceRecord::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun subjectDao(): SubjectDao
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendmate_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
