package com.success.examprep.ui.screens.subject

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.Subject
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.repository.EntitlementRepository
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSelectionScreen(
    examType: ExamType,
    onNavigateBack: () -> Unit,
    onSubjectSelected: (Subject) -> Unit,
    entitlementRepository: EntitlementRepository? = null,
    currentStudentUid: String? = null
) {
    val subjects = examType.getAvailableSubjects()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var studentJambSubjects by remember {
        mutableStateOf(
            listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY
            )
        )
    }
    var hasActiveFullAccess by remember { mutableStateOf(false) }
    var showCombinationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentStudentUid, examType) {
        if (currentStudentUid != null && entitlementRepository != null) {
            if (examType == ExamType.JAMB) {
                val existing = entitlementRepository.getStudentJambCombination(currentStudentUid)
                if (existing != null && existing.size == 4) {
                    studentJambSubjects = existing
                    hasActiveFullAccess = true
                } else {
                    hasActiveFullAccess = entitlementRepository.hasActiveEntitlementSync(
                        currentStudentUid,
                        ExamType.JAMB,
                        SubjectType.ENGLISH
                    )
                }
            } else {
                hasActiveFullAccess = entitlementRepository.hasActiveEntitlementSync(
                    currentStudentUid,
                    examType,
                    SubjectType.MATHEMATICS
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${examType.displayName} Subjects",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (examType == ExamType.JAMB) "4-Subject UTME combination" else "Select a subject to start test",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("subject_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.testTag("subject_selection_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Free CBT / Full Access Status Card
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreenPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreenDark,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = examType.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (hasActiveFullAccess) "Full Access Active" else "Free CBT Practice Available",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (hasActiveFullAccess) {
                                    if (examType == ExamType.JAMB) "All questions unlocked for your 4 chosen JAMB subjects." else "All subjects fully unlocked for ${examType.displayName}."
                                } else {
                                    "First 20 questions free for each subject. Unlock full access via administrator."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Dedicated JAMB Subject Combination Card (Students can view & customize their 4 subjects)
            if (examType == ExamType.JAMB) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jamb_combination_banner")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your JAMB Combination (4 Subjects)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                OutlinedButton(
                                    onClick = { showCombinationDialog = true },
                                    modifier = Modifier.height(32.dp).testTag("change_jamb_combination_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Change", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = studentJambSubjects.joinToString(separator = "  •  ") { it.displayName },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = EmeraldGreenDark
                            )
                            Text(
                                text = "English is compulsory + 3 chosen subjects. Changing combination preserves your past test scores.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Subject Cards List
            items(subjects, key = { it.id }) { subject ->
                val subjectType = SubjectType.fromId(subject.id)
                val isJambSubject = examType == ExamType.JAMB && studentJambSubjects.contains(subjectType)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_card_${subject.id}")
                        .clickable { onSubjectSelected(subject) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isJambSubject) EmeraldGreenPrimary.copy(alpha = 0.2f) else EmeraldGreenPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = subject.name.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenDark,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                if (isJambSubject) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = EmeraldGreenPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (subjectType == SubjectType.ENGLISH) "Compulsory" else "In Combination",
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
                            Text(
                                text = if (hasActiveFullAccess && (examType != ExamType.JAMB || isJambSubject)) {
                                    "Full CBT syllabus • All questions unlocked"
                                } else {
                                    "20 free questions • Practice mode"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Student JAMB Combination Dialog
    if (showCombinationDialog) {
        var tempSelection by remember { mutableStateOf(studentJambSubjects) }
        val isExactFour = tempSelection.size == 4 && tempSelection.contains(SubjectType.ENGLISH)

        AlertDialog(
            onDismissRequest = { showCombinationDialog = false },
            title = {
                Text(
                    text = "Choose Your 4 JAMB Subjects",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Use of English is compulsory and locked. Select exactly three additional subjects for your UTME combination.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected Subjects",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isExactFour) EmeraldGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "${tempSelection.size} / 4",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExactFour) EmeraldGreenDark else MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Compulsory English item
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreenDark.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreenDark.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Locked",
                                tint = EmeraldGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Use of English (Compulsory - Locked)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Other available subjects
                    val candidateSubjects = SubjectType.entries.filter { it != SubjectType.ENGLISH }
                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(candidateSubjects) { sub ->
                            val isChecked = tempSelection.contains(sub)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isChecked) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        val current = tempSelection.toMutableList()
                                        if (isChecked) {
                                            current.remove(sub)
                                        } else {
                                            if (current.size < 4) {
                                                current.add(sub)
                                            }
                                        }
                                        tempSelection = current
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val current = tempSelection.toMutableList()
                                            if (checked) {
                                                if (current.size < 4 && !current.contains(sub)) {
                                                    current.add(sub)
                                                }
                                            } else {
                                                current.remove(sub)
                                            }
                                            tempSelection = current
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = sub.displayName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isExactFour) {
                            studentJambSubjects = tempSelection
                            showCombinationDialog = false
                            if (currentStudentUid != null && entitlementRepository != null) {
                                coroutineScope.launch {
                                    val result = entitlementRepository.updateStudentJambCombination(
                                        currentStudentUid,
                                        tempSelection
                                    )
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("JAMB combination updated successfully.")
                                    } else {
                                        snackbarHostState.showSnackbar("Combination saved for practice session.")
                                    }
                                }
                            }
                        }
                    },
                    enabled = isExactFour,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    modifier = Modifier.testTag("save_jamb_combination_button")
                ) {
                    Text("Save Combination")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCombinationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
