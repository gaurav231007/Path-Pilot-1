package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private val Blue = Color(0xFF087BFF)
private val Navy = Color(0xFF101A33)
private val Muted = Color(0xFF64748B)
private val Bg = Color(0xFFF5F8FD)
private val Green = Color(0xFF12A36A)

private data class Track(val name: String, val category: String, val outcome: String, val lessons: List<String>)
private data class Project(val name: String, val stack: String, val tasks: List<String>)

private val tracks = listOf(
    Track("Python Engineering", "Programming", "Build useful Python software", listOf("Control flow", "Functions + modules", "Files + exceptions", "Testing", "CLI project")),
    Track("DSA Practice", "DSA", "Solve problems by patterns, not memorization", listOf("Complexity", "Arrays + strings", "Hashing", "Two pointers", "Sliding window", "Stack + queue", "Linked list", "Binary search", "Trees", "Graphs", "Greedy", "Dynamic programming")),
    Track("Web Engineering", "Web", "Build production web interfaces", listOf("HTML5", "Responsive CSS", "JavaScript", "TypeScript", "React", "Next.js", "Node.js")),
    Track("Backend Engineering", "Backend", "Build and deploy APIs", listOf("HTTP", "REST", "FastAPI", "SQL + PostgreSQL", "Auth", "Testing + deployment")),
    Track("Git + GitHub", "Professional", "Work like a real developer", listOf("Tickets", "Branches", "Status + diff", "Staging", "Commits", "Push + PR", "CI + review", "Merge + release")),
    Track("Cybersecurity", "Security", "Assess and secure authorized labs", listOf("Scope", "Linux + networking", "Threat model", "Authorized testing", "Evidence", "Remediation", "Retest")),
    Track("Automation Agency", "Agency", "Build client automation systems", listOf("Offers", "APIs + webhooks", "n8n", "CRM automation", "WhatsApp + Telegram", "AI agents", "Monitoring", "Client delivery")),
    Track("Marketing AI Agency", "Agency", "Build measurable marketing systems", listOf("Positioning", "Offers", "Landing pages", "AI content", "SEO", "Ads", "Analytics", "Reporting")),
    Track("AI Product Engineering", "AI", "Ship AI features safely", listOf("LLM APIs", "Structured output", "Tools", "RAG", "Evaluation", "Guardrails", "Production"))
)

private val projects = listOf(
    Project("Expense Manager", "React + FastAPI + PostgreSQL", listOf("Requirement", "Repository + README", "Database", "REST API", "Frontend", "Tests + CI", "PR + review", "Deploy", "Security review")),
    Project("AI Support Agent", "Next.js + Gemini + tools", listOf("Product brief", "Chat UI", "Model integration", "Structured output", "Tool calling", "Evaluation", "Failure handling", "Deploy")),
    Project("Automation CRM", "n8n + APIs + CRM", listOf("Client brief", "Webhook", "Validation", "CRM write", "Follow-up", "Deduplication", "Monitoring", "Handoff"))
)

private fun completed(c: Context, key: String) = c.getSharedPreferences("pathpilot_v2", Context.MODE_PRIVATE).getBoolean(key, false)
private fun save(c: Context, key: String) = c.getSharedPreferences("pathpilot_v2", Context.MODE_PRIVATE).edit().putBoolean(key, true).apply()

@Composable
fun PathPilotApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableStateOf("home") }
    var track by remember { mutableStateOf<Track?>(null) }
    var lesson by remember { mutableStateOf<String?>(null) }
    var project by remember { mutableStateOf<Project?>(null) }
    var special by remember { mutableStateOf<String?>(null) }

    BackHandler(track != null || project != null || special != null || lesson != null) {
        when {
            lesson != null -> lesson = null
            track != null -> track = null
            project != null -> project = null
            else -> special = null
        }
    }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        when {
            lesson != null && track != null -> LessonScreen(track!!, lesson!!) { lesson = null }
            track != null -> TrackScreen(track!!, { lesson = it }) { track = null }
            project != null -> ProjectScreen(project!!) { project = null }
            special == "mentor" -> MentorScreen { special = null }
            special == "git" -> GitScreen { special = null }
            else -> MainShell(tab, { tab = it }, { track = it }, { project = it }, { special = it })
        }
    }
}

@Composable
private fun MainShell(tab: String, setTab: (String) -> Unit, openTrack: (Track) -> Unit, openProject: (Project) -> Unit, openSpecial: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                "learn" -> LearnScreen(openTrack)
                "build" -> BuildScreen(openProject, openSpecial)
                "college" -> CollegeScreen()
                else -> HomeScreen(openTrack, openProject, openSpecial)
            }
        }
        Row(Modifier.fillMaxWidth().background(Color.White).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            BottomTab("Home", tab == "home") { setTab("home") }
            BottomTab("Learn", tab == "learn") { setTab("learn") }
            BottomTab("Build", tab == "build") { setTab("build") }
            BottomTab("College", tab == "college") { setTab("college") }
        }
    }
}

@Composable private fun BottomTab(text: String, selected: Boolean, click: () -> Unit) {
    Text(text, Modifier.clickable { click() }.padding(horizontal = 14.dp, vertical = 9.dp), color = if (selected) Blue else Muted, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
}

@Composable private fun Brand(title: String = "PathPilot", subtitle: String = "AI Engineering Mentor") {
    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(46.dp), RoundedCornerShape(14.dp), color = Blue) { Box(contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black) } }
        Spacer(Modifier.width(11.dp))
        Column { Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Navy); Text(subtitle, fontSize = 10.sp, color = Muted) }
    }
}

@Composable private fun HomeScreen(openTrack: (Track) -> Unit, openProject: (Project) -> Unit, openSpecial: (String) -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    val dsa = tracks.first { it.category == "DSA" }
    val dsaDone = dsa.lessons.count { completed(c, "track:${dsa.name}:$it") }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Brand() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("Build. Solve. Debug. Ship.", fontSize = 29.sp, fontWeight = FontWeight.Black, color = Navy); Text("A hands-on mentor, not a flashcard dashboard.", color = Muted, fontSize = 13.sp) } }
        item { Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(18.dp)) { Text("TODAY", color = Color(0xFFBBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("DSA practice", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black); Text("Pattern → attempt → hint → code → test → review", color = Color(0xFFD8E0EF), fontSize = 11.sp); Button({ openTrack(dsa) }, Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("Practice DSA  •  $dsaDone/${dsa.lessons.size}") } } } }
        item { Section("Workspaces") }
        item { Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { ActionCard("Git Lab") { openSpecial("git") }; ActionCard("AI Mentor") { openSpecial("mentor") }; ActionCard("Projects") { openProject(projects.first()) } } }
        item { Section("Continue learning") }
        items(tracks.take(6)) { TrackCard(it, openTrack) }
    }
}

@Composable private fun LearnScreen(openTrack: (Track) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Brand("Learn", "Skill paths + DSA practice") }
        item { Text("Choose a track. Every lesson ends with an action or evidence.", Modifier.padding(horizontal = 18.dp), color = Muted, fontSize = 12.sp) }
        items(tracks) { TrackCard(it, openTrack) }
    }
}

@Composable private fun TrackCard(track: Track, open: (Track) -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    val done = track.lessons.count { completed(c, "track:${track.name}:$it") }
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { open(track) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(15.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(track.name, color = Navy, fontWeight = FontWeight.Black, fontSize = 16.sp); Text(track.category, color = Blue, fontSize = 10.sp) }; Text("$done/${track.lessons.size}", color = Blue, fontWeight = FontWeight.Bold) }; Text(track.outcome, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp)); LinearProgressIndicator(progress = { if (track.lessons.isEmpty()) 0f else done.toFloat() / track.lessons.size }, Modifier.fillMaxWidth().padding(top = 8.dp), color = Blue) }
    }
}

@Composable private fun TrackScreen(track: Track, openLesson: (String) -> Unit, back: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header(track.name, "${track.category} • hands-on path", back) }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(18.dp)) { Text("OUTCOME", color = Color(0xFFBBD9FF), fontSize = 10.sp); Text(track.outcome, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black); Text(if (track.category == "DSA") "Learn the pattern, attempt without the answer, use hints only when stuck, then review complexity." else "Understand → do it → submit evidence → review → apply it in a project.", color = Color(0xFFD8E0EF), fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) } } }
        item { Section("Practice sequence") }
        items(track.lessons) { item -> LessonRow(track, item, openLesson) }
    }
}

@Composable private fun LessonRow(track: Track, lesson: String, open: (String) -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    val done = completed(c, "track:${track.name}:$lesson")
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { open(lesson) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(15.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (done) "✓" else "○", color = if (done) Green else Blue, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(lesson, color = Navy, fontWeight = FontWeight.Bold); Text(if (track.category == "DSA") "Solve → test → analyze complexity" else "Learn → implement → verify", color = Muted, fontSize = 10.sp) }; Text("→", color = Muted) }
    }
}

@Composable private fun LessonScreen(track: Track, lesson: String, back: () -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    var evidence by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        Header(lesson, track.name, back)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StepCard("1 • UNDERSTAND", if (track.category == "DSA") "Identify the pattern and constraints before writing code." else "Know what you are building and why it matters.") }
            item { StepCard("2 • DO IT", if (track.category == "DSA") "Attempt the problem yourself. Do not open the solution first." else "Implement the smallest working version.") }
            item { OutlinedTextField(evidence, { evidence = it }, Modifier.fillMaxWidth(), label = { Text(if (track.category == "DSA") "Your approach / complexity / test result" else "Evidence: what did you actually do?") }, minLines = 4) }
            item { StepCard("3 • REVIEW", "Submit evidence, inspect mistakes, and take one concrete next action.") }
            if (feedback.isNotBlank()) item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.White)) { Text(feedback, Modifier.padding(16.dp), color = Navy, fontSize = 13.sp) } }
        }
        Button({
            if (evidence.trim().length >= 12) { save(c, "track:${track.name}:$lesson"); feedback = "Completed with evidence. Next: apply this skill to a real project or retry a harder problem." }
            else feedback = "Write at least a short attempt/evidence first — PathPilot tracks proof of work, not taps."
        }, Modifier.fillMaxWidth().padding(16.dp)) { Text("Verify & Complete") }
    }
}

@Composable private fun BuildScreen(openProject: (Project) -> Unit, openSpecial: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Brand("Build", "Real project workflow") }
        item { Text("Build from requirement to review instead of following passive tutorials.", Modifier.padding(horizontal = 18.dp), color = Muted, fontSize = 12.sp) }
        items(projects) { p -> Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { openProject(p) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(p.name, color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Black); Text(p.stack, color = Blue, fontSize = 11.sp); Text("${p.tasks.size} production stages", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) } } }
        item { Button({ openSpecial("git") }, Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { Text("Open Git + GitHub Lab") } }
        item { OutlinedButton({ openSpecial("mentor") }, Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { Text("Ask AI Mentor") } }
    }
}

@Composable private fun ProjectScreen(project: Project, back: () -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header(project.name, project.stack, back) }
        item { Text("Production lifecycle", Modifier.padding(horizontal = 18.dp), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Navy) }
        items(project.tasks) { task -> val done = completed(c, "project:${project.name}:$task"); Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { save(c, "project:${project.name}:$task") }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (done) "✓" else "○", color = if (done) Green else Blue, fontSize = 19.sp); Spacer(Modifier.width(10.dp)); Text(task, color = Navy, fontWeight = FontWeight.Bold) } } }
    }
}

@Composable private fun GitScreen(back: () -> Unit) {
    val steps = listOf("Ticket + acceptance criteria", "git switch -c feature/login-validation", "git status", "git diff", "git add .", "git commit -m \"feat: add login validation\"", "git push origin feature/login-validation", "Open Pull Request", "CI + code review", "Fix review comments", "Merge after checks pass")
    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { Header("Git + GitHub Lab", "Professional workflow", back) }; item { StepCard("IMPORTANT", "These are learning steps. PathPilot never pretends it performed real GitHub actions without repository access.") }; items(steps) { s -> Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White)) { Text(s, Modifier.padding(14.dp), color = Navy, fontWeight = FontWeight.Bold) } } }
}

@Composable private fun MentorScreen(back: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxSize()) { Header("AI Mentor", "Teacher • senior developer • debugger", back); LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp)) { item { StepCard("HOW TO USE IT", "Tell the mentor what you tried, what failed, and what you expect. It should guide your reasoning instead of blindly dumping an answer.") }; if (answer.isNotBlank()) item { Card(Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(Color.White)) { Text(answer, Modifier.padding(16.dp), color = Navy, fontSize = 13.sp) } } }; OutlinedTextField(question, { question = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("What are you stuck on?") }, minLines = 3); Button({ scope.launch { answer = try { GeminiMentorService.askGeminiMentor(context, question, "PathPilot learner working hands-on on programming, DSA, backend, cybersecurity or agency skills. Ask for evidence and end with one concrete next action.") } catch (e: Exception) { "Mentor unavailable right now: ${e.message ?: "unknown error"}" } } }, Modifier.fillMaxWidth().padding(16.dp), enabled = question.trim().length >= 3) { Text("Ask Mentor") } }
}

@Composable private fun CollegeScreen() { LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Brand("College", "Syllabus → PYQs → practice → revision") }; item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White)) { Column(Modifier.padding(16.dp)) { Text("Study workflow", color = Navy, fontWeight = FontWeight.Black, fontSize = 18.sp); Text("Upload syllabus/material later, rank questions using evidence, then practice and revise. No fake exact-question guarantees.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) } } }; item { Section("Daily") }; item { Text("• Classes + commute\n• Assignment / PYQ block\n• DSA practice\n• Engineering skill\n• Revision + weak topics", Modifier.padding(horizontal = 20.dp), color = Navy, lineHeight = 23.sp) } } }

@Composable private fun Header(title: String, subtitle: String, back: () -> Unit) { Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) { Text("‹", Modifier.clickable { back() }.padding(10.dp), fontSize = 30.sp, color = Navy); Column { Text(title, color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Black); Text(subtitle, color = Muted, fontSize = 10.sp) } } }
@Composable private fun Section(text: String) { Text(text, Modifier.padding(horizontal = 18.dp), color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Black) }
@Composable private fun ActionCard(title: String, click: () -> Unit) { Card(Modifier.width(105.dp).clickable { click() }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(15.dp)) { Text(title, Modifier.padding(13.dp), color = Navy, fontWeight = FontWeight.Bold, fontSize = 11.sp) } }
@Composable private fun StepCard(title: String, body: String) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(15.dp)) { Text(title, color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(body, color = Navy, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp)) } } }
