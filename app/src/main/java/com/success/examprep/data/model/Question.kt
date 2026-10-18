package com.success.examprep.data.model

/**
 * Scalable Question data model for ExamPrep CBT.
 * Structured with strict types for offline storage, Room persistence, and remote syncing.
 * Stage 7A: Extended with authentic past-question provenance and editorial quality-control metadata.
 */
data class Question(
    val id: Int,
    val examType: ExamType,
    val subject: SubjectType,
    val year: Int,
    val topic: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: OptionLetter,
    val explanation: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isFree: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "SYSTEM",
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "SYSTEM",

    // Stage 7A: Provenance and Quality-Control Metadata
    val sourceType: QuestionSourceType = QuestionSourceType.PRACTICE,
    val sourceTitle: String = "",
    val sourceOrganization: String = "",
    val sourceReference: String = "",
    val sourceUrl: String = "",
    val sourceYear: Int? = null,
    val sourcePage: String = "",
    val originalQuestionNumber: String = "",
    val verificationStatus: QuestionVerificationStatus = QuestionVerificationStatus.VERIFIED,
    val answerVerificationStatus: AnswerVerificationStatus = AnswerVerificationStatus.REVIEWED,
    val verifiedBy: String = "SYSTEM",
    val verifiedAt: Long? = System.currentTimeMillis(),
    val reviewNotes: String = "",
    val contentPermissionStatus: ContentPermissionStatus = ContentPermissionStatus.PUBLIC_DOMAIN_PAST_EXAM
) {
    /**
     * Four multiple-choice options in ordered list format (A, B, C, D).
     */
    val options: List<String>
        get() = listOf(optionA, optionB, optionC, optionD)

    /**
     * 0-based index of the correct answer (0 = A, 1 = B, 2 = C, 3 = D).
     */
    val correctAnswerIndex: Int
        get() = correctAnswer.index

    /**
     * Letter representing the correct answer ("A", "B", "C", "D").
     */
    val correctAnswerLetter: String
        get() = correctAnswer.name

    /**
     * Text corresponding to the correct option.
     */
    val correctAnswerText: String
        get() = when (correctAnswer) {
            OptionLetter.A -> optionA
            OptionLetter.B -> optionB
            OptionLetter.C -> optionC
            OptionLetter.D -> optionD
        }

    /**
     * Helper string subject ID for routing and backward compatibility.
     */
    val subjectId: String
        get() = subject.id

    /**
     * Flag indicating if question is an approved authentic past question vs practice item.
     */
    val isVerifiedPastQuestion: Boolean
        get() = (sourceType == QuestionSourceType.OFFICIAL || sourceType == QuestionSourceType.AUTHORIZED_PUBLICATION) &&
            verificationStatus == QuestionVerificationStatus.VERIFIED

    /**
     * Validates this question instance using the QuestionValidator.
     */
    fun validate(): ValidationResult = QuestionValidator.validate(this)
}
