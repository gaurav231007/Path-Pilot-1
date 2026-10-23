package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ai.StudyMaterialService
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private data class NativeStudyTask(val id: String, val title: String, val subtitle: String, val minutes: Int)

@Composable
fun PathPilotNativeApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Learn", "Mentor", "College")
    val icons = listOf(Icons.Default.Home, Icons.Default.Code, Icons.Default.Psychology, Icons.Default.MenuBook)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (tab) {
            0 -> HomeTab(viewModel, onMentor = { tab = 2 }, onLearn = { tab = 1 })
            1 -> LearnTab()
            2 -> MentorTab()
            3 -> CollegeTab()
        }
        NavigationBar {
            tabs.forEachIndexed { index, label ->
                NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(icons[index], contentDescription = label) }, label = { Text(label) })
            }
        }
    }
}

@Composable
private fun HomeTab(viewModel: PathPilotViewModel, onMentor: () -> Unit, onLearn: () -> Unit) {
    val user by viewModel.currentUser.collectAsState()
    val name = user?.name?.substringBefore(" ")?.ifBlank { "Builder" } ?: "Builder"
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("path_pilot_native_tracker", Context.MODE_PRIVATE) }
    val tasks = remember {
        listOf(
            NativeStudyTask("college", "College revision", "Review today's lecture notes", 45),
            NativeStudyTask("code", "Developer practice", "Build one small feature", 60),
            NativeStudyTask("security", "Security lab", "Networking or web-security lab", 45),
            NativeStudyTask("recall", "Active recall", "Review yesterday's mistakes", 20)
        )
    }
    var refresh by remember { mutableStateOf(0) }
    @Suppress("UNUSED_VARIABLE") val token = refresh
    val completed = tasks.count { prefs.getBoolean(it.id, false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Hey $name 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Your personal engineering mentor", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = BrandIndigo.copy(alpha = .10f))) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = BrandIndigo.copy(alpha = .15f)) { Icon(Icons.Default.AutoAwesome, null, tint = BrandIndigo, modifier = Modifier.padding(10.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("TODAY'S MISSION", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BrandIndigo)
                            Text("Build your first REST API", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("HTTP → route → validation → database → JSON response", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onLearn, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Continue learning", fontWeight = FontWeight.Bold) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("4", "Tracks", Icons.Default.TrackChanges, BrandCyan, Modifier.weight(1f))
                MetricCard("$completed", "Done today", Icons.Default.CheckCircle, BrandEmerald, Modifier.weight(1f))
                MetricCard("0", "Projects", Icons.Default.Code, BrandRose, Modifier.weight(1f))
            }
        }
        item { Text("Your roadmap", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
        item { RoadmapCard() }
        item {
            Card(Modifier.fillMaxWidth().clickable(onClick = onMentor), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = BrandIndigo, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Ask your AI Mentor", fontWeight = FontWeight.Bold); Text("Explain, debug, review or plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text("→", fontSize = 24.sp, color = BrandIndigo)
                }
            }
        }
        item {
            Text("Today's plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            tasks.forEach { task ->
                val done = prefs.getBoolean(task.id, false)
                Row(Modifier.fillMaxWidth().clickable { prefs.edit().putBoolean(task.id, !done).apply(); refresh++ }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (done) Icons.Default.CheckCircle else Icons.Default.CalendarMonth, null, tint = if (done) BrandEmerald else MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.Bold); Text(task.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text("${task.minutes}m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LearnTab() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Learning paths", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("Learn by building, not by collecting courses.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { LearningCard("01", "Programming Core", "C → Python → Git → problem solving", Icons.Default.Code, BrandCyan, "Start with variables, functions, files and debugging.") }
        item { LearningCard("02", "Backend Engineering", "HTTP → REST → FastAPI → SQL → PostgreSQL", Icons.Default.Storage, BrandEmerald, "Build APIs, connect a database, add auth and tests.") }
        item { LearningCard("03", "Professional Workflow", "Requirements → branch → code → tests → PR → CI/CD", Icons.Default.Smartphone, BrandIndigo, "Practice the workflow used by real engineering teams.") }
        item { LearningCard("04", "Cybersecurity", "Linux → networking → web security → reporting", Icons.Default.Security, BrandRose, "Only practice against systems you own or are explicitly authorized to test.") }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(16.dp)) { Text("Learning loop", fontWeight = FontWeight.Black); Spacer(Modifier.height(6.dp)); Text("Concept → tiny exercise → real feature → bug → debugging → review → repeat.", color = MaterialTheme.colorScheme.onPrimaryContainer) } } }
    }
}

@Composable
private fun MentorTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("Tell me what you are building or where you are stuck. I'll guide you step by step.") }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("AI Engineering Mentor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Teacher • Senior Developer • Debugging Coach • Security Mentor", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        Card(Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { LazyColumn(Modifier.fillMaxSize().padding(16.dp)) { item { Text(answer, style = MaterialTheme.typography.bodyLarge) }; if (loading) item { Spacer(Modifier.height(14.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) } } }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = question, onValueChange = { question = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. Explain REST API like a senior developer") }, maxLines = 4, trailingIcon = {
            androidx.compose.material3.IconButton(onClick = {
                val q = question.trim()
                if (q.isEmpty() || loading) return@IconButton
                loading = true
                scope.launch {
                    answer = GeminiMentorService.askGeminiMentor(context, q, "The learner is studying software engineering, backend development, professional development workflow, cybersecurity and college subjects. Teach with practical examples, ask useful follow-up questions, and never claim unauthorized security testing is acceptable.")
                    loading = false
                    question = ""
                }
            }) { Icon(Icons.Default.Send, "Send") }
        })
    }
}

@Composable
private fun CollegeTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val subjects = listOf("Engineering Mathematics", "Thermodynamics", "Engineering Chemistry", "Programming / C", "Mechanical Core")
    var selectedFiles by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    var fileNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var analysis by remember { mutableStateOf("Upload your syllabus, PYQs, assignments or teacher notes. Path-Pilot will analyze them and build an evidence-based priority list.") }
    var analyzing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedFiles = uris.take(4)
        fileNames = selectedFiles.map { uri ->
            var name = uri.lastPathSegment ?: "Study material"
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor -> if (cursor.moveToFirst()) name = cursor.getString(0) }
            name
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("College command center", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Syllabus → PYQs → practice → revision", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandIndigo.copy(alpha = .09f))) {
                Column(Modifier.padding(16.dp)) {
                    Text("Add study material", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(6.dp))
                    Text("Select up to 4 PDF, image or text files at once. Use this for syllabus, PYQs, assignments and teacher material.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { picker.launch(arrayOf("application/pdf", "image/*", "text/*")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("+ Select files") }
                }
            }
        }
        if (fileNames.isNotEmpty()) {
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Selected material", fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        fileNames.forEach { name -> Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.MenuBook, null, tint = BrandIndigo, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(name, fontSize = 12.sp) } }
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = {
                            if (selectedFiles.isEmpty() || analyzing) return@Button
                            analyzing = true
                            scope.launch {
                                analysis = StudyMaterialService.analyze(context, selectedFiles)
                                analyzing = false
                            }
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text(if (analyzing) "Analyzing with AI…" else "Analyze syllabus + PYQs") }
                        if (analyzing) { Spacer(Modifier.height(10.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Exam intelligence", fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(7.dp))
                    Text(analysis, fontSize = 12.sp)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text("How priority is decided", fontWeight = FontWeight.Black); Spacer(Modifier.height(6.dp)); Text("PYQ repetition + syllabus weight + assignment overlap + recent patterns + your mistakes. The app will never promise an exact exam question.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
        item { Text("Subjects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
        items(subjects) { subject ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = BrandIndigo.copy(alpha = .12f)) { Icon(Icons.Default.MenuBook, null, tint = BrandIndigo, modifier = Modifier.padding(9.dp)) }
                Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(subject, fontWeight = FontWeight.Bold); Text("Syllabus + PYQ workspace", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun RoadmapCard() {
    Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        RoadmapItem("01", "Programming Core", "C → Python → problem solving", BrandCyan)
        RoadmapItem("02", "Backend Engineering", "HTTP → FastAPI → SQL → PostgreSQL", BrandEmerald)
        RoadmapItem("03", "Professional Workflow", "Git → tests → PR → CI/CD → deploy", BrandIndigo)
        RoadmapItem("04", "Cybersecurity", "Linux → networking → authorized web labs", BrandRose)
    } }
}

@Composable
private fun RoadmapItem(n: String, title: String, subtitle: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = tint.copy(alpha = .14f)) { Text(n, Modifier.padding(horizontal = 9.dp, vertical = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = tint) }
        Spacer(Modifier.width(11.dp)); Column { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun LearningCard(n: String, title: String, path: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, detail: String) {
    Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, color = tint.copy(alpha = .13f)) { Icon(icon, null, tint = tint, modifier = Modifier.padding(10.dp)) }; Spacer(Modifier.width(10.dp)); Column { Text("$n  $title", fontWeight = FontWeight.Black); Text(path, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        Spacer(Modifier.height(9.dp)); Text(detail, fontSize = 12.sp)
    } }
}

@Composable
private fun MetricCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)); Text(value, fontWeight = FontWeight.Black); Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
