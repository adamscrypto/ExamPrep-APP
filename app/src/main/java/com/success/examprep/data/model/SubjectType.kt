package com.success.examprep.data.model

/**
 * Standard examination subject types for Nigerian secondary/CBT curricula.
 */
enum class SubjectType(val id: String, val displayName: String) {
    MATHEMATICS("mathematics", "Mathematics"),
    ENGLISH("english", "English Language"),
    PHYSICS("physics", "Physics"),
    CHEMISTRY("chemistry", "Chemistry"),
    BIOLOGY("biology", "Biology"),
    GOVERNMENT("government", "Government"),
    CRS("crs", "Christian Religious Studies"),
    IRS("irs", "Islamic Religious Studies"),
    LITERATURE("literature", "Literature in English"),
    COMMERCE("commerce", "Commerce"),
    ECONOMICS("economics", "Economics"),
    GEOGRAPHY("geography", "Geography"),
    AGRICULTURAL_SCIENCE("agricultural_science", "Agricultural Science"),
    CIVIC_EDUCATION("civic_education", "Civic Education");

    companion object {
        fun fromId(id: String?): SubjectType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MATHEMATICS
        }
    }
}
