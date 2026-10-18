package com.success.examprep.data.model

import com.success.examprep.data.repository.QuestionRepository

/**
 * Quality-control metrics and audit summary for past-question bank.
 */
data class QuestionBankQualitySummary(
    val examType: ExamType,
    val subject: SubjectType,
    val totalCount: Int,
    val verifiedPastQuestionCount: Int,
    val practiceQuestionCount: Int,
    val pendingReviewCount: Int,
    val draftCount: Int,
    val rejectedCount: Int,
    val targetThreshold: Int = 200
) {
    val meetsThreshold: Boolean get() = (verifiedPastQuestionCount + practiceQuestionCount) >= targetThreshold
    val verifiedPercentage: Float
        get() = if (totalCount > 0) (verifiedPastQuestionCount.toFloat() / totalCount) * 100f else 0f
}

/**
 * Subject offering metadata clarifying supported subjects across examination bodies.
 */
data class ExamSubjectOffering(
    val examType: ExamType,
    val subject: SubjectType,
    val isOfficiallyOffered: Boolean = true,
    val targetQuestionQuota: Int = 200,
    val notes: String = ""
)
