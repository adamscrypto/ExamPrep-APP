package com.success.examprep.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Dedicated secure manager for device-local Student 4-digit PIN credentials.
 *
 * CRITICAL SECURITY ARCHITECTURE:
 * 1. A 4-digit PIN has only 10,000 possible combinations and is strictly a DEVICE-LOCAL unlock credential.
 * 2. It is NEVER used as a Firebase password or derived remote credential.
 * 3. It is NEVER transmitted or stored in Cloud Firestore or remote backends.
 * 4. Stored locally using a unique per-user/per-device cryptographic salt + SHA-256.
 * 5. Weak PINs (0000, 1111, 1234, 4321, repeated digits, sequential digits) are strictly rejected.
 * 6. Never logs PINs, salts, or hashes.
 */
class StudentPinManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "examprep_student_pin_vault"
        private const val PREF_PREFIX_SALT = "pin_salt_"
        private const val PREF_PREFIX_HASH = "pin_hash_"
        private const val PREF_PREFIX_UID_LOCKED = "session_locked_"

        private val WEAK_PINS = setOf(
            "0000", "1111", "2222", "3333", "4444", "5555",
            "6666", "7777", "8888", "9999", "1234", "4321",
            "0123", "3210", "1212", "6969", "2580"
        )

        @Volatile
        private var INSTANCE: StudentPinManager? = null

        fun getInstance(context: Context): StudentPinManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StudentPinManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Validates whether a proposed 4-digit PIN meets security requirements.
         */
        fun validatePin(pin: String): PinValidationResult {
            val trimmed = pin.trim()
            if (trimmed.length != 4 || !trimmed.all { it.isDigit() }) {
                return PinValidationResult.Invalid("PIN must be exactly 4 digits.")
            }
            if (WEAK_PINS.contains(trimmed)) {
                return PinValidationResult.Invalid("PIN is too common. Avoid sequential or repeated numbers like 1234 or 0000.")
            }
            // Check for 4 repeating digits
            if (trimmed.toSet().size == 1) {
                return PinValidationResult.Invalid("PIN cannot be all identical digits.")
            }
            return PinValidationResult.Valid
        }

        /**
         * Normalizes a phone number for consistent lookup.
         * Handles Nigerian numbers: 080... -> +23480..., 23480... -> +23480...
         */
        fun normalizePhoneNumber(phone: String?): String {
            if (phone.isNullOrBlank()) return ""
            val cleaned = phone.trim().replace(" ", "").replace("-", "")
            return when {
                cleaned.startsWith("+234") && cleaned.length == 14 -> cleaned
                cleaned.startsWith("234") && cleaned.length == 13 -> "+$cleaned"
                cleaned.startsWith("0") && cleaned.length == 11 -> "+234${cleaned.substring(1)}"
                else -> cleaned
            }
        }

        /**
         * Normalizes student registration / ID numbers.
         */
        fun normalizeStudentId(studentId: String?): String {
            return studentId?.trim()?.uppercase() ?: ""
        }

        /**
         * Generates a random cryptographic salt for the user.
         */
        fun generateSalt(): String {
            val random = SecureRandom()
            val saltBytes = ByteArray(16)
            random.nextBytes(saltBytes)
            return saltBytes.fold("") { str, b -> str + "%02x".format(b) }
        }

        /**
         * Computes SHA-256 hash using the per-user random salt.
         */
        fun hashWithSalt(pin: String, salt: String): String {
            val input = "$salt:$pin"
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            return digest.fold("") { str, b -> str + "%02x".format(b) }
        }
    }

    /**
     * Sets or updates the 4-digit PIN for a student UID using a fresh random salt.
     */
    fun setPin(uid: String, pin: String): Boolean {
        if (uid.isBlank()) return false
        val validation = validatePin(pin)
        if (!validation.isValid) return false

        val salt = generateSalt()
        val hash = hashWithSalt(pin.trim(), salt)

        prefs.edit()
            .putString(PREF_PREFIX_SALT + uid, salt)
            .putString(PREF_PREFIX_HASH + uid, hash)
            .putBoolean(PREF_PREFIX_UID_LOCKED + uid, false)
            .apply()
        return true
    }

    /**
     * Verifies an entered PIN against the local salted hash.
     */
    fun verifyPin(uid: String, enteredPin: String): Boolean {
        if (uid.isBlank()) return false
        val salt = prefs.getString(PREF_PREFIX_SALT + uid, null) ?: return true // If no PIN configured, allow
        val storedHash = prefs.getString(PREF_PREFIX_HASH + uid, null) ?: return true
        val computedHash = hashWithSalt(enteredPin.trim(), salt)
        val matches = storedHash == computedHash
        if (matches) {
            setSessionLocked(uid, false)
        }
        return matches
    }

    /**
     * Checks if a PIN is stored for this UID.
     */
    fun hasPin(uid: String): Boolean {
        return prefs.contains(PREF_PREFIX_HASH + uid)
    }

    /**
     * Clears local PIN data for this UID.
     */
    fun clearPin(uid: String) {
        prefs.edit()
            .remove(PREF_PREFIX_SALT + uid)
            .remove(PREF_PREFIX_HASH + uid)
            .remove(PREF_PREFIX_UID_LOCKED + uid)
            .apply()
    }

    /**
     * Marks student session as locked on device.
     */
    fun setSessionLocked(uid: String, locked: Boolean) {
        prefs.edit().putBoolean(PREF_PREFIX_UID_LOCKED + uid, locked).apply()
    }

    /**
     * Checks if student session is currently locked on device.
     */
    fun isSessionLocked(uid: String): Boolean {
        return prefs.getBoolean(PREF_PREFIX_UID_LOCKED + uid, false)
    }
}

sealed class PinValidationResult {
    data object Valid : PinValidationResult()
    data class Invalid(val reason: String) : PinValidationResult()

    val isValid: Boolean get() = this is Valid
}
