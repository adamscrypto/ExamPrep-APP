package com.success.examprep.data.model

/**
 * Represents the authentication state of the user in ExamPrep CBT.
 * As per core design, GUEST mode is first-class and allows full access to all free CBT tests.
 */
sealed class AuthState {
    data object Guest : AuthState()
    data class Authenticated(val student: StudentUser) : AuthState()

    val isAuthenticated: Boolean
        get() = this is Authenticated

    val currentUser: StudentUser?
        get() = (this as? Authenticated)?.student
}
