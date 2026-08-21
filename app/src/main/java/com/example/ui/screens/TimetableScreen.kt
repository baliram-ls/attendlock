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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry
import com.example.ui.components.AddEditTimetableEntryDialog
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.AttendBluePrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun TimetableScreen(
    timetableEntries: List<TimetableEntry>,
    subjects: List<Subject>,
    selectedDay: Int,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<TimetableEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<TimetableEntry?>(null) }
    var showScanDialog by remember { mutableStateOf(false) }

    val scanUiState by viewModel.scanUiState.collectAsStateWithLifecycle()

    val days = listOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu",
        5 to "Fri", 6 to "Sat", 7 to "Sun"
    )

    val dayEntries = remember(timetableEntries, selectedDay) {
        timetableEntries.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startTime }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("timetable_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Timetable",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${timetableEntries.size} total weekly classes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Surface(
                    onClick = {
                        viewModel.resetScanState()
                        showScanDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = AttendBluePrimary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AttendBluePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("scan_timetable_header_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AttendBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan Schedule",
                            color = AttendBluePrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Day Selector Tabs
            TabRow(
                selectedTabIndex = selectedDay - 1,
                containerColor = DarkBackground,
                contentColor = AttendBluePrimary,
                indicator = { tabPositions ->
                    if (selectedDay - 1 < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedDay - 1]),
                            color = AttendBluePrimary,
                            height = 3.dp
                        )
                    }
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DarkCardBorder)
                    )
                }
            ) {
                days.forEach { (dayInt, dayLabel) ->
                    val isSelected = selectedDay == dayInt
                    val classCountForDay = timetableEntries.count { it.dayOfWeek == dayInt }

                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTimetableDay(dayInt) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayLabel,
                                    color = if (isSelected) AttendBluePrimary else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (classCountForDay > 0) {
                                    Text(
                                        text = "$classCountForDay",
                                        color = if (isSelected) Color.White else TextMuted,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_day_$dayInt")
                    )
                }
            }

            // Classes List for Selected Day
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (dayEntries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface)
                                .border(androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder), RoundedCornerShape(20.dp))
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
                                        imageVector = Icons.Default.EventNote,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Classes on ${days.find { it.first == selectedDay }?.second ?: "this day"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap below to add classes manually or scan an image.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("empty_schedule_add_class_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Class Manually", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(dayEntries, key = { it.id }) { entry ->
                        val subject = subjects.find { it.id == entry.subjectId }
                        val subjectColorHex = subject?.colorHex ?: "#3B82F6"
                        val subjectColor = remember(subjectColorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(subjectColorHex))
                            } catch (e: Exception) {
                                AttendBluePrimary
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkSurface)
                                .border(androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder), RoundedCornerShape(18.dp))
                                .clickable { entryToEdit = entry }
                                .padding(16.dp)
                                .testTag("timetable_card_${entry.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(subjectColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = entry.subjectName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (entry.subjectCode.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = DarkSurfaceElevated,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = entry.subjectCode,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextSecondary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = AttendBluePrimary, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${entry.startTime} - ${entry.endTime}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF93C5FD),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            if (entry.room.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(entry.room, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                }
                                            }
                                        }

                                        if (entry.teacher.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(entry.teacher, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { entryToEdit = entry },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { entryToDelete = entry },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsent.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to add class
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = AttendBluePrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
                .testTag("timetable_fab_add_class")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Class", modifier = Modifier.size(26.dp))
        }
    }

    if (showAddDialog) {
        AddEditTimetableEntryDialog(
            subjects = subjects,
            defaultDayOfWeek = selectedDay,
            onDismiss = { showAddDialog = false },
            onConfirm = { subjId, subjName, subjCode, teacher, room, dayOfWeek, start, end ->
                viewModel.addTimetableEntry(
                    subjectId = subjId,
                    subjectName = subjName,
                    subjectCode = subjCode,
                    teacher = teacher,
                    room = room,
                    dayOfWeek = dayOfWeek,
                    startTime = start,
                    endTime = end
                )
                showAddDialog = false
            }
        )
    }

    entryToEdit?.let { entry ->
        AddEditTimetableEntryDialog(
            initialEntry = entry,
            subjects = subjects,
            defaultDayOfWeek = entry.dayOfWeek,
            onDismiss = { entryToEdit = null },
            onConfirm = { subjId, subjName, subjCode, teacher, room, dayOfWeek, start, end ->
                viewModel.updateTimetableEntry(
                    entry.copy(
                        subjectId = subjId,
                        subjectName = subjName,
                        subjectCode = subjCode,
                        teacher = teacher,
                        room = room,
                        dayOfWeek = dayOfWeek,
                        startTime = start,
                        endTime = end
                    )
                )
                entryToEdit = null
            },
            onDelete = {
                viewModel.deleteTimetableEntry(entry)
                entryToEdit = null
            }
        )
    }

    entryToDelete?.let { entry ->
        ConfirmDialog(
            title = "Delete Class",
            message = "Remove ${entry.subjectName} (${entry.startTime} - ${entry.endTime}) from timetable?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTimetableEntry(entry)
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    if (showScanDialog) {
        ScanTimetableDialog(
            scanUiState = scanUiState,
            onPickImage = { uri ->
                viewModel.scanTimetableImageUri(uri)
            },
            onApplyScanned = { items ->
                viewModel.applyScannedTimetable(items)
                showScanDialog = false
            },
            onDismiss = {
                viewModel.resetScanState()
                showScanDialog = false
            }
        )
    }
}
