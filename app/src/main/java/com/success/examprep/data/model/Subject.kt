package com.success.examprep.data.model

/**
 * Model representing a subject available for an examination.
 */
data class Subject(
    val type: SubjectType,
    val id: String = type.id,
    val name: String = type.displayName,
    val description: String,
    val iconName: String,
    val questionCount: Int,
    val durationMinutes: Int,
    val topics: List<String>
)
