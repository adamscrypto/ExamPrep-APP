package com.success.examprep.data.model

/**
 * Validation result for questions.
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
}

typealias QuestionValidationResult = ValidationResult

/**
 * Question validator ensuring data integrity and provenance compliance before ingestion.
 */
object QuestionValidator {
    fun validate(question: Question): ValidationResult {
        if (question.questionText.isBlank()) {
            return ValidationResult.Invalid("Question text cannot be blank.")
        }
        if (question.optionA.isBlank()) {
            return ValidationResult.Invalid("Option A cannot be blank.")
        }
        if (question.optionB.isBlank()) {
            return ValidationResult.Invalid("Option B cannot be blank.")
        }
        if (question.optionC.isBlank()) {
            return ValidationResult.Invalid("Option C cannot be blank.")
        }
        if (question.optionD.isBlank()) {
            return ValidationResult.Invalid("Option D cannot be blank.")
        }
        if (question.topic.isBlank()) {
            return ValidationResult.Invalid("Question topic cannot be blank.")
        }
        if (question.year < 1990 || question.year > 2050) {
            return ValidationResult.Invalid("Invalid examination year: ${question.year}")
        }

        // Distinct options check
        val distinctOpts = setOf(
            question.optionA.trim().lowercase(),
            question.optionB.trim().lowercase(),
            question.optionC.trim().lowercase(),
            question.optionD.trim().lowercase()
        )
        if (distinctOpts.size < 4) {
            return ValidationResult.Invalid("Options must be distinct from one another.")
        }

        // Provenance verification integrity check
        if (question.sourceType == QuestionSourceType.OFFICIAL && question.verificationStatus == QuestionVerificationStatus.VERIFIED) {
            if (question.sourceTitle.isBlank() && question.sourceReference.isBlank()) {
                return ValidationResult.Invalid("Official verified past questions require sourceTitle or sourceReference citation.")
            }
        }

        return ValidationResult.Valid
    }
}
