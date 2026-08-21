package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TimetableEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_entries ORDER BY startTime ASC")
    fun getAllEntries(): Flow<List<TimetableEntry>>

    @Query("SELECT * FROM timetable_entries ORDER BY startTime ASC")
    suspend fun getAllEntriesSync(): List<TimetableEntry>

    @Query("SELECT * FROM timetable_entries WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getEntriesByDay(dayOfWeek: Int): Flow<List<TimetableEntry>>

    @Query("SELECT * FROM timetable_entries WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    suspend fun getEntriesByDaySync(dayOfWeek: Int): List<TimetableEntry>

    @Query("SELECT * FROM timetable_entries WHERE subjectId = :subjectId")
    suspend fun getEntriesBySubject(subjectId: Long): List<TimetableEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TimetableEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TimetableEntry>)

    @Update
    suspend fun updateEntry(entry: TimetableEntry)

    @Delete
    suspend fun deleteEntry(entry: TimetableEntry)

    @Query("DELETE FROM timetable_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM timetable_entries WHERE subjectId = :subjectId")
    suspend fun deleteEntriesBySubjectId(subjectId: Long)

    @Query("DELETE FROM timetable_entries")
    suspend fun clear()
}
