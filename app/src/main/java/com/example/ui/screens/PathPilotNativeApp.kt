package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ai.StudyMaterialService
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private data class TrainingLesson(
    val id: String,
    val track: String,
    val title: String,
    val level: String,
    val concept: String,
    val lab: String,
    val build: String,
    val debug: String,
    val review: String
)

private val trainingPlan = listOf(
    TrainingLesson("c01","Programming","Programming Foundations","Beginner","Variables, conditions, loops, functions, files and how to read errors.","Write a small input validator. Submit your code or explanation to the mentor for review.","Turn it into a reusable CLI utility with clean functions.","Break one condition intentionally and trace the error from message to root cause.","Explain the bug, fix and lesson in your own words."),
    TrainingLesson("c02","C","C: Memory and Systems Thinking","Beginner","Pointers, arrays, structs, stack/heap basics and why memory bugs happen.","Implement a dynamic array and explain each pointer operation.","Build a small contact manager using structs and file I/O.","Diagnose a deliberately introduced out-of-bounds bug.","Explain ownership and lifetime without memorizing definitions."),
    TrainingLesson("py01","Python","Python for Real Work","Beginner","Modules, collections, exceptions, files, virtual environments and clean functions.","Process a CSV/text file and validate bad rows.","Build a command-line expense/task analyzer.","Handle missing files, malformed data and unexpected types.","Refactor duplicated logic and write three tests."),
    TrainingLesson("py02","Python","Python Testing + Automation","Intermediate","pytest-style thinking, fixtures, mocking concepts and automation.","Write tests for a function before changing its implementation.","Build a small automation script with logs and configuration.","Use a failing test to locate a regression.","Write what should be tested before coding."),
    TrainingLesson("js01","Web","JavaScript + TypeScript Foundations","Beginner","Browser/runtime basics, async code, modules, types and APIs.","Call a JSON API and handle loading/error/success states.","Build a small typed client for a backend service.","Debug an async race or bad response shape.","Explain the data flow from UI to network response."),
    TrainingLesson("b01","Backend","How HTTP Actually Works","Beginner","Requests, responses, methods, headers, status codes, JSON and cookies.","Inspect a GET and POST request and describe every important field.","Build a tiny API client and handle 2xx/4xx/5xx responses.","Debug a 400, 404 and 500 response using evidence.","Draw the request lifecycle from client to server and back."),
    TrainingLesson("b02","Backend","Build a REST API with FastAPI","Intermediate","Routing, validation, schemas, errors and API design.","Create GET/POST/PUT/DELETE endpoints for tasks.","Build a real notes/tasks API with validation.","Trace a validation and route-not-found failure.","Write endpoint tests and explain your API contract."),
    TrainingLesson("b03","Backend","SQL + PostgreSQL","Intermediate","Tables, CRUD, joins, constraints, indexes and transactions.","Write queries for users, tasks and projects.","Connect your API to PostgreSQL/Supabase.","Diagnose a bad query and constraint violation.","Explain why your schema prevents inconsistent data."),
    TrainingLesson("b04","Backend","Authentication + Secure APIs","Intermediate","Authentication vs authorization, sessions/tokens, roles and safe secrets.","Design public/protected routes and a role matrix.","Add authentication and authorization to the API.","Debug an authentication failure without logging secrets.","Document trust boundaries and security assumptions."),
    TrainingLesson("b05","Backend","Testing + Production Debugging","Intermediate","Unit/integration tests, logs, observability and reproducible bugs.","Turn a bug report into a minimal reproduction.","Create a test suite and structured error handling.","Find the root cause from logs and failing tests.","Write a short incident/postmortem note."),
    TrainingLesson("w01","Professional","Git Like a Professional","Beginner","Requirements, issues, branches, commits, pull requests and reviews.","Turn a feature request into acceptance criteria and a branch plan.","Implement the feature with focused commits and tests.","Resolve a failing test or merge conflict safely.","Write a PR description a teammate could review."),
    TrainingLesson("w02","Professional","CI/CD + Deployment","Intermediate","Build pipelines, tests, artifacts, staging, production and rollback.","Read a CI failure and identify the first useful evidence.","Deploy a small service with automated checks.","Debug a failed build/deployment from logs.","Write a deployment checklist and rollback plan."),
    TrainingLesson("ai01","AI Development","LLM API Fundamentals","Beginner","Models, tokens, prompts, structured output, errors, latency and cost.","Call an LLM API and parse a structured response.","Build a small AI feature inside a normal application.","Handle timeout, invalid output and API errors.","Explain where deterministic code should remain in control."),
    TrainingLesson("ai02","AI Development","RAG + Tool Calling","Intermediate","Embeddings, retrieval, grounding, tools and permission boundaries.","Design a tiny retrieval flow and identify what must be cited.","Build a document Q&A or tool-using assistant.","Debug retrieval misses and malformed tool arguments.","Evaluate hallucination, relevance and tool safety."),
    TrainingLesson("ai03","AI Development","AI Agents + Evaluation","Advanced","Planning, tools, state, guardrails, eval sets and failure analysis.","Design an agent state machine before writing code.","Build a bounded agent that can use approved tools.","Create failing evaluation cases and fix one failure at a time.","Measure quality instead of trusting a few good demos."),
    TrainingLesson("s01","Cybersecurity","Linux + Networking Foundations","Beginner","Processes, permissions, ports, DNS, TCP/IP, HTTP and logs.","Inspect a local service and explain its ports and requests.","Run and observe a local service in your own lab.","Diagnose a permission, port or DNS problem safely.","Keep a lab notebook with commands and evidence."),
    TrainingLesson("s02","Cybersecurity","Web Security Fundamentals","Intermediate","Threat modeling, input handling, auth, sessions and common web weaknesses.","Threat-model a toy application and identify trust boundaries.","Test an intentionally vulnerable local lab only.","Document one finding with evidence, impact and remediation.","Retest after the fix and compare before/after evidence."),
    TrainingLesson("s03","Cybersecurity","Security Testing Workflow","Intermediate","Scope, authorization, recon, testing, evidence, severity, remediation and retest.","Create a written scope for a local/authorized lab before testing.","Run a small authorized lab assessment and produce a report.","Separate observations from assumptions and avoid unsafe claims.","Write a professional finding a developer can act on."),
    TrainingLesson("s04","Cybersecurity","Secure Coding + Code Review","Advanced","Input validation, secrets, least privilege, dependency risk and secure review.","Review a deliberately insecure sample and identify risks.","Harden one of your own API features.","Prove the fix with a test or before/after evidence.","Write remediation that is specific enough to implement."),
    TrainingLesson("p01","Capstone","Build a Production-Style AI Product","Advanced","Requirements, architecture, backend, database, AI, testing, Git, deployment and security.","Write a one-page spec, acceptance criteria and architecture before coding.","Build an end-to-end AI product in small milestones.","Handle a real bug, failed test and security finding.","Ship a release, review it like a senior engineer and write a postmortem.")
)

@Composable
fun PathPilotNativeApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var lessonId by remember { mutableStateOf<String?>(null) }
    val lesson = trainingPlan.firstOrNull { it.id == lessonId }
    BackHandler(enabled = lesson != null || tab != 0) { if (lesson != null) lessonId = null else tab = 0 }
    if (lesson != null) LessonScreen(lesson) { lessonId = null }
    else MainShell(tab, { tab = it }, { lessonId = it })
}

@Composable
private fun MainShell(tab: Int, onTab: (Int) -> Unit, onLesson: (String) -> Unit) {
    val labels = listOf("Home", "Academy", "Mentor", "College")
    val icons = listOf(Icons.Default.Home, Icons.Default.Code, Icons.Default.Psychology, Icons.Default.MenuBook)
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                0 -> HomeTab(onLesson, { onTab(1) }, { onTab(2) })
                1 -> AcademyTab(onLesson)
                2 -> MentorTab()
                else -> CollegeTab()
            }
        }
        NavigationBar { labels.forEachIndexed { i, label -> NavigationBarItem(tab == i, { onTab(i) }, icon = { Icon(icons[i], label) }, label = { Text(label) }) } }
    }
}

@Composable
private fun HomeTab(onLesson: (String) -> Unit, onAcademy: () -> Unit, onMentor: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("path_pilot_progress", Context.MODE_PRIVATE) }
    var refresh by remember { mutableIntStateOf(0) }
    val completed = trainingPlan.count { prefs.getBoolean(it.id, false) }
    val next = trainingPlan.firstOrNull { !prefs.getBoolean(it.id, false) }
    val pct = completed.toFloat() / trainingPlan.size
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Path-Pilot", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black); Text("Your hands-on engineering school", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(18.dp)) { Text("TODAY'S MISSION", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BrandIndigo); Text(next?.title ?: "Choose your capstone", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Text(next?.lab ?: "Keep building and review your previous mistakes.", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); Button(enabled = next != null, onClick = { next?.let { onLesson(it.id) } }, Modifier.fillMaxWidth()) { Text("Enter lab") } } } }
        item { Text("Academy progress  •  $completed / ${trainingPlan.size}", fontWeight = FontWeight.Bold); LinearProgressIndicator({ pct }, Modifier.fillMaxWidth()) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = onAcademy, Modifier.weight(1f)) { Text("Academy") }; OutlinedButton(onClick = onMentor, Modifier.weight(1f)) { Text("AI Mentor") } } }
        item { Card { Column(Modifier.padding(16.dp)) { Text("The rule", fontWeight = FontWeight.Black); Text("AI will guide you, but you must do the lab. Every skill ends with something you build, break, test or explain.") } } }
        item { Text("Tracks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
        item { listOf("Programming & Languages", "Backend & Professional Engineering", "AI Development", "Cybersecurity", "College Study").forEach { Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = BrandEmerald); Spacer(Modifier.width(10.dp)); Text(it) } } }
    }
}

@Composable
private fun AcademyTab(onLesson: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Engineering Academy", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("Concept → lab → build → debug → review → next skill", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(trainingPlan) { lesson -> Card(Modifier.fillMaxWidth().clickable { onLesson(lesson.id) }, shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text("${lesson.track.uppercase()}  •  ${lesson.level}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = BrandIndigo); Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black); Text(lesson.concept, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(7.dp)); Text("Open lab →", fontSize = 12.sp, fontWeight = FontWeight.Bold) } } }
    }
}

@Composable
private fun LessonScreen(lesson: TrainingLesson, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("path_pilot_progress", Context.MODE_PRIVATE) }
    var done by remember { mutableStateOf(prefs.getBoolean(lesson.id, false)) }
    var submission by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var grading by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Column { Text("${lesson.track.uppercase()} • ${lesson.level}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = BrandIndigo); Text(lesson.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) } } }
        item { LessonSection("1 · Learn", lesson.concept, Icons.Default.Lightbulb) }
        item { LessonSection("2 · LAB — you do this", lesson.lab, Icons.Default.Science) }
        item { LessonSection("3 · BUILD", lesson.build, Icons.Default.Build) }
        item { LessonSection("4 · DEBUG", lesson.debug, Icons.Default.BugReport) }
        item { LessonSection("5 · REVIEW", lesson.review, Icons.Default.CheckCircle) }
        item { Card { Column(Modifier.padding(16.dp)) { Text("Submit your work", fontWeight = FontWeight.Black); Text("Paste code, your approach, output, or explain what you tried. The mentor will review it instead of doing it for you.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); OutlinedTextField(submission, { submission = it }, Modifier.fillMaxWidth(), minLines = 5, label = { Text("Your solution / reasoning") }); Spacer(Modifier.height(8.dp)); Button(enabled = submission.isNotBlank() && !grading, onClick = { val work = submission.trim(); grading = true; scope.launch { feedback = GeminiMentorService.askGeminiMentor(context, "You are Path-Pilot's strict but supportive senior mentor. Review this learner submission for the lab '${lesson.title}'. Do not simply solve it. First identify what is correct, then give one or two precise hints for mistakes, ask a follow-up question if reasoning is weak, and finish with the next action. Encourage the learner to fix it themselves. Submission:\n$work"); grading = false } }, Modifier.fillMaxWidth()) { Text(if (grading) "Reviewing…" else "Get mentor review") }; if (grading) LinearProgressIndicator(Modifier.fillMaxWidth()); if (feedback.isNotBlank()) { Spacer(Modifier.height(10.dp)); HorizontalDivider(); Spacer(Modifier.height(10.dp)); Text("Mentor review", fontWeight = FontWeight.Black); Text(feedback, fontSize = 13.sp) } } } }
        item { Button(onClick = { prefs.edit().putBoolean(lesson.id, !done).apply(); done = !done }, Modifier.fillMaxWidth()) { Text(if (done) "Completed ✓ — mark incomplete" else "I completed this lab") } }
    }
}

@Composable private fun LessonSection(title: String, text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Card { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) { Icon(icon, null, tint = BrandIndigo, Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Column { Text(title, fontWeight = FontWeight.Black); Spacer(Modifier.height(5.dp)); Text(text) } } } }

@Composable
private fun MentorTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var q by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("Ask me to explain a concept, review your code, design a project, debug an error, or plan your next skill. I will guide you step-by-step.") }
    var loading by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) { Text("AI Engineering Mentor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("Teacher • Senior Developer • AI Engineer • Security Mentor • Study Coach", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); Card(Modifier.weight(1f).fillMaxWidth()) { LazyColumn(Modifier.fillMaxSize().padding(16.dp)) { item { Text(answer) } } }; Spacer(Modifier.height(10.dp)); OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("What are you stuck on?") }, minLines = 3); Spacer(Modifier.height(8.dp)); Button(enabled = q.isNotBlank() && !loading, onClick = { val prompt = q.trim(); loading = true; scope.launch { answer = GeminiMentorService.askGeminiMentor(context, prompt, "You are Path-Pilot, a long-term engineering mentor. Teach programming, C, Python, JavaScript/TypeScript, backend, databases, Git, CI/CD, AI development and ethical cybersecurity in authorized labs, plus college study. Never encourage unauthorized access. Prefer guided questions, small exercises, debugging and real projects. Do not do the learner's lab immediately; make them think, then reveal hints progressively."); loading = false; q = "" } }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Send, null); Spacer(Modifier.width(7.dp)); Text(if (loading) "Thinking…" else "Ask Mentor") }; if (loading) LinearProgressIndicator(Modifier.fillMaxWidth()) }
}

@Composable
private fun CollegeTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var files by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    var names by remember { mutableStateOf<List<String>>(emptyList()) }
    var result by remember { mutableStateOf("Upload syllabus, PYQs, assignments or teacher material. Path-Pilot will rank study priorities from evidence.") }
    var loading by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris -> files = uris.take(4); names = files.map { it.lastPathSegment ?: "Study material" } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("College command center", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("Syllabus → topics → important questions → PYQs → revision", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Card { Column(Modifier.padding(16.dp)) { Text("Study material", fontWeight = FontWeight.Black); Text("Upload up to 4 PDF/image/text files.", fontSize = 12.sp); Spacer(Modifier.height(10.dp)); Button(onClick = { picker.launch(arrayOf("application/pdf", "image/*", "text/*")) }, Modifier.fillMaxWidth()) { Text("Select syllabus / PYQs / assignments") } } } }
        if (names.isNotEmpty()) item { Card { Column(Modifier.padding(16.dp)) { Text("Selected files", fontWeight = FontWeight.Black); names.forEach { Text("• $it", fontSize = 12.sp) }; Spacer(Modifier.height(10.dp)); Button(enabled = !loading, onClick = { loading = true; scope.launch { result = StudyMaterialService.analyze(context, files); loading = false } }, Modifier.fillMaxWidth()) { Text(if (loading) "Analyzing…" else "Analyze with Gemini") }; if (loading) LinearProgressIndicator(Modifier.fillMaxWidth()) } } }
        item { Card { Column(Modifier.padding(16.dp)) { Text("Exam intelligence", fontWeight = FontWeight.Black); Spacer(Modifier.height(6.dp)); Text(result, fontSize = 12.sp) } } }
        item { Text("Priority method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Text("PYQ repetition + syllabus weight + assignment overlap + recent patterns + your mistakes. It ranks probability; it never promises an exact exam question.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) }
    }
}
