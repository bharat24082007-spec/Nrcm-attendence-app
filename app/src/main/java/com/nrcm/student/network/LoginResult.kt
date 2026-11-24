package com.nrcm.student.network

import com.nrcm.student.data.*

sealed class LoginResult {
    data class Success(val months: List<MonthRecord>, val semester: SemesterSummary) : LoginResult()
    object WrongCredentials : LoginResult()
    object NetworkError : LoginResult()
}