package com.success.examprep.ui.screens.cbt

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.success.examprep.data.access.AccessConfig
import com.success.examprep.data.model.Question
import com.success.examprep.ui.theme.AmberTertiary
import com.success.examprep.ui.theme.EmeraldGreenPrimary
import com.success.examprep.ui.theme.ErrorRed
import com.success.examprep.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbtScreen(
    viewModel: CbtViewModel,
    onExamSubmitted: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToUnlock: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle Android system back press with confirmation
    BackHandler {
        viewModel.setSubmitDialogVisible(true)
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onExamSubmitted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.examType.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${uiState.subject?.name ?: "Subject"}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldGreenPrimary
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Countdown Timer Pill in App Bar
                    CbtTimerPill(
                        formattedTime = uiState.formattedTime,
                        isTimeCritical = uiState.isTimeCritical
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            CbtBottomActionBar(
                isFirst = uiState.isFirstQuestion,
                isLast = uiState.currentIndex >= uiState.totalQuestions - 1,
                onPreviousClick = { viewModel.previousQuestion() },
                onNextClick = { viewModel.nextQuestion() },
                onSubmitClick = { viewModel.setSubmitDialogVisible(true) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("cbt_screen")
        ) {
            // Free Practice Badge Banner (if in free mode)
            if (uiState.isFreePracticeMode) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AmberTertiary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, AmberTertiary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onNavigateToUnlock() }
                        .testTag("free_mode_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AmberTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Free Practice: First ${AccessConfig.FREE_QUESTION_LIMIT} of ${uiState.totalQuestions} questions",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AmberTertiary
                            )
                        }

                        Text(
                            text = "Unlock All",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberTertiary
                            )
                        )
                    }
                }
            }

            // Question Progress Header & Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreenPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Question ${uiState.currentIndex + 1} of ${uiState.accessibleQuestionsCount}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    uiState.currentQuestion?.topic?.let { topic ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Answered counter badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.answeredCount}/${uiState.accessibleQuestionsCount} Answered",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Question Card
            val currentQ = uiState.currentQuestion
            if (currentQ != null) {
                QuestionCard(question = currentQ)

                Spacer(modifier = Modifier.height(16.dp))

                // Options List (A, B, C, D)
                val selectedOptionIndex = uiState.selectedAnswers[currentQ.id]
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentQ.options.forEachIndexed { index, optionText ->
                        OptionRow(
                            optionIndex = index,
                            optionText = optionText,
                            isSelected = selectedOptionIndex == index,
                            onSelect = { viewModel.selectOption(index) },
                            testTag = "option_${index}_q${currentQ.id}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Question Navigator Grid (1 to 20 free, 21.. locked with 🔒)
            QuestionNavigatorSection(
                totalQuestions = uiState.totalQuestions,
                currentIndex = uiState.currentIndex,
                selectedAnswers = uiState.selectedAnswers,
                questions = uiState.questions,
                isQuestionLocked = { index -> uiState.isQuestionLocked(index) },
                onQuestionClick = { index -> viewModel.jumpToQuestion(index) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Submit Confirmation Dialog
    if (uiState.showSubmitDialog) {
        SubmitConfirmDialog(
            answeredCount = uiState.answeredCount,
            unansweredCount = uiState.unansweredCount,
            formattedTime = uiState.formattedTime,
            isFreePractice = uiState.isFreePracticeMode,
            onDismiss = { viewModel.setSubmitDialogVisible(false) },
            onConfirmSubmit = { viewModel.submitExam() }
        )
    }

    // Full Access Required Dialog
    if (uiState.showLockDialog) {
        FullAccessRequiredDialog(
            lockedQuestionNumber = uiState.lockedAttemptNumber ?: (AccessConfig.FREE_QUESTION_LIMIT + 1),
            onDismiss = { viewModel.dismissLockDialog() },
            onUnlockClick = {
                viewModel.dismissLockDialog()
                onNavigateToUnlock()
            }
        )
    }
}

@Composable
private fun CbtTimerPill(
    formattedTime: String,
    isTimeCritical: Boolean
) {
    val backgroundColor = if (isTimeCritical) {
        ErrorRed.copy(alpha = 0.15f)
    } else {
        EmeraldGreenPrimary.copy(alpha = 0.12f)
    }

    val contentColor = if (isTimeCritical) ErrorRed else EmeraldGreenPrimary

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HourglassBottom,
                contentDescription = "Timer",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "TIME LEFT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = contentColor
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(question: Question) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Stage 7A: Provenance Indicator in CBT test view
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (question.isVerifiedPastQuestion) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldGreenPrimary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VERIFIED PAST QUESTION • ${question.examType.displayName} ${question.year}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = EmeraldGreenPrimary
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "CURATED PRACTICE ITEM • ${question.examType.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun OptionRow(
    optionIndex: Int,
    optionText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    testTag: String
) {
    val letter = when (optionIndex) {
        0 -> "A"
        1 -> "B"
        2 -> "C"
        3 -> "D"
        else -> "?"
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            EmeraldGreenPrimary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) EmeraldGreenPrimary else MaterialTheme.colorScheme.outlineVariant,
        label = "optionBorder"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option Letter Box (A, B, C, D)
            Surface(
                shape = CircleShape,
                color = if (isSelected) EmeraldGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) EmeraldGreenPrimary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionNavigatorSection(
    totalQuestions: Int,
    currentIndex: Int,
    selectedAnswers: Map<Int, Int>,
    questions: List<Question>,
    isQuestionLocked: (Int) -> Boolean,
    onQuestionClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question Navigator",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap number to jump",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-state pills 1..total (with 🔒 for locked questions)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until totalQuestions) {
                    val question = questions.getOrNull(i)
                    val isCurrent = (i == currentIndex)
                    val isAnswered = question != null && selectedAnswers.containsKey(question.id)
                    val isLocked = isQuestionLocked(i)

                    QuestionNumberPill(
                        number = i + 1,
                        isCurrent = isCurrent,
                        isAnswered = isAnswered,
                        isLocked = isLocked,
                        onClick = { onQuestionClick(i) },
                        testTag = if (isLocked) "locked_nav_pill_${i + 1}" else "nav_pill_${i + 1}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual legend explaining states
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    icon = Icons.Default.RadioButtonChecked,
                    iconTint = EmeraldGreenPrimary,
                    label = "Current"
                )
                LegendItem(
                    icon = Icons.Default.Check,
                    iconTint = SuccessGreen,
                    label = "Answered"
                )
                LegendItem(
                    icon = Icons.Default.FiberManualRecord,
                    iconTint = MaterialTheme.colorScheme.outline,
                    label = "Unanswered"
                )
                LegendItem(
                    icon = Icons.Default.Lock,
                    iconTint = AmberTertiary,
                    label = "Locked"
                )
            }
        }
    }
}

@Composable
private fun QuestionNumberPill(
    number: Int,
    isCurrent: Boolean,
    isAnswered: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    // Accessible, multi-state: distinguished by shape, border, icon, and background
    val pillBackground = when {
        isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        isCurrent -> EmeraldGreenPrimary
        isAnswered -> SuccessGreen.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val pillTextColor = when {
        isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        isCurrent -> Color.White
        isAnswered -> SuccessGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderModifier = when {
        isLocked -> Modifier.border(1.dp, AmberTertiary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        isCurrent -> Modifier.border(2.dp, EmeraldGreenPrimary, RoundedCornerShape(10.dp))
        isAnswered -> Modifier.border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        else -> Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
    }

    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(borderModifier)
            .background(pillBackground)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = pillTextColor
            )

            if (isLocked) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Question",
                    tint = AmberTertiary,
                    modifier = Modifier.size(13.dp)
                )
            } else if (isAnswered && !isCurrent) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Answered",
                    tint = SuccessGreen,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CbtBottomActionBar(
    isFirst: Boolean,
    isLast: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            OutlinedButton(
                onClick = onPreviousClick,
                enabled = !isFirst,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("previous_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Previous")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Next Button
            OutlinedButton(
                onClick = onNextClick,
                enabled = !isLast,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("next_button")
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Submit Button
            Button(
                onClick = onSubmitClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberTertiary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("submit_test_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Submit Test",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun SubmitConfirmDialog(
    answeredCount: Int,
    unansweredCount: Int,
    formattedTime: String,
    isFreePractice: Boolean,
    onDismiss: () -> Unit,
    onConfirmSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isFreePractice) "Submit Free Practice Test?" else "Are you sure you want to submit?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "You will not be able to change your answers once submitted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Answered Questions:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "$answeredCount",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Unanswered Questions:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "$unansweredCount",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (unansweredCount > 0) ErrorRed else SuccessGreen
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Time Remaining:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenPrimary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                modifier = Modifier.testTag("dialog_submit_confirm")
            ) {
                Text("SUBMIT")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_submit_cancel")
            ) {
                Text("CANCEL")
            }
        }
    )
}

/**
 * Full-Access Required Dialog triggered when tapping locked questions or advancing past free limit.
 */
@Composable
private fun FullAccessRequiredDialog(
    lockedQuestionNumber: Int,
    onDismiss: () -> Unit,
    onUnlockClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = AmberTertiary.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AmberTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "FULL ACCESS REQUIRED",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "You have completed your ${AccessConfig.FREE_QUESTION_LIMIT} free questions.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = "Question $lockedQuestionNumber and beyond are locked. Unlock full access to continue practising all available questions with complete syllabus coverage.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUnlockClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_unlock_full_access_button")
            ) {
                Text("Unlock Full Access")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_maybe_later_button")
            ) {
                Text("Maybe Later")
            }
        }
    )
}
