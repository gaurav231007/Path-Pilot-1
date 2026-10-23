package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiMentorService
import com.example.ai.StudyMaterialService
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandRose
import com.example.ui.viewmodel.PathPilotViewModel
import kotlinx.coroutines.launch

private data class Lesson(val id:String,val track:String,val title:String,val subtitle:String,val concept:String,val exercise:String,val build:String,val debug:String,val review:String)
private val lessonPlan=listOf(
 Lesson("p1","Programming","Programming Foundations","Variables, functions, files and debugging","Learn types, conditions, loops, functions and how to read errors.","Write a small input validator.","Turn it into a reusable CLI utility.","Break it intentionally and trace the error.","Record what failed and the rule learned."),
 Lesson("p2","Programming","Python for Real Work","Modules, data structures, files and errors","Move beyond syntax into clean reusable Python.","Process a small CSV/text file.","Add validation and useful output.","Handle missing files and bad input.","Refactor and add three test cases."),
 Lesson("b1","Backend","How HTTP Works","Request, response, status, headers and JSON","Understand clients, servers, methods and status codes.","Inspect GET and POST requests.","Build a tiny API client.","Debug 400, 404 and 500 responses.","Explain one request end-to-end."),
 Lesson("b2","Backend","Build a REST API","Routes, validation and JSON","Design resources and predictable endpoints.","Create GET, POST, PUT and DELETE routes.","Build a notes/tasks API.","Trace route and validation errors.","Write endpoint tests."),
 Lesson("b3","Backend","SQL + PostgreSQL","Tables, queries and relationships","Learn CRUD, joins, constraints and indexes.","Write queries for a task/shop database.","Connect the API to PostgreSQL/Supabase.","Debug a query and constraint error.","Explain why the schema fits the API."),
 Lesson("b4","Backend","Authentication + Testing","Auth, authorization and tests","Understand protected routes, roles and test boundaries.","Design public and protected endpoints.","Add auth and tests to the API.","Debug a failed auth test.","Document security assumptions."),
 Lesson("w1","Professional","Professional Git Workflow","Issue → branch → commit → PR","Practice requirements, branches and reviewable commits.","Turn a feature request into a task and branch.","Implement it with tests and focused commits.","Resolve a failing test or merge conflict.","Write a PR summary."),
 Lesson("w2","Professional","CI/CD + Deployment","Tests, CI, staging and production","Understand what happens after code leaves your machine.","Create a CI checklist.","Build and deploy a small service.","Investigate a failed build log.","Write a short postmortem."),
 Lesson("s1","Security","Linux + Networking","Shell, processes, ports and DNS","Build safe foundations for security work.","Use basic shell commands on your own device/lab.","Run and inspect a local service.","Diagnose a port/DNS/permission issue.","Keep a command reference sheet."),
 Lesson("s2","Security","Web Security Fundamentals","Threat model, input, auth and evidence","Learn web security concepts in authorized labs only.","Threat-model a toy application.","Test an intentionally vulnerable local lab.","Document a safe lab finding.","Give risk, evidence and remediation."),
 Lesson("s3","Security","Security Reporting + Retest","Risk, remediation and retest","Turn findings into useful engineering work.","Write a finding with evidence and impact.","Propose concrete remediation.","Retest the fixed local lab.","Produce a concise security report.")
)

@Composable
fun PathPilotNativeApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var lessonId by remember { mutableStateOf<String?>(null) }
    val lesson=lessonPlan.firstOrNull{it.id==lessonId}
    BackHandler(enabled=lesson!=null || tab!=0){ if(lesson!=null) lessonId=null else tab=0 }
    if(lesson!=null) LessonScreen(lesson){lessonId=null}
    else MainShell(tab,{tab=it},{lessonId=it})
}

@Composable private fun MainShell(tab:Int,onTab:(Int)->Unit,onLesson:(String)->Unit){
    val labels=listOf("Home","Learn","Mentor","College")
    val icons=listOf(Icons.Default.Home,Icons.Default.Code,Icons.Default.Psychology,Icons.Default.MenuBook)
    Column(Modifier.fillMaxSize()){
        Box(Modifier.weight(1f).fillMaxWidth()){
            when(tab){0->HomeTab(onLesson,{onTab(1)},{onTab(2)});1->LearnTab(onLesson);2->MentorTab();3->CollegeTab()}
        }
        NavigationBar{labels.forEachIndexed{i,label->NavigationBarItem(selected=tab==i,onClick={onTab(i)},icon={Icon(icons[i],label)},label={Text(label)})}}
    }
}

@Composable private fun HomeTab(onLesson:(String)->Unit,onLearn:()->Unit,onMentor:()->Unit){
    val context=LocalContext.current;val prefs=remember{context.getSharedPreferences("path_pilot_progress",Context.MODE_PRIVATE)};var refresh by remember{mutableIntStateOf(0)}
    val completed=lessonPlan.count{prefs.getBoolean(it.id,false)};val next=lessonPlan.firstOrNull{!prefs.getBoolean(it.id,false)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Text("Your engineering mentor",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Learn → build → debug → review → repeat",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item{Card(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp)){Text("TODAY'S MISSION",fontSize=11.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(next?.title?:"Core learning plan complete",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text(next?.subtitle?:"Choose a project and keep building.",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));Button(enabled=next!=null,onClick={next?.let{onLesson(it.id)}},modifier=Modifier.fillMaxWidth()){Text("Start lesson")}}}}
        item{Text("Progress: $completed / ${lessonPlan.size} lessons",fontWeight=FontWeight.Bold);LinearProgressIndicator(progress={completed.toFloat()/lessonPlan.size},Modifier.fillMaxWidth())}
        item{Button(onClick=onLearn,modifier=Modifier.fillMaxWidth()){Text("Open full lesson plan")};Spacer(Modifier.height(8.dp));OutlinedButton(onClick=onMentor,modifier=Modifier.fillMaxWidth()){Text("Ask AI Mentor")}}
        item{Text("Adaptive daily plan",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black)}
        item{val plan=listOf("College revision — 45m","Developer lesson — 60m","Cybersecurity practice — 45m","Active recall + mistakes — 20m");Column{plan.forEachIndexed{i,t->Row(Modifier.fillMaxWidth().clickable{refresh++}.padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(i<completed%4)Icons.Default.CheckCircle else Icons.Default.CalendarMonth,null,tint=if(i<completed%4)BrandEmerald else MaterialTheme.colorScheme.outline);Spacer(Modifier.width(10.dp));Text(t)}}}}
    }
}

@Composable private fun LearnTab(onLesson:(String)->Unit){
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{Text("Lesson plan",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Every card opens a real lesson with exercise, build, debugging and review.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        items(lessonPlan){lesson->Card(Modifier.fillMaxWidth().clickable{onLesson(lesson.id)},shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp)){Text(lesson.track.uppercase(),fontSize=10.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(lesson.title,fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleMedium);Text(lesson.subtitle,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(6.dp));Text("Open lesson →",fontSize=12.sp,fontWeight=FontWeight.Bold)}}}
        item{Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){Column(Modifier.padding(16.dp)){Text("Learning loop",fontWeight=FontWeight.Black);Text("Concept → tiny exercise → real feature → bug/debugging → review → repeat.")}}}
    }
}

@Composable private fun LessonScreen(lesson:Lesson,onBack:()->Unit){
    val context=LocalContext.current;val prefs=remember{context.getSharedPreferences("path_pilot_progress",Context.MODE_PRIVATE)};var done by remember{mutableStateOf(prefs.getBoolean(lesson.id,false))}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")};Column{Text(lesson.track.uppercase(),fontSize=10.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(lesson.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text(lesson.subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
        item{LessonSection("1 · Concept",lesson.concept,Icons.Default.Lightbulb)}
        item{LessonSection("2 · Tiny exercise",lesson.exercise,Icons.Default.Code)}
        item{LessonSection("3 · Build",lesson.build,Icons.Default.Build)}
        item{LessonSection("4 · Debug",lesson.debug,Icons.Default.BugReport)}
        item{LessonSection("5 · Review",lesson.review,Icons.Default.CheckCircle)}
        item{Button(onClick={prefs.edit().putBoolean(lesson.id,!done).apply();done=!done},modifier=Modifier.fillMaxWidth()){Text(if(done)"Lesson completed ✓" else "Mark lesson complete")}}
    }
}
@Composable private fun LessonSection(title:String,text:String,icon:androidx.compose.ui.graphics.vector.ImageVector){Card{Row(Modifier.padding(16.dp),verticalAlignment=Alignment.Top){Icon(icon,null,tint=BrandIndigo,modifier=Modifier.size(24.dp));Spacer(Modifier.width(12.dp));Column{Text(title,fontWeight=FontWeight.Black);Spacer(Modifier.height(5.dp));Text(text)}}}}

@Composable private fun MentorTab(){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var q by remember{mutableStateOf("")};var answer by remember{mutableStateOf("Tell me what you are building, learning or debugging.")};var loading by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().padding(16.dp)){Text("AI Engineering Mentor",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Teacher • Senior Developer • Debugging Coach • Security Mentor",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));Card(Modifier.weight(1f).fillMaxWidth()){LazyColumn(Modifier.fillMaxSize().padding(16.dp)){item{Text(answer)}}};Spacer(Modifier.height(10.dp));OutlinedTextField(value=q,onValueChange={q=it},modifier=Modifier.fillMaxWidth(),label={Text("Ask Gemini")},placeholder={Text("Explain REST APIs / debug my code / plan my project")},minLines=3);Spacer(Modifier.height(8.dp));Button(enabled=!loading&&q.isNotBlank(),onClick={val text=q.trim();loading=true;scope.launch{answer=GeminiMentorService.askGeminiMentor(context,text,"Path-Pilot learner. Use the lesson plan across programming, backend, professional workflow, cybersecurity in authorized labs and college study. Teach concept → exercise → build → debug → review.");loading=false;q=""}},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Send,null);Spacer(Modifier.width(7.dp));Text(if(loading)"Thinking…" else "Ask Gemini")};if(loading)LinearProgressIndicator(Modifier.fillMaxWidth())}
}

@Composable private fun CollegeTab(){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var files by remember{mutableStateOf<List<android.net.Uri>>(emptyList())};var names by remember{mutableStateOf<List<String>>(emptyList())};var result by remember{mutableStateOf("Upload syllabus, PYQs, assignments or teacher material. The AI will rank priorities from evidence.")};var loading by remember{mutableStateOf(false)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){uris->files=uris.take(4);names=files.map{it.lastPathSegment?:"Study material"}}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Text("College command center",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Syllabus → topics → important questions → PYQs → revision",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item{Card{Column(Modifier.padding(16.dp)){Text("Study material",fontWeight=FontWeight.Black);Text("Upload up to 4 PDF/image/text files.",fontSize=12.sp);Spacer(Modifier.height(10.dp));Button(onClick={picker.launch(arrayOf("application/pdf","image/*","text/*"))},modifier=Modifier.fillMaxWidth()){Text("Select syllabus / PYQs / assignments")}}}}
        if(names.isNotEmpty())item{Card{Column(Modifier.padding(16.dp)){Text("Selected files",fontWeight=FontWeight.Black);names.forEach{Text("• $it",fontSize=12.sp)};Spacer(Modifier.height(10.dp));Button(enabled=!loading,onClick={loading=true;scope.launch{result=StudyMaterialService.analyze(context,files);loading=false}},modifier=Modifier.fillMaxWidth()){Text(if(loading)"Analyzing…" else "Analyze with Gemini")};if(loading)LinearProgressIndicator(Modifier.fillMaxWidth())}}}
        item{Card{Column(Modifier.padding(16.dp)){Text("Exam intelligence",fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text(result,fontSize=12.sp)}}}
        item{Text("Priority method",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text("PYQ repetition + syllabus weight + assignment overlap + recent patterns + your mistakes. No exact-question guarantee.",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp)}
    }
}
