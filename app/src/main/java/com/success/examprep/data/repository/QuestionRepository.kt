package com.success.examprep.data.repository

import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.SubjectType

/**
 * QuestionRepository abstraction for retrieving and filtering CBT questions.
 * Decouples the UI and ViewModels from underlying local/mock or remote data storage.
 */
interface QuestionRepository {

    /**
     * Retrieves all questions for a specific exam board and subject.
     */
    fun getQuestions(examType: ExamType, subject: SubjectType): List<Question>

    /**
     * Retrieves questions filtered by syllabus topic.
     */
    fun getQuestionsByTopic(examType: ExamType, subject: SubjectType, topic: String): List<Question>

    /**
     * Retrieves questions filtered by exam year.
     */
    fun getQuestionsByYear(examType: ExamType, subject: SubjectType, year: Int): List<Question>

    /**
     * Retrieves questions filtered by difficulty level.
     */
    fun getQuestionsByDifficulty(examType: ExamType, subject: SubjectType, difficulty: Difficulty): List<Question>

    /**
     * Retrieves questions matching multiple optional filter criteria.
     */
    fun getFilteredQuestions(
        examType: ExamType,
        subject: SubjectType,
        year: Int? = null,
        topic: String? = null,
        difficulty: Difficulty? = null
    ): List<Question>

    /**
     * Retrieves a single question by its unique ID.
     */
    fun getQuestionById(id: Int): Question?

    /**
     * Selects a random subset of non-duplicate questions for an exam session.
     */
    fun getRandomQuestions(examType: ExamType, subject: SubjectType, numberOfQuestions: Int): List<Question>

    /**
     * Returns the list of distinct available years for an exam and subject.
     */
    fun getAvailableYears(examType: ExamType, subject: SubjectType): List<Int>

    /**
     * Returns the list of distinct available topics for an exam and subject.
     */
    fun getAvailableTopics(examType: ExamType, subject: SubjectType): List<String>

    /**
     * Returns all questions across all exams and subjects.
     */
    fun getAllQuestions(): List<Question>

    /**
     * Returns all questions with option to include inactive questions (for Administrator Question Bank).
     */
    fun getAllQuestions(includeInactive: Boolean): List<Question> = getAllQuestions()

    /**
     * Admin: Adds a new question to the bank.
     */
    suspend fun addQuestion(question: Question): Result<Question>

    /**
     * Admin: Updates an existing question in the bank.
     */
    suspend fun updateQuestion(question: Question): Result<Question>

    /**
     * Admin: Activates or deactivates a question.
     */
    suspend fun setQuestionActive(id: Int, isActive: Boolean, updatedBy: String): Result<Unit>

    /**
     * Admin: Deletes a question from the bank.
     */
    suspend fun deleteQuestion(id: Int): Result<Unit>

    /**
     * Admin: Bulk adds multiple questions (e.g. from Excel/CSV import).
     */
    suspend fun bulkAddQuestions(
        questions: List<Question>,
        onProgress: ((processed: Int, total: Int) -> Unit)? = null
    ): Result<Int>

    /**
     * Generates a unique question ID for a new question.
     */
    fun generateNextId(): Int = (System.currentTimeMillis() % 1_000_000).toInt()
}
