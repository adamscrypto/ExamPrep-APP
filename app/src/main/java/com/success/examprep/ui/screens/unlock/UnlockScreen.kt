package com.success.examprep.ui.screens.unlock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.auth.AuthRepository
import com.success.examprep.data.auth.FirebaseAuthRepository
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.PaymentSettings
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.repository.DefaultEntitlementRepository
import com.success.examprep.data.repository.DefaultPaymentSettingsRepository
import com.success.examprep.data.repository.EntitlementRepository
import com.success.examprep.data.repository.PaymentSettingsRepository
import com.success.examprep.ui.screens.cbt.CbtViewModel
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockScreen(
    examType: ExamType,
    subjectType: SubjectType,
    viewModel: CbtViewModel? = null,
    paymentSettingsRepository: PaymentSettingsRepository? = null,
    authRepository: AuthRepository? = null,
    entitlementRepository: EntitlementRepository? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val resolvedPaymentRepo = remember {
        paymentSettingsRepository ?: DefaultPaymentSettingsRepository.getInstance(context)
    }
    val resolvedAuthRepo = remember {
        authRepository ?: FirebaseAuthRepository.getInstance(context)
    }
    val resolvedEntitlementRepo = remember {
        entitlementRepository ?: DefaultEntitlementRepository.getInstance(context)
    }

    val paymentSettings by resolvedPaymentRepo.getPaymentSettingsFlow().collectAsState(
        initial = resolvedPaymentRepo.getCachedPaymentSettings()
    )

    var jambSubjects by remember { mutableStateOf<List<SubjectType>?>(null) }
    var isCheckingAccess by remember { mutableStateOf(false) }
    var accessCheckResult by remember { mutableStateOf<Boolean?>(null) }
    var isRefreshingConfig by remember { mutableStateOf(false) }

    val currentUser = resolvedAuthRepo.getCurrentUser()
    val currentUserProfile = resolvedAuthRepo.getCurrentUserProfile()

    val studentName = currentUserProfile?.fullName?.ifBlank { currentUser?.displayName }
        ?: currentUser?.displayName ?: "Student"
    val studentPhone = currentUserProfile?.phone?.ifBlank { currentUser?.phone }
        ?: currentUser?.phone ?: "Not registered"
    val studentId = currentUserProfile?.studentId ?: currentUser?.studentId ?: currentUser?.uid ?: "GUEST-USER"
    val studentUid = currentUser?.uid ?: ""

    // Load initial live config and check JAMB subject combination
    LaunchedEffect(studentUid, examType) {
        isRefreshingConfig = true
        resolvedPaymentRepo.getPaymentSettings()
        isRefreshingConfig = false

        if (studentUid.isNotBlank() && examType == ExamType.JAMB) {
            jambSubjects = resolvedEntitlementRepo.getStudentJambCombination(studentUid)
        }
    }

    // Function to compose the prefilled WhatsApp message
    fun buildWhatsAppMessage(settings: PaymentSettings): String {
        val examDisplayName = when (examType) {
            ExamType.JAMB -> "JAMB UTME"
            ExamType.WAEC -> "WAEC (SSCE)"
            ExamType.NECO -> "NECO (SSCE)"
        }

        val subjectInfo = if (examType == ExamType.JAMB && jambSubjects != null && jambSubjects!!.isNotEmpty()) {
            "Selected JAMB Subjects: " + jambSubjects!!.joinToString(", ") { it.displayName }
        } else {
            "Subject Target: ${subjectType.displayName}"
        }

        return buildString {
            append("Hello Admin, I have transferred ${settings.formattedAmount} for CBT Full Access.\n\n")
            append("*Student Details:*\n")
            append("• Name: $studentName\n")
            append("• Phone: $studentPhone\n")
            append("• Student ID / UID: $studentId\n")
            append("• Examination: $examDisplayName\n")
            append("• $subjectInfo\n")
            append("• Amount Transferred: ${settings.formattedAmount}\n\n")
            append("I have attached my bank transfer receipt. Kindly verify and grant full access to my account. Thank you!")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Request Full CBT Access", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("unlock_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshingConfig = true
                                resolvedPaymentRepo.refreshPaymentSettings()
                                isRefreshingConfig = false
                                snackbarHostState.showSnackbar("Payment settings refreshed.")
                            }
                        },
                        modifier = Modifier.testTag("unlock_refresh_config_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("unlock_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Target Exam & Subject Banner
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
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = AmberTertiary)
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "${examType.displayName} ${subjectType.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Full Question Bank & Unlimited Practice",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Price & Fee Details Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Access Fee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldGreenPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = paymentSettings.formattedAmount,
                                    color = EmeraldGreenDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).testTag("payment_amount_text")
                                )
                            }
                        }

                        if (!paymentSettings.isLiveConfig) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AmberTertiary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Using cached settings. Reconnect to sync latest bank details.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "One-time payment for full comprehensive access to practice questions, verified past questions, and detailed explanations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bank Account Details Card with One-Tap Copy
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bank Transfer Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bank Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Bank Name",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = paymentSettings.bankName,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.testTag("bank_name_text")
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(paymentSettings.bankName))
                                    Toast.makeText(context, "Bank name copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Bank Name", modifier = Modifier.size(18.dp))
                            }
                        }

                        // Account Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Account Name",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = paymentSettings.accountName,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.testTag("account_name_text")
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(paymentSettings.accountName))
                                    Toast.makeText(context, "Account name copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Account Name", modifier = Modifier.size(18.dp))
                            }
                        }

                        // Account Number with Highlight
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Account Number",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = paymentSettings.accountNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = EmeraldGreenDark,
                                        modifier = Modifier.testTag("account_number_text")
                                    )
                                }
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(paymentSettings.accountNumber))
                                        Toast.makeText(context, "Account number copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("copy_account_number_button")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy")
                                }
                            }
                        }

                        if (paymentSettings.paymentInstructions.isNotBlank()) {
                            Text(
                                text = paymentSettings.paymentInstructions,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Step-by-Step Instructions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "How to Request Access",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        InstructionStep(number = "1", text = "Transfer ${paymentSettings.formattedAmount} to the bank account shown above.")
                        InstructionStep(number = "2", text = "Tap 'Request Access via WhatsApp' to open WhatsApp with your student details.")
                        InstructionStep(number = "3", text = "Attach your transfer receipt or screenshot and send the message.")
                        InstructionStep(number = "4", text = "Wait for the administrator to verify payment and grant access, then tap 'Check Access Status' below.")
                    }
                }
            }

            // Student Identity Confirmation
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreenDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Your Identifying Details:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "Name: $studentName", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Phone: $studentPhone", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Student ID: $studentId", style = MaterialTheme.typography.bodySmall)
                        if (examType == ExamType.JAMB && jambSubjects != null && jambSubjects!!.isNotEmpty()) {
                            Text(
                                text = "JAMB Subjects: " + jambSubjects!!.joinToString(", ") { it.displayName },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // WhatsApp Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val message = buildWhatsAppMessage(paymentSettings)
                            val cleanNumber = paymentSettings.cleanWhatsAppNumber
                            val uri = Uri.parse("https://wa.me/$cleanNumber?text=" + Uri.encode(message))
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback: copy to clipboard
                                clipboardManager.setText(AnnotatedString(message))
                                Toast.makeText(
                                    context,
                                    "WhatsApp could not be opened. Request message copied to clipboard.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("whatsapp_request_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Request Access via WhatsApp",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val message = buildWhatsAppMessage(paymentSettings)
                            clipboardManager.setText(AnnotatedString(message))
                            Toast.makeText(context, "Request message copied to clipboard.", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("copy_message_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Request Message Text")
                    }
                }
            }

            // Check Entitlements / Access Status Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Already Made Payment?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "After sending your receipt, check here to verify if the administrator has activated your access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Access status result banner
                        accessCheckResult?.let { hasAccess ->
                            if (hasAccess) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldGreenPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldGreenDark
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Access Active!",
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGreenDark
                                            )
                                            Text(
                                                text = "Your entitlement has been confirmed. You now have full CBT access.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = EmeraldGreenDark,
                                                modifier = Modifier.testTag("access_status_text")
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Pending Confirmation",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "Access is not yet active. Please ensure you sent your payment transfer receipt to the administrator via WhatsApp.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.testTag("access_status_text")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    isCheckingAccess = true
                                    val active = if (studentUid.isNotBlank()) {
                                        resolvedEntitlementRepo.hasActiveAccess(studentUid, examType, subjectType)
                                    } else {
                                        false
                                    }
                                    accessCheckResult = active
                                    isCheckingAccess = false
                                }
                            },
                            enabled = !isCheckingAccess,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("refresh_access_button")
                        ) {
                            if (isCheckingAccess) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking Entitlements...")
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check Access Status")
                            }
                        }

                        if (accessCheckResult == true) {
                            Button(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("continue_to_cbt_button")
                            ) {
                                Text("Continue to CBT Practice", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Security & Policy Guarantee
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Note: Opening WhatsApp or sending messages does not automatically grant access. The administrator must verify payment before activating your account.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = CircleShape,
            color = EmeraldGreenPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EmeraldGreenDark
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
