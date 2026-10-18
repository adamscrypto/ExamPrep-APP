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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.model.AnswerVerificationStatus
import com.success.examprep.data.model.ContentPermissionStatus
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.QuestionReviewChecklist
import com.success.examprep.data.model.QuestionSourceType
import com.success.examprep.data.model.QuestionVerificationStatus
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import com.success.examprep.ui.theme.ErrorRed
import com.success.examprep.ui.theme.SuccessGreen

/**
 * Stage 7A: Formal Admin Question Verification and Editorial Quality-Control Dialog.
 *
 * Implements the full review workflow:
 * DRAFT -> PENDING_REVIEW -> VERIFIED -> PUBLISHED
 *
 * Provides:
 * - Structured review checklist (authenticity, exam body/year, text, options, independent solve, explanation, permissions)
 * - Option inspection and answer correction before publication
 * - Provenance metadata inspection (source type, publication title, organization, reference, URL, page, original number)
 * - Verification status transitions (PENDING_REVIEW, VERIFIED, REJECTED, DRAFT)
 * - Answer verification status (UNVERIFIED, REVIEWED, DISPUTED)
 * - Review notes recording
 * - Immediate or deferred publication toggle (rejected questions are never published)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionVerificationReviewDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSubmitReview: (
        verificationStatus: QuestionVerificationStatus,
        answerVerificationStatus: AnswerVerificationStatus,
        shouldPublish: Boolean,
        reviewNotes: String,
        correctedAnswer: OptionLetter?
    ) -> Unit
) {
    var verificationStatus by remember { mutableStateOf(question.verificationStatus) }
    var answerVerificationStatus by remember { mutableStateOf(question.answerVerificationStatus) }
    var selectedAnswer by remember { mutableStateOf(question.correctAnswer) }
    var shouldPublish by remember { mutableStateOf(question.isActive) }
    var reviewNotes by remember { mutableStateOf(question.reviewNotes) }

    // Checklist state
    var checklist by remember {
        mutableStateOf(
            QuestionReviewChecklist(
                sourceAuthenticityChecked = question.sourceTitle.isNotBlank() || question.sourceReference.isNotBlank(),
                examBodyAndSubjectConfirmed = true,
                examYearConfirmed = question.year in 2000..2050,
                questionTextCheckedAgainstSource = question.questionText.length > 10,
                allAnswerOptionsChecked = question.options.all { it.isNotBlank() },
                correctAnswerIndependentlySolved = question.answerVerificationStatus == AnswerVerificationStatus.REVIEWED,
                explanationReviewed = question.explanation.isNotBlank(),
                duplicateCheckCompleted = true,
                permissionRequirementsSatisfied = question.contentPermissionStatus != ContentPermissionStatus.RESTRICTED
            )
        )
    }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var answerStatusDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question Review & Verification",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    ProvenanceStatusBadge(question.verificationStatus)
                }
                Text(
                    text = "ID: #${question.id} • ${question.examType.displayName} • ${question.subject.displayName} (${question.year})",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Provenance & Source Metadata Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Source Provenance",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            SourceTypeBadge(question.sourceType)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Title / Work: ${question.sourceTitle.ifBlank { "Not provided (classified as Practice Question)" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (question.sourceOrganization.isNotBlank()) {
                            Text(
                                text = "Organization: ${question.sourceOrganization}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (question.sourceReference.isNotBlank()) {
                            Text(
                                text = "Reference / Citation: ${question.sourceReference}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (question.originalQuestionNumber.isNotBlank() || question.sourcePage.isNotBlank()) {
                            Text(
                                text = "Original Q#: ${question.originalQuestionNumber.ifBlank { "N/A" }} • Page: ${question.sourcePage.ifBlank { "N/A" }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (question.sourceUrl.isNotBlank()) {
                            Text(
                                text = "Source URL: ${question.sourceUrl}",
                                style = MaterialTheme.typography.bodySmall.copy(color = EmeraldGreenDark)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Permission Status: ${question.contentPermissionStatus.label}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                // Section 2: Question Prompt & Syllabus Topic
                Column {
                    Text(
                        text = "Topic: ${question.topic}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = EmeraldGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }

                // Section 3: Inspect & Correct Answer Options
                Text(
                    text = "Options & Proposed Answer:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                val optionsList = listOf(
                    Triple(OptionLetter.A, "A", question.optionA),
                    Triple(OptionLetter.B, "B", question.optionB),
                    Triple(OptionLetter.C, "C", question.optionC),
                    Triple(OptionLetter.D, "D", question.optionD)
                )

                optionsList.forEach { (letter, label, text) ->
                    val isProposed = letter == selectedAnswer
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isProposed) EmeraldGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isProposed) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreenPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnswer = letter }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isProposed) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isProposed) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isProposed) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (isProposed) {
                                Text(
                                    text = if (letter != question.correctAnswer) "CORRECTED" else "CURRENT KEY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreenDark,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                if (question.explanation.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Explanation / Solution:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = question.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Section 4: Quality Review Checklist (Stage 7A)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verification Checklist (${checklist.satisfiedCount}/${checklist.totalCount})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (checklist.isFullySatisfied) {
                                Surface(shape = RoundedCornerShape(6.dp), color = SuccessGreen.copy(alpha = 0.2f)) {
                                    Text(
                                        text = "AUDIT PASSED",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        ChecklistItem("Source authenticity verified", checklist.sourceAuthenticityChecked) {
                            checklist = checklist.copy(sourceAuthenticityChecked = it)
                        }
                        ChecklistItem("Exam body and subject confirmed", checklist.examBodyAndSubjectConfirmed) {
                            checklist = checklist.copy(examBodyAndSubjectConfirmed = it)
                        }
                        ChecklistItem("Examination year confirmed", checklist.examYearConfirmed) {
                            checklist = checklist.copy(examYearConfirmed = it)
                        }
                        ChecklistItem("Question text checked against source", checklist.questionTextCheckedAgainstSource) {
                            checklist = checklist.copy(questionTextCheckedAgainstSource = it)
                        }
                        ChecklistItem("All 4 answer options reviewed", checklist.allAnswerOptionsChecked) {
                            checklist = checklist.copy(allAnswerOptionsChecked = it)
                        }
                        ChecklistItem("Answer key independently solved/checked", checklist.correctAnswerIndependentlySolved) {
                            checklist = checklist.copy(correctAnswerIndependentlySolved = it)
                        }
                        ChecklistItem("Explanation reviewed and accurate", checklist.explanationReviewed) {
                            checklist = checklist.copy(explanationReviewed = it)
                        }
                        ChecklistItem("Duplicate check completed", checklist.duplicateCheckCompleted) {
                            checklist = checklist.copy(duplicateCheckCompleted = it)
                        }
                        ChecklistItem("Permission/provenance requirements satisfied", checklist.permissionRequirementsSatisfied) {
                            checklist = checklist.copy(permissionRequirementsSatisfied = it)
                        }
                    }
                }

                // Section 5: Verification Action Controls
                Text(
                    text = "Editorial Decision:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Verification Status Selector
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = verificationStatus.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Verification Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        QuestionVerificationStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label) },
                                onClick = {
                                    verificationStatus = status
                                    statusDropdownExpanded = false
                                    // If rejected, automatically uncheck publication
                                    if (status == QuestionVerificationStatus.REJECTED) {
                                        shouldPublish = false
                                    }
                                }
                            )
                        }
                    }
                }

                // Answer Verification Status Selector
                ExposedDropdownMenuBox(
                    expanded = answerStatusDropdownExpanded,
                    onExpandedChange = { answerStatusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = answerVerificationStatus.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Answer Review Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = answerStatusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = answerStatusDropdownExpanded,
                        onDismissRequest = { answerStatusDropdownExpanded = false }
                    ) {
                        AnswerVerificationStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label) },
                                onClick = {
                                    answerVerificationStatus = status
                                    answerStatusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Publication Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Publish to Active CBT Bank:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (verificationStatus == QuestionVerificationStatus.REJECTED)
                                "Rejected questions cannot be published"
                            else
                                "Makes question accessible to CBT test takers",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = shouldPublish && verificationStatus != QuestionVerificationStatus.REJECTED,
                        onCheckedChange = {
                            if (verificationStatus != QuestionVerificationStatus.REJECTED) {
                                shouldPublish = it
                            }
                        },
                        enabled = verificationStatus != QuestionVerificationStatus.REJECTED,
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreenPrimary)
                    )
                }

                // Reviewer Notes
                OutlinedTextField(
                    value = reviewNotes,
                    onValueChange = { reviewNotes = it },
                    label = { Text("Audit / Review Notes") },
                    placeholder = { Text("Record verification notes, source edition, or resolution details...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val corrected = if (selectedAnswer != question.correctAnswer) selectedAnswer else null
                    onSubmitReview(
                        verificationStatus,
                        answerVerificationStatus,
                        shouldPublish,
                        reviewNotes,
                        corrected
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (verificationStatus) {
                        QuestionVerificationStatus.VERIFIED -> EmeraldGreenPrimary
                        QuestionVerificationStatus.REJECTED -> ErrorRed
                        else -> MaterialTheme.colorScheme.primary
                    }
                ),
                modifier = Modifier.testTag("admin_submit_question_review_btn")
            ) {
                Text(
                    text = when (verificationStatus) {
                        QuestionVerificationStatus.VERIFIED -> if (shouldPublish) "Approve & Publish" else "Approve (Unpublished)"
                        QuestionVerificationStatus.REJECTED -> "Confirm Rejection"
                        else -> "Save Review"
                    }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChecklistItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 1.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = EmeraldGreenPrimary),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = if (checked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun ProvenanceStatusBadge(status: QuestionVerificationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        QuestionVerificationStatus.VERIFIED -> Pair(EmeraldGreenPrimary.copy(alpha = 0.15f), EmeraldGreenDark)
        QuestionVerificationStatus.PENDING_REVIEW -> Pair(AmberTertiary.copy(alpha = 0.2f), Color(0xFFB45309))
        QuestionVerificationStatus.DRAFT -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        QuestionVerificationStatus.REJECTED -> Pair(ErrorRed.copy(alpha = 0.15f), ErrorRed)
        QuestionVerificationStatus.UNVERIFIED -> Pair(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), MaterialTheme.colorScheme.error)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = status.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = textColor, fontSize = 9.sp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SourceTypeBadge(sourceType: QuestionSourceType, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (sourceType) {
        QuestionSourceType.OFFICIAL -> Pair(EmeraldGreenPrimary.copy(alpha = 0.15f), EmeraldGreenDark)
        QuestionSourceType.AUTHORIZED_PUBLICATION -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        QuestionSourceType.USER_SUPPLIED_WITH_SOURCE -> Pair(AmberTertiary.copy(alpha = 0.2f), Color(0xFFB45309))
        QuestionSourceType.PRACTICE -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = sourceType.displayName,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = textColor, fontSize = 9.sp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
