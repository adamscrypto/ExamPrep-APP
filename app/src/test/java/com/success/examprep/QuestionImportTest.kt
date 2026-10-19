package com.success.examprep

import com.success.examprep.data.auth.AuthRepository
import com.success.examprep.data.importer.ExcelParser
import com.success.examprep.data.importer.QuestionImportService
import com.success.examprep.data.local.LocalQuestionDataSource
import com.success.examprep.data.model.AuthState
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.UserProfile
import com.success.examprep.data.model.UserSession
import com.success.examprep.data.repository.LocalQuestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionImportTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var localDataSource: LocalQuestionDataSource
    private lateinit var localRepository: LocalQuestionRepository
    
    private class FakeAuthRepository : AuthRepository {
        override val authState = MutableStateFlow<AuthState>(AuthState.Guest)
        override val isAdmin = MutableStateFlow(false)
        override val userSession = MutableStateFlow<UserSession>(UserSession.Guest)
        
        override fun isGuest(): Boolean = true
        override fun isUserAuthenticated(): Boolean = false
        override fun getCurrentUser(): StudentUser? = null
        override fun getCurrentUserProfile(): UserProfile? = null
        override suspend fun checkAdminStatus(): Boolean = false
        
        override suspend fun registerStudent(fullName: String, phone: String, studentId: String?, pin: String, targetExam: ExamType): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override suspend fun loginStudentWithPin(phoneOrStudentId: String, pin: String): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override suspend fun loginAdmin(email: String, password: String): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override suspend fun login(emailOrId: String, passwordOrPin: String): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override suspend fun register(email: String, password: String, fullName: String, phone: String?, targetExam: ExamType): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override suspend fun logout() {}
        override fun lockStudentSession() {}
        override fun isStudentSessionLocked(uid: String): Boolean = false
        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.failure(Exception("Not implemented"))
        override suspend fun updateProfile(name: String, targetExam: ExamType): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override fun setDevAdminClaim(enabled: Boolean) {}

        override fun observeAuthState(): StateFlow<AuthState> = authState
        override fun observeUserSession(): StateFlow<UserSession> = userSession
        override suspend fun signIn(email: String, password: String): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override suspend fun signUp(name: String, email: String, password: String, targetExam: ExamType): Result<StudentUser> = Result.failure(Exception("Not implemented"))
        override suspend fun signOut() {}
        override suspend fun sendPasswordReset(email: String): Result<Unit> = Result.failure(Exception("Not implemented"))
    }
    
    private lateinit var fakeAuthRepository: FakeAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        localDataSource = LocalQuestionDataSource()
        localRepository = LocalQuestionRepository(localDataSource)
        fakeAuthRepository = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMinimalXlsxBytes(rows: List<List<String>>): ByteArray {
        val baos = ByteArrayOutputStream()
        val zip = ZipOutputStream(baos)

        // 1. sheet1.xml
        val sheetXml = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
            append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n")
            append("<sheetData>\n")
            rows.forEachIndexed { rIdx, row ->
                val rowNum = rIdx + 1
                append("<row r=\"$rowNum\">\n")
                row.forEachIndexed { cIdx, cellValue ->
                    val colLetter = ('A'.code + cIdx).toChar().toString()
                    val cellRef = "$colLetter$rowNum"
                    append("<c r=\"$cellRef\" t=\"inlineStr\"><is><t>")
                    append(cellValue.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
                    append("</t></is></c>\n")
                }
                append("</row>\n")
            }
            append("</sheetData>\n</worksheet>")
        }

        zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
        zip.write(sheetXml.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
        zip.close()
        return baos.toByteArray()
    }

    @Test
    fun testExcelParserWithInlineStrings() {
        val rows = listOf(
            listOf("Exam", "Subject", "Year", "Topic", "Question", "OptionA", "OptionB", "OptionC", "OptionD", "Answer", "Explanation", "Difficulty", "Active"),
            listOf("JAMB", "Mathematics", "2023", "Calculus", "What is the derivative of x^2?", "2x", "x", "x^2", "2", "A", "Power rule", "EASY", "TRUE")
        )
        val xlsxBytes = createMinimalXlsxBytes(rows)
        val parsed = ExcelParser.parse(ByteArrayInputStream(xlsxBytes))

        assertEquals(13, parsed.headers.size)
        assertEquals(1, parsed.rows.size)
    }

    @Test
    fun testProcessSpreadsheetWithExcelData() = runTest {
        val rows = listOf(
            listOf("Exam", "Subject", "Year", "Topic", "Question", "OptionA", "OptionB", "OptionC", "OptionD", "Answer", "Explanation", "Difficulty", "Active"),
            listOf("JAMB", "Mathematics", "2023", "Calculus", "What is the derivative of x^2?", "2x", "x", "x^2", "2", "A", "Power rule", "EASY", "TRUE")
        )
        val xlsxBytes = createMinimalXlsxBytes(rows)
        val (summary, importRows) = QuestionImportService.processSpreadsheet(
            inputStream = ByteArrayInputStream(xlsxBytes),
            fileName = "sample_questions.xlsx",
            adminUid = "admin_test",
            existingQuestions = emptyList(),
            startingQuestionId = 500000
        )

        assertEquals(1, summary.totalRows)
        assertEquals(1, summary.importableRows)
        assertNotNull(importRows.first().question)

        val questionsToImport = importRows.mapNotNull { it.question }
        val result = localRepository.bulkAddQuestions(questionsToImport)
        assertTrue(result.isSuccess)
    }
}
