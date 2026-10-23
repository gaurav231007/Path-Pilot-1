package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PathPilotViewModel

private val FBlue = Color(0xFF087BFF)
private val FNavy = Color(0xFF111A32)
private val FGray = Color(0xFF64748B)
private val FSoft = Color(0xFFEAF5FF)

private data class GitMission(val title: String, val goal: String, val expected: List<String>, val hint: String)
private val gitMissions = listOf(
    GitMission("01 • Read the ticket", "Convert a feature request into acceptance criteria.", listOf("done"), "Write 2–3 acceptance criteria before touching code."),
    GitMission("02 • Create the branch", "Create an isolated feature branch.", listOf("git switch -c feature/login-validation", "git checkout -b feature/login-validation"), "Use a feature/ branch. Never build a feature directly on main."),
    GitMission("03 • Inspect the worktree", "Check what changed before staging.", listOf("git status"), "Run git status and understand every changed file."),
    GitMission("04 • Stage intentionally", "Stage only the files belonging to this ticket.", listOf("git add .", "git add"), "Avoid blindly staging unrelated secrets or generated files."),
    GitMission("05 • Make a useful commit", "Create a small, descriptive commit.", listOf("git commit -m \"feat: add login validation\"", "git commit -m \"feat: add login validation\""), "Commit messages should describe the change, not 'final' or 'changes'."),
    GitMission("06 • Inspect history", "Verify the commit and understand the diff.", listOf("git log", "git show"), "Use git log and git show before sharing work."),
    GitMission("07 • Push the branch", "Publish your feature branch.", listOf("git push origin feature/login-validation", "git push"), "Push the branch, not random generated files."),
    GitMission("08 • Open a PR", "Turn the work into a reviewable pull request.", listOf("open pr", "pr"), "Include summary, screenshots, tests and known limitations."),
    GitMission("09 • Survive CI", "Read a failed check and isolate the root cause.", listOf("fix ci", "done"), "Do not guess. Read the failing test/log, reproduce, fix and rerun."),
    GitMission("10 • Review + merge", "Respond to review comments and merge only after checks pass.", listOf("merge", "done"), "Review the diff, resolve comments, confirm CI, then merge."),
)

private data class ProjectPlan(val title: String, val stack: String, val brief: String, val phases: List<String>)
private val projects = listOf(
    ProjectPlan("Expense Manager", "React + FastAPI + PostgreSQL", "A production-style expense tracker with auth, categories, reports and an API.", listOf("Requirements + acceptance criteria", "UI + component structure", "REST API + validation", "PostgreSQL schema + queries", "Auth + authorization", "Tests + regression bug", "Git branch + PR + review", "Docker + deployment", "Security review + README")),
    ProjectPlan("AI Support Agent", "Next.js + FastAPI + Gemini", "A bounded AI support assistant with structured answers, tool calls and human handoff.", listOf("Product spec + safety boundaries", "Chat UI + API contract", "LLM structured output", "Tool calling + approval gates", "Evaluation set + failure cases", "Logs + retries + rate limits", "Security + prompt-injection tests", "Deployment + monitoring", "Final demo + postmortem")),
    ProjectPlan("Automation CRM", "n8n + APIs + Google Sheets", "A real agency-style lead pipeline from form submission to qualification, follow-up and reporting.", listOf("Client discovery + process map", "Webhook + JSON", "Lead routing", "CRM + follow-up", "WhatsApp/Telegram notification design", "Error handling + idempotency", "Monitoring + runbook", "Client handover", "ROI report + retainer proposal")),
    ProjectPlan("Secure Web Lab", "Local vulnerable app + Linux", "An authorized security lab where you find, document, fix and retest web issues.", listOf("Scope + authorization", "Recon + threat model", "Evidence collection", "Controlled vulnerability test", "Risk rating", "Remediation", "Retest", "Security report", "Lessons + defensive checklist")),
)

@Composable
fun PathPilotFullApp(viewModel: PathPilotViewModel) {
    var mode by remember { mutableStateOf("academy") }
    var showGit by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    BackHandler(enabled = showGit || showProjects) { showGit = false; showProjects = false }
    Box(Modifier.fillMaxSize()) {
        PathPilotAppV4(viewModel)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp), horizontalArrangement = Arrangement.End) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = .96f), shadowElevation = 5.dp) {
                Row(Modifier.padding(5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    QuickAction(Icons.Default.Terminal, "Git") { showGit = true; showProjects = false }
                    QuickAction(Icons.Default.Folder, "Projects") { showProjects = true; showGit = false }
                }
            }
        }
        if (showGit) GitLab { showGit = false }
        if (showProjects) ProjectRoom { showProjects = false }
    }
}

@Composable private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), color = FSoft) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, label, tint = FBlue, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FNavy)
        }
    }
}

@Composable private fun GitLab(onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var command by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val current = gitMissions[step]
    val progress = step.toFloat() / gitMissions.size
    Surface(Modifier.fillMaxSize(), color = Color(0xFFF7FAFF)) {
        LazyColumn(contentPadding = PaddingValues(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { TopBar("Git + GitHub Practice", onBack) }
            item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(FNavy)) { Column(Modifier.padding(20.dp)) { Text("REAL DEVELOPER WORKFLOW", color=FSoft, fontSize=10.sp, fontWeight=FontWeight.Black); Text("Ticket → Branch → Code → Commit → PR → CI → Review → Merge", color=Color.White, fontSize=20.sp, fontWeight=FontWeight.Black); Spacer(Modifier.height(12.dp)); LinearProgressIndicator({ progress }, Modifier.fillMaxWidth(), color=FBlue, trackColor=Color(0xFF33415F)); Text("Step ${step + 1} of ${gitMissions.size}", color=Color(0xFFB9C6DC), fontSize=11.sp, modifier=Modifier.padding(top=6.dp)) } } }
            item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape=RoundedCornerShape(18.dp), colors=CardDefaults.cardColors(Color.White)) { Column(Modifier.padding(18.dp)) { Text(current.title, fontSize=19.sp, fontWeight=FontWeight.Black, color=FNavy); Text(current.goal, fontSize=13.sp, color=FGray, modifier=Modifier.padding(top=5.dp)); Text("Mentor hint: ${current.hint}", fontSize=11.sp, color=FBlue, modifier=Modifier.padding(top=10.dp)); if (current.expected.first() != "done") { Spacer(Modifier.height(12.dp)); OutlinedTextField(command, { command = it }, Modifier.fillMaxWidth(), singleLine=true, label={Text("Enter Git command")}, placeholder={Text("e.g. git status")}, leadingIcon={Icon(Icons.Default.Terminal, null)}); Spacer(Modifier.height(9.dp)); Button({ val normalized = command.trim().lowercase(); val ok = current.expected.any { normalized == it.lowercase() || (it == "git add" && normalized.startsWith("git add ")) }; message = if (ok) "✓ Correct. Think about why this command is used before continuing." else "Not quite. Read the hint, check the syntax and try again."; if (ok && step < gitMissions.lastIndex) step++ }, Modifier.fillMaxWidth(), shape=RoundedCornerShape(12.dp)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text("Run in practice terminal") } } else { Spacer(Modifier.height(12.dp)); Button({ if (step < gitMissions.lastIndex) step++; message="✓ Mission completed" }, Modifier.fillMaxWidth(), shape=RoundedCornerShape(12.dp)) { Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(5.dp)); Text("Mark complete") } } if (message.isNotBlank()) Text(message, fontSize=11.sp, color=if(message.startsWith("✓")) Color(0xFF138A52) else Color(0xFFC2410C), modifier=Modifier.padding(top=10.dp)) } } }
            item { Column(Modifier.padding(horizontal=16.dp)) { Text("What you are learning", fontSize=16.sp, fontWeight=FontWeight.Black, color=FNavy); Text("Git is not a theory chapter here. You will repeat the same workflow on every real project. Later, the same missions can be connected to an actual GitHub repository after GitHub authorization is added.", fontSize=12.sp, color=FGray, modifier=Modifier.padding(top=5.dp)) } }
            items(gitMissions) { m -> Row(Modifier.padding(horizontal=18.dp).fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) { Icon(if (m == current) Icons.Default.PlayArrow else Icons.Default.CheckCircle, null, tint=if(m==current)FBlue else Color(0xFF94A3B8), modifier=Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(m.title, fontSize=12.sp, color=if(m==current)FNavy else FGray, fontWeight=if(m==current)FontWeight.Bold else FontWeight.Normal) } }
        }
    }
}

@Composable private fun ProjectRoom(onBack: () -> Unit) {
    var selected by remember { mutableStateOf<ProjectPlan?>(null) }
    if (selected == null) {
        Surface(Modifier.fillMaxSize(), color=Color(0xFFF7FAFF)) { LazyColumn(contentPadding=PaddingValues(bottom=30.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) { item { TopBar("Real Project Room", onBack) }; item { Text("You don't just finish a lesson. You ship the project.", Modifier.padding(horizontal=18.dp), fontSize=22.sp, fontWeight=FontWeight.Black, color=FNavy) }; item { Text("Each project follows requirements → implementation → bugs → tests → Git → review → deployment → postmortem.", Modifier.padding(horizontal=18.dp), fontSize=12.sp, color=FGray) }; items(projects) { p -> ProjectCard(p) { selected=p } } } }
    } else ProjectDetail(selected!!){ selected=null }
}

@Composable private fun ProjectCard(p: ProjectPlan, onClick: () -> Unit) { Card(Modifier.padding(horizontal=16.dp).fillMaxWidth().clickable(onClick=onClick), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(Color.White)) { Column(Modifier.padding(18.dp)) { Row(verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Default.Code, null, tint=FBlue); Spacer(Modifier.width(8.dp)); Text(p.title, fontSize=17.sp, fontWeight=FontWeight.Black, color=FNavy) }; Text(p.stack, fontSize=10.sp, color=FBlue, fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=5.dp)); Text(p.brief, fontSize=12.sp, color=FGray, modifier=Modifier.padding(top=7.dp)); Text("${p.phases.size} production milestones", fontSize=10.sp, color=FGray, modifier=Modifier.padding(top=10.dp)); OutlinedButton(onClick, Modifier.fillMaxWidth().padding(top=8.dp), shape=RoundedCornerShape(12.dp)) { Text("Open project") } } } }

@Composable private fun ProjectDetail(p: ProjectPlan, onBack: () -> Unit) { var completed by remember { mutableStateOf(setOf<Int>()) }; val progress=completed.size.toFloat()/p.phases.size; Surface(Modifier.fillMaxSize(), color=Color(0xFFF7FAFF)) { LazyColumn(contentPadding=PaddingValues(bottom=30.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) { item { TopBar(p.title,onBack) }; item { Card(Modifier.padding(horizontal=16.dp).fillMaxWidth(), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(FSoft)) { Column(Modifier.padding(18.dp)) { Text(p.stack, fontSize=10.sp, color=FBlue, fontWeight=FontWeight.Bold); Text(p.brief, fontSize=14.sp, color=FNavy, fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=5.dp)); LinearProgressIndicator({progress}, Modifier.fillMaxWidth().padding(top=12.dp), color=FBlue) } } }; item { Text("Production milestones", Modifier.padding(horizontal=18.dp), fontSize=18.sp, fontWeight=FontWeight.Black, color=FNavy) }; items(p.phases.indices.toList()) { i -> val done=i in completed; Card(Modifier.padding(horizontal=16.dp).fillMaxWidth().clickable { completed = if(done) completed-i else completed+i }, shape=RoundedCornerShape(16.dp), colors=CardDefaults.cardColors(Color.White)) { Row(Modifier.padding(15.dp), verticalAlignment=Alignment.CenterVertically) { Icon(if(done) Icons.Default.CheckCircle else Icons.Default.Build, null, tint=if(done)Color(0xFF138A52) else FBlue); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)){Text("Milestone ${i+1}",fontSize=9.sp,color=FBlue,fontWeight=FontWeight.Bold);Text(p.phases[i],fontSize=13.sp,fontWeight=FontWeight.Bold,color=FNavy)}; Icon(Icons.Default.ArrowForward,null,tint=Color(0xFF94A3B8)) } } }; item { Card(Modifier.padding(horizontal=16.dp).fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(Color.White)){Column(Modifier.padding(16.dp)){Text("Definition of Done",fontWeight=FontWeight.Black,color=FNavy);Text("Code works • tests pass • Git history is clean • PR reviewed • deployment verified • security checked • README explains setup and decisions.",fontSize=12.sp,color=FGray,modifier=Modifier.padding(top=6.dp))}} } } } }

@Composable private fun TopBar(title: String, onBack: () -> Unit) { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment=Alignment.CenterVertically) { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back") }; Text(title, fontSize=19.sp, fontWeight=FontWeight.Black, color=FNavy) } }
