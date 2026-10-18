package com.success.examprep.data.local

import com.success.examprep.data.local.questions.JambQuestions
import com.success.examprep.data.local.questions.NecoQuestions
import com.success.examprep.data.local.questions.WaecQuestions
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.SubjectType
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Local in-memory question data source aggregating modular question banks.
 * Validates integrity of questions and supports dynamic admin mutations & offline operation.
 */
class LocalQuestionDataSource {

    private val questions: CopyOnWriteArrayList<Question> by lazy {
        val initialList = JambQuestions.allQuestions + WaecQuestions.allQuestions + NecoQuestions.allQuestions
        // Validate question dataset integrity on load
        initialList.forEach { question ->
            val result = question.validate()
            check(result.isValid) { "Invalid question found in database: ID ${question.id}: $result" }
        }
        CopyOnWriteArrayList(initialList)
    }

    fun getQuestions(examType: ExamType, subject: SubjectType, includeInactive: Boolean = false): List<Question> {
        return questions.filter {
            it.examType == examType && it.subject == subject && (includeInactive || it.isActive)
        }
    }

    fun getQuestionsByTopic(examType: ExamType, subject: SubjectType, topic: String): List<Question> {
        return questions.filter {
            it.examType == examType && it.subject == subject && it.isActive && it.topic.equals(topic, ignoreCase = true)
        }
    }

    fun getQuestionsByYear(examType: ExamType, subject: SubjectType, year: Int): List<Question> {
        return questions.filter {
            it.examType == examType && it.subject == subject && it.isActive && it.year == year
        }
    }

    fun getQuestionsByDifficulty(examType: ExamType, subject: SubjectType, difficulty: Difficulty): List<Question> {
        return questions.filter {
            it.examType == examType && it.subject == subject && it.isActive && it.difficulty == difficulty
        }
    }

    fun getFilteredQuestions(
        examType: ExamType,
        subject: SubjectType,
        year: Int? = null,
        topic: String? = null,
        difficulty: Difficulty? = null,
        includeInactive: Boolean = false
    ): List<Question> {
        return questions.filter { q ->
            q.examType == examType &&
                q.subject == subject &&
                (includeInactive || q.isActive) &&
                (year == null || q.year == year) &&
                (topic == null || q.topic.equals(topic, ignoreCase = true)) &&
                (difficulty == null || q.difficulty == difficulty)
        }
    }

    fun getQuestionById(id: Int): Question? {
        return questions.firstOrNull { it.id == id }
    }

    fun getAllQuestions(includeInactive: Boolean = false): List<Question> {
        return if (includeInactive) {
            questions.toList()
        } else {
            questions.filter { it.isActive }
        }
    }

    fun addQuestion(question: Question): Question {
        // Validate question integrity
        val validation = question.validate()
        require(validation.isValid) {
            val reason = (validation as? com.success.examprep.data.model.ValidationResult.Invalid)?.reason ?: "Invalid question"
            "Question validation failed: $reason"
        }

        // Remove existing if any with same id
        questions.removeIf { it.id == question.id }
        questions.add(question)
        return question
    }

    fun updateQuestion(question: Question): Question {
        val validation = question.validate()
        require(validation.isValid) {
            val reason = (validation as? com.success.examprep.data.model.ValidationResult.Invalid)?.reason ?: "Invalid question"
            "Question validation failed: $reason"
        }

        val index = questions.indexOfFirst { it.id == question.id }
        if (index >= 0) {
            questions[index] = question
        } else {
            questions.add(question)
        }
        return question
    }

    fun setQuestionActive(id: Int, isActive: Boolean, updatedBy: String): Boolean {
        val index = questions.indexOfFirst { it.id == id }
        if (index >= 0) {
            val current = questions[index]
            questions[index] = current.copy(
                isActive = isActive,
                updatedAt = System.currentTimeMillis(),
                updatedBy = updatedBy
            )
            return true
        }
        return false
    }

    fun deleteQuestion(id: Int): Boolean {
        return questions.removeIf { it.id == id }
    }

    fun bulkAddQuestions(newQuestions: List<Question>): Int {
        var count = 0
        newQuestions.forEach { question ->
            val validation = question.validate()
            if (validation.isValid) {
                questions.removeIf { it.id == question.id }
                questions.add(question)
                count++
            }
        }
        return count
    }

    fun generateNextId(): Int {
        val maxId = questions.maxOfOrNull { it.id } ?: 1000
        return maxId + 1
    }
}
