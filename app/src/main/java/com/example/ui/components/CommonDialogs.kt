package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry
import com.example.ui.theme.AttendBluePrimary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

val SUBJECT_PALETTE = listOf(
    "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4", "#6366F1", "#14B8A6", "#F97316", "#84CC16"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubjectDialog(
    initialSubject: Subject? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, teacher: String, target: Int, colorHex: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialSubject?.name ?: "") }
    var code by remember { mutableStateOf(initialSubject?.code ?: "") }
    var teacher by remember { mutableStateOf(initialSubject?.teacher ?: "") }
    var targetAttendance by remember { mutableFloatStateOf(initialSubject?.targetAttendance?.toFloat() ?: 75f) }
    var selectedColor by remember { mutableStateOf(initialSubject?.colorHex ?: SUBJECT_PALETTE[0]) }

    var nameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_subject_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialSubject == null) "Add New Subject" else "Edit Subject",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Data Structures") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Subject name is required", color = StatusAbsent) } } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AttendBluePrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Subject Code") },
                        placeholder = { Text("CS301") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { teacher = it },
                        label = { Text("Teacher / Faculty") },
                        placeholder = { Text("Dr. Sharma") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_teacher_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target attendance slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target Attendance",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${targetAttendance.toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = AttendBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = targetAttendance,
                    onValueChange = { targetAttendance = it },
                    valueRange = 50f..95f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = AttendBluePrimary,
                        activeTrackColor = AttendBluePrimary,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("target_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Subject Color Tag",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SUBJECT_PALETTE.take(5).forEach { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SUBJECT_PALETTE.drop(5).forEach { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (initialSubject != null && onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusAbsent.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).testTag("delete_subject_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                            } else {
                                onConfirm(
                                    name.trim(),
                                    code.trim(),
                                    teacher.trim(),
                                    targetAttendance.toInt(),
                                    selectedColor
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(if (initialSubject != null) 1.5f else 1f).testTag("save_subject_btn")
                    ) {
                        Text(if (initialSubject == null) "Add Subject" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTimetableEntryDialog(
    initialEntry: TimetableEntry? = null,
    subjects: List<Subject>,
    defaultDayOfWeek: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (
        subjectId: Long,
        subjectName: String,
        subjectCode: String,
        teacher: String,
        room: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var selectedSubject by remember {
        mutableStateOf(
            if (initialEntry != null) subjects.find { it.id == initialEntry.subjectId } ?: subjects.firstOrNull()
            else subjects.firstOrNull()
        )
    }
    var customSubjectName by remember { mutableStateOf(initialEntry?.subjectName ?: "") }
    var room by remember { mutableStateOf(initialEntry?.room ?: "") }
    var teacher by remember { mutableStateOf(initialEntry?.teacher ?: "") }
    var selectedDay by remember { mutableIntStateOf(initialEntry?.dayOfWeek ?: defaultDayOfWeek) }

    var startTime by remember { mutableStateOf(initialEntry?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialEntry?.endTime ?: "10:00") }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val daysList = listOf(
        1 to "Monday", 2 to "Tuesday", 3 to "Wednesday", 4 to "Thursday",
        5 to "Friday", 6 to "Saturday", 7 to "Sunday"
    )

    var expandedSubjectDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_timetable_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialEntry == null) "Add Class to Schedule" else "Edit Timetable Class",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Selector
                Text("Subject *", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                if (subjects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedSubjectDropdown,
                        onExpandedChange = { expandedSubjectDropdown = !expandedSubjectDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.name ?: "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubjectDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("subject_dropdown_anchor"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AttendBluePrimary,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedSubjectDropdown,
                            onDismissRequest = { expandedSubjectDropdown = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(s.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                                            if (s.code.isNotBlank()) {
                                                Text(s.code, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedSubject = s
                                        if (teacher.isBlank() && s.teacher.isNotBlank()) teacher = s.teacher
                                        expandedSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customSubjectName,
                        onValueChange = { customSubjectName = it },
                        label = { Text("Subject Name *") },
                        placeholder = { Text("e.g. Mathematics") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day of Week Selector
                Text("Day of Week", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysList.forEach { (dayInt, dayStr) ->
                        val isSelected = selectedDay == dayInt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AttendBluePrimary else Color(0xFF1E293B))
                                .clickable { selectedDay = dayInt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayStr.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            .clickable { showStartPicker = true }
                            .padding(12.dp)
                            .testTag("start_time_picker_btn")
                    ) {
                        Column {
                            Text("Start Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = AttendBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(startTime, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            .clickable { showEndPicker = true }
                            .padding(12.dp)
                            .testTag("end_time_picker_btn")
                    ) {
                        Column {
                            Text("End Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = AttendBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(endTime, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Classroom / Lab") },
                        placeholder = { Text("Room 302") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("room_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { teacher = it },
                        label = { Text("Faculty / Teacher") },
                        placeholder = { Text("Prof. Rao") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("timetable_teacher_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (initialEntry != null && onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusAbsent.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).testTag("delete_timetable_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }

                    Button(
                        onClick = {
                            val targetSubject = selectedSubject
                            val subjId = targetSubject?.id ?: 0L
                            val subjName = targetSubject?.name ?: customSubjectName.ifBlank { "Subject" }
                            val subjCode = targetSubject?.code ?: ""
                            val finalTeacher = teacher.ifBlank { targetSubject?.teacher ?: "" }

                            onConfirm(
                                subjId,
                                subjName,
                                subjCode,
                                finalTeacher,
                                room.trim(),
                                selectedDay,
                                startTime,
                                endTime
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(if (initialEntry != null) 1.5f else 1f).testTag("save_timetable_btn")
                    ) {
                        Text(if (initialEntry == null) "Schedule Class" else "Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        TimePickerModal(
            initialTime = startTime,
            onDismiss = { showStartPicker = false },
            onConfirm = {
                startTime = it
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        TimePickerModal(
            initialTime = endTime,
            onDismiss = { showEndPicker = false },
            onConfirm = {
                endTime = it
                showEndPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialParts = initialTime.split(":")
    val initialHour = initialParts.getOrNull(0)?.toIntOrNull() ?: 9
    val initialMinute = initialParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time", color = Color.White) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formatted = String.format(Locale.US, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onConfirm(formatted)
                }
            ) {
                Text("OK", color = AttendBluePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val initialMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(localDate)
                    }
                    onDismiss()
                }
            ) {
                Text("Select Date", color = AttendBluePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        colors = androidx.compose.material3.DatePickerDefaults.colors(
            containerColor = DarkSurface
        )
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StatusAbsent else AttendBluePrimary
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}
