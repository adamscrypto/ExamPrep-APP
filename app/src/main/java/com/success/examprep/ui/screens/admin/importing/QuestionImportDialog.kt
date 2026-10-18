package com.success.examprep.ui.screens.admin.importing

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.success.examprep.data.importer.ImportProgress
import com.success.examprep.data.importer.ImportResult
import com.success.examprep.data.importer.ImportRow
import com.success.examprep.data.importer.ImportSummary
import com.success.examprep.data.importer.QuestionImportService
import com.success.examprep.ui.screens.admin.QuestionPreviewDialog
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import com.success.examprep.ui.theme.TealSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionImportDialog(
    viewModel: QuestionImportViewModel,
    onDismiss: () -> Unit,
    onImportSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "questions.xlsx"
            viewModel.onFileSelected(uri, fileName, context.contentResolver)
        }
    }

    Dialog(
        onDismissRequest = {
            if (uiState !is QuestionImportUiState.Importing) {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Bulk Question Import",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Excel (.xlsx) and CSV (.csv) Importer",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            enabled = uiState !is QuestionImportUiState.Importing,
                            modifier = Modifier.testTag("import_dialog_close_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Import Dialog")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("question_import_dialog_content")
            ) {
                when (val state = uiState) {
                    is QuestionImportUiState.Idle -> {
                        ImportIdleView(
                            onSelectFile = {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "text/csv",
                                        "text/comma-separated-values",
                                        "application/csv",
                                        "text/plain",
                                        "*/*"
                                    )
                                )
                            }
                        )
                    }

                    is QuestionImportUiState.Parsing -> {
                        ImportParsingView(fileName = state.fileName)
                    }

                    is QuestionImportUiState.PreviewReady -> {
                        ImportPreviewView(
                            summary = state.summary,
                            filteredRows = state.filteredRows,
                            activeFilter = state.activeFilter,
                            onSelectFilter = { viewModel.setFilter(it) },
                            onRowClick = { viewModel.openRowDetail(it) },
                            onCancel = onDismiss,
                            onStartImport = { viewModel.openConfirmDialog() }
                        )

                        // Question Preview Dialog
                        state.selectedRowForDetail?.question?.let { question ->
                            QuestionPreviewDialog(
                                question = question,
                                onDismiss = { viewModel.openRowDetail(null) }
                            )
                        }

                        // Confirmation Dialog
                        if (state.isConfirmDialogOpen) {
                            ImportConfirmationDialog(
                                summary = state.summary,
                                onDismiss = { viewModel.closeConfirmDialog() },
                                onConfirm = { viewModel.confirmAndStartImport(onImportSuccess) }
                            )
                        }
                    }

                    is QuestionImportUiState.Importing -> {
                        ImportProgressView(
                            fileName = state.fileName,
                            progress = state.progress
                        )
                    }

                    is QuestionImportUiState.Completed -> {
                        ImportCompletedView(
                            result = state.result,
                            onDone = {
                                viewModel.reset()
                                onDismiss()
                            }
                        )
                    }

                    is QuestionImportUiState.Error -> {
                        ImportErrorView(
                            errorMessage = state.message,
                            onRetry = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImportIdleView(
    onSelectFile: () -> Unit
) {
    var showTemplateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = EmeraldGreenPrimary.copy(alpha = 0.12f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    tint = EmeraldGreenPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Import Questions to Question Bank",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Upload examination questions from Excel (.xlsx) or CSV (.csv) files. All questions will be validated against JAMB, WAEC, and NECO syllabi before ingestion.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSelectFile,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
                .testTag("import_select_file_button")
        ) {
            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Excel or CSV File", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = { showTemplateDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .testTag("import_view_template_button")
        ) {
            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = EmeraldGreenDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Standard Template & Schema", color = EmeraldGreenDark, fontWeight = FontWeight.SemiBold)
        }
    }

    if (showTemplateDialog) {
        TemplateFormatDialog(onDismiss = { showTemplateDialog = false })
    }
}

@Composable
fun ImportParsingView(fileName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = EmeraldGreenPrimary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Analyzing $fileName",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Parsing spreadsheet rows, verifying headers, and checking for duplicates...",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
fun ImportPreviewView(
    summary: ImportSummary,
    filteredRows: List<ImportRow>,
    activeFilter: RowFilter,
    onSelectFilter: (RowFilter) -> Unit,
    onRowClick: (ImportRow) -> Unit,
    onCancel: () -> Unit,
    onStartImport: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Header Card
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.fileName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${summary.totalRows} Total Rows in Spreadsheet",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (summary.fileType.extension) {
                            "xlsx" -> EmeraldGreenPrimary.copy(alpha = 0.15f)
                            else -> TealSecondary.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = summary.fileType.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SummaryMetricChip(
                        title = "Valid",
                        count = summary.importableRows,
                        color = EmeraldGreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricChip(
                        title = "No Provenance",
                        count = summary.rowsWithMissingProvenance,
                        color = Color(0xFFB45309),
                        modifier = Modifier.weight(1.2f)
                    )
                    SummaryMetricChip(
                        title = "Duplicates",
                        count = summary.duplicateRows,
                        color = AmberTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricChip(
                        title = "Errors",
                        count = summary.invalidRows,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (summary.missingHeaders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Missing Required Column(s): ${summary.missingHeaders.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Filter Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(RowFilter.entries) { filter ->
                val count = when (filter) {
                    RowFilter.ALL -> summary.totalRows
                    RowFilter.VALID -> summary.importableRows
                    RowFilter.MISSING_PROVENANCE -> summary.rowsWithMissingProvenance
                    RowFilter.ERRORS -> summary.invalidRows
                    RowFilter.DUPLICATES -> summary.duplicateRows
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (activeFilter == filter) EmeraldGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onSelectFilter(filter) }
                        .testTag("filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = "${filter.label} ($count)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (activeFilter == filter) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeFilter == filter) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Row List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredRows, key = { it.rowNumber }) { row ->
                ImportRowItemCard(row = row, onClick = { onRowClick(row) })
            }
        }

        // Bottom Action Bar
        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onStartImport,
                    enabled = summary.importableRows > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("confirm_import_button")
                ) {
                    Text("Import ${summary.importableRows} Valid Questions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun SummaryMetricChip(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
fun ImportRowItemCard(
    row: ImportRow,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !row.isValid -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                row.isDuplicate -> AmberTertiary.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("import_row_${row.rowNumber}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Row ${row.rowNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val exam = row.question?.examType?.name ?: row.rawValues[QuestionImportService.KEY_EXAM] ?: ""
                    val subject = row.question?.subject?.displayName ?: row.rawValues[QuestionImportService.KEY_SUBJECT] ?: ""
                    if (exam.isNotBlank()) {
                        Text(
                            text = "$exam • $subject",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreenDark
                            )
                        )
                    }
                }

                // Status Badge
                when {
                    !row.isValid -> {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.error) {
                            Text(
                                text = "ERROR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    row.isDuplicate -> {
                        Surface(shape = RoundedCornerShape(6.dp), color = AmberTertiary) {
                            Text(
                                text = "DUPLICATE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldGreenPrimary) {
                            Text(
                                text = "VALID",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val questionText = row.question?.questionText
                ?: row.rawValues[QuestionImportService.KEY_QUESTION]
                ?: "Empty Question"
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Errors Report
            if (row.errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.errors.forEach { err ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = err,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            // Duplicate Note
            if (row.isDuplicate && row.duplicateReason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AmberTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${row.duplicateReason} (Will be skipped)",
                        style = MaterialTheme.typography.labelSmall.copy(color = AmberTertiary, fontWeight = FontWeight.Medium)
                    )
                }
            }

            // Stage 7A Warnings (Missing provenance / defaults)
            if (row.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.warnings.forEach { warning ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = EmeraldGreenDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldGreenDark, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportProgressView(
    fileName: String,
    progress: ImportProgress
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = EmeraldGreenPrimary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Importing Questions to Firestore...",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Batch ${progress.currentBatch} of ${progress.totalBatches}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = EmeraldGreenDark)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress.percentage },
            color = EmeraldGreenPrimary,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${progress.processedCount} / ${progress.totalCount} Questions Committed",
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
fun ImportCompletedView(
    result: ImportResult,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = EmeraldGreenPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldGreenPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Import Completed Successfully!",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = EmeraldGreenDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Questions have been validated and saved to the CBT Question Bank.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportRow(label = "Spreadsheet File", value = result.fileName)
                ReportRow(label = "Total Rows Processed", value = result.totalRows.toString())
                ReportRow(label = "Successfully Imported", value = result.importedCount.toString(), isHighlight = true)
                ReportRow(label = "Duplicates Skipped", value = result.skippedDuplicatesCount.toString())
                ReportRow(label = "Invalid Rows Skipped", value = result.failedCount.toString())
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
                .testTag("import_done_button")
        ) {
            Text("Done & Refresh Question Bank", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReportRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isHighlight) EmeraldGreenDark else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun ImportErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Unable to Process File",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Select Another File")
        }
    }
}

@Composable
fun ImportConfirmationDialog(
    summary: ImportSummary,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Bulk Import", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("You are about to add ${summary.importableRows} valid question(s) to the Question Bank.")
                if (summary.duplicateRows > 0) {
                    Text(
                        text = "• ${summary.duplicateRows} duplicate question(s) will be safely skipped.",
                        style = MaterialTheme.typography.bodySmall.copy(color = AmberTertiary)
                    )
                }
                if (summary.invalidRows > 0) {
                    Text(
                        text = "• ${summary.invalidRows} row(s) containing errors will not be imported.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Imported questions will immediately become available in the CBT system according to standard access rules.",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_import_dialog_ok")
            ) {
                Text("Confirm & Ingest")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TemplateFormatDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Standard Spreadsheet Schema", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Prepare your Excel (.xlsx) or CSV (.csv) file with the following column headers:",
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Exam, Subject, Year, Topic, Question, OptionA, OptionB, OptionC, OptionD, Answer, Explanation, Difficulty, Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark
                            )
                        )
                    }
                }

                Text(
                    text = "Accepted Values:\n" +
                        "• Exam: JAMB, WAEC, NECO\n" +
                        "• Subject: Mathematics, English, Physics, Chemistry, Biology\n" +
                        "• Year: 2000 – 2050\n" +
                        "• Answer: A, B, C, or D\n" +
                        "• Difficulty: EASY, MEDIUM, or HARD\n" +
                        "• Active: TRUE or FALSE",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text("Close")
            }
        }
    )
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = it.getString(index)
                }
            }
        }
    }
    if (name == null) {
        name = uri.path?.substringAfterLast('/')
    }
    return name
}
