package com.success.examprep.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saved test record for tracking authenticated student performance.
 */
data class TestHistoryRecord(
    val id: String,
    val studentUid: String,
    val examType: ExamType,
    val subjectId: String,
    val subjectName: String,
    val scorePercentage: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val totalQuestions: Int,
    val timeSpentSeconds: Long,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isFreePracticeMode: Boolean = true
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }

    val gradeRating: String
        get() = when {
            scorePercentage >= 80 -> "Excellent"
            scorePercentage >= 65 -> "Very Good"
            scorePercentage >= 50 -> "Good Pass"
            scorePercentage >= 40 -> "Fair"
            else -> "Needs Practice"
        }
}

/**
 * Aggregated performance metrics for student profile.
 */
data class StudentPerformanceStats(
    val totalTests: Int = 0,
    val averageScore: Int = 0,
    val highestScore: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val bestSubject: String = "None yet"
)
