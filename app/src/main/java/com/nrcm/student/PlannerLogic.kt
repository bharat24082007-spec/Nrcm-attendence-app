package com.nrcm.student

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

// Pure planning logic — no Android dependencies, easy to unit test

/// Current attendance percentage and status
data class PlannerStatus(
    val percent: Float,
    val status: Status,
    val canSkip: Int,
    val mustAttend: Int,
    val daysNeeded: Int,
    val estimatedDate: String,
    val afterAttendCanSkip: Int,
    val targetPercent: Int
)

enum class Status { SAFE, CLOSE, BELOW }

/// Calculate current percentage: present * 100 / total
/// Returns 0 if total is 0 to avoid division by zero
fun calculatePercent(present: Int, total: Int): Float {
    return if (total <= 0) 0f else present.toFloat() * 100f / total.toFloat()
}

/// canSkip: minimum classes student can skip and still stay above target
/// floor(present / target - total), minimum 0
fun canSkip(present: Int, total: Int, target: Float): Int {
    val t = if (target > 1f) target / 100f else target
    if (t <= 0f || t >= 1f) return 0
    val raw = present.toDouble() / t.toDouble() - total.toDouble()
    return max(0, floor(raw).toInt())
}

/// mustAttend: classes student must attend to reach target
/// ceil((target * total - present) / (1 - target)), minimum 0
fun mustAttend(present: Int, total: Int, target: Float): Int {
    val t = if (target > 1f) target / 100f else target
    if (t <= 0f || t >= 1f) return 0
    val numerator = t.toDouble() * total.toDouble() - present.toDouble()
    val denominator = 1.0 - t.toDouble()
    if (denominator <= 0.0) return 0
    val raw = numerator / denominator
    return max(0, ceil(raw).toInt())
}

/// whatIf: percentage if N more classes are skipped
/// present * 100 / (total + skipN)
fun whatIf(present: Int, total: Int, skipN: Int): Float {
    val newTotal = total + skipN
    return if (newTotal <= 0) 0f else present.toFloat() * 100f / newTotal.toFloat()
}

/// Determine status comparing exact percent against target
/// SAFE:   percent >= target + 3
/// CLOSE:  target <= percent < target + 3
/// BELOW:  percent < target
/// Decided from percent only, never from canSkip
fun determineStatus(percent: Float, targetPercent: Float): Status {
    val target = if (targetPercent <= 1f && targetPercent > 0f) targetPercent * 100f else targetPercent
    return when {
        percent >= target + 3f -> Status.SAFE
        percent >= target -> Status.CLOSE
        else -> Status.BELOW
    }
}

/// Estimate days needed assuming 6 classes a day: ceil(classes / 6)
fun estimateWorkingDays(neededClasses: Int): Int {
    return max(0, ceil(neededClasses.toDouble() / 6.0).toInt())
}

/// Estimate date reached by counting Monday to Saturday starting from next working day
fun estimateTargetDate(
    workingDays: Int,
    fromCalendar: Calendar = Calendar.getInstance()
): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    if (workingDays <= 0) {
        return sdf.format(fromCalendar.time)
    }
    val cal = fromCalendar.clone() as Calendar
    // Advance to next day
    cal.add(Calendar.DAY_OF_MONTH, 1)
    // If that day is Sunday, advance to Monday (next working day)
    if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    // Now cal is at working day 1. We need (workingDays - 1) additional working days.
    var remaining = workingDays - 1
    while (remaining > 0) {
        cal.add(Calendar.DAY_OF_MONTH, 1)
        if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            remaining--
        }
    }
    return sdf.format(cal.time)
}

/// Build PlannerStatus from present/total counts and target percentage (e.g. 75 for 75%)
fun buildPlannerStatus(
    present: Int,
    total: Int,
    targetPercent: Int, // e.g. 75 for 75%
    fromCalendar: Calendar = Calendar.getInstance()
): PlannerStatus {
    val safeTargetPercent = if (targetPercent >= 100) 90 else targetPercent
    val targetFraction = safeTargetPercent.toFloat() / 100f
    val percent = calculatePercent(present, total)
    val status = determineStatus(percent, safeTargetPercent.toFloat())
    val canS = canSkip(present, total, targetFraction)
    val mustA = mustAttend(present, total, targetFraction)
    val days = estimateWorkingDays(mustA)
    val estDate = estimateTargetDate(days, fromCalendar)
    val afterSkip = canSkip(present + mustA, total + mustA, targetFraction)

    return PlannerStatus(
        percent = percent,
        status = status,
        canSkip = canS,
        mustAttend = mustA,
        daysNeeded = days,
        estimatedDate = estDate,
        afterAttendCanSkip = afterSkip,
        targetPercent = safeTargetPercent
    )
}
