package com.success.examprep.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.success.examprep.data.model.AccessEntitlement
import com.success.examprep.data.model.AccessType
import com.success.examprep.data.model.EntitlementStatus
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.SubjectType
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Repository interface for managing student access entitlements.
 */
interface EntitlementRepository {
    fun getAllEntitlementsFlow(): Flow<List<AccessEntitlement>>
    suspend fun getAllEntitlements(): List<AccessEntitlement>
    suspend fun getEntitlementsForStudent(studentUid: String): List<AccessEntitlement>

    suspend fun grantManualAccess(
        studentUid: String,
        studentEmail: String,
        studentName: String,
        examType: ExamType,
        subjectType: SubjectType,
        grantedByAdmin: String,
        reason: String = "",
        expiresAt: Long? = null
    ): Result<AccessEntitlement>

    suspend fun revokeAccess(
        entitlementId: String,
        revokedByAdmin: String,
        reason: String = ""
    ): Result<Unit>

    suspend fun hasActiveAccess(
        studentUid: String,
        examType: ExamType,
        subjectType: SubjectType
    ): Boolean

    fun hasActiveEntitlementSync(
        studentUid: String,
        examType: ExamType,
        subjectType: SubjectType
    ): Boolean

    suspend fun refreshEntitlements()
}

/**
 * Concrete implementation supporting both Firestore `access_entitlements` collection
 * and persistent local caching for 100% offline availability.
 */
class DefaultEntitlementRepository(
    private val context: Context
) : EntitlementRepository {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("examprep_entitlements_prefs", Context.MODE_PRIVATE)

    private val _entitlements = MutableStateFlow<List<AccessEntitlement>>(emptyList())

    init {
        val cached = loadFromPrefs()
        _entitlements.value = cached
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    override fun getAllEntitlementsFlow(): Flow<List<AccessEntitlement>> = _entitlements.asStateFlow()

    override suspend fun getAllEntitlements(): List<AccessEntitlement> = withContext(Dispatchers.IO) {
        refreshEntitlements()
        _entitlements.value
    }

    override suspend fun getEntitlementsForStudent(studentUid: String): List<AccessEntitlement> = withContext(Dispatchers.IO) {
        _entitlements.value.filter { it.studentUid == studentUid }
    }

    override suspend fun grantManualAccess(
        studentUid: String,
        studentEmail: String,
        studentName: String,
        examType: ExamType,
        subjectType: SubjectType,
        grantedByAdmin: String,
        reason: String,
        expiresAt: Long?
    ): Result<AccessEntitlement> = withContext(Dispatchers.IO) {
        val newEntitlement = AccessEntitlement(
            id = "ent_" + UUID.randomUUID().toString().take(12),
            studentUid = studentUid,
            studentEmail = studentEmail,
            studentName = studentName,
            examType = examType,
            subjectType = subjectType,
            accessType = AccessType.MANUAL_ADMIN,
            status = EntitlementStatus.ACTIVE,
            grantedAt = System.currentTimeMillis(),
            grantedBy = grantedByAdmin.ifBlank { "Administrator" },
            expiresAt = expiresAt,
            reason = reason,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Update in-memory and local cache
        val currentList = _entitlements.value.toMutableList()
        // Deactivate any previous entitlement for this exact student, exam, and subject
        currentList.removeIf {
            it.studentUid == studentUid && it.examType == examType && it.subjectType == subjectType
        }
        currentList.add(0, newEntitlement)
        _entitlements.value = currentList
        saveToPrefs(currentList)

        // Sync with Firestore if available
        val db = getFirestore()
        if (db != null) {
            try {
                val data = mapEntitlementToMap(newEntitlement)
                db.collection(COLLECTION_NAME)
                    .document(newEntitlement.id)
                    .set(data)
                    .await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }

        Result.success(newEntitlement)
    }

    override suspend fun revokeAccess(
        entitlementId: String,
        revokedByAdmin: String,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val currentList = _entitlements.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == entitlementId }
        if (index == -1) {
            return@withContext Result.failure(NoSuchElementException("Entitlement $entitlementId not found"))
        }

        val existing = currentList[index]
        val revoked = existing.copy(
            status = EntitlementStatus.REVOKED,
            reason = if (reason.isNotBlank()) "${existing.reason} | Revoked: $reason" else existing.reason,
            updatedAt = System.currentTimeMillis()
        )
        currentList[index] = revoked
        _entitlements.value = currentList
        saveToPrefs(currentList)

        // Sync with Firestore
        val db = getFirestore()
        if (db != null) {
            try {
                db.collection(COLLECTION_NAME)
                    .document(entitlementId)
                    .update(
                        mapOf(
                            "status" to EntitlementStatus.REVOKED.name,
                            "reason" to revoked.reason,
                            "updatedAt" to revoked.updatedAt
                        )
                    ).await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }

        Result.success(Unit)
    }

    override suspend fun hasActiveAccess(
        studentUid: String,
        examType: ExamType,
        subjectType: SubjectType
    ): Boolean = withContext(Dispatchers.IO) {
        hasActiveEntitlementSync(studentUid, examType, subjectType)
    }

    override fun hasActiveEntitlementSync(
        studentUid: String,
        examType: ExamType,
        subjectType: SubjectType
    ): Boolean {
        val studentEntitlements = _entitlements.value.filter { it.studentUid == studentUid }
        return studentEntitlements.any { entitlement ->
            entitlement.examType == examType &&
                entitlement.subjectType == subjectType &&
                entitlement.isCurrentlyActive
        }
    }

    override suspend fun refreshEntitlements() = withContext(Dispatchers.IO) {
        val db = getFirestore()
        if (db != null) {
            try {
                var snapshot = db.collection(COLLECTION_NAME).get().await()
                if (snapshot.isEmpty) {
                    // Check fallback collection if exists
                    snapshot = db.collection("access_entitlements").get().await()
                }
                if (!snapshot.isEmpty) {
                    val remoteList = snapshot.documents.mapNotNull { doc ->
                        mapDocToEntitlement(doc.data ?: emptyMap(), doc.id)
                    }
                    _entitlements.value = remoteList
                    saveToPrefs(remoteList)
                    return@withContext
                }
            } catch (_: Exception) {
                // Offline fallback
            }
        }
        _entitlements.value = loadFromPrefs()
    }

    private fun saveToPrefs(list: List<AccessEntitlement>) {
        val array = JSONArray()
        list.forEach { ent ->
            val obj = JSONObject().apply {
                put("id", ent.id)
                put("studentUid", ent.studentUid)
                put("studentEmail", ent.studentEmail)
                put("studentName", ent.studentName)
                put("examType", ent.examType.id)
                put("subjectType", ent.subjectType.id)
                put("accessType", ent.accessType.name)
                put("status", ent.status.name)
                put("grantedAt", ent.grantedAt)
                put("grantedBy", ent.grantedBy)
                if (ent.expiresAt != null) put("expiresAt", ent.expiresAt)
                put("reason", ent.reason)
                put("createdAt", ent.createdAt)
                put("updatedAt", ent.updatedAt)
            }
            array.put(obj)
        }
        prefs.edit().putString("entitlements_json", array.toString()).apply()
    }

    private fun loadFromPrefs(): List<AccessEntitlement> {
        val jsonStr = prefs.getString("entitlements_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<AccessEntitlement>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val examType = ExamType.fromId(obj.getString("examType"))
                val subjectType = SubjectType.fromId(obj.getString("subjectType"))
                val accessType = try {
                    AccessType.valueOf(obj.optString("accessType", AccessType.MANUAL_ADMIN.name))
                } catch (_: Exception) {
                    AccessType.MANUAL_ADMIN
                }
                val status = try {
                    EntitlementStatus.valueOf(obj.optString("status", EntitlementStatus.ACTIVE.name))
                } catch (_: Exception) {
                    EntitlementStatus.ACTIVE
                }
                val expiresAt = if (obj.has("expiresAt") && !obj.isNull("expiresAt")) {
                    obj.getLong("expiresAt")
                } else null

                list.add(
                    AccessEntitlement(
                        id = obj.getString("id"),
                        studentUid = obj.getString("studentUid"),
                        studentEmail = obj.optString("studentEmail", ""),
                        studentName = obj.optString("studentName", ""),
                        examType = examType,
                        subjectType = subjectType,
                        accessType = accessType,
                        status = status,
                        grantedAt = obj.optLong("grantedAt", System.currentTimeMillis()),
                        grantedBy = obj.optString("grantedBy", "Administrator"),
                        expiresAt = expiresAt,
                        reason = obj.optString("reason", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mapEntitlementToMap(ent: AccessEntitlement): Map<String, Any?> {
        return mapOf(
            "id" to ent.id,
            "studentUid" to ent.studentUid,
            "studentEmail" to ent.studentEmail,
            "studentName" to ent.studentName,
            "examType" to ent.examType.id,
            "subjectType" to ent.subjectType.id,
            "accessType" to ent.accessType.name,
            "status" to ent.status.name,
            "grantedAt" to ent.grantedAt,
            "grantedBy" to ent.grantedBy,
            "expiresAt" to ent.expiresAt,
            "reason" to ent.reason,
            "createdAt" to ent.createdAt,
            "updatedAt" to ent.updatedAt
        )
    }

    private fun mapDocToEntitlement(data: Map<String, Any>, id: String): AccessEntitlement? {
        return try {
            val studentUid = data["studentUid"] as? String ?: return null
            val examStr = data["examType"] as? String ?: ExamType.JAMB.id
            val subjectStr = data["subjectType"] as? String ?: SubjectType.MATHEMATICS.id
            val accessTypeStr = data["accessType"] as? String ?: AccessType.MANUAL_ADMIN.name
            val statusStr = data["status"] as? String ?: EntitlementStatus.ACTIVE.name

            AccessEntitlement(
                id = id,
                studentUid = studentUid,
                studentEmail = data["studentEmail"] as? String ?: "",
                studentName = data["studentName"] as? String ?: "",
                examType = ExamType.fromId(examStr),
                subjectType = SubjectType.fromId(subjectStr),
                accessType = try { AccessType.valueOf(accessTypeStr) } catch (_: Exception) { AccessType.MANUAL_ADMIN },
                status = try { EntitlementStatus.valueOf(statusStr) } catch (_: Exception) { EntitlementStatus.ACTIVE },
                grantedAt = (data["grantedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                grantedBy = data["grantedBy"] as? String ?: "Administrator",
                expiresAt = (data["expiresAt"] as? Number)?.toLong(),
                reason = data["reason"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val COLLECTION_NAME = "entitlements"

        @Volatile
        private var INSTANCE: DefaultEntitlementRepository? = null

        fun getInstance(context: Context): DefaultEntitlementRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DefaultEntitlementRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
