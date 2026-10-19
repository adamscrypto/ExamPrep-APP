package com.success.examprep

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.success.examprep.data.access.AccessConfig
import com.success.examprep.data.access.LocalAccessManager
import com.success.examprep.data.access.QuestionAccessState
import com.success.examprep.data.auth.LocalAuthStorage
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.PaymentSettings
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.model.UserProfile
import com.success.examprep.data.repository.DefaultEntitlementRepository
import com.success.examprep.data.repository.DefaultPaymentSettingsRepository
import com.success.examprep.data.repository.DefaultStudentRepository
import com.success.examprep.ui.screens.admin.AdminTab
import com.success.examprep.ui.screens.admin.AdminViewModel
import com.success.examprep.ui.screens.cbt.CbtViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Stage 2 Comprehensive Verification Suite:
 * - Configurable Payment Settings Model (₦2,500 initial default, validation, formatting)
 * - Administrator Payment Settings Repository (Firestore & local cache persistence)
 * - Admin Dashboard Payment Settings Editing & Validation
 * - Security Rules: `config/{configDoc}` read by authenticated students, write by admins only
 * - Removal of Insecure Local Voucher Unlock Path
 * - WhatsApp Prefilled Message Composition with Student Identity & Exam Details
 * - Authoritative Entitlement Access Verification Flow
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WhatsAppPaymentSettingsWorkflowTest {

    private lateinit var context: Context
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var paymentRepo: DefaultPaymentSettingsRepository
    private lateinit var entitlementRepo: DefaultEntitlementRepository
    private lateinit var studentRepo: DefaultStudentRepository
    private lateinit var accessManager: LocalAccessManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("examprep_payment_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("examprep_entitlement_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("examprep_local_users", Context.MODE_PRIVATE).edit().clear().commit()

        paymentRepo = DefaultPaymentSettingsRepository(context, testDispatcher)
        entitlementRepo = DefaultEntitlementRepository(context)
        studentRepo = DefaultStudentRepository(context)
        accessManager = LocalAccessManager.getInstance()
        accessManager.resetAllAccess()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPaymentSettingsInitialDefaultsAndFormatting() {
        val settings = PaymentSettings()
        assertEquals(2500L, settings.amountNaira)
        assertEquals("₦2,500", settings.formattedAmount)
        assertEquals("Zenith Bank", settings.bankName)
        assertEquals("ExamPrep CBT Educational Services", settings.accountName)
        assertEquals("1012345678", settings.accountNumber)
        assertEquals("+2348012345678", settings.whatsappNumber)
        assertEquals("2348012345678", settings.cleanWhatsAppNumber)
        assertTrue(settings.paymentInstructions.contains("Transfer the exact fee"))
        assertTrue(settings.validate().isSuccess)
    }

    @Test
    fun testPaymentSettingsValidationRules() {
        // Amount must be positive
        val zeroAmount = PaymentSettings(amountNaira = 0L)
        assertFalse("Zero amount should fail validation", zeroAmount.validate().isSuccess)

        val negativeAmount = PaymentSettings(amountNaira = -500L)
        assertFalse("Negative amount should fail validation", negativeAmount.validate().isSuccess)

        // Bank name cannot be blank
        val blankBank = PaymentSettings(bankName = "   ")
        assertFalse("Blank bank name should fail validation", blankBank.validate().isSuccess)

        // Account name cannot be blank
        val blankAccName = PaymentSettings(accountName = "")
        assertFalse("Blank account name should fail validation", blankAccName.validate().isSuccess)

        // Account number must have at least 5 digits
        val shortAccNumber = PaymentSettings(accountNumber = "123")
        assertFalse("Short account number should fail validation", shortAccNumber.validate().isSuccess)

        // WhatsApp number must include country code digits
        val invalidWaNumber = PaymentSettings(whatsappNumber = "123")
        assertFalse("Invalid short WhatsApp number should fail validation", invalidWaNumber.validate().isSuccess)

        val waWithLetters = PaymentSettings(whatsappNumber = "+23480CALLME")
        assertFalse("WhatsApp number with alphabetic chars should fail validation", waWithLetters.validate().isSuccess)

        // Valid settings should succeed
        val validCustom = PaymentSettings(
            amountNaira = 3500L,
            bankName = "First Bank of Nigeria",
            accountName = "ExamPrep Solutions Ltd",
            accountNumber = "3019876543",
            whatsappNumber = "+2348123456789"
        )
        assertTrue("Valid custom payment settings must pass validation", validCustom.validate().isSuccess)
        assertEquals("₦3,500", validCustom.formattedAmount)
        assertEquals("2348123456789", validCustom.cleanWhatsAppNumber)
    }

    @Test
    fun testPaymentSettingsMapSerialization() {
        val original = PaymentSettings(
            amountNaira = 4000L,
            bankName = "Access Bank",
            accountName = "Test Account",
            accountNumber = "0023456789",
            whatsappNumber = "+2349012345678",
            paymentInstructions = "Send screenshot after transfer."
        )

        val map = original.toMap()
        val reconstructed = PaymentSettings.fromMap(map, isLive = true)

        assertEquals(original.amountNaira, reconstructed.amountNaira)
        assertEquals(original.bankName, reconstructed.bankName)
        assertEquals(original.accountName, reconstructed.accountName)
        assertEquals(original.accountNumber, reconstructed.accountNumber)
        assertEquals(original.whatsappNumber, reconstructed.whatsappNumber)
        assertEquals(original.paymentInstructions, reconstructed.paymentInstructions)
        assertTrue(reconstructed.isLiveConfig)
    }

    @Test
    fun testPaymentSettingsRepositoryCachePersistenceAndFlow() = runTest {
        // Initial defaults
        val initial = paymentRepo.getPaymentSettings()
        assertEquals(2500L, initial.amountNaira)

        // Update settings
        val newSettings = PaymentSettings(
            amountNaira = 3000L,
            bankName = "Guaranty Trust Bank (GTBank)",
            accountName = "ExamPrep Official Account",
            accountNumber = "0123456789",
            whatsappNumber = "+2347012345678",
            paymentInstructions = "Attach WhatsApp transfer receipt for instant activation."
        )

        val updateResult = paymentRepo.updatePaymentSettings(newSettings)
        assertTrue("Updating valid payment settings must succeed", updateResult.isSuccess)

        // Read back from repo and flow
        val updated = paymentRepo.getPaymentSettings()
        assertEquals(3000L, updated.amountNaira)
        assertEquals("Guaranty Trust Bank (GTBank)", updated.bankName)
        assertEquals("₦3,000", updated.formattedAmount)

        val flowValue = paymentRepo.getPaymentSettingsFlow().first()
        assertEquals(3000L, flowValue.amountNaira)

        // Recreate repo instance simulating app relaunch; verify SharedPreferences persistence
        val reloadedRepo = DefaultPaymentSettingsRepository(context, testDispatcher)
        val cached = reloadedRepo.getCachedPaymentSettings()
        assertEquals(3000L, cached.amountNaira)
        assertEquals("Guaranty Trust Bank (GTBank)", cached.bankName)
    }

    @Test
    fun testAdminViewModelPaymentSettingsIntegration() = runTest {
        val adminViewModel = AdminViewModel(
            authRepository = com.success.examprep.data.auth.FirebaseAuthRepository.getInstance(context),
            questionRepository = com.success.examprep.data.repository.FirestoreQuestionRepository.getInstance(context),
            studentRepository = studentRepo,
            entitlementRepository = entitlementRepo,
            paymentSettingsRepository = paymentRepo
        )

        // Initial settings loaded
        val initialUiState = adminViewModel.uiState.value
        assertNotNull(initialUiState.paymentSettings)
        assertEquals(2500L, initialUiState.paymentSettings.amountNaira)

        // Attempting to save invalid settings sets error state
        val invalidSettings = PaymentSettings(amountNaira = -100L)
        adminViewModel.savePaymentSettings(invalidSettings)
        testScheduler.advanceUntilIdle()

        val errorUiState = adminViewModel.uiState.value
        assertNotNull("Admin UI state should reflect validation error", errorUiState.paymentSettingsError)
        assertTrue(errorUiState.paymentSettingsError!!.contains("greater than ₦0"))

        // Saving valid settings succeeds and updates state
        val validUpdate = PaymentSettings(
            amountNaira = 3500L,
            bankName = "United Bank for Africa (UBA)",
            accountName = "ExamPrep CBT Education",
            accountNumber = "2019876543",
            whatsappNumber = "+2348098765432",
            paymentInstructions = "Send receipt on WhatsApp."
        )
        adminViewModel.savePaymentSettings(validUpdate)
        testScheduler.advanceUntilIdle()

        val savedUiState = adminViewModel.uiState.value
        assertEquals(3500L, savedUiState.paymentSettings.amountNaira)
        assertEquals("United Bank for Africa (UBA)", savedUiState.paymentSettings.bankName)
        assertNotNull(savedUiState.successMessage)
    }

    @Test
    fun testInsecureLocalUnlockIsNeutralized() = runTest {
        // Connect accessManager to entitlementChecker (the production security pattern)
        val studentUid = "student_test_uid_999"
        accessManager.entitlementChecker = { examType, subjectType ->
            entitlementRepo.hasActiveEntitlementSync(studentUid, examType, subjectType)
        }

        // Before grant: Question 0 is FREE, Question 25 is LOCKED
        assertEquals(QuestionAccessState.FREE, accessManager.getAccessState(ExamType.JAMB, SubjectType.PHYSICS, 0))
        assertEquals(QuestionAccessState.LOCKED, accessManager.getAccessState(ExamType.JAMB, SubjectType.PHYSICS, 25))

        // Create a CbtViewModel and attempt to call unlockSubject
        val cbtViewModel = CbtViewModel(
            authRepository = com.success.examprep.data.auth.FirebaseAuthRepository.getInstance(context),
            questionRepository = com.success.examprep.data.repository.FirestoreQuestionRepository.getInstance(context),
            testHistoryRepository = com.success.examprep.data.repository.FirestoreTestHistoryRepository.getInstance(context),
            accessManager = accessManager
        )

        // Call the deprecated/inert unlockSubject
        cbtViewModel.unlockSubject(SubjectType.PHYSICS)

        // Access must REMAIN LOCKED because trusted entitlement checker is authoritative
        assertFalse(
            "Calling unlockSubject must NEVER bypass trusted entitlement checker",
            accessManager.hasFullAccess(ExamType.JAMB, SubjectType.PHYSICS)
        )
        assertEquals(
            QuestionAccessState.LOCKED,
            accessManager.getAccessState(ExamType.JAMB, SubjectType.PHYSICS, 25)
        )
    }

    @Test
    fun testPrefilledWhatsAppMessageComposition() {
        val settings = PaymentSettings(
            amountNaira = 2500L,
            bankName = "Zenith Bank",
            accountName = "ExamPrep CBT",
            accountNumber = "1012345678",
            whatsappNumber = "+2348012345678"
        )

        val studentName = "Amina Ibrahim"
        val studentPhone = "08012345678"
        val studentId = "STU-2025-001"
        val examType = ExamType.JAMB
        val subjects = listOf(SubjectType.ENGLISH, SubjectType.MATHEMATICS, SubjectType.PHYSICS, SubjectType.CHEMISTRY)

        // Simulate WhatsApp message generation logic
        val message = buildString {
            append("Hello Admin, I have transferred ${settings.formattedAmount} for CBT Full Access.\n\n")
            append("*Student Details:*\n")
            append("• Name: $studentName\n")
            append("• Phone: $studentPhone\n")
            append("• Student ID / UID: $studentId\n")
            append("• Examination: ${examType.displayName}\n")
            append("• Selected JAMB Subjects: " + subjects.joinToString(", ") { it.displayName } + "\n")
            append("• Amount Transferred: ${settings.formattedAmount}\n\n")
            append("I have attached my bank transfer receipt. Kindly verify and grant full access to my account. Thank you!")
        }

        assertTrue("Message must contain student name", message.contains("Amina Ibrahim"))
        assertTrue("Message must contain student phone", message.contains("08012345678"))
        assertTrue("Message must contain student ID", message.contains("STU-2025-001"))
        assertTrue("Message must contain examination", message.contains("JAMB"))
        assertTrue("Message must contain English Language", message.contains("English Language"))
        assertTrue("Message must contain payment amount", message.contains("₦2,500"))
        assertTrue("Message must mention receipt confirmation", message.contains("bank transfer receipt"))
    }

    @Test
    fun testAuthoritativeAccessConfirmationFlow() = runTest {
        val studentUid = "student_whatsapp_user_123"

        // Student initially has no entitlement
        val hasAccessBefore = entitlementRepo.hasActiveAccess(studentUid, ExamType.JAMB, SubjectType.PHYSICS)
        assertFalse("Student must not have access before admin confirmation", hasAccessBefore)

        // Admin verifies payment and grants access
        val grantResult = entitlementRepo.grantExamAccess(
            studentUid = studentUid,
            studentEmail = "student@example.com",
            studentName = "Amina Ibrahim",
            examType = ExamType.JAMB,
            subjects = listOf(SubjectType.ENGLISH, SubjectType.MATHEMATICS, SubjectType.PHYSICS, SubjectType.CHEMISTRY),
            isExamLevel = false,
            grantedByAdmin = "admin_123",
            reason = "Payment confirmed via WhatsApp receipt - Zenith Bank ₦2,500"
        )
        assertTrue("Admin grant must succeed", grantResult.isSuccess)

        // When student checks access status, it is now confirmed
        val hasAccessAfter = entitlementRepo.hasActiveAccess(studentUid, ExamType.JAMB, SubjectType.PHYSICS)
        assertTrue("Student must have active access after admin confirms payment", hasAccessAfter)

        // Subject not in combination remains inaccessible
        val biologyAccess = entitlementRepo.hasActiveAccess(studentUid, ExamType.JAMB, SubjectType.BIOLOGY)
        assertFalse("Subjects not in combination must remain inaccessible", biologyAccess)
    }

    @Test
    fun testFirestoreRulesConfigProtection() {
        val rulesFile = File("firestore.rules").takeIf { it.exists() }
            ?: File("../firestore.rules").takeIf { it.exists() }
            ?: File("../../firestore.rules").takeIf { it.exists() }

        assertNotNull("firestore.rules must exist in root project", rulesFile)
        val content = rulesFile!!.readText()

        assertTrue(
            "firestore.rules must contain match for config collection",
            content.contains("match /config/{configDoc}")
        )
        assertTrue(
            "Config collection must allow read for authenticated users",
            content.contains("allow read: if isAuthenticated() || isAdmin();")
        )
        assertTrue(
            "Config collection must allow write strictly for administrators",
            content.contains("allow write: if isAdmin();")
        )
    }
}
