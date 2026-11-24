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
        val refreshError: String? = null
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

            // If Remember Me was on, show cached attendance immediately if available
            val cached = CacheManager.loadCache(getApplication())
            if (cached != null) {
                _ui.value = UiState.Success(
                    months = cached.months,
                    semester = cached.semester,
                    lastUpdated = cached.lastUpdated
                )
            }
            // Auto login / background refresh with saved credentials
            login(lastRollNo, lastPass, rememberMe = true)
        } else {
            // With "Remember me" off, reopening the app must not show a dashboard that can't refresh.
            // It should ask the user to log in again.
            _ui.value = UiState.Idle
        }
    }

    fun login(roll: String, pass: String, rememberMe: Boolean = false) {
        if (roll.isBlank() || pass.isBlank()) return
        lastRollNo = roll
        lastPass = pass
        isRememberMe = rememberMe

        // Handle Remember Me storage
        if (rememberMe) {
            CacheManager.saveRememberMe(getApplication(), roll, pass)
        } else {
            CacheManager.clearRememberMe(getApplication())
        }

        val currentState = _ui.value
        if (currentState is UiState.Success) {
            // Already showing cached data, perform background refresh
            _ui.value = currentState.copy(isRefreshing = true, refreshError = null)
            viewModelScope.launch(Dispatchers.IO) {
                performFetch(roll, pass, isBackground = true)
            }
        } else {
            // No cache shown, show full loading state
            _ui.value = UiState.Loading
            viewModelScope.launch(Dispatchers.IO) {
                performFetch(roll, pass, isBackground = false)
            }
        }
    }

    private suspend fun performFetch(roll: String, pass: String, isBackground: Boolean) {
        when (val result = client.loginAndFetch(roll, pass)) {
            is LoginResult.Success -> {
                val now = System.currentTimeMillis()
                val cache = CachedAttendance(
                    months = result.months,
                    semester = result.semester,
                    lastUpdated = now,
                    fetchSuccessful = true
                )
                CacheManager.saveCache(getApplication(), cache)

                _ui.value = UiState.Success(
                    months = result.months,
                    semester = result.semester,
                    isRefreshing = false,
                    lastUpdated = now,
                    refreshError = null
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
