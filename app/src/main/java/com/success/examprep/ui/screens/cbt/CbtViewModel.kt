package com.success.examprep.ui.screens.cbt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.success.examprep.data.access.AccessManager
import com.success.examprep.data.access.LocalAccessManager
import com.success.examprep.data.model.CbtResult
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.QuestionReviewItem
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.repository.ExamRepository
import com.success.examprep.data.repository.LocalQuestionRepository
import com.success.examprep.data.repository.MockExamRepository
import com.success.examprep.data.repository.QuestionRepository
import com.success.examprep.data.auth.AuthRepository
import com.success.examprep.data.repository.TestHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CbtViewModel(
    private val examRepository: ExamRepository = MockExamRepository(),
    private val questionRepository: QuestionRepository = LocalQuestionRepository(),
    private val accessManager: AccessManager = LocalAccessManager.getInstance(),
    private val authRepository: AuthRepository? = null,
    private val testHistoryRepository: TestHistoryRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CbtUiState())
    val uiState: StateFlow<CbtUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    /**
     * Initializes or restarts an examination session for the given exam and subject.
     */
    fun startExam(
        examType: ExamType,
        subjectId: String,
        year: Int? = null,
        topic: String? = null,
        difficulty: Difficulty? = null
    ) {
        val subject = examRepository.getSubject(examType, subjectId)
            ?: examRepository.getSubjects(examType).first()
        val subjectType = SubjectType.fromId(subject.id)

        // Strict exam & subject filtering from QuestionRepository
        val rawQuestions = if (year != null || topic != null || difficulty != null) {
            questionRepository.getFilteredQuestions(
                examType = examType,
                subject = subjectType,
                year = year,
                topic = topic,
                difficulty = difficulty
            )
        } else {
            questionRepository.getQuestions(examType, subjectType)
        }

        // Randomize question presentation order for fresh, non-repetitive practice attempts
        val questions = if (rawQuestions.size > 1) {
            rawQuestions.shuffled()
        } else {
            rawQuestions
        }

        val hasFullAccess = accessManager.hasFullAccess(examType, subjectType)
        val durationSeconds = (subject.durationMinutes * 60).toLong()

        stopTimer()

        _uiState.value = CbtUiState(
            examType = examType,
            subject = subject,
            questions = questions,
            currentIndex = 0,
            selectedAnswers = emptyMap(),
            remainingSeconds = durationSeconds,
            totalDurationSeconds = durationSeconds,
            isTimerRunning = true,
            showSubmitDialog = false,
            showLockDialog = false,
            lockedAttemptNumber = null,
            hasFullAccess = hasFullAccess,
            isCompleted = false,
            result = null
        )

        startTimer()
    }

    /**
     * Stores or updates the student's selected option for the current question.
     */
    fun selectOption(optionIndex: Int) {
        val currentState = _uiState.value
        val currentQ = currentState.currentQuestion ?: return
        if (currentState.isCompleted) return

        // Guard: Do not allow answering locked questions
        if (currentState.isQuestionLocked(currentState.currentIndex)) {
            showLockedQuestionDialog(currentState.currentIndex + 1)
            return
        }

        _uiState.update { state ->
            val updatedMap = state.selectedAnswers.toMutableMap()
            updatedMap[currentQ.id] = optionIndex
            state.copy(selectedAnswers = updatedMap)
        }
    }

    /**
     * Navigates to the next question.
     * If the next question is locked (e.g., student is on Question 20 and Question 21 is locked),
     * presents the Full-Access Dialog instead of advancing.
     */
    fun nextQuestion() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.questions.size) {
            // Reached end of entire question bank
            return
        }

        if (currentState.isQuestionLocked(nextIndex)) {
            // Next question requires full access
            showLockedQuestionDialog(nextIndex + 1)
        } else {
            _uiState.update { it.copy(currentIndex = nextIndex) }
        }
    }

    /**
     * Navigates to the previous question within accessible bounds.
     */
    fun previousQuestion() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(currentIndex = state.currentIndex - 1)
            } else {
                state
            }
        }
    }

    /**
     * Directly navigates to a specific question index.
     * Enforces access control: if target index is locked, presents the Full-Access Dialog.
     */
    fun jumpToQuestion(index: Int) {
        val currentState = _uiState.value
        if (index !in currentState.questions.indices) return

        if (currentState.isQuestionLocked(index)) {
            showLockedQuestionDialog(index + 1)
        } else {
            _uiState.update { it.copy(currentIndex = index) }
        }
    }

    /**
     * Displays the full-access required dialog for a locked question.
     */
    fun showLockedQuestionDialog(questionNumber: Int) {
        _uiState.update {
            it.copy(
                showLockDialog = true,
                lockedAttemptNumber = questionNumber
            )
        }
    }

    /**
     * Hides the full-access required dialog.
     */
    fun dismissLockDialog() {
        _uiState.update { it.copy(showLockDialog = false) }
    }

    /**
     * Shows or dismisses the submission confirmation dialog.
     */
    fun setSubmitDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showSubmitDialog = visible) }
    }

    /**
     * Submits the test, stops the countdown timer, and calculates scores.
     * IMPORTANT: Score denominator evaluates strictly the accessible question count (e.g. out of 20 for free test).
     */
    fun submitExam() {
        stopTimer()

        val currentState = _uiState.value
        val accessibleQuestions = currentState.questions.take(currentState.accessibleQuestionsCount)
        val selectedAnswers = currentState.selectedAnswers
        val timeSpentSeconds = currentState.totalDurationSeconds - currentState.remainingSeconds

        var correctCount = 0
        var wrongCount = 0
        var unansweredCount = 0

        val reviewList = accessibleQuestions.map { question ->
            val selectedOption = selectedAnswers[question.id]
            if (selectedOption == null) {
                unansweredCount++
                QuestionReviewItem(
                    question = question,
                    selectedOptionIndex = null,
                    isCorrect = false
                )
            } else if (selectedOption == question.correctAnswerIndex) {
                correctCount++
                QuestionReviewItem(
                    question = question,
                    selectedOptionIndex = selectedOption,
                    isCorrect = true
                )
            } else {
                wrongCount++
                QuestionReviewItem(
                    question = question,
                    selectedOptionIndex = selectedOption,
                    isCorrect = false
                )
            }
        }

        val accessibleTotal = accessibleQuestions.size.coerceAtLeast(1)
        val percentage = (correctCount * 100) / accessibleTotal

        val result = CbtResult(
            examType = currentState.examType,
            subject = currentState.subject ?: examRepository.getSubjects(currentState.examType).first(),
            totalQuestions = currentState.questions.size,
            accessibleQuestionsCount = accessibleTotal,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unansweredCount = unansweredCount,
            scorePercentage = percentage,
            timeSpentSeconds = timeSpentSeconds.coerceAtLeast(0),
            questionResults = reviewList,
            isFreePracticeMode = currentState.isFreePracticeMode
        )

        _uiState.update {
            it.copy(
                isTimerRunning = false,
                showSubmitDialog = false,
                showLockDialog = false,
                isCompleted = true,
                isResultSavedToProfile = false,
                result = result
            )
        }

        // Automatic persistence for authenticated students
        val currentUser = authRepository?.getCurrentUser()
        if (currentUser != null && testHistoryRepository != null) {
            viewModelScope.launch {
                try {
                    testHistoryRepository.saveTestResult(currentUser.uid, result)
                    _uiState.update { it.copy(isResultSavedToProfile = true) }
                } catch (_: Exception) {
                    // Non-fatal
                }
            }
        }
    }

    /**
     * Allows a student (e.g. guest who just created an account) to manually save the current test result.
     */
    fun saveResultForStudent(studentUid: String) {
        val currentResult = _uiState.value.result ?: return
        viewModelScope.launch {
            try {
                testHistoryRepository?.saveTestResult(studentUid, currentResult)
                _uiState.update { it.copy(isResultSavedToProfile = true) }
            } catch (_: Exception) {
                // Non-fatal
            }
        }
    }

    /**
     * Resets the examination for a clean retry attempt.
     */
    fun retryExam() {
        val currentState = _uiState.value
        val currentSubjectId = currentState.subject?.id ?: "mathematics"
        startExam(currentState.examType, currentSubjectId)
    }

    /**
     * Insecure local voucher activation removed in Stage 2.
     * All access is strictly authorized via trusted admin entitlements.
     */
    @Deprecated("Insecure local unlock removed in Stage 2. Access must be verified via admin entitlements.")
    fun unlockSubject(subjectType: SubjectType) {
        // Deliberately inert for security: local UI actions cannot bypass trusted admin entitlements.
    }

    /**
     * Developer/test mechanism to toggle full access for the active exam & subject.
     * Isolated for testing Stage 3 access-control transitions.
     */
    fun setFullAccessForTesting(granted: Boolean) {
        val currentState = _uiState.value
        val subjectType = SubjectType.fromId(currentState.subject?.id)
        accessManager.setFullAccess(currentState.examType, subjectType, granted)
        _uiState.update { it.copy(hasFullAccess = granted) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && !_uiState.value.isCompleted) {
                delay(1000L)
                _uiState.update { state ->
                    val newTime = (state.remainingSeconds - 1).coerceAtLeast(0)
                    state.copy(remainingSeconds = newTime)
                }

                if (_uiState.value.remainingSeconds <= 0L) {
                    // Time elapsed! Automatically submit
                    submitExam()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
