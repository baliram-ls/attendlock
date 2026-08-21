package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.StudentProfile
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.AttendBluePrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavigationTab
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
            val studentProfile by viewModel.studentProfile.collectAsStateWithLifecycle()

            val isDarkTheme = when (appSettings?.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                // Request notification permissions for Android 13+
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* permission handled */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val isSetupCompleted = studentProfile?.isSetupCompleted == true

                if (!isSetupCompleted && studentProfile != null) {
                    SetupScreen(
                        onComplete = { profile ->
                            viewModel.saveProfile(profile)
                        },
                        onSkip = {
                            viewModel.saveProfile(
                                studentProfile?.copy(isSetupCompleted = true)
                                    ?: StudentProfile(isSetupCompleted = true)
                            )
                        }
                    )
                } else {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val studentProfile by viewModel.studentProfile.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val timetableEntries by viewModel.timetableEntries.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.attendanceRecords.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedTimetableDay by viewModel.selectedTimetableDay.collectAsStateWithLifecycle()
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AttendMateBottomNav(
                currentTab = selectedTab,
                onTabSelected = { viewModel.setSelectedTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabContent"
            ) { tab ->
                when (tab) {
                    NavigationTab.HOME -> HomeScreen(
                        studentProfile = studentProfile,
                        overallStats = overallStats,
                        todayClasses = todayClasses,
                        selectedDate = selectedDate,
                        subjects = subjects,
                        viewModel = viewModel
                    )

                    NavigationTab.TIMETABLE -> TimetableScreen(
                        timetableEntries = timetableEntries,
                        subjects = subjects,
                        selectedDay = selectedTimetableDay,
                        viewModel = viewModel
                    )

                    NavigationTab.SUBJECTS -> SubjectsScreen(
                        subjects = subjects,
                        attendanceRecords = attendanceRecords,
                        defaultTarget = appSettings?.defaultTarget ?: 75,
                        viewModel = viewModel
                    )

                    NavigationTab.ANALYTICS -> AnalyticsScreen(
                        overallStats = overallStats
                    )

                    NavigationTab.SETTINGS -> SettingsScreen(
                        studentProfile = studentProfile,
                        appSettings = appSettings,
                        subjects = subjects,
                        timetable = timetableEntries,
                        attendanceRecords = attendanceRecords,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AttendMateBottomNav(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem(NavigationTab.HOME, "Home", Icons.Default.Home, "nav_home"),
        NavItem(NavigationTab.TIMETABLE, "Timetable", Icons.Default.CalendarMonth, "nav_timetable"),
        NavItem(NavigationTab.SUBJECTS, "Subjects", Icons.Default.MenuBook, "nav_subjects"),
        NavItem(NavigationTab.ANALYTICS, "Analytics", Icons.Default.QueryStats, "nav_analytics"),
        NavItem(NavigationTab.SETTINGS, "Settings", Icons.Default.Settings, "nav_settings")
    )

    NavigationBar(
        containerColor = com.example.ui.theme.BottomNavBg,
        contentColor = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier
            .border(
                width = 1.dp,
                color = DarkCardBorder,
                shape = androidx.compose.ui.graphics.RectangleShape
            )
            .testTag("bottom_navigation_bar")
    ) {
        navItems.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = AttendBluePrimary,
                    indicatorColor = AttendBluePrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

data class NavItem(
    val tab: NavigationTab,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)
