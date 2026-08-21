package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.AttendanceStatus
import com.example.data.local.entity.Subject
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.AttendanceSimulatorSheet
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.AttendBluePrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusBunked
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.utils.AttendanceCalculator
import com.example.utils.SubjectAttendanceStats
import java.util.Locale

@Composable
fun SubjectsScreen(
    subjects: List<Subject>,
    attendanceRecords: List<AttendanceRecord>,
    defaultTarget: Int,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }
    var subjectToSimulate by remember { mutableStateOf<Subject?>(null) }

    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) subjects
        else subjects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true) ||
                    it.teacher.contains(searchQuery, ignoreCase = true)
        }
    }

    val subjectStatsMap = remember(subjects, attendanceRecords, defaultTarget) {
        subjects.associate { it.id to AttendanceCalculator.calculateSubjectStats(it, attendanceRecords, defaultTarget) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("subjects_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Subjects",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${subjects.size} registered subjects",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_subject_header_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Subject", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Field
            if (subjects.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search subjects, codes, teachers...", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            // Subject Cards
            if (filteredSubjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (subjects.isEmpty()) "No Subjects Added Yet" else "No matching subjects found",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (subjects.isEmpty()) "Add your college subjects to track attendance." else "Try searching for a different keyword.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            if (subjects.isEmpty()) {
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("empty_add_first_subject_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Subject", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredSubjects, key = { it.id }) { subject ->
                    val stats = subjectStatsMap[subject.id]
                    SubjectDetailCard(
                        subject = subject,
                        stats = stats,
                        onEdit = { subjectToEdit = subject },
                        onSimulate = { subjectToSimulate = subject },
                        onQuickMark = { status ->
                            viewModel.quickMarkSubject(subject.id, status)
                        }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = AttendBluePrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
                .testTag("subjects_fab_add")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject", modifier = Modifier.size(26.dp))
        }
    }

    if (showAddDialog) {
        AddEditSubjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, code, teacher, target, colorHex ->
                viewModel.addSubject(name, code, teacher, target, colorHex)
                showAddDialog = false
            }
        )
    }

    subjectToEdit?.let { subject ->
        AddEditSubjectDialog(
            initialSubject = subject,
            onDismiss = { subjectToEdit = null },
            onConfirm = { name, code, teacher, target, colorHex ->
                viewModel.updateSubject(
                    subject.copy(
                        name = name,
                        code = code,
                        teacher = teacher,
                        targetAttendance = target,
                        colorHex = colorHex
                    )
                )
                subjectToEdit = null
            },
            onDelete = {
                subjectToDelete = subject
                subjectToEdit = null
            }
        )
    }

    subjectToDelete?.let { subject ->
        ConfirmDialog(
            title = "Delete Subject",
            message = "Are you sure you want to delete '${subject.name}'? This will also remove all its timetable entries and attendance records.",
            confirmText = "Delete Subject",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSubject(subject)
                subjectToDelete = null
            },
            onDismiss = { subjectToDelete = null }
        )
    }

    subjectToSimulate?.let { subject ->
        val stats = subjectStatsMap[subject.id]
        AttendanceSimulatorSheet(
            subject = subject,
            stats = stats,
            onDismiss = { subjectToSimulate = null }
        )
    }
}

@Composable
fun SubjectDetailCard(
    subject: Subject,
    stats: SubjectAttendanceStats?,
    onEdit: () -> Unit,
    onSimulate: () -> Unit,
    onQuickMark: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = remember(subject.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(subject.colorHex))
        } catch (e: Exception) {
            AttendBluePrimary
        }
    }

    val percentage = stats?.percentage ?: 0.0
    val totalUnits = stats?.totalUnits ?: 0
    val target = subject.targetAttendance

    val isAboveTarget = percentage >= target

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .clickable { onEdit() }
            .padding(18.dp)
            .testTag("subject_card_${subject.id}")
    ) {
        Column {
            // Top Row: Title, Code, Color bar, Edit icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(42.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(subjectColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (subject.code.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = DarkSurfaceElevated,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = subject.code,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (subject.teacher.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(subject.teacher, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Subject", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage & Target Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (totalUnits == 0) "0.0%" else String.format(Locale.US, "%.1f%%", percentage),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (totalUnits == 0) TextSecondary else if (isAboveTarget) StatusPresent else StatusAbsent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stats?.presentCount ?: 0}/$totalUnits Attended",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Surface(
                    color = if (isAboveTarget) StatusPresent.copy(alpha = 0.15f) else StatusAbsent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (isAboveTarget) StatusPresent.copy(alpha = 0.4f) else StatusAbsent.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "Target $target%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isAboveTarget) StatusPresent else StatusAbsent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Bar
            val progress = if (totalUnits > 0) (percentage / 100.0).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAboveTarget) StatusPresent else StatusAbsent,
                trackColor = Color(0xFF334155),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Safe bunks or Required Classes Helper text
            if (stats != null && totalUnits > 0) {
                if (stats.safeBunks > 0) {
                    Text(
                        text = "🎉 You can miss next ${stats.safeBunks} class${if (stats.safeBunks > 1) "es" else ""} safely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusBunked,
                        fontWeight = FontWeight.Medium
                    )
                } else if (stats.requiredConsecutive > 0) {
                    Text(
                        text = "⚠️ Must attend next ${stats.requiredConsecutive} class${if (stats.requiredConsecutive > 1) "es" else ""} to reach $target%.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusAbsent,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Attendance exactly at target. Keep attending!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                Text(
                    text = "No attendance logged yet. Use quick action buttons below:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Mark Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickMarkButton(
                    label = "+ Present",
                    color = StatusPresent,
                    onClick = { onQuickMark(AttendanceStatus.PRESENT) },
                    modifier = Modifier.weight(1f)
                )
                QuickMarkButton(
                    label = "+ Absent",
                    color = StatusAbsent,
                    onClick = { onQuickMark(AttendanceStatus.ABSENT) },
                    modifier = Modifier.weight(1f)
                )
                QuickMarkButton(
                    label = "+ Bunk",
                    color = StatusBunked,
                    onClick = { onQuickMark(AttendanceStatus.BUNKED) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simulate Button
            Surface(
                onClick = onSimulate,
                shape = RoundedCornerShape(8.dp),
                color = AttendBluePrimary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AttendBluePrimary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = AttendBluePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simulate Attendance",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = AttendBluePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickMarkButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = color
            )
        }
    }
}
