package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.QuestionEntity
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.viewmodel.PathPilotViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: PathPilotViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Add New, 2: Bulk CSV, 3: Audit Logs
    val questions by viewModel.allQuestions.collectAsState()
    val totalAttempts by viewModel.totalAttemptsCount.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val notification by viewModel.adminNotification.collectAsState()

    var questionToDelete by remember { mutableStateOf<QuestionEntity?>(null) }
    var questionToEdit by remember { mutableStateOf<QuestionEntity?>(null) }

    if (questionToDelete != null) {
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("Delete Question?") },
            text = { Text("Are you sure you want to permanently delete question #${questionToDelete?.id}? This action is irreversible and will be logged in the audit ledger.") },
            confirmButton = {
                Button(
                    onClick = {
                        questionToDelete?.let { viewModel.deleteQuestion(it.id) }
                        questionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRose)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (questionToEdit != null) {
        EditQuestionDialog(
            question = questionToEdit!!,
            onDismiss = { questionToEdit = null },
            onSave = { updated ->
                viewModel.updateQuestion(updated)
                questionToEdit = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("admin_panel_screen")
    ) {
        // Admin Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Admin")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = BrandIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Path Pilot Admin Control",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Syllabus, Question Bank & Calibration Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandIndigo.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "SUPER ADMIN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Global Notification Banner
                AnimatedVisibility(visible = notification != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandEmerald.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = notification ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandEmerald,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // KPI overview cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiItem(
                        label = "Total Questions",
                        value = "${questions.size}",
                        color = BrandCyan,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiItem(
                        label = "Total Attempts",
                        value = "$totalAttempts",
                        color = BrandAmber,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiItem(
                        label = "Audit Events",
                        value = "${auditLogs.size}",
                        color = BrandIndigo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Questions", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Add New", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Bulk CSV", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Audit Logs", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Supabase", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> QuestionsListTab(
                    questions = questions,
                    onEdit = { questionToEdit = it },
                    onDelete = { questionToDelete = it }
                )
                1 -> AddQuestionTab(
                    onAdd = { newQ -> viewModel.addQuestion(newQ) }
                )
                2 -> BulkCsvTab(
                    onImport = { csv -> viewModel.bulkImportCsv(csv) }
                )
                3 -> AuditLogsTab(logs = auditLogs)
                4 -> SupabaseDeployTab(viewModel = viewModel)
            }
        }

    }
}

@Composable
fun AdminKpiItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuestionsListTab(
    questions: List<QuestionEntity>,
    onEdit: (QuestionEntity) -> Unit,
    onDelete: (QuestionEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterDifficulty by remember { mutableStateOf("ALL") }

    val filtered = questions.filter { q ->
        val matchesSearch = q.question.contains(searchQuery, ignoreCase = true) || q.explanation.contains(searchQuery, ignoreCase = true)
        val matchesDiff = filterDifficulty == "ALL" || q.difficulty.equals(filterDifficulty, ignoreCase = true)
        matchesSearch && matchesDiff
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search question text or concepts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "EASY", "MEDIUM", "HARD").forEach { diff ->
                    FilterChip(
                        selected = filterDifficulty == diff,
                        onClick = { filterDifficulty = diff },
                        label = { Text(diff, fontSize = 11.sp) }
                    )
                }
            }
        }

        items(filtered) { q ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${q.id}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Tagged: ${q.difficulty} • Recalc: ${q.calculatedDifficulty}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onEdit(q) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandCyan, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { onDelete(q) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BrandRose, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Correct Answer: ${q.correctAnswer}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandEmerald
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Attempts: ${q.totalAttempts}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Correct: ${q.correctAttempts}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Avg Time: ${q.averageTimeSec}s",
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
fun AddQuestionTab(
    onAdd: (QuestionEntity) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }
    var difficulty by remember { mutableStateOf("MEDIUM") }
    var explanation by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Create Single Question", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Question Text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("Option A") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("Option B") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("Option C") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("Option D") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Correct Answer", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("A", "B", "C", "D").forEach { opt ->
                            FilterChip(
                                selected = correctAnswer == opt,
                                onClick = { correctAnswer = opt },
                                label = { Text(opt) }
                            )
                        }
                    }
                }

                Column {
                    Text("Difficulty", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff },
                                label = { Text(diff, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Conceptual Explanation / Solution") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Button(
                onClick = {
                    if (questionText.isNotBlank()) {
                        val newQ = QuestionEntity(
                            examId = 1,
                            subjectId = 1,
                            chapterId = 1,
                            difficulty = difficulty,
                            question = questionText.trim(),
                            optionA = optionA.ifBlank { "Option A" },
                            optionB = optionB.ifBlank { "Option B" },
                            optionC = optionC.ifBlank { "Option C" },
                            optionD = optionD.ifBlank { "Option D" },
                            correctAnswer = correctAnswer,
                            explanation = explanation.ifBlank { "Standard syllabus deduction." }
                        )
                        onAdd(newQ)
                        // Reset fields
                        questionText = ""
                        optionA = ""
                        optionB = ""
                        optionC = ""
                        optionD = ""
                        explanation = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Publish to Question Bank")
            }
        }
    }
}

@Composable
fun BulkCsvTab(
    onImport: (String) -> Unit
) {
    var csvText by remember { mutableStateOf("") }

    val sampleCsvTemplate = """
Question|Option A|Option B|Option C|Option D|Correct|Difficulty|Explanation
What is the dimension of Planck's constant h?|[M L^2 T^-1]|[M L T^-1]|[M L^2 T^-2]|[M^2 L T^-1]|A|EASY|E = h * nu => h = [M L^2 T^-2] / [T^-1] = [M L^2 T^-1].
Which orbital has spherical symmetry in hydrogen?|1s|2p|3d|4f|A|EASY|s-orbitals have l=0 and are spherically symmetric in space.
Find dy/dx if y = e^(sin x)|e^(sin x) * cos x|e^(sin x)|cos x|-sin x * e^(sin x)|A|EASY|Chain rule: d(e^u)/dx = e^u * du/dx = e^(sin x) * cos x.
    """.trimIndent()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Bulk Upload Questions (CSV / Pipe-Delimited)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Paste formatted CSV or pipe-separated lines with format:\nQuestion|Option A|Option B|Option C|Option D|Correct|Difficulty|Explanation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedButton(
                onClick = { csvText = sampleCsvTemplate },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Load Sample Question Bank Template")
            }
        }

        item {
            OutlinedTextField(
                value = csvText,
                onValueChange = { csvText = it },
                label = { Text("CSV Data") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Paste CSV or click 'Load Sample' above...") }
            )
        }

        item {
            Button(
                onClick = {
                    if (csvText.isNotBlank()) {
                        onImport(csvText)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
            ) {
                Text("Run Live Validation & Batch Import")
            }
        }
    }
}

@Composable
fun AuditLogsTab(logs: List<com.example.data.entity.AuditLogEntity>) {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("System Audit Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Immutable record of administrative mutations & calibrations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(logs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandIndigo.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = log.action,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = sdf.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = log.details, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun EditQuestionDialog(
    question: QuestionEntity,
    onDismiss: () -> Unit,
    onSave: (QuestionEntity) -> Unit
) {
    var text by remember { mutableStateOf(question.question) }
    var optA by remember { mutableStateOf(question.optionA) }
    var optB by remember { mutableStateOf(question.optionB) }
    var optC by remember { mutableStateOf(question.optionC) }
    var optD by remember { mutableStateOf(question.optionD) }
    var correct by remember { mutableStateOf(question.correctAnswer) }
    var diff by remember { mutableStateOf(question.difficulty) }
    var expl by remember { mutableStateOf(question.explanation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Question #${question.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Question") })
                OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") })
                OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") })
                OutlinedTextField(value = expl, onValueChange = { expl = it }, label = { Text("Explanation") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    question.copy(
                        question = text,
                        optionA = optA,
                        optionB = optB,
                        explanation = expl
                    )
                )
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SupabaseDeployTab(viewModel: PathPilotViewModel) {
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val isConfigured = com.example.data.remote.SupabaseService.isConfigured()
    val supabaseUrl = com.example.data.remote.SupabaseService.getSupabaseUrl()
    val sqlSchema = com.example.data.remote.SupabaseService.getSupabaseSqlSchema()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Supabase Cloud Database & Deployment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Connect PostgreSQL on Supabase to sync Student DNA, Questions, and Attempts across devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Connection Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cloud Connection Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isConfigured) BrandEmerald.copy(alpha = 0.15f) else BrandCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isConfigured) "SUPABASE CONNECTED" else "LOCAL MODE (ROOM DB)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isConfigured) BrandEmerald else BrandCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isConfigured) "Project URL: $supabaseUrl" else "Supabase URL is not configured yet. Operating in high-speed local Room SQLite mode with automatic migration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when (val status = supabaseStatus) {
                        is com.example.data.remote.SupabaseSyncStatus.Syncing -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Testing live connection...", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        is com.example.data.remote.SupabaseSyncStatus.Success -> {
                            Text(status.message, style = MaterialTheme.typography.labelSmall, color = BrandEmerald, fontWeight = FontWeight.SemiBold)
                        }
                        is com.example.data.remote.SupabaseSyncStatus.Error -> {
                            Text(status.error, style = MaterialTheme.typography.labelSmall, color = BrandRose, fontWeight = FontWeight.SemiBold)
                        }
                        is com.example.data.remote.SupabaseSyncStatus.Unconfigured -> {
                            Text("To connect, add SUPABASE_URL and SUPABASE_ANON_KEY to your Secrets panel or .env file.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.testSupabaseConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Connection & Force Sync")
                    }
                }
            }
        }

        // 2. Setup Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How to Connect Your Supabase Project",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val steps = listOf(
                        "1. Create a free project at supabase.com.",
                        "2. In Supabase Dashboard, navigate to Project Settings -> API.",
                        "3. Copy your Project URL (https://xyz.supabase.co) and anon public key.",
                        "4. Add SUPABASE_URL and SUPABASE_ANON_KEY in AI Studio's Secrets panel.",
                        "5. Run the SQL schema below in Supabase's SQL Editor."
                    )

                    steps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 3. PostgreSQL SQL Schema
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Supabase PostgreSQL Schema (One-Click Setup)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Execute this in your Supabase SQL Editor:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sqlSchema,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // 4. Deployment Instructions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Deployment & Distribution Options",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Live Web Stream Preview: Your app is already hosted and accessible via the Shared App URL.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Android APK / AAB Bundle: To release on phones or Google Play, tap the settings/build menu in AI Studio to download the signed release APK or Android App Bundle.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Export Code: Export as a ZIP archive or push directly to GitHub from the AI Studio menu.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

