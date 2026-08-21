package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AttendanceDonutChartCard(
    stats: OverallAttendanceStats,
    modifier: Modifier = Modifier
) {
    val total = stats.presentCount + stats.absentCount + stats.bunkedCount + stats.cancelledCount

    val pPresent = if (total > 0) (stats.presentCount.toFloat() / total) else 0f
    val pAbsent = if (total > 0) (stats.absentCount.toFloat() / total) else 0f
    val pBunked = if (total > 0) (stats.bunkedCount.toFloat() / total) else 0f
    val pCancelled = if (total > 0) (stats.cancelledCount.toFloat() / total) else 0f

    val animFactor by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "donutAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("attendance_donut_chart_card")
    ) {
        Column {
            Text(
                text = "Attendance Unit Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Distribution across all logged sessions",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 18.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                        if (total == 0) {
                            drawArc(
                                color = Color(0xFF334155),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            var currentAngle = -90f

                            val slices = listOf(
                                pPresent to StatusPresent,
                                pAbsent to StatusAbsent,
                                pBunked to StatusBunked,
                                pCancelled to StatusCancelled
                            )

                            slices.forEach { (pct, color) ->
                                val sweep = (pct * 360f * animFactor).coerceAtLeast(0f)
                                if (sweep > 0.5f) {
                                    drawArc(
                                        color = color,
                                        startAngle = currentAngle,
                                        sweepAngle = sweep - 2f, // slight gap
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    currentAngle += sweep
                                }
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (stats.totalUnits == 0) "0%" else String.format(Locale.US, "%.0f%%", stats.percentage),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "$total Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // Legend Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DonutLegendItem(
                        label = "Present",
                        count = stats.presentCount,
                        percent = if (total > 0) (stats.presentCount * 100 / total) else 0,
                        color = StatusPresent
                    )
                    DonutLegendItem(
                        label = "Absent",
                        count = stats.absentCount,
                        percent = if (total > 0) (stats.absentCount * 100 / total) else 0,
                        color = StatusAbsent
                    )
                    DonutLegendItem(
                        label = "Bunked",
                        count = stats.bunkedCount,
                        percent = if (total > 0) (stats.bunkedCount * 100 / total) else 0,
                        color = StatusBunked
                    )
                    DonutLegendItem(
                        label = "Cancelled",
                        count = stats.cancelledCount,
                        percent = if (total > 0) (stats.cancelledCount * 100 / total) else 0,
                        color = StatusCancelled
                    )
                }
            }
        }
    }
}

@Composable
fun DonutLegendItem(
    label: String,
    count: Int,
    percent: Int,
    color: Color
) {
    Row(
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
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(65.dp)
        )
        Text(
            text = "$count ($percent%)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}
