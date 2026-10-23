package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private data class Skill(val id:String,val track:String,val title:String,val level:String,val concept:String,val mission:String,val build:String,val debug:String,val review:String)

private val skills=listOf(
 Skill("py1","Programming","Python Foundations","Beginner","Variables, conditions, loops, functions and errors","Write a validator that rejects empty input and invalid numbers.","Turn it into a reusable CLI utility.","Intentionally break one condition and trace the error from symptom to root cause.","Explain the fix in your own words and add 3 tests."),
 Skill("http1","Backend","HTTP From Request To Response","Beginner","Methods, headers, status codes, JSON, cookies and request flow","Inspect a GET and POST request and explain each important field.","Build a tiny API client that handles 2xx, 4xx and 5xx responses.","Diagnose a 400, 404 and 500 using evidence instead of guessing.","Draw the request lifecycle and explain where validation belongs."),
 Skill("api1","Backend","FastAPI REST API","Intermediate","Routes, validation, schemas, errors and API contracts","Create GET/POST/PUT/DELETE endpoints for tasks.","Build a notes API with validation and clean error responses.","Debug a route-not-found and validation failure.","Write endpoint tests and document the contract."),
 Skill("sql1","Backend","SQL + PostgreSQL","Intermediate","CRUD, joins, constraints, indexes and transactions","Write queries for users, projects and tasks.","Connect an API to PostgreSQL/Supabase.","Find the cause of a bad query or constraint violation.","Explain how your schema prevents inconsistent data."),
 Skill("git1","Professional","Professional Git Workflow","Beginner","Requirements, branches, commits, PRs, reviews and CI","Turn a feature request into acceptance criteria and a branch plan.","Implement it with focused commits and tests.","Read a failing CI check and find the first useful evidence.","Write a PR description a teammate can review."),
 Skill("prod1","Professional","Production Debugging","Intermediate","Logs, reproduction, tests, observability and postmortems","Turn a bug report into a minimal reproduction.","Create a regression test and structured error handling.","Use logs and tests to isolate the root cause.","Write a short incident/postmortem note."),
 Skill("ai1","AI Development","Build With LLM APIs","Beginner","Prompts, tokens, structured output, errors, latency and cost","Call an LLM and safely parse structured output.","Build a bounded AI feature inside a normal application.","Handle timeout, invalid output and API errors.","Explain which decisions must stay deterministic."),
 Skill("agent1","AI Development","RAG + Tool Calling","Intermediate","Retrieval, grounding, tools, state and permission boundaries","Design a retrieval flow and identify what needs evidence.","Build a document Q&A or approved tool-using assistant.","Debug retrieval misses and malformed tool arguments.","Evaluate relevance, hallucination and tool safety."),
 Skill("sec1","Cybersecurity","Linux + Networking Lab","Beginner","Processes, permissions, ports, DNS, TCP/IP, HTTP and logs","Inspect a service in your own lab and explain its ports.","Run and observe a local service.","Diagnose a permission, port or DNS problem safely.","Keep a lab notebook with commands and evidence."),
 Skill("sec2","Cybersecurity","Web Security Testing","Intermediate","Scope, authorization, threat modeling, input handling and remediation","Write scope and trust boundaries before testing.","Test an intentionally vulnerable local lab only.","Document one finding with evidence, impact and remediation.","Retest after the fix and compare evidence."),
 Skill("cap","Capstone","Production-Style AI Product","Advanced","Requirements, architecture, backend, DB, AI, tests, Git, deployment and security","Write a one-page spec and acceptance criteria before coding.","Build the product in small milestones.","Handle a real bug, failed test and security finding.","Ship, review like a senior engineer and write a postmortem.")
)

private enum class Phase(val label:String){ TEACH("Learn"), LAB("Lab"), BUILD("Build"), DEBUG("Debug"), REVIEW("Review") }

@Composable
fun PathPilotAcademyApp(viewModel: PathPilotViewModel){
 var selected by remember{mutableStateOf<String?>(null)}
 var tab by remember{mutableIntStateOf(0)}
 val skill=skills.firstOrNull{it.id==selected}
 BackHandler(enabled=skill!=null||tab!=0){if(skill!=null)selected=null else tab=0}
 if(skill!=null) TrainingScreen(skill){selected=null}
 else Shell(tab,{tab=it},{selected=it})
}

@Composable private fun Shell(tab:Int,onTab:(Int)->Unit,onSkill:(String)->Unit){
 val names=listOf("Home","Learn","Mentor","College")
 val icons=listOf(Icons.Default.Home,Icons.Default.Code,Icons.Default.Psychology,Icons.Default.School)
 Column(Modifier.fillMaxSize()){
  Box(Modifier.weight(1f).fillMaxWidth()){
   when(tab){0->Home(onSkill,{onTab(1)});1->Learn(onSkill);2->Mentor();else->College()}
  }
  NavigationBar{names.forEachIndexed{i,n->NavigationBarItem(tab==i,{onTab(i)},icon={Icon(icons[i],n)},label={Text(n)})}}
 }
}

@Composable private fun Home(onSkill:(String)->Unit,onLearn:()->Unit){
 val c=LocalContext.current;val p=remember{c.getSharedPreferences("pathpilot_engine",Context.MODE_PRIVATE)}
 val done=skills.count{p.getBoolean("done_${it.id}",false)};val next=skills.firstOrNull{!p.getBoolean("done_${it.id}",false)}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Text("Path-Pilot",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Black);Text("AI Engineering Academy",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  item{Card{Column(Modifier.padding(18.dp)){Text("NEXT MISSION",fontSize=11.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(next?.title?:"Capstone unlocked",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text("Learn → do → submit → get feedback → fix → prove → advance",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));Button(enabled=next!=null,onClick={next?.let{onSkill(it.id)}},Modifier.fillMaxWidth()){Text("Start mission")}}}}
  item{Text("Mastery  $done / ${skills.size}",fontWeight=FontWeight.Bold);LinearProgressIndicator(done.toFloat()/skills.size,Modifier.fillMaxWidth())}
  item{Card{Column(Modifier.padding(16.dp)){Text("No passive course mode",fontWeight=FontWeight.Black);Text("Every lesson has a real task. The mentor remembers your attempts, changes the next hint based on mistakes, and asks you to prove the fix.")}}}
  item{Button(onClick=onLearn,Modifier.fillMaxWidth()){Text("Open full academy")}}
 }
}

@Composable private fun Learn(onSkill:(String)->Unit){
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  item{Text("Engineering Academy",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Real engineering workflow, not flashcards.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  items(skills){s->Card(Modifier.fillMaxWidth().clickable{onSkill(s.id)}){Column(Modifier.padding(16.dp)){Text("${s.track.uppercase()} • ${s.level}",fontSize=10.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(s.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(s.concept,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(8.dp));Text("Open mission →",fontWeight=FontWeight.Bold)}}}
 }
}

@Composable private fun TrainingScreen(s:Skill,onBack:()->Unit){
 val c=LocalContext.current;val scope=rememberCoroutineScope();val p=remember{c.getSharedPreferences("pathpilot_engine",Context.MODE_PRIVATE)}
 var phase by remember{mutableStateOf(Phase.TEACH)};var work by remember{mutableStateOf("")};var answer by remember{mutableStateOf("")};var feedback by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};var attempts by remember{mutableIntStateOf(p.getInt("attempts_${s.id}",0))}
 val context="Learner is on ${s.title}; track=${s.track}; level=${s.level}; prior attempts=$attempts; phase=${phase.label}."
 fun ask(prompt:String){busy=true;scope.launch{feedback=GeminiMentorService.askGeminiMentor(c,"You are Path-Pilot's adaptive senior engineering mentor. $context Teach by asking the learner to think and act. Do not dump a complete solution unless the learner has already attempted and explicitly asks after useful hints. Review evidence, identify exactly one highest-value mistake, give a precise hint, ask one diagnostic question, and finish with one next action. Increase difficulty after strong work and simplify after repeated mistakes. Never help with unauthorized cybersecurity.\n$prompt");busy=false}}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")};Column{Text("${s.track.uppercase()} • ${s.level}",fontSize=10.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(s.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black)}}}
  item{ScrollableTabRow(selectedTabIndex=phase.ordinal){Phase.values().forEachIndexed{i,x->Tab(i==phase.ordinal,{phase=x},text={Text(x.label)})}}}
  when(phase){
   Phase.TEACH->{item{EngineCard("Teacher",s.concept,"First, explain this concept in your own words. Then press 'Check my understanding'.")};item{Button(enabled=!busy,onClick={ask("Start with a short explanation tailored to a beginner, then ask me one conceptual question. Do not turn it into flashcards.")},Modifier.fillMaxWidth()){Text("Ask AI teacher")}};item{Button(onClick={phase=Phase.LAB},Modifier.fillMaxWidth()){Text("I understand → enter lab")}}}
   Phase.LAB->{item{EngineCard("Your lab",s.mission,"You must produce the work. AI is the coach, not the answer key.")};item{WorkBox(work,{work=it},"Paste code, command output, design, SQL, or reasoning…")};item{Button(enabled=work.isNotBlank()&&!busy,onClick={attempts++;p.edit().putInt("attempts_${s.id}",attempts).apply();ask("LAB SUBMISSION. Evaluate this attempt against the mission. Identify what is correct, one key gap, and give a hint rather than the solution. Submission:\n$work")},Modifier.fillMaxWidth()){Text(if(busy)"Reviewing…" else "Submit for mentor review")};item{Feedback(feedback)};item{Button(onClick={phase=Phase.BUILD},Modifier.fillMaxWidth()){Text("Move to build")}}}
   Phase.BUILD->{item{EngineCard("Build challenge",s.build,"Write the smallest working version first. Then improve it.")};item{WorkBox(work,{work=it},"Your implementation / architecture / test…")};item{Button(enabled=work.isNotBlank()&&!busy,onClick={attempts++;p.edit().putInt("attempts_${s.id}",attempts).apply();ask("BUILD REVIEW. Assess implementation quality, correctness, edge cases, tests and maintainability. Give one prioritized improvement and a test the learner should add. Work:\n$work")},Modifier.fillMaxWidth()){Text("Run code review")};item{Feedback(feedback)};item{Button(onClick={phase=Phase.DEBUG},Modifier.fillMaxWidth()){Text("Break it → debug")}}}
   Phase.DEBUG->{item{EngineCard("Debug challenge",s.debug,"Use symptoms → evidence → hypothesis → smallest test → root cause → fix → regression test.")};item{WorkBox(answer,{answer=it},"What failed? Error/log/output + what you tried…")};item{Button(enabled=answer.isNotBlank()&&!busy,onClick={ask("DEBUG COACH. Do not give the fix immediately. Analyze the evidence, ask one diagnostic question, then give the smallest useful hint. Debug report:\n$answer")},Modifier.fillMaxWidth()){Text("Get debugging coaching")};item{Feedback(feedback)};item{Button(onClick={phase=Phase.REVIEW},Modifier.fillMaxWidth()){Text("I fixed it → final review")}}}
   Phase.REVIEW->{item{EngineCard("Proof of learning",s.review,"Explain the concept, your mistake, the fix, and what test proves it works.")};item{WorkBox(answer,{answer=it},"Explain your final solution and evidence…")};item{Button(enabled=answer.isNotBlank()&&!busy,onClick={ask("FINAL REVIEW. Decide whether the learner demonstrated mastery. Score concept understanding, implementation, debugging and proof from 0-100. If below 75, prescribe a smaller remediation task; if 75+, prescribe a harder follow-up. Never claim mastery without evidence. Final evidence:\n$answer")},Modifier.fillMaxWidth()){Text("Evaluate mastery")};item{Feedback(feedback)};item{Button(enabled=feedback.isNotBlank(),onClick={p.edit().putBoolean("done_${s.id}",true).apply();onBack()},Modifier.fillMaxWidth()){Text("Complete skill & unlock next")}}}
  }
  item{Text("Attempts: $attempts",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}

@Composable private fun EngineCard(title:String,body:String,foot:String){Card{Column(Modifier.padding(16.dp)){Text(title,fontWeight=FontWeight.Black);Spacer(Modifier.height(5.dp));Text(body);Spacer(Modifier.height(8.dp));Text(foot,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable private fun WorkBox(v:String,on:(String)->Unit,label:String){OutlinedTextField(v,on,Modifier.fillMaxWidth(),minLines=6,label={Text(label)},supportingText={Text("Your work is the evidence. Don't paste a copied answer.")})}
@Composable private fun Feedback(v:String){if(v.isNotBlank())Card{Column(Modifier.padding(16.dp)){Text("AI mentor feedback",fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text(v)}}}

@Composable private fun Mentor(){val c=LocalContext.current;val scope=rememberCoroutineScope();var q by remember{mutableStateOf("")};var a by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};Column(Modifier.fillMaxSize().padding(16.dp)){Text("AI Mentor",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Ask about code, architecture, debugging, career or authorized security labs.",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));WorkBox(q,{q=it},"Ask your mentor…");Button(enabled=q.isNotBlank()&&!busy,onClick={busy=true;scope.launch{a=GeminiMentorService.askGeminiMentor(c,q,"Adaptive engineering learner");busy=false}},Modifier.fillMaxWidth()){Text(if(busy)"Thinking…" else "Ask mentor")};Spacer(Modifier.height(12.dp));Feedback(a)}}

@Composable private fun College(){LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("College Study",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Syllabus → PYQs → evidence-based priority → practice → revision.",color=MaterialTheme.colorScheme.onSurfaceVariant)};item{EngineCard("Important-question engine","Upload syllabus, PYQs, assignments and teacher material from the College section. Path-Pilot should rank Very High / High / Medium / Low using repetition, syllabus weight, overlap and marks patterns.","No fake 99% prediction. Rankings are evidence-based and never guarantees.")};item{EngineCard("Daily adaptive plan","Keep college classes, commute, coding, cybersecurity and revision in one plan. If a task is missed, the next plan should rebalance rather than simply punish you.","Focus on the next action, not a giant timetable.")}}}
