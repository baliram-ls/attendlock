package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.AttendanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, timestamp DESC")
    suspend fun getAllRecordsSync(): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getRecordsByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getRecordsByDateSync(date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId")
    fun getRecordsBySubject(subjectId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId")
    suspend fun getRecordsBySubjectSync(subjectId: Long): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE timetableEntryId = :timetableEntryId AND date = :date LIMIT 1")
    suspend fun getRecordForClass(timetableEntryId: Long, date: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE timetableEntryId = :timetableEntryId AND date = :date LIMIT 1")
    fun getRecordForClassFlow(timetableEntryId: Long, date: String): Flow<AttendanceRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE timetableEntryId = :timetableEntryId AND date = :date")
    suspend fun deleteRecordForClass(timetableEntryId: Long, date: String)

    @Query("DELETE FROM attendance_records WHERE subjectId = :subjectId")
    suspend fun deleteRecordsBySubjectId(subjectId: Long)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM attendance_records")
    suspend fun clear()
}
