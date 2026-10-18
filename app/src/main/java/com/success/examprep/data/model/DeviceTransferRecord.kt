package com.success.examprep.data.model

import com.google.firebase.Timestamp

/**
 * Migration status for controlled admin-authorized device transfers.
 */
enum class MigrationStatus {
    PENDING,
    COMPLETED,
    EXPIRED,
    CANCELLED
}

/**
 * Audit record representing an Admin-authorized Student Device Transfer.
 *
 * CRITICAL ARCHITECTURAL CONSTRAINTS:
 * 1. Firebase Anonymous Authentication cannot transfer an existing anonymous UID to another physical device.
 * 2. We do NOT fake UIDs or copy arbitrary credentials across devices.
 * 3. The admin authorizes a transfer by issuing a secure one-time 6-character transfer token
 *    linking oldStudentUid -> target new anonymous session.
 * 4. When completed by the admin or student on the target device:
 *    - The new anonymous session UID is bound to the student profile.
 *    - All existing entitlements are re-assigned to the new UID.
 *    - The previous anonymous account profile is marked as 'TRANSFERRED' / superseded.
 * 5. Neither plain PINs nor PIN hashes are stored in this record or Firestore.
 */
data class DeviceTransferRecord(
    val id: String = "",
    val oldStudentUid: String = "",
    val studentName: String = "",
    val studentPhone: String = "",
    val studentId: String? = null,
    val newStudentUid: String? = null,
    val transferToken: String = "",
    val status: MigrationStatus = MigrationStatus.PENDING,
    val authorizedByAdmin: String = "",
    val authorizedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val reason: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "oldStudentUid" to oldStudentUid,
        "studentName" to studentName,
        "studentPhone" to studentPhone,
        "studentId" to studentId,
        "newStudentUid" to newStudentUid,
        "transferToken" to transferToken,
        "status" to status.name,
        "authorizedByAdmin" to authorizedByAdmin,
        "authorizedAt" to authorizedAt,
        "completedAt" to completedAt,
        "reason" to reason
    )

    companion object {
        fun fromMap(map: Map<String, Any?>, documentId: String): DeviceTransferRecord {
            val statusStr = map["status"] as? String ?: MigrationStatus.PENDING.name
            val status = try { MigrationStatus.valueOf(statusStr) } catch (_: Exception) { MigrationStatus.PENDING }
            return DeviceTransferRecord(
                id = (map["id"] as? String) ?: documentId,
                oldStudentUid = (map["oldStudentUid"] as? String) ?: "",
                studentName = (map["studentName"] as? String) ?: "",
                studentPhone = (map["studentPhone"] as? String) ?: "",
                studentId = map["studentId"] as? String,
                newStudentUid = map["newStudentUid"] as? String,
                transferToken = (map["transferToken"] as? String) ?: "",
                status = status,
                authorizedByAdmin = (map["authorizedByAdmin"] as? String) ?: "",
                authorizedAt = (map["authorizedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                completedAt = (map["completedAt"] as? Number)?.toLong(),
                reason = (map["reason"] as? String) ?: ""
            )
        }
    }
}
