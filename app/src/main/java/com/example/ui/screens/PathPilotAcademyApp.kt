package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ai.GeminiMentorService
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private val Blue = Color(0xFF087BFF)
private val Cyan = Color(0xFF12C8EA)
private val Navy = Color(0xFF101A33)
private val SoftBlue = Color(0xFFEAF5FF)
private val SoftPurple = Color(0xFFF0ECFF)
private val SoftGreen = Color(0xFFE9FBF2)
private val SoftOrange = Color(0xFFFFF4DF)

private data class Skill(
    val id: String,
    val track: String,
    val title: String,
    val level: String,
    val concept: String,
    val mission: String,
    val build: String,
    val debug: String,
    val review: String
)

private val skills = listOf(
    Skill("py1", "Programming", "Python Programming", "Beginner", "Variables, conditions, loops, functions and errors", "Build a validator that rejects empty input and invalid numbers.", "Turn it into a reusable CLI utility with tests.", "Trace a deliberately broken condition from symptom to root cause.", "Explain the fix and add three regression tests."),
    Skill("http1", "Backend", "HTTP Request to Response", "Beginner", "Methods, headers, status codes, JSON and request flow", "Inspect GET and POST requests and explain the important fields.", "Build a small API client handling 2xx, 4xx and 5xx responses.", "Diagnose a 400, 404 and 500 using evidence.", "Explain the request lifecycle and validation boundary."),
    Skill("api1", "Backend", "FastAPI REST API", "Intermediate", "Routes, schemas, validation, errors and API contracts", "Create CRUD endpoints for tasks.", "Build a notes API with validation and clean errors.", "Debug route-not-found and validation failures.", "Write endpoint tests and document the contract."),
    Skill("sql1", "Backend", "SQL + PostgreSQL", "Intermediate", "CRUD, joins, constraints, indexes and transactions", "Write queries for users, projects and tasks.", "Connect an API to PostgreSQL or Supabase.", "Find the cause of a bad query or constraint violation.", "Explain how the schema prevents inconsistent data."),
    Skill("git1", "Professional", "Git & GitHub Workflow", "Beginner", "Requirements, branches, commits, PRs, reviews and CI", "Turn a feature request into acceptance criteria and a branch plan.", "Implement it with focused commits and tests.", "Read a failing CI check and find useful evidence.", "Write a PR description a teammate can review."),
    Skill("prod1", "Professional", "Production Debugging", "Intermediate", "Logs, reproduction, tests, observability and postmortems", "Turn a bug report into a minimal reproduction.", "Create a regression test and structured error handling.", "Use logs and tests to isolate root cause.", "Write a short incident note."),
    Skill("ai1", "AI Development", "Build with LLM APIs", "Beginner", "Prompts, structured output, errors, latency and cost", "Call an LLM and safely parse structured output.", "Build a bounded AI feature inside a normal application.", "Handle timeout, invalid output and API errors.", "Explain which decisions must stay deterministic."),
    Skill("sec1", "Cybersecurity", "Linux + Networking Lab", "Beginner", "Processes, permissions, ports, DNS, TCP/IP, HTTP and logs", "Inspect a service in your own lab and explain its ports.", "Run and observe a local service.", "Diagnose a permission, port or DNS problem safely.", "Keep a lab notebook with commands and evidence."),
    Skill("sec2", "Cybersecurity", "Web Security Testing", "Intermediate", "Scope, authorization, threat modeling and remediation", "Write scope and trust boundaries before testing.", "Test an intentionally vulnerable local lab only.", "Document one finding with evidence, impact and remediation.", "Retest after the fix and compare evidence."),
    Skill("cap", "Capstone", "Production-Style AI Product", "Advanced", "Requirements, architecture, backend, DB, AI, tests, Git and security", "Write a one-page spec and acceptance criteria before coding.", "Build the product in small milestones.", "Handle a real bug, failed test and security finding.", "Review like a senior engineer and write a postmortem.")
)

private enum class Phase { TEACH, LAB, BUILD, DEBUG, REVIEW }

@Composable
fun PathPilotAcademyApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = skills.firstOrNull { it.id == selectedId }
    BackHandler(enabled = selected != null) { selectedId = null }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8FBFF)) {
        if (selected != null) {
            TrainingScreen(selected) { selectedId = null }
        } else {
            AcademyShell(tab, { tab = it }, { selectedId = it })
        }
    }
}

@Composable
private fun AcademyShell(tab: Int, onTab: (Int) -> Unit, onSkill: (String) -> Unit) {
    val names = listOf("Home", "Learn", "Mentor", "College")
    val icons = listOf(Icons.Default.Home, Icons.Default.Code, Icons.Default.Psychology, Icons.Default.School)
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                0 -> HomeScreen(onSkill, { onTab(1) })
                1 -> LearnScreen(onSkill)
                2 -> MentorScreen()
                else -> CollegeScreen()
            }
        }
        NavigationBar(containerColor = Color.White) {
            names.forEachIndexed { i, name ->
                NavigationBarItem(
                    selected = tab == i,
                    onClick = { onTab(i) },
                    icon = { Icon(icons[i], name) },
                    label = { Text(name) }
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Cyan, Blue))),
            contentAlignment = Alignment.Center
        ) { Text("➤", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Path", fontSize = 23.sp, fontWeight = FontWeight.Black, color = Navy)
                Text("Pilot", fontSize = 23.sp, fontWeight = FontWeight.Black, color = Blue)
            }
            Text("AI Engineering Academy", fontSize = 10.sp, color = Color(0xFF64748B))
        }
        IconButton(onClick = {}) { Icon(Icons.Default.Notifications, "Notifications") }
        Box(Modifier.size(36.dp).clip(CircleShape).background(SoftBlue), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, "Profile", tint = Blue)
        }
    }
}

@Composable
private fun HomeScreen(onSkill: (String) -> Unit, onLearn: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pathpilot_engine", Context.MODE_PRIVATE) }
    val done = skills.count { prefs.getBoolean("done_${it.id}", false) }
    val next = skills.firstOrNull { !prefs.getBoolean("done_${it.id}", false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { BrandHeader() }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Text("Hi Gaurav 👋", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text("Keep learning. Keep building.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
        item {
            Card(
                modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFFECF8FF), Color(0xFFDCEEFF), Color(0xFFF6F0FF))).padding(20.dp)) {
                    Column {
                        Text("LEARN. PRACTICE. BUILD. GROW.", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Blue)
                        Spacer(Modifier.height(5.dp))
                        Text("Your path to real\nengineering skills.", fontSize = 26.sp, lineHeight = 31.sp, fontWeight = FontWeight.Black, color = Navy)
                        Spacer(Modifier.height(6.dp))
                        Text("AI-powered guidance that makes you build, debug and prove what you know.", fontSize = 12.sp, color = Color(0xFF475569))
                        Spacer(Modifier.height(15.dp))
                        Button(onClick = { next?.let { onSkill(it.id) } }, enabled = next != null, colors = ButtonDefaults.buttonColors(containerColor = Blue), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(5.dp))
                            Text(if (next == null) "Academy complete" else "Continue learning")
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Your learning path", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Navy, modifier = Modifier.weight(1f))
                    Text("$done/${skills.size}", fontSize = 12.sp, color = Blue, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { done.toFloat() / skills.size }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Blue, trackColor = Color(0xFFDDE8F5))
            }
        }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { FeatureTile(Icons.Default.Psychology, "AI Teacher", "Learn concepts", SoftPurple) }
                item { FeatureTile(Icons.Default.Code, "Coding Lab", "Hands-on practice", SoftBlue) }
                item { FeatureTile(Icons.Default.BugReport, "Debug Coach", "Fix errors", SoftOrange) }
                item { FeatureTile(Icons.Default.RateReview, "Code Review", "Senior feedback", SoftGreen) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Text("Today", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Navy)
                Spacer(Modifier.height(8.dp))
                MissionCard(next, onSkill)
            }
        }
        item {
            OutlinedButton(onClick = onLearn, modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Explore the full academy")
                Spacer(Modifier.width(5.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
        }
    }
}

@Composable
private fun FeatureTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, background: Color) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = background), modifier = Modifier.width(145.dp)) {
        Column(Modifier.padding(13.dp)) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Blue) }
            Spacer(Modifier.height(9.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun MissionCard(next: Skill?, onSkill: (String) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(SoftBlue), contentAlignment = Alignment.Center) { Icon(Icons.Default.Star, null, tint = Blue) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("NEXT MISSION", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Blue)
                    Text(next?.title ?: "All missions complete", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
            }
            if (next != null) {
                Spacer(Modifier.height(9.dp))
                Text(next.mission, fontSize = 12.sp, color = Color(0xFF475569))
                Spacer(Modifier.height(11.dp))
                Button(onClick = { onSkill(next.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Start mission") }
            }
        }
    }
}

@Composable
private fun LearnScreen(onSkill: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val categories = listOf("All", "Programming", "Backend", "Professional", "AI Development", "Cybersecurity")
    var selectedCategory by remember { mutableStateOf("All") }
    val filtered = skills.filter { (selectedCategory == "All" || it.track == selectedCategory) && (query.isBlank() || it.title.contains(query, true)) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BrandHeader() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("Learn", fontSize = 27.sp, fontWeight = FontWeight.Black, color = Navy); Text("Build professional skills step by step.", fontSize = 12.sp, color = Color(0xFF64748B)); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Search skills…") }, shape = RoundedCornerShape(14.dp)) } }
        item { Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { categories.forEach { category -> FilterChip(category, selectedCategory == category) { selectedCategory = category } } } }
        items(filtered) { skill -> SkillCard(skill, onSkill) }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), color = if (selected) Blue else Color.White, modifier = Modifier.clickable(onClick = onClick)) {
        Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), fontSize = 11.sp, color = if (selected) Color.White else Color(0xFF475569), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SkillCard(skill: Skill, onSkill: (String) -> Unit) {
    val icon = when (skill.track) { "Cybersecurity" -> Icons.Default.Security; "Professional" -> Icons.Default.Build; "Backend" -> Icons.Default.Code; else -> Icons.Default.School }
    Card(modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth().clickable { onSkill(skill.id) }, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(45.dp).clip(RoundedCornerShape(13.dp)).background(SoftBlue), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Blue) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${skill.track} • ${skill.level}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Blue)
                Text(skill.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text(skill.concept, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 2)
            }
            Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun TrainingScreen(skill: Skill, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("pathpilot_engine", Context.MODE_PRIVATE) }
    var phase by remember { mutableStateOf(Phase.TEACH) }
    var work by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(prefs.getInt("attempts_${skill.id}", 0)) }

    fun ask(prompt: String) {
        busy = true
        scope.launch {
            feedback = GeminiMentorService.askGeminiMentor(context, prompt, "Skill=${skill.title}; track=${skill.track}; level=${skill.level}; attempts=$attempts; phase=${phase.name}")
            busy = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) {
                Text(skill.title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Navy)
                Text("${skill.track} • ${skill.level}", fontSize = 10.sp, color = Blue)
            }
            Box(Modifier.padding(end = 12.dp).clip(RoundedCornerShape(20.dp)).background(SoftBlue)) { Text("${attempts} tries", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Bold) }
        }
        ScrollableTabRow(selectedTabIndex = phase.ordinal, edgePadding = 16.dp, containerColor = Color.Transparent, divider = {}) {
            listOf("Learn", "Lab", "Build", "Debug", "Review").forEachIndexed { i, label -> Tab(selected = phase.ordinal == i, onClick = { phase = Phase.values()[i] }, text = { Text(label) }) }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            when (phase) {
                Phase.TEACH -> {
                    item { LessonHero(skill) }
                    item { MentorAction("AI Teacher", "Teach this concept with a practical explanation, then give me a tiny exercise.", Icons.Default.Psychology, SoftPurple) { ask("Teach ${skill.concept} for ${skill.title} practically. Ask one thinking question and one tiny exercise. Do not dump the complete solution.") } }
                    item { FeedbackCard(feedback) }
                    item { PrimaryAction("Enter coding lab") { phase = Phase.LAB } }
                }
                Phase.LAB -> {
                    item { MissionPanel("LAB TASK", skill.mission, "Produce the work yourself. AI gives hints before solutions.") }
                    item { WorkBox(work, { work = it }, "Code / reasoning / output") }
                    item { PrimaryAction(if (busy) "Reviewing…" else "Submit to AI mentor", work.isNotBlank() && !busy) { attempts++; prefs.edit().putInt("attempts_${skill.id}", attempts).apply(); ask("Review this lab attempt. Identify what is correct, one highest-value gap, and a useful hint. Do not give the complete answer. Mission: ${skill.mission}\nAttempt:\n$work") } }
                    item { FeedbackCard(feedback) }
                    item { SecondaryAction("Move to build") { phase = Phase.BUILD } }
                }
                Phase.BUILD -> {
                    item { MissionPanel("BUILD CHALLENGE", skill.build, "Turn the concept into something that could live in a real project.") }
                    item { WorkBox(work, { work = it }, "Implementation / architecture / tests") }
                    item { PrimaryAction(if (busy) "Reviewing…" else "Run AI code review", work.isNotBlank() && !busy) { ask("Act as a senior code reviewer. Review this implementation for correctness, maintainability, edge cases and tests. Give one prioritized improvement and one test to add. Do not rewrite everything.\nWork:\n$work") } }
                    item { FeedbackCard(feedback) }
                    item { SecondaryAction("Break it → debug") { phase = Phase.DEBUG } }
                }
                Phase.DEBUG -> {
                    item { MissionPanel("DEBUG CHALLENGE", skill.debug, "Use symptom → evidence → hypothesis → test → root cause → fix → regression test.") }
                    item { ErrorCard() }
                    item { WorkBox(work, { work = it }, "Logs / error / symptoms / what you tried") }
                    item { PrimaryAction(if (busy) "Thinking…" else "Get debugging coaching", work.isNotBlank() && !busy) { ask("Be a debugging coach. Analyze the evidence. Ask one diagnostic question and give the smallest useful hint before the fix.\nEvidence:\n$work") } }
                    item { FeedbackCard(feedback) }
                    item { SecondaryAction("Final review") { phase = Phase.REVIEW } }
                }
                Phase.REVIEW -> {
                    item { MissionPanel("PROOF OF LEARNING", skill.review, "Mastery is based on evidence, not completion taps.") }
                    item { WorkBox(work, { work = it }, "Final explanation + evidence") }
                    item { PrimaryAction(if (busy) "Evaluating…" else "Evaluate mastery", work.isNotBlank() && !busy) { ask("Evaluate mastery from this evidence. Score understanding, implementation, debugging and proof from 0-100. If below 75 prescribe remediation; if 75+ prescribe a harder follow-up. Never claim mastery without evidence.\nEvidence:\n$work") } }
                    item { FeedbackCard(feedback) }
                    item { PrimaryAction("Complete skill", feedback.isNotBlank()) { prefs.edit().putBoolean("done_${skill.id}", true).apply(); onBack() } }
                }
            }
        }
    }
}

@Composable
private fun LessonHero(skill: Skill) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFFE8F6FF), Color(0xFFF1EEFF))).padding(20.dp))) {
            Text("LESSON • 1 / 12", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Blue)
            Spacer(Modifier.height(5.dp))
            Text("Build skills you can actually use.", fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black, color = Navy)
            Spacer(Modifier.height(6.dp))
            Text(skill.concept, fontSize = 12.sp, color = Color(0xFF475569))
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { 0.25f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Blue, trackColor = Color.White)
        }
    }
}

@Composable
private fun MissionPanel(title: String, body: String, footer: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(17.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Blue)
            Spacer(Modifier.height(5.dp))
            Text(body, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(7.dp))
            Text(footer, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun MentorAction(title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector, background: Color, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = background), modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Blue) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, color = Navy); Text(body, fontSize = 11.sp, color = Color(0xFF475569)) }
            Icon(Icons.Default.ArrowForward, null, tint = Blue)
        }
    }
}

@Composable
private fun ErrorCard() {
    Card(shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))) {
        Column(Modifier.padding(16.dp)) {
            Text("ERROR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF6B6B))
            Spacer(Modifier.height(5.dp))
            Text("Cannot find module 'express'\n    at Module._resolveFilename\n    at Module._load", fontSize = 12.sp, color = Color(0xFFF8FAFC), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        }
    }
}

@Composable
private fun WorkBox(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), minLines = 6, label = { Text(label) }, shape = RoundedCornerShape(15.dp))
}

@Composable
private fun FeedbackCard(text: String) {
    if (text.isNotBlank()) {
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SoftGreen)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF0E9F6E)); Spacer(Modifier.width(7.dp)); Text("AI mentor feedback", fontWeight = FontWeight.Black, color = Navy) }
                Spacer(Modifier.height(7.dp)); Text(text, fontSize = 12.sp, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
private fun PrimaryAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Text(text, fontWeight = FontWeight.Bold); Spacer(Modifier.width(5.dp)); Icon(Icons.Default.ArrowForward, null) }
}

@Composable
private fun SecondaryAction(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
private fun MentorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { BrandHeader() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("AI Mentor", fontSize = 27.sp, fontWeight = FontWeight.Black, color = Navy); Text("Your teacher, senior developer and debugging coach.", fontSize = 12.sp, color = Color(0xFF64748B)) } }
        item { MentorAction("Ask a teacher", "Understand a concept without being handed the answer.", Icons.Default.Lightbulb, SoftPurple) {} }
        item { MentorAction("Debug with me", "Bring logs, errors and what you already tried.", Icons.Default.BugReport, SoftOrange) {} }
        item { Column(Modifier.padding(horizontal = 18.dp)) { WorkBox(question, { question = it }, "Ask anything about your learning…"); Spacer(Modifier.height(9.dp)); PrimaryAction(if (busy) "Thinking…" else "Ask mentor", question.isNotBlank() && !busy) { busy = true; scope.launch { answer = GeminiMentorService.askGeminiMentor(context, question, "Adaptive engineering learner"); busy = false } }; Spacer(Modifier.height(10.dp)); FeedbackCard(answer) } }
    }
}

@Composable
private fun CollegeScreen() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { BrandHeader() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("College Mode", fontSize = 27.sp, fontWeight = FontWeight.Black, color = Navy); Text("Syllabus → PYQs → practice → revision.", fontSize = 12.sp, color = Color(0xFF64748B)) } }
        item { StatsRow() }
        item { MissionPanel("IMPORTANT QUESTIONS", "Rank questions as Very High / High / Medium / Low using evidence from syllabus, PYQs, assignments and teacher material.", "No fake 99% prediction. Rankings are evidence-based, never guarantees.") }
        item { MissionPanel("ADAPTIVE DAILY PLAN", "Balance college, commute, coding, cybersecurity, assignments and revision. Missed work gets rebalanced.", "The goal is the next useful action, not a giant timetable.") }
        item { PrimaryAction("Open study planner") {} }
    }
}

@Composable
private fun StatsRow() {
    Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        StatBox("0h", "Today", SoftBlue)
        StatBox("0", "Questions", SoftPurple)
        StatBox("0%", "Mastery", SoftGreen)
    }
}

@Composable
private fun StatBox(value: String, label: String, background: Color) {
    Card(Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = background)) { Column(Modifier.padding(13.dp)) { Text(value, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Navy); Text(label, fontSize = 10.sp, color = Color(0xFF64748B)) } }
}
