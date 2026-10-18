package com.success.examprep.data.repository

import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.Subject

/**
 * Repository interface abstraction for Exam and Question data access.
 * Easily replaceable with a Room database or Remote API client in future stages.
 */
interface ExamRepository {
    fun getExams(): List<ExamType>
    fun getSubjects(examType: ExamType): List<Subject>
    fun getSubject(examType: ExamType, subjectId: String): Subject?
    fun getQuestions(examType: ExamType, subjectId: String): List<Question>
}
