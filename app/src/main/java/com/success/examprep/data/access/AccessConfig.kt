package com.success.examprep.data.access

/**
 * Global configuration constants for question access control.
 * Changing FREE_QUESTION_LIMIT here configures the free question threshold across the entire app.
 */
object AccessConfig {
    /**
     * Maximum number of questions accessible for free per subject exam session.
     */
    const val FREE_QUESTION_LIMIT: Int = 20
}
