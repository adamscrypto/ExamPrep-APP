package com.success.examprep.data.auth

import android.content.Context
import com.success.examprep.data.model.AuthState
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.UserProfile
import com.success.examprep.data.model.UserSession
import com.success.examprep.data.repository.FirestoreUserRepository
import com.success.examprep.data.repository.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Concrete implementation of AuthRepository managing:
 * 1. Firebase Anonymous Authentication + Cloud Firestore user profiles (`users/{uid}`) + device-local PIN for students.
 * 2. Real Firebase Email/Password Authentication for Administrators with verified role in `admin_roles/{uid}`.
 * 3. Guest Mode without authentication barriers.
 * 4. Passwords and raw PINs are NEVER stored in Firestore.
 */
class FirebaseAuthRepository(
    private val context: Context,
    private val localStorage: LocalAuthStorage = LocalAuthStorage.getInstance(context),
    private val userRepository: UserRepository = FirestoreUserRepository.getInstance(context),
    private val adminRoleRepository: AdminRoleRepository = FirestoreAdminRoleRepository.create(context)
) : AuthRepository {

    private val _userSession = MutableStateFlow<UserSession>(UserSession.Guest)
    override val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Guest)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isAdmin = MutableStateFlow<Boolean>(false)
    override val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    init {
        restoreSessionOnStartup()
    }

    private fun restoreSessionOnStartup() {
        val auth = getFirebaseAuth()
        val fbUser = auth?.currentUser

        if (fbUser != null) {
            val cachedProfile = userRepository.getCachedProfile()
            val userProfile = if (cachedProfile != null && cachedProfile.uid == fbUser.uid) {
                cachedProfile
            } else {
                UserProfile(
                    uid = fbUser.uid,
                    fullName = fbUser.displayName ?: localStorage.getActiveUser()?.displayName ?: "Student",
                    email = fbUser.email ?: "",
                    phone = localStorage.getActivePhone(),
                    studentId = localStorage.getActiveStudentId(),
                    accountStatus = "ACTIVE"
                )
            }

            val studentUser = userProfile.toStudentUser().copy(isAnonymous = fbUser.isAnonymous)
            _userSession.value = UserSession.Authenticated(fbUser.uid, userProfile)
            _authState.value = AuthState.Authenticated(studentUser)
            _isAdmin.value = false
            if (!fbUser.isAnonymous) {
                CoroutineScope(Dispatchers.IO).launch {
                    val isActiveAdmin = adminRoleRepository.isUserActiveAdmin(fbUser.uid)
                    _isAdmin.value = isActiveAdmin
                }
            }
        } else {
            // Check offline cache if Firebase is offline
            val cachedUser = localStorage.getActiveUser()
            if (cachedUser != null) {
                val profile = UserProfile(
                    uid = cachedUser.uid,
                    fullName = cachedUser.displayName,
                    email = cachedUser.email,
                    phone = cachedUser.phone,
                    studentId = cachedUser.studentId,
                    targetExam = cachedUser.targetExam.name,
                    targetYear = cachedUser.targetYear
                )
                _userSession.value = UserSession.Authenticated(cachedUser.uid, profile)
                _authState.value = AuthState.Authenticated(cachedUser)
                _isAdmin.value = false
            } else {
                _userSession.value = UserSession.Guest
                _authState.value = AuthState.Guest
                _isAdmin.value = false
            }
        }
    }

    override fun isGuest(): Boolean {
        return _userSession.value is UserSession.Guest
    }

    override fun isUserAuthenticated(): Boolean {
        return _userSession.value is UserSession.Authenticated
    }

    override fun getCurrentUser(): StudentUser? {
        val session = _userSession.value
        return if (session is UserSession.Authenticated) {
            session.user.toStudentUser()
        } else {
            _authState.value.currentUser
        }
    }

    override fun getCurrentUserProfile(): UserProfile? {
        val session = _userSession.value
        return (session as? UserSession.Authenticated)?.user
    }

    override suspend fun checkAdminStatus(): Boolean = withContext(Dispatchers.IO) {
        val fbUser = getFirebaseAuth()?.currentUser
        if (fbUser != null && !fbUser.isAnonymous) {
            val uid = fbUser.uid
            val isActiveAdmin = adminRoleRepository.isUserActiveAdmin(uid)
            _isAdmin.value = isActiveAdmin
            return@withContext isActiveAdmin
        }
        val currentUser = getCurrentUser()
        if (currentUser != null && !currentUser.isAnonymous) {
            val isActiveAdmin = adminRoleRepository.isUserActiveAdmin(currentUser.uid)
            _isAdmin.value = isActiveAdmin
            return@withContext isActiveAdmin
        }
        _isAdmin.value = false
        false
    }

    override fun setDevAdminClaim(enabled: Boolean) {
        _isAdmin.value = enabled
    }

    // ==========================================
    // Student Registration: Firebase Anonymous + Profile + Local PIN
    // ==========================================

    override suspend fun registerStudent(
        fullName: String,
        phone: String,
        studentId: String?,
        pin: String,
        targetExam: ExamType
    ): Result<StudentUser> = withContext(Dispatchers.IO) {
        val trimmedName = fullName.trim()
        val normalizedPhone = StudentPinManager.normalizePhoneNumber(phone.trim())
        val trimmedPin = pin.trim()
        val normalizedStudentId = studentId?.let { StudentPinManager.normalizeStudentId(it).ifBlank { null } }

        if (trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your full name."))
        }
        if (normalizedPhone.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your phone number."))
        }

        val pinValidation = StudentPinManager.validatePin(trimmedPin)
        if (!pinValidation.isValid) {
            val reason = (pinValidation as? PinValidationResult.Invalid)?.reason ?: "Invalid 4-digit PIN."
            return@withContext Result.failure(IllegalArgumentException(reason))
        }

        val auth = getFirebaseAuth()
        var uid: String

        if (auth != null) {
            try {
                // If there's already an active anonymous user, reuse it; otherwise create anonymous session
                val currentFbUser = auth.currentUser
                val fbUser = if (currentFbUser != null && currentFbUser.isAnonymous) {
                    currentFbUser
                } else {
                    if (currentFbUser != null) auth.signOut()
                    val authResult = auth.signInAnonymously().await()
                    authResult.user ?: throw IllegalStateException("Failed to initialize Firebase student session.")
                }

                uid = fbUser.uid

                // Update display name on Firebase User
                try {
                    val updateProfileReq = UserProfileChangeRequest.Builder()
                        .setDisplayName(trimmedName)
                        .build()
                    fbUser.updateProfile(updateProfileReq).await()
                } catch (_: Exception) { }

                // Create Cloud Firestore profile in `users/{uid}`
                val profile = UserProfile(
                    uid = uid,
                    fullName = trimmedName,
                    email = "", // No email required for students
                    phone = normalizedPhone,
                    studentId = normalizedStudentId,
                    accountStatus = "ACTIVE",
                    targetExam = targetExam.name,
                    targetYear = 2025
                )

                userRepository.createUserProfile(profile)
                localStorage.setStudentPin(uid, trimmedPin)

                val studentUser = StudentUser(
                    uid = uid,
                    displayName = trimmedName,
                    email = "",
                    phone = normalizedPhone,
                    studentId = normalizedStudentId,
                    targetExam = targetExam,
                    targetYear = 2025,
                    isAnonymous = true
                )

                localStorage.saveActiveUser(studentUser, phone = normalizedPhone, studentId = normalizedStudentId, isAnonymous = true)
                localStorage.registerLocalAccount(studentUser, phone = normalizedPhone, studentId = normalizedStudentId)

                _userSession.value = UserSession.Authenticated(uid, profile)
                _authState.value = AuthState.Authenticated(studentUser)
                _isAdmin.value = false

                return@withContext Result.success(studentUser)
            } catch (e: Exception) {
                // Offline fallback
                uid = "offline_student_" + UUID.randomUUID().toString().take(8)
            }
        } else {
            uid = "student_" + UUID.randomUUID().toString().take(8)
        }

        // Offline / Local storage fallback
        val fallbackStudent = StudentUser(
            uid = uid,
            displayName = trimmedName,
            email = "",
            phone = normalizedPhone,
            studentId = normalizedStudentId,
            targetExam = targetExam,
            targetYear = 2025,
            isAnonymous = true
        )
        val fallbackProfile = UserProfile(
            uid = uid,
            fullName = trimmedName,
            email = "",
            phone = normalizedPhone,
            studentId = normalizedStudentId,
            accountStatus = "ACTIVE",
            targetExam = targetExam.name,
            targetYear = 2025
        )

        localStorage.setStudentPin(uid, trimmedPin)
        localStorage.saveActiveUser(fallbackStudent, phone = normalizedPhone, studentId = normalizedStudentId, isAnonymous = true)
        localStorage.registerLocalAccount(fallbackStudent, phone = normalizedPhone, studentId = normalizedStudentId)
        userRepository.saveCachedProfile(fallbackProfile)

        _userSession.value = UserSession.Authenticated(uid, fallbackProfile)
        _authState.value = AuthState.Authenticated(fallbackStudent)
        _isAdmin.value = false

        Result.success(fallbackStudent)
    }

    override suspend fun loginStudentWithPin(
        phoneOrStudentId: String,
        pin: String
    ): Result<StudentUser> = withContext(Dispatchers.IO) {
        val query = phoneOrStudentId.trim()
        val trimmedPin = pin.trim()

        if (query.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your Phone Number or Student ID."))
        }
        if (trimmedPin.length != 4 || !trimmedPin.all { it.isDigit() }) {
            return@withContext Result.failure(IllegalArgumentException("PIN must be exactly 4 digits."))
        }

        // Search in local registry on this device
        val localStudent = localStorage.findStudentByPhoneOrId(query)
            ?: localStorage.getActiveUser()?.takeIf {
                it.phone.equals(query, ignoreCase = true) || it.studentId.equals(query, ignoreCase = true)
            }

        if (localStudent == null) {
            return@withContext Result.failure(
                IllegalArgumentException("No profile found with this Phone or Student ID on this device. If you changed phones, contact an ExamPrep Administrator for device transfer.")
            )
        }

        // Verify device-local PIN hash
        val pinValid = localStorage.verifyStudentPin(localStudent.uid, trimmedPin)
        if (!pinValid) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect 4-digit PIN. Please try again."))
        }

        val auth = getFirebaseAuth()
        if (auth != null && auth.currentUser == null) {
            try {
                auth.signInAnonymously().await()
            } catch (_: Exception) {}
        }

        val profile = UserProfile(
            uid = localStudent.uid,
            fullName = localStudent.displayName,
            email = localStudent.email,
            phone = localStudent.phone,
            studentId = localStudent.studentId,
            targetExam = localStudent.targetExam.name,
            targetYear = localStudent.targetYear
        )

        localStorage.saveActiveUser(localStudent, phone = localStudent.phone, studentId = localStudent.studentId, isAnonymous = true)
        userRepository.saveCachedProfile(profile)

        _userSession.value = UserSession.Authenticated(localStudent.uid, profile)
        _authState.value = AuthState.Authenticated(localStudent)
        _isAdmin.value = false

        Result.success(localStudent)
    }

    // ==========================================
    // Administrator Authentication: Firebase Email/Password
    // ==========================================

    override suspend fun loginAdmin(email: String, password: String): Result<StudentUser> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter both administrator email and password."))
        }

        val auth = getFirebaseAuth()
        if (auth != null) {
            try {
                val authResult = auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                val fbUser = authResult.user ?: throw IllegalStateException("Firebase returned empty session.")
                val uid = fbUser.uid

                val profileResult = userRepository.getUserProfile(uid)
                val profile = profileResult.getOrNull() ?: UserProfile(
                    uid = uid,
                    fullName = fbUser.displayName ?: trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = fbUser.email ?: trimmedEmail,
                    accountStatus = "ACTIVE"
                )

                val studentUser = profile.toStudentUser().copy(isAnonymous = false)
                localStorage.saveActiveUser(studentUser, isAnonymous = false)
                userRepository.saveCachedProfile(profile)

                _userSession.value = UserSession.Authenticated(uid, profile)
                _authState.value = AuthState.Authenticated(studentUser)

                // Authorize role by checking Firestore document admin_roles/{uid}
                val isActiveAdmin = adminRoleRepository.isUserActiveAdmin(uid)
                _isAdmin.value = isActiveAdmin

                return@withContext Result.success(studentUser)
            } catch (e: Exception) {
                _isAdmin.value = false
                return@withContext Result.failure(Exception(getFriendlyFirebaseErrorMessage(e, isRegistering = false)))
            }
        } else {
            // Offline admin login fallback for test environments
            val uid = "admin_" + trimmedEmail.hashCode()
            val studentUser = StudentUser(
                uid = uid,
                displayName = "Administrator",
                email = trimmedEmail,
                isAnonymous = false
            )
            val profile = UserProfile(uid = uid, fullName = "Administrator", email = trimmedEmail)
            localStorage.saveActiveUser(studentUser, isAnonymous = false)
            userRepository.saveCachedProfile(profile)

            _userSession.value = UserSession.Authenticated(uid, profile)
            _authState.value = AuthState.Authenticated(studentUser)

            val isActiveAdmin = adminRoleRepository.isUserActiveAdmin(uid)
            _isAdmin.value = isActiveAdmin

            Result.success(studentUser)
        }
    }

    override suspend fun login(emailOrId: String, passwordOrPin: String): Result<StudentUser> {
        val trimmed = emailOrId.trim()
        return if (trimmed.contains("@")) {
            loginAdmin(trimmed, passwordOrPin)
        } else {
            loginStudentWithPin(trimmed, passwordOrPin)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String?,
        targetExam: ExamType
    ): Result<StudentUser> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedName = fullName.trim()

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            // Student Registration path without email
            return@withContext registerStudent(
                fullName = trimmedName,
                phone = phone ?: "08000000000",
                studentId = null,
                pin = if (trimmedPassword.length == 4 && trimmedPassword.all { it.isDigit() }) trimmedPassword else "1234",
                targetExam = targetExam
            )
        }

        // Administrator / Email registration path
        val auth = getFirebaseAuth()
        if (auth != null) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                val fbUser = authResult.user ?: throw IllegalStateException("Firebase returned empty session.")
                val uid = fbUser.uid

                val profile = UserProfile(
                    uid = uid,
                    fullName = trimmedName.ifBlank { trimmedEmail.substringBefore("@") },
                    email = trimmedEmail,
                    phone = phone,
                    accountStatus = "ACTIVE",
                    targetExam = targetExam.name,
                    targetYear = 2025
                )

                userRepository.createUserProfile(profile)
                val studentUser = profile.toStudentUser()
                localStorage.saveActiveUser(studentUser, phone = phone, isAnonymous = false)
                localStorage.registerLocalAccount(studentUser, phone = phone)

                _userSession.value = UserSession.Authenticated(uid, profile)
                _authState.value = AuthState.Authenticated(studentUser)
                _isAdmin.value = false

                return@withContext Result.success(studentUser)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception(getFriendlyFirebaseErrorMessage(e, isRegistering = true)))
            }
        } else {
            val uid = "user_" + UUID.randomUUID().toString().take(8)
            val studentUser = StudentUser(
                uid = uid,
                displayName = trimmedName,
                email = trimmedEmail,
                targetExam = targetExam
            )
            val profile = UserProfile(
                uid = uid,
                fullName = trimmedName,
                email = trimmedEmail,
                phone = phone,
                targetExam = targetExam.name
            )
            localStorage.saveActiveUser(studentUser, phone = phone, isAnonymous = false)
            localStorage.registerLocalAccount(studentUser, phone = phone)
            userRepository.saveCachedProfile(profile)

            _userSession.value = UserSession.Authenticated(uid, profile)
            _authState.value = AuthState.Authenticated(studentUser)

            Result.success(studentUser)
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        val auth = getFirebaseAuth()
        try {
            auth?.signOut()
        } catch (_: Exception) {}

        localStorage.clearActiveUser()
        userRepository.clearCachedProfile()

        _userSession.value = UserSession.Guest
        _authState.value = AuthState.Guest
        _isAdmin.value = false
    }

    override fun lockStudentSession() {
        val user = getCurrentUser()
        if (user != null) {
            localStorage.lockStudentSession(user.uid)
        }
        _userSession.value = UserSession.Guest
        _authState.value = AuthState.Guest
    }

    override fun isStudentSessionLocked(uid: String): Boolean {
        return localStorage.isStudentSessionLocked(uid)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid administrator email address."))
        }

        val auth = getFirebaseAuth()
        if (auth != null) {
            try {
                auth.sendPasswordResetEmail(trimmedEmail).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception(getFriendlyFirebaseErrorMessage(e, isRegistering = false)))
            }
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun updateProfile(name: String, targetExam: ExamType): Result<StudentUser> = withContext(Dispatchers.IO) {
        val currentUser = getCurrentUser() ?: return@withContext Result.failure(IllegalStateException("No active user session."))
        val uid = currentUser.uid

        val updates = mapOf(
            "fullName" to name.trim(),
            "targetExam" to targetExam.name
        )
        userRepository.updateUserProfile(uid, updates)

        val updatedUser = currentUser.copy(
            displayName = name.trim(),
            targetExam = targetExam
        )
        localStorage.saveActiveUser(updatedUser, isAnonymous = currentUser.isAnonymous)

        val cachedProfile = userRepository.getCachedProfile()
        if (cachedProfile != null) {
            val updatedProfile = cachedProfile.copy(fullName = name.trim(), targetExam = targetExam.name)
            userRepository.saveCachedProfile(updatedProfile)
            _userSession.value = UserSession.Authenticated(uid, updatedProfile)
        }

        _authState.value = AuthState.Authenticated(updatedUser)
        Result.success(updatedUser)
    }

    private fun getFriendlyFirebaseErrorMessage(e: Exception, isRegistering: Boolean): String {
        return when (e) {
            is FirebaseAuthUserCollisionException ->
                "An account already exists with this email address. Try logging in instead."
            is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
                if (isRegistering) "Please provide valid credentials." else "Invalid credentials. Please verify and try again."
            is FirebaseAuthWeakPasswordException ->
                "Password must be at least 8 characters."
            is FirebaseNetworkException ->
                "Unable to connect to server. Please check your internet connection."
            is FirebaseTooManyRequestsException ->
                "Too many attempts. Please wait a few moments and try again."
            else ->
                e.localizedMessage ?: if (isRegistering) "Failed to create account. Please try again." else "Sign in failed. Please check your credentials."
        }
    }

    private fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseAuthRepository? = null

        fun getInstance(context: Context): FirebaseAuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAuthRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun createForTesting(
            context: Context,
            localStorage: LocalAuthStorage = LocalAuthStorage.getInstance(context),
            userRepository: UserRepository = FirestoreUserRepository.getInstance(context),
            adminRoleRepository: AdminRoleRepository = FirestoreAdminRoleRepository.create(context)
        ): FirebaseAuthRepository {
            return FirebaseAuthRepository(
                context = context,
                localStorage = localStorage,
                userRepository = userRepository,
                adminRoleRepository = adminRoleRepository
            )
        }

        fun resetInstanceForTesting() {
            INSTANCE = null
        }
    }
}
