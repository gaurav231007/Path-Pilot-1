package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private val Blue = Color(0xFF087BFF)
private val Navy = Color(0xFF111A32)
private val Gray = Color(0xFF64748B)
private val Bg = Color(0xFFF6F9FE)
private val Green = Color(0xFF12A36A)
private val Red = Color(0xFFD94841)

private data class Course(val title: String, val category: String, val lessons: List<String>)
private data class Project(val title: String, val stack: String, val brief: String, val tasks: List<String>)

private val courses = listOf(
    Course("Python Engineering", "Programming", listOf("Variables + control flow", "Functions + modules", "Files + exceptions", "OOP + testing", "Build a CLI tool")),
    Course("Web Development", "Web", listOf("HTML structure", "Responsive CSS", "JavaScript DOM", "TypeScript", "React components", "Next.js app")),
    Course("Backend Engineering", "Backend", listOf("HTTP + REST", "FastAPI", "SQL + PostgreSQL", "Auth + security", "Testing + deployment")),
    Course("Git + GitHub Workflow", "Professional", listOf("Repository + tickets", "Branches", "Diff + staging", "Commits", "Push + PR", "CI + review", "Merge + release")),
    Course("Cybersecurity Lab", "Security", listOf("Linux + networking", "Threat modelling", "Authorized web testing", "Evidence + risk", "Remediation + retest")),
    Course("Automation Agency", "Agency", listOf("Agency foundations", "APIs + webhooks", "n8n", "CRM + lead automation", "WhatsApp + Telegram", "AI agents", "Client delivery")),
    Course("Marketing AI Agency", "Agency", listOf("Positioning", "Offers + landing pages", "AI content", "SEO", "Meta + Google Ads", "Analytics", "Client reporting")),
    Course("AI Product Engineering", "AI", listOf("LLM APIs", "Prompt + structured output", "Tool calling", "RAG", "Evaluation", "Guardrails", "Production AI app"))
)

private val projects = listOf(
    Project("Expense Manager", "React + FastAPI + PostgreSQL", "Build a real multi-screen expense product instead of copying a tutorial.", listOf("Create repo + README", "Design expense data model", "Build add-expense form", "Build REST API", "Persist to PostgreSQL", "Add validation + tests", "Git PR + review", "Deploy", "Security + postmortem")),
    Project("AI Support Agent", "Next.js + Gemini + tools", "Build an assistant that answers, calls tools and records useful actions.", listOf("Write requirements", "Create chat UI", "Connect model", "Add structured responses", "Add tool calling", "Add evaluation cases", "Fix failed cases", "Deploy", "Production review")),
    Project("Automation CRM", "n8n + APIs + Sheets/CRM", "Build a client lead pipeline with webhook, routing, follow-up and failure recovery.", listOf("Client brief", "Lead webhook", "Validation", "CRM write", "WhatsApp/email follow-up", "Duplicate handling", "Error workflow", "Monitoring", "Client handoff")),
    Project("Secure Web Lab", "Local authorized lab", "Find, document and remediate vulnerabilities in an intentionally vulnerable app.", listOf("Define authorization", "Recon", "Threat model", "Test", "Capture evidence", "Risk rating", "Fix", "Retest", "Security report"))
)

private val gitMissions = listOf(
    Pair("Ticket", "Read the ticket and acceptance criteria"),
    Pair("Branch", "git switch -c feature/login-validation"),
    Pair("Inspect", "git status"),
    Pair("Diff", "git diff"),
    Pair("Stage", "git add ."),
    Pair("Commit", "git commit -m \"feat: add login validation\""),
    Pair("History", "git log --oneline"),
    Pair("Push", "git push origin feature/login-validation"),
    Pair("PR", "Open a Pull Request"),
    Pair("Review", "Read CI/review feedback and fix it"),
    Pair("Merge", "Merge only after checks pass")
)

private fun saveDone(context: Context, key: String, value: Boolean) {
    context.getSharedPreferences("pathpilot_real_progress", Context.MODE_PRIVATE).edit().putBoolean(key, value).apply()
}

private fun isDone(context: Context, key: String): Boolean =
    context.getSharedPreferences("pathpilot_real_progress", Context.MODE_PRIVATE).getBoolean(key, false)

@Composable
fun PathPilotFullApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableStateOf("home") }
    var course by remember { mutableStateOf<Course?>(null) }
    var project by remember { mutableStateOf<Project?>(null) }
    var lesson by remember { mutableStateOf<String?>(null) }
    var showGit by remember { mutableStateOf(false) }
    var showMentor by remember { mutableStateOf(false) }

    BackHandler(enabled = course != null || project != null || lesson != null || showGit || showMentor) {
        when {
            lesson != null -> lesson = null
            course != null -> course = null
            project != null -> project = null
            showGit -> showGit = false
            showMentor -> showMentor = false
        }
    }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        when {
            lesson != null -> LessonScreen(lesson!!, { lesson = null })
            course != null -> CourseScreen(course!!, { lesson = it }, { course = null })
            project != null -> ProjectWorkspace(project!!, { project = null })
            showGit -> GitPractice { showGit = false }
            showMentor -> MentorScreen { showMentor = false }
            else -> MainShell(
                tab = tab,
                onTab = { tab = it },
                onCourse = { course = it },
                onProject = { project = it },
                onGit = { showGit = true },
                onMentor = { showMentor = true }
            )
        }
    }
}

@Composable
private fun MainShell(tab: String, onTab: (String) -> Unit, onCourse: (Course) -> Unit, onProject: (Project) -> Unit, onGit: () -> Unit, onMentor: () -> Unit) {
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavItem(Icons.Default.Home, "Home", tab == "home") { onTab("home") }
                NavItem(Icons.Default.School, "Learn", tab == "learn") { onTab("learn") }
                NavItem(Icons.Default.Build, "Build", tab == "build") { onTab("build") }
                NavItem(Icons.Default.CalendarMonth, "College", tab == "college") { onTab("college") }
            }
        }
    ) { padding ->
        when (tab) {
            "learn" -> LearnHub(Modifier.padding(padding), onCourse)
            "build" -> BuildHub(Modifier.padding(padding), onProject, onGit, onMentor)
            "college" -> CollegeHub(Modifier.padding(padding))
            else -> HomeHub(Modifier.padding(padding), onCourse, onProject, onGit, onMentor)
        }
    }
}

@Composable
private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean, click: () -> Unit) {
    NavigationBarItem(selected = selected, onClick = click, icon = { Icon(icon, null) }, label = { Text(label) })
}

@Composable
private fun TopBar(title: String = "PathPilot", subtitle: String = "AI Engineering Academy") {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(45.dp), RoundedCornerShape(14.dp), color = Blue) {
            Box(contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black) }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Navy)
            Text(subtitle, fontSize = 10.sp, color = Gray)
        }
    }
}

@Composable
private fun HomeHub(modifier: Modifier, onCourse: (Course) -> Unit, onProject: (Project) -> Unit, onGit: () -> Unit, onMentor: () -> Unit) {
    val context = LocalContext.current
    val completed = courses.sumOf { c -> c.lessons.count { isDone(context, "lesson:${c.title}:$it") } }
    val total = courses.sumOf { it.lessons.size }
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TopBar() }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Text("Learn by doing.", fontSize = 31.sp, fontWeight = FontWeight.Black, color = Navy)
                Text("Build real software, break it, debug it, review it and ship it.", fontSize = 13.sp, color = Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
        item {
            Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(23.dp)) {
                Column(Modifier.padding(19.dp)) {
                    Text("YOUR ENGINEERING SCORE", color = Color(0xFFBBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$completed / $total learning actions", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    LinearProgressIndicator(progress = { if (total == 0) 0f else completed.toFloat() / total }, Modifier.fillMaxWidth().padding(top = 10.dp), color = Blue, trackColor = Color(0xFF33415F))
                    Text("No flashcards. Every completed item unlocks the next action.", color = Color(0xFFCFD8E8), fontSize = 10.sp, modifier = Modifier.padding(top = 9.dp))
                }
            }
        }
        item { SectionTitle("Continue") }
        item {
            ActionCard("Today’s mission", "Finish one lesson → write code → verify → commit", Icons.Default.PlayArrow) { onCourse(courses.first()) }
        }
        item { SectionTitle("Work like a developer") }
        item {
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                SmallAction("Projects", Icons.Default.Folder, onProject(courses, onProject))
                SmallAction("Git Lab", Icons.Default.Terminal, onGit)
                SmallAction("AI Mentor", Icons.Default.Psychology, onMentor)
            }
        }
        item { SectionTitle("Your paths") }
        items(courses.take(4)) { c -> CourseCard(c, onCourse) }
        item { SectionTitle("Capstones") }
        items(projects.take(3)) { p -> ProjectCard(p, onProject) }
    }
}

private fun onProject(courses: List<Course>, onProject: (Project) -> Unit): () -> Unit = { onProject(projects.first()) }

@Composable
private fun SectionTitle(text: String) { Text(text, Modifier.padding(horizontal = 18.dp, vertical = 2.dp), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Navy) }

@Composable
private fun SmallAction(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit) {
    Card(Modifier.weight(1f).clickable(onClick = click), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.padding(13.dp)) { Icon(icon, null, tint = Blue); Spacer(Modifier.height(7.dp)); Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy) }
    }
}

@Composable
private fun LearnHub(modifier: Modifier, onCourse: (Course) -> Unit) {
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Programming", "Web", "Backend", "Professional", "Security", "Agency", "AI")
    val list = courses.filter { filter == "All" || it.category == filter }
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { TopBar("Learn", "Courses are workspaces, not flashcards") }
        item { LazyRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) { items(filters) { f -> FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) }) } } }
        item { Text("Pick a path → learn a concept → do the task → submit evidence.", Modifier.padding(horizontal = 18.dp, vertical = 6.dp), fontSize = 12.sp, color = Gray) }
        items(list) { CourseCard(it, onCourse) }
    }
}

@Composable
private fun CourseCard(course: Course, click: (Course) -> Unit) {
    val context = LocalContext.current
    val done = course.lessons.count { isDone(context, "lesson:${course.title}:$it") }
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { click(course) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(19.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(42.dp), RoundedCornerShape(12.dp), color = Color(0xFFEAF4FF)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Code, null, tint = Blue) } }
                Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(course.title, fontWeight = FontWeight.Black, color = Navy); Text(course.category, fontSize = 10.sp, color = Blue) }; Text("$done/${course.lessons.size}", fontWeight = FontWeight.Bold, color = Blue)
            }
            LinearProgressIndicator(progress = { done.toFloat() / course.lessons.size }, Modifier.fillMaxWidth().padding(top = 11.dp), color = Blue)
            Text("Open workspace →", fontSize = 11.sp, color = Gray, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun CourseScreen(course: Course, openLesson: (String) -> Unit, back: () -> Unit) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { HeaderWithBack(course.title, "Back", back) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(21.dp)) {
                Column(Modifier.padding(18.dp)) { Text("COURSE WORKSPACE", color = Color(0xFFBBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Black); Text("Do the work, don’t memorize it.", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black); Text("Each lesson ends with an action and evidence.", color = Color(0xFFD8E0EF), fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp)) }
            }
        }
        itemsIndexed(course.lessons) { index, title ->
            val done = isDone(context, "lesson:${course.title}:$title")
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { openLesson(title) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(15.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(34.dp), RoundedCornerShape(10.dp), color = if (done) Color(0xFFE8FAF2) else Color(0xFFEAF4FF)) { Box(contentAlignment = Alignment.Center) { if (done) Icon(Icons.Default.Check, null, tint = Green) else Text("${index + 1}", color = Blue, fontWeight = FontWeight.Bold) } }
                    Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, color = Navy); Text("Concept → task → evidence", fontSize = 10.sp, color = Gray) }; Icon(Icons.Default.ChevronRight, null, tint = Gray)
                }
            }
        }
    }
}

@Composable
private fun LessonScreen(title: String, back: () -> Unit) {
    val context = LocalContext.current
    var work by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    val key = "lesson:current:$title"
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { HeaderWithBack(title, "Lesson", back) }
        item { LessonBlock("1. Understand", lessonConcept(title)) }
        item { LessonBlock("2. Do this", lessonTask(title)) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("YOUR WORK / EVIDENCE", fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Black)
                    OutlinedTextField(work, { work = it }, Modifier.fillMaxWidth().heightIn(min = 150.dp).padding(top = 8.dp), label = { Text("Write code, command, explanation or evidence") })
                    Button(onClick = {
                        result = if (work.trim().length >= 8) "✓ Evidence accepted. This lesson is complete." else "Add more evidence — show what you actually did."
                        if (work.trim().length >= 8) saveDone(context, key, true)
                    }, Modifier.fillMaxWidth().padding(top = 9.dp)) { Text("Submit & Verify") }
                    if (result.isNotBlank()) Text(result, color = if (result.startsWith("✓")) Green else Red, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        item { LessonBlock("3. Next action", "Commit what you changed. Then move to the next lesson or apply this skill inside a project.") }
    }
}

private fun lessonConcept(title: String): String = when {
    title.contains("Branch", true) -> "A branch isolates a piece of work so the main branch stays stable. A feature branch should describe the work, not your mood."
    title.contains("Commit", true) -> "A commit is a small, understandable checkpoint. It should explain one logical change and be easy for another developer to review."
    title.contains("HTTP", true) -> "HTTP is the protocol between clients and servers. Learn method, URL, headers, status code and body before writing an API."
    title.contains("REST", true) -> "A REST API exposes resources through predictable URLs and HTTP methods. Your job is to design clear contracts and validate inputs."
    title.contains("Testing", true) -> "A test is executable evidence. Write the expected behaviour first, then make the implementation satisfy it."
    else -> "Understand the concept, then immediately use it in a tiny piece of working software. PathPilot values evidence over memorisation."
}

private fun lessonTask(title: String): String = when {
    title.contains("Branch", true) -> "Create feature/login-validation. Tell the mentor why you did not edit main directly."
    title.contains("Commit", true) -> "Make one small change and write a conventional commit such as feat:, fix:, test: or docs:."
    title.contains("REST", true) -> "Design GET /expenses and POST /expenses. Write request and response examples before coding."
    title.contains("Testing", true) -> "Write one happy-path test and one failure-path test for the feature you just built."
    title.contains("n8n", true) -> "Create a webhook → validation → CRM workflow. Include a failure path for invalid data."
    title.contains("AI", true) -> "Define one user task, one tool the model can call and one evaluation case where the model must refuse or recover."
    else -> "Create a tiny implementation, run it, observe the result and record what happened."
}

@Composable
private fun LessonBlock(title: String, body: String) {
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) { Text(title, fontWeight = FontWeight.Black, color = Navy, fontSize = 15.sp); Text(body, fontSize = 12.sp, color = Gray, modifier = Modifier.padding(top = 7.dp)) }
    }
}

@Composable
private fun BuildHub(modifier: Modifier, onProject: (Project) -> Unit, onGit: () -> Unit, onMentor: () -> Unit) {
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { TopBar("Build", "Projects + Git + review") }
        item { Text("This is where learning becomes software.", Modifier.padding(horizontal = 18.dp), fontSize = 23.sp, fontWeight = FontWeight.Black, color = Navy) }
        item { ActionCard("Git workflow lab", "Practice ticket → branch → diff → commit → PR → CI → review → merge", Icons.Default.Terminal, onGit) }
        item { ActionCard("AI senior mentor", "Ask about architecture, bugs or code. Get reasoning + one next action.", Icons.Default.Psychology, onMentor) }
        item { SectionTitle("Production projects") }
        items(projects) { ProjectCard(it, onProject) }
    }
}

@Composable
private fun ProjectCard(project: Project, click: (Project) -> Unit) {
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { click(project) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) { Text(project.title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Navy); Text(project.stack, fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Bold); Text(project.brief, fontSize = 11.sp, color = Gray, modifier = Modifier.padding(top = 6.dp)); Text("Open project workspace →", fontSize = 11.sp, color = Blue, modifier = Modifier.padding(top = 9.dp)) }
    }
}

@Composable
private fun ProjectWorkspace(project: Project, back: () -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableIntStateOf(0) }
    var code by remember { mutableStateOf("// Start here\nfun calculateTotal(expenses: List<Int>): Int {\n    return expenses.sum()\n}") }
    var testResult by remember { mutableStateOf("") }
    val doneCount = project.tasks.count { isDone(context, "project:${project.title}:$it") }
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { HeaderWithBack(project.title, "Project workspace", back) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(17.dp)) { Text(project.stack, color = Color(0xFFBBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("$doneCount / ${project.tasks.size} tasks complete", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black); LinearProgressIndicator(progress = { doneCount.toFloat() / project.tasks.size }, Modifier.fillMaxWidth().padding(top = 9.dp), color = Blue, trackColor = Color(0xFF33415F)) }
            }
        }
        item { SectionTitle("Task board") }
        itemsIndexed(project.tasks) { index, task ->
            val done = isDone(context, "project:${project.title}:$task")
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { saveDone(context, "project:${project.title}:$task", !done) }, colors = CardDefaults.cardColors(if (done) Color(0xFFF0FBF6) else Color.White), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (done) Green else Gray); Spacer(Modifier.width(9.dp)); Text("${index + 1}. $task", fontSize = 12.sp, fontWeight = if (done) FontWeight.Bold else FontWeight.Normal, color = Navy) }
            }
        }
        item { SectionTitle("Working area") }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(15.dp)) {
                    Text("CODE / IMPLEMENTATION", fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Black)
                    OutlinedTextField(code, { code = it }, Modifier.fillMaxWidth().heightIn(min = 220.dp).padding(top = 8.dp), textStyle = LocalTextStyle.current.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace), label = { Text("Write the implementation") })
                    Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { testResult = if (code.contains("sum()") || code.contains("return")) "✓ Basic mentor checks passed. Now add a failure case and a test." else "✗ Test failed: implement the required behaviour first." }, Modifier.weight(1f)) { Text("Run checks") }
                        OutlinedButton(onClick = { saveDone(context, "project:${project.title}:Implementation", true); testResult = "Saved as implementation evidence." }, Modifier.weight(1f)) { Text("Save") }
                    }
                    if (testResult.isNotBlank()) Text(testResult, fontSize = 11.sp, color = if (testResult.startsWith("✓")) Green else if (testResult.startsWith("✗")) Red else Blue, modifier = Modifier.padding(top = 7.dp))
                }
            }
        }
        item { SectionTitle("Next professional action") }
        item { Text("When checks pass: inspect git diff → make a focused commit → push branch → open PR → review → fix comments → merge → deploy.", Modifier.padding(horizontal = 18.dp), fontSize = 12.sp, color = Gray) }
    }
}

@Composable
private fun GitPractice(back: () -> Unit) {
    val context = LocalContext.current
    var index by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    val mission = gitMissions[index]
    val expected = mission.second.lowercase()
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { HeaderWithBack("Git + GitHub Lab", "Real workflow practice", back) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) { Text("MISSION ${index + 1}/${gitMissions.size}", color = Color(0xFFBBD9FF), fontSize = 10.sp, fontWeight = FontWeight.Black); Text(mission.first, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text("Do it yourself. PathPilot checks the action.", color = Color(0xFFD8E0EF), fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp)) }
            }
        }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("TASK", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(if (mission.first == "Ticket") "Read this requirement: validate login email and password before submission." else mission.second, color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                    OutlinedTextField(answer, { answer = it }, Modifier.fillMaxWidth().padding(top = 10.dp), label = { Text(if (mission.first == "Ticket") "Explain the acceptance criteria" else "Command / action") })
                    Button(onClick = {
                        val ok = if (mission.first == "Ticket") answer.length >= 20 else answer.trim().lowercase() == expected
                        feedback = if (ok) "✓ Correct. Next mission unlocked." else "✗ Not quite. Think about what a developer must actually do at this stage."
                        if (ok) {
                            saveDone(context, "git:${mission.first}", true)
                            if (index < gitMissions.lastIndex) { index++; answer = "" }
                        }
                    }, Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(if (index == gitMissions.lastIndex) "Finish workflow" else "Check & continue") }
                    if (feedback.isNotBlank()) Text(feedback, color = if (feedback.startsWith("✓")) Green else Red, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp))
                }
            }
        }
        item { SectionTitle("Workflow") }
        itemsIndexed(gitMissions) { i, m -> Text("${if (i < index) "✓" else if (i == index) "→" else "○"} ${m.first}: ${m.second}", Modifier.padding(horizontal = 20.dp), fontSize = 11.sp, color = if (i == index) Navy else Gray, fontWeight = if (i == index) FontWeight.Bold else FontWeight.Normal) }
        item { Text("Note: this first lab teaches the real Git commands. Actual GitHub push/PR execution can be connected later through explicit GitHub authorization; the app never pretends a simulated action is a real commit.", Modifier.padding(18.dp), fontSize = 10.sp, color = Gray) }
    }
}

@Composable
private fun MentorScreen(back: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { HeaderWithBack("AI Mentor", "Senior engineer mode", back) }
        item { Text("Don’t ask for the final answer first. Show me what you tried.", Modifier.padding(horizontal = 18.dp), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Navy) }
        item {
            OutlinedTextField(question, { question = it }, Modifier.padding(horizontal = 16.dp).fillMaxWidth().heightIn(min = 120.dp), label = { Text("Bug, architecture decision, code or career question") })
            Button(onClick = {
                if (question.isNotBlank()) {
                    loading = true
                    scope.launch { answer = GeminiMentorService.askGeminiMentor(context, question, "Learner is doing a real PathPilot project. Teach reasoning, ask for evidence, review code, and finish with one concrete next action. Never pretend a simulated Git action is a real GitHub action."); loading = false }
                }
            }, Modifier.padding(horizontal = 16.dp).fillMaxWidth().padding(top = 8.dp), enabled = question.isNotBlank() && !loading) { Text(if (loading) "Thinking…" else "Ask senior mentor") }
        }
        if (answer.isNotBlank()) item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) { Text(answer, Modifier.padding(17.dp), fontSize = 12.sp, color = Navy) } }
        item { Text("Good prompts", Modifier.padding(horizontal = 18.dp), fontSize = 16.sp, fontWeight = FontWeight.Black, color = Navy) }
        items(listOf("Review this function like a senior engineer.", "Why is my API design wrong?", "Give me debugging hints without solving it.", "Review my Git commit and suggest a better message.")) { prompt -> Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { question = prompt }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(13.dp)) { Text(prompt, Modifier.padding(13.dp), fontSize = 11.sp, color = Gray) } }
    }
}

@Composable
private fun CollegeHub(modifier: Modifier) {
    val context = LocalContext.current
    val tasks = listOf("Thermodynamics: revise laws + processes", "Engineering Maths: solve 5 matrix problems", "C lab: implement pointers program", "Review yesterday’s mistakes", "30-minute spoken English practice")
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { TopBar("College", "Evidence-based study mode") }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFFFF4DF)), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) { Text("EXAM + SKILL BALANCE", color = Color(0xFFB45309), fontSize = 10.sp, fontWeight = FontWeight.Black); Text("Syllabus → PYQs → practice → mistakes → revision", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Black); Text("Important-question rankings are evidence-based, never guaranteed exact predictions.", color = Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp)) }
            }
        }
        item { SectionTitle("Today’s study plan") }
        items(tasks) { task ->
            var checked by remember { mutableStateOf(isDone(context, "college:$task")) }
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { checked = !checked; saveDone(context, "college:$task", checked) }, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (checked) Green else Gray); Spacer(Modifier.width(9.dp)); Text(task, color = Navy, fontSize = 12.sp, fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal) }
            }
        }
        item { SectionTitle("Study rules") }
        item { Text("Upload syllabus/PYQs when the material analyzer is connected, then rank topics by repetition, marks, syllabus weight and your mistakes. PathPilot must show evidence behind every priority.", Modifier.padding(horizontal = 18.dp), fontSize = 12.sp, color = Gray) }
    }
}

@Composable
private fun ActionCard(title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit) {
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable(onClick = click), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Surface(Modifier.size(43.dp), RoundedCornerShape(13.dp), color = Color(0xFFEAF4FF)) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Blue) } }; Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Black, color = Navy); Text(detail, fontSize = 11.sp, color = Gray, modifier = Modifier.padding(top = 4.dp)) }; Icon(Icons.Default.ChevronRight, null, tint = Gray) }
    }
}

@Composable
private fun HeaderWithBack(title: String, subtitle: String, back: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Back") }
        Column { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Navy); Text(subtitle, fontSize = 9.sp, color = Gray) }
    }
}
