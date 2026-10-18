package com.success.examprep.data.access

/**
 * Access state for an individual question in a CBT session.
 */
enum class QuestionAccessState {
    /**
     * Accessible to all students within the free question limit (e.g. Questions 1..20).
     */
    FREE,

    /**
     * Accessible beyond the free limit because full access has been unlocked for this exam + subject.
     */
    UNLOCKED,

    /**
     * Locked beyond the free limit because full access has not been granted.
     */
    LOCKED;

    val isAccessible: Boolean
        get() = this == FREE || this == UNLOCKED
}
