package com.nrcm.student

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PlannerLogicTest {

    @Test
    fun testMustAttend() {
        // Requirement: mustAttend(106, 156, 0.75) == 44
        assertEquals(44, mustAttend(106, 156, 0.75f))
    }

    @Test
    fun testCanSkip() {
        // Requirement: canSkip(409, 531, 0.75) == 14
        assertEquals(14, canSkip(409, 531, 0.75f))
    }

    @Test
    fun testStatusAtTarget75() {
        // Requirement: Status at target 75 for 67.95, 75.0, 77.02 and 80.0 is
        // BELOW, CLOSE, CLOSE and SAFE.
        assertEquals(Status.BELOW, determineStatus(67.95f, 75f))
        assertEquals(Status.CLOSE, determineStatus(75.0f, 75f))
        assertEquals(Status.CLOSE, determineStatus(77.02f, 75f))
        assertEquals(Status.SAFE, determineStatus(80.0f, 75f))
    }

    @Test
    fun testAfterAttendingNClassesReachTargetForBelowCases() {
        // Requirement: After attending N classes, (present + N) / (total + N) >= 0.75 for any BELOW case.
        val testCases = listOf(
            106 to 156,
            39 to 60,
            68 to 96,
            10 to 20,
            1 to 10,
            0 to 50,
            50 to 100,
            200 to 300,
            400 to 600
        )

        for ((present, total) in testCases) {
            val target = 0.75f
            val currentPercent = calculatePercent(present, total)
            if (currentPercent < 75f) {
                val n = mustAttend(present, total, target)
                assertTrue("N must be >= 0 for ($present, $total)", n >= 0)
                val newPresent = present + n
                val newTotal = total + n
                val newRatio = newPresent.toDouble() / newTotal.toDouble()
                assertTrue(
                    "After attending $n classes for ($present, $total), new ratio $newRatio should be >= 0.75",
                    newRatio >= 0.75
                )
            }
        }
    }

    @Test
    fun testEdgeCasesTotalZero() {
        // Requirement: Edge cases: if total == 0 show 0.00% and no planner sentence.
        assertEquals(0f, calculatePercent(0, 0), 0.0001f)
        assertEquals(0, canSkip(0, 0, 0.75f))
        assertEquals(0, mustAttend(0, 0, 0.75f))
    }

    @Test
    fun testWhatIfSlider() {
        val pct = whatIf(409, 531, 0)
        assertEquals(77.024f, pct, 0.01f)
        val pctAfterSkip = whatIf(409, 531, 14)
        assertTrue("At 14 skipped classes, attendance should still be >= 75%", pctAfterSkip >= 75f)
        val pctAfterSkip15 = whatIf(409, 531, 15)
        assertTrue("At 15 skipped classes, attendance should drop below 75%", pctAfterSkip15 < 75f)
    }

    @Test
    fun testWorkingDaysAndEstimateDate() {
        assertEquals(8, estimateWorkingDays(44))
        assertEquals(1, estimateWorkingDays(6))
        assertEquals(2, estimateWorkingDays(7))
        assertEquals(0, estimateWorkingDays(0))

        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.NOVEMBER, 24) // Tuesday
        val est = estimateTargetDate(8, cal)
        assertTrue(est.isNotBlank())
    }
}
