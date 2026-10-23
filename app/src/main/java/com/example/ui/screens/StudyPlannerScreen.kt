package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private data class StudyTask(
    val id: String,
    val title: String,
    val subtitle: String,
    val minutes: Int,
    val type: String
)

private fun tasksFor(day: String): List<StudyTask> = when (day) {
    "Mon", "Tue", "Wed", "Thu" -> listOf(
        StudyTask("college_$day", "College revision", "Review today's class + make short notes", 45, "College"),
        StudyTask("math_$day", "Core subject", "Solve 5–8 exam-style problems", 45, "College"),
        StudyTask("code_$day", "Developer track", "Concept + small coding exercise", 60, "Coding"),
        StudyTask("review_$day", "Active recall", "Revise yesterday's mistakes", 20, "Revision")
    )
    "Fri" -> listOf(
        StudyTask("college_$day", "Weekly college review", "Consolidate the week's lectures", 45, "College"),
        StudyTask("assignment_$day", "Assignment block", "Finish the highest-priority submission", 50, "College"),
        StudyTask("code_$day", "Backend practice", "HTTP / API / SQL hands-on practice", 60, "Coding"),
        StudyTask("review_$day", "Weekly review", "Check weak topics and plan Saturday", 20, "Revision")
    )
    "Sat" -> listOf(
        StudyTask("pyq_$day", "PYQ Sprint", "Practice high-frequency questions", 60, "Exam"),
        StudyTask("project_$day", "Build session", "Turn a concept into a real feature", 90, "Coding"),
        StudyTask("security_$day", "Cybersecurity lab", "Authorized lab / CTF practice", 45, "Security")
    )
    else -> listOf(
        StudyTask("weekly_$day", "Weekly test", "Mixed college + coding assessment", 60, "Exam"),
        StudyTask("review_$day", "Mistake review", "Revisit everything you got wrong", 45, "Revision"),
        StudyTask("planning_$day", "Next-week planning", "Set priorities from syllabus and deadlines", 20, "Planning")
    )
}

@Composable
fun StudyPlannerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("study_tracker", Context.MODE_PRIVATE) }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val today = days[(calendarDay + 5) % 7]
    var selectedDay by remember { mutableStateOf(today) }
    var filter by remember { mutableStateOf("All") }
    var refresh by remember { mutableStateOf(0) }
    val allTasks = tasksFor(selectedDay)
    val visibleTasks = if (filter == "All") allTasks else allTasks.filter { it.type == filter }
    val completed = allTasks.count { prefs.getBoolean("${selectedDay}_${it.id}", false) }
    val progress = if (allTasks.isEmpty()) 0f else completed.toFloat() / allTasks.size
    val totalMinutes = allTasks.sumOf { it.minutes }
    @Suppress("UNUSED_VARIABLE") val refreshToken = refresh

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("Study Planner", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("College + coding + revision, tracked in one place", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.padding(9.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("TODAY'S PLAN", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("$completed/${allTasks.size} sessions complete", fontWeight = FontWeight.Bold)
                    }
                    Text("${totalMinutes}m", fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(7.dp))
                Text("Finish the important work first. Missed sessions can be moved to the next available slot.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            days.forEach { day ->
                FilterChip(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    label = { Text(day) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("All", "College", "Coding", "Exam", "Revision", "Security").forEach { value ->
                FilterChip(
                    selected = filter == value,
                    onClick = { filter = value },
                    label = { Text(value) }
                )
            }
        }

        visibleTasks.forEach { task ->
            val done = prefs.getBoolean("${selectedDay}_${task.id}", false)
            TaskCard(task, done) {
                prefs.edit().putBoolean("${selectedDay}_${task.id}", !done).apply()
                refresh += 1
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Exam intelligence", fontWeight = FontWeight.Black)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Add your syllabus, PYQs and assignments later. Path-Pilot will rank topics by repetition, syllabus weight, assignment overlap and your mistakes. It will show high-priority questions—not fake '99% guaranteed' predictions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.MenuBook, null, modifier = Modifier.padding(end = 6.dp))
                    Text("Syllabus / PYQ import — coming next")
                }
            }
        }
    }
}

@Composable
private fun TaskCard(task: StudyTask, done: Boolean, onToggle: () -> Unit) {
    val icon = when (task.type) {
        "Coding" -> Icons.Default.Code
        "College", "Exam" -> Icons.Default.School
        else -> Icons.Default.TaskAlt
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (done) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (done) Color.Gray else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold)
                Text(task.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Text("${task.minutes} min • ${task.type}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
            Icon(
                if (done) Icons.Default.CheckCircle else Icons.Default.TaskAlt,
                null,
                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}
