package com.example.utils

import com.example.data.local.entity.AttendanceRecord
import com.example.data.local.entity.AttendanceStatus
import com.example.data.local.entity.Subject
import kotlin.math.ceil
import kotlin.math.floor

data class SubjectAttendanceStats(
    val subject: Subject,
    val presentCount: Int,
    val absentCount: Int,
    val bunkedCount: Int,
    val cancelledCount: Int,
    val totalUnits: Int,
    val percentage: Double,
    val requiredConsecutive: Int,
    val safeBunks: Int,
    val goalStatus: String
)

data class SimulationResult(
    val currentPresent: Int,
    val currentTotal: Int,
    val currentPercentage: Double,
    val hypotheticalAttended: Int,
    val hypotheticalMissed: Int,
    val projectedPresent: Int,
    val projectedTotal: Int,
    val projectedPercentage: Double,
    val percentageDifference: Double,
    val requiredFor75: Int,
    val requiredFor80: Int,
    val maxMissableFor75: Int,
    val target: Int
)

data class OverallAttendanceStats(
    val presentCount: Int,
    val absentCount: Int,
    val bunkedCount: Int,
    val cancelledCount: Int,
    val totalUnits: Int,
    val percentage: Double,
    val targetPercentage: Int,
    val requiredConsecutive: Int,
    val totalSafeBunks: Int,
    val goalStatus: String,
    val subjectsBelowTarget: Int,
    val subjectsAboveTarget: Int,
    val bestSubject: SubjectAttendanceStats?,
    val lowestSubject: SubjectAttendanceStats?,
    val subjectStats: List<SubjectAttendanceStats>
)

object AttendanceCalculator {

    fun calculateSubjectStats(
        subject: Subject,
        records: List<AttendanceRecord>,
        fallbackTarget: Int = 75
    ): SubjectAttendanceStats {
        val subjectRecords = records.filter { it.subjectId == subject.id }
        val present = subjectRecords.count { it.status == AttendanceStatus.PRESENT }
        val absent = subjectRecords.count { it.status == AttendanceStatus.ABSENT }
        val bunked = subjectRecords.count { it.status == AttendanceStatus.BUNKED }
        val cancelled = subjectRecords.count { it.status == AttendanceStatus.CANCELLED }

        val totalUnits = present + absent + bunked
        val percentage = if (totalUnits > 0) (present.toDouble() / totalUnits) * 100.0 else 0.0
        val target = if (subject.targetAttendance > 0) subject.targetAttendance else fallbackTarget

        val required = if (totalUnits == 0) {
            0
        } else if (percentage < target) {
            val numerator = target * totalUnits - 100 * present
            val denominator = 100 - target
            if (denominator > 0 && numerator > 0) {
                ceil(numerator.toDouble() / denominator).toInt()
            } else 0
        } else {
            0
        }

        val safeBunks = if (totalUnits == 0 || percentage < target) {
            0
        } else {
            val numerator = 100 * present - target * totalUnits
            if (target > 0 && numerator >= 0) {
                floor(numerator.toDouble() / target).toInt()
            } else 0
        }

        val goalStatus = when {
            totalUnits == 0 -> "Needs Work"
            percentage >= target + 5.0 -> "Target Achieved"
            percentage >= target -> "On Track"
            else -> "Needs Work"
        }

        return SubjectAttendanceStats(
            subject = subject,
            presentCount = present,
            absentCount = absent,
            bunkedCount = bunked,
            cancelledCount = cancelled,
            totalUnits = totalUnits,
            percentage = percentage,
            requiredConsecutive = required,
            safeBunks = safeBunks,
            goalStatus = goalStatus
        )
    }

    fun calculateOverallStats(
        subjects: List<Subject>,
        records: List<AttendanceRecord>,
        targetPercentage: Int = 75
    ): OverallAttendanceStats {
        val subjectStats = subjects.map { calculateSubjectStats(it, records, targetPercentage) }

        val present = records.count { it.status == AttendanceStatus.PRESENT }
        val absent = records.count { it.status == AttendanceStatus.ABSENT }
        val bunked = records.count { it.status == AttendanceStatus.BUNKED }
        val cancelled = records.count { it.status == AttendanceStatus.CANCELLED }

        val totalUnits = present + absent + bunked
        val percentage = if (totalUnits > 0) (present.toDouble() / totalUnits) * 100.0 else 0.0

        val required = if (totalUnits == 0) {
            0
        } else if (percentage < targetPercentage) {
            val numerator = targetPercentage * totalUnits - 100 * present
            val denominator = 100 - targetPercentage
            if (denominator > 0 && numerator > 0) {
                ceil(numerator.toDouble() / denominator).toInt()
            } else 0
        } else {
            0
        }

        val safeBunks = if (totalUnits == 0 || percentage < targetPercentage) {
            0
        } else {
            val numerator = 100 * present - targetPercentage * totalUnits
            if (targetPercentage > 0 && numerator >= 0) {
                floor(numerator.toDouble() / targetPercentage).toInt()
            } else 0
        }

        val goalStatus = when {
            totalUnits == 0 -> "Needs Work"
            percentage >= targetPercentage + 5.0 -> "Target Achieved"
            percentage >= targetPercentage -> "On Track"
            else -> "Needs Work"
        }

        val subjectsBelow = subjectStats.count { it.totalUnits > 0 && it.percentage < it.subject.targetAttendance }
        val subjectsAbove = subjectStats.count { it.totalUnits > 0 && it.percentage >= it.subject.targetAttendance }

        val activeSubjects = subjectStats.filter { it.totalUnits > 0 }
        val bestSubject = activeSubjects.maxByOrNull { it.percentage }
        val lowestSubject = activeSubjects.minByOrNull { it.percentage }
        val totalSafeBunksSum = subjectStats.sumOf { it.safeBunks }

        return OverallAttendanceStats(
            presentCount = present,
            absentCount = absent,
            bunkedCount = bunked,
            cancelledCount = cancelled,
            totalUnits = totalUnits,
            percentage = percentage,
            targetPercentage = targetPercentage,
            requiredConsecutive = required,
            totalSafeBunks = if (safeBunks > 0) safeBunks else totalSafeBunksSum,
            goalStatus = goalStatus,
            subjectsBelowTarget = subjectsBelow,
            subjectsAboveTarget = subjectsAbove,
            bestSubject = bestSubject,
            lowestSubject = lowestSubject,
            subjectStats = subjectStats
        )
    }

    fun simulateAttendance(
        currentStats: SubjectAttendanceStats,
        hypotheticalAttended: Int,
        hypotheticalMissed: Int
    ): SimulationResult {
        val currentPresent = currentStats.presentCount
        val currentTotal = currentStats.totalUnits
        val currentPercentage = currentStats.percentage
        val target = currentStats.subject.targetAttendance

        val safeAttended = hypotheticalAttended.coerceAtLeast(0)
        val safeMissed = hypotheticalMissed.coerceAtLeast(0)

        val projectedPresent = currentPresent + safeAttended
        val projectedTotal = currentTotal + safeAttended + safeMissed
        val projectedPercentage = if (projectedTotal > 0) {
            (projectedPresent.toDouble() / projectedTotal) * 100.0
        } else {
            0.0
        }

        val percentageDifference = projectedPercentage - currentPercentage

        val requiredFor75 = classesRequiredToReachTarget(projectedPresent, projectedTotal, 75)
        val requiredFor80 = classesRequiredToReachTarget(projectedPresent, projectedTotal, 80)
        val maxMissableFor75 = maxClassesCanMiss(projectedPresent, projectedTotal, 75)

        return SimulationResult(
            currentPresent = currentPresent,
            currentTotal = currentTotal,
            currentPercentage = currentPercentage,
            hypotheticalAttended = safeAttended,
            hypotheticalMissed = safeMissed,
            projectedPresent = projectedPresent,
            projectedTotal = projectedTotal,
            projectedPercentage = projectedPercentage,
            percentageDifference = percentageDifference,
            requiredFor75 = requiredFor75,
            requiredFor80 = requiredFor80,
            maxMissableFor75 = maxMissableFor75,
            target = target
        )
    }

    private fun classesRequiredToReachTarget(present: Int, total: Int, targetPercent: Int): Int {
        if (total == 0) return 0
        val currentPct = (present.toDouble() / total) * 100.0
        if (currentPct >= targetPercent) return 0
        val numerator = targetPercent * total - 100 * present
        val denominator = 100 - targetPercent
        if (denominator <= 0 || numerator <= 0) return 0
        return ceil(numerator.toDouble() / denominator).toInt()
    }

    private fun maxClassesCanMiss(present: Int, total: Int, targetPercent: Int): Int {
        if (total == 0) return 0
        val currentPct = (present.toDouble() / total) * 100.0
        if (currentPct < targetPercent) return 0
        val numerator = 100 * present - targetPercent * total
        if (targetPercent <= 0 || numerator < 0) return 0
        return floor(numerator.toDouble() / targetPercent).toInt()
    }
}
