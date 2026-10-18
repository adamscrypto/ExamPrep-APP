package com.success.examprep.data.importer

import com.success.examprep.data.model.Question

/**
 * Supported spreadsheet formats for bulk question import.
 */
enum class SpreadsheetType(val extension: String, val displayName: String) {
    CSV("csv", "CSV (.csv)"),
    XLSX("xlsx", "Excel (.xlsx)");

    companion object {
        fun fromFileName(name: String?): SpreadsheetType? {
            if (name == null) return null
            val lower = name.lowercase().trim()
            return when {
                lower.endsWith(".xlsx") -> XLSX
                lower.endsWith(".csv") -> CSV
                else -> null
            }
        }
    }
}

/**
 * Raw tabular data extracted from a spreadsheet.
 */
data class RawSpreadsheetData(
    val headers: List<String>,
    val rows: List<List<String>>
)

/**
 * Result of mapping raw spreadsheet columns to Question model attributes.
 */
data class HeaderMappingResult(
    val detectedMappings: Map<String, String>, // Normalized Header -> Column Name in File
    val missingRequiredHeaders: List<String>,
    val isSuccess: Boolean = missingRequiredHeaders.isEmpty()
)

/**
 * Represents a single processed row from an imported file.
 * Used exclusively for preview, validation, and user feedback.
 */
data class ImportRow(
    val rowNumber: Int, // 1-based spreadsheet row number (Row 1 is headers, Row 2 is first question)
    val rawValues: Map<String, String>,
    val question: Question?,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val isDuplicate: Boolean = false,
    val duplicateReason: String? = null,
    val missingSourceMetadata: Boolean = false
) {
    val isValid: Boolean get() = errors.isEmpty() && question != null
    val isImportable: Boolean get() = isValid && !isDuplicate
}

/**
 * Summary of spreadsheet analysis prior to committing writes to Firestore.
 */
data class ImportSummary(
    val fileName: String,
    val fileType: SpreadsheetType,
    val totalRows: Int,
    val validRows: Int,
    val invalidRows: Int,
    val duplicateRows: Int,
    val importableRows: Int,
    val rowsWithMissingProvenance: Int = 0,
    val headerMappings: Map<String, String> = emptyMap(),
    val missingHeaders: List<String> = emptyList()
)

/**
 * Real-time progress update during Firestore batch commits.
 */
data class ImportProgress(
    val processedCount: Int,
    val totalCount: Int,
    val currentBatch: Int,
    val totalBatches: Int,
    val percentage: Float = if (totalCount > 0) processedCount.toFloat() / totalCount else 0f
)

/**
 * Final execution report after import transaction completes.
 */
data class ImportResult(
    val fileName: String,
    val totalRows: Int,
    val importedCount: Int,
    val skippedDuplicatesCount: Int,
    val failedCount: Int,
    val errorMessages: List<String> = emptyList()
)
