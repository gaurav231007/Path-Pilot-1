package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private data class Skill(val id:String,val track:String,val title:String,val level:String,val concept:String,val mission:String,val build:String,val debug:String,val review:String)
private val skills=listOf(
 Skill("py1","Programming","Python Foundations","Beginner","Variables, conditions, loops, functions and errors","Write a validator that rejects empty input and invalid numbers.","Turn it into a reusable CLI utility.","Trace a deliberately broken condition from symptom to root cause.","Explain the fix and add three tests."),
 Skill("http1","Backend","HTTP Request to Response","Beginner","Methods, headers, status codes, JSON and request flow","Inspect GET and POST requests and explain the important fields.","Build a small API client handling 2xx, 4xx and 5xx responses.","Diagnose a 400, 404 and 500 using evidence.","Explain the request lifecycle and validation boundary."),
 Skill("api1","Backend","FastAPI REST API","Intermediate","Routes, schemas, validation, errors and API contracts","Create CRUD endpoints for tasks.","Build a notes API with validation and clean errors.","Debug route-not-found and validation failures.","Write endpoint tests and document the contract."),
 Skill("sql1","Backend","SQL + PostgreSQL","Intermediate","CRUD, joins, constraints, indexes and transactions","Write queries for users, projects and tasks.","Connect an API to PostgreSQL or Supabase.","Find the cause of a bad query or constraint violation.","Explain how the schema prevents inconsistent data."),
 Skill("git1","Professional","Professional Git Workflow","Beginner","Requirements, branches, commits, PRs, reviews and CI","Turn a feature request into acceptance criteria and a branch plan.","Implement it with focused commits and tests.","Read a failing CI check and find useful evidence.","Write a PR description a teammate can review."),
 Skill("prod1","Professional","Production Debugging","Intermediate","Logs, reproduction, tests, observability and postmortems","Turn a bug report into a minimal reproduction.","Create a regression test and structured error handling.","Use logs and tests to isolate root cause.","Write a short incident note."),
 Skill("ai1","AI Development","Build with LLM APIs","Beginner","Prompts, structured output, errors, latency and cost","Call an LLM and safely parse structured output.","Build a bounded AI feature inside a normal application.","Handle timeout, invalid output and API errors.","Explain which decisions must stay deterministic."),
 Skill("sec1","Cybersecurity","Linux + Networking Lab","Beginner","Processes, permissions, ports, DNS, TCP/IP, HTTP and logs","Inspect a service in your own lab and explain its ports.","Run and observe a local service.","Diagnose a permission, port or DNS problem safely.","Keep a lab notebook with commands and evidence."),
 Skill("sec2","Cybersecurity","Web Security Testing","Intermediate","Scope, authorization, threat modeling and remediation","Write scope and trust boundaries before testing.","Test an intentionally vulnerable local lab only.","Document one finding with evidence, impact and remediation.","Retest after the fix and compare evidence."),
 Skill("cap","Capstone","Production-Style AI Product","Advanced","Requirements, architecture, backend, DB, AI, tests, Git and security","Write a one-page spec and acceptance criteria before coding.","Build the product in small milestones.","Handle a real bug, failed test and security finding.","Review like a senior engineer and write a postmortem.")
)
private enum class Phase { TEACH, LAB, BUILD, DEBUG, REVIEW }

@Composable
fun PathPilotAcademyApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = skills.firstOrNull { it.id == selectedId }
    BackHandler(enabled = selected != null) { selectedId = null }
    if (selected != null) TrainingScreen(selected, { selectedId = null }) else Shell(tab, { tab = it }, { selectedId = it })
}

@Composable private fun Shell(tab:Int,onTab:(Int)->Unit,onSkill:(String)->Unit) {
    val names=listOf("Home","Learn","Mentor","College")
    val icons=listOf(Icons.Default.Home,Icons.Default.Code,Icons.Default.Psychology,Icons.Default.School)
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth()) {
            when(tab) { 0->HomeScreen(onSkill,{onTab(1)}); 1->LearnScreen(onSkill); 2->MentorScreen(); else->CollegeScreen() }
        }
        NavigationBar { names.forEachIndexed { i,n -> NavigationBarItem(selected=tab==i,onClick={onTab(i)},icon={Icon(icons[i],n)},label={Text(n)}) } }
    }
}

@Composable private fun HomeScreen(onSkill:(String)->Unit,onLearn:()->Unit) {
    val c=LocalContext.current; val p=remember{c.getSharedPreferences("pathpilot_engine",Context.MODE_PRIVATE)}
    val done=skills.count{p.getBoolean("done_${it.id}",false)}; val next=skills.firstOrNull{!p.getBoolean("done_${it.id}",false)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { Text("Path-Pilot",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Black); Text("AI Engineering Academy",color=MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Card { Column(Modifier.padding(18.dp)) { Text("NEXT MISSION",fontSize=11.sp,fontWeight=FontWeight.Black); Text(next?.title?:"Capstone unlocked",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black); Spacer(Modifier.height(6.dp)); Text("Learn → do → submit → feedback → fix → prove → advance"); Spacer(Modifier.height(12.dp)); Button(onClick={next?.let{onSkill(it.id)}},enabled=next!=null,modifier=Modifier.fillMaxWidth()){Text("Start mission")} } } }
        item { Text("Mastery  $done / ${skills.size}",fontWeight=FontWeight.Bold); LinearProgressIndicator(progress={done.toFloat()/skills.size},modifier=Modifier.fillMaxWidth()) }
        item { Card { Column(Modifier.padding(16.dp)) { Text("Not a flash-card app",fontWeight=FontWeight.Black); Text("Every skill has teaching, a real task, submission, debugging, review and a next challenge.") } } }
        item { Button(onClick=onLearn,modifier=Modifier.fillMaxWidth()){Text("Open full academy")} }
    }
}

@Composable private fun LearnScreen(onSkill:(String)->Unit) {
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("Engineering Academy",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black); Text("Real engineering workflow, not passive flashcards.",color=MaterialTheme.colorScheme.onSurfaceVariant) }
        items(skills) { s -> Card(Modifier.fillMaxWidth().clickable{onSkill(s.id)}) { Column(Modifier.padding(16.dp)) { Text("${s.track.uppercase()} • ${s.level}",fontSize=10.sp,fontWeight=FontWeight.Black); Text(s.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold); Text(s.concept,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); Text("Open mission →",fontWeight=FontWeight.Bold) } } }
    }
}

@Composable private fun TrainingScreen(s:Skill,onBack:()->Unit) {
    val c=LocalContext.current; val scope=rememberCoroutineScope(); val p=remember{c.getSharedPreferences("pathpilot_engine",Context.MODE_PRIVATE)}
    var phase by remember{mutableStateOf(Phase.TEACH)}; var work by remember{mutableStateOf("")}; var feedback by remember{mutableStateOf("")}; var busy by remember{mutableStateOf(false)}; var attempts by remember{mutableIntStateOf(p.getInt("attempts_${s.id}",0))}
    fun ask(prompt:String) { busy=true; scope.launch { feedback=GeminiMentorService.askGeminiMentor(c,prompt,"Skill=${s.title}; track=${s.track}; level=${s.level}; attempts=$attempts; phase=${phase.name}"); busy=false } }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Row(verticalAlignment=Alignment.CenterVertically) { IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}; Column { Text("${s.track.uppercase()} • ${s.level}",fontSize=10.sp,fontWeight=FontWeight.Black); Text(s.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black) } } }
        item { ScrollableTabRow(selectedTabIndex=phase.ordinal) { listOf("Learn","Lab","Build","Debug","Review").forEachIndexed{i,label->Tab(selected=phase.ordinal==i,onClick={phase=Phase.values()[i]},text={Text(label)})} } }
        when(phase) {
            Phase.TEACH -> { item{InfoCard("Teacher",s.concept,"Understand the idea first, then prove it.")}; item{Button(onClick={ask("Teach ${s.concept} for ${s.title} practically. Ask one thinking question and one tiny exercise. Do not use flashcards and do not dump the complete solution.")},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text(if(busy)"Teaching…" else "Ask AI teacher")}}; item{FeedbackCard(feedback)}; item{Button(onClick={phase=Phase.LAB},modifier=Modifier.fillMaxWidth()){Text("Enter lab")}} }
            Phase.LAB -> { item{InfoCard("Your lab",s.mission,"You produce the work. AI coaches and reviews it.")}; item{WorkBox(work,{work=it},"Your code / reasoning / output")}; item{Button(onClick={attempts++;p.edit().putInt("attempts_${s.id}",attempts).apply();ask("Review this lab attempt. Identify what is correct, one highest-value gap, and a useful hint. Do not give the complete answer. Mission: ${s.mission}\nAttempt:\n$work")},enabled=work.isNotBlank()&&!busy,modifier=Modifier.fillMaxWidth()){Text("Submit to mentor")}}; item{FeedbackCard(feedback)}; item{Button(onClick={phase=Phase.BUILD},modifier=Modifier.fillMaxWidth()){Text("Move to build")}} }
            Phase.BUILD -> { item{InfoCard("Build challenge",s.build,"Start small, then add tests and edge cases.")}; item{WorkBox(work,{work=it},"Implementation / architecture / tests")}; item{Button(onClick={ask("Act as a senior code reviewer. Review this implementation for correctness, maintainability, edge cases and tests. Give one prioritized improvement and one test to add. Do not rewrite everything.\nWork:\n$work")},enabled=work.isNotBlank()&&!busy,modifier=Modifier.fillMaxWidth()){Text("Run code review")}}; item{FeedbackCard(feedback)}; item{Button(onClick={phase=Phase.DEBUG},modifier=Modifier.fillMaxWidth()){Text("Break it → debug")}} }
            Phase.DEBUG -> { item{InfoCard("Debug challenge",s.debug,"Use symptom → evidence → hypothesis → test → root cause → fix → regression test.")}; item{WorkBox(work,{work=it},"Error / logs / symptoms / what you tried")}; item{Button(onClick={ask("Be a debugging coach. Analyze the evidence. Ask one diagnostic question and give the smallest useful hint before the fix.\nEvidence:\n$work")},enabled=work.isNotBlank()&&!busy,modifier=Modifier.fillMaxWidth()){Text("Get debugging coaching")}}; item{FeedbackCard(feedback)}; item{Button(onClick={phase=Phase.REVIEW},modifier=Modifier.fillMaxWidth()){Text("Final review")}} }
            Phase.REVIEW -> { item{InfoCard("Proof of learning",s.review,"Explain what happened, what changed and what proves it works.")}; item{WorkBox(work,{work=it},"Final explanation + evidence")}; item{Button(onClick={ask("Evaluate mastery from this evidence. Score understanding, implementation, debugging and proof from 0-100. If below 75 prescribe remediation; if 75+ prescribe a harder follow-up. Never claim mastery without evidence.\nEvidence:\n$work")},enabled=work.isNotBlank()&&!busy,modifier=Modifier.fillMaxWidth()){Text("Evaluate mastery")}}; item{FeedbackCard(feedback)}; item{Button(onClick={p.edit().putBoolean("done_${s.id}",true).apply();onBack()},enabled=feedback.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("Complete skill")}} }
        }
        item{Text("Attempts: $attempts",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}

@Composable private fun InfoCard(title:String,body:String,footer:String){Card{Column(Modifier.padding(16.dp)){Text(title,fontWeight=FontWeight.Black);Spacer(Modifier.height(5.dp));Text(body);Spacer(Modifier.height(8.dp));Text(footer,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable private fun WorkBox(v:String,on:(String)->Unit,label:String){OutlinedTextField(value=v,onValueChange=on,modifier=Modifier.fillMaxWidth(),minLines=6,label={Text(label)})}
@Composable private fun FeedbackCard(text:String){if(text.isNotBlank())Card{Column(Modifier.padding(16.dp)){Text("AI mentor feedback",fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text(text)}}}

@Composable private fun MentorScreen(){
    val c=LocalContext.current; val scope=rememberCoroutineScope(); var q by remember{mutableStateOf("")}; var a by remember{mutableStateOf("")}; var busy by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().padding(16.dp)){Text("AI Mentor",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Code, architecture, debugging, career and authorized security labs.",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));WorkBox(q,{q=it},"Ask your mentor…");Spacer(Modifier.height(8.dp));Button(onClick={busy=true;scope.launch{a=GeminiMentorService.askGeminiMentor(c,q,"Adaptive engineering learner");busy=false}},enabled=q.isNotBlank()&&!busy,modifier=Modifier.fillMaxWidth()){Text(if(busy)"Thinking…" else "Ask mentor")};Spacer(Modifier.height(12.dp));FeedbackCard(a)}
}

@Composable private fun CollegeScreen(){
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("College Study",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black)};item{InfoCard("Evidence-based question ranking","Use syllabus, PYQs, assignments and teacher material to rank Very High / High / Medium / Low using repetition, syllabus weight, overlap and marks patterns.","No fake 99% prediction. Rankings are evidence-based, never guarantees.")};item{InfoCard("Adaptive daily plan","Combine college, commute, coding, cybersecurity, assignments and revision. Missed work should rebalance the next plan.","Focus on the next useful action.")}}
}
