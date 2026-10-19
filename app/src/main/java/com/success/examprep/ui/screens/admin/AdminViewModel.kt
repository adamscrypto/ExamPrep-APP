package com.success.examprep.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.success.examprep.data.auth.AuthRepository
import com.success.examprep.data.model.AccessEntitlement
import com.success.examprep.data.model.AccessType
import com.success.examprep.data.model.AnswerVerificationStatus
import com.success.examprep.data.model.ContentPermissionStatus
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.EntitlementStatus
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.QuestionBankQualitySummary
import com.success.examprep.data.model.QuestionReviewChecklist
import com.success.examprep.data.model.QuestionSourceType
import com.success.examprep.data.model.QuestionVerificationStatus
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.model.UserSession
import com.success.examprep.data.repository.EntitlementRepository
import com.success.examprep.data.repository.QuestionRepository
import com.success.examprep.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.success.examprep.data.model.PaymentSettings
import com.success.examprep.data.repository.PaymentSettingsRepository

enum class AdminTab(val title: String) {
    OVERVIEW("Overview"),
    QUESTIONS("Question Bank"),
    QUALITY_CONTROL("Quality Control"),
    STUDENTS("Students"),
    ACCESS("Access Management"),
    PAYMENT_SETTINGS("Payment Settings")
}

data class AdminStats(
    val totalQuestions: Int = 0,
    val activeQuestions: Int = 0,
    val inactiveQuestions: Int = 0,
    val verifiedPastQuestions: Int = 0,
    val practiceQuestions: Int = 0,
    val pendingReviewQuestions: Int = 0,
    val rejectedQuestions: Int = 0,
    val jambQuestions: Int = 0,
    val waecQuestions: Int = 0,
    val necoQuestions: Int = 0,
    val totalStudents: Int = 0,
    val activeEntitlements: Int = 0
)

data class AdminUiState(
    val selectedTab: AdminTab = AdminTab.OVERVIEW,
    val stats: AdminStats = AdminStats(),
    val questions: List<Question> = emptyList(),
    val searchQuestionQuery: String = "",
    val filterExamType: ExamType? = null,
    val filterSubjectType: SubjectType? = null,
    val filterIsActive: Boolean? = null,
    val filterDifficulty: Difficulty? = null,
    val filterSourceType: QuestionSourceType? = null,
    val filterVerificationStatus: QuestionVerificationStatus? = null,
    val qualitySummaries: List<QuestionBankQualitySummary> = emptyList(),
    val students: List<StudentUser> = emptyList(),
    val searchStudentQuery: String = "",
    val entitlements: List<AccessEntitlement> = emptyList(),
    val filterEntitlementExam: ExamType? = null,
    val filterEntitlementStatus: EntitlementStatus? = null,
    val selectedStudent: StudentUser? = null,
    val selectedStudentEntitlements: List<AccessEntitlement> = emptyList(),
    val questionToPreview: Question? = null,
    val questionToEdit: Question? = null,
    val questionToReview: Question? = null,
    val isAddQuestionDialogOpen: Boolean = false,
    val isImportDialogOpen: Boolean = false,
    val isGrantAccessDialogOpen: Boolean = false,
    val grantTargetStudent: StudentUser? = null,
    val paymentSettings: PaymentSettings = PaymentSettings(),
    val isSavingPaymentSettings: Boolean = false,
    val paymentSettingsError: String? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val authRepository: AuthRepository,
    private val questionRepository: QuestionRepository,
    private val studentRepository: StudentRepository,
    private val entitlementRepository: EntitlementRepository,
    private val paymentSettingsRepository: PaymentSettingsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val questions = questionRepository.getAllQuestions(includeInactive = true)
                val students = studentRepository.getAllStudents()
                val entitlements = entitlementRepository.getAllEntitlements()

                val verifiedPastCount = questions.count {
                    it.isVerifiedPastQuestion
                }
                val practiceCount = questions.count {
                    it.sourceType == QuestionSourceType.PRACTICE
                }
                val pendingReviewCount = questions.count {
                    it.verificationStatus == QuestionVerificationStatus.PENDING_REVIEW ||
                        it.verificationStatus == QuestionVerificationStatus.DRAFT
                }
                val rejectedCount = questions.count {
                    it.verificationStatus == QuestionVerificationStatus.REJECTED
                }

                val stats = AdminStats(
                    totalQuestions = questions.size,
                    activeQuestions = questions.count { it.isActive },
                    inactiveQuestions = questions.count { !it.isActive },
                    verifiedPastQuestions = verifiedPastCount,
                    practiceQuestions = practiceCount,
                    pendingReviewQuestions = pendingReviewCount,
                    rejectedQuestions = rejectedCount,
                    jambQuestions = questions.count { it.examType == ExamType.JAMB },
                    waecQuestions = questions.count { it.examType == ExamType.WAEC },
                    necoQuestions = questions.count { it.examType == ExamType.NECO },
                    totalStudents = students.size,
                    activeEntitlements = entitlements.count { it.isCurrentlyActive }
                )

                // Compute Quality Summaries for each supported exam and subject
                val summaries = mutableListOf<QuestionBankQualitySummary>()
                for (exam in ExamType.entries) {
                    for (subject in SubjectType.entries) {
                        val subQuestions = questions.filter { it.examType == exam && it.subject == subject }
                        summaries.add(
                            QuestionBankQualitySummary(
                                examType = exam,
                                subject = subject,
                                totalCount = subQuestions.size,
                                verifiedPastQuestionCount = subQuestions.count { it.isVerifiedPastQuestion },
                                practiceQuestionCount = subQuestions.count { it.sourceType == QuestionSourceType.PRACTICE },
                                pendingReviewCount = subQuestions.count { it.verificationStatus == QuestionVerificationStatus.PENDING_REVIEW },
                                draftCount = subQuestions.count { it.verificationStatus == QuestionVerificationStatus.DRAFT },
                                rejectedCount = subQuestions.count { it.verificationStatus == QuestionVerificationStatus.REJECTED },
                                targetThreshold = 200
                            )
                        )
                    }
                }

                val paymentSettings = paymentSettingsRepository?.getPaymentSettings() ?: PaymentSettings()

                _uiState.update {
                    it.copy(
                        questions = questions,
                        students = students,
                        entitlements = entitlements,
                        paymentSettings = paymentSettings,
                        stats = stats,
                        qualitySummaries = summaries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to load admin data") }
            }
        }
    }

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // --- Question Management Actions ---

    fun onQuestionSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuestionQuery = query) }
    }

    fun onFilterExamChanged(examType: ExamType?) {
        _uiState.update { it.copy(filterExamType = examType) }
    }

    fun onFilterSubjectChanged(subjectType: SubjectType?) {
        _uiState.update { it.copy(filterSubjectType = subjectType) }
    }

    fun onFilterActiveChanged(isActive: Boolean?) {
        _uiState.update { it.copy(filterIsActive = isActive) }
    }

    fun onFilterDifficultyChanged(difficulty: Difficulty?) {
        _uiState.update { it.copy(filterDifficulty = difficulty) }
    }

    fun onFilterSourceTypeChanged(sourceType: QuestionSourceType?) {
        _uiState.update { it.copy(filterSourceType = sourceType) }
    }

    fun onFilterVerificationStatusChanged(status: QuestionVerificationStatus?) {
        _uiState.update { it.copy(filterVerificationStatus = status) }
    }

    fun openQuestionPreview(question: Question) {
        _uiState.update { it.copy(questionToPreview = question) }
    }

    fun closeQuestionPreview() {
        _uiState.update { it.copy(questionToPreview = null) }
    }

    fun openQuestionReview(question: Question) {
        _uiState.update { it.copy(questionToReview = question) }
    }

    fun closeQuestionReview() {
        _uiState.update { it.copy(questionToReview = null) }
    }

    fun openAddQuestionDialog() {
        _uiState.update { it.copy(isAddQuestionDialogOpen = true) }
    }

    fun closeAddQuestionDialog() {
        _uiState.update { it.copy(isAddQuestionDialogOpen = false) }
    }

    fun openImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = true) }
    }

    fun closeImportDialog() {
        _uiState.update { it.copy(isImportDialogOpen = false) }
    }

    fun getAuthRepository(): AuthRepository = authRepository

    fun getQuestionRepository(): QuestionRepository = questionRepository

    fun openEditQuestionDialog(question: Question) {
        _uiState.update { it.copy(questionToEdit = question) }
    }

    fun closeEditQuestionDialog() {
        _uiState.update { it.copy(questionToEdit = null) }
    }

    fun addQuestion(
        examType: ExamType,
        subject: SubjectType,
        year: Int,
        topic: String,
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswer: OptionLetter,
        explanation: String,
        difficulty: Difficulty,
        isFree: Boolean,
        isActive: Boolean,
        sourceType: QuestionSourceType = QuestionSourceType.PRACTICE,
        sourceTitle: String = "",
        sourceOrganization: String = "",
        sourceReference: String = "",
        sourceUrl: String = "",
        sourceYear: Int? = null,
        sourcePage: String = "",
        originalQuestionNumber: String = "",
        verificationStatus: QuestionVerificationStatus = QuestionVerificationStatus.VERIFIED,
        answerVerificationStatus: AnswerVerificationStatus = AnswerVerificationStatus.REVIEWED,
        reviewNotes: String = "",
        contentPermissionStatus: ContentPermissionStatus = ContentPermissionStatus.PUBLIC_DOMAIN_PAST_EXAM
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val adminUid = (authRepository.userSession.value as? UserSession.Authenticated)?.uid
                ?: authRepository.getCurrentUser()?.uid
                ?: "admin"
            val newId = questionRepository.generateNextId()
            val newQuestion = Question(
                id = newId,
                examType = examType,
                subject = subject,
                year = year,
                topic = topic.trim(),
                questionText = questionText.trim(),
                optionA = optionA.trim(),
                optionB = optionB.trim(),
                optionC = optionC.trim(),
                optionD = optionD.trim(),
                correctAnswer = correctAnswer,
                explanation = explanation.trim(),
                difficulty = difficulty,
                isFree = isFree,
                isActive = isActive,
                createdAt = System.currentTimeMillis(),
                createdBy = adminUid,
                updatedAt = System.currentTimeMillis(),
                updatedBy = adminUid,
                // Stage 7A fields
                sourceType = sourceType,
                sourceTitle = sourceTitle.trim(),
                sourceOrganization = sourceOrganization.trim(),
                sourceReference = sourceReference.trim(),
                sourceUrl = sourceUrl.trim(),
                sourceYear = sourceYear,
                sourcePage = sourcePage.trim(),
                originalQuestionNumber = originalQuestionNumber.trim(),
                verificationStatus = verificationStatus,
                answerVerificationStatus = answerVerificationStatus,
                verifiedBy = if (verificationStatus == QuestionVerificationStatus.VERIFIED) adminUid else "",
                verifiedAt = if (verificationStatus == QuestionVerificationStatus.VERIFIED) System.currentTimeMillis() else null,
                reviewNotes = reviewNotes.trim(),
                contentPermissionStatus = contentPermissionStatus
            )

            val result = questionRepository.addQuestion(newQuestion)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isAddQuestionDialogOpen = false,
                        successMessage = "Question #${newId} successfully added to question bank!"
                    )
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to add question"
                    )
                }
            }
        }
    }

    fun updateQuestion(
        id: Int,
        examType: ExamType,
        subject: SubjectType,
        year: Int,
        topic: String,
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswer: OptionLetter,
        explanation: String,
        difficulty: Difficulty,
        isFree: Boolean,
        isActive: Boolean,
        sourceType: QuestionSourceType = QuestionSourceType.PRACTICE,
        sourceTitle: String = "",
        sourceOrganization: String = "",
        sourceReference: String = "",
        sourceUrl: String = "",
        sourceYear: Int? = null,
        sourcePage: String = "",
        originalQuestionNumber: String = "",
        verificationStatus: QuestionVerificationStatus = QuestionVerificationStatus.VERIFIED,
        answerVerificationStatus: AnswerVerificationStatus = AnswerVerificationStatus.REVIEWED,
        reviewNotes: String = "",
        contentPermissionStatus: ContentPermissionStatus = ContentPermissionStatus.PUBLIC_DOMAIN_PAST_EXAM
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val adminUid = (authRepository.userSession.value as? UserSession.Authenticated)?.uid
                ?: authRepository.getCurrentUser()?.uid
                ?: "admin"
            val existing = _uiState.value.questions.firstOrNull { it.id == id }
            val updatedQuestion = Question(
                id = id,
                examType = examType,
                subject = subject,
                year = year,
                topic = topic.trim(),
                questionText = questionText.trim(),
                optionA = optionA.trim(),
                optionB = optionB.trim(),
                optionC = optionC.trim(),
                optionD = optionD.trim(),
                correctAnswer = correctAnswer,
                explanation = explanation.trim(),
                difficulty = difficulty,
                isFree = isFree,
                isActive = isActive,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                createdBy = existing?.createdBy ?: adminUid,
                updatedAt = System.currentTimeMillis(),
                updatedBy = adminUid,
                // Stage 7A metadata
                sourceType = sourceType,
                sourceTitle = sourceTitle.trim(),
                sourceOrganization = sourceOrganization.trim(),
                sourceReference = sourceReference.trim(),
                sourceUrl = sourceUrl.trim(),
                sourceYear = sourceYear,
                sourcePage = sourcePage.trim(),
                originalQuestionNumber = originalQuestionNumber.trim(),
                verificationStatus = verificationStatus,
                answerVerificationStatus = answerVerificationStatus,
                verifiedBy = if (verificationStatus == QuestionVerificationStatus.VERIFIED) adminUid else (existing?.verifiedBy ?: ""),
                verifiedAt = if (verificationStatus == QuestionVerificationStatus.VERIFIED) System.currentTimeMillis() else existing?.verifiedAt,
                reviewNotes = reviewNotes.trim(),
                contentPermissionStatus = contentPermissionStatus
            )

            val result = questionRepository.updateQuestion(updatedQuestion)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        questionToEdit = null,
                        questionToReview = null,
                        successMessage = "Question #${id} updated successfully."
                    )
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to update question"
                    )
                }
            }
        }
    }

    /**
     * Stage 7A: Admin Verification & Publication Workflow.
     * Transitions a question through DRAFT -> PENDING_REVIEW -> VERIFIED -> PUBLISHED (isActive = true).
     */
    fun performQuestionReview(
        question: Question,
        newVerificationStatus: QuestionVerificationStatus,
        newAnswerVerificationStatus: AnswerVerificationStatus,
        shouldPublish: Boolean,
        reviewNotes: String,
        correctedAnswer: OptionLetter? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val adminEmail = authRepository.getCurrentUser()?.email ?: "admin@examprep.cbt"
            val effectiveAnswer = correctedAnswer ?: question.correctAnswer

            // Rule: Rejected questions must remain unpublished (isActive = false)
            val effectiveIsActive = if (newVerificationStatus == QuestionVerificationStatus.REJECTED) {
                false
            } else if (newVerificationStatus == QuestionVerificationStatus.VERIFIED && shouldPublish) {
                true
            } else {
                question.isActive
            }

            val updated = question.copy(
                correctAnswer = effectiveAnswer,
                verificationStatus = newVerificationStatus,
                answerVerificationStatus = newAnswerVerificationStatus,
                isActive = effectiveIsActive,
                reviewNotes = reviewNotes.trim(),
                verifiedBy = if (newVerificationStatus == QuestionVerificationStatus.VERIFIED) adminEmail else question.verifiedBy,
                verifiedAt = if (newVerificationStatus == QuestionVerificationStatus.VERIFIED) System.currentTimeMillis() else question.verifiedAt,
                updatedAt = System.currentTimeMillis(),
                updatedBy = adminEmail
            )

            val result = questionRepository.updateQuestion(updated)
            if (result.isSuccess) {
                val statusText = when {
                    newVerificationStatus == QuestionVerificationStatus.VERIFIED && effectiveIsActive -> "Verified and Published"
                    newVerificationStatus == QuestionVerificationStatus.VERIFIED -> "Verified (Unpublished)"
                    newVerificationStatus == QuestionVerificationStatus.REJECTED -> "Rejected"
                    else -> "Saved as ${newVerificationStatus.label}"
                }
                _uiState.update {
                    it.copy(
                        questionToReview = null,
                        successMessage = "Question #${question.id} is now $statusText."
                    )
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to perform review"
                    )
                }
            }
        }
    }

    fun toggleQuestionActive(question: Question) {
        viewModelScope.launch {
            val adminEmail = authRepository.getCurrentUser()?.email ?: "admin@examprep.cbt"
            val newStatus = !question.isActive

            // Guard: Cannot activate a rejected question
            if (newStatus && question.verificationStatus == QuestionVerificationStatus.REJECTED) {
                _uiState.update {
                    it.copy(errorMessage = "Cannot activate a rejected question. Verify it first.")
                }
                return@launch
            }

            val result = questionRepository.setQuestionActive(question.id, newStatus, adminEmail)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        successMessage = "Question #${question.id} is now ${if (newStatus) "Active (Published)" else "Deactivated"}."
                    )
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update question status.")
                }
            }
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            val result = questionRepository.deleteQuestion(question.id)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(successMessage = "Question #${question.id} deleted successfully.")
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete question.")
                }
            }
        }
    }

    // --- Student Management Actions ---

    fun onStudentSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchStudentQuery = query) }
    }

    fun selectStudent(student: StudentUser) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedStudent = student, isLoading = true) }
            val studentEntitlements = entitlementRepository.getEntitlementsForStudent(student.uid)
            _uiState.update {
                it.copy(
                    selectedStudentEntitlements = studentEntitlements,
                    isLoading = false
                )
            }
        }
    }

    fun clearSelectedStudent() {
        _uiState.update { it.copy(selectedStudent = null, selectedStudentEntitlements = emptyList()) }
    }

    fun openGrantAccessDialog(student: StudentUser? = null) {
        _uiState.update {
            it.copy(
                isGrantAccessDialogOpen = true,
                grantTargetStudent = student
            )
        }
    }

    fun closeGrantAccessDialog() {
        _uiState.update {
            it.copy(
                isGrantAccessDialogOpen = false,
                grantTargetStudent = null
            )
        }
    }

    fun grantExamAccess(
        student: StudentUser,
        examType: ExamType,
        subjects: List<SubjectType>,
        isExamLevel: Boolean,
        reason: String,
        durationDays: Int?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val adminEmail = authRepository.getCurrentUser()?.email ?: "admin@examprep.cbt"
            val expiresAt = durationDays?.let { System.currentTimeMillis() + it.toLong() * 24 * 60 * 60 * 1000 }
            val result = entitlementRepository.grantExamAccess(
                studentUid = student.uid,
                studentEmail = student.email,
                studentName = student.displayName,
                examType = examType,
                subjects = subjects,
                isExamLevel = isExamLevel,
                grantedByAdmin = adminEmail,
                reason = reason,
                expiresAt = expiresAt
            )

            if (result.isSuccess) {
                val detailStr = if (isExamLevel) {
                    "all configured subjects for ${examType.displayName}"
                } else {
                    "${subjects.size} subjects (${subjects.joinToString { it.displayName }}) for ${examType.displayName}"
                }
                _uiState.update {
                    it.copy(
                        isGrantAccessDialogOpen = false,
                        grantTargetStudent = null,
                        isLoading = false,
                        successMessage = "Full access granted: $detailStr for ${student.displayName}!"
                    )
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to grant access"
                    )
                }
            }
        }
    }

    fun grantAccess(
        student: StudentUser,
        examType: ExamType,
        subjectType: SubjectType,
        reason: String,
        durationDays: Int?
    ) {
        grantExamAccess(
            student = student,
            examType = examType,
            subjects = listOf(subjectType),
            isExamLevel = false,
            reason = reason,
            durationDays = durationDays
        )
    }

    fun revokeAccess(entitlement: AccessEntitlement) {
        viewModelScope.launch {
            val adminEmail = authRepository.getCurrentUser()?.email ?: "admin@examprep.cbt"
            val result = entitlementRepository.revokeAccess(
                entitlementId = entitlement.id,
                revokedByAdmin = adminEmail,
                reason = "Manual revocation by admin"
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(successMessage = "Entitlement revoked successfully.")
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Failed to revoke entitlement.")
                }
            }
        }
    }

    fun closeStudentDetail() {
        clearSelectedStudent()
    }

    fun resetStudentPin(student: StudentUser, newPin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = studentRepository.resetStudentPinByAdmin(student.uid, newPin)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Student PIN reset successfully."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to reset PIN"
                    )
                }
            }
        }
    }

    fun transferStudentToDevice(
        student: StudentUser,
        initialPin: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = studentRepository.transferStudentToDeviceByAdmin(
                student = student,
                initialPin = initialPin
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Student profile transferred to this device successfully."
                    )
                }
                loadAllData()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to transfer profile"
                    )
                }
            }
        }
    }

    // --- Administrator Payment Settings Actions ---

    fun loadPaymentSettings() {
        viewModelScope.launch {
            val settings = paymentSettingsRepository?.getPaymentSettings() ?: PaymentSettings()
            _uiState.update { it.copy(paymentSettings = settings, paymentSettingsError = null) }
        }
    }

    fun savePaymentSettings(settings: PaymentSettings) {
        viewModelScope.launch {
            val validation = settings.validate()
            if (validation.isFailure) {
                val errorMsg = validation.exceptionOrNull()?.message ?: "Invalid payment configuration."
                _uiState.update {
                    it.copy(
                        paymentSettingsError = errorMsg,
                        errorMessage = errorMsg
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSavingPaymentSettings = true, paymentSettingsError = null) }
            val repo = paymentSettingsRepository
            if (repo != null) {
                val result = repo.updatePaymentSettings(settings)
                if (result.isSuccess) {
                    val saved = result.getOrThrow()
                    _uiState.update {
                        it.copy(
                            isSavingPaymentSettings = false,
                            paymentSettings = saved,
                            successMessage = "Payment settings updated and published successfully."
                        )
                    }
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Failed to save payment settings."
                    _uiState.update {
                        it.copy(
                            isSavingPaymentSettings = false,
                            paymentSettingsError = err,
                            errorMessage = err
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSavingPaymentSettings = false,
                        paymentSettings = settings,
                        successMessage = "Payment settings updated locally."
                    )
                }
            }
        }
    }

    fun clearPaymentSettingsError() {
        _uiState.update { it.copy(paymentSettingsError = null) }
    }

    fun clearFeedbackMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
