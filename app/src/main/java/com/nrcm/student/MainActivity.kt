package com.nrcm.student

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nrcm.student.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val vm: MainViewModel = viewModel()
                    val state by vm.ui.collectAsState()

                    when (val s = state) {
                        UiState.Idle -> LoginScreen(
                            onLogin = { r, p, rem -> vm.login(r, p, rem) },
                            error = null,
                            loading = false,
                            rememberMe = vm.isRememberMe,
                            savedRollNo = vm.lastRollNo
                        )
                        UiState.Loading -> LoginScreen(
                            onLogin = { r, p, rem -> vm.login(r, p, rem) },
                            error = null,
                            loading = true,
                            rememberMe = vm.isRememberMe,
                            savedRollNo = vm.lastRollNo
                        )
                        is UiState.Error -> LoginScreen(
                            onLogin = { r, p, rem -> vm.login(r, p, rem) },
                            error = s.message,
                            loading = false,
                            rememberMe = vm.isRememberMe,
                            savedRollNo = vm.lastRollNo
                        )
                        is UiState.Success -> DashboardScreen(
                            months = s.months,
                            semester = s.semester,
                            studentName = s.studentName,
                            onLogout = { vm.logout() },
                            onRefresh = { vm.refresh() },
                            isRefreshing = s.isRefreshing,
                            lastUpdated = s.lastUpdated,
                            refreshError = s.refreshError
                        )
                    }
                }
            }
        }
    }
}
