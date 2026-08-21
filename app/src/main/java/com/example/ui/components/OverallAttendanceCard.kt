package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AttendBluePrimary
import com.example.ui.theme.AttendBlueSecondary
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusBunked
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.utils.OverallAttendanceStats
import java.util.Locale

@Composable
fun OverallAttendanceCard(
    stats: OverallAttendanceStats,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (stats.percentage / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "overallProgress"
    )

    val progressColor = when {
        stats.totalUnits == 0 -> Color(0xFF64748B)
        stats.percentage >= stats.targetPercentage -> StatusPresent
        stats.percentage >= stats.targetPercentage - 10 -> StatusBunked
        else -> StatusAbsent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(30.dp))
            .padding(22.dp)
            .testTag("overall_attendance_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "OVERALL ATTENDANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (stats.totalUnits == 0) "0.0%" else String.format(Locale.US, "%.1f%%", stats.percentage),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (stats.percentage >= stats.targetPercentage) "Target Met" else "Goal: ${stats.targetPercentage}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (stats.percentage >= stats.targetPercentage) AttendBlueSecondary else Color(0xFFF87171)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${stats.presentCount} / ${stats.totalUnits} Units Attended",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats breakdown pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusPill(label = "P", count = stats.presentCount, color = StatusPresent)
                    StatusPill(label = "A", count = stats.absentCount, color = StatusAbsent)
                    StatusPill(label = "B", count = stats.bunkedCount, color = StatusBunked)
                    if (stats.cancelledCount > 0) {
                        StatusPill(label = "C", count = stats.cancelledCount, color = StatusCancelled)
                    }
                }
            }

            // Sleek Circular Progress Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .padding(start = 6.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(96.dp),
                    color = Color(0x14FFFFFF),
                    strokeWidth = 9.dp,
                    strokeCap = StrokeCap.Round
                )
                // Foreground Progress
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(96.dp),
                    color = if (stats.percentage >= stats.targetPercentage) AttendBluePrimary else progressColor,
                    strokeWidth = 9.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            stats.totalUnits == 0 -> "NO DATA"
                            stats.percentage >= stats.targetPercentage -> "ON TRACK"
                            else -> "BELOW"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    count: Int,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = color
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = Color.White
            )
        }
    }
}
