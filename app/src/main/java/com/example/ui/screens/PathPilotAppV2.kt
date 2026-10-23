package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private data class MentorLesson(
    val id: String,
    val title: String,
    val subtitle: String,
    val detail: String,
    val steps: List<String>,
    val tint: Color
)

@Composable
fun PathPilotAppV2(viewModel: PathPilotViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    var tab by remember { mutableStateOf(0) }
    var lesson by remember { mutableStateOf<MentorLesson?>(null) }

    BackHandler {
        when {
            lesson != null -> lesson = null
            tab != 0 -> tab = 0
            else -> activity?.finish()
        }
    }

    val lessons = remember {
        listOf(
            MentorLesson("api", "How an API request works", "HTTP → route → validation → response", "Understand what really happens when a mobile app calls a backend.", listOf("Client creates an HTTP request", "Server receives the method and path", "Route validates input and runs business logic", "Database is queried or updated", "Server returns a JSON response", "Client renders the result"), BrandIndigo),
            MentorLesson("python", "Programming Core", "Variables → functions → files → debugging", "Build programming fundamentals that transfer across languages.", listOf("Write small functions", "Practice collections and control flow", "Read and write files", "Use breakpoints and logs", "Refactor repeated code", "Solve one small problem daily"), BrandCyan),
            MentorLesson("backend", "Backend Engineering", "FastAPI → SQL → PostgreSQL → auth", "Move from knowing syntax to shipping a working backend.", listOf("Learn HTTP methods and status codes", "Build a FastAPI route", "Validate request data", "Store data in PostgreSQL", "Add authentication", "Write tests and deploy"), BrandEmerald),
            MentorLesson("workflow", "Professional Workflow", "Ticket → branch → PR → CI/CD", "Practice the same loop used by real engineering teams.", listOf("Understand the requirement", "Create a small branch", "Implement the smallest safe change", "Run tests locally", "Open a pull request", "Fix review comments and deploy"), BrandIndigo),
            MentorLesson("security", "Cybersecurity Workflow", "Scope → recon → test → report", "Learn security through authorized labs and systems you own or have permission to test.", listOf("Define scope and authorization", "Map the attack surface", "Build a threat model", "Test safely in a lab", "Capture evidence and rate risk", "Remediate and retest"), BrandRose)
        )
    }

    Scaffold(
        bottomBar = {
            if (lesson == null) {
                NavigationBar {
                    listOf("Home", "Learn", "Mentor", "College").forEachIndexed { index, label ->
                        val icon = listOf(Icons.Default.Home, Icons.Default.Book, Icons.Default.Psychology, Icons.Default.MenuBook)[index]
                        NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(icon, label) }, label = { Text(label) })
                    }
                }
            }
        }
    ) { padding ->
        if (lesson != null) {
            LessonScreen(lesson!!, Modifier.padding(padding), onBack = { lesson = null })
        } else {
            when (tab) {
                0 -> HomeScreen(Modifier.padding(padding), lessons.first(), onOpen = { lesson = it }, onMentor = { tab = 2 })
                1 -> LearnScreen(Modifier.padding(padding), lessons, onOpen = { lesson = it })
                2 -> MentorScreen(Modifier.padding(padding))
                else -> CollegeScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, mission: MentorLesson, onOpen: (MentorLesson) -> Unit, onMentor: () -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Path-Pilot", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("Your personal engineering mentor", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = BrandIndigo.copy(alpha = .10f))) {
                Column(Modifier.padding(18.dp)) {
                    Text("TODAY'S LESSON", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BrandIndigo)
                    Spacer(Modifier.height(5.dp)); Text(mission.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(5.dp)); Text(mission.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp)); Button(onClick = { onOpen(mission) }, modifier = Modifier.fillMaxWidth()) { Text("Open lesson") }
                }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { StatCard("4", "Tracks", Icons.Default.Code, BrandCyan, Modifier.weight(1f)); StatCard("0", "Projects", Icons.Default.CheckCircle, BrandEmerald, Modifier.weight(1f)); StatCard("AI", "Mentor", Icons.Default.Psychology, BrandRose, Modifier.weight(1f)) } }
        item { Text("Learning loop", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
        item { Card(shape = RoundedCornerShape(18.dp)) { Text("Concept → tiny exercise → real feature → bug → debugging → review → repeat.", Modifier.padding(16.dp)) } }
        item {
            Card(Modifier.fillMaxWidth().clickable { onMentor() }, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = BrandIndigo, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(12.dp));
                    Column(Modifier.weight(1f)) { Text("Ask AI Mentor", fontWeight = FontWeight.Bold); Text("Explain, debug, review or plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text("→", fontSize = 24.sp, color = BrandIndigo)
                }
            }
        }
    }
}

@Composable
private fun LearnScreen(modifier: Modifier, lessons: List<MentorLesson>, onOpen: (MentorLesson) -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Learning paths", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("Every path opens into a real lesson.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(lessons.drop(1)) { lesson ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(lesson) }, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = lesson.tint.copy(alpha = .13f)) { Icon(Icons.Default.Code, null, tint = lesson.tint, modifier = Modifier.padding(11.dp)) }
                    Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(lesson.title, fontWeight = FontWeight.Black); Text(lesson.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text("Open", color = lesson.tint, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LessonScreen(lesson: MentorLesson, modifier: Modifier, onBack: () -> Unit) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Text("Lesson", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text(lesson.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text(lesson.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = lesson.tint.copy(alpha = .09f))) { Text(lesson.detail, Modifier.padding(16.dp)) } }
            item { Text("Step by step", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
            items(lesson.steps) { step ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                    Surface(shape = CircleShape, color = lesson.tint.copy(alpha = .14f)) { Icon(Icons.Default.CheckCircle, null, tint = lesson.tint, modifier = Modifier.padding(7.dp).size(18.dp)) }
                    Spacer(Modifier.width(10.dp)); Text(step, Modifier.weight(1f))
                }
            }
            item { Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to learning") } }
        }
    }
}

@Composable
private fun MentorScreen(modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("Ask me anything about coding, backend, cybersecurity labs, Git/GitHub or your college subjects.") }
    var loading by remember { mutableStateOf(false) }
    val configured = remember { GeminiMentorService.isConfigured(context) }
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("AI Engineering Mentor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(if (configured) "Gemini connected • ready" else "Gemini key not found in this build", fontSize = 12.sp, color = if (configured) BrandEmerald else BrandRose)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { LazyColumn(Modifier.fillMaxSize().padding(16.dp)) { item { Text(answer, style = MaterialTheme.typography.bodyLarge) }; if (loading) item { Spacer(Modifier.height(12.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) } } }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = question, onValueChange = { question = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Ask: Why do we use REST APIs?") }, maxLines = 4, trailingIcon = {
            IconButton(enabled = !loading, onClick = {
                val q = question.trim(); if (q.isEmpty()) return@IconButton
                loading = true
                scope.launch {
                    answer = GeminiMentorService.askGeminiMentor(context, q, "Software engineering, backend development, professional workflow, authorized cybersecurity and college study")
                    loading = false; question = ""
                }
            }) { Icon(Icons.Default.Send, "Send") }
        })
    }
}

@Composable
private fun CollegeScreen(modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFiles by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    var names by remember { mutableStateOf<List<String>>(emptyList()) }
    var result by remember { mutableStateOf("Upload your syllabus, PYQs, assignments or teacher notes. The AI will rank evidence-backed priorities.") }
    var loading by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        selectedFiles = uris.take(4)
        names = selectedFiles.map { uri ->
            var n = uri.lastPathSegment ?: "Study material"
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) n = c.getString(0) }
            n
        }
    }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("College command center", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("Syllabus → PYQs → practice → revision", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Button(onClick = { picker.launch(arrayOf("application/pdf", "image/*", "text/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Select syllabus / PYQ / notes") } }
        if (names.isNotEmpty()) item { Card { Column(Modifier.padding(16.dp)) { Text("Selected files", fontWeight = FontWeight.Black); names.forEach { Text("• $it", Modifier.padding(top = 5.dp), fontSize = 12.sp) }; Spacer(Modifier.height(10.dp)); Button(onClick = { if (!loading) { loading = true; scope.launch { result = StudyMaterialService.analyze(context, selectedFiles); loading = false } } }, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "Analyzing…" else "Analyze with Gemini") }; if (loading) { Spacer(Modifier.height(10.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) } } } }
        item { Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text("Exam intelligence", fontWeight = FontWeight.Black); Spacer(Modifier.height(7.dp)); Text(result, fontSize = 13.sp) } } }
        item { Text("Priority method", fontWeight = FontWeight.Black); Text("PYQ repetition + syllabus weight + assignment overlap + recent patterns + your mistakes. No guaranteed-question claims.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)); Text(value, fontWeight = FontWeight.Black); Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
