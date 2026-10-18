package com.success.examprep.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.model.ExamType
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import com.success.examprep.ui.theme.ErrorRed
import com.success.examprep.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStudent: () -> Unit = onNavigateBack,
    onNavigateToAdmin: () -> Unit = {},
    onAuthSuccess: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForgotAdminPasswordDialog by remember { mutableStateOf(false) }
    var forgotAdminEmail by remember { mutableStateOf("") }
    var showDeviceRecoveryInfoDialog by remember { mutableStateOf(false) }

    BackHandler { onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.mode) {
                            AuthMode.STUDENT_SIGN_UP -> "Create Student Profile"
                            AuthMode.STUDENT_SIGN_IN -> "Student Sign In"
                            AuthMode.ADMIN_SIGN_IN -> "Administrator Login"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("auth_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("auth_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector
            item {
                TabRow(
                    selectedTabIndex = when (uiState.mode) {
                        AuthMode.STUDENT_SIGN_UP -> 0
                        AuthMode.STUDENT_SIGN_IN -> 1
                        AuthMode.ADMIN_SIGN_IN -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = EmeraldGreenPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = uiState.mode == AuthMode.STUDENT_SIGN_UP,
                        onClick = { viewModel.setMode(AuthMode.STUDENT_SIGN_UP) },
                        text = { Text("New Student", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("auth_tab_student_signup")
                    )
                    Tab(
                        selected = uiState.mode == AuthMode.STUDENT_SIGN_IN,
                        onClick = { viewModel.setMode(AuthMode.STUDENT_SIGN_IN) },
                        text = { Text("Student Login", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("auth_tab_student_login")
                    )
                    Tab(
                        selected = uiState.mode == AuthMode.ADMIN_SIGN_IN,
                        onClick = { viewModel.setMode(AuthMode.ADMIN_SIGN_IN) },
                        text = { Text("Admin Portal", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("auth_tab_admin_login")
                    )
                }
            }

            // Header Banner
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (uiState.mode) {
                        AuthMode.ADMIN_SIGN_IN -> EmeraldGreenDark
                        else -> EmeraldGreenPrimary.copy(alpha = 0.08f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (uiState.mode == AuthMode.ADMIN_SIGN_IN) Color.White.copy(alpha = 0.2f) else EmeraldGreenDark,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (uiState.mode == AuthMode.ADMIN_SIGN_IN) Icons.Default.AdminPanelSettings else Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (uiState.mode) {
                                    AuthMode.STUDENT_SIGN_UP -> "Student Identity (No Email Required)"
                                    AuthMode.STUDENT_SIGN_IN -> "Unlock Student Profile on Device"
                                    AuthMode.ADMIN_SIGN_IN -> "Administrator Secure Access"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (uiState.mode == AuthMode.ADMIN_SIGN_IN) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (uiState.mode) {
                                    AuthMode.STUDENT_SIGN_UP -> "All you need is your name, phone number, and a 4-digit PIN."
                                    AuthMode.STUDENT_SIGN_IN -> "Enter your registered phone or Student ID with your 4-digit PIN."
                                    AuthMode.ADMIN_SIGN_IN -> "Verified Firebase email and custom claim required."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.mode == AuthMode.ADMIN_SIGN_IN) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Messages
            uiState.errorMessage?.let { error ->
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().testTag("auth_error_card")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = error, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            uiState.successMessage?.let { success ->
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().testTag("auth_success_card")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = success, color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // FORM FIELDS ACCORDING TO MODE
            when (uiState.mode) {
                AuthMode.STUDENT_SIGN_UP -> {
                    item {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = { Text("Full Name (e.g. Adebayo Ibrahim)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldGreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_student_name_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.onPhoneChange(it) },
                            label = { Text("Phone Number (e.g. 08012345678)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = EmeraldGreenPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_student_phone_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.studentId,
                            onValueChange = { viewModel.onStudentIdChange(it) },
                            label = { Text("Student ID / Reg No (Optional)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.School, contentDescription = null, tint = EmeraldGreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_student_id_input")
                        )
                    }

                    item {
                        Text(text = "Target Examination", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExamType.entries.forEach { exam ->
                                val isSelected = uiState.targetExam == exam
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onTargetExamChange(exam) },
                                    label = { Text(exam.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreenPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("auth_chip_${exam.name.lowercase()}")
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.pin,
                            onValueChange = { viewModel.onPinChange(it) },
                            label = { Text("4-Digit PIN") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreenPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_student_pin_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.confirmPin,
                            onValueChange = { viewModel.onConfirmPinChange(it) },
                            label = { Text("Confirm 4-Digit PIN") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreenPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_student_confirm_pin_input")
                        )
                    }
                }

                AuthMode.STUDENT_SIGN_IN -> {
                    item {
                        OutlinedTextField(
                            value = if (uiState.phone.isNotBlank()) uiState.phone else uiState.studentId,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    viewModel.onPhoneChange(it)
                                } else {
                                    viewModel.onStudentIdChange(it)
                                }
                            },
                            label = { Text("Registered Phone Number or Student ID") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = EmeraldGreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_student_login_id_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.pin,
                            onValueChange = { viewModel.onPinChange(it) },
                            label = { Text("4-Digit Device PIN") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreenPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_student_login_pin_input")
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showDeviceRecoveryInfoDialog = true },
                                modifier = Modifier.testTag("auth_forgot_pin_button")
                            ) {
                                Text("Forgot PIN / Changed Device?", color = EmeraldGreenDark, fontSize = 12.sp)
                            }
                        }
                    }
                }

                AuthMode.ADMIN_SIGN_IN -> {
                    item {
                        OutlinedTextField(
                            value = uiState.adminEmail,
                            onValueChange = { viewModel.onAdminEmailChange(it) },
                            label = { Text("Administrator Email") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = EmeraldGreenPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_admin_email_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = uiState.adminPassword,
                            onValueChange = { viewModel.onAdminPasswordChange(it) },
                            label = { Text("Administrator Password") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldGreenPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_admin_password_input")
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    forgotAdminEmail = uiState.adminEmail
                                    showForgotAdminPasswordDialog = true
                                },
                                modifier = Modifier.testTag("auth_forgot_admin_password_button")
                            ) {
                                Text("Forgot Password?", color = EmeraldGreenDark, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Submit Button
            item {
                val isBusy = uiState.isLoading || uiState.isAuthorizing
                Button(
                    onClick = {
                        viewModel.submit(
                            onNavigateToStudent = {
                                onAuthSuccess?.invoke() ?: onNavigateToStudent()
                            },
                            onNavigateToAdmin = onNavigateToAdmin
                        )
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_button")
                ) {
                    if (isBusy) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp).testTag("auth_loading_indicator")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isAuthorizing) "Verifying Administrator Role..." else "Signing in...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = when (uiState.mode) {
                                AuthMode.STUDENT_SIGN_UP -> "Create Student Profile"
                                AuthMode.STUDENT_SIGN_IN -> "Sign In to Profile"
                                AuthMode.ADMIN_SIGN_IN -> "Sign In as Administrator"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Continue as Guest Option
            item {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().testTag("auth_continue_guest_button")
                ) {
                    Text("Continue as Guest (Attempt Free CBT Questions)")
                }
            }
        }
    }

    // Admin Forgot Password Dialog
    if (showForgotAdminPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotAdminPasswordDialog = false },
            title = { Text("Reset Administrator Password") },
            text = {
                Column {
                    Text("Enter your administrator email to receive a Firebase password reset link.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgotAdminEmail,
                        onValueChange = { forgotAdminEmail = it },
                        label = { Text("Administrator Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendAdminPasswordReset(forgotAdminEmail) {
                            showForgotAdminPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotAdminPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Student Recovery / Device Transfer Info Dialog
    if (showDeviceRecoveryInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceRecoveryInfoDialog = false },
            title = { Text("Device Transfer & PIN Recovery") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Because student accounts are protected on-device without requiring email addresses, account recovery or transferring your profile to a new device is handled securely with administrative assistance.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Visit an authorized ExamPrep CBT Administrator or your school CBT coordinator.\n• Provide your registered Phone Number and Full Name.\n• The administrator will verify your profile in the Admin Dashboard and assign a new device transfer PIN.\n• You can then immediately log in on your new device and resume your test history and entitlements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeviceRecoveryInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Text("Understood")
                }
            }
        )
    }
}
