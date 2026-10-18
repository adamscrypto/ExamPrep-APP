package com.success.examprep

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.success.examprep.data.access.AccessConfig
import com.success.examprep.data.access.LocalAccessManager
import com.success.examprep.data.access.QuestionAccessState
import com.success.examprep.data.auth.LocalAuthStorage
import com.success.examprep.data.model.AccessEntitlement
import com.success.examprep.data.model.CbtResult
import com.success.examprep.data.model.EntitlementStatus
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.Subject
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.repository.DefaultEntitlementRepository
import com.success.examprep.data.repository.DefaultStudentRepository
import com.success.examprep.data.repository.LocalTestHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Stage 1 Verification Suite:
 * - Exam Entitlements Logic & Model
 * - JAMB 4-Subject Combination Validation (Compulsory English + 3 Choices)
 * - WAEC & NECO All-Configured-Subjects Full Access
 * - Single Operation Grant Consistency & Absence of Duplicates
 * - Multi-Exam Access Preservation
 * - Preserving Historical CBT Results when JAMB Combination Changes
 * - 20-Question Free Access Enforcement
 * - Authoritative Entitlement Access Control
 * - Student Search by Name, Phone, and Student ID
 * - Revocation Workflow
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExamEntitlementsAndAdminGrantTest {

    private lateinit var context: Context
    private lateinit var entitlementRepository: DefaultEntitlementRepository
    private lateinit var historyRepository: LocalTestHistoryRepository
    private lateinit var studentRepository: DefaultStudentRepository
    private lateinit var accessManager: LocalAccessManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear test preferences for clean isolation
        context.getSharedPreferences("examprep_entitlements_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("examprep_history_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("examprep_local_auth_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        entitlementRepository = DefaultEntitlementRepository(context)
        historyRepository = LocalTestHistoryRepository(context)
        studentRepository = DefaultStudentRepository(context, LocalAuthStorage.getInstance(context))
        accessManager = LocalAccessManager.getInstance()
        accessManager.resetAllAccess()
    }

    // ==========================================
    // 1. JAMB 4-SUBJECT COMBINATION VALIDATION
    // ==========================================

    @Test
    fun testJambGrantRequiresExactlyFourSubjectsIncludingEnglish() = runTest {
        val studentUid = "student_jamb_001"

        // Attempt 1: Only 3 subjects (Fails)
        val failThreeSubjects = entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "student@jamb.ng",
            studentName = "Adaeze Okafor",
            examType = ExamType.JAMB,
            subjects = listOf(SubjectType.ENGLISH, SubjectType.MATHEMATICS, SubjectType.PHYSICS),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt",
            reason = "Test 3 subjects"
        )
        assertTrue("Granting only 3 subjects for JAMB must fail", failThreeSubjects.isFailure)
        assertTrue(
            failThreeSubjects.exceptionOrNull()?.message?.contains("exactly four distinct subjects") == true
        )

        // Attempt 2: 5 subjects (Fails)
        val failFiveSubjects = entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "student@jamb.ng",
            studentName = "Adaeze Okafor",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY,
                SubjectType.BIOLOGY
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )
        assertTrue("Granting 5 subjects for JAMB must fail", failFiveSubjects.isFailure)

        // Attempt 3: 4 subjects but MISSING compulsory Use of English (Fails)
        val failMissingEnglish = entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "student@jamb.ng",
            studentName = "Adaeze Okafor",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY,
                SubjectType.BIOLOGY
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )
        assertTrue("JAMB combination without Use of English must fail", failMissingEnglish.isFailure)
        assertTrue(
            failMissingEnglish.exceptionOrNull()?.message?.contains("compulsory Use of English") == true
        )

        // Attempt 4: 4 subjects with duplicates (e.g. English, Maths, Maths, Physics) (Fails)
        val failDuplicates = entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "student@jamb.ng",
            studentName = "Adaeze Okafor",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )
        assertTrue("JAMB combination with duplicates must fail", failDuplicates.isFailure)

        // Attempt 5: Valid combination (English + 3 distinct subjects) (Succeeds)
        val successResult = entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "student@jamb.ng",
            studentName = "Adaeze Okafor",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt",
            reason = "Paid registration verified"
        )
        assertTrue("Valid 4-subject JAMB combination must succeed", successResult.isSuccess)

        val entitlement = successResult.getOrThrow()
        assertEquals(4, entitlement.subjects.size)
        assertTrue(entitlement.subjects.contains(SubjectType.ENGLISH))
        assertTrue(entitlement.coversSubject(SubjectType.ENGLISH))
        assertTrue(entitlement.coversSubject(SubjectType.MATHEMATICS))
        assertTrue(entitlement.coversSubject(SubjectType.PHYSICS))
        assertTrue(entitlement.coversSubject(SubjectType.CHEMISTRY))
        assertFalse("Subject not in combination must NOT be covered", entitlement.coversSubject(SubjectType.BIOLOGY))
    }

    // ==========================================
    // 2. WAEC & NECO ALL-CONFIGURED-SUBJECTS
    // ==========================================

    @Test
    fun testWaecAndNecoExamLevelAccessCoversAllConfiguredAndFutureSubjects() = runTest {
        val studentUid = "student_waec_002"

        val grantResult = entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "waec.student@waec.org",
            studentName = "Emeka Obi",
            examType = ExamType.WAEC,
            isExamLevel = true,
            grantedByAdmin = "admin@examprep.cbt",
            reason = "WAEC Full Package"
        )
        assertTrue("WAEC exam-level grant must succeed", grantResult.isSuccess)

        // Must cover all subjects for WAEC
        ExamType.WAEC.getAvailableSubjects().forEach { sub ->
            val subType = SubjectType.fromId(sub.id)
            assertTrue(
                "WAEC exam-level access must cover ${sub.name}",
                entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.WAEC, subType)
            )
        }

        // Even newly added / configured subjects are covered
        assertTrue(
            "Any future subject under WAEC must be automatically covered",
            entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.WAEC, SubjectType.CIVIC_EDUCATION)
        )

        // But must NOT cover other exam types
        assertFalse(
            "WAEC entitlement must NOT grant access to JAMB subjects",
            entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.MATHEMATICS)
        )
    }

    // ==========================================
    // 3. SINGLE-OPERATION BATCH-GRANT CONSISTENCY
    // ==========================================

    @Test
    fun testSingleOperationGrantReplacesPreviousExamEntitlementCleanly() = runTest {
        val studentUid = "student_repeat_003"

        // Grant 1: Initial JAMB subjects
        entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "test@examprep.cbt",
            studentName = "Chidi",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )

        var studentEntitlements = entitlementRepository.getEntitlementsForStudent(studentUid)
        assertEquals("Should have exactly 1 active entitlement", 1, studentEntitlements.size)

        // Grant 2: Re-grant / update JAMB combination in one single operation
        entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "test@examprep.cbt",
            studentName = "Chidi",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.BIOLOGY,
                SubjectType.CHEMISTRY,
                SubjectType.AGRICULTURAL_SCIENCE
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt",
            reason = "Subject switch approved"
        )

        studentEntitlements = entitlementRepository.getEntitlementsForStudent(studentUid)
        assertEquals(
            "Re-granting for same exam must replace previous entitlement, preventing duplicate or inconsistent records",
            1,
            studentEntitlements.size
        )

        // New subjects are active
        assertTrue(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.BIOLOGY))
        assertTrue(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.AGRICULTURAL_SCIENCE))
        // Old subjects not in new combination are no longer active
        assertFalse(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.MATHEMATICS))
        assertFalse(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.PHYSICS))
    }

    // ==========================================
    // 4. PRESERVATION OF VALID ACCESS FOR OTHER EXAMS
    // ==========================================

    @Test
    fun testGrantingExamAccessPreservesAccessForOtherExaminations() = runTest {
        val studentUid = "student_multi_exam_004"

        // Grant 1: WAEC Full Access
        entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "multi@examprep.cbt",
            studentName = "Kalu",
            examType = ExamType.WAEC,
            isExamLevel = true,
            grantedByAdmin = "admin@examprep.cbt"
        )

        // Grant 2: JAMB Combination
        entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "multi@examprep.cbt",
            studentName = "Kalu",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.GOVERNMENT,
                SubjectType.LITERATURE,
                SubjectType.CRS
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )

        val entitlements = entitlementRepository.getEntitlementsForStudent(studentUid)
        assertEquals("Both WAEC and JAMB entitlements must coexist safely", 2, entitlements.size)

        // Verify WAEC is still active
        assertTrue(
            "WAEC full access must be preserved after granting JAMB",
            entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.WAEC, SubjectType.MATHEMATICS)
        )
        assertTrue(
            "WAEC full access must cover literature",
            entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.WAEC, SubjectType.LITERATURE)
        )

        // Verify JAMB access is active for chosen subjects
        assertTrue(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.ENGLISH))
        assertTrue(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.GOVERNMENT))
        assertFalse(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.PHYSICS))
    }

    // ==========================================
    // 5. CHANGING JAMB COMBINATION PRESERVES CBT RESULTS
    // ==========================================

    @Test
    fun testChangingJambCombinationDoesNotDeleteOrCorruptHistoricalCbtResults() = runTest {
        val studentUid = "student_history_005"

        // 1. Initial JAMB access with Physics
        entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "candidate@jamb.ng",
            studentName = "Ngozi",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )

        // 2. Student takes Physics CBT test and scores 85%
        val physicsSubject = Subject(
            type = SubjectType.PHYSICS,
            description = "Physics CBT",
            iconName = "physics",
            questionCount = 40,
            durationMinutes = 60,
            topics = listOf("Mechanics")
        )
        val physicsResult = CbtResult(
            examType = ExamType.JAMB,
            subject = physicsSubject,
            totalQuestions = 40,
            accessibleQuestionsCount = 40,
            correctCount = 34,
            wrongCount = 6,
            unansweredCount = 0,
            scorePercentage = 85,
            timeSpentSeconds = 1800,
            questionResults = emptyList(),
            isFreePracticeMode = false
        )
        val savedRecord = historyRepository.saveTestResult(studentUid, physicsResult)
        assertNotNull(savedRecord.id)

        // 3. Student / Admin updates JAMB combination, swapping Physics for Biology
        val updateResult = entitlementRepository.updateStudentJambCombination(
            studentUid = studentUid,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.BIOLOGY,
                SubjectType.CHEMISTRY
            )
        )
        assertTrue("Updating combination must succeed", updateResult.isSuccess)

        // 4. Verify historical CBT results for Physics are completely intact!
        val stats = historyRepository.getStatsForUser(studentUid)
        assertEquals("Total tests in history must remain 1", 1, stats.totalTests)
        assertEquals("Historical score must remain 85%", 85, stats.averageScore)
        assertEquals("Best subject must remain Physics", "Physics", stats.bestSubject)

        // 5. Verify entitlement access has switched
        assertTrue(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.BIOLOGY))
        assertFalse(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.PHYSICS))
    }

    // ==========================================
    // 6. PRESERVATION OF 20-QUESTION FREE LIMIT
    // ==========================================

    @Test
    fun testFreeAccessLimitOfTwentyQuestionsIsPreserved() {
        val studentUid = "guest_student_006"

        // Connect accessManager to entitlementChecker for student
        accessManager.entitlementChecker = { examType, subjectType ->
            entitlementRepository.hasActiveEntitlementSync(studentUid, examType, subjectType)
        }

        // Student has NO entitlement for JAMB Physics
        // Questions 0 to 19 must be FREE
        for (i in 0 until AccessConfig.FREE_QUESTION_LIMIT) {
            assertTrue("Question order index $i must be accessible for free", accessManager.canAccessQuestion(ExamType.JAMB, SubjectType.PHYSICS, i))
            assertEquals(QuestionAccessState.FREE, accessManager.getAccessState(ExamType.JAMB, SubjectType.PHYSICS, i))
        }

        // Question 20 (the 21st question) must be LOCKED
        assertFalse("Question 21 (index 20) must be locked without entitlement", accessManager.canAccessQuestion(ExamType.JAMB, SubjectType.PHYSICS, 20))
        assertEquals(QuestionAccessState.LOCKED, accessManager.getAccessState(ExamType.JAMB, SubjectType.PHYSICS, 20))

        assertEquals("Accessible question count must be capped at 20", 20, accessManager.getAccessibleQuestionCount(ExamType.JAMB, SubjectType.PHYSICS, 50))
    }

    // ==========================================
    // 7. AUTHORITATIVE ENTITLEMENT ENFORCEMENT
    // ==========================================

    @Test
    fun testAuthorizationEnforcedThroughTrustedEntitlementArchitecture() = runTest {
        val studentUid = "student_trusted_007"

        accessManager.entitlementChecker = { examType, subjectType ->
            entitlementRepository.hasActiveEntitlementSync(studentUid, examType, subjectType)
        }

        // Initially locked for question 21
        assertFalse(accessManager.hasFullAccess(ExamType.JAMB, SubjectType.PHYSICS))

        // Grant valid entitlement through EntitlementRepository
        entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "trusted@examprep.cbt",
            studentName = "Trusted Student",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY,
                SubjectType.MATHEMATICS
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )

        // Now unlocked for Physics
        assertTrue(accessManager.hasFullAccess(ExamType.JAMB, SubjectType.PHYSICS))
        assertTrue(accessManager.canAccessQuestion(ExamType.JAMB, SubjectType.PHYSICS, 20))
        assertEquals(QuestionAccessState.UNLOCKED, accessManager.getAccessState(ExamType.JAMB, SubjectType.PHYSICS, 20))
    }

    // ==========================================
    // 8. STUDENT SEARCH BY NAME, PHONE, AND ID
    // ==========================================

    @Test
    fun testStudentSearchByNamePhoneAndStudentId() = runTest {
        val student1 = StudentUser(
            uid = "uid_001",
            displayName = "Bolanle Adeyemi",
            email = "bolanle@test.ng",
            phone = "08031112222",
            studentId = "STU-2025-001",
            targetExam = ExamType.JAMB
        )
        val student2 = StudentUser(
            uid = "uid_002",
            displayName = "Chukwuemeka Okonkwo",
            email = "emeka@test.ng",
            phone = "08145556666",
            studentId = "STU-2025-002",
            targetExam = ExamType.WAEC
        )

        val localStorage = LocalAuthStorage.getInstance(context)
        localStorage.registerLocalAccount(student1, phone = student1.phone, studentId = student1.studentId)
        localStorage.registerLocalAccount(student2, phone = student2.phone, studentId = student2.studentId)
        studentRepository.refreshStudents()

        // 1. Search by full/partial name
        val nameSearch = studentRepository.searchStudents("Bolanle")
        assertEquals(1, nameSearch.size)
        assertEquals("Bolanle Adeyemi", nameSearch.first().displayName)

        // 2. Search by phone number
        val phoneSearch = studentRepository.searchStudents("0814555")
        assertEquals(1, phoneSearch.size)
        assertEquals("Chukwuemeka Okonkwo", phoneSearch.first().displayName)

        // 3. Search by student ID
        val idSearch = studentRepository.searchStudents("STU-2025-001")
        assertEquals(1, idSearch.size)
        assertEquals("Bolanle Adeyemi", idSearch.first().displayName)
    }

    // ==========================================
    // 9. ENTITLEMENT REVOCATION WORKFLOW
    // ==========================================

    @Test
    fun testRevokeAccessDeactivatesEntitlementImmediately() = runTest {
        val studentUid = "student_revoke_009"

        val grantResult = entitlementRepository.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "revoke.test@examprep.cbt",
            studentName = "Tari",
            examType = ExamType.JAMB,
            subjects = listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY
            ),
            isExamLevel = false,
            grantedByAdmin = "admin@examprep.cbt"
        )
        val entitlement = grantResult.getOrThrow()
        assertTrue(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.PHYSICS))

        // Admin revokes access
        val revokeResult = entitlementRepository.revokeAccess(
            entitlementId = entitlement.id,
            revokedByAdmin = "admin@examprep.cbt",
            reason = "Subscription ended"
        )
        assertTrue(revokeResult.isSuccess)

        // Access is immediately revoked
        assertFalse(entitlementRepository.hasActiveEntitlementSync(studentUid, ExamType.JAMB, SubjectType.PHYSICS))
        val updatedList = entitlementRepository.getEntitlementsForStudent(studentUid)
        assertEquals(EntitlementStatus.REVOKED, updatedList.first().status)
        assertFalse(updatedList.first().isCurrentlyActive)
    }
}
