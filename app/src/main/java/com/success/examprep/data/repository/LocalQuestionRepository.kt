package com.success.examprep.data.repository

import com.success.examprep.data.local.LocalQuestionDataSource
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.SubjectType
import kotlin.random.Random

/**
 * Concrete implementation of QuestionRepository backed by local offline data sources.
 * Designed to be easily swapped with a RemoteQuestionRepository or RoomDatabaseRepository in future stages.
 */
class LocalQuestionRepository(
    private val dataSource: LocalQuestionDataSource = LocalQuestionDataSource()
) : QuestionRepository {

    override fun getQuestions(examType: ExamType, subject: SubjectType): List<Question> {
        return dataSource.getQuestions(examType, subject)
    }

    override fun getQuestionsByTopic(examType: ExamType, subject: SubjectType, topic: String): List<Question> {
        return dataSource.getQuestionsByTopic(examType, subject, topic)
    }

    override fun getQuestionsByYear(examType: ExamType, subject: SubjectType, year: Int): List<Question> {
        return dataSource.getQuestionsByYear(examType, subject, year)
    }

    override fun getQuestionsByDifficulty(
        examType: ExamType,
        subject: SubjectType,
        difficulty: Difficulty
    ): List<Question> {
        return dataSource.getQuestionsByDifficulty(examType, subject, difficulty)
    }

    override fun getFilteredQuestions(
        examType: ExamType,
        subject: SubjectType,
        year: Int?,
        topic: String?,
        difficulty: Difficulty?
    ): List<Question> {
        return dataSource.getFilteredQuestions(examType, subject, year, topic, difficulty)
    }

    override fun getQuestionById(id: Int): Question? {
        return dataSource.getQuestionById(id)
    }

    override fun getRandomQuestions(
        examType: ExamType,
        subject: SubjectType,
        numberOfQuestions: Int
    ): List<Question> {
        val available = dataSource.getQuestions(examType, subject)
        if (available.isEmpty()) return emptyList()

        val takeCount = numberOfQuestions.coerceIn(1, available.size)
        // Shuffle to randomize, guarantee no duplicates by sampling distinct set
        return available.shuffled(Random(System.currentTimeMillis())).take(takeCount)
    }

    override fun getAvailableYears(examType: ExamType, subject: SubjectType): List<Int> {
        return dataSource.getQuestions(examType, subject)
            .map { it.year }
            .distinct()
            .sortedDescending()
    }

    override fun getAvailableTopics(examType: ExamType, subject: SubjectType): List<String> {
        return dataSource.getQuestions(examType, subject)
            .map { it.topic }
            .distinct()
            .sorted()
    }

    override fun getAllQuestions(): List<Question> {
        return dataSource.getAllQuestions(includeInactive = false)
    }

    override fun getAllQuestions(includeInactive: Boolean): List<Question> {
        return dataSource.getAllQuestions(includeInactive = includeInactive)
    }

    override suspend fun addQuestion(question: Question): Result<Question> {
        return try {
            val added = dataSource.addQuestion(question)
            Result.success(added)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuestion(question: Question): Result<Question> {
        return try {
            val updated = dataSource.updateQuestion(question)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setQuestionActive(id: Int, isActive: Boolean, updatedBy: String): Result<Unit> {
        return try {
            val ok = dataSource.setQuestionActive(id, isActive, updatedBy)
            if (ok) Result.success(Unit) else Result.failure(NoSuchElementException("Question $id not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteQuestion(id: Int): Result<Unit> {
        return try {
            val ok = dataSource.deleteQuestion(id)
            if (ok) Result.success(Unit) else Result.failure(NoSuchElementException("Question $id not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun bulkAddQuestions(
        questions: List<Question>,
        onProgress: ((processed: Int, total: Int) -> Unit)?
    ): Result<Int> {
        return try {
            val addedCount = dataSource.bulkAddQuestions(questions)
            onProgress?.invoke(addedCount, questions.size)
            Result.success(addedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun generateNextId(): Int {
        return dataSource.generateNextId()
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalQuestionRepository? = null

        fun getInstance(): LocalQuestionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalQuestionRepository().also { INSTANCE = it }
            }
        }
    }
}
