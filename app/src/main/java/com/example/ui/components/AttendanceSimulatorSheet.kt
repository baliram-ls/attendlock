package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.Subject
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
import com.example.utils.AttendanceCalculator
import com.example.utils.SimulationResult
import com.example.utils.SubjectAttendanceStats
import java.util.Locale

@Composable
fun AttendanceSimulatorSheet(
    subject: Subject,
    stats: SubjectAttendanceStats?,
    onDismiss: () -> Unit
) {
    var hypoAttended by remember { mutableIntStateOf(0) }
    var hypoMissed by remember { mutableIntStateOf(0) }

    val currentStats = stats ?: SubjectAttendanceStats(
        subject = subject,
        presentCount = 0,
        absentCount = 0,
        bunkedCount = 0,
        cancelledCount = 0,
        totalUnits = 0,
        percentage = 0.0,
        requiredConsecutive = 0,
        safeBunks = 0,
        goalStatus = "Needs Work"
    )

    val simulation = remember(hypoAttended, hypoMissed, currentStats) {
        AttendanceCalculator.simulateAttendance(currentStats, hypoAttended, hypoMissed)
    }

    val subjectColor = remember(subject.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(subject.colorHex))
        } catch (e: Exception) {
            AttendBluePrimary
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AttendBluePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = AttendBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Attendance Simulator",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(subjectColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Stats Card
                StatsSection(
                    label = "CURRENT",
                    percentage = simulation.currentPercentage,
                    present = simulation.currentPresent,
                    total = simulation.currentTotal,
                    target = simulation.target,
                    isCurrent = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Simulation Controls
                SimulationControls(
                    hypoAttended = hypoAttended,
                    hypoMissed = hypoMissed,
                    onAttendedChange = { hypoAttended = (hypoAttended + it).coerceAtLeast(0) },
                    onMissedChange = { hypoMissed = (hypoMissed + it).coerceAtLeast(0) },
                    onReset = {
                        hypoAttended = 0
                        hypoMissed = 0
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Projected Stats Card
                StatsSection(
                    label = "PROJECTED",
                    percentage = simulation.projectedPercentage,
                    present = simulation.projectedPresent,
                    total = simulation.projectedTotal,
                    target = simulation.target,
                    isCurrent = false,
                    difference = simulation.percentageDifference
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Insights
                SimulationInsights(simulation = simulation)
            }
        }
    }
}

@Composable
private fun StatsSection(
    label: String,
    percentage: Double,
    present: Int,
    total: Int,
    target: Int,
    isCurrent: Boolean,
    difference: Double? = null
) {
    val isAboveTarget = percentage >= target
    val statusColor = if (total == 0) TextSecondary else if (isAboveTarget) StatusPresent else StatusAbsent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = TextMuted
                )
                if (!isCurrent && difference != null && difference != 0.0) {
                    val diffColor = if (difference > 0) StatusPresent else StatusAbsent
                    val diffPrefix = if (difference > 0) "+" else ""
                    Surface(
                        color = diffColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (difference > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = diffColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${diffPrefix}${String.format(Locale.US, "%.1f", difference)}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = diffColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (total == 0) "0.0%" else String.format(Locale.US, "%.1f%%", percentage),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$present/$total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 3.dp)
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

            val progress = if (total > 0) (percentage / 100.0).toFloat().coerceIn(0f, 1f) else 0f
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = Color(0xFF334155),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
private fun SimulationControls(
    hypoAttended: Int,
    hypoMissed: Int,
    onAttendedChange: (Int) -> Unit,
    onMissedChange: (Int) -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkBackground)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIMULATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = TextMuted
                )
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attended Row
            SimulationCounterRow(
                label = "Classes Attended",
                value = hypoAttended,
                accentColor = StatusPresent,
                onAdd = { onAttendedChange(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Missed Row
            SimulationCounterRow(
                label = "Classes Missed",
                value = hypoMissed,
                accentColor = StatusAbsent,
                onAdd = { onMissedChange(it) }
            )
        }
    }
}

@Composable
private fun SimulationCounterRow(
    label: String,
    value: Int,
    accentColor: Color,
    onAdd: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickSimButton(text = "+1", color = accentColor) { onAdd(1) }
            QuickSimButton(text = "+5", color = accentColor) { onAdd(5) }

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (value > 0) accentColor else TextMuted
                )
            }
        }
    }
}

@Composable
private fun QuickSimButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier
            .width(42.dp)
            .height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = color
            )
        }
    }
}

@Composable
private fun SimulationInsights(simulation: SimulationResult) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "INSIGHTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            InsightRow(
                label = "Classes to reach 75%",
                value = if (simulation.requiredFor75 == 0) "Already above" else "${simulation.requiredFor75}",
                valueColor = if (simulation.requiredFor75 == 0) StatusPresent else StatusBunked
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = DarkCardBorder
            )

            InsightRow(
                label = "Classes to reach 80%",
                value = if (simulation.requiredFor80 == 0) "Already above" else "${simulation.requiredFor80}",
                valueColor = if (simulation.requiredFor80 == 0) StatusPresent else StatusBunked
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = DarkCardBorder
            )

            InsightRow(
                label = "Max classes missable (75%)",
                value = if (simulation.projectedTotal == 0) "N/A" else "${simulation.maxMissableFor75}",
                valueColor = if (simulation.maxMissableFor75 > 0) StatusPresent else StatusAbsent
            )
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}
