package com.success.examprep.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.success.examprep.data.model.CbtResult
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentPerformanceStats
import com.success.examprep.data.model.TestHistoryRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface TestHistoryRepository {
    fun getHistoryForUser(uid: String): Flow<List<TestHistoryRecord>>
    suspend fun saveTestResult(uid: String, result: CbtResult): TestHistoryRecord
    suspend fun getStatsForUser(uid: String): StudentPerformanceStats
    suspend fun clearHistory(uid: String)
}

/**
 * Local persistent implementation of TestHistoryRepository.
 * Stores records safely in SharedPreferences JSON structures per student UID.
 */
class LocalTestHistoryRepository(context: Context) : TestHistoryRepository {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("examprep_history_prefs", Context.MODE_PRIVATE)

    private val _recordsFlow = MutableStateFlow<Map<String, List<TestHistoryRecord>>>(loadAllFromPrefs())

    override fun getHistoryForUser(uid: String): Flow<List<TestHistoryRecord>> {
        return _recordsFlow.map { map ->
            map[uid] ?: emptyList()
        }
    }

    override suspend fun saveTestResult(uid: String, result: CbtResult): TestHistoryRecord = withContext(Dispatchers.IO) {
        val record = TestHistoryRecord(
            id = "test_" + UUID.randomUUID().toString().take(8),
            studentUid = uid,
            examType = result.examType,
            subjectId = result.subject.id,
            subjectName = result.subject.name,
            scorePercentage = result.scorePercentage,
            correctCount = result.correctCount,
            wrongCount = result.wrongCount,
            unansweredCount = result.unansweredCount,
            totalQuestions = result.accessibleQuestionsCount,
            timeSpentSeconds = result.timeSpentSeconds,
            timestampMillis = System.currentTimeMillis(),
            isFreePracticeMode = result.isFreePracticeMode
        )

        val currentMap = _recordsFlow.value.toMutableMap()
        val userList = (currentMap[uid] ?: emptyList()).toMutableList()
        // Add to top of list
        userList.add(0, record)
        currentMap[uid] = userList
        _recordsFlow.value = currentMap

        persistUserRecords(uid, userList)
        record
    }

    override suspend fun getStatsForUser(uid: String): StudentPerformanceStats = withContext(Dispatchers.IO) {
        val records = _recordsFlow.value[uid] ?: emptyList()
        if (records.isEmpty()) {
            return@withContext StudentPerformanceStats()
        }

        val totalTests = records.size
        val averageScore = records.map { it.scorePercentage }.average().toInt()
        val highestScore = records.maxOfOrNull { it.scorePercentage } ?: 0
        val totalQuestionsAnswered = records.sumOf { it.totalQuestions - it.unansweredCount }
        val totalCorrect = records.sumOf { it.correctCount }

        // Find subject with highest average score
        val subjectScores = records.groupBy { it.subjectName }
            .mapValues { (_, recs) -> recs.map { it.scorePercentage }.average() }
        val bestSubject = subjectScores.maxByOrNull { it.value }?.key ?: "None yet"

        StudentPerformanceStats(
            totalTests = totalTests,
            averageScore = averageScore,
            highestScore = highestScore,
            totalQuestionsAnswered = totalQuestionsAnswered,
            totalCorrect = totalCorrect,
            bestSubject = bestSubject
        )
    }

    override suspend fun clearHistory(uid: String) = withContext(Dispatchers.IO) {
        val currentMap = _recordsFlow.value.toMutableMap()
        currentMap.remove(uid)
        _recordsFlow.value = currentMap
        prefs.edit().remove("history_$uid").apply()
    }

    private fun persistUserRecords(uid: String, list: List<TestHistoryRecord>) {
        val jsonArray = JSONArray()
        list.forEach { record ->
            val obj = JSONObject().apply {
                put("id", record.id)
                put("studentUid", record.studentUid)
                put("examType", record.examType.name)
                put("subjectId", record.subjectId)
                put("subjectName", record.subjectName)
                put("scorePercentage", record.scorePercentage)
                put("correctCount", record.correctCount)
                put("wrongCount", record.wrongCount)
                put("unansweredCount", record.unansweredCount)
                put("totalQuestions", record.totalQuestions)
                put("timeSpentSeconds", record.timeSpentSeconds)
                put("timestampMillis", record.timestampMillis)
                put("isFreePracticeMode", record.isFreePracticeMode)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("history_$uid", jsonArray.toString()).apply()
    }

    private fun loadAllFromPrefs(): Map<String, List<TestHistoryRecord>> {
        val map = mutableMapOf<String, List<TestHistoryRecord>>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("history_") && value is String) {
                val uid = key.removePrefix("history_")
                try {
                    val jsonArray = JSONArray(value)
                    val list = mutableListOf<TestHistoryRecord>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val examStr = obj.optString("examType", ExamType.JAMB.name)
                        val examType = try { ExamType.valueOf(examStr) } catch (_: Exception) { ExamType.JAMB }

                        list.add(
                            TestHistoryRecord(
                                id = obj.optString("id", UUID.randomUUID().toString()),
                                studentUid = obj.optString("studentUid", uid),
                                examType = examType,
                                subjectId = obj.optString("subjectId", "mathematics"),
                                subjectName = obj.optString("subjectName", "Mathematics"),
                                scorePercentage = obj.optInt("scorePercentage", 0),
                                correctCount = obj.optInt("correctCount", 0),
                                wrongCount = obj.optInt("wrongCount", 0),
                                unansweredCount = obj.optInt("unansweredCount", 0),
                                totalQuestions = obj.optInt("totalQuestions", 20),
                                timeSpentSeconds = obj.optLong("timeSpentSeconds", 0L),
                                timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                                isFreePracticeMode = obj.optBoolean("isFreePracticeMode", true)
                            )
                        )
                    }
                    map[uid] = list
                } catch (_: Exception) {
                    // skip corrupted
                }
            }
        }
        return map
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalTestHistoryRepository? = null

        fun getInstance(context: Context): LocalTestHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalTestHistoryRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Concrete implementation of TestHistoryRepository persisting CBT test records
 * to Cloud Firestore (`cbtHistory/{historyId}`) with local offline preservation.
 */
class FirestoreTestHistoryRepository(
    private val context: Context,
    private val localRepo: LocalTestHistoryRepository = LocalTestHistoryRepository.getInstance(context)
) : TestHistoryRepository by localRepo {

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

    override suspend fun saveTestResult(uid: String, result: CbtResult): TestHistoryRecord = withContext(Dispatchers.IO) {
        val record = localRepo.saveTestResult(uid, result)
        val db = getFirestore()
        if (db != null) {
            try {
                val data = hashMapOf<String, Any?>(
                    "id" to record.id,
                    "studentUid" to uid,
                    "examType" to record.examType.name,
                    "subjectId" to record.subjectId,
                    "subjectName" to record.subjectName,
                    "scorePercentage" to record.scorePercentage,
                    "correctCount" to record.correctCount,
                    "wrongCount" to record.wrongCount,
                    "unansweredCount" to record.unansweredCount,
                    "totalQuestions" to record.totalQuestions,
                    "timeSpentSeconds" to record.timeSpentSeconds,
                    "timestampMillis" to record.timestampMillis,
                    "isFreePracticeMode" to record.isFreePracticeMode,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("cbtHistory")
                    .document(record.id)
                    .set(data)
                    .await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }
        record
    }

    companion object {
        @Volatile
        private var INSTANCE: FirestoreTestHistoryRepository? = null

        fun getInstance(context: Context): FirestoreTestHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreTestHistoryRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
