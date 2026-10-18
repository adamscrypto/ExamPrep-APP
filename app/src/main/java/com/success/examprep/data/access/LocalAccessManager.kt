package com.success.examprep.data.access

import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.SubjectType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory thread-safe implementation of AccessManager.
 * Stores access permissions per (ExamType, SubjectType) pair without external dependencies.
 */
class LocalAccessManager : AccessManager {

    // Keyed strictly by Pair(ExamType, SubjectType) to ensure subject- and exam-specific access isolation
    private val accessMap = ConcurrentHashMap<Pair<ExamType, SubjectType>, Boolean>()

    /**
     * Optional entitlement checker delegate connecting Stage 5 student entitlements with CBT access control.
     */
    var entitlementChecker: ((ExamType, SubjectType) -> Boolean)? = null

    private val _accessStateFlow = MutableStateFlow<Map<Pair<ExamType, SubjectType>, Boolean>>(emptyMap())
    val accessStateFlow: StateFlow<Map<Pair<ExamType, SubjectType>, Boolean>> = _accessStateFlow.asStateFlow()

    override fun hasFullAccess(examType: ExamType, subject: SubjectType): Boolean {
        // Enforce trusted entitlement architecture as authoritative:
        // When entitlementChecker is configured, local UI state alone cannot grant paid access.
        val checker = entitlementChecker
        if (checker != null) {
            return checker.invoke(examType, subject)
        }
        return accessMap[Pair(examType, subject)] == true
    }

    override fun canAccessQuestion(
        examType: ExamType,
        subject: SubjectType,
        questionOrderIndex: Int
    ): Boolean {
        return getAccessState(examType, subject, questionOrderIndex).isAccessible
    }

    override fun getAccessState(
        examType: ExamType,
        subject: SubjectType,
        questionOrderIndex: Int
    ): QuestionAccessState {
        return when {
            hasFullAccess(examType, subject) -> {
                if (questionOrderIndex < AccessConfig.FREE_QUESTION_LIMIT) {
                    QuestionAccessState.FREE
                } else {
                    QuestionAccessState.UNLOCKED
                }
            }
            questionOrderIndex < AccessConfig.FREE_QUESTION_LIMIT -> QuestionAccessState.FREE
            else -> QuestionAccessState.LOCKED
        }
    }

    override fun getAccessibleQuestionCount(
        examType: ExamType,
        subject: SubjectType,
        totalQuestions: Int
    ): Int {
        return if (hasFullAccess(examType, subject)) {
            totalQuestions
        } else {
            totalQuestions.coerceAtMost(AccessConfig.FREE_QUESTION_LIMIT)
        }
    }

    override fun setFullAccess(examType: ExamType, subject: SubjectType, granted: Boolean) {
        val key = Pair(examType, subject)
        if (granted) {
            accessMap[key] = true
        } else {
            accessMap.remove(key)
        }
        _accessStateFlow.value = accessMap.toMap()
    }

    override fun resetAllAccess() {
        accessMap.clear()
        _accessStateFlow.value = emptyMap()
    }

    companion object {
        @Volatile
        private var instance: LocalAccessManager? = null

        fun getInstance(): LocalAccessManager {
            return instance ?: synchronized(this) {
                instance ?: LocalAccessManager().also { instance = it }
            }
        }
    }
}
