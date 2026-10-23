package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.viewmodel.PathPilotViewModel
import java.util.Calendar

private val BrandAmberCompat = Color(0xFFFFB020)

private data class DailyStudyTask(val id: String, val title: String, val detail: String, val minutes: Int, val kind: String)

private fun dailyTasks(): List<DailyStudyTask> = listOf(
    DailyStudyTask("college", "College revision", "Review today's lectures + short notes", 45, "College"),
    DailyStudyTask("exam", "Exam practice", "Solve 5–8 syllabus/PYQ-style questions", 45, "College"),
    DailyStudyTask("coding", "Developer track", "Concept + a small coding exercise", 60, "Coding"),
    DailyStudyTask("review", "Active recall", "Review yesterday's mistakes", 20, "Revision")
)

@Composable
fun DeveloperMentorScreen(
    viewModel: PathPilotViewModel,
    onOpenMentor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val name = user?.name?.substringBefore(" ")?.ifBlank { "Builder" } ?: "Builder"
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("path_pilot_study_tracker", Context.MODE_PRIVATE) }
    var refresh by remember { mutableStateOf(0) }
    val tasks = remember { dailyTasks() }
    val completed = tasks.count { prefs.getBoolean("done_${it.id}", false) }
    val progress = completed.toFloat() / tasks.size.coerceAtLeast(1)
    @Suppress("UNUSED_VARIABLE") val refreshToken = refresh

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Good to see you, $name 👋", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Learn smarter: college + development + security", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = BrandIndigo.copy(alpha = 0.14f)) {
                    Icon(Icons.Default.Psychology, null, tint = BrandIndigo, modifier = Modifier.padding(10.dp).size(22.dp))
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = BrandIndigo.copy(alpha = 0.12f)) {
                            Icon(Icons.Default.Flag, null, tint = BrandIndigo, modifier = Modifier.padding(9.dp).size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("TODAY'S MISSION", fontSize = 11.sp, fontWeight = FontWeight.Black, color = BrandIndigo)
                            Text("How an API request works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = BrandEmerald.copy(alpha = 0.12f)) {
                            Text("25 min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandEmerald, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("App → HTTP → Backend → Database → Response", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { 0.03f }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onOpenMentor, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Start learning", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = BrandCyan)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Today's study plan", fontWeight = FontWeight.Black)
                            Text("College + coding + revision", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$completed/${tasks.size}", fontWeight = FontWeight.Black, color = BrandCyan)
                    }
                    Spacer(Modifier.height(9.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(5.dp))
                    Spacer(Modifier.height(9.dp))
                    tasks.forEach { task ->
                        val done = prefs.getBoolean("done_${task.id}", false)
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                prefs.edit().putBoolean("done_${task.id}", !done).apply()
                                refresh++
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(if (done) Icons.Default.CheckCircle else Icons.Default.TaskAlt, null, modifier = Modifier.size(19.dp), tint = if (done) BrandEmerald else MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(task.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(task.detail, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${task.minutes}m", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Tap a task when finished — progress is saved on this device.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                StatCard("$completed", "Done today", Icons.Default.Bolt, BrandAmberCompat, Modifier.weight(1f))
                StatCard("0", "Projects", Icons.Default.Code, BrandCyan, Modifier.weight(1f))
                StatCard("0%", "Mastery", Icons.Default.TrackChanges, BrandEmerald, Modifier.weight(1f))
            }
        }

        item { Text("Your learning path", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(end = 4.dp)) {
                item { PathChip("Programming", Icons.Default.Code, BrandCyan, true) }
                item { PathChip("Backend", Icons.Default.DataObject, BrandEmerald, false) }
                item { PathChip("Professional", Icons.Default.Storage, BrandIndigo, false) }
                item { PathChip("Security", Icons.Default.Security, BrandRose, false) }
            }
        }

        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoadmapRow("01", "Programming Core", "C → Python → problem solving", BrandCyan, true)
                    RoadmapRow("02", "Backend Engineering", "HTTP → API → FastAPI → SQL", BrandEmerald, false)
                    RoadmapRow("03", "Professional Workflow", "Git → tests → PR → deploy", BrandIndigo, false)
                    RoadmapRow("04", "Cybersecurity", "Linux → networking → web labs", BrandRose, false)
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth().clickable(onClick = onOpenMentor), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = BrandIndigo.copy(alpha = 0.10f))) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = BrandIndigo.copy(alpha = 0.14f)) {
                        Icon(Icons.Default.AutoAwesome, null, tint = BrandIndigo, modifier = Modifier.padding(9.dp).size(21.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ask your AI Mentor", fontWeight = FontWeight.Bold)
                        Text("Explain • hint • debug • review", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("→", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandIndigo)
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Exam intelligence", fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text("When you add your syllabus, PYQs and assignments, Path-Pilot will rank questions using repetition, syllabus weight, assignment overlap and your mistakes. It will show high-probability questions—not fake 99% guarantees.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, title: String, icon: ImageVector, tint: Color, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column(Modifier.padding(11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(19.dp), tint = tint)
            Spacer(Modifier.height(4.dp))
            Text(label, fontWeight = FontWeight.Black)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PathChip(label: String, icon: ImageVector, tint: Color, selected: Boolean) {
    Surface(shape = RoundedCornerShape(13.dp), color = if (selected) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface, modifier = Modifier.clickable { }) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(17.dp), tint = tint)
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RoadmapRow(number: String, title: String, subtitle: String, tint: Color, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = if (active) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant) {
            Text(number, Modifier.padding(horizontal = 9.dp, vertical = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (active) tint else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(if (active) "START" else "LOCKED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (active) tint else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
