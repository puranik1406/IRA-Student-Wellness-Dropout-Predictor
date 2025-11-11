package com.example.ira.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ira.data.local.entities.Counselor
import com.example.ira.data.local.entities.Student
import com.example.ira.data.repository.IRARepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class StudentLoginSuccess(val student: Student) : AuthState()
    data class CounselorLoginSuccess(val counselor: Counselor) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: IRARepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun loginStudent(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val student = repository.loginStudent(email, password)
                if (student != null) {
                    _authState.value = AuthState.StudentLoginSuccess(student)
                } else {
                    _authState.value = AuthState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun loginCounselor(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val counselor = repository.loginCounselor(email, password)
                if (counselor != null) {
                    _authState.value = AuthState.CounselorLoginSuccess(counselor)
                } else {
                    _authState.value = AuthState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun registerStudent(student: Student) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val studentId = repository.registerStudent(student)
                val registeredStudent = student.copy(id = studentId)
                _authState.value = AuthState.StudentLoginSuccess(registeredStudent)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun registerCounselor(counselor: Counselor) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val counselorId = repository.registerCounselor(counselor)
                val registeredCounselor = counselor.copy(id = counselorId)
                _authState.value = AuthState.CounselorLoginSuccess(registeredCounselor)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
