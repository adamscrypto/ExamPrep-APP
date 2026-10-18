package com.success.examprep.data.repository

import android.content.Context
import com.success.examprep.data.model.AnswerVerificationStatus
import com.success.examprep.data.model.ContentPermissionStatus
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.QuestionSourceType
import com.success.examprep.data.model.QuestionVerificationStatus
import com.success.examprep.data.model.SubjectType
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * QuestionRepository combining Cloud Firestore remote question sync with local caching.
 * Ensures questions can be fetched and modified via Firestore while preserving 100% offline
 * CBT capability.
 * Stage 7A: Synchronizes complete provenance and QC metadata to Firestore questions collection.
 */
class FirestoreQuestionRepository(
    private val context: Context,
    private val localRepo: LocalQuestionRepository = LocalQuestionRepository.getInstance()
) : QuestionRepository by localRepo {

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

    override suspend fun addQuestion(question: Question): Result<Question> = withContext(Dispatchers.IO) {
        val localResult = localRepo.addQuestion(question)
        if (localResult.isFailure) return@withContext localResult

        val db = getFirestore()
        if (db != null) {
            try {
                val data = mapQuestionToFirestore(question)
                db.collection("questions")
                    .document(question.id.toString())
                    .set(data)
                    .await()
            } catch (_: Exception) {
                // Keep local modification intact if offline / Firestore unreachable
            }
        }
        Result.success(question)
    }

    override suspend fun updateQuestion(question: Question): Result<Question> = withContext(Dispatchers.IO) {
        val localResult = localRepo.updateQuestion(question)
        if (localResult.isFailure) return@withContext localResult

        val db = getFirestore()
        if (db != null) {
            try {
                val data = mapQuestionToFirestore(question)
                db.collection("questions")
                    .document(question.id.toString())
                    .set(data)
                    .await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }
        Result.success(question)
    }

    override suspend fun setQuestionActive(id: Int, isActive: Boolean, updatedBy: String): Result<Unit> = withContext(Dispatchers.IO) {
        val localResult = localRepo.setQuestionActive(id, isActive, updatedBy)
        if (localResult.isFailure) return@withContext localResult

        val db = getFirestore()
        if (db != null) {
            try {
                db.collection("questions")
                    .document(id.toString())
                    .update(
                        mapOf(
                            "isActive" to isActive,
                            "updatedAt" to System.currentTimeMillis(),
                            "updatedBy" to updatedBy
                        )
                    ).await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }
        Result.success(Unit)
    }

    override suspend fun deleteQuestion(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val localResult = localRepo.deleteQuestion(id)
        if (localResult.isFailure) return@withContext localResult

        val db = getFirestore()
        if (db != null) {
            try {
                db.collection("questions")
                    .document(id.toString())
                    .delete()
                    .await()
            } catch (_: Exception) {
                // Offline fallback
            }
        }
        Result.success(Unit)
    }

    override suspend fun bulkAddQuestions(
        questions: List<Question>,
        onProgress: ((processed: Int, total: Int) -> Unit)?
    ): Result<Int> = withContext(Dispatchers.IO) {
        val localResult = localRepo.bulkAddQuestions(questions, onProgress)
        val db = getFirestore()
        if (db != null && questions.isNotEmpty()) {
            try {
                val chunkSize = 300 // Safe chunk under Firestore batch limit of 500
                val chunks = questions.chunked(chunkSize)
                var processed = 0
                for ((_, chunk) in chunks.withIndex()) {
                    val batch = db.batch()
                    for (q in chunk) {
                        val docRef = db.collection("questions").document(q.id.toString())
                        batch.set(docRef, mapQuestionToFirestore(q))
                    }
                    batch.commit().await()
                    processed += chunk.size
                    onProgress?.invoke(processed, questions.size)
                }
            } catch (_: Exception) {
                // Offline fallback - localRepo has received questions
            }
        }
        localResult
    }

    private fun mapQuestionToFirestore(q: Question): Map<String, Any?> {
        return mapOf(
            "id" to q.id,
            "examType" to q.examType.id,
            "subject" to q.subject.id,
            "year" to q.year,
            "topic" to q.topic,
            "questionText" to q.questionText,
            "optionA" to q.optionA,
            "optionB" to q.optionB,
            "optionC" to q.optionC,
            "optionD" to q.optionD,
            "correctAnswer" to q.correctAnswer.name,
            "explanation" to q.explanation,
            "difficulty" to q.difficulty.name,
            "isFree" to q.isFree,
            "isActive" to q.isActive,
            "createdAt" to q.createdAt,
            "createdBy" to q.createdBy,
            "updatedAt" to q.updatedAt,
            "updatedBy" to q.updatedBy,
            // Stage 7A metadata
            "sourceType" to q.sourceType.name,
            "sourceTitle" to q.sourceTitle,
            "sourceOrganization" to q.sourceOrganization,
            "sourceReference" to q.sourceReference,
            "sourceUrl" to q.sourceUrl,
            "sourceYear" to q.sourceYear,
            "sourcePage" to q.sourcePage,
            "originalQuestionNumber" to q.originalQuestionNumber,
            "verificationStatus" to q.verificationStatus.name,
            "answerVerificationStatus" to q.answerVerificationStatus.name,
            "verifiedBy" to q.verifiedBy,
            "verifiedAt" to q.verifiedAt,
            "reviewNotes" to q.reviewNotes,
            "contentPermissionStatus" to q.contentPermissionStatus.name
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: FirestoreQuestionRepository? = null

        fun getInstance(context: Context): FirestoreQuestionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreQuestionRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
