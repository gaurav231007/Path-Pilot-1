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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private val PPBlue = Color(0xFF087BFF)
private val PPCyan = Color(0xFF11C9E9)
private val PPNavy = Color(0xFF111A32)
private val PPText = Color(0xFF475569)
private val PPBlueSoft = Color(0xFFEAF5FF)
private val PPPurpleSoft = Color(0xFFF0ECFF)
private val PPGreenSoft = Color(0xFFE9FBF2)
private val PPOrangeSoft = Color(0xFFFFF4DF)

private data class LearningSkill(val id: String, val category: String, val title: String, val level: String, val concept: String, val mission: String, val build: String, val debug: String, val review: String)

private val learningSkills = listOf(
    LearningSkill("python", "Programming", "Python Programming", "Beginner", "Variables, conditions, loops, functions and errors", "Build a validator that rejects empty input and invalid numbers.", "Turn it into a reusable CLI utility with tests.", "Trace a broken condition from symptom to root cause.", "Explain the fix and add three regression tests."),
    LearningSkill("http", "Backend", "HTTP Request to Response", "Beginner", "Methods, headers, status codes, JSON and request flow", "Inspect GET and POST requests and explain the important fields.", "Build a small API client handling 2xx, 4xx and 5xx responses.", "Diagnose 400, 404 and 500 responses using evidence.", "Explain the request lifecycle and validation boundary."),
    LearningSkill("fastapi", "Backend", "FastAPI REST API", "Intermediate", "Routes, schemas, validation, errors and API contracts", "Create CRUD endpoints for tasks.", "Build a notes API with validation and clean errors.", "Debug route-not-found and validation failures.", "Write endpoint tests and document the contract."),
    LearningSkill("sql", "Backend", "SQL + PostgreSQL", "Intermediate", "CRUD, joins, constraints, indexes and transactions", "Write queries for users, projects and tasks.", "Connect an API to PostgreSQL or Supabase.", "Find the cause of a bad query or constraint violation.", "Explain how the schema prevents inconsistent data."),
    LearningSkill("git", "Professional", "Git & GitHub Workflow", "Beginner", "Requirements, branches, commits, PRs, reviews and CI", "Turn a feature request into acceptance criteria and a branch plan.", "Implement it with focused commits and tests.", "Read a failing CI check and find useful evidence.", "Write a PR description a teammate can review."),
    LearningSkill("debug", "Professional", "Production Debugging", "Intermediate", "Logs, reproduction, tests, observability and postmortems", "Turn a bug report into a minimal reproduction.", "Create a regression test and structured error handling.", "Use logs and tests to isolate root cause.", "Write a short incident note."),
    LearningSkill("llm", "AI Development", "Build with LLM APIs", "Beginner", "Prompts, structured output, errors, latency and cost", "Call an LLM and safely parse structured output.", "Build a bounded AI feature inside a normal application.", "Handle timeout, invalid output and API errors.", "Explain which decisions must stay deterministic."),
    LearningSkill("linux", "Cybersecurity", "Linux + Networking Lab", "Beginner", "Processes, permissions, ports, DNS, TCP/IP, HTTP and logs", "Inspect a service in your own lab and explain its ports.", "Run and observe a local service.", "Diagnose a permission, port or DNS problem safely.", "Keep a lab notebook with commands and evidence."),
    LearningSkill("websec", "Cybersecurity", "Web Security Testing", "Intermediate", "Scope, authorization, threat modeling and remediation", "Write scope and trust boundaries before testing.", "Test an intentionally vulnerable local lab only.", "Document a finding with evidence, impact and remediation.", "Retest after the fix and compare evidence."),
    LearningSkill("capstone", "Capstone", "Production-Style AI Product", "Advanced", "Requirements, architecture, backend, DB, AI, tests, Git and security", "Write a one-page spec and acceptance criteria before coding.", "Build the product in small milestones.", "Handle a real bug, failed test and security finding.", "Review like a senior engineer and write a postmortem.")
)

private enum class LearningPhase { LEARN, LAB, BUILD, DEBUG, REVIEW }

@Composable
fun PathPilotPremiumApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<LearningSkill?>(null) }
    BackHandler(enabled = selected != null) { selected = null }
    Surface(Modifier.fillMaxSize(), color = Color(0xFFF8FBFF)) {
        if (selected != null) SkillJourney(selected!!) { selected = null } else MainShell(tab, { tab = it }) { selected = it }
    }
}

@Composable
private fun MainShell(tab: Int, onTab: (Int) -> Unit, onSkill: (LearningSkill) -> Unit) {
    val labels = listOf("Home", "Learn", "Mentor", "College")
    val icons = listOf(Icons.Default.Home, Icons.Default.Code, Icons.Default.Psychology, Icons.Default.School)
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                0 -> HomePage(onSkill) { onTab(1) }
                1 -> LearnPage(onSkill)
                2 -> MentorPage()
                else -> CollegePage()
            }
        }
        NavigationBar(containerColor = Color.White) {
            labels.forEachIndexed { index, label ->
                NavigationBarItem(selected = tab == index, onClick = { onTab(index) }, icon = { Icon(icons[index], label) }, label = { Text(label) })
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(43.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(PPCyan, PPBlue))), contentAlignment = Alignment.Center) { Text("➤", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row { Text("Path", fontSize = 23.sp, fontWeight = FontWeight.Black, color = PPNavy); Text("Pilot", fontSize = 23.sp, fontWeight = FontWeight.Black, color = PPBlue) }
            Text("AI Engineering Academy", fontSize = 10.sp, color = Color(0xFF64748B))
        }
        IconButton(onClick = {}) { Icon(Icons.Default.Notifications, "Notifications") }
        Box(Modifier.size(36.dp).clip(CircleShape).background(PPBlueSoft), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, "Profile", tint = PPBlue) }
    }
}

@Composable
private fun HomePage(onSkill: (LearningSkill) -> Unit, onLearn: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pathpilot_engine", Context.MODE_PRIVATE) }
    val completed = learningSkills.count { prefs.getBoolean("done_${it.id}", false) }
    val next = learningSkills.firstOrNull { !prefs.getBoolean("done_${it.id}", false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { BrandHeader() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("Hi Gaurav 👋", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PPNavy); Text("Keep learning. Keep building.", fontSize = 12.sp, color = Color(0xFF64748B)) } }
        item { HeroCard(next, onSkill) }
        item { ProgressCard(completed) }
        item { FeatureStrip() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("Today's mission", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PPNavy); Spacer(Modifier.height(8.dp)); MissionCard(next, onSkill) } }
        item { OutlinedButton(onClick = onLearn, modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Explore the full academy"); Spacer(Modifier.width(5.dp)); Icon(Icons.Default.ArrowForward, null) } }
    }
}

@Composable
private fun HeroCard(next: LearningSkill?, onSkill: (LearningSkill) -> Unit) {
    Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFFEAF8FF), Color(0xFFDDEEFF), Color(0xFFF2EEFF))).padding(20.dp))) {
            Column {
                Text("LEARN. PRACTICE. BUILD. GROW.", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PPBlue)
                Spacer(Modifier.height(5.dp))
                Text("Your path to real\nengineering skills.", fontSize = 26.sp, lineHeight = 31.sp, fontWeight = FontWeight.Black, color = PPNavy)
                Spacer(Modifier.height(6.dp))
                Text("AI-powered guidance that makes you build, debug and prove what you know.", fontSize = 12.sp, color = PPText)
                Spacer(Modifier.height(15.dp))
                Button(onClick = { next?.let(onSkill) }, enabled = next != null, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = PPBlue)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text(if (next == null) "Academy complete" else "Continue learning") }
            }
        }
    }
}

@Composable
private fun ProgressCard(completed: Int) {
    Column(Modifier.padding(horizontal = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Your learning path", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PPNavy, modifier = Modifier.weight(1f)); Text("$completed/${learningSkills.size}", fontSize = 12.sp, color = PPBlue, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(progress = { completed.toFloat() / learningSkills.size }, Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = PPBlue, trackColor = Color(0xFFDDE8F5))
    }
}

@Composable
private fun FeatureStrip() {
    LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { FeatureTile(Icons.Default.Psychology, "AI Teacher", "Learn concepts", PPPurpleSoft) }
        item { FeatureTile(Icons.Default.Code, "Coding Lab", "Hands-on practice", PPBlueSoft) }
        item { FeatureTile(Icons.Default.BugReport, "Debug Coach", "Fix errors", PPOrangeSoft) }
        item { FeatureTile(Icons.Default.RateReview, "Code Review", "Senior feedback", PPGreenSoft) }
    }
}

@Composable
private fun FeatureTile(icon: ImageVector, title: String, subtitle: String, background: Color) {
    Card(Modifier.width(145.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Column(Modifier.padding(13.dp)) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), contentAlignment = Alignment.Center) { Icon(icon, null, tint = PPBlue) }
            Spacer(Modifier.height(9.dp)); Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PPNavy); Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun MissionCard(next: LearningSkill?, onSkill: (LearningSkill) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(PPBlueSoft), contentAlignment = Alignment.Center) { Icon(Icons.Default.Star, null, tint = PPBlue) }
                Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("NEXT MISSION", fontSize = 9.sp, fontWeight = FontWeight.Black, color = PPBlue); Text(next?.title ?: "All missions complete", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PPNavy) }
            }
            if (next != null) { Spacer(Modifier.height(9.dp)); Text(next.mission, fontSize = 12.sp, color = PPText); Spacer(Modifier.height(11.dp)); Button(onClick = { onSkill(next) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Start mission") } }
        }
    }
}

@Composable
private fun LearnPage(onSkill: (LearningSkill) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All", "Programming", "Backend", "Professional", "AI Development", "Cybersecurity")
    val filtered = learningSkills.filter { (category == "All" || it.category == category) && (query.isBlank() || it.title.contains(query, true)) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BrandHeader() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("Learn", fontSize = 27.sp, fontWeight = FontWeight.Black, color = PPNavy); Text("Build professional skills step by step.", fontSize = 12.sp, color = Color(0xFF64748B)); Spacer(Modifier.height(12.dp)); OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Search skills…") }, shape = RoundedCornerShape(14.dp)) } }
        item { Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { categories.forEach { Chip(it, category == it) { category = it } } } }
        items(filtered) { SkillListItem(it, onSkill) }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), color = if (selected) PPBlue else Color.White) { Text(text, Modifier.padding(horizontal = 14.dp, vertical = 9.dp), fontSize = 11.sp, color = if (selected) Color.White else PPText, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun SkillListItem(skill: LearningSkill, onSkill: (LearningSkill) -> Unit) {
    val icon = when (skill.category) { "Cybersecurity" -> Icons.Default.Security; "Professional" -> Icons.Default.Build; "Backend" -> Icons.Default.Code; else -> Icons.Default.School }
    Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth().clickable { onSkill(skill) }, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(45.dp).clip(RoundedCornerShape(13.dp)).background(PPBlueSoft), contentAlignment = Alignment.Center) { Icon(icon, null, tint = PPBlue) }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("${skill.category} • ${skill.level}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = PPBlue); Text(skill.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PPNavy); Text(skill.concept, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 2) }; Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun SkillJourney(skill: LearningSkill, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("pathpilot_engine", Context.MODE_PRIVATE) }
    var phase by remember { mutableStateOf(LearningPhase.LEARN) }
    var work by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(prefs.getInt("attempts_${skill.id}", 0)) }
    fun ask(prompt: String) { busy = true; scope.launch { feedback = GeminiMentorService.askGeminiMentor(context, prompt, "Skill=${skill.title}; level=${skill.level}; attempts=$attempts; phase=${phase.name}"); busy = false } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Column(Modifier.weight(1f)) { Text(skill.title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = PPNavy); Text("${skill.category} • ${skill.level}", fontSize = 10.sp, color = PPBlue) }
            Surface(shape = RoundedCornerShape(20.dp), color = PPBlueSoft) { Text("$attempts tries", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 10.sp, color = PPBlue, fontWeight = FontWeight.Bold) }
        }
        ScrollableTabRow(selectedTabIndex = phase.ordinal, edgePadding = 16.dp, containerColor = Color.Transparent, divider = {}) { listOf("Learn", "Lab", "Build", "Debug", "Review").forEachIndexed { i, label -> Tab(phase.ordinal == i, { phase = LearningPhase.values()[i] }, text = { Text(label) }) } }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            when (phase) {
                LearningPhase.LEARN -> { item { LessonHero(skill) }; item { CoachCard("AI Teacher", "Understand the concept, then solve a tiny exercise yourself.", Icons.Default.Lightbulb, PPPurpleSoft) { ask("Teach ${skill.concept} for ${skill.title} practically. Ask one thinking question and one tiny exercise. Do not dump the complete solution.") } }; item { Feedback(feedback) }; item { Primary("Enter coding lab") { phase = LearningPhase.LAB } } }
                LearningPhase.LAB -> { item { MissionPanel("LAB TASK", skill.mission, "Produce the work yourself. AI gives hints before solutions.") }; item { WorkBox(work, { work = it }, "Code / reasoning / output") }; item { Primary(if (busy) "Reviewing…" else "Submit to AI mentor", work.isNotBlank() && !busy) { attempts++; prefs.edit().putInt("attempts_${skill.id}", attempts).apply(); ask("Review this lab attempt. Identify what is correct, one highest-value gap, and a useful hint. Do not give the complete answer. Mission: ${skill.mission}\nAttempt:\n$work") } }; item { Feedback(feedback) }; item { Secondary("Move to build") { phase = LearningPhase.BUILD } } }
                LearningPhase.BUILD -> { item { MissionPanel("BUILD CHALLENGE", skill.build, "Turn the concept into something that could live in a real project.") }; item { WorkBox(work, { work = it }, "Implementation / architecture / tests") }; item { Primary(if (busy) "Reviewing…" else "Run AI code review", work.isNotBlank() && !busy) { ask("Act as a senior code reviewer. Review this implementation for correctness, maintainability, edge cases and tests. Give one prioritized improvement and one test to add. Do not rewrite everything.\nWork:\n$work") } }; item { Feedback(feedback) }; item { Secondary("Break it → debug") { phase = LearningPhase.DEBUG } } }
                LearningPhase.DEBUG -> { item { MissionPanel("DEBUG CHALLENGE", skill.debug, "Use symptom → evidence → hypothesis → test → root cause → fix → regression test.") }; item { ErrorPanel() }; item { WorkBox(work, { work = it }, "Logs / error / symptoms / what you tried") }; item { Primary(if (busy) "Thinking…" else "Get debugging coaching", work.isNotBlank() && !busy) { ask("Be a debugging coach. Analyze the evidence. Ask one diagnostic question and give the smallest useful hint before the fix.\nEvidence:\n$work") } }; item { Feedback(feedback) }; item { Secondary("Final review") { phase = LearningPhase.REVIEW } } }
                LearningPhase.REVIEW -> { item { MissionPanel("PROOF OF LEARNING", skill.review, "Mastery is based on evidence, not completion taps.") }; item { WorkBox(work, { work = it }, "Final explanation + evidence") }; item { Primary(if (busy) "Evaluating…" else "Evaluate mastery", work.isNotBlank() && !busy) { ask("Evaluate mastery from this evidence. Score understanding, implementation, debugging and proof from 0-100. If below 75 prescribe remediation; if 75+ prescribe a harder follow-up. Never claim mastery without evidence.\nEvidence:\n$work") } }; item { Feedback(feedback) }; item { Primary("Complete skill", feedback.isNotBlank()) { prefs.edit().putBoolean("done_${skill.id}", true).apply(); onBack() } } }
            }
        }
    }
}

@Composable
private fun LessonHero(skill: LearningSkill) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFFE8F6FF), Color(0xFFF1EEFF))).padding(20.dp))) {
            Column { Text("LESSON • 1 / 12", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue); Spacer(Modifier.height(5.dp)); Text("Build skills you can actually use.", fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Black, color = PPNavy); Spacer(Modifier.height(6.dp)); Text(skill.concept, fontSize = 12.sp, color = PPText); Spacer(Modifier.height(12.dp)); LinearProgressIndicator({ 0.25f }, Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = PPBlue, trackColor = Color.White) }
        }
    }
}

@Composable
private fun CoachCard(title: String, body: String, icon: ImageVector, background: Color, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White), contentAlignment = Alignment.Center) { Icon(icon, null, tint = PPBlue) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, color = PPNavy); Text(body, fontSize = 11.sp, color = PPText) }; Icon(Icons.Default.ArrowForward, null, tint = PPBlue) }
    }
}

@Composable
private fun MissionPanel(title: String, body: String, footer: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(17.dp)) { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue); Spacer(Modifier.height(5.dp)); Text(body, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PPNavy); Spacer(Modifier.height(7.dp)); Text(footer, fontSize = 11.sp, color = Color(0xFF64748B)) } }
}

@Composable
private fun ErrorPanel() {
    Card(shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))) { Column(Modifier.padding(16.dp)) { Text("ERROR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF6B6B)); Spacer(Modifier.height(5.dp)); Text("Cannot find module 'express'\n  at Module._resolveFilename\n  at Module._load", fontSize = 12.sp, color = Color.White, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) } }
}

@Composable
private fun WorkBox(value: String, onChange: (String) -> Unit, label: String) { OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), minLines = 6, label = { Text(label) }, shape = RoundedCornerShape(15.dp)) }

@Composable
private fun Feedback(text: String) { if (text.isNotBlank()) Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PPGreenSoft)) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF0E9F6E)); Spacer(Modifier.width(7.dp)); Text("AI mentor feedback", fontWeight = FontWeight.Black, color = PPNavy) }; Spacer(Modifier.height(7.dp)); Text(text, fontSize = 12.sp, color = Color(0xFF334155)) } } }

@Composable
private fun Primary(text: String, enabled: Boolean = true, onClick: () -> Unit) { Button(onClick, enabled = enabled, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = PPBlue)) { Text(text, fontWeight = FontWeight.Bold); Spacer(Modifier.width(5.dp)); Icon(Icons.Default.ArrowForward, null) } }

@Composable
private fun Secondary(text: String, onClick: () -> Unit) { OutlinedButton(onClick, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text(text, fontWeight = FontWeight.Bold) } }

@Composable
private fun MentorPage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { BrandHeader() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("AI Mentor", fontSize = 27.sp, fontWeight = FontWeight.Black, color = PPNavy); Text("Teacher + senior developer + debugging coach.", fontSize = 12.sp, color = Color(0xFF64748B)) } }
        item { CoachCard("Ask a teacher", "Understand concepts without being handed the answer.", Icons.Default.Lightbulb, PPPurpleSoft) {} }
        item { CoachCard("Debug with me", "Bring logs, errors and what you already tried.", Icons.Default.BugReport, PPOrangeSoft) {} }
        item { Column(Modifier.padding(horizontal = 18.dp)) { WorkBox(question, { question = it }, "Ask anything about your learning…"); Spacer(Modifier.height(9.dp)); Primary(if (busy) "Thinking…" else "Ask mentor", question.isNotBlank() && !busy) { busy = true; scope.launch { answer = GeminiMentorService.askGeminiMentor(context, question, "Adaptive engineering learner"); busy = false } }; Spacer(Modifier.height(10.dp)); Feedback(answer) } }
    }
}

@Composable
private fun CollegePage() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { BrandHeader() }
        item { Column(Modifier.padding(horizontal = 18.dp)) { Text("College Mode", fontSize = 27.sp, fontWeight = FontWeight.Black, color = PPNavy); Text("Syllabus → PYQs → practice → revision.", fontSize = 12.sp, color = Color(0xFF64748B)) } }
        item { Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) { StatBox("0h", "Today", PPBlueSoft); StatBox("0", "Questions", PPPurpleSoft); StatBox("0%", "Mastery", PPGreenSoft) } }
        item { MissionPanel("IMPORTANT QUESTIONS", "Rank Very High / High / Medium / Low using evidence from syllabus, PYQs, assignments and teacher material.", "No fake 99% prediction. Rankings are evidence-based, never guarantees.") }
        item { MissionPanel("ADAPTIVE DAILY PLAN", "Balance college, commute, coding, cybersecurity, assignments and revision. Missed work gets rebalanced.", "The goal is the next useful action, not a giant timetable.") }
        item { Primary("Open study planner") {} }
    }
}

@Composable
private fun RowScope.StatBox(value: String, label: String, background: Color) {
    Card(Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = background)) { Column(Modifier.padding(13.dp)) { Text(value, fontSize = 19.sp, fontWeight = FontWeight.Black, color = PPNavy); Text(label, fontSize = 10.sp, color = Color(0xFF64748B)) } }
}
