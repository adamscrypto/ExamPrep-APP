package com.success.examprep.data.repository

import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.Subject
import com.success.examprep.data.model.SubjectType

/**
 * Examination and Subject catalog repository.
 * Coordinates with QuestionRepository for question retrieval across JAMB, WAEC, and NECO.
 */
class MockExamRepository(
    private val questionRepository: QuestionRepository = LocalQuestionRepository()
) : ExamRepository {

    override fun getExams(): List<ExamType> {
        return ExamType.entries
    }

    override fun getSubjects(examType: ExamType): List<Subject> {
        return listOf(
            Subject(
                type = SubjectType.MATHEMATICS,
                description = "Algebra, Number Bases, Indices, Sets, Geometry, Statistics & Probability",
                iconName = "calculate",
                questionCount = questionRepository.getQuestions(examType, SubjectType.MATHEMATICS).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.MATHEMATICS).ifEmpty {
                    listOf("Algebra", "Geometry", "Statistics")
                }
            ),
            Subject(
                type = SubjectType.ENGLISH,
                description = "Lexis & Structure, Comprehension, Synonyms, Antonyms, Sentence Completion",
                iconName = "menu_book",
                questionCount = questionRepository.getQuestions(examType, SubjectType.ENGLISH).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.ENGLISH).ifEmpty {
                    listOf("Lexis and Structure", "Concord")
                }
            ),
            Subject(
                type = SubjectType.PHYSICS,
                description = "Mechanics, Heat, Waves, Optics, Electricity, Magnetism & Modern Physics",
                iconName = "bolt",
                questionCount = questionRepository.getQuestions(examType, SubjectType.PHYSICS).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.PHYSICS).ifEmpty {
                    listOf("Motion", "Waves", "Electricity")
                }
            ),
            Subject(
                type = SubjectType.CHEMISTRY,
                description = "Atomic Structure, Periodic Table, Stoichiometry, Organic & Inorganic",
                iconName = "science",
                questionCount = questionRepository.getQuestions(examType, SubjectType.CHEMISTRY).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.CHEMISTRY).ifEmpty {
                    listOf("Atomic Structure", "Chemical Bonding")
                }
            ),
            Subject(
                type = SubjectType.BIOLOGY,
                description = "Cell Biology, Genetics, Ecology, Human Physiology & Plant Nutrition",
                iconName = "eco",
                questionCount = questionRepository.getQuestions(examType, SubjectType.BIOLOGY).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.BIOLOGY).ifEmpty {
                    listOf("Cell Biology", "Genetics", "Ecology")
                }
            ),
            Subject(
                type = SubjectType.GOVERNMENT,
                description = "Political Concepts, Constitutions, Public Administration & International Relations",
                iconName = "account_balance",
                questionCount = questionRepository.getQuestions(examType, SubjectType.GOVERNMENT).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.GOVERNMENT).ifEmpty {
                    listOf("Basic Concepts", "Constitutional Development", "Public Administration")
                }
            ),
            Subject(
                type = SubjectType.CRS,
                description = "Old Testament, Gospels, Acts of the Apostles & Christian Epistles",
                iconName = "church",
                questionCount = questionRepository.getQuestions(examType, SubjectType.CRS).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.CRS).ifEmpty {
                    listOf("Sovereignty of God", "Ministry of Jesus", "The Early Church")
                }
            ),
            Subject(
                type = SubjectType.IRS,
                description = "Tawhid, Quran, Hadith, Fiqh, Islamic History & Civilization",
                iconName = "mosque",
                questionCount = questionRepository.getQuestions(examType, SubjectType.IRS).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.IRS).ifEmpty {
                    listOf("Tawhid", "Hadith Studies", "Pillars of Islam")
                }
            ),
            Subject(
                type = SubjectType.LITERATURE,
                description = "Drama, Poetry, Prose, Literary Devices & Prescribed Texts",
                iconName = "auto_stories",
                questionCount = questionRepository.getQuestions(examType, SubjectType.LITERATURE).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.LITERATURE).ifEmpty {
                    listOf("Literary Principles", "African Poetry", "Shakespearean Drama")
                }
            ),
            Subject(
                type = SubjectType.COMMERCE,
                description = "Trade, Aids to Trade, Business Organizations, Banking & Stock Exchange",
                iconName = "storefront",
                questionCount = questionRepository.getQuestions(examType, SubjectType.COMMERCE).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.COMMERCE).ifEmpty {
                    listOf("Introduction to Commerce", "Home Trade", "Aids to Trade")
                }
            ),
            Subject(
                type = SubjectType.ECONOMICS,
                description = "Microeconomics, Macroeconomics, Elasticity, Fiscal Policy & Trade",
                iconName = "trending_up",
                questionCount = questionRepository.getQuestions(examType, SubjectType.ECONOMICS).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.ECONOMICS).ifEmpty {
                    listOf("Price Theory", "National Income", "Money and Banking")
                }
            ),
            Subject(
                type = SubjectType.GEOGRAPHY,
                description = "Physical Geography, Map Reading, Climatology, Human & Regional Geography",
                iconName = "public",
                questionCount = questionRepository.getQuestions(examType, SubjectType.GEOGRAPHY).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.GEOGRAPHY).ifEmpty {
                    listOf("Map Reading", "Weather and Climate", "Regional Geography")
                }
            ),
            Subject(
                type = SubjectType.AGRICULTURAL_SCIENCE,
                description = "Crop Production, Soil Science, Animal Husbandry & Farm Economics",
                iconName = "agriculture",
                questionCount = questionRepository.getQuestions(examType, SubjectType.AGRICULTURAL_SCIENCE).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.AGRICULTURAL_SCIENCE).ifEmpty {
                    listOf("Soil Science", "Crop Husbandry", "Animal Nutrition")
                }
            ),
            Subject(
                type = SubjectType.CIVIC_EDUCATION,
                description = "Democratic Governance, Citizenship, Human Rights & Rule of Law",
                iconName = "gavel",
                questionCount = questionRepository.getQuestions(examType, SubjectType.CIVIC_EDUCATION).size.coerceAtLeast(10),
                durationMinutes = 10,
                topics = questionRepository.getAvailableTopics(examType, SubjectType.CIVIC_EDUCATION).ifEmpty {
                    listOf("Values & Citizenship", "Rule of Law", "National Consciousness")
                }
            )
        )
    }

    override fun getSubject(examType: ExamType, subjectId: String): Subject? {
        return getSubjects(examType).firstOrNull { it.id.equals(subjectId, ignoreCase = true) }
    }

    override fun getQuestions(examType: ExamType, subjectId: String): List<Question> {
        val subjectType = SubjectType.fromId(subjectId)
        return questionRepository.getQuestions(examType, subjectType)
    }
}
