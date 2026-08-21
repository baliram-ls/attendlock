package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AttendanceStatus
import com.example.data.local.entity.StudentProfile
import com.example.data.local.entity.Subject
import com.example.ui.components.AddEditTimetableEntryDialog
import com.example.ui.components.DatePickerModalDialog
import com.example.ui.components.DateSelectorStrip
import com.example.ui.components.OverallAttendanceCard
import com.example.ui.components.RequiredAndGoalCards
import com.example.ui.components.SafeBunkBanner
import com.example.ui.theme.AttendBluePrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusBunked
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.TodayClassItem
import com.example.utils.OverallAttendanceStats
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    studentProfile: StudentProfile?,
    overallStats: OverallAttendanceStats,
    todayClasses: List<TodayClassItem>,
    selectedDate: LocalDate,
    subjects: List<Subject>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddClassDialog by remember { mutableStateOf(false) }

    val greeting = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 4..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon 🌤️"
            in 17..21 -> "Good Evening 🌙"
            else -> "Good Night 🌌"
        }
    }

    val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sleek Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                                .testTag("calendar_picker_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pick Date",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setSelectedTab(NavigationTab.SETTINGS) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                                .testTag("settings_shortcut_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Horizontal Date Strip
            item {
                DateSelectorStrip(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.setSelectedDate(it) }
                )
            }

            // Overall Attendance Card
            item {
                OverallAttendanceCard(stats = overallStats)
            }

            // Required & Goal Cards
            item {
                RequiredAndGoalCards(stats = overallStats)
            }

            // Safe Bunk Banner
            if (overallStats.totalUnits > 0) {
                item {
                    SafeBunkBanner(safeBunks = overallStats.totalSafeBunks)
                }
            }

            // Today's Classes Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S CLASSES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextSecondary
                    )

                    Text(
                        text = "+ Add Class",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = AttendBluePrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAddClassDialog = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("add_class_header_btn")
                    )
                }
            }

            // Class Cards List or Empty State
            if (todayClasses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(24.dp))
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventSeat,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Classes Scheduled",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enjoy your free time or schedule a class.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = AttendBluePrimary,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { showAddClassDialog = true }
                            ) {
                                Text(
                                    text = "+ Schedule a Class",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                items(todayClasses, key = { it.timetableEntry.id }) { item ->
                    ClassAttendanceItemCard(
                        item = item,
                        onMark = { status ->
                            viewModel.markAttendance(item.timetableEntry, status)
                        },
                        onClear = {
                            viewModel.removeAttendance(item.timetableEntry)
                        }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddClassDialog = true },
            containerColor = AttendBluePrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
                .testTag("home_fab_add_class")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Class", modifier = Modifier.size(26.dp))
        }
    }

    if (showDatePicker) {
        DatePickerModalDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                viewModel.setSelectedDate(it)
                showDatePicker = false
            }
        )
    }

    if (showAddClassDialog) {
        AddEditTimetableEntryDialog(
            subjects = subjects,
            defaultDayOfWeek = selectedDate.dayOfWeek.value,
            onDismiss = { showAddClassDialog = false },
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
                showAddClassDialog = false
            }
        )
    }
}

@Composable
fun ClassAttendanceItemCard(
    item: TodayClassItem,
    onMark: (AttendanceStatus) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.timetableEntry
    val subject = item.subject
    val record = item.attendanceRecord
    val currentStatus = record?.status

    val subjectColorHex = subject?.colorHex ?: "#3B82F6"
    val subjectColor = remember(subjectColorHex) {
        try {
            Color(android.graphics.Color.parseColor(subjectColorHex))
        } catch (e: Exception) {
            AttendBluePrimary
        }
    }

    // Split start time (e.g. "09:00 AM" or "09:00") into time and AM/PM
    val (timeDigits, amPm) = remember(entry.startTime) {
        val parts = entry.startTime.trim().split(" ")
        if (parts.size >= 2) {
            parts[0] to parts[1].uppercase()
        } else {
            parts[0] to ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("class_card_${entry.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Border Color Strip
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(subjectColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Time Badge: rounded squircle with stacked time & AM/PM
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AttendBluePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeDigits,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AttendBluePrimary
                        )
                        if (amPm.isNotBlank()) {
                            Text(
                                text = amPm,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = AttendBluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Subject info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.subjectName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val metaParts = listOfNotNull(
                        entry.subjectCode.takeIf { it.isNotBlank() },
                        entry.room.takeIf { it.isNotBlank() },
                        entry.teacher.takeIf { it.isNotBlank() }
                    )
                    Text(
                        text = if (metaParts.isNotEmpty()) metaParts.joinToString(" • ") else "${entry.startTime} - ${entry.endTime}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextMuted,
                        maxLines = 1
                    )
                }

                // If marked, show clear icon
                if (currentStatus != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .testTag("btn_clear_mark_${entry.id}")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Present, Absent, Bunked, Cancelled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttendanceActionButton(
                    label = "Present",
                    isSelected = currentStatus == AttendanceStatus.PRESENT,
                    activeColor = StatusPresent,
                    onClick = { onMark(AttendanceStatus.PRESENT) },
                    modifier = Modifier.weight(1f).testTag("btn_present_${entry.id}")
                )

                AttendanceActionButton(
                    label = "Absent",
                    isSelected = currentStatus == AttendanceStatus.ABSENT,
                    activeColor = StatusAbsent,
                    onClick = { onMark(AttendanceStatus.ABSENT) },
                    modifier = Modifier.weight(1f).testTag("btn_absent_${entry.id}")
                )

                AttendanceActionButton(
                    label = "Bunked",
                    isSelected = currentStatus == AttendanceStatus.BUNKED,
                    activeColor = StatusBunked,
                    onClick = { onMark(AttendanceStatus.BUNKED) },
                    modifier = Modifier.weight(1f).testTag("btn_bunked_${entry.id}")
                )

                AttendanceActionButton(
                    label = "Cancelled",
                    isSelected = currentStatus == AttendanceStatus.CANCELLED,
                    activeColor = StatusCancelled,
                    onClick = { onMark(AttendanceStatus.CANCELLED) },
                    modifier = Modifier.weight(1f).testTag("btn_cancelled_${entry.id}")
                )
            }
        }
    }
}

@Composable
fun AttendanceActionButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor else DarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.dp else 0.5.dp,
            color = if (isSelected) activeColor else DarkCardBorder
        ),
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}
