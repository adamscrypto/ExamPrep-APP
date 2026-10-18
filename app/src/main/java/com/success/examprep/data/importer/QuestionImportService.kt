package com.success.examprep.data.importer

import com.success.examprep.data.model.AnswerVerificationStatus
import com.success.examprep.data.model.ContentPermissionStatus
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.QuestionSourceType
import com.success.examprep.data.model.QuestionValidationResult
import com.success.examprep.data.model.QuestionValidator
import com.success.examprep.data.model.QuestionVerificationStatus
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.model.ValidationResult
import java.io.InputStream

/**
 * Service responsible for parsing, validating, duplicate-checking, and mapping
 * bulk spreadsheet rows to the authoritative Question domain model.
 * Stage 7A: Extended with full provenance column support and draft/pending_review lifecycle import.
 */
object QuestionImportService {

    // Canonical Header Keys
    const val KEY_EXAM = "exam"
    const val KEY_SUBJECT = "subject"
    const val KEY_YEAR = "year"
    const val KEY_TOPIC = "topic"
    const val KEY_QUESTION = "question"
    const val KEY_OPTION_A = "optiona"
    const val KEY_OPTION_B = "optionb"
    const val KEY_OPTION_C = "optionc"
    const val KEY_OPTION_D = "optiond"
    const val KEY_ANSWER = "answer"
    const val KEY_EXPLANATION = "explanation"
    const val KEY_DIFFICULTY = "difficulty"
    const val KEY_ACTIVE = "active"

    // Stage 7A: Provenance & Verification Columns
    const val KEY_SOURCE_TYPE = "sourcetype"
    const val KEY_SOURCE_TITLE = "sourcetitle"
    const val KEY_SOURCE_ORGANIZATION = "sourceorganization"
    const val KEY_SOURCE_REFERENCE = "sourcereference"
    const val KEY_SOURCE_URL = "sourceurl"
    const val KEY_SOURCE_YEAR = "sourceyear"
    const val KEY_SOURCE_PAGE = "sourcepage"
    const val KEY_ORIGINAL_Q_NUM = "originalquestionnumber"
    const val KEY_VERIFICATION_STATUS = "verificationstatus"
    const val KEY_ANSWER_VERIFICATION_STATUS = "answerverificationstatus"
    const val KEY_PERMISSION_STATUS = "contentpermissionstatus"

    private val REQUIRED_KEYS = listOf(
        KEY_EXAM,
        KEY_SUBJECT,
        KEY_YEAR,
        KEY_TOPIC,
        KEY_QUESTION,
        KEY_OPTION_A,
        KEY_OPTION_B,
        KEY_OPTION_C,
        KEY_OPTION_D,
        KEY_ANSWER
    )

    private val DISPLAY_NAMES = mapOf(
        KEY_EXAM to "Exam",
        KEY_SUBJECT to "Subject",
        KEY_YEAR to "Year",
        KEY_TOPIC to "Topic",
        KEY_QUESTION to "Question",
        KEY_OPTION_A to "OptionA",
        KEY_OPTION_B to "OptionB",
        KEY_OPTION_C to "OptionC",
        KEY_OPTION_D to "OptionD",
        KEY_ANSWER to "Answer",
        KEY_EXPLANATION to "Explanation",
        KEY_DIFFICULTY to "Difficulty",
        KEY_ACTIVE to "Active",
        KEY_SOURCE_TYPE to "SourceType",
        KEY_SOURCE_TITLE to "SourceTitle",
        KEY_SOURCE_ORGANIZATION to "SourceOrganization",
        KEY_SOURCE_REFERENCE to "SourceReference",
        KEY_SOURCE_URL to "SourceUrl",
        KEY_SOURCE_YEAR to "SourceYear",
        KEY_SOURCE_PAGE to "SourcePage",
        KEY_ORIGINAL_Q_NUM to "OriginalQuestionNumber",
        KEY_VERIFICATION_STATUS to "VerificationStatus",
        KEY_ANSWER_VERIFICATION_STATUS to "AnswerVerificationStatus",
        KEY_PERMISSION_STATUS to "ContentPermissionStatus"
    )

    /**
     * Parses and analyzes a spreadsheet input stream.
     */
    fun processSpreadsheet(
        inputStream: InputStream,
        fileName: String,
        adminUid: String,
        existingQuestions: List<Question> = emptyList(),
        startingQuestionId: Int = 10_000
    ): Pair<ImportSummary, List<ImportRow>> {
        val fileType = SpreadsheetType.fromFileName(fileName)
            ?: throw IllegalArgumentException("Unsupported file type. Please select an Excel (.xlsx) or CSV (.csv) file.")

        val rawData = when (fileType) {
            SpreadsheetType.CSV -> CsvParser.parse(inputStream)
            SpreadsheetType.XLSX -> ExcelParser.parse(inputStream)
        }

        if (rawData.headers.isEmpty()) {
            val summary = ImportSummary(
                fileName = fileName,
                fileType = fileType,
                totalRows = 0,
                validRows = 0,
                invalidRows = 0,
                duplicateRows = 0,
                importableRows = 0,
                rowsWithMissingProvenance = 0,
                missingHeaders = REQUIRED_KEYS.map { DISPLAY_NAMES[it] ?: it }
            )
            return Pair(summary, emptyList())
        }

        // 1. Map & validate headers
        val mappingResult = validateAndMapHeaders(rawData.headers)
        if (!mappingResult.isSuccess) {
            val summary = ImportSummary(
                fileName = fileName,
                fileType = fileType,
                totalRows = rawData.rows.size,
                validRows = 0,
                invalidRows = rawData.rows.size,
                duplicateRows = 0,
                importableRows = 0,
                rowsWithMissingProvenance = 0,
                headerMappings = mappingResult.detectedMappings,
                missingHeaders = mappingResult.missingRequiredHeaders
            )
            val errorRows = rawData.rows.mapIndexed { index, _ ->
                ImportRow(
                    rowNumber = index + 2, // Row 1 is header
                    rawValues = emptyMap(),
                    question = null,
                    errors = listOf("Import cannot continue: Missing required column(s): ${mappingResult.missingRequiredHeaders.joinToString(", ")}")
                )
            }
            return Pair(summary, errorRows)
        }

        // 2. Prepare duplicate detection fingerprints
        val existingFingerprints = HashSet<String>()
        existingQuestions.forEach { q ->
            existingFingerprints.add(generateFingerprint(q.examType, q.subject, q.year, q.questionText))
        }
        val fileFingerprints = HashSet<String>()

        // 3. Process every data row
        val processedRows = ArrayList<ImportRow>(rawData.rows.size)
        var currentId = startingQuestionId
        var rowsWithMissingProvenanceCount = 0

        for ((index, rowValues) in rawData.rows.withIndex()) {
            val spreadsheetRowNumber = index + 2 // 1-based, row 1 is header

            // Build raw values map
            val rowMap = HashMap<String, String>()
            mappingResult.detectedMappings.forEach { (canonicalKey, originalHeader) ->
                val colIndex = rawData.headers.indexOfFirst { it.equals(originalHeader, ignoreCase = true) }
                if (colIndex in rowValues.indices) {
                    rowMap[canonicalKey] = rowValues[colIndex].trim()
                } else {
                    rowMap[canonicalKey] = ""
                }
            }

            val rowErrors = mutableListOf<String>()
            val rowWarnings = mutableListOf<String>()

            // Exam validation
            val rawExam = rowMap[KEY_EXAM].orEmpty()
            val examType = parseExamType(rawExam)
            if (examType == null) {
                rowErrors.add("Unsupported exam '$rawExam'. Must be JAMB, WAEC, or NECO.")
            }

            // Subject validation
            val rawSubject = rowMap[KEY_SUBJECT].orEmpty()
            val subjectType = parseSubjectType(rawSubject)
            if (subjectType == null) {
                rowErrors.add("Unknown subject '$rawSubject'. Supported subjects: Mathematics, English, Physics, Chemistry, Biology.")
            }

            // Year validation
            val rawYear = rowMap[KEY_YEAR].orEmpty()
            val yearInt = rawYear.toIntOrNull()
            if (yearInt == null || yearInt < 2000 || yearInt > 2050) {
                rowErrors.add("Invalid year '$rawYear'. Must be a 4-digit examination year between 2000 and 2050.")
            }

            // Topic validation
            val rawTopic = rowMap[KEY_TOPIC].orEmpty()
            if (rawTopic.isBlank()) {
                rowErrors.add("Topic cannot be blank.")
            }

            // Question text validation
            val rawQuestion = rowMap[KEY_QUESTION].orEmpty()
            if (rawQuestion.isBlank()) {
                rowErrors.add("Question text cannot be blank.")
            }

            // Options validation
            val rawOptA = rowMap[KEY_OPTION_A].orEmpty()
            val rawOptB = rowMap[KEY_OPTION_B].orEmpty()
            val rawOptC = rowMap[KEY_OPTION_C].orEmpty()
            val rawOptD = rowMap[KEY_OPTION_D].orEmpty()

            if (rawOptA.isBlank()) rowErrors.add("Option A cannot be blank.")
            if (rawOptB.isBlank()) rowErrors.add("Option B cannot be blank.")
            if (rawOptC.isBlank()) rowErrors.add("Option C cannot be blank.")
            if (rawOptD.isBlank()) rowErrors.add("Option D cannot be blank.")

            // Answer validation
            val rawAnswer = rowMap[KEY_ANSWER].orEmpty()
            val correctAnswer = parseAnswer(rawAnswer)
            if (correctAnswer == null) {
                rowErrors.add("Invalid answer '$rawAnswer'. Must be A, B, C, or D.")
            }

            // Explanation
            val explanation = rowMap[KEY_EXPLANATION].orEmpty()

            // Difficulty validation
            val rawDifficulty = rowMap[KEY_DIFFICULTY].orEmpty()
            val difficulty = parseDifficulty(rawDifficulty)
            if (difficulty == null && rawDifficulty.isNotBlank()) {
                rowErrors.add("Difficulty '$rawDifficulty' is invalid. Must be EASY, MEDIUM, or HARD.")
            }

            // Stage 7A: Provenance metadata parsing
            val rawSourceType = rowMap[KEY_SOURCE_TYPE].orEmpty()
            val sourceType = parseSourceType(rawSourceType) ?: QuestionSourceType.PRACTICE

            val sourceTitle = rowMap[KEY_SOURCE_TITLE].orEmpty()
            val sourceOrg = rowMap[KEY_SOURCE_ORGANIZATION].orEmpty()
            val sourceRef = rowMap[KEY_SOURCE_REFERENCE].orEmpty()
            val sourceUrl = rowMap[KEY_SOURCE_URL].orEmpty()
            val sourceYearInt = rowMap[KEY_SOURCE_YEAR]?.toIntOrNull()
            val sourcePage = rowMap[KEY_SOURCE_PAGE].orEmpty()
            val origQNum = rowMap[KEY_ORIGINAL_Q_NUM].orEmpty()

            // Provenance check flag: missing source title / reference
            val isMissingProvenance = sourceTitle.isBlank() && sourceRef.isBlank()
            if (isMissingProvenance) {
                rowWarnings.add("Missing source provenance (source reference/title). Defaulted to PRACTICE question pending review.")
                rowsWithMissingProvenanceCount++
            }

            // Imported questions must NOT become verified or active automatically!
            // They enter as DRAFT or PENDING_REVIEW and isActive = false
            val parsedVerificationStatus = parseVerificationStatus(rowMap[KEY_VERIFICATION_STATUS].orEmpty())
                ?: QuestionVerificationStatus.PENDING_REVIEW

            // If user supplied verified in spreadsheet, safety-downgrade to PENDING_REVIEW because
            // imported questions must not be verified automatically
            val finalVerificationStatus = if (parsedVerificationStatus == QuestionVerificationStatus.VERIFIED) {
                rowWarnings.add("Imported questions cannot be auto-verified. Status set to PENDING_REVIEW.")
                QuestionVerificationStatus.PENDING_REVIEW
            } else {
                parsedVerificationStatus
            }

            val parsedAnswerVerificationStatus = parseAnswerVerificationStatus(rowMap[KEY_ANSWER_VERIFICATION_STATUS].orEmpty())
                ?: AnswerVerificationStatus.UNVERIFIED

            val parsedPermissionStatus = parsePermissionStatus(rowMap[KEY_PERMISSION_STATUS].orEmpty())
                ?: ContentPermissionStatus.PENDING_VERIFICATION

            // Active status: imported questions must not become active automatically unless explicitly approved by admin
            val isActive = false

            var question: Question? = null
            var isDuplicate = false
            var duplicateReason: String? = null

            if (rowErrors.isEmpty() && examType != null && subjectType != null && yearInt != null && correctAnswer != null) {
                val candidate = Question(
                    id = currentId++,
                    examType = examType,
                    subject = subjectType,
                    year = yearInt,
                    topic = rawTopic,
                    questionText = rawQuestion,
                    optionA = rawOptA,
                    optionB = rawOptB,
                    optionC = rawOptC,
                    optionD = rawOptD,
                    correctAnswer = correctAnswer,
                    explanation = explanation,
                    difficulty = difficulty ?: Difficulty.MEDIUM,
                    isFree = true,
                    isActive = isActive,
                    createdAt = System.currentTimeMillis(),
                    createdBy = adminUid,
                    updatedAt = System.currentTimeMillis(),
                    updatedBy = adminUid,
                    // Stage 7A metadata
                    sourceType = sourceType,
                    sourceTitle = sourceTitle,
                    sourceOrganization = sourceOrg,
                    sourceReference = sourceRef,
                    sourceUrl = sourceUrl,
                    sourceYear = sourceYearInt,
                    sourcePage = sourcePage,
                    originalQuestionNumber = origQNum,
                    verificationStatus = finalVerificationStatus,
                    answerVerificationStatus = parsedAnswerVerificationStatus,
                    verifiedBy = "",
                    verifiedAt = null,
                    reviewNotes = "Imported from $fileName on ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}",
                    contentPermissionStatus = parsedPermissionStatus
                )

                // Validate with authoritative QuestionValidator
                val validation = QuestionValidator.validate(candidate)
                if (!validation.isValid) {
                    val reason = (validation as? ValidationResult.Invalid)?.reason ?: "Question validation failed"
                    rowErrors.add(reason)
                } else {
                    question = candidate

                    // Duplicate detection
                    val fingerprint = generateFingerprint(examType, subjectType, yearInt, rawQuestion)
                    if (fileFingerprints.contains(fingerprint)) {
                        isDuplicate = true
                        duplicateReason = "Duplicate question within this file"
                    } else if (existingFingerprints.contains(fingerprint)) {
                        isDuplicate = true
                        duplicateReason = "Question already exists in Question Bank"
                    } else {
                        fileFingerprints.add(fingerprint)
                    }
                }
            }

            processedRows.add(
                ImportRow(
                    rowNumber = spreadsheetRowNumber,
                    rawValues = rowMap,
                    question = question,
                    errors = rowErrors,
                    warnings = rowWarnings,
                    isDuplicate = isDuplicate,
                    duplicateReason = duplicateReason,
                    missingSourceMetadata = isMissingProvenance
                )
            )
        }

        val validCount = processedRows.count { it.isValid }
        val invalidCount = processedRows.count { !it.isValid }
        val duplicateCount = processedRows.count { it.isDuplicate }
        val importableCount = processedRows.count { it.isImportable }

        val summary = ImportSummary(
            fileName = fileName,
            fileType = fileType,
            totalRows = processedRows.size,
            validRows = validCount,
            invalidRows = invalidCount,
            duplicateRows = duplicateCount,
            importableRows = importableCount,
            rowsWithMissingProvenance = rowsWithMissingProvenanceCount,
            headerMappings = mappingResult.detectedMappings,
            missingHeaders = emptyList()
        )

        return Pair(summary, processedRows)
    }

    /**
     * Maps headers to canonical keys tolerantly.
     */
    fun validateAndMapHeaders(headers: List<String>): HeaderMappingResult {
        val detected = HashMap<String, String>()
        val missing = mutableListOf<String>()

        REQUIRED_KEYS.forEach { key ->
            val match = findMatchingHeader(key, headers)
            if (match != null) {
                detected[key] = match
            } else {
                missing.add(DISPLAY_NAMES[key] ?: key)
            }
        }

        // Optional headers including Stage 7A columns
        listOf(
            KEY_EXPLANATION, KEY_DIFFICULTY, KEY_ACTIVE,
            KEY_SOURCE_TYPE, KEY_SOURCE_TITLE, KEY_SOURCE_ORGANIZATION,
            KEY_SOURCE_REFERENCE, KEY_SOURCE_URL, KEY_SOURCE_YEAR, KEY_SOURCE_PAGE,
            KEY_ORIGINAL_Q_NUM, KEY_VERIFICATION_STATUS, KEY_ANSWER_VERIFICATION_STATUS,
            KEY_PERMISSION_STATUS
        ).forEach { key ->
            val match = findMatchingHeader(key, headers)
            if (match != null) {
                detected[key] = match
            }
        }

        return HeaderMappingResult(
            detectedMappings = detected,
            missingRequiredHeaders = missing
        )
    }

    private fun findMatchingHeader(canonicalKey: String, headers: List<String>): String? {
        val synonyms = when (canonicalKey) {
            KEY_EXAM -> listOf("exam", "examtype", "exam_type", "exam type", "examination", "exam body")
            KEY_SUBJECT -> listOf("subject", "subjecttype", "subject_type", "subject type", "course", "subject name")
            KEY_YEAR -> listOf("year", "examyear", "exam_year", "exam year", "examination year")
            KEY_TOPIC -> listOf("topic", "syllabus topic", "question topic", "theme", "unit")
            KEY_QUESTION -> listOf("question", "questiontext", "question_text", "question text", "prompt", "item")
            KEY_OPTION_A -> listOf("optiona", "option a", "option_a", "opt a", "opta", "a")
            KEY_OPTION_B -> listOf("optionb", "option b", "option_b", "opt b", "optb", "b")
            KEY_OPTION_C -> listOf("optionc", "option c", "option_c", "opt c", "optc", "c")
            KEY_OPTION_D -> listOf("optiond", "option d", "option_d", "opt d", "optd", "d")
            KEY_ANSWER -> listOf("answer", "correctanswer", "correct_answer", "correct answer", "correct option", "correct", "ans")
            KEY_EXPLANATION -> listOf("explanation", "solution", "reason", "rationale", "explain")
            KEY_DIFFICULTY -> listOf("difficulty", "difficultylevel", "difficulty_level", "difficulty level", "level")
            KEY_ACTIVE -> listOf("active", "isactive", "is_active", "is active", "status", "enabled")
            // Stage 7A synonyms
            KEY_SOURCE_TYPE -> listOf("sourcetype", "source_type", "source type", "origin", "question type", "type")
            KEY_SOURCE_TITLE -> listOf("sourcetitle", "source_title", "source title", "publication", "title")
            KEY_SOURCE_ORGANIZATION -> listOf("sourceorganization", "source_organization", "source org", "organization", "body")
            KEY_SOURCE_REFERENCE -> listOf("sourcereference", "source_reference", "source ref", "citation", "reference")
            KEY_SOURCE_URL -> listOf("sourceurl", "source_url", "source url", "link", "url")
            KEY_SOURCE_YEAR -> listOf("sourceyear", "source_year", "source year", "original year")
            KEY_SOURCE_PAGE -> listOf("sourcepage", "source_page", "source page", "page")
            KEY_ORIGINAL_Q_NUM -> listOf("originalquestionnumber", "original_question_number", "original q num", "qnum", "original number")
            KEY_VERIFICATION_STATUS -> listOf("verificationstatus", "verification_status", "verification status", "review status")
            KEY_ANSWER_VERIFICATION_STATUS -> listOf("answerverificationstatus", "answer_verification_status", "answer status", "ans review")
            KEY_PERMISSION_STATUS -> listOf("contentpermissionstatus", "content_permission_status", "permission", "copyright status")
            else -> listOf(canonicalKey)
        }

        return headers.firstOrNull { header ->
            val normalized = header.lowercase().replace(" ", "").replace("_", "").trim()
            synonyms.any { syn ->
                val normalizedSyn = syn.lowercase().replace(" ", "").replace("_", "").trim()
                normalized == normalizedSyn
            }
        }
    }

    fun parseExamType(value: String): ExamType? {
        val clean = value.uppercase().trim()
        return when {
            clean.contains("JAMB") || clean.contains("UTME") -> ExamType.JAMB
            clean.contains("WAEC") || clean.contains("WASSCE") -> ExamType.WAEC
            clean.contains("NECO") || clean.contains("SSCE") -> ExamType.NECO
            else -> null
        }
    }

    fun parseSubjectType(value: String): SubjectType? {
        val clean = value.lowercase().trim()
        return when {
            clean.contains("math") -> SubjectType.MATHEMATICS
            clean.contains("english") -> SubjectType.ENGLISH
            clean.contains("physic") -> SubjectType.PHYSICS
            clean.contains("chemist") -> SubjectType.CHEMISTRY
            clean.contains("biolog") -> SubjectType.BIOLOGY
            clean.contains("govern") -> SubjectType.GOVERNMENT
            clean.contains("crs") || clean.contains("christian") -> SubjectType.CRS
            clean.contains("irs") || clean.contains("islam") -> SubjectType.IRS
            clean.contains("lit") -> SubjectType.LITERATURE
            clean.contains("commerc") -> SubjectType.COMMERCE
            clean.contains("econom") -> SubjectType.ECONOMICS
            clean.contains("geograph") -> SubjectType.GEOGRAPHY
            clean.contains("agric") -> SubjectType.AGRICULTURAL_SCIENCE
            clean.contains("civic") -> SubjectType.CIVIC_EDUCATION
            else -> null
        }
    }

    fun parseAnswer(value: String): OptionLetter? {
        val clean = value.trim().uppercase()
        return when (clean) {
            "A", "OPTION A", "OPTIONA" -> OptionLetter.A
            "B", "OPTION B", "OPTIONB" -> OptionLetter.B
            "C", "OPTION C", "OPTIONC" -> OptionLetter.C
            "D", "OPTION D", "OPTIOND" -> OptionLetter.D
            else -> null
        }
    }

    fun parseDifficulty(value: String): Difficulty? {
        val clean = value.trim().uppercase()
        return when {
            clean.contains("EASY") -> Difficulty.EASY
            clean.contains("MED") -> Difficulty.MEDIUM
            clean.contains("HARD") || clean.contains("DIFF") -> Difficulty.HARD
            else -> null
        }
    }

    fun parseSourceType(value: String): QuestionSourceType? {
        val clean = value.trim().uppercase()
        return when {
            clean.contains("OFFICIAL") -> QuestionSourceType.OFFICIAL
            clean.contains("AUTH") || clean.contains("PUB") -> QuestionSourceType.AUTHORIZED_PUBLICATION
            clean.contains("USER") -> QuestionSourceType.USER_SUPPLIED_WITH_SOURCE
            clean.contains("PRACTICE") -> QuestionSourceType.PRACTICE
            else -> null
        }
    }

    fun parseVerificationStatus(value: String): QuestionVerificationStatus? {
        val clean = value.trim().uppercase()
        return when {
            clean.contains("VERIF") -> QuestionVerificationStatus.VERIFIED
            clean.contains("PEND") -> QuestionVerificationStatus.PENDING_REVIEW
            clean.contains("DRAFT") -> QuestionVerificationStatus.DRAFT
            clean.contains("REJ") -> QuestionVerificationStatus.REJECTED
            clean.contains("UNVER") -> QuestionVerificationStatus.UNVERIFIED
            else -> null
        }
    }

    fun parseAnswerVerificationStatus(value: String): AnswerVerificationStatus? {
        val clean = value.trim().uppercase()
        return when {
            clean.contains("REV") -> AnswerVerificationStatus.REVIEWED
            clean.contains("DISP") -> AnswerVerificationStatus.DISPUTED
            clean.contains("UNVER") -> AnswerVerificationStatus.UNVERIFIED
            else -> null
        }
    }

    fun parsePermissionStatus(value: String): ContentPermissionStatus? {
        val clean = value.trim().uppercase()
        return when {
            clean.contains("PUBLIC") -> ContentPermissionStatus.PUBLIC_DOMAIN_PAST_EXAM
            clean.contains("AUTH") -> ContentPermissionStatus.AUTHORIZED
            clean.contains("REST") -> ContentPermissionStatus.RESTRICTED
            else -> ContentPermissionStatus.PENDING_VERIFICATION
        }
    }

    private fun generateFingerprint(
        examType: ExamType,
        subject: SubjectType,
        year: Int,
        questionText: String
    ): String {
        val normalizedText = questionText.lowercase().replace(Regex("[^a-z0-9]"), "")
        return "${examType.id}_${subject.id}_${year}_${normalizedText.take(64)}"
    }
}
