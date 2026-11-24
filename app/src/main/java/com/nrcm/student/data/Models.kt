package com.nrcm.student.data

import kotlinx.serialization.Serializable

@Serializable
enum class PeriodStatus { PRESENT, ABSENT, NOT_POSTED }

@Serializable
data class DayRecord(val date: String, val dayName: String, val periods: List<PeriodStatus>)

@Serializable
data class MonthRecord(val name: String, val days: List<DayRecord>, val present: Int, val total: Int, val percent: Float, val summaryPresent: Int, val summaryTotal: Int, val matches: Boolean)

@Serializable
data class SemesterSummary(val present: Int, val total: Int, val percent: Float, val countedPresent: Int, val countedTotal: Int, val matches: Boolean)