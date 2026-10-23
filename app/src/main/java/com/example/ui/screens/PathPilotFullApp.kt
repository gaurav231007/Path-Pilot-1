package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
private val Navy = Color(0xFF111A32)
private val Gray = Color(0xFF64748B)
private val Soft = Color(0xFFEAF5FF)
private val Green = Color(0xFFE9FBF2)
private val Purple = Color(0xFFF0ECFF)
private val Orange = Color(0xFFFFF4DF)

private val skills = listOf(
    "C", "C++", "Java", "Kotlin", "Python", "HTML5", "CSS", "JavaScript", "TypeScript",
    "React", "Next.js", "Node.js", "HTTP + REST", "FastAPI", "SQL + PostgreSQL",
    "Authentication", "Git + GitHub", "Testing + QA", "Docker + Deployment", "LLM Engineering",
    "Cybersecurity Labs", "System Design", "Automation Agency", "Marketing AI Agency",
    "Production AI Capstone"
)

private val projects = listOf(
    "Expense Manager • React + FastAPI + PostgreSQL",
    "AI Support Agent • Next.js + Gemini",
    "Automation CRM • n8n + APIs",
    "Secure Web Lab • local authorized lab"
)

private val gitSteps = listOf(
    "Read ticket + acceptance criteria",
    "git switch -c feature/login-validation",
    "git status",
    "git add .",
    "git commit -m \"feat: add login validation\"",
    "git log",
    "git push origin feature/login-validation",
    "Open Pull Request",
    "Read CI failure → fix → rerun",
    "Review → resolve comments → merge"
)

@Composable
fun PathPilotFullApp(viewModel: PathPilotViewModel) {
    var screen by remember { mutableStateOf("home") }

    BackHandler(enabled = screen != "home") {
        screen = "home"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7FAFF)
    ) {
        when (screen) {
            "home" -> Home(
                onLearn = { screen = "learn" },
                onProjects = { screen = "projects" },
                onGit = { screen = "git" },
                onMentor = { screen = "mentor" },
                onCollege = { screen = "college" }
            )
            "learn" -> Learn { screen = "home" }
            "projects" -> Projects { screen = "home" }
            "git" -> GitLab { screen = "home" }
            "mentor" -> Mentor { screen = "home" }
            else -> College { screen = "home" }
        }
    }
}

@Composable
private fun Header(title: String = "PathPilot", back: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (back != null) {
            IconButton(onClick = back) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        }
        Surface(
            modifier = Modifier.size(43.dp),
            shape = RoundedCornerShape(13.dp),
            color = Blue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("➤", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.width(9.dp))
        Column {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Navy)
            Text("AI Engineering Academy", fontSize = 9.sp, color = Gray)
        }
    }
}

@Composable
private fun Home(
    onLearn: () -> Unit,
    onProjects: () -> Unit,
    onGit: () -> Unit,
    onMentor: () -> Unit,
    onCollege: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("pathpilot_progress", Context.MODE_PRIVATE)
    val done = skills.count { prefs.getBoolean(it, false) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header() }
        item {
            Text(
                "Learn. Build. Debug. Ship.",
                Modifier.padding(horizontal = 18.dp),
                fontSize = 29.sp,
                fontWeight = FontWeight.Black,
                color = Navy
            )
        }
        item {
            Text(
                "A practical mentor that makes you do real engineering work.",
                Modifier.padding(horizontal = 18.dp),
                fontSize = 12.sp,
                color = Gray
            )
        }
        item {
            Card(
                Modifier.padding(horizontal = 18.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(Soft),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(19.dp)) {
                    Text("TODAY", fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Black)
                    Text(
                        "LEARN → LAB → BUILD → DEBUG → REVIEW",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Navy
                    )
                    Text(
                        "Every skill becomes a real feature, bug, review or project milestone.",
                        fontSize = 11.sp,
                        color = Gray,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
        item {
            Card(
                Modifier.padding(horizontal = 18.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text("Progress", fontWeight = FontWeight.Black, color = Navy, modifier = Modifier.weight(1f))
                        Text("$done/${skills.size}", color = Blue, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { done.toFloat() / skills.size },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = Blue
                    )
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tile(Icons.Default.Code, "Learn", Soft, onLearn)
                Tile(Icons.Default.Folder, "Projects", Green, onProjects)
                Tile(Icons.Default.Terminal, "Git Lab", Purple, onGit)
            }
        }
        item {
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tile(Icons.Default.Psychology, "AI Mentor", Purple, onMentor)
                Tile(Icons.Default.School, "College", Orange, onCollege)
            }
        }
        item {
            Text("Professional workflow", Modifier.padding(horizontal = 18.dp), fontSize = 17.sp, fontWeight = FontWeight.Black, color = Navy)
        }
        item {
            Text(
                "Requirements → tickets → branch → code → tests → commit → PR → CI → review → deploy → security → postmortem.",
                Modifier.padding(horizontal = 18.dp), fontSize = 12.sp, color = Gray
            )
        }
    }
}

@Composable
private fun RowScope.Tile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, bg: Color, click: () -> Unit) {
    Card(
        Modifier.weight(1f).clickable(onClick = click),
        colors = CardDefaults.cardColors(bg),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(Modifier.padding(13.dp)) {
            Icon(icon, null, tint = Blue)
            Spacer(Modifier.height(5.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
    }
}

@Composable
private fun Learn(back: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val list = skills.filter { it.contains(query, ignoreCase = true) }

    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Header("Learn", back) }
        item {
            OutlinedTextField(
                query,
                { query = it },
                Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                singleLine = true,
                label = { Text("Search skills") }
            )
        }
        items(list) { skill ->
            Card(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(15.dp)
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, null, tint = Blue)
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(skill, fontWeight = FontWeight.Black, color = Navy)
                        Text("LEARN → LAB → BUILD → DEBUG → REVIEW", fontSize = 10.sp, color = Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun Projects(back: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header("Real Projects", back) }
        item {
            Text("Complete the project, not just the tutorial.", Modifier.padding(horizontal = 18.dp), fontSize = 23.sp, fontWeight = FontWeight.Black, color = Navy)
        }
        items(projects) { project ->
            Card(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(17.dp)) {
                    Text(project, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Navy)
                    Text(
                        "Requirements → implementation → bug → tests → Git → PR → review → deployment → security → README",
                        fontSize = 11.sp, color = Gray, modifier = Modifier.padding(top = 6.dp)
                    )
                    Text("9 milestones", fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun GitLab(back: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val expected = gitSteps[step].lowercase()

    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Git + GitHub Practice", back) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Navy), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("TICKET → BRANCH → COMMIT → PR → CI → REVIEW → MERGE", fontSize = 10.sp, color = Soft, fontWeight = FontWeight.Black)
                    Text("Real developer workflow", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Black)
                    LinearProgressIndicator(
                        progress = { step.toFloat() / gitSteps.size },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        color = Blue
                    )
                }
            }
        }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(17.dp)) {
                    Text("Step ${step + 1}", fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Black)
                    Text(gitSteps[step], fontSize = 19.sp, fontWeight = FontWeight.Black, color = Navy)
                    OutlinedTextField(input, { input = it }, Modifier.fillMaxWidth().padding(top = 9.dp), singleLine = true, label = { Text("Command / action") })
                    Button(
                        onClick = {
                            val answer = input.trim().lowercase()
                            val ok = answer == expected || (expected == "git add ." && answer.startsWith("git add "))
                            message = if (ok) "✓ Correct. Next workflow step unlocked." else "Try again — follow the exact Git idea in the task."
                            if (ok && step < gitSteps.lastIndex) {
                                step++
                                input = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Check") }
                    if (message.isNotBlank()) {
                        Text(message, fontSize = 11.sp, color = if (message.startsWith("✓")) Color(0xFF138A52) else Color(0xFFC2410C), modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }
        }
        items(gitSteps) { task ->
            Text("• $task", Modifier.padding(horizontal = 20.dp), fontSize = 11.sp, color = Gray)
        }
    }
}

@Composable
private fun Mentor(back: () -> Unit) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("AI Mentor", back) }
        item {
            Text("Ask a senior engineer. Learn the reasoning, then do the next action.", Modifier.padding(horizontal = 18.dp), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Navy)
        }
        item {
            OutlinedTextField(question, { question = it }, Modifier.padding(horizontal = 16.dp).fillMaxWidth(), label = { Text("What are you stuck on?") })
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        loading = true
                        scope.launch {
                            answer = GeminiMentorService.askGeminiMentor(context, question, "PathPilot learner. Teach first, ask for evidence, give one concrete next action.")
                            loading = false
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                enabled = question.isNotBlank() && !loading,
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (loading) "Thinking…" else "Ask Gemini") }
        }
        if (answer.isNotBlank()) {
            item {
                Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp)) {
                    Text(answer, Modifier.padding(17.dp), fontSize = 12.sp, color = Navy)
                }
            }
        }
    }
}

@Composable
private fun College(back: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 25.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header("College Study", back) }
        item {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Orange), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("EXAM MODE", fontSize = 10.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Black)
                    Text("Syllabus → concepts → PYQs → practice → revision", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Navy)
                    Text("Rank by evidence; never promise exact questions.", fontSize = 11.sp, color = Gray, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
        item { Study("Thermodynamics", "Very High", "Laws, processes, definitions, numericals and PYQs.") }
        item { Study("Engineering Mathematics", "High", "Matrices, linear algebra, calculus and repeated problems.") }
        item { Study("C Programming", "High", "Loops, functions, arrays, pointers and lab programs.") }
        item { Study("Chemistry", "Medium", "Use syllabus weight, assignments and mistakes.") }
    }
}

@Composable
private fun Study(title: String, priority: String, detail: String) {
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(15.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.School, null, tint = Blue)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Navy)
                Text(detail, fontSize = 11.sp, color = Gray)
            }
            Text(priority, fontSize = 10.sp, color = Blue, fontWeight = FontWeight.Black)
        }
    }
}
