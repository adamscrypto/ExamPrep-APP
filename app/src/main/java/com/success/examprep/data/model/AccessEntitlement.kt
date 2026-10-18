package com.success.examprep.data.model

import java.util.UUID

/**
 * Access method/source for an entitlement.
 * Designed so future stages can seamlessly reuse this model for Payments, Access Codes, etc.
 */
enum class AccessType(val displayName: String) {
    MANUAL_ADMIN("Manual Admin Grant"),
    PAYMENT("Direct Payment"),
    ACCESS_CODE("Access Code Voucher"),
    PROMOTION("Promotional Campaign"),
    SCHOLARSHIP("Student Scholarship"),
    COMPLIMENTARY("Complimentary Access")
}

/**
 * Lifecycle status of an access entitlement.
 */
enum class EntitlementStatus(val label: String) {
    ACTIVE("Active"),
    REVOKED("Revoked"),
    EXPIRED("Expired")
}

/**
 * Access entitlement granting a student full CBT access to a specific examination board and subject.
 * Integrates directly with AccessManager while maintaining strict separation between
 * Authentication, Admin Authorization, and Access Entitlement.
 */
data class AccessEntitlement(
    val id: String = UUID.randomUUID().toString(),
    val studentUid: String,
    val studentEmail: String = "",
    val studentName: String = "",
    val examType: ExamType,
    val subjectType: SubjectType = SubjectType.ENGLISH,
    val isExamLevel: Boolean = false, // When true, covers ALL configured subjects for examType (WAEC, NECO)
    val subjects: List<SubjectType> = emptyList(), // For multi-subject combinations like JAMB's 4 subjects
    val accessType: AccessType = AccessType.MANUAL_ADMIN,
    val status: EntitlementStatus = EntitlementStatus.ACTIVE,
    val grantedAt: Long = System.currentTimeMillis(),
    val grantedBy: String = "", // Administrator UID or email
    val expiresAt: Long? = null, // null indicates lifetime / non-expiring access
    val reason: String = "", // Optional administrative note / justification
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if this entitlement is currently valid and active.
     */
    val isCurrentlyActive: Boolean
        get() {
            if (status != EntitlementStatus.ACTIVE) return false
            val expiry = expiresAt
            if (expiry != null && System.currentTimeMillis() > expiry) {
                return false
            }
            return true
        }

    /**
     * Effective status accounting for real-time expiration timestamp.
     */
    val effectiveStatus: EntitlementStatus
        get() {
            if (status == EntitlementStatus.REVOKED) return EntitlementStatus.REVOKED
            val expiry = expiresAt
            if (expiry != null && System.currentTimeMillis() > expiry) {
                return EntitlementStatus.EXPIRED
            }
            return EntitlementStatus.ACTIVE
        }

    /**
     * Determines whether this entitlement covers access to the given subject.
     * - Returns true if the entitlement is active and covers all subjects for this exam (WAEC/NECO).
     * - Returns true if the subject is part of the multi-subject combination (JAMB 4 subjects).
     * - Returns true if this matches the single subjectType (legacy entitlements).
     */
    fun coversSubject(subject: SubjectType): Boolean {
        if (!isCurrentlyActive) return false
        if (isExamLevel) return true
        if (subjects.isNotEmpty()) {
            return subjects.contains(subject)
        }
        return subjectType == subject
    }

    /**
     * Returns a human-readable summary of the subjects covered.
     */
    fun getSubjectsSummary(): String {
        return when {
            isExamLevel -> "All Configured Subjects"
            subjects.isNotEmpty() -> "${subjects.size} Subjects: ${subjects.joinToString { it.displayName }}"
            else -> subjectType.displayName
        }
    }
}
