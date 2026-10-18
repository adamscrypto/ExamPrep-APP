package com.success.examprep.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.success.examprep.data.auth.AuthRepository
import com.success.examprep.data.model.AuthState
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    STUDENT_SIGN_UP,
    STUDENT_SIGN_IN,
    ADMIN_SIGN_IN
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.STUDENT_SIGN_UP,
    // Student Access fields (No email required!)
    val name: String = "",
    val phone: String = "",
    val studentId: String = "",
    val pin: String = "",
    val confirmPin: String = "",
    val targetExam: ExamType = ExamType.JAMB,
    // Admin Login fields (Secure Firebase Email/Password)
    val adminEmail: String = "",
    val adminPassword: String = "",
    // UI controls
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthorizing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val authState: StateFlow<AuthState> = authRepository.authState
    val userSession: StateFlow<UserSession> = authRepository.userSession
    val isAdmin: StateFlow<Boolean> = authRepository.isAdmin

    fun setMode(mode: AuthMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun onStudentIdChange(studentId: String) {
        _uiState.update { it.copy(studentId = studentId, errorMessage = null) }
    }

    fun onPinChange(pin: String) {
        val filtered = pin.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(pin = filtered, errorMessage = null) }
    }

    fun onConfirmPinChange(confirmPin: String) {
        val filtered = confirmPin.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(confirmPin = filtered, errorMessage = null) }
    }

    fun onTargetExamChange(examType: ExamType) {
        _uiState.update { it.copy(targetExam = examType) }
    }

    fun onAdminEmailChange(email: String) {
        _uiState.update { it.copy(adminEmail = email, errorMessage = null) }
    }

    fun onAdminPasswordChange(password: String) {
        _uiState.update { it.copy(adminPassword = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    /**
     * Role-based submit action:
     * - In Student modes: registers or authenticates student and calls [onNavigateToStudent].
     * - In Admin mode: authenticates with Firebase Auth, queries Firestore `admin_roles/{uid}`.
     *   If `active: true` Boolean is confirmed, calls [onNavigateToAdmin].
     *   If role document is missing, inactive, or fails with Firestore error, access is denied and [onNavigateToAdmin] is NEVER called.
     * - Repeated taps or recompositions while loading or authorizing are strictly ignored.
     */
    fun submit(
        onNavigateToStudent: () -> Unit,
        onNavigateToAdmin: () -> Unit
    ) {
        val state = _uiState.value

        if (state.isLoading || state.isAuthorizing) {
            // Prevent duplicate authorization requests or duplicate navigation
            return
        }

        when (state.mode) {
            AuthMode.STUDENT_SIGN_UP -> {
                val name = state.name.trim()
                val phone = state.phone.trim()
                val pin = state.pin.trim()
                val confirmPin = state.confirmPin.trim()

                if (name.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter your full name.") }
                    return
                }
                if (phone.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter your phone number.") }
                    return
                }
                if (pin.length != 4 || !pin.all { it.isDigit() }) {
                    _uiState.update { it.copy(errorMessage = "Please enter a 4-digit PIN.") }
                    return
                }
                if (pin != confirmPin) {
                    _uiState.update { it.copy(errorMessage = "PINs do not match.") }
                    return
                }

                _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
                viewModelScope.launch {
                    val result = authRepository.registerStudent(
                        fullName = name,
                        phone = phone,
                        studentId = state.studentId.trim().ifBlank { null },
                        pin = pin,
                        targetExam = state.targetExam
                    )
                    _uiState.update { it.copy(isLoading = false) }
                    result.fold(
                        onSuccess = {
                            _uiState.update { it.copy(successMessage = "Student profile created successfully!") }
                            onNavigateToStudent()
                        },
                        onFailure = { err ->
                            _uiState.update { it.copy(errorMessage = err.localizedMessage ?: "Failed to create profile.") }
                        }
                    )
                }
            }

            AuthMode.STUDENT_SIGN_IN -> {
                val query = if (state.phone.isNotBlank()) state.phone.trim() else state.studentId.trim()
                val pin = state.pin.trim()

                if (query.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter your Phone Number or Student ID.") }
                    return
                }
                if (pin.length != 4) {
                    _uiState.update { it.copy(errorMessage = "Please enter your 4-digit PIN.") }
                    return
                }

                _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
                viewModelScope.launch {
                    val result = authRepository.loginStudentWithPin(
                        phoneOrStudentId = query,
                        pin = pin
                    )
                    _uiState.update { it.copy(isLoading = false) }
                    result.fold(
                        onSuccess = { student: StudentUser ->
                            _uiState.update { it.copy(successMessage = "Welcome back, ${student.displayName}!") }
                            onNavigateToStudent()
                        },
                        onFailure = { err ->
                            _uiState.update { it.copy(errorMessage = err.localizedMessage ?: "Failed to sign in.") }
                        }
                    )
                }
            }

            AuthMode.ADMIN_SIGN_IN -> {
                val email = state.adminEmail.trim()
                val password = state.adminPassword.trim()

                if (email.isBlank() || password.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter both administrator email and password.") }
                    return
                }

                _uiState.update { it.copy(isLoading = true, isAuthorizing = false, errorMessage = null, successMessage = null) }
                viewModelScope.launch {
                    val result = authRepository.loginAdmin(email = email, password = password)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(isLoading = false, isAuthorizing = true) }
                        val isAuthorisedAdmin = authRepository.isAdmin.value
                        _uiState.update { it.copy(isAuthorizing = false) }

                        if (isAuthorisedAdmin) {
                            _uiState.update { it.copy(successMessage = "Administrator signed in.") }
                            onNavigateToAdmin()
                        } else {
                            _uiState.update {
                                it.copy(
                                    errorMessage = "Administrator authorization denied. Your account does not have an active administrator role in admin_roles."
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthorizing = false,
                                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Admin sign in failed."
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Backward-compatible submit overload that routes according to authenticated administrator role status.
     */
    fun submit(onSuccess: () -> Unit) {
        submit(
            onNavigateToStudent = onSuccess,
            onNavigateToAdmin = onSuccess
        )
    }

    fun sendAdminPasswordReset(email: String, onSent: () -> Unit) {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !trimmed.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid administrator email.") }
            return
        }

        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(trimmed)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "Password reset email sent to $trimmed") }
                    onSent()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.localizedMessage ?: "Failed to send reset email.") }
                }
            )
        }
    }
}
