package com.success.examprep.data.model

import com.success.examprep.data.repository.MockExamRepository

/**
 * Supported Nigerian Examination Bodies
 */
enum class ExamType(
    val id: String,
    val title: String,
    val subtitle: String,
    val fullName: String,
    val description: String,
    val badgeText: String
) {
    JAMB(
        id = "jamb",
        title = "JAMB",
        subtitle = "UTME Practice",
        fullName = "Joint Admissions and Matriculation Board",
        description = "Unified Tertiary Matriculation Examination practice questions aligned with the latest syllabus.",
        badgeText = "UTME"
    ),
    WAEC(
        id = "waec",
        title = "WAEC",
        subtitle = "WASSCE Practice",
        fullName = "West African Examinations Council",
        description = "West African Senior School Certificate Examination practice tailored for high scoring.",
        badgeText = "WASSCE"
    ),
    NECO(
        id = "neco",
        title = "NECO",
        subtitle = "SSCE Practice",
        fullName = "National Examinations Council",
        description = "National Senior School Certificate Examination standard practice and timing.",
        badgeText = "SSCE"
    );

    val displayName: String
        get() = title

    fun getAvailableSubjects(): List<Subject> {
        return MockExamRepository().getSubjects(this)
    }

    companion object {
        fun fromId(id: String?): ExamType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: JAMB
        }
    }
}
