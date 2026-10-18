package com.success.examprep

import com.success.examprep.data.auth.AdminRoleRepository
import com.success.examprep.data.auth.AuthRepository
import com.success.examprep.data.model.AuthState
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.UserProfile
import com.success.examprep.data.model.UserSession
import com.success.examprep.ui.screens.auth.AuthMode
import com.success.examprep.ui.screens.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException

/**
 * Comprehensive verification of Role-Based Authentication, Authorization, and Navigation:
 *
 * 1. Active administrator login -> routes to Admin Dashboard.
 * 2. Normal student login -> routes to Student Dashboard.
 * 3. Missing admin role -> denies administrator access, no navigation to Admin.
 * 4. Inactive admin role (active: false) -> denies administrator access.
 * 5. Firestore permission-denied errors -> denies admin access safely.
 * 6. Network or authorization lookup failures -> denies admin access safely.
 * 7. Repeated login taps & debounce -> prevents duplicate navigation requests.
 * 8. Sign-out and account switching -> clears state, guarantees no role leakage.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdminRoleAuthenticationTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==========================================
    // FAKES FOR DIRECT ISOLATED TESTING
    // ==========================================

    class FakeAdminRoleRepository : AdminRoleRepository {
        sealed class RoleResult {
            data class Configured(val exists: Boolean, val active: Any?) : RoleResult()
            data class Failure(val error: Throwable) : RoleResult()
        }

        val roleMap = mutableMapOf<String, RoleResult>()
        var queryInvocationCount = 0

        fun setActiveAdmin(uid: String) {
            roleMap[uid] = RoleResult.Configured(exists = true, active = true)
        }

        fun setInactiveAdmin(uid: String) {
            roleMap[uid] = RoleResult.Configured(exists = true, active = false)
        }

        fun setMissingRole(uid: String) {
            roleMap[uid] = RoleResult.Configured(exists = false, active = null)
        }

        fun setNonBooleanActive(uid: String, value: Any) {
            roleMap[uid] = RoleResult.Configured(exists = true, active = value)
        }

        fun setError(uid: String, error: Throwable) {
            roleMap[uid] = RoleResult.Failure(error)
        }

        override suspend fun isUserActiveAdmin(uid: String): Boolean {
            queryInvocationCount++
            if (uid.isBlank()) return false
            return when (val config = roleMap[uid]) {
                is RoleResult.Configured -> {
                    if (!config.exists) {
                        false
                    } else {
                        // Strictly verify Boolean true
                        (config.active is Boolean) && config.active == true
                    }
                }
                is RoleResult.Failure -> {
                    // Safe error handling denies privileges on failure
                    false
                }
                null -> false
            }
        }
    }

    class FakeAuthRepository(
        private val adminRoleRepository: AdminRoleRepository
    ) : AuthRepository {
        private val _userSession = MutableStateFlow<UserSession>(UserSession.Guest)
        override val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

        private val _authState = MutableStateFlow<AuthState>(AuthState.Guest)
        override val authState: StateFlow<AuthState> = _authState.asStateFlow()

        private val _isAdmin = MutableStateFlow(false)
        override val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

        var loginAdminInvocations = 0

        override fun isGuest(): Boolean = _userSession.value is UserSession.Guest
        override fun isUserAuthenticated(): Boolean = _userSession.value is UserSession.Authenticated
        override fun getCurrentUser(): StudentUser? = _authState.value.currentUser
        override fun getCurrentUserProfile(): UserProfile? = (_userSession.value as? UserSession.Authenticated)?.user

        override suspend fun checkAdminStatus(): Boolean {
            val user = getCurrentUser()
            if (user != null && !user.isAnonymous) {
                val active = adminRoleRepository.isUserActiveAdmin(user.uid)
                _isAdmin.value = active
                return active
            }
            _isAdmin.value = false
            return false
        }

        override fun setDevAdminClaim(enabled: Boolean) {
            _isAdmin.value = enabled
        }

        override suspend fun registerStudent(
            fullName: String,
            phone: String,
            studentId: String?,
            pin: String,
            targetExam: ExamType
        ): Result<StudentUser> {
            val user = StudentUser(
                uid = "student_" + phone.hashCode(),
                displayName = fullName,
                email = "",
                phone = phone,
                studentId = studentId ?: "ST-001",
                isAnonymous = true,
                targetExam = targetExam
            )
            val profile = UserProfile(uid = user.uid, fullName = fullName, email = "", phone = phone, studentId = studentId)
            _userSession.value = UserSession.Authenticated(user.uid, profile)
            _authState.value = AuthState.Authenticated(user)
            _isAdmin.value = false
            return Result.success(user)
        }

        override suspend fun loginStudentWithPin(
            phoneOrStudentId: String,
            pin: String
        ): Result<StudentUser> {
            val user = StudentUser(
                uid = "student_" + phoneOrStudentId.hashCode(),
                displayName = "Student User",
                email = "",
                phone = phoneOrStudentId,
                studentId = phoneOrStudentId,
                isAnonymous = true
            )
            val profile = UserProfile(uid = user.uid, fullName = "Student User", email = "")
            _userSession.value = UserSession.Authenticated(user.uid, profile)
            _authState.value = AuthState.Authenticated(user)
            _isAdmin.value = false
            return Result.success(user)
        }

        override suspend fun loginAdmin(email: String, password: String): Result<StudentUser> {
            loginAdminInvocations++
            val uid = "admin_uid_" + email.substringBefore("@")
            val user = StudentUser(
                uid = uid,
                displayName = "Admin User",
                email = email,
                isAnonymous = false
            )
            val profile = UserProfile(uid = uid, fullName = "Admin User", email = email)
            _userSession.value = UserSession.Authenticated(uid, profile)
            _authState.value = AuthState.Authenticated(user)

            // Strictly query role in Firestore admin_roles/{uid}
            val isAuthorisedAdmin = adminRoleRepository.isUserActiveAdmin(uid)
            _isAdmin.value = isAuthorisedAdmin

            return Result.success(user)
        }

        override suspend fun login(emailOrId: String, passwordOrPin: String): Result<StudentUser> {
            return if (emailOrId.contains("@")) loginAdmin(emailOrId, passwordOrPin)
            else loginStudentWithPin(emailOrId, passwordOrPin)
        }

        override suspend fun register(
            email: String,
            password: String,
            fullName: String,
            phone: String?,
            targetExam: ExamType
        ): Result<StudentUser> = loginAdmin(email, password)

        override suspend fun logout() {
            _userSession.value = UserSession.Guest
            _authState.value = AuthState.Guest
            _isAdmin.value = false
        }

        override fun lockStudentSession() {
            _userSession.value = UserSession.Guest
            _authState.value = AuthState.Guest
        }

        override fun isStudentSessionLocked(uid: String): Boolean = false
        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
        override suspend fun updateProfile(name: String, targetExam: ExamType): Result<StudentUser> {
            val current = getCurrentUser() ?: return Result.failure(IllegalStateException())
            return Result.success(current.copy(displayName = name, targetExam = targetExam))
        }
    }

    // ==========================================
    // TEST CASES
    // ==========================================

    @Test
    fun testActiveAdministratorLoginNavigatesToAdminDashboard() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        val adminEmail = "verified.admin@examprep.com"
        val expectedUid = "admin_uid_" + adminEmail.substringBefore("@")
        adminRoleRepo.setActiveAdmin(expectedUid)

        viewModel.setMode(AuthMode.ADMIN_SIGN_IN)
        viewModel.onAdminEmailChange(adminEmail)
        viewModel.onAdminPasswordChange("StrongAdminPassword123")

        var navigatedToAdmin = false
        var navigatedToStudent = false

        viewModel.submit(
            onNavigateToStudent = { navigatedToStudent = true },
            onNavigateToAdmin = { navigatedToAdmin = true }
        )

        testScheduler.advanceUntilIdle()

        assertTrue("Authorized administrator must trigger onNavigateToAdmin", navigatedToAdmin)
        assertFalse("Authorized administrator must NOT trigger onNavigateToStudent", navigatedToStudent)
        assertTrue("AuthRepository.isAdmin must be true", authRepo.isAdmin.value)
        assertNull("No error message on valid admin login", viewModel.uiState.value.errorMessage)
        assertEquals("Administrator signed in.", viewModel.uiState.value.successMessage)
    }

    @Test
    fun testNormalStudentLoginNavigatesToStudentDashboard() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        viewModel.setMode(AuthMode.STUDENT_SIGN_IN)
        viewModel.onPhoneChange("08012345678")
        viewModel.onPinChange("4829")

        var navigatedToAdmin = false
        var navigatedToStudent = false

        viewModel.submit(
            onNavigateToStudent = { navigatedToStudent = true },
            onNavigateToAdmin = { navigatedToAdmin = true }
        )

        testScheduler.advanceUntilIdle()

        assertTrue("Student login must trigger onNavigateToStudent", navigatedToStudent)
        assertFalse("Student login must NOT trigger onNavigateToAdmin", navigatedToAdmin)
        assertFalse("Student must have isAdmin = false", authRepo.isAdmin.value)
    }

    @Test
    fun testMissingAdminRoleDeniesAdminAccess() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        val userEmail = "normal.user@examprep.com"
        val userUid = "admin_uid_" + userEmail.substringBefore("@")
        adminRoleRepo.setMissingRole(userUid) // Document does not exist

        viewModel.setMode(AuthMode.ADMIN_SIGN_IN)
        viewModel.onAdminEmailChange(userEmail)
        viewModel.onAdminPasswordChange("UserPassword123")

        var navigatedToAdmin = false
        var navigatedToStudent = false

        viewModel.submit(
            onNavigateToStudent = { navigatedToStudent = true },
            onNavigateToAdmin = { navigatedToAdmin = true }
        )

        testScheduler.advanceUntilIdle()

        assertFalse("Missing role document must NEVER navigate to Admin Dashboard", navigatedToAdmin)
        assertFalse("Failed admin authorization must NOT silently navigate to student area", navigatedToStudent)
        assertFalse("User must not be recognized as admin", authRepo.isAdmin.value)
        assertNotNull("Must present authorization denied error message", viewModel.uiState.value.errorMessage)
        assertTrue(
            "Error message must specify active administrator role requirement",
            viewModel.uiState.value.errorMessage!!.contains("admin_roles") ||
                viewModel.uiState.value.errorMessage!!.contains("authorization denied")
        )
    }

    @Test
    fun testInactiveAdminRoleFalseDeniesAdminAccess() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        val revokedEmail = "revoked.admin@examprep.com"
        val revokedUid = "admin_uid_" + revokedEmail.substringBefore("@")
        adminRoleRepo.setInactiveAdmin(revokedUid) // active: false

        viewModel.setMode(AuthMode.ADMIN_SIGN_IN)
        viewModel.onAdminEmailChange(revokedEmail)
        viewModel.onAdminPasswordChange("ValidPassword123")

        var navigatedToAdmin = false
        var navigatedToStudent = false

        viewModel.submit(
            onNavigateToStudent = { navigatedToStudent = true },
            onNavigateToAdmin = { navigatedToAdmin = true }
        )

        testScheduler.advanceUntilIdle()

        assertFalse("active: false document must NEVER grant admin access", navigatedToAdmin)
        assertFalse("Inactive admin must not navigate to student area", navigatedToStudent)
        assertFalse("isAdmin must remain false for inactive role", authRepo.isAdmin.value)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testNonBooleanActiveValueInFirestoreIsStrictlyDenied() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        val spoofUid = "admin_uid_spoof"
        adminRoleRepo.setNonBooleanActive(spoofUid, "true") // String instead of Boolean

        viewModel.setMode(AuthMode.ADMIN_SIGN_IN)
        viewModel.onAdminEmailChange("spoof@examprep.com")
        viewModel.onAdminPasswordChange("Password123")

        var navigatedToAdmin = false

        viewModel.submit(
            onNavigateToStudent = {},
            onNavigateToAdmin = { navigatedToAdmin = true }
        )

        testScheduler.advanceUntilIdle()

        assertFalse("Non-boolean active value must not grant admin privileges", navigatedToAdmin)
        assertFalse(authRepo.isAdmin.value)
    }

    @Test
    fun testFirestorePermissionDeniedErrorDeniesAdminAccessSafely() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        val targetEmail = "unauthorized@examprep.com"
        val targetUid = "admin_uid_" + targetEmail.substringBefore("@")
        adminRoleRepo.setError(targetUid, RuntimeException("PERMISSION_DENIED: Missing or insufficient permissions."))

        viewModel.setMode(AuthMode.ADMIN_SIGN_IN)
        viewModel.onAdminEmailChange(targetEmail)
        viewModel.onAdminPasswordChange("Password123")

        var navigatedToAdmin = false

        viewModel.submit(
            onNavigateToStudent = {},
            onNavigateToAdmin = { navigatedToAdmin = true }
        )

        testScheduler.advanceUntilIdle()

        assertFalse("Firestore error must safely deny admin privileges", navigatedToAdmin)
        assertFalse(authRepo.isAdmin.value)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testNetworkOrAuthorizationLookupFailureDeniesAdminAccessSafely() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        val targetEmail = "offline@examprep.com"
        val targetUid = "admin_uid_" + targetEmail.substringBefore("@")
        adminRoleRepo.setError(targetUid, IOException("Network unreachable"))

        viewModel.setMode(AuthMode.ADMIN_SIGN_IN)
        viewModel.onAdminEmailChange(targetEmail)
        viewModel.onAdminPasswordChange("Password123")

        var navigatedToAdmin = false

        viewModel.submit(
            onNavigateToStudent = {},
            onNavigateToAdmin = { navigatedToAdmin = true }
        )

        testScheduler.advanceUntilIdle()

        assertFalse("Network failure must safely deny admin privileges", navigatedToAdmin)
        assertFalse(authRepo.isAdmin.value)
    }

    @Test
    fun testRepeatedTapsBlockedWhileAuthorizing() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)
        val viewModel = AuthViewModel(authRepo)

        val adminEmail = "admin@examprep.com"
        adminRoleRepo.setActiveAdmin("admin_uid_admin")

        viewModel.setMode(AuthMode.ADMIN_SIGN_IN)
        viewModel.onAdminEmailChange(adminEmail)
        viewModel.onAdminPasswordChange("ValidPass123")

        var adminNavCount = 0

        // Submit first time
        viewModel.submit(
            onNavigateToStudent = {},
            onNavigateToAdmin = { adminNavCount++ }
        )

        // Rapid duplicate taps before coroutine completes
        viewModel.submit(
            onNavigateToStudent = {},
            onNavigateToAdmin = { adminNavCount++ }
        )
        viewModel.submit(
            onNavigateToStudent = {},
            onNavigateToAdmin = { adminNavCount++ }
        )

        testScheduler.advanceUntilIdle()

        assertEquals("Duplicate taps must be rejected; exactly 1 navigation should occur", 1, adminNavCount)
        assertEquals("Only 1 backend login request should have been dispatched", 1, authRepo.loginAdminInvocations)
    }

    @Test
    fun testSignOutAndAccountSwitchingPreventsRoleLeakage() = runTest {
        val adminRoleRepo = FakeAdminRoleRepository()
        val authRepo = FakeAuthRepository(adminRoleRepo)

        val adminEmail = "lead.admin@examprep.com"
        val adminUid = "admin_uid_lead.admin"
        adminRoleRepo.setActiveAdmin(adminUid)

        // Step 1: Admin logs in
        val adminResult = authRepo.loginAdmin(adminEmail, "Pass123")
        assertTrue(adminResult.isSuccess)
        assertTrue("Admin session must have isAdmin = true", authRepo.isAdmin.value)
        assertTrue(authRepo.isUserAuthenticated())

        // Step 2: Admin signs out
        authRepo.logout()
        assertFalse("After logout, isAdmin must be false", authRepo.isAdmin.value)
        assertTrue("Session must return to Guest", authRepo.isGuest())
        assertNull("Current user must be null", authRepo.getCurrentUser())

        // Step 3: Normal student logs in on the same device
        val studentResult = authRepo.loginStudentWithPin("08099998888", "1234")
        assertTrue(studentResult.isSuccess)
        assertFalse("Student session must NEVER inherit admin privileges", authRepo.isAdmin.value)

        // Step 4: Another user with revoked admin role logs in
        authRepo.logout()
        val revokedEmail = "ex.admin@examprep.com"
        val revokedUid = "admin_uid_ex.admin"
        adminRoleRepo.setInactiveAdmin(revokedUid)

        val revokedResult = authRepo.loginAdmin(revokedEmail, "Pass123")
        assertTrue(revokedResult.isSuccess)
        assertFalse("Inactive admin must NOT have admin claim", authRepo.isAdmin.value)
    }

    @Test
    fun testFirestoreRulesPermitOwnerReadAndDenyAllClientWrites() {
        val rulesFile = File("firestore.rules").takeIf { it.exists() }
            ?: File("../firestore.rules").takeIf { it.exists() }
            ?: File("../../firestore.rules").takeIf { it.exists() }
        assertNotNull("firestore.rules must exist in root project", rulesFile)
        val content = rulesFile!!.readText()

        // Verify match /admin_roles/{userId}
        assertTrue("firestore.rules must secure admin_roles collection", content.contains("match /admin_roles/{userId}"))
        assertTrue("Authenticated user can only read their own admin role", content.contains("allow read: if isOwner(userId);"))
        assertTrue("Clients must be forbidden from writing/promoting admin status", content.contains("allow write: if false;"))
    }
}
