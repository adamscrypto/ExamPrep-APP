package com.success.examprep.ui.screens.admin.importing

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.success.examprep.data.auth.AuthRepository
import com.success.examprep.data.importer.ImportProgress
import com.success.examprep.data.importer.ImportResult
import com.success.examprep.data.importer.ImportRow
import com.success.examprep.data.importer.ImportSummary
import com.success.examprep.data.importer.QuestionImportService
import com.success.examprep.data.importer.SpreadsheetType
import com.success.examprep.data.model.Question
import com.success.examprep.data.repository.QuestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RowFilter(val label: String) {
    ALL("All Rows"),
    VALID("Valid"),
    MISSING_PROVENANCE("Missing Provenance"),
    ERRORS("Errors"),
    DUPLICATES("Duplicates")
}

sealed interface QuestionImportUiState {
    data object Idle : QuestionImportUiState

    data class Parsing(val fileName: String) : QuestionImportUiState

    data class PreviewReady(
        val summary: ImportSummary,
        val allRows: List<ImportRow>,
        val filteredRows: List<ImportRow>,
        val activeFilter: RowFilter = RowFilter.ALL,
        val selectedRowForDetail: ImportRow? = null,
        val isConfirmDialogOpen: Boolean = false,
        val importAllOrNothing: Boolean = false
    ) : QuestionImportUiState

    data class Importing(
        val fileName: String,
        val progress: ImportProgress
    ) : QuestionImportUiState

    data class Completed(
        val result: ImportResult
    ) : QuestionImportUiState

    data class Error(
        val message: String
    ) : QuestionImportUiState
}

class QuestionImportViewModel(
    private val authRepository: AuthRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionImportUiState>(QuestionImportUiState.Idle)
    val uiState: StateFlow<QuestionImportUiState> = _uiState.asStateFlow()

    fun onFileSelected(uri: Uri, fileName: String, contentResolver: ContentResolver) {
        val detectedType = SpreadsheetType.fromFileName(fileName)
        if (detectedType == null) {
            _uiState.value = QuestionImportUiState.Error(
                "Unsupported file format. Please choose an Excel (.xlsx) or CSV (.csv) spreadsheet."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = QuestionImportUiState.Parsing(fileName)
            try {
                val adminUser = authRepository.getCurrentUser()
                val adminUid = adminUser?.uid ?: "ADMIN_${System.currentTimeMillis()}"

                withContext(Dispatchers.IO) {
                    val existingQuestions = questionRepository.getAllQuestions(includeInactive = true)
                    val maxExistingId = existingQuestions.maxOfOrNull { it.id } ?: 1000

                    val stream = contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Could not open input stream for selected file.")

                    stream.use { inputStream ->
                        val (summary, rows) = QuestionImportService.processSpreadsheet(
                            inputStream = inputStream,
                            fileName = fileName,
                            adminUid = adminUid,
                            existingQuestions = existingQuestions,
                            startingQuestionId = maxExistingId + 1
                        )

                        _uiState.value = QuestionImportUiState.PreviewReady(
                            summary = summary,
                            allRows = rows,
                            filteredRows = rows,
                            activeFilter = RowFilter.ALL
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = QuestionImportUiState.Error(
                    e.message ?: "Failed to read and parse the selected spreadsheet."
                )
            }
        }
    }

    fun setFilter(filter: RowFilter) {
        val current = _uiState.value as? QuestionImportUiState.PreviewReady ?: return
        val filtered = when (filter) {
            RowFilter.ALL -> current.allRows
            RowFilter.VALID -> current.allRows.filter { it.isImportable }
            RowFilter.MISSING_PROVENANCE -> current.allRows.filter { it.missingSourceMetadata }
            RowFilter.ERRORS -> current.allRows.filter { !it.isValid }
            RowFilter.DUPLICATES -> current.allRows.filter { it.isDuplicate }
        }
        _uiState.update {
            current.copy(
                activeFilter = filter,
                filteredRows = filtered
            )
        }
    }

    fun openRowDetail(row: ImportRow?) {
        val current = _uiState.value as? QuestionImportUiState.PreviewReady ?: return
        _uiState.update { current.copy(selectedRowForDetail = row) }
    }

    fun openConfirmDialog() {
        val current = _uiState.value as? QuestionImportUiState.PreviewReady ?: return
        _uiState.update { current.copy(isConfirmDialogOpen = true) }
    }

    fun closeConfirmDialog() {
        val current = _uiState.value as? QuestionImportUiState.PreviewReady ?: return
        _uiState.update { current.copy(isConfirmDialogOpen = false) }
    }

    fun confirmAndStartImport(onImportSuccess: () -> Unit) {
        val current = _uiState.value as? QuestionImportUiState.PreviewReady ?: return
        closeConfirmDialog()

        val importableRows = current.allRows.filter { it.isImportable }
        if (importableRows.isEmpty()) {
            _uiState.value = QuestionImportUiState.Error("No valid, non-duplicate questions found to import.")
            return
        }

        val questionsToImport: List<Question> = importableRows.mapNotNull { it.question }
        val totalCount = questionsToImport.size
        val chunkSize = 300
        val totalBatches = (totalCount + chunkSize - 1) / chunkSize

        viewModelScope.launch {
            _uiState.value = QuestionImportUiState.Importing(
                fileName = current.summary.fileName,
                progress = ImportProgress(
                    processedCount = 0,
                    totalCount = totalCount,
                    currentBatch = 1,
                    totalBatches = totalBatches
                )
            )

            val result = withContext(Dispatchers.IO) {
                questionRepository.bulkAddQuestions(questionsToImport) { processed, total ->
                    val currentBatch = ((processed / chunkSize) + 1).coerceAtMost(totalBatches)
                    _uiState.value = QuestionImportUiState.Importing(
                        fileName = current.summary.fileName,
                        progress = ImportProgress(
                            processedCount = processed,
                            totalCount = total,
                            currentBatch = currentBatch,
                            totalBatches = totalBatches
                        )
                    )
                }
            }

            if (result.isSuccess) {
                val importedCount = result.getOrDefault(totalCount)
                _uiState.value = QuestionImportUiState.Completed(
                    result = ImportResult(
                        fileName = current.summary.fileName,
                        totalRows = current.summary.totalRows,
                        importedCount = importedCount,
                        skippedDuplicatesCount = current.summary.duplicateRows,
                        failedCount = current.summary.invalidRows
                    )
                )
                onImportSuccess()
            } else {
                _uiState.value = QuestionImportUiState.Error(
                    result.exceptionOrNull()?.message ?: "An error occurred while saving imported questions."
                )
            }
        }
    }

    fun reset() {
        _uiState.value = QuestionImportUiState.Idle
    }
}
