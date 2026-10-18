package com.success.examprep

import com.success.examprep.data.local.LocalQuestionDataSource
import com.success.examprep.data.local.questions.JambQuestions
import com.success.examprep.data.local.questions.NecoQuestions
import com.success.examprep.data.local.questions.WaecQuestions
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.SubjectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuestionBankQualityAndIntegrityTest {

    private lateinit var dataSource: LocalQuestionDataSource

    @Before
    fun setUp() {
        dataSource = LocalQuestionDataSource()
    }

    @Test
    fun testAllFourteenSubjectsMeet200QuestionThreshold() {
        SubjectType.entries.forEach { subject ->
            val jambCount = dataSource.getQuestions(ExamType.JAMB, subject).size
            val waecCount = dataSource.getQuestions(ExamType.WAEC, subject).size
            val necoCount = dataSource.getQuestions(ExamType.NECO, subject).size
            val totalForSubject = jambCount + waecCount + necoCount

            assertTrue(
                "Subject ${subject.name} must meet the minimum threshold of 200 authentic questions (found $totalForSubject)",
                totalForSubject >= 200
            )

            assertTrue(
                "Subject ${subject.name} must have a dedicated JAMB pool (found $jambCount)",
                jambCount >= 70
            )
            assertTrue(
                "Subject ${subject.name} must have a dedicated WAEC pool (found $waecCount)",
                waecCount >= 70
            )
            assertTrue(
                "Subject ${subject.name} must have a dedicated NECO pool (found $necoCount)",
                necoCount >= 70
            )
        }
    }

    @Test
    fun testNoCrossExamDuplicatesPerSubject() {
        val syntheticRegex = Regex("\\([^)]*(?:Query|Item|Record|Quiz|Test|Sample|Function|Template|#\\d+)[^)]*\\)")

        SubjectType.entries.forEach { subject ->
            val jambTexts = dataSource.getQuestions(ExamType.JAMB, subject).map { it.questionText.trim().lowercase() }.toSet()
            val waecTexts = dataSource.getQuestions(ExamType.WAEC, subject).map { it.questionText.trim().lowercase() }.toSet()
            val necoTexts = dataSource.getQuestions(ExamType.NECO, subject).map { it.questionText.trim().lowercase() }.toSet()

            val jambWaecOverlap = jambTexts.intersect(waecTexts)
            val jambNecoOverlap = jambTexts.intersect(necoTexts)
            val waecNecoOverlap = waecTexts.intersect(necoTexts)

            assertTrue(
                "Subject ${subject.name} has cross-exam duplicates between JAMB and WAEC: $jambWaecOverlap",
                jambWaecOverlap.isEmpty()
            )
            assertTrue(
                "Subject ${subject.name} has cross-exam duplicates between JAMB and NECO: $jambNecoOverlap",
                jambNecoOverlap.isEmpty()
            )
            assertTrue(
                "Subject ${subject.name} has cross-exam duplicates between WAEC and NECO: $waecNecoOverlap",
                waecNecoOverlap.isEmpty()
            )
        }
    }

    @Test
    fun testNoSyntheticTagsInAnyQuestionText() {
        val syntheticRegex = Regex("\\([^)]*(?:Query|Item|Record|Quiz|Test|Sample|Function|Template|#\\d+)[^)]*\\)")

        SubjectType.entries.forEach { subject ->
            val allSubjectQuestions = ExamType.entries.flatMap { exam ->
                dataSource.getQuestions(exam, subject)
            }

            allSubjectQuestions.forEach { question ->
                assertFalse(
                    "Question ID ${question.id} in ${subject.name} contains synthetic placeholder tag: ${question.questionText}",
                    syntheticRegex.containsMatchIn(question.questionText)
                )
            }
        }
    }

    @Test
    fun testAllQuestionsHaveFourDistinctOptionsAndValidFields() {
        ExamType.entries.forEach { exam ->
            SubjectType.entries.forEach { subject ->
                val questions = dataSource.getQuestions(exam, subject)
                assertTrue("Exam ${exam.name} - Subject ${subject.name} must contain questions", questions.isNotEmpty())

                questions.forEach { q ->
                    assertTrue("Question text cannot be blank (ID: ${q.id})", q.questionText.isNotBlank())
                    assertTrue("Option A cannot be blank (ID: ${q.id})", q.optionA.isNotBlank())
                    assertTrue("Option B cannot be blank (ID: ${q.id})", q.optionB.isNotBlank())
                    assertTrue("Option C cannot be blank (ID: ${q.id})", q.optionC.isNotBlank())
                    assertTrue("Option D cannot be blank (ID: ${q.id})", q.optionD.isNotBlank())
                    assertTrue("Topic cannot be blank (ID: ${q.id})", q.topic.isNotBlank())
                    assertTrue("Explanation cannot be blank (ID: ${q.id})", q.explanation.isNotBlank())
                    assertNotNull("Correct answer must be valid (ID: ${q.id})", q.correctAnswer)

                    val distinctOptions = setOf(q.optionA.trim(), q.optionB.trim(), q.optionC.trim(), q.optionD.trim())
                    assertEquals(
                        "Question ID ${q.id} in ${q.subject.name} must have 4 distinct options: $distinctOptions",
                        4,
                        distinctOptions.size
                    )
                }
            }
        }
    }
}
