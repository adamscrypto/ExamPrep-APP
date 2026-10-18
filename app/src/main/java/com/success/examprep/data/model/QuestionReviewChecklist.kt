package com.success.examprep.data.model

/**
 * Structured review checklist for Stage 7A past-question quality control.
 */
data class QuestionReviewChecklist(
    val sourceAuthenticityChecked: Boolean = false,
    val examBodyAndSubjectConfirmed: Boolean = false,
    val examYearConfirmed: Boolean = false,
    val questionTextCheckedAgainstSource: Boolean = false,
    val allAnswerOptionsChecked: Boolean = false,
    val correctAnswerIndependentlySolved: Boolean = false,
    val explanationReviewed: Boolean = false,
    val duplicateCheckCompleted: Boolean = false,
    val permissionRequirementsSatisfied: Boolean = false
) {
    val isFullySatisfied: Boolean
        get() = sourceAuthenticityChecked &&
            examBodyAndSubjectConfirmed &&
            examYearConfirmed &&
            questionTextCheckedAgainstSource &&
            allAnswerOptionsChecked &&
            correctAnswerIndependentlySolved &&
            explanationReviewed &&
            duplicateCheckCompleted &&
            permissionRequirementsSatisfied

    val satisfiedCount: Int
        get() = listOf(
            sourceAuthenticityChecked,
            examBodyAndSubjectConfirmed,
            examYearConfirmed,
            questionTextCheckedAgainstSource,
            allAnswerOptionsChecked,
            correctAnswerIndependentlySolved,
            explanationReviewed,
            duplicateCheckCompleted,
            permissionRequirementsSatisfied
        ).count { it }

    val totalCount: Int get() = 9
}
