package com.success.examprep.ui.screens.cbt

import com.success.examprep.data.access.AccessConfig
import com.success.examprep.data.model.CbtResult
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.Subject

/**
 * Immutable UI state for the CBT Examination flow, supporting Stage 3 Free & Locked question states.
 */
data class CbtUiState(
    val examType: ExamType = ExamType.JAMB,
    val subject: Subject? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // question.id -> selected option index (0..3)
    val remainingSeconds: Long = 600L, // 10 minutes default
    val totalDurationSeconds: Long = 600L,
    val isTimerRunning: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val showLockDialog: Boolean = false,
    val lockedAttemptNumber: Int? = null,
    val hasFullAccess: Boolean = false,
    val isCompleted: Boolean = false,
    val isResultSavedToProfile: Boolean = false,
    val result: CbtResult? = null
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentIndex)

    val totalQuestions: Int
        get() = questions.size

    /**
     * How many questions the student has permission to attempt in this test.
     * If hasFullAccess = true -> totalQuestions.
     * Else -> min(totalQuestions, FREE_QUESTION_LIMIT).
     */
    val accessibleQuestionsCount: Int
        get() = if (hasFullAccess) {
            questions.size
        } else {
            questions.size.coerceAtMost(AccessConfig.FREE_QUESTION_LIMIT)
        }

    val isFreePracticeMode: Boolean
        get() = !hasFullAccess && questions.size > AccessConfig.FREE_QUESTION_LIMIT

    /**
     * Checks if a question at a 0-based index in the test is locked.
     */
    fun isQuestionLocked(orderIndex: Int): Boolean {
        if (hasFullAccess) return false
        return orderIndex >= AccessConfig.FREE_QUESTION_LIMIT
    }

    /**
     * Number of answered questions strictly within the student's accessible question quota.
     */
    val answeredCount: Int
        get() = questions.take(accessibleQuestionsCount).count { q -> selectedAnswers.containsKey(q.id) }

    /**
     * Unanswered questions within the accessible question quota.
     */
    val unansweredCount: Int
        get() = (accessibleQuestionsCount - answeredCount).coerceAtLeast(0)

    val isFirstQuestion: Boolean
        get() = currentIndex <= 0

    val isLastAccessibleQuestion: Boolean
        get() = currentIndex >= (accessibleQuestionsCount - 1).coerceAtLeast(0)

    val formattedTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val isTimeCritical: Boolean
        get() = remainingSeconds in 1..120 // Last 2 minutes
}
