package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private data class Lesson(val id:String,val title:String,val subtitle:String,val concept:String,val exercise:String,val build:String,val debug:String,val review:String)
private val lessons= listOf(
    Lesson("p1","Programming Foundations","Variables, functions, files and debugging","Learn variables, types, conditions, loops, functions and how to read an error.","Write a small program that accepts input and validates it.","Turn it into a reusable command-line utility.","Intentionally break it, read the error, isolate the cause and fix it.","Write down what failed, why it failed and the rule you learned."),
    Lesson("p2","Python for Real Work","Python syntax → modules → files → errors","Move from syntax to clean functions, modules, data structures and file handling.","Build a small CSV/text processing script.","Add validation and useful output.","Handle a bad file, missing value and invalid input.","Refactor duplicated code and add three test cases."),
    Lesson("b1","How HTTP Works","Request → response → status → headers → body","Understand clients, servers, methods, status codes, headers and JSON.","Inspect GET and POST requests with a local or public API.","Build a tiny API client.","Debug a 404, 400 and 500 response.","Explain one request end-to-end without notes."),
    Lesson("b2","Build a REST API","FastAPI → routes → validation → JSON","Design resources and implement predictable REST endpoints.","Create GET, POST, PUT and DELETE routes.","Build a small notes/tasks API.","Trace validation and route errors.","Write endpoint tests and document the API."),
    Lesson("b3","SQL + PostgreSQL","Tables → queries → relationships","Learn SELECT, INSERT, UPDATE, DELETE, joins, constraints and indexes.","Write queries for a small shop/task database.","Connect your API to PostgreSQL/Supabase.","Debug a bad query and a missing constraint.","Explain why the schema matches the API."),
    Lesson("p3","Authentication + Testing","Auth → authorization → tests","Understand sessions/tokens, roles, validation and test boundaries.","Design protected and public endpoints.","Add auth to the API and write tests.","Debug a failed auth/test case.","Record security assumptions and test coverage."),
    Lesson("w1","Professional Git Workflow","Issue → branch → commit → PR","Work like a professional: requirements, small commits and reviewable changes.","Turn a feature request into a task and branch.","Implement it with focused commits and tests.","Resolve a failing test or merge conflict.","Write a PR summary and review checklist."),
    Lesson("w2","CI/CD + Deployment","Tests → CI → staging → production","Learn what happens after code leaves your machine.","Create a CI checklist for your project.","Build and deploy a small API.","Investigate a failed build or deployment log.","Write a short postmortem and prevention step."),
    Lesson("s1","Linux + Networking","Shell → processes → ports → DNS","Build the foundations needed for secure systems work.","Use basic shell commands and inspect a local service.","Run a small local service and inspect its traffic safely.","Diagnose a port, DNS or permission problem.","Keep a command/reference sheet."),
    Lesson("s2","Web Security Fundamentals","Threat model → input → auth → evidence","Learn common web security concepts in authorized labs only.","Threat-model a toy application.","Test an intentionally vulnerable local lab.","Reproduce and document a safe lab finding.","Give risk, evidence and remediation."),
    Lesson("s3","Security Reporting + Retest","Risk → remediation → retest","Turn technical findings into useful engineering work.","Write a finding with evidence and impact.","Propose a concrete remediation.","Retest the fixed local lab.","Produce a concise security report."),
)

@Composable
fun PathPilotNativeApp(viewModel: PathPilotViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var lessonId by remember { mutableStateOf<String?>(null) }
    val selected = lessons.firstOrNull { it.id == lessonId }
    BackHandler(enabled = selected != null || tab != 0) {
        if (selected != null) lessonId = null else tab = 0
    }
    if (selected != null) LessonScreen(selected) { lessonId = null }
    else MainShell(tab, onTab={tab=it}, onLesson={lessonId=it})
}

@Composable private fun MainShell(tab:Int,onTab:(Int)->Unit,onLesson:(String)->Unit){
    val tabs=listOf("Home","Learn","Mentor","College")
    val icons=listOf(Icons.Default.Home,Icons.Default.Code,Icons.Default.Psychology,Icons.Default.MenuBook)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)){
        Box(Modifier.weight(1f).fillMaxWidth()){
            when(tab){0->HomeTab(onLearn={onTab(1)},onMentor={onTab(2)},onLesson=onLesson);1->LearnTab(onLesson);2->MentorTab();3->CollegeTab()}
        }
        NavigationBar{tabs.forEachIndexed{i,label->NavigationBarItem(selected=tab==i,onClick={onTab(i)},icon={Icon(icons[i],label)},label={Text(label)})}}
    }
}

@Composable private fun HomeTab(onLearn:()->Unit,onMentor:()->Unit,onLesson:(String)->Unit){
    val context=LocalContext.current
    val prefs=remember{context.getSharedPreferences("path_pilot_progress",0)}
    var refresh by remember{mutableIntStateOf(0)}
    val done=lessons.count{prefs.getBoolean(it.id,false)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Text("Your engineering mentor",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Learn → build → debug → review → repeat",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item{Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=BrandIndigo.copy(alpha=.1f))){Column(Modifier.padding(18.dp)){Text("TODAY'S MISSION",fontSize=11.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(lessons.firstOrNull{!prefs.getBoolean(it.id,false)}?.title?:"All core lessons completed",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text(lessons.firstOrNull{!prefs.getBoolean(it.id,false)}?.subtitle?:"Choose a project and keep building",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));Button(onClick={lessons.firstOrNull{!prefs.getBoolean(it.id,false)}?.let{onLesson(it.id)}},modifier=Modifier.fillMaxWidth()){Text("Start lesson")}}}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.fillMaxWidth()){Metric("${done}/${lessons.size}","Lessons",Icons.Default.CheckCircle,BrandEmerald,Modifier.weight(1f));Metric("4","Tracks",Icons.Default.TrackChanges,BrandCyan,Modifier.weight(1f));Metric("0","Projects",Icons.Default.Code,BrandRose,Modifier.weight(1f))}}
        item{Text("Your learning system",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black)}
        item{Card{Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Text("1. Learn the concept",fontWeight=FontWeight.Bold);Text("2. Do a tiny exercise",fontWeight=FontWeight.Bold);Text("3. Build a real feature",fontWeight=FontWeight.Bold);Text("4. Debug what breaks",fontWeight=FontWeight.Bold);Text("5. Review and move to the next skill",fontWeight=FontWeight.Bold)}}}
        item{Button(onClick=onLearn,modifier=Modifier.fillMaxWidth()){Text("Open full lesson plan")};Spacer(Modifier.height(2.dp));OutlinedButton(onClick=onMentor,modifier=Modifier.fillMaxWidth()){Text("Ask AI Mentor")}}
        item{Text("Today's adaptive plan",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black)}
        item{val plan=listOf("College revision — 45m","Developer lesson — 60m","Cybersecurity practice — 45m","Active recall + mistakes — 20m");Column{plan.forEachIndexed{i,t->Row(Modifier.fillMaxWidth().padding(vertical=7.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(i<done%4)Icons.Default.CheckCircle else Icons.Default.CalendarMonth,null,tint=if(i<done%4)BrandEmerald else MaterialTheme.colorScheme.outline);Spacer(Modifier.width(10.dp));Text(t)}}}}
    }
}

@Composable private fun LearnTab(onLesson:(String)->Unit){
    val groups=listOf("Programming Core" to BrandCyan,"Backend Engineering" to BrandEmerald,"Professional Workflow" to BrandIndigo,"Cybersecurity" to BrandRose)
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Text("Lesson plan",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("No dead cards: every lesson opens into a real learning flow.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item{groups.forEachIndexed{i,(name,color)->Card(Modifier.fillMaxWidth().padding(vertical=4.dp),colors=CardDefaults.cardColors(containerColor=color.copy(alpha=.08f))){Column(Modifier.padding(15.dp)){Text("${i+1}. $name",fontWeight=FontWeight.Black);Text(when(i){0->"C → Python → problem solving → debugging";1->"HTTP → REST → FastAPI → SQL → PostgreSQL → auth";2->"Requirements → Git → tests → PR → CI/CD → deploy";else->"Linux → networking → web security → reporting"},fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}
        item{Text("Lessons",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black)}
        items(lessons){lesson->Card(Modifier.fillMaxWidth().clickable{onLesson(lesson.id)},shape=RoundedCornerShape(18.dp)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Surface(shape=CircleShape,color=BrandIndigo.copy(alpha=.12f)){Icon(Icons.Default.MenuBook,null,tint=BrandIndigo,modifier=Modifier.padding(10.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(lesson.title,fontWeight=FontWeight.Black);Text(lesson.subtitle,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text("→",fontSize=24.sp,color=BrandIndigo)}}}
        item{Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){Column(Modifier.padding(16.dp)){Text("Learning loop",fontWeight=FontWeight.Black);Text("Concept → tiny exercise → real feature → bug/debugging → review → repeat.")}}}
    }
}

@Composable private fun LessonScreen(lesson:Lesson,onBack:()->Unit){
    val context=LocalContext.current;val prefs=remember{context.getSharedPreferences("path_pilot_progress",0)};var done by remember{mutableStateOf(prefs.getBoolean(lesson.id,false))}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Row(verticalAlignment=Alignment.CenterVertically){Text("‹",fontSize=38.sp,modifier=Modifier.clickable{onBack()});Spacer(Modifier.width(8.dp));Column{Text("LESSON",fontSize=11.sp,fontWeight=FontWeight.Black,color=BrandIndigo);Text(lesson.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text(lesson.subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
        Section("1 · Concept",lesson.concept,Icons.Default.Lightbulb)
        Section("2 · Tiny exercise",lesson.exercise,Icons.Default.Code)
        Section("3 · Build",lesson.build,Icons.Default.Build)
        Section("4 · Debug",lesson.debug,Icons.Default.BugReport)
        Section("5 · Review",lesson.review,Icons.Default.CheckCircle)
        item{Button(onClick={prefs.edit().putBoolean(lesson.id,!done).apply();done=!done},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text(if(done)"Lesson completed ✓" else "Mark lesson complete")}}
        item{Text("Next step",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text("After this lesson, return to Learn and open the next skill. Your progress stays on this device.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}
@Composable private fun Section(title:String,text:String,icon:androidx.compose.ui.graphics.vector.ImageVector){Card{Row(Modifier.padding(16.dp),verticalAlignment=Alignment.Top){Icon(icon,null,tint=BrandIndigo,modifier=Modifier.size(24.dp));Spacer(Modifier.width(12.dp));Column{Text(title,fontWeight=FontWeight.Black);Spacer(Modifier.height(5.dp));Text(text)}}}}

@Composable private fun MentorTab(){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var q by remember{mutableStateOf("")};var answer by remember{mutableStateOf("Tell me what you are building, learning or debugging. I will guide you step by step.")};var loading by remember{mutableStateOf(false)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("AI Engineering Mentor",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Teacher • Senior Developer • Debugging Coach • Security Mentor",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};item{Card{Column(Modifier.padding(16.dp)){Text(answer);if(loading){Spacer(Modifier.height(12.dp));LinearProgressIndicator(Modifier.fillMaxWidth())}}}};item{OutlinedTextField(value=q,onValueChange={q=it},modifier=Modifier.fillMaxWidth(),label={Text("Ask your mentor")},placeholder={Text("Explain REST APIs / debug my code / plan my project")},minLines=3);Spacer(Modifier.height(8.dp));Button(enabled=!loading&&q.isNotBlank(),onClick={val text=q.trim();loading=true;scope.launch{answer=GeminiMentorService.askGeminiMentor(context,text,"Path-Pilot learner. Follow the lesson plan: programming, backend, professional workflow, cybersecurity in authorized labs, and college study. Use concept → exercise → build → debug → review. No unauthorized security testing and no guaranteed exam predictions.");loading=false;q=""}},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Send,null);Spacer(Modifier.width(7.dp));Text(if(loading)"Thinking…" else "Ask Gemini")}}
    }
}

@Composable private fun CollegeTab(){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var files by remember{mutableStateOf<List<android.net.Uri>>(emptyList())};var names by remember{mutableStateOf<List<String>>(emptyList())};var result by remember{mutableStateOf("Upload syllabus, PYQs, assignments or teacher material. The AI will rank practice priorities from evidence.")};var loading by remember{mutableStateOf(false)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()){u->files=u.take(4);names=files.map{it.lastPathSegment?:"Study material"}}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("College command center",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text("Syllabus → topics → important questions → PYQs → revision",color=MaterialTheme.colorScheme.onSurfaceVariant)};item{Card{Column(Modifier.padding(16.dp)){Text("Study material",fontWeight=FontWeight.Black);Text("Upload up to 4 PDF/image/text files at once.",fontSize=12.sp);Spacer(Modifier.height(10.dp));Button(onClick={picker.launch(arrayOf("application/pdf","image/*","text/*"))},modifier=Modifier.fillMaxWidth()){Text("Select syllabus / PYQs / assignments")}}}};if(names.isNotEmpty())item{Card{Column(Modifier.padding(16.dp)){Text("Selected",fontWeight=FontWeight.Black);names.forEach{Text("• $it",fontSize=12.sp)};Spacer(Modifier.height(10.dp));Button(enabled=!loading,onClick={loading=true;scope.launch{result=StudyMaterialService.analyze(context,files);loading=false}},modifier=Modifier.fillMaxWidth()){Text(if(loading)"Analyzing…" else "Analyze with Gemini")}}}};item{Card{Column(Modifier.padding(16.dp)){Text("Exam intelligence",fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text(result)}}};item{Text("Priority rule",fontWeight=FontWeight.Black);Text("PYQ repetition + syllabus weight + assignment overlap + recent patterns + your mistakes. No fake 99% guarantee.",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
}

@Composable private fun Metric(value:String,label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,tint:Color,modifier:Modifier){Surface(modifier,shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=tint,modifier=Modifier.size(20.dp));Text(value,fontWeight=FontWeight.Black);Text(label,fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
