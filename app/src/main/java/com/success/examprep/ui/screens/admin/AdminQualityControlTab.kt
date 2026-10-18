package com.success.examprep.ui.screens.admin

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.QuestionBankQualitySummary
import com.success.examprep.data.model.QuestionSourceType
import com.success.examprep.data.model.QuestionVerificationStatus
import com.success.examprep.data.model.SubjectType
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenDark
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import com.success.examprep.ui.theme.ErrorRed
import com.success.examprep.ui.theme.SuccessGreen
import com.success.examprep.ui.theme.TealSecondary

/**
 * Stage 7A: Quality Control & Audit Dashboard Tab.
 *
 * Tracks the target of 50 verified questions per exam-body and subject combination.
 * Enforces provenance separation: Verified Past Questions vs Practice Questions.
 * Warns against unverified or fabricated provenance.
 */
@Composable
fun AdminQualityControlTab(
    viewModel: AdminViewModel,
    uiState: AdminUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_quality_control_tab"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Provenance Integrity Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = EmeraldGreenPrimary.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreenPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = EmeraldGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Proven Quality & Editorial Integrity (Stage 7A)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Target: 50 approved questions per exam body and subject combination. Practice items are kept separate from genuine past questions. Unverified questions require administrator review before publication.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        item {
            // Summary KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Verified Past Qs",
                    value = uiState.stats.verifiedPastQuestions.toString(),
                    subtitle = "Authenticated",
                    iconColor = EmeraldGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Practice Qs",
                    value = uiState.stats.practiceQuestions.toString(),
                    subtitle = "Curated",
                    iconColor = TealSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Review",
                    value = uiState.stats.pendingReviewQuestions.toString(),
                    subtitle = "Needs QC",
                    iconColor = AmberTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Subject Quota Tracking (Min 50 per Offering)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Group summaries by Exam Body
        ExamType.entries.forEach { exam ->
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${exam.displayName} — ${exam.fullName}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            val examSummaries = uiState.qualitySummaries.filter { it.examType == exam }
            items(examSummaries) { summary ->
                QuotaProgressCard(
                    summary = summary,
                    onInspectQuestions = {
                        viewModel.onFilterExamChanged(summary.examType)
                        viewModel.onFilterSubjectChanged(summary.subject)
                        viewModel.selectTab(AdminTab.QUESTIONS)
                    }
                )
            }
        }

        // Review queue quick jump
        if (uiState.stats.pendingReviewQuestions > 0) {
            item {
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
                                text = "Editorial Review Queue (${uiState.stats.pendingReviewQuestions})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Questions imported or drafted waiting for administrator verification and answer key confirmation.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.onFilterVerificationStatusChanged(QuestionVerificationStatus.PENDING_REVIEW)
                                viewModel.selectTab(AdminTab.QUESTIONS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                        ) {
                            Text("Open Queue", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaProgressCard(
    summary: QuestionBankQualitySummary,
    onInspectQuestions: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = summary.subject.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (summary.meetsThreshold) {
                        Surface(shape = RoundedCornerShape(6.dp), color = SuccessGreen.copy(alpha = 0.15f)) {
                            Text(
                                text = "TARGET MET (≥50)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(6.dp), color = AmberTertiary.copy(alpha = 0.2f)) {
                            Text(
                                text = "${summary.totalCount}/200 QUESTIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                TextButton(onClick = onInspectQuestions) {
                    Text("Inspect", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            val progressFraction = (summary.totalCount.toFloat() / summary.targetThreshold).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progressFraction },
                color = if (summary.meetsThreshold) SuccessGreen else EmeraldGreenPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Past Questions: ${summary.verifiedPastQuestionCount}",
                    style = MaterialTheme.typography.labelSmall.copy(color = EmeraldGreenDark, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Practice: ${summary.practiceQuestionCount}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Pending: ${summary.pendingReviewCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (summary.pendingReviewCount > 0) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
