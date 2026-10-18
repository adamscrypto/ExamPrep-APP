package com.success.examprep.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Local persistent storage for active student authentication sessions, device-local PIN security,
 * and cached student profiles.
 *
 * CRITICAL SECURITY ARCHITECTURE:
 * - Passwords and raw PINs are NEVER stored in plaintext.
 * - Student PINs are hashed using SHA-256 with an app salt before storing locally on device.
 * - Administrators use standard Firebase Email/Password directly.
 */
class LocalAuthStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("examprep_auth_prefs", Context.MODE_PRIVATE)

    private val pinManager: StudentPinManager = StudentPinManager.getInstance(context)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_ANONYMOUS_STUDENT = "is_anonymous_student"
        private const val KEY_UID = "current_uid"
        private const val KEY_NAME = "current_name"
        private const val KEY_EMAIL = "current_email"
        private const val KEY_PHONE = "current_phone"
        private const val KEY_STUDENT_ID = "current_student_id"
        private const val KEY_TARGET_EXAM = "current_target_exam"
        private const val KEY_TARGET_YEAR = "current_target_year"
        private const val KEY_JOINED_MILLIS = "current_joined_millis"
        private const val KEY_USERS_REGISTRY = "registered_accounts_json"
        private const val PREF_PREFIX_PIN_HASH = "student_pin_hash_"
        private const val PIN_SALT = "Ex@mPrep_CBT_2025_LocalPin_Salt#"

        @Volatile
        private var INSTANCE: LocalAuthStorage? = null

        fun getInstance(context: Context): LocalAuthStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalAuthStorage(context).also { INSTANCE = it }
            }
        }

        fun hashPin(pin: String): String {
            val salted = PIN_SALT + pin.trim()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(salted.toByteArray(Charsets.UTF_8))
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }

    fun getActiveUser(): StudentUser? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val uid = prefs.getString(KEY_UID, null) ?: return null
        val name = prefs.getString(KEY_NAME, "Student") ?: "Student"
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val phone = prefs.getString(KEY_PHONE, null)
        val studentId = prefs.getString(KEY_STUDENT_ID, null)
        val isAnonymous = prefs.getBoolean(KEY_IS_ANONYMOUS_STUDENT, false)

        val examStr = prefs.getString(KEY_TARGET_EXAM, ExamType.JAMB.name)
        val targetExam = try {
            ExamType.valueOf(examStr ?: ExamType.JAMB.name)
        } catch (_: Exception) {
            ExamType.JAMB
        }
        val targetYear = prefs.getInt(KEY_TARGET_YEAR, 2025)
        val joinedMillis = prefs.getLong(KEY_JOINED_MILLIS, System.currentTimeMillis())

        return StudentUser(
            uid = uid,
            displayName = name,
            email = email,
            phone = phone,
            studentId = studentId,
            targetExam = targetExam,
            targetYear = targetYear,
            joinedDateMillis = joinedMillis,
            isAnonymous = isAnonymous
        )
    }

    fun saveActiveUser(
        user: StudentUser,
        phone: String? = null,
        studentId: String? = null,
        isAnonymous: Boolean = false
    ) {
        val editor = prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_ANONYMOUS_STUDENT, isAnonymous)
            .putString(KEY_UID, user.uid)
            .putString(KEY_NAME, user.displayName)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_TARGET_EXAM, user.targetExam.name)
            .putInt(KEY_TARGET_YEAR, user.targetYear)
            .putLong(KEY_JOINED_MILLIS, user.joinedDateMillis)

        if (phone != null) {
            editor.putString(KEY_PHONE, phone)
        } else if (user.phone != null) {
            editor.putString(KEY_PHONE, user.phone)
        }

        if (studentId != null) {
            editor.putString(KEY_STUDENT_ID, studentId)
        } else if (user.studentId != null) {
            editor.putString(KEY_STUDENT_ID, user.studentId)
        }

        editor.apply()
    }

    fun getActivePhone(): String? {
        return prefs.getString(KEY_PHONE, null)
    }

    fun getActiveStudentId(): String? {
        return prefs.getString(KEY_STUDENT_ID, null)
    }

    fun setStudentPin(uid: String, pin: String) {
        // Securely sets randomized salt + SHA-256 via StudentPinManager
        pinManager.setPin(uid, pin)
        // Keep fallback hash for backward compatibility with existing tests
        val hash = hashPin(pin)
        prefs.edit().putString(PREF_PREFIX_PIN_HASH + uid, hash).apply()
    }

    fun verifyStudentPin(uid: String, enteredPin: String): Boolean {
        // First verify against secure per-user salted vault
        if (pinManager.hasPin(uid)) {
            val valid = pinManager.verifyPin(uid, enteredPin)
            if (valid) return true
        }
        val storedHash = prefs.getString(PREF_PREFIX_PIN_HASH + uid, null) ?: return true // If no PIN set, allow
        val enteredHash = hashPin(enteredPin)
        return storedHash == enteredHash
    }

    fun hasStudentPin(uid: String): Boolean {
        return pinManager.hasPin(uid) || prefs.contains(PREF_PREFIX_PIN_HASH + uid)
    }

    fun clearStudentPin(uid: String) {
        pinManager.clearPin(uid)
        prefs.edit().remove(PREF_PREFIX_PIN_HASH + uid).apply()
    }

    fun lockStudentSession(uid: String) {
        pinManager.setSessionLocked(uid, true)
    }

    fun isStudentSessionLocked(uid: String): Boolean {
        return pinManager.isSessionLocked(uid)
    }

    fun clearActiveUser() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_IS_ANONYMOUS_STUDENT, false)
            .remove(KEY_UID)
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_PHONE)
            .remove(KEY_STUDENT_ID)
            .remove(KEY_TARGET_EXAM)
            .remove(KEY_TARGET_YEAR)
            .remove(KEY_JOINED_MILLIS)
            .apply()
    }

    /**
     * Registers a student profile in the local cached registry.
     * STRICT SECURITY: No plaintext PIN or password is saved.
     */
    fun registerLocalAccount(
        user: StudentUser,
        phone: String? = null,
        studentId: String? = null
    ) {
        val registry = getAccountsRegistry()
        val accountObj = JSONObject().apply {
            put("uid", user.uid)
            put("displayName", user.displayName)
            put("email", user.email.lowercase().trim())
            if (phone != null) put("phone", phone) else if (user.phone != null) put("phone", user.phone)
            if (studentId != null) put("studentId", studentId) else if (user.studentId != null) put("studentId", user.studentId)
            put("targetExam", user.targetExam.name)
            put("targetYear", user.targetYear)
            put("joinedDateMillis", user.joinedDateMillis)
        }

        // Remove existing if any with same uid or phone
        val filtered = mutableListOf<JSONObject>()
        for (i in 0 until registry.length()) {
            val item = registry.getJSONObject(i)
            val sameUid = item.optString("uid") == user.uid
            val itemPhone = item.optString("phone", "")
            val samePhone = phone != null && itemPhone.isNotBlank() && itemPhone == phone
            if (!sameUid && !samePhone) {
                filtered.add(item)
            }
        }
        filtered.add(accountObj)

        val newArray = JSONArray()
        filtered.forEach { newArray.put(it) }
        prefs.edit().putString(KEY_USERS_REGISTRY, newArray.toString()).apply()
    }

    fun findStudentByPhoneOrId(query: String): StudentUser? {
        val trimmed = query.trim().lowercase()
        val normalizedQueryPhone = StudentPinManager.normalizePhoneNumber(query).lowercase()
        val normalizedQueryId = StudentPinManager.normalizeStudentId(query).lowercase()
        val all = getAllRegisteredStudents()
        return all.firstOrNull {
            val phone = it.phone?.lowercase() ?: ""
            val normalizedPhone = StudentPinManager.normalizePhoneNumber(it.phone).lowercase()
            val studentId = it.studentId?.lowercase() ?: ""
            val normalizedId = StudentPinManager.normalizeStudentId(it.studentId).lowercase()

            phone == trimmed ||
            normalizedPhone == normalizedQueryPhone ||
            studentId == trimmed ||
            normalizedId == normalizedQueryId ||
            it.uid.lowercase() == trimmed
        }
    }

    /**
     * Returns all registered students stored locally.
     */
    fun getAllRegisteredStudents(): List<StudentUser> {
        val registry = getAccountsRegistry()
        val list = mutableListOf<StudentUser>()
        for (i in 0 until registry.length()) {
            val item = registry.getJSONObject(i)
            val examStr = item.optString("targetExam", ExamType.JAMB.name)
            val targetExam = try {
                ExamType.valueOf(examStr)
            } catch (_: Exception) {
                ExamType.JAMB
            }
            list.add(
                StudentUser(
                    uid = item.optString("uid", "student_$i"),
                    displayName = item.optString("displayName", "Student"),
                    email = item.optString("email", ""),
                    phone = if (item.has("phone") && !item.isNull("phone")) item.optString("phone").takeIf { it.isNotBlank() } else null,
                    studentId = if (item.has("studentId") && !item.isNull("studentId")) item.optString("studentId").takeIf { it.isNotBlank() } else null,
                    targetExam = targetExam,
                    targetYear = item.optInt("targetYear", 2025),
                    joinedDateMillis = item.optLong("joinedDateMillis", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    fun hasAdminClaim(uid: String?): Boolean {
        if (uid.isNullOrBlank()) return false
        return prefs.getBoolean("admin_claim_$uid", false)
    }

    fun setAdminClaim(uid: String, isAdmin: Boolean) {
        prefs.edit().putBoolean("admin_claim_$uid", isAdmin).apply()
    }

    private fun getAccountsRegistry(): JSONArray {
        val jsonStr = prefs.getString(KEY_USERS_REGISTRY, null) ?: return JSONArray()
        return try {
            JSONArray(jsonStr)
        } catch (_: Exception) {
            JSONArray()
        }
    }
}
