package com.success.examprep.data.repository

import android.content.Context
import com.success.examprep.data.auth.LocalAuthStorage
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository interface for managing registered student accounts in ExamPrep CBT,
 * including Admin-assisted Device Transfer and PIN reset capabilities.
 */
interface StudentRepository {
    fun getStudentsFlow(): Flow<List<StudentUser>>
    suspend fun getAllStudents(): List<StudentUser>
    suspend fun searchStudents(query: String): List<StudentUser>
    suspend fun getStudentByUid(uid: String): StudentUser?
    suspend fun refreshStudents()

    /**
     * Admin-Assisted PIN Reset: Sets a new 4-digit PIN for a verified student on this device.
     */
    suspend fun resetStudentPinByAdmin(uid: String, newPin: String): Result<Unit>

    /**
     * Admin-Assisted Device Transfer: Transfers an existing student profile into this device's local registry
     * and initializes the local hashed PIN.
     */
    suspend fun transferStudentToDeviceByAdmin(
        student: StudentUser,
        initialPin: String
    ): Result<Unit>
}

/**
 * Concrete implementation supporting both Firestore and local offline student registry.
 */
class DefaultStudentRepository(
    private val context: Context,
    private val localStorage: LocalAuthStorage = LocalAuthStorage.getInstance(context)
) : StudentRepository {

    private val _students = MutableStateFlow<List<StudentUser>>(emptyList())

    init {
        _students.value = localStorage.getAllRegisteredStudents()
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

    override fun getStudentsFlow(): Flow<List<StudentUser>> = _students.asStateFlow()

    override suspend fun getAllStudents(): List<StudentUser> = withContext(Dispatchers.IO) {
        refreshStudents()
        _students.value
    }

    override suspend fun searchStudents(query: String): List<StudentUser> = withContext(Dispatchers.IO) {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) {
            return@withContext _students.value
        }
        _students.value.filter { student ->
            student.displayName.lowercase().contains(trimmed) ||
                student.email.lowercase().contains(trimmed) ||
                (student.phone != null && student.phone.contains(trimmed)) ||
                (student.studentId != null && student.studentId.lowercase().contains(trimmed)) ||
                student.uid.lowercase().contains(trimmed)
        }
    }

    override suspend fun getStudentByUid(uid: String): StudentUser? = withContext(Dispatchers.IO) {
        _students.value.firstOrNull { it.uid == uid }
            ?: localStorage.getAllRegisteredStudents().firstOrNull { it.uid == uid }
    }

    override suspend fun refreshStudents() = withContext(Dispatchers.IO) {
        val db = getFirestore()
        if (db != null) {
            try {
                val snapshot = db.collection("users").get().await()
                if (!snapshot.isEmpty) {
                    val remoteList = snapshot.documents.mapNotNull { doc ->
                        val uid = doc.getString("uid") ?: doc.id
                        val name = doc.getString("fullName") ?: doc.getString("displayName") ?: "Student"
                        val email = doc.getString("email") ?: ""
                        val phone = doc.getString("phone")
                        val studentId = doc.getString("studentId")
                        val examStr = doc.getString("targetExam") ?: ExamType.JAMB.name
                        val targetExam = try { ExamType.valueOf(examStr) } catch (_: Exception) { ExamType.JAMB }
                        val targetYear = doc.getLong("targetYear")?.toInt() ?: 2025
                        val createdAtTimestamp = doc.getTimestamp("createdAt")
                        val joined = createdAtTimestamp?.toDate()?.time
                            ?: doc.getLong("joinedDateMillis")
                            ?: doc.getLong("createdAt")
                            ?: System.currentTimeMillis()

                        StudentUser(
                            uid = uid,
                            displayName = name,
                            email = email,
                            phone = phone,
                            studentId = studentId,
                            targetExam = targetExam,
                            targetYear = targetYear,
                            joinedDateMillis = joined,
                            isAnonymous = email.isBlank()
                        )
                    }
                    _students.value = remoteList
                    return@withContext
                }
            } catch (_: Exception) {
                // Offline fallback
            }
        }
        _students.value = localStorage.getAllRegisteredStudents()
    }

    override suspend fun resetStudentPinByAdmin(uid: String, newPin: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
            return@withContext Result.failure(IllegalArgumentException("New PIN must be exactly 4 digits."))
        }
        localStorage.setStudentPin(uid, newPin)
        Result.success(Unit)
    }

    override suspend fun transferStudentToDeviceByAdmin(
        student: StudentUser,
        initialPin: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (initialPin.length != 4 || !initialPin.all { it.isDigit() }) {
            return@withContext Result.failure(IllegalArgumentException("Transfer PIN must be exactly 4 digits."))
        }
        localStorage.registerLocalAccount(student, phone = student.phone, studentId = student.studentId)
        localStorage.setStudentPin(student.uid, initialPin)
        refreshStudents()
        Result.success(Unit)
    }

    companion object {
        @Volatile
        private var INSTANCE: DefaultStudentRepository? = null

        fun getInstance(context: Context): DefaultStudentRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DefaultStudentRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
