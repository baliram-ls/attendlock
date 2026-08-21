package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AttendanceDonutChartCard
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
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.utils.OverallAttendanceStats
import com.example.utils.SubjectAttendanceStats
import java.util.Locale

@Composable
fun AnalyticsScreen(
    overallStats: OverallAttendanceStats,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("analytics_screen")
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
                        text = "Attendance Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Performance insights, breakdown & projections",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
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

            // Donut Chart Card
            item {
                AttendanceDonutChartCard(stats = overallStats)
            }

            // Summary Highlights Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricHighlightCard(
                        title = "Above Target",
                        value = "${overallStats.subjectsAboveTarget} Subjects",
                        icon = Icons.Default.CheckCircleOutline,
                        color = StatusPresent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricHighlightCard(
                        title = "Below Target",
                        value = "${overallStats.subjectsBelowTarget} Subjects",
                        icon = Icons.Default.ReportProblem,
                        color = if (overallStats.subjectsBelowTarget > 0) StatusAbsent else StatusPresent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Best / Lowest Subjects if data exists
            if (overallStats.bestSubject != null || overallStats.lowestSubject != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        overallStats.bestSubject?.let { best ->
                            SubjectHighlightCard(
                                label = "Best Attendance",
                                subjectName = best.subject.name,
                                percentage = best.percentage,
                                isBest = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        overallStats.lowestSubject?.let { lowest ->
                            SubjectHighlightCard(
                                label = "Lowest Attendance",
                                subjectName = lowest.subject.name,
                                percentage = lowest.percentage,
                                isBest = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Subject Breakdown Progress Bars
            if (overallStats.subjectStats.isNotEmpty()) {
                item {
                    Text(
                        text = "Subject Performance Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(overallStats.subjectStats, key = { it.subject.id }) { stat ->
                    SubjectComparisonBarItem(stat = stat)
                }
            }
        }
    }
}

@Composable
fun MetricHighlightCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubjectHighlightCard(
    label: String,
    subjectName: String,
    percentage: Double,
    isBest: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isBest) StatusPresent else if (percentage < 75) StatusAbsent else StatusBunked

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isBest) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subjectName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(Locale.US, "%.1f%%", percentage),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SubjectComparisonBarItem(
    stat: SubjectAttendanceStats,
    modifier: Modifier = Modifier
) {
    val subject = stat.subject
    val target = subject.targetAttendance
    val percentage = stat.percentage
    val isAbove = percentage >= target

    val color = remember(subject.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(subject.colorHex))
        } catch (e: Exception) {
            AttendBluePrimary
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = if (stat.totalUnits == 0) "0.0%" else String.format(Locale.US, "%.1f%%", percentage),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (stat.totalUnits == 0) TextSecondary else if (isAbove) StatusPresent else StatusAbsent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = if (stat.totalUnits > 0) (percentage / 100.0).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAbove) StatusPresent else StatusAbsent,
                trackColor = Color(0xFF334155),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stat.presentCount}P / ${stat.absentCount}A / ${stat.bunkedCount}B (${stat.totalUnits} Units)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "Goal: $target%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
