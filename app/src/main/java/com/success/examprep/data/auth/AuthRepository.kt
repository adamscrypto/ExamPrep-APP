package com.success.examprep.data.auth

import com.success.examprep.data.model.AuthState
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.UserProfile
import com.success.examprep.data.model.UserSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Authentication repository interface for ExamPrep CBT.
 *
 * Core Architecture:
 * 1. Guest Mode: immediate CBT access without signing up or signing in.
 * 2. Student Access: Firebase Anonymous Authentication + Cloud Firestore User Profile (`users/{uid}`)
 *    + device-local hashed 4-digit PIN for device security. Email is NOT required.
 * 3. Administrator Access: Secure Firebase Email/Password Authentication with verified custom claim `admin: true`.
 * 4. Admin-Assisted Account Recovery / Transfer: Administrators can verify and restore student profiles across devices.
 * 5. Passwords and raw PINs are NEVER stored in Firestore or plaintext.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>
    val userSession: StateFlow<UserSession>
    val isAdmin: StateFlow<Boolean>

    fun isGuest(): Boolean
    fun isUserAuthenticated(): Boolean
    fun getCurrentUser(): StudentUser?
    fun getCurrentUserProfile(): UserProfile?

    fun observeAuthState(): StateFlow<AuthState> = authState
    fun observeUserSession(): StateFlow<UserSession> = userSession

    suspend fun checkAdminStatus(): Boolean

    // ==========================================
    // Student Authentication (Anonymous Auth + Profile + Local PIN)
    // ==========================================

    /**
     * Registers a student identity by creating/reusing a Firebase Anonymous user,
     * writing the student's profile to Firestore `users/{uid}`, and saving a salted PIN hash locally.
     */
    suspend fun registerStudent(
        fullName: String,
        phone: String,
        studentId: String? = null,
        pin: String,
        targetExam: ExamType = ExamType.JAMB
    ): Result<StudentUser>

    /**
     * Unlocks an existing student profile on this device by checking the local hashed PIN.
     */
    suspend fun loginStudentWithPin(
        phoneOrStudentId: String,
        pin: String
    ): Result<StudentUser>

    // ==========================================
    // Administrator Authentication (Firebase Email/Password)
    // ==========================================

    /**
     * Authenticates an administrator with Firebase Email and Password.
     */
    suspend fun loginAdmin(email: String, password: String): Result<StudentUser>

    /**
     * Generic login method - delegates to admin login when email-formatted, or student lookup otherwise.
     */
    suspend fun login(emailOrId: String, passwordOrPin: String): Result<StudentUser>

    /**
     * Registers an account. If email provided, uses Firebase Email/Password; otherwise registers student profile.
     */
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String? = null,
        targetExam: ExamType = ExamType.JAMB
    ): Result<StudentUser>

    /**
     * Signs out current user session from Firebase Auth and restores Guest session.
     */
    suspend fun logout()

    /**
     * Locks the student's active session on this device requiring 4-digit PIN unlock.
     * Does NOT delete or wipe the account.
     */
    fun lockStudentSession()

    /**
     * Checks if a student's session is currently locked on this device.
     */
    fun isStudentSessionLocked(uid: String): Boolean

    /**
     * Dispatches password reset email for administrators via Firebase Auth.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun updateProfile(name: String, targetExam: ExamType): Result<StudentUser>

    fun setDevAdminClaim(enabled: Boolean)

    // Backwards compatibility aliases
    suspend fun signIn(email: String, password: String): Result<StudentUser> = login(email, password)
    suspend fun signUp(name: String, email: String, password: String, targetExam: ExamType): Result<StudentUser> =
        register(email, password, name, null, targetExam)
    suspend fun signOut() = logout()
    suspend fun sendPasswordReset(email: String): Result<Unit> = sendPasswordResetEmail(email)
}
