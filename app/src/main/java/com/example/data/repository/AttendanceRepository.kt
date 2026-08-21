package com.example.data.repository

import com.example.data.local.dao.AppSettingsDao
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.StudentProfileDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.entity.AppSettings
import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.AttendanceStatus
import com.example.data.local.entity.StudentProfile
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AttendanceRepository(
    private val studentProfileDao: StudentProfileDao,
    private val subjectDao: SubjectDao,
    private val timetableDao: TimetableDao,
    private val attendanceDao: AttendanceDao,
    private val appSettingsDao: AppSettingsDao
) {
    val studentProfile: Flow<StudentProfile?> = studentProfileDao.getProfile()
    val subjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val timetableEntries: Flow<List<TimetableEntry>> = timetableDao.getAllEntries()
    val attendanceRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val appSettings: Flow<AppSettings?> = appSettingsDao.getSettings()

    fun getTimetableForDay(dayOfWeek: Int): Flow<List<TimetableEntry>> =
        timetableDao.getEntriesByDay(dayOfWeek)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsByDate(date)

    suspend fun getSettingsSync(): AppSettings {
        return withContext(Dispatchers.IO) {
            appSettingsDao.getSettingsSync() ?: AppSettings()
        }
    }

    suspend fun saveProfile(profile: StudentProfile) {
        withContext(Dispatchers.IO) {
            studentProfileDao.insertOrUpdateProfile(profile)
        }
    }

    suspend fun addSubject(subject: Subject): Long {
        return withContext(Dispatchers.IO) {
            subjectDao.insertSubject(subject)
        }
    }

    suspend fun updateSubject(subject: Subject) {
        withContext(Dispatchers.IO) {
            subjectDao.updateSubject(subject)
        }
    }

    suspend fun deleteSubject(subject: Subject) {
        withContext(Dispatchers.IO) {
            attendanceDao.deleteRecordsBySubjectId(subject.id)
            timetableDao.deleteEntriesBySubjectId(subject.id)
            subjectDao.deleteSubject(subject)
        }
    }

    suspend fun addTimetableEntry(entry: TimetableEntry): Long {
        return withContext(Dispatchers.IO) {
            timetableDao.insertEntry(entry)
        }
    }

    suspend fun addAllTimetableEntries(entries: List<TimetableEntry>) {
        withContext(Dispatchers.IO) {
            timetableDao.insertAll(entries)
        }
    }

    suspend fun updateTimetableEntry(entry: TimetableEntry) {
        withContext(Dispatchers.IO) {
            timetableDao.updateEntry(entry)
        }
    }

    suspend fun deleteTimetableEntry(entry: TimetableEntry) {
        withContext(Dispatchers.IO) {
            timetableDao.deleteEntry(entry)
        }
    }

    suspend fun markAttendance(
        timetableEntryId: Long?,
        subjectId: Long,
        date: String,
        status: AttendanceStatus,
        remarks: String = ""
    ) {
        withContext(Dispatchers.IO) {
            val existing = if (timetableEntryId != null) {
                attendanceDao.getRecordForClass(timetableEntryId, date)
            } else {
                null
            }

            if (existing != null) {
                attendanceDao.insertOrUpdateRecord(
                    existing.copy(
                        status = status,
                        remarks = remarks,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                attendanceDao.insertOrUpdateRecord(
                    AttendanceRecord(
                        timetableEntryId = timetableEntryId,
                        subjectId = subjectId,
                        date = date,
                        status = status,
                        remarks = remarks,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun removeAttendanceForClass(timetableEntryId: Long, date: String) {
        withContext(Dispatchers.IO) {
            attendanceDao.deleteRecordForClass(timetableEntryId, date)
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        withContext(Dispatchers.IO) {
            appSettingsDao.insertOrUpdateSettings(settings)
        }
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            studentProfileDao.clear()
            subjectDao.clear()
            timetableDao.clear()
            attendanceDao.clear()
            appSettingsDao.clear()
        }
    }

    suspend fun getAllDataSync(): Triple<List<Subject>, List<TimetableEntry>, List<AttendanceRecord>> {
        return withContext(Dispatchers.IO) {
            val subjects = subjectDao.getAllSubjectsSync()
            val timetable = timetableDao.getAllEntriesSync()
            val attendance = attendanceDao.getAllRecordsSync()
            Triple(subjects, timetable, attendance)
        }
    }

    suspend fun restoreData(
        subjects: List<Subject>,
        timetable: List<TimetableEntry>,
        attendance: List<AttendanceRecord>
    ) {
        withContext(Dispatchers.IO) {
            subjectDao.clear()
            timetableDao.clear()
            attendanceDao.clear()

            subjectDao.insertAll(subjects)
            timetableDao.insertAll(timetable)
            attendanceDao.insertAll(attendance)
        }
    }
}
