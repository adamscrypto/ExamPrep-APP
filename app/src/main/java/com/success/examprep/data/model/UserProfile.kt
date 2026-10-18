package com.success.examprep.data.model

import com.google.firebase.Timestamp

/**
 * Firestore student user profile model for ExamPrep CBT.
 * Corresponds to documents in the `users/{uid}` collection.
 *
 * CRITICAL SECURITY RULE:
 * This model NEVER contains password or confirmPassword fields.
 * Passwords are handled exclusively by Firebase Authentication.
 */
data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String? = null,
    val studentId: String? = null,
    val profilePhotoUrl: String? = null,
    val accountStatus: String = "ACTIVE",
    val targetExam: String = ExamType.JAMB.name,
    val targetYear: Int = 2025,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val initials: String
        get() {
            val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "S"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }

    val firstName: String
        get() = fullName.trim().split(" ").firstOrNull { it.isNotBlank() } ?: "Student"

    /**
     * Converts to StudentUser for backwards compatibility with earlier CBT stages.
     */
    fun toStudentUser(): StudentUser {
        val exam = try {
            ExamType.valueOf(targetExam)
        } catch (_: Exception) {
            ExamType.JAMB
        }
        return StudentUser(
            uid = uid,
            displayName = fullName.ifBlank { "Student" },
            email = email,
            phone = phone,
            studentId = studentId,
            targetExam = exam,
            targetYear = targetYear,
            joinedDateMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>, documentId: String): UserProfile {
            val createdAtTs = map["createdAt"] as? Timestamp
            val updatedAtTs = map["updatedAt"] as? Timestamp
            val targetYearInt = (map["targetYear"] as? Number)?.toInt() ?: 2025

            return UserProfile(
                uid = (map["uid"] as? String) ?: documentId,
                fullName = (map["fullName"] as? String) ?: "",
                email = (map["email"] as? String) ?: "",
                phone = map["phone"] as? String,
                studentId = map["studentId"] as? String,
                profilePhotoUrl = map["profilePhotoUrl"] as? String,
                accountStatus = (map["accountStatus"] as? String) ?: "ACTIVE",
                targetExam = (map["targetExam"] as? String) ?: ExamType.JAMB.name,
                targetYear = targetYearInt,
                createdAt = createdAtTs,
                updatedAt = updatedAtTs
            )
        }
    }
}
