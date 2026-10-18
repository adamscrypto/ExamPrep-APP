package com.success.examprep

import com.success.examprep.data.auth.LocalAuthStorage
import com.success.examprep.data.auth.StudentPinManager
import com.success.examprep.data.model.DeviceTransferRecord
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.MigrationStatus
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StudentAnonymousAuthArchitectureTest {

    @Test
    fun testPinHashingIsDeterministicAndSalted() {
        val pin1 = "5829"
        val pin2 = "5829"
        val pin3 = "9285"

        val hash1 = LocalAuthStorage.hashPin(pin1)
        val hash2 = LocalAuthStorage.hashPin(pin2)
        val hash3 = LocalAuthStorage.hashPin(pin3)

        assertEquals("Same PIN must produce identical hash", hash1, hash2)
        assertFalse("Different PINs must produce different hashes", hash1 == hash3)
        assertFalse("Hash must not equal plaintext PIN", hash1 == pin1)
        assertEquals("SHA-256 hex string should be 64 characters", 64, hash1.length)
    }

    @Test
    fun testStudentPinManagerRandomSaltAndSecurityValidation() {
        // Validation check for common weak PINs
        assertFalse("0000 must be rejected", StudentPinManager.validatePin("0000").isValid)
        assertFalse("1234 must be rejected", StudentPinManager.validatePin("1234").isValid)
        assertFalse("4321 must be rejected", StudentPinManager.validatePin("4321").isValid)
        assertFalse("1111 must be rejected", StudentPinManager.validatePin("1111").isValid)
        assertFalse("3-digit PIN must be rejected", StudentPinManager.validatePin("123").isValid)
        assertFalse("5-digit PIN must be rejected", StudentPinManager.validatePin("12345").isValid)
        assertFalse("Letters must be rejected", StudentPinManager.validatePin("abcd").isValid)

        assertTrue("Strong 4-digit PIN must be valid", StudentPinManager.validatePin("4829").isValid)
        assertTrue("Strong 4-digit PIN must be valid", StudentPinManager.validatePin("7193").isValid)

        // Randomized salt test
        val salt1 = StudentPinManager.generateSalt()
        val salt2 = StudentPinManager.generateSalt()
        assertFalse("Per-user salts must be random and distinct", salt1 == salt2)

        val hashSalted1 = StudentPinManager.hashWithSalt("4829", salt1)
        val hashSalted2 = StudentPinManager.hashWithSalt("4829", salt2)
        assertFalse("Identical PIN with different salts must produce different hashes", hashSalted1 == hashSalted2)
    }

    @Test
    fun testPhoneNormalization() {
        assertEquals("+2348012345678", StudentPinManager.normalizePhoneNumber("08012345678"))
        assertEquals("+2348012345678", StudentPinManager.normalizePhoneNumber("+2348012345678"))
        assertEquals("+2348012345678", StudentPinManager.normalizePhoneNumber("2348012345678"))
        assertEquals("+2348012345678", StudentPinManager.normalizePhoneNumber("080 1234 5678"))
    }

    @Test
    fun testUserProfileDoesNotContainPasswordFields() {
        val profile = UserProfile(
            uid = "student_test_123",
            fullName = "Amina Bello",
            phone = "08012345678",
            studentId = "ST-2025-001",
            targetExam = ExamType.JAMB.name
        )

        val properties = UserProfile::class.java.declaredFields.map { it.name }
        assertFalse("UserProfile must never contain password field", properties.contains("password"))
        assertFalse("UserProfile must never contain pin field", properties.contains("pin"))
        assertFalse("UserProfile must never contain confirmPassword field", properties.contains("confirmPassword"))

        assertEquals("Amina Bello", profile.fullName)
        assertEquals("08012345678", profile.phone)
        assertEquals("ST-2025-001", profile.studentId)
    }

    @Test
    fun testStudentUserConversionPreservesPhoneAndStudentId() {
        val profile = UserProfile(
            uid = "student_test_456",
            fullName = "Chinedu Okafor",
            email = "", // No email for student
            phone = "08123456789",
            studentId = "ST-WAEC-99",
            targetExam = ExamType.WAEC.name,
            targetYear = 2025
        )

        val studentUser = profile.toStudentUser()
        assertEquals("student_test_456", studentUser.uid)
        assertEquals("Chinedu Okafor", studentUser.displayName)
        assertEquals("", studentUser.email)
        assertEquals("08123456789", studentUser.phone)
        assertEquals("ST-WAEC-99", studentUser.studentId)
        assertEquals(ExamType.WAEC, studentUser.targetExam)
        assertEquals("CO", studentUser.initials)
    }

    @Test
    fun testDeviceTransferRecordDoesNotContainPasswordsOrPins() {
        val record = DeviceTransferRecord(
            id = "trans_123",
            oldStudentUid = "anon_old_999",
            studentName = "Tunde Bakare",
            studentPhone = "+2348098765432",
            studentId = "ST-NECO-44",
            transferToken = "TR-8492",
            status = MigrationStatus.PENDING,
            authorizedByAdmin = "admin@examprep.cbt"
        )

        val properties = DeviceTransferRecord::class.java.declaredFields.map { it.name }
        assertFalse("DeviceTransferRecord must never contain pin field", properties.contains("pin"))
        assertFalse("DeviceTransferRecord must never contain password field", properties.contains("password"))
        assertFalse("DeviceTransferRecord must never contain pinHash field", properties.contains("pinHash"))

        val map = record.toMap()
        assertFalse("Map must not expose pin", map.containsKey("pin"))
        assertFalse("Map must not expose password", map.containsKey("password"))
    }

    @Test
    fun testGoogleServicesAlignmentWithCanonicalPackage() {
        val candidates = listOf(
            File("app/google-services.json"),
            File("../app/google-services.json"),
            File("../../app/google-services.json")
        )
        val file = candidates.firstOrNull { it.exists() }
        assertNotNull("google-services.json must exist in project", file)

        val content = file!!.readText()
        assertTrue("google-services.json must contain com.success.examprep", content.contains("com.success.examprep"))
        assertTrue("google-services.json must contain examprep-ff363", content.contains("examprep-ff363"))
    }

    @Test
    fun testFirestoreRulesPreserveZeroTrustAndAdminSecurity() {
        val candidates = listOf(
            File("firestore.rules"),
            File("../firestore.rules"),
            File("../../firestore.rules")
        )
        val file = candidates.firstOrNull { it.exists() }
        assertNotNull("firestore.rules must exist", file)

        val rules = file!!.readText()
        assertTrue("Must verify request.auth.token.admin == true", rules.contains("request.auth.token.admin == true"))
        assertTrue("Users collection must be protected", rules.contains("match /users/{userId}"))
        assertTrue("Questions collection must be protected", rules.contains("match /questions/{questionId}"))
        assertTrue("Entitlements collection must be protected", rules.contains("match /entitlements/{entitlementId}"))
        assertTrue("CBT History collection must be protected", rules.contains("match /cbtHistory/{historyId}"))
        assertFalse("Must not allow password in user data", !rules.contains("!('password' in request.resource.data)"))
    }
}
