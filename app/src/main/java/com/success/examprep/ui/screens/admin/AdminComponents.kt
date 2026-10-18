package com.success.examprep.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.auth.StudentPinManager
import com.success.examprep.data.auth.PinValidationResult
import com.success.examprep.data.model.AccessEntitlement
import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.EntitlementStatus
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.QuestionSourceType
import com.success.examprep.data.model.QuestionVerificationStatus
import com.success.examprep.data.model.AnswerVerificationStatus
import com.success.examprep.data.model.ContentPermissionStatus
import com.success.examprep.data.model.StudentUser
import com.success.examprep.data.model.SubjectType
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusPill(status: EntitlementStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        EntitlementStatus.ACTIVE -> Pair(EmeraldGreenPrimary.copy(alpha = 0.15f), EmeraldGreenDark)
        EntitlementStatus.REVOKED -> Pair(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), MaterialTheme.colorScheme.error)
        EntitlementStatus.EXPIRED -> Pair(AmberTertiary.copy(alpha = 0.2f), Color(0xFFB45309))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = textColor),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun QuestionPreviewDialog(
    question: Question,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question #${question.id} Preview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
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
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Metadata chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = question.examType.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
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
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = "${question.year} • ${question.difficulty.name}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    ProvenanceStatusBadge(question.verificationStatus)
                }

                // Provenance Details Box
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Source: ${question.sourceType.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            SourceTypeBadge(question.sourceType)
                        }
                        if (question.sourceTitle.isNotBlank()) {
                            Text(
                                text = "Title: ${question.sourceTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                        if (question.sourceReference.isNotBlank()) {
                            Text(
                                text = "Ref: ${question.sourceReference}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                        if (question.originalQuestionNumber.isNotBlank()) {
                            Text(
                                text = "Original Q#: ${question.originalQuestionNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                        if (question.reviewNotes.isNotBlank()) {
                            Text(
                                text = "Review Notes: ${question.reviewNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Topic: ${question.topic}",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Options:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))
                val options = listOf(
                    Triple(OptionLetter.A, "A", question.optionA),
                    Triple(OptionLetter.B, "B", question.optionB),
                    Triple(OptionLetter.C, "C", question.optionC),
                    Triple(OptionLetter.D, "D", question.optionD)
                )

                options.forEach { (letter, label, text) ->
                    val isCorrect = letter == question.correctAnswer
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCorrect) EmeraldGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isCorrect) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreenPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isCorrect) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCorrect) EmeraldGreenDark else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct Answer",
                                    tint = EmeraldGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (question.explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Explanation:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Created by: ${question.createdBy} • Updated: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(question.updatedAt))}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormDialog(
    initialQuestion: Question? = null,
    onDismiss: () -> Unit,
    onSave: (
        examType: ExamType,
        subject: SubjectType,
        year: Int,
        topic: String,
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctAnswer: OptionLetter,
        explanation: String,
        difficulty: Difficulty,
        isFree: Boolean,
        isActive: Boolean,
        sourceType: QuestionSourceType,
        sourceTitle: String,
        sourceOrganization: String,
        sourceReference: String,
        sourceUrl: String,
        sourceYear: Int?,
        sourcePage: String,
        originalQuestionNumber: String,
        verificationStatus: QuestionVerificationStatus,
        answerVerificationStatus: AnswerVerificationStatus,
        reviewNotes: String,
        contentPermissionStatus: ContentPermissionStatus
    ) -> Unit
) {
    var examType by remember { mutableStateOf(initialQuestion?.examType ?: ExamType.JAMB) }
    var subject by remember { mutableStateOf(initialQuestion?.subject ?: SubjectType.MATHEMATICS) }
    var yearText by remember { mutableStateOf((initialQuestion?.year ?: 2024).toString()) }
    var topic by remember { mutableStateOf(initialQuestion?.topic ?: "") }
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var optionA by remember { mutableStateOf(initialQuestion?.optionA ?: "") }
    var optionB by remember { mutableStateOf(initialQuestion?.optionB ?: "") }
    var optionC by remember { mutableStateOf(initialQuestion?.optionC ?: "") }
    var optionD by remember { mutableStateOf(initialQuestion?.optionD ?: "") }
    var correctAnswer by remember { mutableStateOf(initialQuestion?.correctAnswer ?: OptionLetter.A) }
    var explanation by remember { mutableStateOf(initialQuestion?.explanation ?: "") }
    var difficulty by remember { mutableStateOf(initialQuestion?.difficulty ?: Difficulty.MEDIUM) }
    var isFree by remember { mutableStateOf(initialQuestion?.isFree ?: true) }
    var isActive by remember { mutableStateOf(initialQuestion?.isActive ?: true) }

    // Stage 7A Provenance State
    var sourceType by remember { mutableStateOf(initialQuestion?.sourceType ?: QuestionSourceType.PRACTICE) }
    var sourceTitle by remember { mutableStateOf(initialQuestion?.sourceTitle ?: "") }
    var sourceOrganization by remember { mutableStateOf(initialQuestion?.sourceOrganization ?: "") }
    var sourceReference by remember { mutableStateOf(initialQuestion?.sourceReference ?: "") }
    var sourceUrl by remember { mutableStateOf(initialQuestion?.sourceUrl ?: "") }
    var sourceYearText by remember { mutableStateOf(initialQuestion?.sourceYear?.toString() ?: "") }
    var sourcePage by remember { mutableStateOf(initialQuestion?.sourcePage ?: "") }
    var originalQuestionNumber by remember { mutableStateOf(initialQuestion?.originalQuestionNumber ?: "") }
    var verificationStatus by remember { mutableStateOf(initialQuestion?.verificationStatus ?: QuestionVerificationStatus.VERIFIED) }
    var answerVerificationStatus by remember { mutableStateOf(initialQuestion?.answerVerificationStatus ?: AnswerVerificationStatus.REVIEWED) }
    var reviewNotes by remember { mutableStateOf(initialQuestion?.reviewNotes ?: "") }
    var contentPermissionStatus by remember { mutableStateOf(initialQuestion?.contentPermissionStatus ?: ContentPermissionStatus.PUBLIC_DOMAIN_PAST_EXAM) }

    var formError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialQuestion == null) "Add New Question" else "Edit Question #${initialQuestion.id}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (formError != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formError!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Exam Type selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExamType.entries.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (examType == item) EmeraldGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { examType = item }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (examType == item) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Subject Type selector
                var subjectExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subject.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        SubjectType.entries.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.displayName) },
                                onClick = {
                                    subject = sub
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Year & Topic
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Year") },
                        modifier = Modifier.weight(0.35f)
                    )
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic (e.g. Algebra)") },
                        modifier = Modifier.weight(0.65f)
                    )
                }

                // Question Text
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Options A-D
                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("Option A *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("Option B *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("Option C *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("Option D *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Correct Answer
                Text(
                    text = "Select Correct Answer: *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OptionLetter.entries.forEach { letter ->
                        val isSelected = letter == correctAnswer
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) EmeraldGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { correctAnswer = letter }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = letter.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                // Explanation
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation (Optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Difficulty
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Difficulty: ", style = MaterialTheme.typography.labelMedium)
                    Difficulty.entries.forEach { diff ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (difficulty == diff) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { difficulty = diff }
                        ) {
                            Text(
                                text = diff.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (difficulty == diff) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Stage 7A: Provenance & Quality-Control Inputs
                Text(
                    text = "Source Provenance & Quality-Control:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuestionSourceType.entries.forEach { st ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sourceType == st) EmeraldGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { sourceType = st }
                        ) {
                            Text(
                                text = st.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (sourceType == st) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = sourceTitle,
                    onValueChange = { sourceTitle = it },
                    label = { Text("Source Title (e.g. Official JAMB Past Questions 2024)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sourceReference,
                        onValueChange = { sourceReference = it },
                        label = { Text("Citation / Reference") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = originalQuestionNumber,
                        onValueChange = { originalQuestionNumber = it },
                        label = { Text("Original Q#") },
                        modifier = Modifier.weight(0.5f)
                    )
                }

                OutlinedTextField(
                    value = reviewNotes,
                    onValueChange = { reviewNotes = it },
                    label = { Text("Review Notes / Source Citation") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Switches for isFree and isActive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Free Question (Part of First 20):")
                    Switch(
                        checked = isFree,
                        onCheckedChange = { isFree = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreenPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Question Status (Active in CBT):")
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreenPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedYear = yearText.toIntOrNull() ?: 2024
                    if (questionText.isBlank()) {
                        formError = "Question text cannot be blank."
                        return@Button
                    }
                    if (optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank()) {
                        formError = "All 4 options (A, B, C, D) are required."
                        return@Button
                    }
                    if (topic.isBlank()) {
                        formError = "Please specify a topic."
                        return@Button
                    }

                    formError = null
                    onSave(
                        examType,
                        subject,
                        parsedYear,
                        topic,
                        questionText,
                        optionA,
                        optionB,
                        optionC,
                        optionD,
                        correctAnswer,
                        explanation,
                        difficulty,
                        isFree,
                        isActive,
                        sourceType,
                        sourceTitle,
                        sourceOrganization,
                        sourceReference,
                        sourceUrl,
                        sourceYearText.toIntOrNull(),
                        sourcePage,
                        originalQuestionNumber,
                        verificationStatus,
                        answerVerificationStatus,
                        reviewNotes,
                        contentPermissionStatus
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text(if (initialQuestion == null) "Create Question" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrantAccessDialog(
    students: List<StudentUser>,
    preselectedStudent: StudentUser? = null,
    onDismiss: () -> Unit,
    onGrant: (
        student: StudentUser,
        examType: ExamType,
        subjects: List<SubjectType>,
        isExamLevel: Boolean,
        reason: String,
        durationDays: Int?
    ) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(preselectedStudent ?: students.firstOrNull()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedExam by remember { mutableStateOf(ExamType.JAMB) }

    // For JAMB: exactly 4 subjects, English compulsory and locked
    var jambSelectedSubjects by remember {
        mutableStateOf(
            listOf(
                SubjectType.ENGLISH,
                SubjectType.MATHEMATICS,
                SubjectType.PHYSICS,
                SubjectType.CHEMISTRY
            )
        )
    }

    // For WAEC and NECO: all configured subjects option
    var isAllConfiguredSubjects by remember { mutableStateOf(true) }
    var waecNecoSelectedSubject by remember { mutableStateOf(SubjectType.MATHEMATICS) }

    var reason by remember { mutableStateOf("Manual admin access approval") }
    var durationDays by remember { mutableStateOf<Int?>(null) } // null = Permanent

    var studentSearchDropdownExpanded by remember { mutableStateOf(false) }

    // Filter students by name, phone, student ID, email or UID
    val filteredStudents = remember(students, searchQuery) {
        val trimmed = searchQuery.trim().lowercase()
        if (trimmed.isBlank()) {
            students
        } else {
            students.filter { s ->
                s.displayName.lowercase().contains(trimmed) ||
                    (s.phone != null && s.phone.contains(trimmed)) ||
                    (s.studentId != null && s.studentId.lowercase().contains(trimmed)) ||
                    s.email.lowercase().contains(trimmed) ||
                    s.uid.lowercase().contains(trimmed)
            }
        }
    }

    val isJambValid = selectedExam != ExamType.JAMB || (
        jambSelectedSubjects.size == 4 &&
            jambSelectedSubjects.contains(SubjectType.ENGLISH) &&
            jambSelectedSubjects.distinct().size == 4
    )

    val isFormValid = selectedStudent != null && reason.isNotBlank() && isJambValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Grant Exam Access",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Grant full CBT question access for a selected student. This updates trusted entitlements immediately and unlocks questions 21+.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                // 1. Student Search and Identification
                Text(
                    text = "1. Student Identification: *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (preselectedStudent == null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            studentSearchDropdownExpanded = true
                        },
                        placeholder = { Text("Search by name, phone, or student ID...") },
                        label = { Text("Search Student") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldGreenPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_grant_student_search")
                    )

                    // Results dropdown/picker
                    if (filteredStudents.isNotEmpty() && (searchQuery.isNotBlank() || studentSearchDropdownExpanded)) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                        ) {
                            LazyColumn(modifier = Modifier.padding(4.dp)) {
                                items(filteredStudents) { s ->
                                    val isPicked = selectedStudent?.uid == s.uid
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPicked) EmeraldGreenPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedStudent = s
                                                studentSearchDropdownExpanded = false
                                            }
                                            .padding(6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = s.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isPicked) EmeraldGreenDark else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "ID: ${s.studentId ?: "N/A"} • Phone: ${s.phone ?: "N/A"} • Email: ${s.email.ifBlank { "N/A" }}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Identifying card for the currently selected student
                selectedStudent?.let { student ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldGreenPrimary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreenPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_grant_confirmed_student_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Confirmed student",
                                    tint = EmeraldGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = student.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenDark,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Phone: ${student.phone ?: "Not registered"}  •  Student ID: ${student.studentId ?: "None"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Email: ${student.email.ifBlank { "No email (PIN login only)" }}  •  UID: ${student.uid.take(12)}...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } ?: run {
                    Text(
                        text = "No student selected. Please search and select a student above.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 2. Exam Board Selection
                Text(
                    text = "2. Select Examination Board: *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ExamType.entries.forEach { exam ->
                        val isSelected = selectedExam == exam
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedExam = exam }
                                .testTag("admin_grant_exam_${exam.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = exam.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = exam.badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Subject Configuration per Examination
                if (selectedExam == ExamType.JAMB) {
                    // JAMB combination: exactly 4 subjects, English compulsory and locked
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. JAMB Combination (4 Subjects): *",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (jambSelectedSubjects.size == 4) EmeraldGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "${jambSelectedSubjects.size} / 4 Selected",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (jambSelectedSubjects.size == 4) EmeraldGreenDark else MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "Use of English is compulsory and locked. Select exactly 3 other subjects to complete the required 4-subject combination.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        // Compulsory English card
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
                                    contentDescription = "Compulsory",
                                    tint = EmeraldGreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Use of English",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Compulsory for all JAMB candidates (Locked)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Selectable other subjects (pick exactly 3)
                        val otherSubjects = SubjectType.entries.filter { it != SubjectType.ENGLISH }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            otherSubjects.forEach { sub ->
                                val isChecked = jambSelectedSubjects.contains(sub)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isChecked) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val current = jambSelectedSubjects.toMutableList()
                                            if (isChecked) {
                                                current.remove(sub)
                                            } else {
                                                if (current.size < 4) {
                                                    current.add(sub)
                                                }
                                            }
                                            jambSelectedSubjects = current
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val current = jambSelectedSubjects.toMutableList()
                                                if (checked) {
                                                    if (current.size < 4 && !current.contains(sub)) {
                                                        current.add(sub)
                                                    }
                                                } else {
                                                    current.remove(sub)
                                                }
                                                jambSelectedSubjects = current
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
                } else {
                    // WAEC and NECO: all-configured-subjects grant option
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "3. Subject Scope for ${selectedExam.displayName}: *",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllConfiguredSubjects) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isAllConfiguredSubjects) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreenPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAllConfiguredSubjects = true }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAllConfiguredSubjects) Icons.Default.CheckCircle else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (isAllConfiguredSubjects) EmeraldGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "All Configured Subjects (Recommended)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Full access to all ${selectedExam.getAvailableSubjects().size} subjects configured for ${selectedExam.displayName}, including any subjects added in the future.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Specific single subject option as secondary choice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isAllConfiguredSubjects) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (!isAllConfiguredSubjects) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreenPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAllConfiguredSubjects = false }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (!isAllConfiguredSubjects) Icons.Default.CheckCircle else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (!isAllConfiguredSubjects) EmeraldGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Single Specific Subject Only",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Grant access to only one selected subject for this exam.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (!isAllConfiguredSubjects) {
                            var waecSubjectMenuExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = waecSubjectMenuExpanded,
                                onExpandedChange = { waecSubjectMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = waecNecoSelectedSubject.displayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Single Subject") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = waecSubjectMenuExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = waecSubjectMenuExpanded,
                                    onDismissRequest = { waecSubjectMenuExpanded = false }
                                ) {
                                    SubjectType.entries.forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub.displayName) },
                                            onClick = {
                                                waecNecoSelectedSubject = sub
                                                waecSubjectMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Duration / Expiry Options
                Text(
                    text = "4. Access Duration: *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                val durations = listOf(
                    Pair(null, "Lifetime"),
                    Pair(30, "30 Days"),
                    Pair(90, "90 Days"),
                    Pair(365, "1 Year")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    durations.forEach { (days, label) ->
                        val isSelected = durationDays == days
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldGreenDark else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { durationDays = days }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 5. Reason / Note
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Note (Required for Audit) *") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedStudent
                    if (s != null && isFormValid) {
                        val subjects = if (selectedExam == ExamType.JAMB) {
                            jambSelectedSubjects
                        } else {
                            if (isAllConfiguredSubjects) emptyList() else listOf(waecNecoSelectedSubject)
                        }
                        val isExamLevel = selectedExam != ExamType.JAMB && isAllConfiguredSubjects
                        onGrant(s, selectedExam, subjects, isExamLevel, reason, durationDays)
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                modifier = Modifier.testTag("admin_grant_confirm_button")
            ) {
                Text("Grant Full Access")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Backward-compatible overload for legacy single-subject callers.
 */
@Composable
fun GrantAccessDialog(
    students: List<StudentUser>,
    preselectedStudent: StudentUser? = null,
    onDismiss: () -> Unit,
    onGrantLegacy: (student: StudentUser, examType: ExamType, subjectType: SubjectType, reason: String, durationDays: Int?) -> Unit
) {
    GrantAccessDialog(
        students = students,
        preselectedStudent = preselectedStudent,
        onDismiss = onDismiss,
        onGrant = { s, exam, subs, isExam, reason, dur ->
            val firstSubject = subs.firstOrNull() ?: SubjectType.ENGLISH
            onGrantLegacy(s, exam, firstSubject, reason, dur)
        }
    )
}

@Composable
fun StudentDetailDialog(
    student: StudentUser,
    entitlements: List<AccessEntitlement>,
    onDismiss: () -> Unit,
    onGrantAccess: () -> Unit,
    onRevokeEntitlement: (AccessEntitlement) -> Unit,
    onResetPin: ((String) -> Unit)? = null,
    onTransferDevice: ((String) -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    var showPinResetDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferPinInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldGreenPrimary,
                    modifier = Modifier.size(40.dp)
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
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = student.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Profile facts
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Student UID: ${student.uid}", style = MaterialTheme.typography.labelSmall)
                        student.phone?.let {
                            Text(text = "Phone: $it", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        student.studentId?.let {
                            Text(text = "Student ID: $it", style = MaterialTheme.typography.labelSmall)
                        }
                        Text(text = "Target Exam: ${student.targetExam.displayName} (${student.targetYear})", style = MaterialTheme.typography.labelSmall)
                        Text(text = "Registered: ${dateFormat.format(Date(student.joinedDateMillis))}", style = MaterialTheme.typography.labelSmall)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (onResetPin != null) {
                                OutlinedButton(
                                    onClick = { showPinResetDialog = true },
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Reset 4-Digit PIN", fontSize = 11.sp)
                                }
                            }
                            if (onTransferDevice != null) {
                                OutlinedButton(
                                    onClick = { showTransferDialog = true },
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Authorize Device Transfer", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Access Entitlements (${entitlements.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Button(
                        onClick = onGrantAccess,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Grant Access", fontSize = 12.sp)
                    }
                }

                if (entitlements.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No access entitlements granted yet. Student is currently on standard free tier (first 20 questions free).",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    entitlements.forEach { ent ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${ent.examType.displayName} • ${ent.subjectType.displayName}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    StatusPill(status = ent.effectiveStatus)
                                }

                                Text(
                                    text = "Source: ${ent.accessType.displayName}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "Granted by: ${ent.grantedBy} on ${dateFormat.format(Date(ent.grantedAt))}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "Expires: ${ent.expiresAt?.let { dateFormat.format(Date(it)) } ?: "Permanent / Lifetime"}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )

                                if (ent.reason.isNotBlank()) {
                                    Text(
                                        text = "Note: ${ent.reason}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    )
                                }

                                if (ent.effectiveStatus == EntitlementStatus.ACTIVE) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedButton(
                                        onClick = { onRevokeEntitlement(ent) },
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
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)) {
                Text("Done")
            }
        }
    )

    if (showPinResetDialog) {
        val pinValidation = remember(newPinInput) { StudentPinManager.validatePin(newPinInput) }
        AlertDialog(
            onDismissRequest = { showPinResetDialog = false },
            title = { Text("Reset Student PIN") },
            text = {
                Column {
                    Text("Enter a new 4-digit PIN for ${student.displayName}. The PIN is hashed locally with a fresh random salt and is never stored in Firestore.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { newPinInput = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("New 4-Digit PIN") },
                        isError = newPinInput.length == 4 && !pinValidation.isValid,
                        supportingText = {
                            if (newPinInput.length == 4 && !pinValidation.isValid) {
                                Text((pinValidation as? PinValidationResult.Invalid)?.reason ?: "Invalid PIN")
                            }
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinValidation.isValid) {
                            onResetPin?.invoke(newPinInput)
                            showPinResetDialog = false
                            newPinInput = ""
                        }
                    },
                    enabled = pinValidation.isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPinResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTransferDialog) {
        val transferPinValidation = remember(transferPinInput) { StudentPinManager.validatePin(transferPinInput) }
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Transfer Student to Device") },
            text = {
                Column {
                    Text("Authorize transferring ${student.displayName}'s profile to this device. Set a 4-digit PIN for device login (will be salted & hashed locally).")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = transferPinInput,
                        onValueChange = { transferPinInput = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Device Access PIN") },
                        isError = transferPinInput.length == 4 && !transferPinValidation.isValid,
                        supportingText = {
                            if (transferPinInput.length == 4 && !transferPinValidation.isValid) {
                                Text((transferPinValidation as? PinValidationResult.Invalid)?.reason ?: "Invalid PIN")
                            }
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (transferPinValidation.isValid) {
                            onTransferDevice?.invoke(transferPinInput)
                            showTransferDialog = false
                            transferPinInput = ""
                        }
                    },
                    enabled = transferPinValidation.isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                ) {
                    Text("Authorize Transfer")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTransferDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
