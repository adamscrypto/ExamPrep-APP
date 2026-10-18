package com.success.examprep.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.success.examprep.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Date

/**
 * Repository interface for managing student profiles stored in Cloud Firestore.
 */
interface UserRepository {
    suspend fun createUserProfile(profile: UserProfile): Result<UserProfile>
    suspend fun getUserProfile(userId: String): Result<UserProfile?>
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any?>): Result<Unit>
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    fun getCachedProfile(): UserProfile?
    fun saveCachedProfile(profile: UserProfile)
    fun clearCachedProfile()
}

/**
 * Concrete implementation of UserRepository backed by Cloud Firestore (`users/{uid}`)
 * and secure local caching for offline session preservation.
 *
 * Passwords are NEVER stored, processed, or logged by this repository.
 */
class FirestoreUserRepository(
    private val context: Context
) : UserRepository {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("examprep_user_profile_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CACHED_PROFILE_JSON = "cached_user_profile_json"

        @Volatile
        private var INSTANCE: FirestoreUserRepository? = null

        fun getInstance(context: Context): FirestoreUserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreUserRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
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

    override suspend fun createUserProfile(profile: UserProfile): Result<UserProfile> = withContext(Dispatchers.IO) {
        val uid = profile.uid
        if (uid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("User profile UID cannot be blank."))
        }

        // Save to local cache immediately
        saveCachedProfile(profile)

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                val data = hashMapOf<String, Any?>(
                    "uid" to uid,
                    "fullName" to profile.fullName.trim(),
                    "email" to profile.email.trim(),
                    "phone" to profile.phone?.trim(),
                    "studentId" to profile.studentId?.trim(),
                    "profilePhotoUrl" to profile.profilePhotoUrl,
                    "accountStatus" to profile.accountStatus,
                    "targetExam" to profile.targetExam,
                    "targetYear" to profile.targetYear,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("users")
                    .document(uid)
                    .set(data, SetOptions.merge())
                    .await()

                return@withContext Result.success(profile)
            } catch (e: Exception) {
                // Return success if locally cached, but return result
                return@withContext Result.success(profile)
            }
        }

        Result.success(profile)
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.success(null)
        }

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("users").document(userId).get().await()
                if (snapshot.exists() && snapshot.data != null) {
                    val profile = UserProfile.fromMap(snapshot.data!!, snapshot.id)
                    saveCachedProfile(profile)
                    return@withContext Result.success(profile)
                }
            } catch (_: Exception) {
                // Fall back to local cache if network/firebase error
            }
        }

        val cached = getCachedProfile()
        if (cached?.uid == userId) {
            Result.success(cached)
        } else {
            Result.success(null)
        }
    }

    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any?>): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("User ID cannot be blank."))
        }

        val currentCached = getCachedProfile()
        if (currentCached != null && currentCached.uid == userId) {
            val updated = currentCached.copy(
                fullName = (updates["fullName"] as? String) ?: currentCached.fullName,
                phone = if (updates.containsKey("phone")) updates["phone"] as? String else currentCached.phone,
                targetExam = (updates["targetExam"] as? String) ?: currentCached.targetExam,
                targetYear = (updates["targetYear"] as? Number)?.toInt() ?: currentCached.targetYear,
                updatedAt = Timestamp.now()
            )
            saveCachedProfile(updated)
        }

        val firestore = getFirestore()
        if (firestore != null) {
            try {
                val firestoreUpdates = updates.toMutableMap()
                firestoreUpdates["updatedAt"] = FieldValue.serverTimestamp()
                firestore.collection("users")
                    .document(userId)
                    .update(firestoreUpdates)
                    .await()
            } catch (e: Exception) {
                // Non-fatal if offline
            }
        }

        Result.success(Unit)
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        // Emit cached profile immediately
        val cached = getCachedProfile()
        if (cached?.uid == userId) {
            trySend(cached)
        }

        val firestore = getFirestore()
        if (firestore == null) {
            awaitClose { }
            return@callbackFlow
        }

        val subscription = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                    val profile = UserProfile.fromMap(snapshot.data!!, snapshot.id)
                    saveCachedProfile(profile)
                    trySend(profile)
                }
            }

        awaitClose { subscription.remove() }
    }

    override fun getCachedProfile(): UserProfile? {
        val jsonStr = prefs.getString(KEY_CACHED_PROFILE_JSON, null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            val createdAtMs = if (obj.has("createdAtMs")) obj.getLong("createdAtMs") else null
            val updatedAtMs = if (obj.has("updatedAtMs")) obj.getLong("updatedAtMs") else null

            UserProfile(
                uid = obj.getString("uid"),
                fullName = obj.optString("fullName", ""),
                email = obj.optString("email", ""),
                phone = if (obj.has("phone") && !obj.isNull("phone")) obj.getString("phone") else null,
                studentId = if (obj.has("studentId") && !obj.isNull("studentId")) obj.getString("studentId") else null,
                profilePhotoUrl = if (obj.has("profilePhotoUrl") && !obj.isNull("profilePhotoUrl")) obj.getString("profilePhotoUrl") else null,
                accountStatus = obj.optString("accountStatus", "ACTIVE"),
                targetExam = obj.optString("targetExam", "JAMB"),
                targetYear = obj.optInt("targetYear", 2025),
                createdAt = createdAtMs?.let { Timestamp(Date(it)) },
                updatedAt = updatedAtMs?.let { Timestamp(Date(it)) }
            )
        } catch (_: Exception) {
            null
        }
    }

    override fun saveCachedProfile(profile: UserProfile) {
        val obj = JSONObject().apply {
            put("uid", profile.uid)
            put("fullName", profile.fullName)
            put("email", profile.email)
            if (profile.phone != null) put("phone", profile.phone)
            if (profile.studentId != null) put("studentId", profile.studentId)
            if (profile.profilePhotoUrl != null) put("profilePhotoUrl", profile.profilePhotoUrl)
            put("accountStatus", profile.accountStatus)
            put("targetExam", profile.targetExam)
            put("targetYear", profile.targetYear)
            profile.createdAt?.toDate()?.time?.let { put("createdAtMs", it) }
            profile.updatedAt?.toDate()?.time?.let { put("updatedAtMs", it) }
        }
        prefs.edit().putString(KEY_CACHED_PROFILE_JSON, obj.toString()).apply()
    }

    override fun clearCachedProfile() {
        prefs.edit().remove(KEY_CACHED_PROFILE_JSON).apply()
    }
}
