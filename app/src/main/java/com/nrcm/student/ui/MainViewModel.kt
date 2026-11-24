package com.nrcm.student.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nrcm.student.data.*
import com.nrcm.student.network.LoginResult
import com.nrcm.student.network.PortalClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Error(val message: String) : UiState()
    data class Success(
        val months: List<MonthRecord>,
        val semester: SemesterSummary,
        val isRefreshing: Boolean = false,
        val lastUpdated: Long? = null,
        val refreshError: String? = null,
        val studentName: String? = null
    ) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _ui = MutableStateFlow<UiState>(UiState.Idle)
    val ui: StateFlow<UiState> = _ui
    private val client = PortalClient()

    var lastRollNo: String = ""
    var lastPass: String = ""
    var isRememberMe: Boolean = false

    init {
        // Check for saved Remember Me credentials
        val savedCreds = CacheManager.loadRememberMe(getApplication())
        if (savedCreds != null) {
            lastRollNo = savedCreds.first
            lastPass = savedCreds.second
            isRememberMe = true

            // If Remember Me was on, only load cache if it matches saved roll number
            val cached = CacheManager.loadCache(getApplication())
            if (cached != null && (cached.rollNo == null || cached.rollNo.equals(lastRollNo, ignoreCase = true))) {
                _ui.value = UiState.Success(
                    months = cached.months,
                    semester = cached.semester,
                    lastUpdated = cached.lastUpdated,
                    studentName = cached.studentName
                )
            } else if (cached != null) {
                // Roll number mismatch in cache: clear stale cache immediately
                CacheManager.clearCache(getApplication())
            }
            // Auto login / background refresh with saved credentials
            login(lastRollNo, lastPass, rememberMe = true)
        } else {
            // With "Remember me" off, reopening the app must not show a dashboard that can't refresh.
            // Clear any stale cache and ask the user to log in again.
            CacheManager.clearCache(getApplication())
            _ui.value = UiState.Idle
        }
    }

    fun login(roll: String, pass: String, rememberMe: Boolean = false) {
        val trimmedRoll = roll.trim()
        if (trimmedRoll.isBlank() || pass.isBlank()) return

        val cached = CacheManager.loadCache(getApplication())
        val isDifferentUser = (lastRollNo.isNotBlank() && !lastRollNo.equals(trimmedRoll, ignoreCase = true)) ||
                (cached?.rollNo != null && !cached.rollNo.equals(trimmedRoll, ignoreCase = true))

        if (isDifferentUser) {
            // Delete old cached name and attendance BEFORE showing anything,
            // so the new user can never see the previous user's data, even for a moment.
            CacheManager.clearCache(getApplication())
            CacheManager.clearRememberMe(getApplication())
            _ui.value = UiState.Loading
        }

        lastRollNo = trimmedRoll
        lastPass = pass
        isRememberMe = rememberMe

        // Handle Remember Me storage
        if (rememberMe) {
            CacheManager.saveRememberMe(getApplication(), trimmedRoll, pass)
        } else {
            CacheManager.clearRememberMe(getApplication())
        }

        val currentState = _ui.value
        if (!isDifferentUser && currentState is UiState.Success) {
            // Same user and already showing cached data: perform background refresh
            _ui.value = currentState.copy(isRefreshing = true, refreshError = null)
            viewModelScope.launch(Dispatchers.IO) {
                performFetch(trimmedRoll, pass, isBackground = true)
            }
        } else {
            // Different user or no cache shown: show full loading state
            _ui.value = UiState.Loading
            viewModelScope.launch(Dispatchers.IO) {
                performFetch(trimmedRoll, pass, isBackground = false)
            }
        }
    }

    private suspend fun performFetch(roll: String, pass: String, isBackground: Boolean) {
        when (val result = client.loginAndFetch(roll, pass)) {
            is LoginResult.Success -> {
                val now = System.currentTimeMillis()
                // Name comes freshly from the portal; fallback to existing only if same user
                val existingName = if (isBackground) (_ui.value as? UiState.Success)?.studentName else null
                val finalName = result.studentName ?: existingName

                val cache = CachedAttendance(
                    months = result.months,
                    semester = result.semester,
                    lastUpdated = now,
                    fetchSuccessful = true,
                    studentName = finalName,
                    rollNo = roll
                )
                CacheManager.saveCache(getApplication(), cache)

                _ui.value = UiState.Success(
                    months = result.months,
                    semester = result.semester,
                    isRefreshing = false,
                    lastUpdated = now,
                    refreshError = null,
                    studentName = finalName
                )
            }
            LoginResult.WrongCredentials -> {
                if (isBackground) {
                    val current = _ui.value
                    if (current is UiState.Success) {
                        _ui.value = current.copy(
                            isRefreshing = false,
                            refreshError = "Session expired. Please log in again."
                        )
                    }
                } else {
                    _ui.value = UiState.Error("Wrong roll number or password")
                }
            }
            LoginResult.NetworkError -> {
                if (isBackground) {
                    val current = _ui.value
                    if (current is UiState.Success) {
                        _ui.value = current.copy(
                            isRefreshing = false,
                            refreshError = "Couldn't refresh - showing saved data"
                        )
                    }
                } else {
                    _ui.value = UiState.Error("Network error. Check connection and try again.")
                }
            }
        }
    }

    fun refresh() {
        if (lastRollNo.isNotBlank() && lastPass.isNotBlank()) {
            val currentState = _ui.value
            if (currentState is UiState.Success) {
                _ui.value = currentState.copy(isRefreshing = true, refreshError = null)
            }
            viewModelScope.launch(Dispatchers.IO) {
                performFetch(lastRollNo, lastPass, isBackground = true)
            }
        }
    }

    fun logout() {
        CacheManager.clearRememberMe(getApplication())
        CacheManager.clearCache(getApplication())
        _ui.value = UiState.Idle
        lastRollNo = ""
        lastPass = ""
        isRememberMe = false
    }

    fun reset() {
        _ui.value = UiState.Idle
    }
}
