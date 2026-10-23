package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private val PPBlue = Color(0xFF087BFF)
private val PPNavy = Color(0xFF101A33)
private val PPMuted = Color(0xFF64748B)
private val PPBg = Color(0xFFF5F8FD)
private val PPGreen = Color(0xFF12A36A)
private val PPRed = Color(0xFFD94841)

private data class Path(val name: String, val category: String, val outcome: String, val lessons: List<String>)
private data class BuildProject(val name: String, val stack: String, val outcome: String, val tasks: List<String>)

private val paths = listOf(
    Path("Python Engineering", "Programming", "Build useful Python software", listOf("Control flow", "Functions + modules", "Files + exceptions", "Testing", "CLI project")),
    Path("Web Engineering", "Web", "Build production web interfaces", listOf("HTML5", "Responsive CSS", "JavaScript", "TypeScript", "React", "Next.js", "Node.js")),
    Path("Backend Engineering", "Backend", "Build and deploy APIs", listOf("HTTP", "REST", "FastAPI", "SQL + PostgreSQL", "Auth", "Testing + deployment")),
    Path("Git + GitHub", "Professional", "Work like a real developer", listOf("Tickets", "Branches", "Status + diff", "Staging", "Commits", "Push + PR", "CI + review", "Merge + release")),
    Path("Cybersecurity", "Security", "Assess and secure authorized labs", listOf("Scope", "Linux + networking", "Threat model", "Authorized testing", "Evidence", "Remediation", "Retest")),
    Path("Automation Agency", "Agency", "Build client automation systems", listOf("Offers", "APIs + webhooks", "n8n", "CRM automation", "WhatsApp + Telegram", "AI agents", "Monitoring", "Client delivery")),
    Path("Marketing AI Agency", "Agency", "Build measurable marketing systems", listOf("Positioning", "Offers", "Landing pages", "AI content", "SEO", "Ads", "Analytics", "Reporting")),
    Path("AI Product Engineering", "AI", "Ship AI features safely", listOf("LLM APIs", "Structured output", "Tools", "RAG", "Evaluation", "Guardrails", "Production"))
)

private val buildProjects = listOf(
    BuildProject("Expense Manager", "React + FastAPI + PostgreSQL", "A multi-user product built from requirement to deployment.", listOf("Requirement + user stories", "Repository + README", "Database schema", "REST API", "Frontend screens", "Authentication", "Tests + CI", "Git PR + review", "Deploy", "Security review", "Postmortem")),
    BuildProject("AI Support Agent", "Next.js + Gemini + tools", "An AI assistant with controlled tools and evaluation.", listOf("Product brief", "Chat UI", "Model integration", "Structured output", "Tool calling", "Evaluation suite", "Failure handling", "Observability", "Deploy", "Production review")),
    BuildProject("Automation CRM", "n8n + APIs + CRM", "A client lead pipeline with recovery and monitoring.", listOf("Client brief", "Webhook", "Validation", "CRM write", "Follow-up", "Deduplication", "Error workflow", "Monitoring", "Client handoff")),
    BuildProject("Secure Web Lab", "Authorized local lab", "Find, fix and retest vulnerabilities safely.", listOf("Authorization + scope", "Recon", "Threat model", "Test case", "Evidence", "Risk rating", "Remediation", "Retest", "Security report"))
)

private fun done(c: Context, key: String) = c.getSharedPreferences("pathpilot_v2", Context.MODE_PRIVATE).getBoolean(key, false)
private fun setDone(c: Context, key: String, value: Boolean) = c.getSharedPreferences("pathpilot_v2", Context.MODE_PRIVATE).edit().putBoolean(key, value).apply()

@Composable
fun PathPilotApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableStateOf("home") }
    var selectedPath by remember { mutableStateOf<Path?>(null) }
    var selectedLesson by remember { mutableStateOf<String?>(null) }
    var selectedProject by remember { mutableStateOf<BuildProject?>(null) }
    var screen by remember { mutableStateOf<String?>(null) }

    BackHandler(selectedPath != null || selectedLesson != null || selectedProject != null || screen != null) {
        when {
            selectedLesson != null -> selectedLesson = null
            selectedPath != null -> selectedPath = null
            selectedProject != null -> selectedProject = null
            else -> screen = null
        }
    }

    Surface(Modifier.fillMaxSize(), color = PPBg) {
        when {
            selectedLesson != null && selectedPath != null -> LessonPage(selectedPath!!, selectedLesson!!) { selectedLesson = null }
            selectedPath != null -> PathPage(selectedPath!!, { selectedLesson = it }) { selectedPath = null }
            selectedProject != null -> ProjectPage(selectedProject!!) { selectedProject = null }
            screen == "git" -> GitLab { screen = null }
            screen == "mentor" -> MentorPage { screen = null }
            else -> Shell(tab, { tab = it }, { selectedPath = it }, { selectedProject = it }, { screen = "git" }, { screen = "mentor" })
        }
    }
}

@Composable private fun Shell(tab: String, setTab: (String) -> Unit, openPath: (Path) -> Unit, openProject: (BuildProject) -> Unit, openGit: () -> Unit, openMentor: () -> Unit) {
    Scaffold(containerColor = PPBg, bottomBar = {
        NavigationBar(containerColor = Color.White) {
            NavItem(Icons.Default.Home, "Home", tab == "home") { setTab("home") }
            NavItem(Icons.Default.School, "Learn", tab == "learn") { setTab("learn") }
            NavItem(Icons.Default.Build, "Build", tab == "build") { setTab("build") }
            NavItem(Icons.Default.CalendarMonth, "College", tab == "college") { setTab("college") }
        }
    }) { p ->
        when (tab) {
            "learn" -> LearnPage(Modifier.padding(p), openPath)
            "build" -> BuildPage(Modifier.padding(p), openProject, openGit, openMentor)
            "college" -> CollegePage(Modifier.padding(p))
            else -> HomePage(Modifier.padding(p), openPath, openProject, openGit, openMentor)
        }
    }
}

@Composable private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, selected: Boolean, click: () -> Unit) {
    NavigationBarItem(selected = selected, onClick = click, icon = { Icon(icon, null) }, label = { Text(text) })
}

@Composable private fun Brand(title: String = "PathPilot", subtitle: String = "AI Engineering Academy") {
    Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(46.dp), RoundedCornerShape(15.dp), color = PPBlue) { Box(contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black) } }
        Spacer(Modifier.width(11.dp))
        Column { Text(title, fontSize = 21.sp, fontWeight = FontWeight.Black, color = PPNavy); Text(subtitle, fontSize = 10.sp, color = PPMuted) }
    }
}

@Composable private fun BackHeader(title: String, subtitle: String, back: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(back) { Icon(Icons.Default.ArrowBack, "Back", tint = PPNavy) }
        Column { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = PPNavy); Text(subtitle, fontSize = 9.sp, color = PPMuted) }
    }
}

@Composable private fun HomePage(m: Modifier, openPath: (Path) -> Unit, openProject: (BuildProject) -> Unit, openGit: () -> Unit, openMentor: () -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    val total = paths.sumOf { it.lessons.size }
    val complete = paths.sumOf { p -> p.lessons.count { done(c, "lesson:${p.name}:$it") } }
    LazyColumn(m, contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Brand() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("Build. Break. Debug. Ship.", fontSize = 30.sp, fontWeight = FontWeight.Black, color = PPNavy); Text("PathPilot is a working environment — not a flashcard app.", fontSize = 13.sp, color = PPMuted, modifier = Modifier.padding(top = 4.dp)) } }
        item { MissionCard("TODAY'S MISSION", "Git workflow: login validation", "Ticket → branch → code → test → commit → PR → review", "Start Git Lab", openGit) }
        item { ProgressCard(complete, total) }
        item { Section("Workspaces") }
        item { Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniCard("Projects", Icons.Default.Folder, { openProject(buildProjects.first()) }); MiniCard("Git Lab", Icons.Default.Terminal, openGit); MiniCard("AI Mentor", Icons.Default.Psychology, openMentor) } }
        item { Section("Continue learning") }
        items(paths.take(5)) { PathCard(it, openPath) }
        item { Section("Production projects") }
        items(buildProjects) { ProjectCard(it, openProject) }
    }
}

@Composable private fun MissionCard(label: String, title: String, detail: String, button: String, click: () -> Unit) {
    Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), colors = CardDefaults.cardColors(PPNavy), shape = RoundedCornerShape(23.dp)) {
        Column(Modifier.padding(19.dp)) { Text(label, color = Color(0xFFBBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp)); Text(detail, color = Color(0xFFD8E0EF), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)); Button(click, Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(button) } }
    }
}

@Composable private fun ProgressCard(doneCount: Int, total: Int) {
    val progress = if (total == 0) 0f else doneCount.toFloat() / total
    Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(17.dp)) { Text("ENGINEERING PROGRESS", color = PPBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("$doneCount / $total actions completed", color = PPNavy, fontSize = 20.sp, fontWeight = FontWeight.Black); LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth().padding(top = 9.dp), color = PPBlue); Text("Completion means you submitted evidence, not that you tapped a card.", color = PPMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 7.dp)) } }
}

@Composable private fun Section(text: String) { Text(text, Modifier.padding(horizontal = 18.dp), fontSize = 18.sp, fontWeight = FontWeight.Black, color = PPNavy) }

@Composable private fun MiniCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit) {
    Card(Modifier.weight(1f).clickable(onClick = click), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(12.dp)) { Icon(icon, null, tint = PPBlue); Spacer(Modifier.height(6.dp)); Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PPNavy) } }
}

@Composable private fun LearnPage(m: Modifier, openPath: (Path) -> Unit) {
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Programming", "Web", "Backend", "Professional", "Security", "Agency", "AI")
    val visible = paths.filter { filter == "All" || it.category == filter }
    LazyColumn(m, contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Brand("Learn", "Skill paths with hands-on work") }
        item { LazyRow(Modifier.padding(horizontal = 15.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) { items(filters) { f -> FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) }) } } }
        item { Text("Open a path → understand → implement → verify → commit → apply in a project.", Modifier.padding(horizontal = 18.dp), fontSize = 12.sp, color = PPMuted) }
        items(visible) { PathCard(it, openPath) }
    }
}

@Composable private fun PathCard(path: Path, click: (Path) -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    val finished = path.lessons.count { done(c, "lesson:${path.name}:$it") }
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { click(path) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(19.dp)) {
        Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Surface(Modifier.size(42.dp), RoundedCornerShape(12.dp), color = Color(0xFFEAF4FF)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Code, null, tint = PPBlue) } }; Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(path.name, fontWeight = FontWeight.Black, color = PPNavy); Text(path.category, fontSize = 10.sp, color = PPBlue) }; Text("$finished/${path.lessons.size}", color = PPBlue, fontWeight = FontWeight.Bold) }; Text(path.outcome, color = PPMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp)); LinearProgressIndicator(progress = { finished.toFloat() / path.lessons.size }, Modifier.fillMaxWidth().padding(top = 9.dp), color = PPBlue); Text("Open workspace →", color = PPBlue, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp)) }
    }
}

@Composable private fun PathPage(path: Path, openLesson: (String) -> Unit, back: () -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { BackHeader(path.name, "Hands-on workspace", back) }
        item { MissionCard("PATH OUTCOME", path.outcome, "Every step produces something you can use in a project.", "Start next action", { openLesson(path.lessons.first()) }) }
        item { Section("Work stages") }
        items(path.lessons) { lesson ->
            val complete = done(c, "lesson:${path.name}:$lesson")
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { openLesson(lesson) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(15.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (complete) Icons.Default.CheckCircle else Icons.Default.Circle, null, tint = if (complete) PPGreen else PPBlue); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(lesson, fontWeight = FontWeight.Bold, color = PPNavy); Text("Understand → do → verify → evidence", fontSize = 10.sp, color = PPMuted) }; Icon(Icons.Default.ChevronRight, null, tint = PPMuted) } }
        }
    }
}

@Composable private fun LessonPage(path: Path, lesson: String, back: () -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    var evidence by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    val key = "lesson:${path.name}:$lesson"
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { BackHeader(lesson, path.name, back) }
        item { LessonCard("UNDERSTAND", conceptFor(lesson)) }
        item { LessonCard("DO IT", taskFor(path.name, lesson)) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) { Text("EVIDENCE", color = PPBlue, fontSize = 10.sp, fontWeight = FontWeight.Black); OutlinedTextField(evidence, { evidence = it }, Modifier.fillMaxWidth().heightIn(min = 150.dp).padding(top = 8.dp), label = { Text("Paste code, command output, explanation or test result") }); Button(onClick = { if (evidence.trim().length >= 12) { setDone(c, key); feedback = "✓ Verified. Next action unlocked." } else feedback = "Add real evidence — show what you actually did." }, Modifier.fillMaxWidth().padding(top = 9.dp)) { Text("Verify & Complete") }; if (feedback.isNotBlank()) Text(feedback, color = if (feedback.startsWith("✓")) PPGreen else PPRed, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp)) }
            }
        }
        item { LessonCard("PROFESSIONAL NEXT STEP", "Apply this skill inside the current project. Then inspect git diff, commit the focused change and continue the workflow.") }
    }
}

@Composable private fun LessonCard(title: String, body: String) { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(title, color = PPNavy, fontWeight = FontWeight.Black, fontSize = 14.sp); Text(body, color = PPMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp)) } } }

private fun conceptFor(s: String) = when {
    s.contains("branch", true) -> "A branch isolates one piece of work so main stays stable. The branch name should describe the feature or fix."
    s.contains("commit", true) -> "A commit is a small, reviewable checkpoint. One commit should explain one logical change."
    s.contains("HTTP", true) -> "HTTP defines how clients and servers communicate through methods, URLs, headers, status codes and bodies."
    s.contains("REST", true) -> "REST uses predictable resource endpoints and HTTP semantics. Good APIs have clear request and response contracts."
    s.contains("testing", true) -> "Tests are executable evidence. Cover both the expected behaviour and an important failure case."
    s.contains("webhook", true) -> "A webhook delivers an event to your workflow. Validate the payload before making side effects."
    s.contains("LLM", true) -> "An AI feature is a product system around a model: inputs, outputs, tools, failures, evaluation and guardrails."
    else -> "Understand the concept, implement a tiny working example, observe the result and record evidence."
}

private fun taskFor(path: String, lesson: String) = when {
    path.contains("Git") && lesson.contains("branch", true) -> "Create feature/login-validation and explain why you did not edit main directly."
    path.contains("Git") && lesson.contains("commit", true) -> "Make one focused change and write a message such as feat:, fix:, test: or docs:."
    lesson.contains("REST", true) -> "Design GET /expenses and POST /expenses with example JSON requests and responses."
    lesson.contains("testing", true) -> "Write one happy-path test and one failure-path test for the feature you are building."
    lesson.contains("n8n", true) -> "Design webhook → validation → CRM → notification, including an error path."
    lesson.contains("AI", true) -> "Define one user task, one controlled tool and one evaluation case where the model must recover safely."
    else -> "Implement a tiny example, run it, inspect the result and record what changed."
}

@Composable private fun BuildPage(m: Modifier, openProject: (BuildProject) -> Unit, openGit: () -> Unit, openMentor: () -> Unit) {
    LazyColumn(m, contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Brand("Build", "Projects + Git + debugging + review") }
        item { Text("This is where learning becomes software.", Modifier.padding(horizontal = 18.dp), fontSize = 23.sp, fontWeight = FontWeight.Black, color = PPNavy) }
        item { MissionCard("DEVELOPER WORKFLOW", "Git → Build → Test → Review → Ship", "Every project follows the same professional lifecycle.", "Open Git Lab", openGit) }
        item { Section("Projects") }
        items(buildProjects) { ProjectCard(it, openProject) }
        item { ActionCard("AI Senior Mentor", "Explain your bug or architecture decision and get hints, review and a concrete next action.", Icons.Default.Psychology, openMentor) }
    }
}

@Composable private fun ProjectCard(project: BuildProject, click: (BuildProject) -> Unit) { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { click(project) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(project.name, fontSize = 17.sp, fontWeight = FontWeight.Black, color = PPNavy); Text(project.stack, fontSize = 10.sp, color = PPBlue, fontWeight = FontWeight.Bold); Text(project.outcome, fontSize = 11.sp, color = PPMuted, modifier = Modifier.padding(top = 6.dp)); Text("Open project workspace →", fontSize = 11.sp, color = PPBlue, modifier = Modifier.padding(top = 9.dp)) } } }

@Composable private fun ProjectPage(project: BuildProject, back: () -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    var code by remember { mutableStateOf("// Implement the current task here\n") }
    var output by remember { mutableStateOf("") }
    val finished = project.tasks.count { done(c, "project:${project.name}:$it") }
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BackHeader(project.name, project.stack, back) }
        item { ProgressCard(finished, project.tasks.size) }
        item { Section("Project board") }
        items(project.tasks) { task -> val checked = done(c, "project:${project.name}:$task"); Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { setDone(c, "project:${project.name}:$task", !checked) }, colors = CardDefaults.cardColors(if (checked) Color(0xFFEFFAF5) else Color.White), shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (checked) Icons.Default.CheckCircle else Icons.Default.Circle, null, tint = if (checked) PPGreen else PPMuted); Spacer(Modifier.width(9.dp)); Text(task, color = PPNavy, fontSize = 12.sp, fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal) } } }
        item { Section("Implementation") }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(15.dp)) { OutlinedTextField(code, { code = it }, Modifier.fillMaxWidth().heightIn(min = 210.dp), textStyle = LocalTextStyle.current.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace), label = { Text("Write the current implementation") }); Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { output = if (code.length >= 25) "✓ Evidence captured. Now write a test and inspect git diff." else "✗ Add the implementation first." }, Modifier.weight(1f)) { Text("Run check") }; OutlinedButton(onClick = { setDone(c, "project:${project.name}:implementation"); output = "Saved. Next: test → diff → commit." }, Modifier.weight(1f)) { Text("Save") } }; if (output.isNotBlank()) Text(output, fontSize = 11.sp, color = if (output.startsWith("✓") || output.startsWith("Saved")) PPGreen else PPRed, modifier = Modifier.padding(top = 7.dp)) } } }
        item { LessonCard("SHIP IT", "When all tasks are done: git status → git diff → commit → push → Pull Request → CI → code review → merge → deploy → postmortem.") }
    }
}

@Composable private fun GitLab(back: () -> Unit) {
    val c = androidx.compose.ui.platform.LocalContext.current
    val steps = listOf("Ticket + acceptance criteria", "git switch -c feature/login-validation", "git status", "git diff", "git add .", "git commit -m \"feat: add login validation\"", "git log --oneline", "git push origin feature/login-validation", "Open Pull Request", "CI + code review", "Fix review comments", "Merge after checks pass")
    var index by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BackHeader("Git + GitHub Lab", "Practice the real developer workflow", back) }
        item { MissionCard("MISSION ${index + 1}/${steps.size}", steps[index], if (index == 0) "Write the acceptance criteria for login validation." else "Enter the command or action exactly as you would perform it.", "Check step", { val ok = answer.trim().isNotEmpty() && (index == 0 || answer.trim().equals(steps[index], true)); feedback = if (ok) { setDone(c, "git:${index}"); if (index < steps.lastIndex) index++; answer = ""; "✓ Correct. Next step unlocked." } else "✗ Not yet. Read the step and perform it yourself." }) }
        item { OutlinedTextField(answer, { answer = it }, Modifier.padding(horizontal = 16.dp).fillMaxWidth(), label = { Text(if (index == 0) "Acceptance criteria" else "Command / action") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)) }
        if (feedback.isNotBlank()) item { Text(feedback, Modifier.padding(horizontal = 18.dp), color = if (feedback.startsWith("✓")) PPGreen else PPRed, fontSize = 11.sp) }
        item { Section("Workflow map") }
        items(steps) { step -> val pos = steps.indexOf(step); Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (pos < index) Icons.Default.CheckCircle else if (pos == index) Icons.Default.PlayArrow else Icons.Default.Circle, null, tint = if (pos < index) PPGreen else if (pos == index) PPBlue else PPMuted); Spacer(Modifier.width(9.dp)); Text(step, fontSize = 11.sp, color = if (pos == index) PPNavy else PPMuted, fontWeight = if (pos == index) FontWeight.Bold else FontWeight.Normal) } }
        item { LessonCard("IMPORTANT", "This lab does not fake GitHub activity. Until the learner explicitly connects GitHub, commands are practice. Real commits, pushes and PRs require real repository access.") }
    }
}

@Composable private fun MentorPage(back: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val c = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { BackHeader("AI Mentor", "Senior engineer mode", back) }
        item { Text("Show me what you tried first.", Modifier.padding(horizontal = 18.dp), fontSize = 25.sp, fontWeight = FontWeight.Black, color = PPNavy) }
        item { OutlinedTextField(question, { question = it }, Modifier.padding(horizontal = 16.dp).fillMaxWidth().heightIn(min = 130.dp), label = { Text("Bug, code, architecture or career problem") }); Button(onClick = { if (question.isNotBlank()) { loading = true; scope.launch { response = GeminiMentorService.askGeminiMentor(c, question, "Learner is working inside PathPilot on a real project. Teach reasoning, ask for evidence, review the attempt and end with one concrete next action. Never pretend simulated Git actions are real GitHub actions."); loading = false } } }, Modifier.padding(horizontal = 16.dp).fillMaxWidth().padding(top = 8.dp), enabled = question.isNotBlank() && !loading) { Text(if (loading) "Thinking…" else "Ask mentor") } }
        if (response.isNotBlank()) item { LessonCard("MENTOR REVIEW", response) }
        item { Section("Try asking") }
        items(listOf("Review this function like a senior engineer.", "Give me debugging hints without solving it.", "Review my API design.", "What should I test before opening my PR?")) { q -> Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { question = q }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) { Text(q, Modifier.padding(13.dp), fontSize = 11.sp, color = PPMuted) } }
    }
}

@Composable private fun CollegePage(m: Modifier) {
    val c = androidx.compose.ui.platform.LocalContext.current
    val tasks = listOf("Thermodynamics revision", "Engineering Maths matrix practice", "C programming lab", "Review mistakes", "Spoken English practice")
    LazyColumn(m, contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Brand("College", "Study + exam preparation") }
        item { LessonCard("STUDY LOOP", "Syllabus → concepts → PYQs → practice → mistakes → revision. Priority is evidence-based; PathPilot never promises exact exam questions.") }
        item { Section("Today") }
        items(tasks) { task -> var checked by remember { mutableStateOf(done(c, "college:$task")) }; Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { checked = !checked; setDone(c, "college:$task", checked) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (checked) Icons.Default.CheckCircle else Icons.Default.Circle, null, tint = if (checked) PPGreen else PPMuted); Spacer(Modifier.width(9.dp)); Text(task, color = PPNavy, fontSize = 12.sp, fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal) } } }
    }
}

@Composable private fun ActionCard(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit) { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable(onClick = click), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = PPBlue); Spacer(Modifier.width(11.dp)); Column { Text(title, fontWeight = FontWeight.Black, color = PPNavy); Text(detail, fontSize = 11.sp, color = PPMuted, modifier = Modifier.padding(top = 4.dp)) } } } }
