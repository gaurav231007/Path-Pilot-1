package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ai.StudyMaterialService
import com.example.ai.TeachingEngine
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private data class AcademyLesson(val id:String,val title:String,val subtitle:String,val concept:String,val exercise:String,val build:String,val debug:String,val review:String,val track:String)
private enum class LessonPhase { CONCEPT, LAB, DEBUG, REVIEW, EXAM }

@Composable
fun PathPilotAppV2(viewModel: PathPilotViewModel) {
    val context=androidx.compose.ui.platform.LocalContext.current
    val activity=context as? Activity
    var tab by remember{mutableStateOf(0)}
    var selected by remember{mutableStateOf<AcademyLesson?>(null)}
    BackHandler { if(selected!=null) selected=null else if(tab!=0) tab=0 else activity?.finish() }
    val lessons=remember{academyLessons()}
    Scaffold(bottomBar={if(selected==null) NavigationBar{listOf("Home","Learn","Mentor","College").forEachIndexed{ i,label->NavigationBarItem(tab==i,{tab=i},{Icon(listOf(Icons.Default.Home,Icons.Default.Book,Icons.Default.Psychology,Icons.Default.MenuBook)[i],label)},label={Text(label)})}}}){pad->
        if(selected!=null) AcademyLessonScreen(selected!!,Modifier.padding(pad)){selected=null}
        else when(tab){0->AcademyHome(Modifier.padding(pad),lessons.first()){selected=it};1->AcademyLearn(Modifier.padding(pad),lessons){selected=it};2->MentorScreen(Modifier.padding(pad));else->CollegeScreen(Modifier.padding(pad))}
    }
}

private fun academyLessons()=listOf(
 AcademyLesson("foundations","Programming Foundations","Variables → functions → data → debugging","Build transferable programming thinking before frameworks.","Write a small program that reads input, validates it and produces the correct output for 5 edge cases.","Turn it into a reusable module with tests and clear error handling.","Break one intentional bug, find it with evidence, fix it and explain why it happened.","Defend your choices like a PR review: correctness, readability, tests and edge cases.","Programming"),
 AcademyLesson("python","Python for Real Work","Files → APIs → automation → testing","Use Python to build useful developer tools, not just syntax exercises.","Build a CLI that reads a file, validates records and reports failures.","Convert it into a small reusable package with tests and logging.","Debug a failing test and a malformed input case without guessing.","Review the package as a senior engineer and improve its structure.","AI / Python"),
 AcademyLesson("http","How HTTP Works","Request → server → route → database → response","Understand the full path of a real app request.","Inspect and explain a GET and POST request, including headers, status codes and JSON.","Design an API contract for a small task manager.","Diagnose a 400, 401, 404 and 500 scenario from evidence.","Review the API for correctness, validation and security.","Backend"),
 AcademyLesson("api","Build a REST API","FastAPI → validation → service → DB","Ship a backend feature end to end.","Design and implement CRUD for one resource with validation.","Connect PostgreSQL/Supabase, add tests and error handling.","Fix a broken endpoint using logs, request data and tests.","Perform a production-style code review and write acceptance criteria.","Backend"),
 AcademyLesson("sql","SQL + PostgreSQL","Schema → query → index → transaction","Learn data modeling and queries that survive real applications.","Design tables for a real feature and write CRUD queries.","Add constraints, indexes and a transaction-safe workflow.","Diagnose a wrong result and a slow query from evidence.","Review schema, constraints, security and performance.","Backend"),
 AcademyLesson("auth","Authentication + Testing","Identity → authorization → tests","Build secure access control and prove behavior with tests.","Design login/session/token boundaries and write authorization tests.","Implement protected routes and negative tests.","Find and fix an authorization bug in an authorized lab app.","Review security boundaries and test coverage.","Security"),
 AcademyLesson("git","Professional Git Workflow","Ticket → branch → commit → PR → review","Work like a professional engineering team.","Take a requirement and turn it into a small ticket and branch plan.","Implement, test, commit clearly, open a PR and respond to review feedback.","Resolve a merge conflict and a failing CI check.","Perform a senior PR review and justify every requested change.","Professional"),
 AcademyLesson("cicd","CI/CD + Deployment","Test → build → artifact → staging → production","Learn the delivery loop used by real teams.","Create a build/test pipeline for a small project.","Deploy to a safe staging environment and define rollback steps.","Diagnose a failed build/deploy from logs rather than guessing.","Review reliability, secrets, rollback and observability.","Professional"),
 AcademyLesson("linux","Linux + Networking","Shell → processes → ports → DNS → TLS","Build the operating-system and networking intuition developers need.","Use safe local commands to inspect processes, ports and HTTP traffic.","Run and monitor a small service locally.","Diagnose a local connection failure from evidence.","Review permissions, logs and network exposure.","Systems"),
 AcademyLesson("security","Web Security Fundamentals","Scope → threat model → test → evidence","Learn ethical hacking through authorized labs and defensive engineering.","Threat-model a toy application and identify likely risks.","Test an intentionally vulnerable local/CTF target and document evidence.","Analyze a safe lab vulnerability and explain root cause and remediation.","Write a professional finding with impact, evidence, remediation and retest.","Cybersecurity"),
 AcademyLesson("project","Capstone Engineering Project","Requirement → architecture → code → test → deploy","Combine development, AI and security into a real project.","Choose a real problem and write requirements plus acceptance criteria.","Build the MVP through branches, tests, review and deployment.","Handle real bugs and failed tests without copying a solution.","Present architecture, tradeoffs, security and next improvements.","Capstone")
)

@Composable private fun AcademyHome(modifier:Modifier,lessons:AcademyLesson,onOpen:(AcademyLesson)->Unit){
 LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Text("Path-Pilot",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Black);Text("Your AI engineering academy — learn by doing.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
  item{Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=BrandIndigo.copy(.10f))){Column(Modifier.padding(18.dp)){Text("TODAY'S MISSION",fontSize=11.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(lessons.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text(lessons.subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));Button({onOpen(lessons)},Modifier.fillMaxWidth()){Text("Start guided lesson")}}}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.fillMaxWidth()){StatCard("11","Lessons",Icons.Default.School,BrandCyan,Modifier.weight(1f));StatCard("7","AI roles",Icons.Default.Psychology,BrandEmerald,Modifier.weight(1f));StatCard("∞","Projects",Icons.Default.Code,BrandRose,Modifier.weight(1f))}}
  item{Text("Mastery loop",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black)}
  item{Card{Text("Teacher → Lab → Problem → Debug → Review → Harder problem → Project → Repeat",Modifier.padding(16.dp),fontWeight=FontWeight.Bold)}}
  item{Text("The AI remembers the lesson context and evaluates your attempt. It is not a video library.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}

@Composable private fun AcademyLearn(modifier:Modifier,lessons:List<AcademyLesson>,onOpen:(AcademyLesson)->Unit){LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Engineering Academy",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Programming • AI • Backend • Security • Professional workflow • Capstone",color=MaterialTheme.colorScheme.onSurfaceVariant)};items(lessons){l->Card(Modifier.fillMaxWidth().clickable{onOpen(l)},shape=RoundedCornerShape(18.dp)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(l.title,fontWeight=FontWeight.Black);Text(l.subtitle,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(5.dp));Text(l.track,fontSize=11.sp,color=BrandIndigo,fontWeight=FontWeight.Bold)};Text("Start →",color=BrandIndigo,fontWeight=FontWeight.Bold)}}}}}

@Composable private fun AcademyLessonScreen(lesson:AcademyLesson,modifier:Modifier,onBack:()->Unit){
 val context=androidx.compose.ui.platform.LocalContext.current;val scope=rememberCoroutineScope();var phase by remember{mutableStateOf(LessonPhase.CONCEPT)};var input by remember{mutableStateOf("")};var ai by remember{mutableStateOf("")};var loading by remember{mutableStateOf(false)};var attempts by remember{mutableStateOf(0)}
 fun run(role:TeachingEngine.Role,task:String){loading=true;scope.launch{ai=TeachingEngine.act(context,role,lesson.title,input+"\nTask: $task","Attempts: $attempts\nCurrent phase: $phase");loading=false}}
 Column(modifier.fillMaxSize()){Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){IconButton({onBack()}){Icon(Icons.Default.ArrowBack,"Back")};Column{Text(lesson.title,fontWeight=FontWeight.Black);Text("${lesson.track} • attempt $attempts",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}};LinearProgressIndicator(progress={((phase.ordinal+1)/5f)},Modifier.fillMaxWidth())
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Text(when(phase){LessonPhase.CONCEPT->"1. AI Teacher";LessonPhase.LAB->"2. Lab + Challenge";LessonPhase.DEBUG->"3. Debugging Coach";LessonPhase.REVIEW->"4. Senior Code Review";LessonPhase.EXAM->"5. Examiner — prove mastery"},style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black)}
  item{Card{Column(Modifier.padding(16.dp)){Text("Lesson brief",fontWeight=FontWeight.Black);Text(lesson.concept);Spacer(Modifier.height(10.dp));Text("AI task",fontWeight=FontWeight.Black);Text(when(phase){LessonPhase.CONCEPT->"Understand the idea, then answer the teacher's check question.";LessonPhase.LAB->lesson.exercise;LessonPhase.DEBUG->lesson.debug;LessonPhase.REVIEW->lesson.review;LessonPhase.EXAM->"The examiner will create a new problem from your weak points. Attempt it without a solution."})}}}
  item{OutlinedTextField(input,{input=it},Modifier.fillMaxWidth().heightIn(min=150.dp),label={Text(if(phase==LessonPhase.DEBUG||phase==LessonPhase.REVIEW)"Paste your code / error / explanation" else "Your answer or work")},placeholder={Text("Do the task yourself. Then send your attempt here.")})}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()){Button({attempts++;run(if(phase==LessonPhase.CONCEPT)TeachingEngine.Role.TEACHER else if(phase==LessonPhase.LAB||phase==LessonPhase.EXAM)TeachingEngine.Role.LAB else if(phase==LessonPhase.DEBUG)TeachingEngine.Role.DEBUGGER else TeachingEngine.Role.REVIEWER,"Continue the current phase")},Modifier.weight(1f),enabled=!loading){Text(if(loading)"Thinking…" else when(phase){LessonPhase.CONCEPT->"Teach me";LessonPhase.LAB->"Check my lab";LessonPhase.DEBUG->"Debug this";LessonPhase.REVIEW->"Review my code";LessonPhase.EXAM->"Give me challenge"})};if(phase!=LessonPhase.CONCEPT)Button({run(TeachingEngine.Role.MENTOR,"Choose my next best action")},Modifier.weight(1f),enabled=!loading){Text("Next action")}}}
  if(ai.isNotBlank())item{Card(colors=CardDefaults.cardColors(containerColor=BrandIndigo.copy(.07f))){Column(Modifier.padding(16.dp)){Text("AI mentor feedback",fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text(ai)}}}
  item{if(phase!=LessonPhase.EXAM)Button({phase=LessonPhase.entries[phase.ordinal+1];ai=""},Modifier.fillMaxWidth()){Text("I completed this phase → next") } else Button({onBack()},Modifier.fillMaxWidth()){Text("Finish lesson / save mastery")}}
 }
 }
}

@Composable private fun MentorScreen(modifier:Modifier){val context=androidx.compose.ui.platform.LocalContext.current;val scope=rememberCoroutineScope();var q by remember{mutableStateOf("")};var a by remember{mutableStateOf("I can teach, set labs, debug your code, review your work and create harder challenges. Try: 'teach me HTTP from zero'.")};var loading by remember{mutableStateOf(false)};Column(modifier.fillMaxSize().padding(16.dp)){Text("AI Mentor Room",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Teacher • Debugger • Reviewer • Tech Lead • Security Mentor",fontSize=12.sp,color=BrandIndigo);Spacer(Modifier.height(10.dp));Card(Modifier.weight(1f).fillMaxWidth()){LazyColumn(Modifier.padding(16.dp)){item{Text(a)};if(loading)item{Spacer(Modifier.height(10.dp));LinearProgressIndicator(Modifier.fillMaxWidth())}}};Spacer(Modifier.height(10.dp));OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),placeholder={Text("Ask / paste code / describe your bug…")},trailingIcon={IconButton(!loading&&q.isNotBlank()){loading=true;scope.launch{a=GeminiMentorService.askGeminiMentor(context,q,"Path-Pilot learner. Teach interactively; require an attempt and give hints before solutions.");loading=false;q=""}}){Icon(Icons.Default.Send,"Send")}})}

@Composable private fun CollegeScreen(modifier:Modifier){val context=androidx.compose.ui.platform.LocalContext.current;val scope=rememberCoroutineScope();var files by remember{mutableStateOf<List<Uri>>(emptyList())};var names by remember{mutableStateOf<List<String>>(emptyList())};var result by remember{mutableStateOf("Upload syllabus/PYQs/assignments. AI will rank evidence-backed priorities and build revision actions.")};var loading by remember{mutableStateOf(false)};val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){u->files=u.take(4);names=files.map{it.lastPathSegment?:"Study material"}};LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("College Command Center",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Syllabus → PYQs → practice → revision → adaptive plan",color=MaterialTheme.colorScheme.onSurfaceVariant)};item{Button({picker.launch(arrayOf("application/pdf","image/*","text/*"))},Modifier.fillMaxWidth()){Text("Select study material")}};if(names.isNotEmpty())item{Card{Column(Modifier.padding(16.dp)){names.forEach{Text("• $it",fontSize=12.sp)};Spacer(Modifier.height(10.dp));Button({loading=true;scope.launch{result=StudyMaterialService.analyze(context,files);loading=false}},Modifier.fillMaxWidth()){Text(if(loading)"Analyzing…" else "Analyze + make plan")};if(loading)LinearProgressIndicator(Modifier.fillMaxWidth())}}};item{Card{Column(Modifier.padding(16.dp)){Text("AI study intelligence",fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text(result,fontSize=13.sp)}}};item{Text("Important: priorities are evidence-based estimates, not guaranteed exam questions.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}

@Composable private fun StatCard(value:String,label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,tint:Color,modifier:Modifier){Surface(modifier,shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=tint,modifier=Modifier.size(20.dp));Text(value,fontWeight=FontWeight.Black);Text(label,fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
