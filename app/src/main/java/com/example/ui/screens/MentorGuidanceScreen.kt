package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.viewmodel.PathPilotViewModel

@Composable
fun MentorGuidanceScreen(
    viewModel: PathPilotViewModel,
    onBack: () -> Unit,
    onStartRecommendedDrill: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val dna by viewModel.studentDNA.collectAsState()
    val report by viewModel.mentorReport.collectAsState()
    val isGenerating by viewModel.isGeneratingMentor.collectAsState()
    val nextAction by viewModel.nextBestAction.collectAsState()
    val isGeminiConfigured by viewModel.isGeminiConfigured.collectAsState()

    val mentorAnswer by viewModel.mentorAnswer.collectAsState()
    val isAskingMentor by viewModel.isAskingMentor.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var mentorQuestionInput by remember { mutableStateOf("") }

    // API Key Dialog
    if (showApiKeyDialog) {
        var enteredKey by remember { mutableStateOf(viewModel.getEffectiveGeminiApiKey()) }
        var testResultMsg by remember { mutableStateOf<String?>(null) }
        var isTestingKey by remember { mutableStateOf(false) }
        var testSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini API Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Path Pilot uses Google's Gemini 3.5 Flash model for hyper-personalized diagnostic debriefs and real-time student Q&A.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = enteredKey,
                        onValueChange = {
                            enteredKey = it
                            testResultMsg = null
                        },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    if (testResultMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (testSuccess) BrandEmerald.copy(alpha = 0.15f) else BrandRose.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (testSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (testSuccess) BrandEmerald else BrandRose,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = testResultMsg ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (testSuccess) BrandEmerald else BrandRose
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isTestingKey = true
                                testResultMsg = null
                                viewModel.testGeminiApiKey(enteredKey) { success, msg ->
                                    isTestingKey = false
                                    testSuccess = success
                                    testResultMsg = msg
                                }
                            },
                            enabled = enteredKey.isNotBlank() && !isTestingKey,
                            modifier = Modifier.testTag("test_gemini_key_button")
                        ) {
                            if (isTestingKey) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text("Test Connection")
                        }

                        if (enteredKey.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    viewModel.clearGeminiApiKey()
                                    enteredKey = ""
                                    testResultMsg = "Key removed. Using local rule engine."
                                    testSuccess = false
                                }
                            ) {
                                Text("Clear", color = BrandRose)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveGeminiApiKey(enteredKey)
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                    modifier = Modifier.testTag("save_gemini_key_button")
                ) {
                    Text("Save & Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("mentor_guidance_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Path Pilot AI Mentor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Observe • Explain • Guide",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.testTag("gemini_config_icon_button")
                    ) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = "Configure Gemini API",
                            tint = if (isGeminiConfigured) BrandEmerald else BrandAmber
                        )
                    }

                    IconButton(
                        onClick = { viewModel.generateMentorGuidance() },
                        enabled = !isGenerating,
                        modifier = Modifier.testTag("refresh_mentor_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate Guidance")
                        }
                    }
                }
            }
        }

        // Gemini Status Pill Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isGeminiConfigured) BrandEmerald.copy(alpha = 0.12f) else BrandAmber.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showApiKeyDialog = true }
                    .testTag("gemini_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isGeminiConfigured) BrandEmerald else BrandAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isGeminiConfigured) "Gemini 3.5 Flash: Live AI Connected" else "Offline AI Rule Engine Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isGeminiConfigured) BrandEmerald else BrandAmber
                            )
                            Text(
                                text = if (isGeminiConfigured) "Real-time generative analysis enabled" else "Tap to configure Google Gemini API Key",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = (if (isGeminiConfigured) BrandEmerald else BrandAmber).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isGeminiConfigured) "LIVE" else "CONFIG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (isGeminiConfigured) BrandEmerald else BrandAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Student DNA Context Summary Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Student DNA Profile",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${user?.name ?: "Student"} • ${user?.targetExam ?: "JEE"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${dna?.readinessScore ?: 72}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandCyan
                            )
                            Text("Readiness", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${"%.1f".format(dna?.accuracy ?: 68.4f)}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandEmerald
                            )
                            Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${"%.0f".format(dna?.speedSec ?: 44f)}s",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandAmber
                            )
                            Text("Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Interactive "Ask Gemini Mentor" Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ask Gemini Mentor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandIndigo.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "INTERACTIVE AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Suggested quick prompt chips
                    val suggestedPrompts = listOf(
                        "How to stop negative marks in JEE?",
                        "Create 3-day revision plan for Electrostatics",
                        "How to solve physics numericals faster?"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedPrompts) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    mentorQuestionInput = prompt
                                    viewModel.askMentorQuestion(prompt)
                                }
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = mentorQuestionInput,
                            onValueChange = { mentorQuestionInput = it },
                            placeholder = { Text("Ask your mentor anything...", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mentor_question_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (mentorQuestionInput.isNotBlank()) {
                                    viewModel.askMentorQuestion(mentorQuestionInput)
                                }
                            },
                            enabled = mentorQuestionInput.isNotBlank() && !isAskingMentor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (mentorQuestionInput.isNotBlank() && !isAskingMentor) BrandIndigo else MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("ask_mentor_button")
                        ) {
                            if (isAskingMentor) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Mentor Answer Display
                    if (mentorAnswer != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Gemini Mentor Guidance:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandCyan
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.clearMentorAnswer() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = mentorAnswer ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Loading State for Full Report
        if (isGenerating) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = BrandCyan)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Synthesizing Personalized Mentorship...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gemini is analyzing your error vectors and high-weightage topics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Mentor report content
            val activeReport = report ?: com.example.ai.MentorReport(
                personalizedAnalysis = "Your learning curve demonstrates strong foundational discipline with high upside in core technical topics.",
                biggestWeakness = dna?.weakTopics?.split(",")?.firstOrNull() ?: "Electrostatics & Capacitance",
                biggestStrength = dna?.strongTopics?.split(",")?.firstOrNull() ?: "Kinematics & Dynamics",
                recommendedFocusArea = dna?.weakTopics?.split(",")?.firstOrNull() ?: "Electrostatics & Capacitance",
                studyStrategy = "Adopt the 30-10 Decision Rule: Spend 30 minutes clarifying the underlying physics or logic, followed by 10 untimed verification questions.",
                improvementSuggestions = listOf(
                    "Maintain an active 'Mistake Ledger' specifically recording why an incorrect option seemed plausible.",
                    "Benchmark your solving rhythm to 45 seconds on standard questions to reserve 2.5 minutes for multi-step problems.",
                    "Perform weekly mixed-topic retention quizzes to prevent decay in strong topics."
                ),
                motivation = "Precision beats volume. Focus on deciding the next move with total clarity.",
                explanationOfNextAction = "Directing focus toward high-weightage topics with low mastery provides your fastest path to rank elevation.",
                isAiGenerated = isGeminiConfigured
            )

            // 1. Philosophy Header: Observe -> Explain -> Guide
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BrandCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MENTOR STRATEGIC BRIEF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCyan
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = (if (activeReport.isAiGenerated) BrandEmerald else BrandAmber).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (activeReport.isAiGenerated) "GEMINI 3.5 AI" else "OFFLINE SYNTHESIZER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeReport.isAiGenerated) BrandEmerald else BrandAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = activeReport.motivation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Personalized Observation (Observe)
            item {
                MentorSectionCard(
                    icon = Icons.Default.Psychology,
                    iconTint = BrandCyan,
                    badgeText = "OBSERVE",
                    title = "Diagnostic Performance Analysis",
                    content = activeReport.personalizedAnalysis
                )
            }

            // 3. SWOT Breakdown: Weakness & Strength Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = BrandRose, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Primary Vector", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandRose)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(activeReport.biggestWeakness, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Key Strength", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandEmerald)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(activeReport.biggestStrength, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 4. Strategic Explanation (Explain)
            item {
                MentorSectionCard(
                    icon = Icons.Default.Lightbulb,
                    iconTint = BrandAmber,
                    badgeText = "EXPLAIN",
                    title = "Why This Next Move Matters",
                    content = activeReport.explanationOfNextAction
                )
            }

            // 5. Tactical Study Strategy (Guide)
            item {
                MentorSectionCard(
                    icon = Icons.Default.School,
                    iconTint = BrandIndigo,
                    badgeText = "GUIDE",
                    title = "Tactical Study Strategy",
                    content = activeReport.studyStrategy
                )
            }

            // 6. Actionable Suggestions Checklist
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tactical Improvement Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        activeReport.improvementSuggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = BrandEmerald,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 7. CTA: Start Recommended Drill Now
            item {
                Button(
                    onClick = onStartRecommendedDrill,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mentor_start_recommended_drill_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Execute Recommended Action Drill Now")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MentorSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    badgeText: String,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = iconTint.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
