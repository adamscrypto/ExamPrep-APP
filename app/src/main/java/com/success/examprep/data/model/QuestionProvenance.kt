package com.success.examprep.data.model

/**
 * Origin of the question material.
 * Critical Rule: Never fabricate past question provenance or label practice items as official past questions.
 */
enum class QuestionSourceType(val displayName: String) {
    OFFICIAL("Official Past Question"),
    AUTHORIZED_PUBLICATION("Authorized Publication"),
    USER_SUPPLIED_WITH_SOURCE("User Supplied with Source"),
    PRACTICE("Practice Question")
}

/**
 * Editorial and provenance verification lifecycle stage.
 * Workflow: DRAFT -> PENDING_REVIEW -> VERIFIED -> PUBLISHED
 */
enum class QuestionVerificationStatus(val label: String) {
    UNVERIFIED("Unverified"),
    DRAFT("Draft"),
    PENDING_REVIEW("Pending Review"),
    VERIFIED("Verified"),
    REJECTED("Rejected")
}

/**
 * Answer key and explanation review status.
 */
enum class AnswerVerificationStatus(val label: String) {
    UNVERIFIED("Unverified"),
    REVIEWED("Reviewed"),
    DISPUTED("Disputed")
}

/**
 * Copyright and dissemination permission compliance status.
 */
enum class ContentPermissionStatus(val label: String) {
    PUBLIC_DOMAIN_PAST_EXAM("Public Domain Past Examination"),
    AUTHORIZED("Authorized"),
    PENDING_VERIFICATION("Pending Verification"),
    RESTRICTED("Restricted")
}
