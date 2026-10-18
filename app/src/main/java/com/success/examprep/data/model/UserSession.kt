package com.success.examprep.data.model

/**
 * Represents the student user session in ExamPrep CBT.
 * Separates GUEST state from AUTHENTICATED state.
 *
 * CRITICAL RULE:
 * Authenticated state establishes identity only.
 * It does NOT grant full access or paid access.
 */
sealed class UserSession {
    data object Guest : UserSession()

    data class Authenticated(
        val uid: String,
        val user: UserProfile
    ) : UserSession()

    val isGuest: Boolean
        get() = this is Guest

    val isAuthenticated: Boolean
        get() = this is Authenticated

    val currentUserProfile: UserProfile?
        get() = (this as? Authenticated)?.user

    val currentUid: String?
        get() = (this as? Authenticated)?.uid
}
