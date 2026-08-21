package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AttendMateApp
import com.example.data.gemini.GeminiTimetableScanner
import com.example.data.gemini.ScannedClassItem
import com.example.data.local.entity.AppSettings
import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.AttendanceStatus
import com.example.data.local.entity.StudentProfile
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry
import com.example.utils.AttendanceCalculator
import com.example.utils.OverallAttendanceStats
import com.example.utils.SampleDataGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TodayClassItem(
    val timetableEntry: TimetableEntry,
    val subject: Subject?,
    val attendanceRecord: AttendanceRecord?
)

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Success(val items: List<ScannedClassItem>) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

enum class NavigationTab(val title: String) {
    HOME("Home"),
    TIMETABLE("Timetable"),
    SUBJECTS("Subjects"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AttendMateApp).repository

    val studentProfile: StateFlow<StudentProfile?> = repository.studentProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subjects: StateFlow<List<Subject>> = repository.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableEntries: StateFlow<List<TimetableEntry>> = repository.timetableEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.attendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings?> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedTab = MutableStateFlow(NavigationTab.HOME)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedTimetableDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)
    val selectedTimetableDay: StateFlow<Int> = _selectedTimetableDay.asStateFlow()

    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val overallStats: StateFlow<OverallAttendanceStats> = combine(
        subjects,
        attendanceRecords,
        appSettings
    ) { subs, records, settings ->
        val target = settings?.defaultTarget ?: 75
        AttendanceCalculator.calculateOverallStats(subs, records, target)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OverallAttendanceStats(
            0, 0, 0, 0, 0, 0.0, 75, 0, 0, "No Data", 0, 0, null, null, emptyList()
        )
    )

    val todayClasses: StateFlow<List<TodayClassItem>> = combine(
        _selectedDate,
        timetableEntries,
        subjects,
        attendanceRecords
    ) { date, entries, subs, records ->
        val dayOfWeek = date.dayOfWeek.value
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dayEntries = entries.filter { it.dayOfWeek == dayOfWeek }
            .sortedBy { it.startTime }

        val subjectMap = subs.associateBy { it.id }
        val recordMap = records.filter { it.date == dateStr }
            .associateBy { it.timetableEntryId }

        dayEntries.map { entry ->
            val subj = subjectMap[entry.subjectId]
            val rec = recordMap[entry.id]
            TodayClassItem(
                timetableEntry = entry,
                subject = subj,
                attendanceRecord = rec
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setSelectedTimetableDay(dayOfWeek: Int) {
        _selectedTimetableDay.value = dayOfWeek
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun saveProfile(profile: StudentProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile.copy(isSetupCompleted = true))
            showSnackbar("Profile saved!")
        }
    }

    fun markAttendance(
        entry: TimetableEntry,
        status: AttendanceStatus,
        remarks: String = ""
    ) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            repository.markAttendance(
                timetableEntryId = entry.id,
                subjectId = entry.subjectId,
                date = dateStr,
                status = status,
                remarks = remarks
            )
        }
    }

    fun quickMarkSubject(
        subjectId: Long,
        status: AttendanceStatus
    ) {
        viewModelScope.launch {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            repository.markAttendance(
                timetableEntryId = null,
                subjectId = subjectId,
                date = dateStr,
                status = status
            )
            showSnackbar("Marked ${status.name.lowercase().replaceFirstChar { it.uppercase() }}")
        }
    }

    fun removeAttendance(entry: TimetableEntry) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            repository.removeAttendanceForClass(entry.id, dateStr)
        }
    }

    fun addSubject(
        name: String,
        code: String,
        teacher: String,
        target: Int,
        colorHex: String
    ) {
        viewModelScope.launch {
            repository.addSubject(
                Subject(
                    name = name.trim(),
                    code = code.trim(),
                    teacher = teacher.trim(),
                    targetAttendance = target,
                    colorHex = colorHex
                )
            )
            showSnackbar("Subject added successfully")
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            showSnackbar("Subject updated")
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            showSnackbar("Subject '${subject.name}' deleted")
        }
    }

    fun addTimetableEntry(
        subjectId: Long,
        subjectName: String,
        subjectCode: String,
        teacher: String,
        room: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            repository.addTimetableEntry(
                TimetableEntry(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    subjectCode = subjectCode,
                    teacher = teacher,
                    room = room,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime
                )
            )
            showSnackbar("Class scheduled on ${getDayName(dayOfWeek)}")
        }
    }

    fun updateTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.updateTimetableEntry(entry)
            showSnackbar("Timetable entry updated")
        }
    }

    fun deleteTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.deleteTimetableEntry(entry)
            showSnackbar("Class removed from timetable")
        }
    }

    fun scanTimetableImageUri(uri: Uri) {
        viewModelScope.launch {
            _scanUiState.value = ScanUiState.Scanning
            try {
                val contentResolver = getApplication<Application>().contentResolver
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val userKey = repository.getSettingsSync().geminiApiKey
                val result = GeminiTimetableScanner.scanTimetableImage(bitmap, userKey)
                if (result.isSuccess) {
                    val items = result.getOrNull() ?: emptyList()
                    if (items.isEmpty()) {
                        _scanUiState.value = ScanUiState.Error("No classes could be detected in this image. Please try a clearer image.")
                    } else {
                        _scanUiState.value = ScanUiState.Success(items)
                    }
                } else {
                    _scanUiState.value = ScanUiState.Error(result.exceptionOrNull()?.message ?: "Failed to scan image.")
                }
            } catch (e: Exception) {
                _scanUiState.value = ScanUiState.Error(e.message ?: "Could not process image")
            }
        }
    }

    fun resetScanState() {
        _scanUiState.value = ScanUiState.Idle
    }

    fun applyScannedTimetable(scannedItems: List<ScannedClassItem>) {
        viewModelScope.launch {
            val existingSubjects = repository.subjects.stateIn(viewModelScope).value.toMutableList()
            val colors = listOf("#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4", "#6366F1")
            var colorIdx = 0

            val newEntries = mutableListOf<TimetableEntry>()

            for (item in scannedItems) {
                // Find existing or create subject
                var match = existingSubjects.find {
                    it.name.equals(item.subjectName.trim(), ignoreCase = true) ||
                            (item.subjectCode.isNotBlank() && it.code.equals(item.subjectCode.trim(), ignoreCase = true))
                }

                val subjectId = if (match != null) {
                    match.id
                } else {
                    val color = colors[colorIdx % colors.size]
                    colorIdx++
                    val newSubj = Subject(
                        name = item.subjectName.trim(),
                        code = item.subjectCode.trim(),
                        teacher = item.teacher.trim(),
                        targetAttendance = 75,
                        colorHex = color
                    )
                    val id = repository.addSubject(newSubj)
                    val created = newSubj.copy(id = id)
                    existingSubjects.add(created)
                    id
                }

                newEntries.add(
                    TimetableEntry(
                        subjectId = subjectId,
                        subjectName = item.subjectName.trim(),
                        subjectCode = item.subjectCode.trim(),
                        teacher = item.teacher.trim(),
                        room = item.room.trim(),
                        dayOfWeek = item.dayOfWeek,
                        startTime = item.startTime,
                        endTime = item.endTime
                    )
                )
            }

            repository.addAllTimetableEntries(newEntries)
            _scanUiState.value = ScanUiState.Idle
            showSnackbar("Successfully imported ${newEntries.size} classes to your timetable!")
        }
    }

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            showSnackbar("Settings updated")
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            SampleDataGenerator.loadSampleCollegeData(repository)
            showSnackbar("Loaded sample college timetable & attendance!")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            showSnackbar("All data cleared")
        }
    }

    private fun getDayName(day: Int): String {
        return when (day) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day"
        }
    }
}
