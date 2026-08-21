package com.example.utils

import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.AttendanceStatus
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry
import com.example.data.repository.AttendanceRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SampleDataGenerator {

    suspend fun loadSampleCollegeData(repository: AttendanceRepository) {
        val subjects = listOf(
            Subject(id = 1, name = "Data Structures & Algorithms", code = "CS301", teacher = "Dr. R. Sharma", targetAttendance = 75, colorHex = "#3B82F6"),
            Subject(id = 2, name = "Operating Systems", code = "CS302", teacher = "Prof. V. Rao", targetAttendance = 75, colorHex = "#10B981"),
            Subject(id = 3, name = "Database Management Systems", code = "CS303", teacher = "Dr. P. Patel", targetAttendance = 75, colorHex = "#F59E0B"),
            Subject(id = 4, name = "Computer Networks", code = "CS304", teacher = "Prof. S. Kumar", targetAttendance = 75, colorHex = "#8B5CF6"),
            Subject(id = 5, name = "Software Engineering", code = "CS305", teacher = "Prof. N. Iyer", targetAttendance = 80, colorHex = "#EC4899")
        )

        // Monday = 1 ... Sunday = 7
        val timetable = listOf(
            // Monday
            TimetableEntry(id = 1, subjectId = 1, subjectName = "Data Structures & Algorithms", subjectCode = "CS301", teacher = "Dr. R. Sharma", room = "Room 302", dayOfWeek = 1, startTime = "09:00", endTime = "10:00"),
            TimetableEntry(id = 2, subjectId = 2, subjectName = "Operating Systems", subjectCode = "CS302", teacher = "Prof. V. Rao", room = "Lab 1", dayOfWeek = 1, startTime = "10:15", endTime = "11:15"),
            TimetableEntry(id = 3, subjectId = 3, subjectName = "Database Management Systems", subjectCode = "CS303", teacher = "Dr. P. Patel", room = "Room 205", dayOfWeek = 1, startTime = "11:30", endTime = "12:30"),

            // Tuesday
            TimetableEntry(id = 4, subjectId = 4, subjectName = "Computer Networks", subjectCode = "CS304", teacher = "Prof. S. Kumar", room = "Room 304", dayOfWeek = 2, startTime = "09:00", endTime = "10:00"),
            TimetableEntry(id = 5, subjectId = 1, subjectName = "Data Structures & Algorithms", subjectCode = "CS301", teacher = "Dr. R. Sharma", room = "CS Lab 2", dayOfWeek = 2, startTime = "10:15", endTime = "12:15"),
            TimetableEntry(id = 6, subjectId = 5, subjectName = "Software Engineering", subjectCode = "CS305", teacher = "Prof. N. Iyer", room = "Room 108", dayOfWeek = 2, startTime = "01:30", endTime = "02:30"),

            // Wednesday
            TimetableEntry(id = 7, subjectId = 2, subjectName = "Operating Systems", subjectCode = "CS302", teacher = "Prof. V. Rao", room = "Room 302", dayOfWeek = 3, startTime = "09:00", endTime = "10:00"),
            TimetableEntry(id = 8, subjectId = 3, subjectName = "Database Management Systems", subjectCode = "CS303", teacher = "Dr. P. Patel", room = "DB Lab", dayOfWeek = 3, startTime = "10:15", endTime = "12:15"),
            TimetableEntry(id = 9, subjectId = 4, subjectName = "Computer Networks", subjectCode = "CS304", teacher = "Prof. S. Kumar", room = "Room 304", dayOfWeek = 3, startTime = "01:30", endTime = "02:30"),

            // Thursday
            TimetableEntry(id = 10, subjectId = 5, subjectName = "Software Engineering", subjectCode = "CS305", teacher = "Prof. N. Iyer", room = "Room 108", dayOfWeek = 4, startTime = "09:00", endTime = "10:00"),
            TimetableEntry(id = 11, subjectId = 1, subjectName = "Data Structures & Algorithms", subjectCode = "CS301", teacher = "Dr. R. Sharma", room = "Room 302", dayOfWeek = 4, startTime = "10:15", endTime = "11:15"),
            TimetableEntry(id = 12, subjectId = 2, subjectName = "Operating Systems", subjectCode = "CS302", teacher = "Prof. V. Rao", room = "Lab 1", dayOfWeek = 4, startTime = "11:30", endTime = "12:30"),

            // Friday
            TimetableEntry(id = 13, subjectId = 3, subjectName = "Database Management Systems", subjectCode = "CS303", teacher = "Dr. P. Patel", room = "Room 205", dayOfWeek = 5, startTime = "09:00", endTime = "10:00"),
            TimetableEntry(id = 14, subjectId = 4, subjectName = "Computer Networks", subjectCode = "CS304", teacher = "Prof. S. Kumar", room = "Net Lab", dayOfWeek = 5, startTime = "10:15", endTime = "12:15"),
            TimetableEntry(id = 15, subjectId = 5, subjectName = "Software Engineering", subjectCode = "CS305", teacher = "Prof. N. Iyer", room = "Room 108", dayOfWeek = 5, startTime = "01:30", endTime = "02:30")
        )

        // Generate past 3 weeks of attendance records
        val records = mutableListOf<AttendanceRecord>()
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (dayOffset in 21 downTo 0) {
            val date = today.minusDays(dayOffset.toLong())
            val dayOfWeek = date.dayOfWeek.value // 1 (Mon) to 7 (Sun)
            val dateStr = date.format(dateFormatter)

            val dayClasses = timetable.filter { it.dayOfWeek == dayOfWeek }
            dayClasses.forEachIndexed { index, entry ->
                // Deterministic realistic pattern: ~82% Present, 10% Absent, 5% Bunked, 3% Cancelled
                val hash = (dateStr.hashCode() + entry.id.toInt() * 31).let { if (it < 0) -it else it } % 100
                val status = when {
                    hash < 78 -> AttendanceStatus.PRESENT
                    hash < 88 -> AttendanceStatus.ABSENT
                    hash < 95 -> AttendanceStatus.BUNKED
                    else -> AttendanceStatus.CANCELLED
                }
                records.add(
                    AttendanceRecord(
                        timetableEntryId = entry.id,
                        subjectId = entry.subjectId,
                        date = dateStr,
                        status = status,
                        remarks = if (status == AttendanceStatus.CANCELLED) "Faculty on leave" else ""
                    )
                )
            }
        }

        repository.restoreData(subjects, timetable, records)
    }
}
