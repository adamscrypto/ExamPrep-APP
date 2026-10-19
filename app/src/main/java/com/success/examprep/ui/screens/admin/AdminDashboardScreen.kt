package com.success.examprep.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.input.KeyboardType
import com.success.examprep.data.model.PaymentSettings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import com.success.examprep.data.auth.StudentPinManager
import com.success.examprep.data.firebase.FirebaseDiagnostics
import com.success.examprep.data.firebase.FirebaseDiagnosticReport
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.model.AccessEntitlement
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.EntitlementStatus
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.QuestionSourceType
import com.success.examprep.data.model.QuestionVerificationStatus
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.SubjectType
import com.success.examprep.ui.screens.admin.importing.QuestionImportDialog
import com.success.examprep.ui.screens.admin.importing.QuestionImportViewModel
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import com.success.examprep.ui.theme.TealSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit = onNavigateBack
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()
    var diagnosticReport by remember { mutableStateOf<FirebaseDiagnosticReport?>(null) }
    var isRunningDiagnostic by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedbackMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedbackMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreenDark,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Admin Dashboard",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = EmeraldGreenPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "SUPER ADMIN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = EmeraldGreenDark
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "ExamPrep CBT Management System",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadAllData() },
                        modifier = Modifier.testTag("admin_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data"
                        )
                    }
                    IconButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.testTag("admin_signout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            when (uiState.selectedTab) {
                AdminTab.QUESTIONS -> {
                    FloatingActionButton(
                        onClick = { viewModel.openAddQuestionDialog() },
                        containerColor = EmeraldGreenPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("admin_add_question_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Question")
                    }
                }
                AdminTab.STUDENTS, AdminTab.ACCESS -> {
                    FloatingActionButton(
                        onClick = { viewModel.openGrantAccessDialog(null) },
                        containerColor = EmeraldGreenPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("admin_grant_access_fab")
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Grant Access")
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("admin_dashboard_screen")
        ) {
            // M3 Tab Navigation
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldGreenPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdminTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldGreenPrimary)
                }
            } else {
                when (uiState.selectedTab) {
                    AdminTab.OVERVIEW -> AdminOverviewTab(
                        stats = uiState.stats,
                        onNavigateToQuestions = { viewModel.selectTab(AdminTab.QUESTIONS) },
                        onNavigateToQualityControl = { viewModel.selectTab(AdminTab.QUALITY_CONTROL) },
                        onNavigateToStudents = { viewModel.selectTab(AdminTab.STUDENTS) },
                        onNavigateToAccess = { viewModel.selectTab(AdminTab.ACCESS) },
                        onAddQuestion = { viewModel.openAddQuestionDialog() },
                        onImportQuestions = { viewModel.openImportDialog() },
                        onGrantAccess = { viewModel.openGrantAccessDialog() },
                        onRunDiagnostics = {
                            coroutineScope.launch {
                                isRunningDiagnostic = true
                                diagnosticReport = FirebaseDiagnostics.runDiagnostic(context)
                                isRunningDiagnostic = false
                            }
                        }
                    )
                    AdminTab.QUESTIONS -> AdminQuestionBankTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    AdminTab.QUALITY_CONTROL -> AdminQualityControlTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    AdminTab.STUDENTS -> AdminStudentsTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    AdminTab.ACCESS -> AdminAccessManagementTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    AdminTab.PAYMENT_SETTINGS -> AdminPaymentSettingsTab(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }

    // Question Preview Dialog
    uiState.questionToPreview?.let { question ->
        QuestionPreviewDialog(
            question = question,
            onDismiss = { viewModel.closeQuestionPreview() }
        )
    }

    // Stage 7A: Question Verification & Editorial Review Dialog
    uiState.questionToReview?.let { question ->
        QuestionVerificationReviewDialog(
            question = question,
            onDismiss = { viewModel.closeQuestionReview() },
            onSubmitReview = { status, answerStatus, shouldPublish, notes, correctedAnswer ->
                viewModel.performQuestionReview(
                    question = question,
                    newVerificationStatus = status,
                    newAnswerVerificationStatus = answerStatus,
                    shouldPublish = shouldPublish,
                    reviewNotes = notes,
                    correctedAnswer = correctedAnswer
                )
            }
        )
    }

    // Add Question Dialog
    if (uiState.isAddQuestionDialogOpen) {
        QuestionFormDialog(
            initialQuestion = null,
            onDismiss = { viewModel.closeAddQuestionDialog() },
            onSave = { examType, subject, year, topic, questionText, optionA, optionB, optionC, optionD, correctAnswer, explanation, difficulty, isFree, isActive,
                       sourceType, sourceTitle, sourceOrganization, sourceReference, sourceUrl, sourceYear, sourcePage, originalQuestionNumber, verificationStatus, answerVerificationStatus, reviewNotes, contentPermissionStatus ->
                viewModel.addQuestion(
                    examType, subject, year, topic, questionText,
                    optionA, optionB, optionC, optionD, correctAnswer, explanation, difficulty, isFree, isActive,
                    sourceType, sourceTitle, sourceOrganization, sourceReference, sourceUrl, sourceYear, sourcePage, originalQuestionNumber, verificationStatus, answerVerificationStatus, reviewNotes, contentPermissionStatus
                )
            }
        )
    }

    // Edit Question Dialog
    uiState.questionToEdit?.let { question ->
        QuestionFormDialog(
            initialQuestion = question,
            onDismiss = { viewModel.closeEditQuestionDialog() },
            onSave = { examType, subject, year, topic, questionText, optionA, optionB, optionC, optionD, correctAnswer, explanation, difficulty, isFree, isActive,
                       sourceType, sourceTitle, sourceOrganization, sourceReference, sourceUrl, sourceYear, sourcePage, originalQuestionNumber, verificationStatus, answerVerificationStatus, reviewNotes, contentPermissionStatus ->
                viewModel.updateQuestion(
                    question.id,
                    examType, subject, year, topic, questionText,
                    optionA, optionB, optionC, optionD, correctAnswer, explanation, difficulty, isFree, isActive,
                    sourceType, sourceTitle, sourceOrganization, sourceReference, sourceUrl, sourceYear, sourcePage, originalQuestionNumber, verificationStatus, answerVerificationStatus, reviewNotes, contentPermissionStatus
                )
            }
        )
    }

    // Grant Access Dialog
    if (uiState.isGrantAccessDialogOpen) {
        GrantAccessDialog(
            students = uiState.students,
            preselectedStudent = uiState.grantTargetStudent,
            onDismiss = { viewModel.closeGrantAccessDialog() },
            onGrant = { student, exam, subjects, isExamLevel, reason, duration ->
                viewModel.grantExamAccess(student, exam, subjects, isExamLevel, reason, duration)
            }
        )
    }

    // Student Detail Dialog
    uiState.selectedStudent?.let { student ->
        StudentDetailDialog(
            student = student,
            entitlements = uiState.selectedStudentEntitlements,
            onDismiss = { viewModel.closeStudentDetail() },
            onGrantAccess = {
                viewModel.closeStudentDetail()
                viewModel.openGrantAccessDialog(student)
            },
            onRevokeEntitlement = { entitlement ->
                viewModel.revokeAccess(entitlement)
            },
            onResetPin = { newPin ->
                viewModel.resetStudentPin(student, newPin)
            },
            onTransferDevice = { transferPin ->
                viewModel.transferStudentToDevice(student, transferPin)
            }
        )
    }

    // Bulk Question Import Dialog (Stage 6)
    if (uiState.isImportDialogOpen) {
        val importViewModel = remember {
            QuestionImportViewModel(
                authRepository = viewModel.getAuthRepository(),
                questionRepository = viewModel.getQuestionRepository()
            )
        }
        QuestionImportDialog(
            viewModel = importViewModel,
            onDismiss = { viewModel.closeImportDialog() },
            onImportSuccess = { 
                viewModel.loadAllData()
                viewModel.selectTab(AdminTab.QUALITY_CONTROL)
            }
        )
    }

    // Firebase Diagnostic Report Dialog
    diagnosticReport?.let { report ->
        AlertDialog(
            onDismissRequest = { diagnosticReport = null },
            title = {
                Text("Firebase System Diagnostics", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Status: ${if (report.isFirestoreReachable) "Online & Connected" else "Local / Offline Mode"}",
                        fontWeight = FontWeight.Bold,
                        color = if (report.isFirestoreReachable) EmeraldGreenDark else MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = report.details,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { diagnosticReport = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out of Admin Dashboard") },
            text = { Text("Are you sure you want to end your administrator session? You will be returned to the student portal.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.logout {
                            onSignedOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("admin_confirm_signout_button")
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// 1. OVERVIEW TAB
// ==========================================
@Composable
fun AdminOverviewTab(
    stats: AdminStats,
    onNavigateToQuestions: () -> Unit,
    onNavigateToQualityControl: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToAccess: () -> Unit,
    onAddQuestion: () -> Unit,
    onImportQuestions: () -> Unit,
    onGrantAccess: () -> Unit,
    onRunDiagnostics: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hero Welcome Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EmeraldGreenDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Control Center",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Centralized question bank curating, student registry, and secure manual access entitlements.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "System Statistics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Questions",
                    value = stats.totalQuestions.toString(),
                    subtitle = "${stats.activeQuestions} Active • ${stats.inactiveQuestions} Inactive",
                    iconColor = EmeraldGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Registered Students",
                    value = stats.totalStudents.toString(),
                    subtitle = "${stats.activeEntitlements} Active Entitlements",
                    iconColor = TealSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Questions by Examination Board",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExamCountCard(
                    title = "JAMB UTME",
                    count = stats.jambQuestions,
                    color = EmeraldGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                ExamCountCard(
                    title = "WAEC SSCE",
                    count = stats.waecQuestions,
                    color = TealSecondary,
                    modifier = Modifier.weight(1f)
                )
                ExamCountCard(
                    title = "NECO SSCE",
                    count = stats.necoQuestions,
                    color = AmberTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Quick Management Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionCard(
                    title = "Add Question to Bank",
                    description = "Create and validate a new multiple-choice CBT question with syllabus topic and explanation.",
                    buttonLabel = "Add Question",
                    onClick = onAddQuestion
                )
                ActionCard(
                    title = "Bulk Import Questions",
                    description = "Import examination questions in bulk from Excel (.xlsx) or CSV (.csv) spreadsheets with automatic syllabus validation.",
                    buttonLabel = "Import Questions",
                    onClick = onImportQuestions
                )
                ActionCard(
                    title = "Firebase Connection Diagnostics",
                    description = "Verify real-time Firebase Authentication, Cloud Firestore connectivity, project ID, and admin claim status.",
                    buttonLabel = "Run Diagnostics",
                    onClick = onRunDiagnostics
                )
                ActionCard(
                    title = "Grant Manual Access",
                    description = "Entitle a student to full question access (unlocked questions 21+) for any examination and subject.",
                    buttonLabel = "Grant Access",
                    onClick = onGrantAccess
                )
                ActionCard(
                    title = "Quality Control & Past-Question Verification",
                    description = "Track 50-question subject quotas, audit past-question provenance, and review unverified questions before publication.",
                    buttonLabel = "Audit Quotas",
                    onClick = onNavigateToQualityControl
                )
                ActionCard(
                    title = "Browse Question Bank",
                    description = "Filter questions by exam board, subject, difficulty, or active/inactive status.",
                    buttonLabel = "Open Bank",
                    onClick = onNavigateToQuestions
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
fun ExamCountCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Questions",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text(buttonLabel, fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// 2. QUESTION BANK MANAGEMENT TAB
// ==========================================
@Composable
fun AdminQuestionBankTab(
    viewModel: AdminViewModel,
    uiState: AdminUiState
) {
    val filteredQuestions = remember(
        uiState.questions,
        uiState.searchQuestionQuery,
        uiState.filterExamType,
        uiState.filterSubjectType,
        uiState.filterIsActive,
        uiState.filterDifficulty,
        uiState.filterSourceType,
        uiState.filterVerificationStatus
    ) {
        uiState.questions.filter { q ->
            val matchesQuery = uiState.searchQuestionQuery.isBlank() ||
                q.questionText.contains(uiState.searchQuestionQuery, ignoreCase = true) ||
                q.topic.contains(uiState.searchQuestionQuery, ignoreCase = true) ||
                q.id.toString().contains(uiState.searchQuestionQuery) ||
                q.sourceTitle.contains(uiState.searchQuestionQuery, ignoreCase = true)

            val matchesExam = uiState.filterExamType == null || q.examType == uiState.filterExamType
            val matchesSubject = uiState.filterSubjectType == null || q.subject == uiState.filterSubjectType
            val matchesActive = uiState.filterIsActive == null || q.isActive == uiState.filterIsActive
            val matchesDiff = uiState.filterDifficulty == null || q.difficulty == uiState.filterDifficulty
            val matchesSource = uiState.filterSourceType == null || q.sourceType == uiState.filterSourceType
            val matchesVerif = uiState.filterVerificationStatus == null || q.verificationStatus == uiState.filterVerificationStatus

            matchesQuery && matchesExam && matchesSubject && matchesActive && matchesDiff && matchesSource && matchesVerif
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Action Buttons: Manual Add & Bulk Import
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.openAddQuestionDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_add_question_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Question", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { viewModel.openImportDialog() },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_import_questions_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EmeraldGreenDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Excel/CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenDark)
                }
            }
        }

        item {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuestionQuery,
                onValueChange = { viewModel.onQuestionSearchQueryChanged(it) },
                placeholder = { Text("Search question text, topic, or ID...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuestionQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQuestionSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_question_search_input")
            )
        }

        item {
            // Filter Pills: Exam Type
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = uiState.filterExamType == null,
                        label = "All Exams",
                        onClick = { viewModel.onFilterExamChanged(null) }
                    )
                }
                items(ExamType.entries) { exam ->
                    FilterChip(
                        selected = uiState.filterExamType == exam,
                        label = exam.displayName,
                        onClick = { viewModel.onFilterExamChanged(exam) }
                    )
                }
            }
        }

        item {
            // Filter Pills: Provenance & Verification (Stage 7A)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = uiState.filterSourceType == null,
                        label = "All Sources",
                        onClick = { viewModel.onFilterSourceTypeChanged(null) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterSourceType == QuestionSourceType.OFFICIAL,
                        label = "Official Past Qs",
                        onClick = { viewModel.onFilterSourceTypeChanged(QuestionSourceType.OFFICIAL) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterSourceType == QuestionSourceType.PRACTICE,
                        label = "Practice Qs",
                        onClick = { viewModel.onFilterSourceTypeChanged(QuestionSourceType.PRACTICE) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterVerificationStatus == QuestionVerificationStatus.PENDING_REVIEW,
                        label = "Pending Review",
                        onClick = { viewModel.onFilterVerificationStatusChanged(
                            if (uiState.filterVerificationStatus == QuestionVerificationStatus.PENDING_REVIEW) null else QuestionVerificationStatus.PENDING_REVIEW
                        ) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterVerificationStatus == QuestionVerificationStatus.REJECTED,
                        label = "Rejected",
                        onClick = { viewModel.onFilterVerificationStatusChanged(
                            if (uiState.filterVerificationStatus == QuestionVerificationStatus.REJECTED) null else QuestionVerificationStatus.REJECTED
                        ) }
                    )
                }
            }
        }

        item {
            // Filter Pills: Status & Difficulty
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = uiState.filterIsActive == null,
                        label = "All Status",
                        onClick = { viewModel.onFilterActiveChanged(null) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterIsActive == true,
                        label = "Active Only",
                        onClick = { viewModel.onFilterActiveChanged(true) }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterIsActive == false,
                        label = "Inactive Only",
                        onClick = { viewModel.onFilterActiveChanged(false) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredQuestions.size} Questions",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        items(filteredQuestions, key = { it.id }) { question ->
            QuestionItemCard(
                question = question,
                onPreview = { viewModel.openQuestionPreview(question) },
                onReview = { viewModel.openQuestionReview(question) },
                onEdit = { viewModel.openEditQuestionDialog(question) },
                onToggleActive = { viewModel.toggleQuestionActive(question) },
                onDelete = { viewModel.deleteQuestion(question) }
            )
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) EmeraldGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun QuestionItemCard(
    question: Question,
    onPreview: () -> Unit,
    onReview: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_question_card_${question.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = "#${question.id} • ${question.examType.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            text = question.subject.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    ProvenanceStatusBadge(question.verificationStatus)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    SourceTypeBadge(question.sourceType)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (question.isActive) EmeraldGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (question.isActive) "ACTIVE" else "INACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (question.isActive) EmeraldGreenDark else MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Topic: ${question.topic} • Year: ${question.year} • Ans: ${question.correctAnswerLetter}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                if (question.sourceTitle.isNotBlank()) {
                    Text(
                        text = "Src: ${question.sourceTitle.take(24)}...",
                        style = MaterialTheme.typography.labelSmall.copy(color = EmeraldGreenDark, fontSize = 10.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPreview) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview")
                }
                TextButton(onClick = onReview) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreenDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review", color = EmeraldGreenDark)
                }
                TextButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = onToggleActive,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (question.isActive) "Deactivate" else "Activate", fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// 3. STUDENT MANAGEMENT TAB
// ==========================================
@Composable
fun AdminStudentsTab(
    viewModel: AdminViewModel,
    uiState: AdminUiState
) {
    val filteredStudents = remember(uiState.students, uiState.searchStudentQuery) {
        if (uiState.searchStudentQuery.isBlank()) {
            uiState.students
        } else {
            val q = uiState.searchStudentQuery.trim().lowercase()
            val normalizedQPhone = StudentPinManager.normalizePhoneNumber(q).lowercase()
            val normalizedQId = StudentPinManager.normalizeStudentId(q).lowercase()
            uiState.students.filter {
                it.displayName.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    (it.phone != null && (it.phone.contains(q) || StudentPinManager.normalizePhoneNumber(it.phone).lowercase().contains(normalizedQPhone))) ||
                    (it.studentId != null && (it.studentId.lowercase().contains(q) || StudentPinManager.normalizeStudentId(it.studentId).lowercase().contains(normalizedQId))) ||
                    it.uid.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.searchStudentQuery,
                onValueChange = { viewModel.onStudentSearchQueryChanged(it) },
                placeholder = { Text("Search by name, phone, student ID, email...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchStudentQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onStudentSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_student_search_input")
            )
        }

        item {
            Text(
                text = "Registered Students (${filteredStudents.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(filteredStudents, key = { it.uid }) { student ->
            val studentEntitlements = uiState.entitlements.filter { it.studentUid == student.uid && it.isCurrentlyActive }
            StudentItemCard(
                student = student,
                activeEntitlementsCount = studentEntitlements.size,
                onViewDetail = { viewModel.selectStudent(student) },
                onGrantAccess = { viewModel.openGrantAccessDialog(student) }
            )
        }
    }
}

@Composable
fun StudentItemCard(
    student: StudentUser,
    activeEntitlementsCount: Int,
    onViewDetail: () -> Unit,
    onGrantAccess: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_student_card_${student.uid}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = EmeraldGreenPrimary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = student.initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = student.email,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = "${student.targetExam.displayName} (${student.targetYear})",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (activeEntitlementsCount > 0) {
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldGreenPrimary.copy(alpha = 0.15f)) {
                            Text(
                                text = "$activeEntitlementsCount Entitlements",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenDark
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onViewDetail,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Manage", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onGrantAccess,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Grant", fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// 4. ACCESS MANAGEMENT TAB
// ==========================================
@Composable
fun AdminAccessManagementTab(
    viewModel: AdminViewModel,
    uiState: AdminUiState
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = EmeraldGreenPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Manual Access Entitlements", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Audited records of full CBT question access granted to students.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Entitlements (${uiState.entitlements.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { viewModel.openGrantAccessDialog(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Grant Manual Access", fontSize = 12.sp)
                }
            }
        }

        if (uiState.entitlements.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = EmeraldGreenPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No Access Entitlements Recorded Yet", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Grant manual full access to any student to unlock test questions beyond the first 20 free questions.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        items(uiState.entitlements, key = { it.id }) { ent ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ent.studentName.ifBlank { "Student" }} • ${ent.examType.displayName} (${ent.getSubjectsSummary()})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        StatusPill(status = ent.effectiveStatus)
                    }

                    Text(
                        text = "Email: ${ent.studentEmail.ifBlank { ent.studentUid }}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "Source: ${ent.accessType.displayName} • Granted by: ${ent.grantedBy} (${dateFormat.format(Date(ent.grantedAt))})",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "Expiry: ${ent.expiresAt?.let { dateFormat.format(Date(it)) } ?: "Permanent / Lifetime"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    if (ent.reason.isNotBlank()) {
                        Text(
                            text = "Reason / Note: ${ent.reason}",
                            style = MaterialTheme.typography.labelSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }

                    if (ent.effectiveStatus == EntitlementStatus.ACTIVE) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.revokeAccess(ent) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(32.dp)
                        ) {
                            Text(text = "Revoke Access", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPaymentSettingsTab(
    viewModel: AdminViewModel,
    uiState: AdminUiState
) {
    val currentSettings = uiState.paymentSettings
    var amountInput by remember(currentSettings.amountNaira) {
        mutableStateOf(currentSettings.amountNaira.toString())
    }
    var bankNameInput by remember(currentSettings.bankName) {
        mutableStateOf(currentSettings.bankName)
    }
    var accountNameInput by remember(currentSettings.accountName) {
        mutableStateOf(currentSettings.accountName)
    }
    var accountNumberInput by remember(currentSettings.accountNumber) {
        mutableStateOf(currentSettings.accountNumber)
    }
    var whatsappNumberInput by remember(currentSettings.whatsappNumber) {
        mutableStateOf(currentSettings.whatsappNumber)
    }
    var instructionsInput by remember(currentSettings.paymentInstructions) {
        mutableStateOf(currentSettings.paymentInstructions)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_payment_settings_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldGreenDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = AmberTertiary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Configurable Payment Settings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Manage bank details, fees in Nigerian Naira (₦), and WhatsApp support contact.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Error message banner
        uiState.paymentSettingsError?.let { errorMsg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().testTag("payment_settings_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Form Fields Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Payment Information & Bank Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Payment Amount
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = {
                            amountInput = it.filter { ch -> ch.isDigit() }
                            viewModel.clearPaymentSettingsError()
                        },
                        label = { Text("Payment Amount (₦)") },
                        placeholder = { Text("2500") },
                        prefix = { Text("₦ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_payment_amount_input")
                    )

                    // Bank Name
                    OutlinedTextField(
                        value = bankNameInput,
                        onValueChange = {
                            bankNameInput = it
                            viewModel.clearPaymentSettingsError()
                        },
                        label = { Text("Bank Name") },
                        placeholder = { Text("e.g. Zenith Bank") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_bank_name_input")
                    )

                    // Account Name
                    OutlinedTextField(
                        value = accountNameInput,
                        onValueChange = {
                            accountNameInput = it
                            viewModel.clearPaymentSettingsError()
                        },
                        label = { Text("Account Name") },
                        placeholder = { Text("e.g. ExamPrep CBT Services") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_account_name_input")
                    )

                    // Account Number
                    OutlinedTextField(
                        value = accountNumberInput,
                        onValueChange = {
                            accountNumberInput = it
                            viewModel.clearPaymentSettingsError()
                        },
                        label = { Text("Account Number") },
                        placeholder = { Text("e.g. 1012345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_account_number_input")
                    )

                    // WhatsApp Business Number
                    OutlinedTextField(
                        value = whatsappNumberInput,
                        onValueChange = {
                            whatsappNumberInput = it
                            viewModel.clearPaymentSettingsError()
                        },
                        label = { Text("Administrator WhatsApp Number") },
                        placeholder = { Text("+2348012345678") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                        },
                        supportingText = { Text("Include international country code prefix (e.g. +234...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_whatsapp_number_input")
                    )

                    // Payment Instructions
                    OutlinedTextField(
                        value = instructionsInput,
                        onValueChange = {
                            instructionsInput = it
                            viewModel.clearPaymentSettingsError()
                        },
                        label = { Text("Instructions for Students") },
                        placeholder = { Text("Describe transfer instructions and receipt upload steps") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("admin_payment_instructions_input")
                    )

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val amount = amountInput.toLongOrNull() ?: 0L
                                val settingsToSave = PaymentSettings(
                                    amountNaira = amount,
                                    bankName = bankNameInput,
                                    accountName = accountNameInput,
                                    accountNumber = accountNumberInput,
                                    whatsappNumber = whatsappNumberInput,
                                    paymentInstructions = instructionsInput
                                )
                                viewModel.savePaymentSettings(settingsToSave)
                            },
                            enabled = !uiState.isSavingPaymentSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("admin_save_payment_settings_button")
                        ) {
                            if (uiState.isSavingPaymentSettings) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...")
                            } else {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Settings", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val defaultSettings = PaymentSettings()
                                amountInput = defaultSettings.amountNaira.toString()
                                bankNameInput = defaultSettings.bankName
                                accountNameInput = defaultSettings.accountName
                                accountNumberInput = defaultSettings.accountNumber
                                whatsappNumberInput = defaultSettings.whatsappNumber
                                instructionsInput = defaultSettings.paymentInstructions
                                viewModel.clearPaymentSettingsError()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(48.dp).testTag("admin_reset_payment_settings_button")
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }
                }
            }
        }

        // Live Student Preview Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = EmeraldGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Student Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Students will see fee: ₦${amountInput.ifBlank { "0" }} payable to $bankNameInput ($accountNumberInput - $accountNameInput).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "WhatsApp destination: $whatsappNumberInput",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldGreenDark
                    )
                }
            }
        }
    }
}

