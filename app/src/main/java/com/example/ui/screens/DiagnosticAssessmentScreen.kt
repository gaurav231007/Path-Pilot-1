package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandRose
import com.example.ui.viewmodel.PathPilotViewModel
import com.example.ui.viewmodel.ScreenState

@Composable
fun DiagnosticAssessmentScreen(
    viewModel: PathPilotViewModel,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session by viewModel.testSession.collectAsState()
    var showQuitDialog by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }

    val currentQ = session.questions.getOrNull(session.currentIndex)
    val totalCount = session.questions.size
    val answeredCount = session.selectedAnswers.size

    val progress = if (totalCount > 0) (session.currentIndex + 1).toFloat() / totalCount else 0f

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Exit Assessment?") },
            text = { Text("Your ongoing attempt will be discarded. Are you sure you want to return to the dashboard?") },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onCancel()
                }) {
                    Text("Exit", color = BrandRose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Continue Test")
                }
            }
        )
    }

    if (showSubmitConfirmation) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            title = { Text("Submit Diagnostic Test?") },
            text = {
                Text("You have answered $answeredCount of $totalCount questions. Submitting will calibrate your Student DNA and generate your Next Best Move.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmation = false
                        viewModel.submitAssessment()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                ) {
                    Text("Submit & Calibrate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmation = false }) {
                    Text("Review Questions")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("diagnostic_assessment_screen")
    ) {
        // Top bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showQuitDialog = true }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit Test")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = session.examName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Question ${session.currentIndex + 1} of $totalCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Question Timer Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val qSec = session.currentQuestionSeconds
                            val formattedTime = String.format("%02d:%02d", qSec / 60, qSec % 60)
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrandCyan,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Question Navigator Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(session.questions) { index, q ->
                val isSelected = index == session.currentIndex
                val isAnswered = session.selectedAnswers.containsKey(q.id)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isAnswered -> BrandEmerald.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            width = if (isSelected) 2.dp else if (isAnswered) 1.5.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else if (isAnswered) BrandEmerald else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { viewModel.navigateQuestion(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected || isAnswered) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isAnswered) BrandEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Main Question & Options Content
        if (currentQ != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Difficulty Chip
                val diffColor = when (currentQ.difficulty.uppercase()) {
                    "HARD" -> BrandRose
                    "MEDIUM" -> BrandAmber
                    else -> BrandEmerald
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = diffColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "${currentQ.difficulty.uppercase()} DIFFICULTY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = diffColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Question Text
                Text(
                    text = currentQ.question,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Options List
                val selectedOption = session.selectedAnswers[currentQ.id]

                OptionCard(
                    optionKey = "A",
                    optionText = currentQ.optionA,
                    isSelected = selectedOption == "A",
                    onSelect = { viewModel.selectAnswer("A") }
                )
                Spacer(modifier = Modifier.height(10.dp))

                OptionCard(
                    optionKey = "B",
                    optionText = currentQ.optionB,
                    isSelected = selectedOption == "B",
                    onSelect = { viewModel.selectAnswer("B") }
                )
                Spacer(modifier = Modifier.height(10.dp))

                OptionCard(
                    optionKey = "C",
                    optionText = currentQ.optionC,
                    isSelected = selectedOption == "C",
                    onSelect = { viewModel.selectAnswer("C") }
                )
                Spacer(modifier = Modifier.height(10.dp))

                OptionCard(
                    optionKey = "D",
                    optionText = currentQ.optionD,
                    isSelected = selectedOption == "D",
                    onSelect = { viewModel.selectAnswer("D") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Controls
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                OutlinedButton(
                    onClick = { viewModel.navigateQuestion(session.currentIndex - 1) },
                    enabled = session.currentIndex > 0,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prev")
                }

                // Next or Submit
                if (session.currentIndex < totalCount - 1) {
                    Button(
                        onClick = { viewModel.navigateQuestion(session.currentIndex + 1) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("next_question_button")
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = { showSubmitConfirmation = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                        modifier = Modifier.testTag("submit_test_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Submit Assessment")
                    }
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    optionKey: String,
    optionText: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .testTag("option_$optionKey"),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionKey,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
