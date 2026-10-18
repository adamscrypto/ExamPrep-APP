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
    val subjectType: SubjectType,
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
}
