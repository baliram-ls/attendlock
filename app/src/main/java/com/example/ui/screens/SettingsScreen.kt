package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AppSettings
import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.StudentProfile
import com.example.data.local.entity.Subject
import com.example.data.local.entity.TimetableEntry
import com.example.notification.NotificationHelper
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
import com.example.utils.BackupHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    studentProfile: StudentProfile?,
    appSettings: AppSettings?,
    subjects: List<Subject>,
    timetable: List<TimetableEntry>,
    attendanceRecords: List<AttendanceRecord>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentSettings = appSettings ?: AppSettings()

    var defaultTarget by remember(currentSettings) { mutableFloatStateOf(currentSettings.defaultTarget.toFloat()) }
    var reminderEnabled by remember(currentSettings) { mutableStateOf(currentSettings.reminderNotificationsEnabled) }
    var soundEnabled by remember(currentSettings) { mutableStateOf(currentSettings.notificationSound) }
    var vibrationEnabled by remember(currentSettings) { mutableStateOf(currentSettings.notificationVibration) }
    var themeMode by remember(currentSettings) { mutableStateOf(currentSettings.themeMode) }
    var geminiKey by remember(currentSettings) { mutableStateOf(currentSettings.geminiApiKey) }
    var showGeminiKey by remember { mutableStateOf(false) }

    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showLoadDemoDataConfirm by remember { mutableStateOf(false) }
    var showRestoreJsonDialog by remember { mutableStateOf(false) }
    var jsonRestoreInput by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf<String?>(null) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rules, preferences, data backup & developer info",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Student Profile Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
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
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AttendBluePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = AttendBluePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = studentProfile?.studentName?.ifBlank { "Student Profile" } ?: "Student Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = listOfNotNull(
                                        studentProfile?.collegeName?.takeIf { it.isNotBlank() },
                                        studentProfile?.semester?.takeIf { it.isNotBlank() },
                                        studentProfile?.section?.takeIf { it.isNotBlank() }
                                    ).joinToString(" • ").ifBlank { "Tap to configure student details" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Edit", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Attendance Rules Section
            item {
                SettingsSectionContainer(title = "Attendance Rules", icon = Icons.Default.Rule) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Default Target Percentage", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text("${defaultTarget.toInt()}%", style = MaterialTheme.typography.titleSmall, color = AttendBluePrimary, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = defaultTarget,
                        onValueChange = {
                            defaultTarget = it
                            viewModel.updateSettings(currentSettings.copy(defaultTarget = it.toInt()))
                        },
                        valueRange = 50f..95f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = AttendBluePrimary,
                            activeTrackColor = AttendBluePrimary,
                            inactiveTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("settings_target_slider")
                    )
                }
            }

            // Notifications Section
            item {
                SettingsSectionContainer(title = "Class Notifications & Reminders", icon = Icons.Default.Notifications) {
                    SettingsSwitchRow(
                        title = "Class Reminders",
                        subtitle = "Alert 10 minutes before scheduled classes start",
                        checked = reminderEnabled,
                        onCheckedChange = {
                            reminderEnabled = it
                            viewModel.updateSettings(currentSettings.copy(reminderNotificationsEnabled = it))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSwitchRow(
                        title = "Notification Sound",
                        subtitle = "Play alert tone for reminders",
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            viewModel.updateSettings(currentSettings.copy(notificationSound = it))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSwitchRow(
                        title = "Vibration",
                        subtitle = "Vibrate on class alerts",
                        checked = vibrationEnabled,
                        onCheckedChange = {
                            vibrationEnabled = it
                            viewModel.updateSettings(currentSettings.copy(notificationVibration = it))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            NotificationHelper.sendTestNotification(context)
                            viewModel.showSnackbar("Test notification sent!")
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("send_test_notification_btn")
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Test Notification")
                    }
                }
            }

            // Appearance Section
            item {
                SettingsSectionContainer(title = "Appearance", icon = Icons.Default.Palette) {
                    Text("Theme Mode", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("dark" to "Dark Mode", "light" to "Light Mode", "system" to "System").forEach { (mode, label) ->
                            val isSelected = themeMode == mode
                            Surface(
                                onClick = {
                                    themeMode = mode
                                    viewModel.updateSettings(currentSettings.copy(themeMode = mode))
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AttendBluePrimary else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) AttendBluePrimary else DarkCardBorder
                                ),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Timetable OCR & Gemini API Key
            item {
                SettingsSectionContainer(title = "Timetable OCR & Gemini AI", icon = Icons.Default.AutoAwesome) {
                    Text(
                        text = "Gemini powers the instant camera & image extraction of weekly timetables. You can configure your own Gemini API key below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = { geminiKey = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                Icon(
                                    imageVector = if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("gemini_key_input"),
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateSettings(currentSettings.copy(geminiApiKey = geminiKey.trim()))
                                viewModel.showSnackbar("Gemini API Key saved!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("save_gemini_key_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Key")
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).testTag("get_gemini_key_btn")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Get Free Key", maxLines = 1)
                        }
                    }
                }
            }

            // Data & Backup Section
            item {
                SettingsSectionContainer(title = "Data & Backup", icon = Icons.Default.Storage) {
                    Text(
                        text = "Backup or export your attendance logs, subjects, and weekly schedules locally as JSON or CSV reports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val json = BackupHelper.generateJsonBackup(subjects, timetable, attendanceRecords)
                                BackupHelper.shareText(context, "AttendMate_Backup.json", json)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("export_json_btn")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON")
                        }

                        OutlinedButton(
                            onClick = { showRestoreJsonDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("restore_json_btn")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore JSON")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val csv = BackupHelper.generateCsvReport(subjects, attendanceRecords)
                            BackupHelper.shareText(context, "AttendMate_Attendance_Report.csv", csv)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_csv_btn")
                    ) {
                        Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Attendance CSV Report")
                    }
                }
            }

            // Developer & Reset Options
            item {
                SettingsSectionContainer(title = "Developer & Reset Options", icon = Icons.Default.Code) {
                    Text(
                        text = "Options for testing, demo loading, and resetting data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showLoadDemoDataConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("load_sample_data_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Sample College Demo Data", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showClearDataConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusAbsent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("clear_all_data_btn")
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All App Data", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Contact Developer
            item {
                SettingsSectionContainer(title = "Contact Developer", icon = Icons.Default.Favorite) {
                    Text(
                        text = "Made with ❤️ by Baliram",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Have questions, suggestions or want to contribute? Visit the developer GitHub profile:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/baliram-ls"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292F)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4B5563)),
                        modifier = Modifier.fillMaxWidth().testTag("github_developer_btn")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open GitHub: @baliram-ls", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Legal & About
            item {
                SettingsSectionContainer(title = "About & Legal", icon = Icons.Default.Info) {
                    Text("AttendMate v1.0.0", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Built for college students with Jetpack Compose & Room", style = MaterialTheme.typography.bodySmall, color = TextMuted)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { showPrivacyDialog = true }) { Text("Privacy Policy", color = TextSecondary, fontSize = 12.sp) }
                        TextButton(onClick = { showTermsDialog = true }) { Text("Terms", color = TextSecondary, fontSize = 12.sp) }
                        TextButton(onClick = { showLicenseDialog = true }) { Text("Licenses", color = TextSecondary, fontSize = 12.sp) }
                    }
                }
            }
        }
    }

    if (showClearDataConfirm) {
        ConfirmDialog(
            title = "Clear All Data?",
            message = "This will erase all registered subjects, timetable entries, and logged attendance records. This cannot be undone.",
            confirmText = "Clear All",
            isDestructive = true,
            onConfirm = {
                viewModel.clearAllData()
                showClearDataConfirm = false
            },
            onDismiss = { showClearDataConfirm = false }
        )
    }

    if (showLoadDemoDataConfirm) {
        ConfirmDialog(
            title = "Load Sample College Data?",
            message = "This will populate realistic college subjects (Data Structures, OS, DBMS, Networks, SE) and 3 weeks of attendance logs for testing.",
            confirmText = "Load Sample Data",
            isDestructive = false,
            onConfirm = {
                viewModel.loadSampleData()
                showLoadDemoDataConfirm = false
            },
            onDismiss = { showLoadDemoDataConfirm = false }
        )
    }

    if (showRestoreJsonDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestoreJsonDialog = false
                restoreError = null
            },
            title = { Text("Restore from JSON", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Paste your exported AttendMate JSON backup string below:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = jsonRestoreInput,
                        onValueChange = {
                            jsonRestoreInput = it
                            restoreError = null
                        },
                        placeholder = { Text("{\"version\": 1, ...}") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    restoreError?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(err, color = StatusAbsent, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val parsed = BackupHelper.parseJsonBackup(jsonRestoreInput)
                            scope.launch {
                                (context.applicationContext as com.example.AttendMateApp).repository.restoreData(
                                    parsed.first,
                                    parsed.second,
                                    parsed.third
                                )
                                viewModel.showSnackbar("Successfully restored ${parsed.first.size} subjects and ${parsed.third.size} attendance records!")
                                showRestoreJsonDialog = false
                                jsonRestoreInput = ""
                            }
                        } catch (e: Exception) {
                            restoreError = "Invalid JSON format: ${e.message}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary)
                ) {
                    Text("Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreJsonDialog = false
                    restoreError = null
                }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showEditProfileDialog) {
        EditProfileModal(
            currentProfile = studentProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { profile ->
                viewModel.saveProfile(profile)
                showEditProfileDialog = false
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = Color.White) },
            text = {
                Text(
                    "AttendMate is an offline-first college attendance app. All your attendance logs, timetable entries, and personal notes are stored strictly locally on your device in a secure SQLite Room database.\n\nNo data is sold, tracked, or sent to third-party advertising servers.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close", color = AttendBluePrimary) }
            },
            containerColor = DarkSurface
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", color = Color.White) },
            text = {
                Text(
                    "AttendMate is provided free of charge for students to track classes and academic targets. Projections and safe bunk estimates are calculated strictly mathematically based on your entered parameters.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("Close", color = AttendBluePrimary) }
            },
            containerColor = DarkSurface
        )
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text("Open Source Licenses", color = Color.White) },
            text = {
                Text(
                    "AttendMate utilizes open source libraries including AndroidX Jetpack, Compose, Room, Kotlin Coroutines, Retrofit, OkHttp, and Material Design 3 licensed under Apache 2.0.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) { Text("Close", color = AttendBluePrimary) }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun SettingsSectionContainer(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AttendBluePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = AttendBluePrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AttendBluePrimary,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}

@Composable
fun EditProfileModal(
    currentProfile: StudentProfile?,
    onDismiss: () -> Unit,
    onSave: (StudentProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile?.studentName ?: "") }
    var college by remember { mutableStateOf(currentProfile?.collegeName ?: "") }
    var semester by remember { mutableStateOf(currentProfile?.semester ?: "") }
    var section by remember { mutableStateOf(currentProfile?.section ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Profile", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AttendBluePrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = college,
                    onValueChange = { college = it },
                    label = { Text("College Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AttendBluePrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Semester") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AttendBluePrimary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        (currentProfile ?: StudentProfile()).copy(
                            studentName = name.trim(),
                            collegeName = college.trim(),
                            semester = semester.trim(),
                            section = section.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AttendBluePrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}
