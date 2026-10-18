package com.success.examprep.data.access

import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.SubjectType

/**
 * Access control abstraction for checking and managing student permissions per exam and subject.
 */
interface AccessManager {

    /**
     * Checks if the student has full access for the given examination and subject.
     */
    fun hasFullAccess(examType: ExamType, subject: SubjectType): Boolean

    /**
     * Determines whether a specific question at the given 0-based test order index is accessible.
     */
    fun canAccessQuestion(examType: ExamType, subject: SubjectType, questionOrderIndex: Int): Boolean

    /**
     * Returns the access state (FREE, UNLOCKED, or LOCKED) for a question at a given order index.
     */
    fun getAccessState(examType: ExamType, subject: SubjectType, questionOrderIndex: Int): QuestionAccessState

    /**
     * Returns how many questions the student has permission to attempt in this session.
     */
    fun getAccessibleQuestionCount(examType: ExamType, subject: SubjectType, totalQuestions: Int): Int

    /**
     * Updates full-access status for a specific exam and subject combination.
     * Used for developer/test simulation and will connect to access-code / payment systems in future stages.
     */
    fun setFullAccess(examType: ExamType, subject: SubjectType, granted: Boolean)

    /**
     * Resets all unlocked subjects back to free-only state.
     */
    fun resetAllAccess()
}
