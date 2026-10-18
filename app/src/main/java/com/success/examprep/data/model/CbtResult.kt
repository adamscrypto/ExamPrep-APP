package com.success.examprep.data.model

/**
 * Result data class representing the student's performance upon completing a CBT exam.
 */
data class CbtResult(
    val examType: ExamType,
    val subject: Subject,
    val totalQuestions: Int,
    val accessibleQuestionsCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val scorePercentage: Int,
    val timeSpentSeconds: Long,
    val questionResults: List<QuestionReviewItem>,
    val isFreePracticeMode: Boolean = false
) {
    val resultTitle: String
        get() = if (isFreePracticeMode) "FREE PRACTICE COMPLETED" else "TEST COMPLETED"

    val performanceFeedback: String
        get() = when {
            scorePercentage >= 80 -> "Excellent! Outstanding performance, well prepared for high cutoff marks."
            scorePercentage >= 60 -> "Good job! You passed comfortably, but review missed topics to reach 80%+."
            scorePercentage >= 50 -> "Fair attempt. Keep practicing key formulas and speed to boost your score."
            else -> "Needs more practice. Revise fundamental concepts and try again."
        }

    val gradeColorHex: Long
        get() = when {
            scorePercentage >= 70 -> 0xFF16A34A // Success Green
            scorePercentage >= 50 -> 0xFFD97706 // Warning Amber
            else -> 0xFFDC2626 // Error Red
        }
}

/**
 * Item for reviewing each individual question on the Result Screen.
 */
data class QuestionReviewItem(
    val question: Question,
    val selectedOptionIndex: Int?,
    val isCorrect: Boolean
) {
    val selectedOptionLetter: String
        get() = when (selectedOptionIndex) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            else -> "Unanswered"
        }

    val selectedOptionText: String
        get() = if (selectedOptionIndex != null && selectedOptionIndex in question.options.indices) {
            question.options[selectedOptionIndex]
        } else {
            "Not attempted"
        }
}
