package com.example.utils

import android.content.Context
import android.content.Intent
import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.AttendanceStatus
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry
import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {

    fun generateJsonBackup(
        subjects: List<Subject>,
        timetable: List<TimetableEntry>,
        attendance: List<AttendanceRecord>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val subjectsArray = JSONArray()
        subjects.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("code", s.code)
                put("teacher", s.teacher)
                put("targetAttendance", s.targetAttendance)
                put("colorHex", s.colorHex)
                put("createdAt", s.createdAt)
            }
            subjectsArray.put(obj)
        }
        root.put("subjects", subjectsArray)

        val timetableArray = JSONArray()
        timetable.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("subjectId", t.subjectId)
                put("subjectName", t.subjectName)
                put("subjectCode", t.subjectCode)
                put("teacher", t.teacher)
                put("room", t.room)
                put("dayOfWeek", t.dayOfWeek)
                put("startTime", t.startTime)
                put("endTime", t.endTime)
            }
            timetableArray.put(obj)
        }
        root.put("timetable", timetableArray)

        val attendanceArray = JSONArray()
        attendance.forEach { a ->
            val obj = JSONObject().apply {
                put("id", a.id)
                if (a.timetableEntryId != null) put("timetableEntryId", a.timetableEntryId)
                put("subjectId", a.subjectId)
                put("date", a.date)
                put("status", a.status.name)
                put("remarks", a.remarks)
                put("timestamp", a.timestamp)
            }
            attendanceArray.put(obj)
        }
        root.put("attendance", attendanceArray)

        return root.toString(2)
    }

    fun parseJsonBackup(jsonString: String): Triple<List<Subject>, List<TimetableEntry>, List<AttendanceRecord>> {
        val root = JSONObject(jsonString)
        val subjects = mutableListOf<Subject>()
        val timetable = mutableListOf<TimetableEntry>()
        val attendance = mutableListOf<AttendanceRecord>()

        if (root.has("subjects")) {
            val subjectsArray = root.getJSONArray("subjects")
            for (i in 0 until subjectsArray.length()) {
                val obj = subjectsArray.getJSONObject(i)
                subjects.add(
                    Subject(
                        id = obj.optLong("id", 0),
                        name = obj.getString("name"),
                        code = obj.optString("code", ""),
                        teacher = obj.optString("teacher", ""),
                        targetAttendance = obj.optInt("targetAttendance", 75),
                        colorHex = obj.optString("colorHex", "#3B82F6"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        if (root.has("timetable")) {
            val timetableArray = root.getJSONArray("timetable")
            for (i in 0 until timetableArray.length()) {
                val obj = timetableArray.getJSONObject(i)
                timetable.add(
                    TimetableEntry(
                        id = obj.optLong("id", 0),
                        subjectId = obj.getLong("subjectId"),
                        subjectName = obj.getString("subjectName"),
                        subjectCode = obj.optString("subjectCode", ""),
                        teacher = obj.optString("teacher", ""),
                        room = obj.optString("room", ""),
                        dayOfWeek = obj.getInt("dayOfWeek"),
                        startTime = obj.getString("startTime"),
                        endTime = obj.getString("endTime")
                    )
                )
            }
        }

        if (root.has("attendance")) {
            val attendanceArray = root.getJSONArray("attendance")
            for (i in 0 until attendanceArray.length()) {
                val obj = attendanceArray.getJSONObject(i)
                val statusStr = obj.getString("status")
                val status = try {
                    AttendanceStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    AttendanceStatus.PRESENT
                }
                attendance.add(
                    AttendanceRecord(
                        id = obj.optLong("id", 0),
                        timetableEntryId = if (obj.has("timetableEntryId")) obj.getLong("timetableEntryId") else null,
                        subjectId = obj.getLong("subjectId"),
                        date = obj.getString("date"),
                        status = status,
                        remarks = obj.optString("remarks", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        return Triple(subjects, timetable, attendance)
    }

    fun generateCsvReport(
        subjects: List<Subject>,
        records: List<AttendanceRecord>
    ): String {
        val subjectMap = subjects.associateBy { it.id }
        val sb = StringBuilder()
        sb.append("Date,Subject Name,Subject Code,Teacher,Status,Remarks\n")

        records.sortedByDescending { it.date }.forEach { r ->
            val subj = subjectMap[r.subjectId]
            val name = (subj?.name ?: "Unknown").replace(",", " ")
            val code = (subj?.code ?: "").replace(",", " ")
            val teacher = (subj?.teacher ?: "").replace(",", " ")
            val status = r.status.name
            val remarks = r.remarks.replace(",", " ")
            sb.append("${r.date},$name,$code,$teacher,$status,$remarks\n")
        }

        return sb.toString()
    }

    fun shareText(context: Context, title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_TITLE, title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
