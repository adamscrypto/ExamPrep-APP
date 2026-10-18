package com.success.examprep.data.model

/**
 * Model representing an authenticated student in ExamPrep CBT.
 */
data class StudentUser(
    val uid: String,
    val displayName: String,
    val email: String,
    val phone: String? = null,
    val studentId: String? = null,
    val targetExam: ExamType = ExamType.JAMB,
    val targetYear: Int = 2025,
    val joinedDateMillis: Long = System.currentTimeMillis(),
    val isAnonymous: Boolean = false
) {
    val initials: String
        get() {
            val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "S"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }

    val firstName: String
        get() = displayName.trim().split(" ").firstOrNull { it.isNotBlank() } ?: "Student"
}
