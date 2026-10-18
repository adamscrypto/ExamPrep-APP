package com.success.examprep.data.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Service interface for querying administrator authorization roles from Firestore.
 */
interface AdminRoleRepository {
    /**
     * Checks if the given [uid] corresponds to an active administrator in Firestore `admin_roles/{uid}`.
     *
     * Rules:
     * 1. Query document `admin_roles/{uid}`.
     * 2. Confirm the document exists.
     * 3. Confirm `active` field is Boolean true.
     * 4. Return false if missing, inactive, or on any Firestore error.
     */
    suspend fun isUserActiveAdmin(uid: String): Boolean
}

/**
 * Production implementation of [AdminRoleRepository] querying Cloud Firestore `admin_roles/{uid}`.
 */
class FirestoreAdminRoleRepository(
    private val firestoreProvider: () -> FirebaseFirestore?
) : AdminRoleRepository {

    override suspend fun isUserActiveAdmin(uid: String): Boolean {
        if (uid.isBlank()) return false
        val firestore = firestoreProvider() ?: return false
        return try {
            val snapshot = firestore.collection(COLLECTION_ADMIN_ROLES)
                .document(uid)
                .get()
                .await()
            if (!snapshot.exists()) {
                Log.d(TAG, "Admin role document missing for UID: $uid")
                false
            } else {
                val activeValue = snapshot.get(FIELD_ACTIVE)
                // Strictly require Boolean true
                val isActive = (activeValue is Boolean) && activeValue == true
                Log.d(TAG, "Admin role for UID $uid evaluated to active=$isActive (raw=$activeValue)")
                isActive
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to verify admin role in Firestore for UID: $uid", e)
            // Any Firestore permission or network error must deny admin privileges
            false
        }
    }

    companion object {
        private const val TAG = "AdminRoleRepository"
        const val COLLECTION_ADMIN_ROLES = "admin_roles"
        const val FIELD_ACTIVE = "active"

        fun create(context: Context): FirestoreAdminRoleRepository {
            return FirestoreAdminRoleRepository {
                try {
                    if (FirebaseApp.getApps(context).isNotEmpty()) {
                        FirebaseFirestore.getInstance()
                    } else {
                        null
                    }
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }
}
