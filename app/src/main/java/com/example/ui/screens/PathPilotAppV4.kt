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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
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
private val PPNavy = Color(0xFF111A32)
private val PPGray = Color(0xFF64748B)
private val PPBlueSoft = Color(0xFFEAF5FF)
private val PPPurpleSoft = Color(0xFFF0ECFF)
private val PPGreenSoft = Color(0xFFE9FBF2)
private val PPOrangeSoft = Color(0xFFFFF4DF)

private data class AcademyCourse(
    val id: String,
    val category: String,
    val title: String,
    val level: String,
    val concept: String,
    val mission: String,
    val build: String,
    val debug: String,
    val review: String
)

private val academyCourses = listOf(
    AcademyCourse("c", "Programming", "C Programming", "Beginner", "Syntax, types, conditions, loops, functions, pointers and memory", "Build a menu-driven calculator with validation.", "Split it into functions and add file I/O.", "Debug a pointer or memory bug from a minimal reproduction.", "Explain memory, functions and edge cases."),
    AcademyCourse("cpp", "Programming", "C++ Programming", "Beginner", "OOP, STL, references, templates and modern C++", "Build an inventory manager with vectors and maps.", "Add classes, persistence and tests.", "Trace an iterator, reference or ownership bug.", "Review API design and complexity."),
    AcademyCourse("java", "Programming", "Java Programming", "Beginner", "OOP, collections, exceptions, generics and JVM basics", "Build a task manager using classes and collections.", "Add persistence, validation and unit tests.", "Diagnose exceptions and state bugs.", "Explain OOP trade-offs."),
    AcademyCourse("kotlin", "Programming", "Kotlin for Android", "Beginner", "Null safety, data classes, coroutines, Compose and Android architecture", "Create a small Compose task screen.", "Connect UI state to a repository and local storage.", "Debug lifecycle, state and coroutine issues.", "Review Android architecture."),
    AcademyCourse("python", "Programming", "Python Programming", "Beginner", "Variables, collections, functions, modules, errors and testing", "Build a CLI validator with clean error handling.", "Turn it into a reusable package with tests.", "Trace a broken condition from symptom to root cause.", "Explain Pythonic design and tests."),
    AcademyCourse("javascript", "Web Development", "JavaScript", "Beginner", "DOM, events, async/await, modules and browser APIs", "Build an interactive form with validation.", "Fetch API data and handle loading and error states.", "Debug async, DOM and state bugs.", "Review browser-side code quality."),
    AcademyCourse("typescript", "Web Development", "TypeScript", "Intermediate", "Types, interfaces, generics, narrowing and tooling", "Convert a small JavaScript module to TypeScript.", "Build a typed API client with runtime validation.", "Find type/runtime mismatches.", "Review public types and maintainability."),
    AcademyCourse("html", "Web Development", "HTML5", "Beginner", "Semantic HTML, forms, accessibility and document structure", "Build an accessible product page.", "Add forms, validation and semantic sections.", "Fix accessibility and structure issues.", "Review semantic quality."),
    AcademyCourse("css", "Web Development", "CSS + Responsive UI", "Beginner", "Layout, Flexbox, Grid, responsive design and animations", "Recreate a responsive mobile-first card layout.", "Build a complete responsive landing page.", "Debug overflow, layout and breakpoint bugs.", "Review responsiveness and accessibility."),
    AcademyCourse("react", "Web Development", "React", "Intermediate", "Components, props, state, hooks and data fetching", "Build a searchable task list.", "Add routing, API data and reusable components.", "Debug rendering, state and effect problems.", "Review component boundaries."),
    AcademyCourse("next", "Web Development", "Next.js", "Intermediate", "Routing, server/client components, data fetching and deployment", "Build a small multi-page app.", "Add a database-backed feature and production deployment.", "Debug hydration and server/client issues.", "Review architecture and performance."),
    AcademyCourse("node", "Web Development", "Node.js", "Intermediate", "Runtime, npm, modules, HTTP and async I/O", "Build a small HTTP service.", "Turn it into a production-style REST backend.", "Debug async and dependency problems.", "Review package and API design."),
    AcademyCourse("http", "Backend", "HTTP + Web Fundamentals", "Beginner", "Methods, headers, cookies, status codes, JSON and request lifecycle", "Inspect GET and POST requests and explain every important field.", "Build an API client handling 2xx, 4xx and 5xx responses.", "Diagnose 400, 404 and 500 responses from evidence.", "Explain the request-response lifecycle."),
    AcademyCourse("fastapi", "Backend", "FastAPI REST APIs", "Intermediate", "Routes, schemas, validation, dependencies and API contracts", "Create CRUD endpoints for tasks.", "Build a tested notes API with clean errors.", "Debug routing and validation failures.", "Write endpoint tests and API documentation."),
    AcademyCourse("sql", "Backend", "SQL + PostgreSQL", "Intermediate", "CRUD, joins, constraints, indexes and transactions", "Write queries for users, projects and tasks.", "Connect an API to PostgreSQL or Supabase.", "Find bad joins, constraints and transaction bugs.", "Review schema integrity and query performance."),
    AcademyCourse("auth", "Backend", "Authentication + Security", "Intermediate", "Sessions, JWT, password hashing, authorization and secrets", "Design roles and protected routes for a sample app.", "Implement secure authentication in a local project.", "Diagnose token, cookie and authorization failures.", "Review trust boundaries and threat model."),
    AcademyCourse("git", "Professional", "Git + GitHub Workflow", "Beginner", "Tickets, branches, commits, PRs, reviews and CI", "Turn a feature request into acceptance criteria and a branch plan.", "Implement it with focused commits and tests.", "Read a failing CI check and isolate the cause.", "Write a reviewable PR and review another change."),
    AcademyCourse("testing", "Professional", "Testing + QA", "Intermediate", "Unit, integration, API, regression and test strategy", "Write tests for a small feature before changing it.", "Build a CI test pipeline for a real project.", "Turn a production bug into a regression test.", "Review coverage quality, not just percentage."),
    AcademyCourse("deploy", "Professional", "Docker + Deployment", "Intermediate", "Containers, environment variables, CI/CD and observability", "Containerize a small API.", "Deploy frontend + backend + database safely.", "Debug build, environment and deployment failures.", "Review rollback and monitoring strategy."),
    AcademyCourse("ai", "AI Development", "LLM Application Engineering", "Intermediate", "Prompts, structured output, tool calls, latency, cost and evaluation", "Call an LLM and safely parse structured output.", "Build a bounded AI feature inside a normal application.", "Handle timeout, invalid output and API errors.", "Define deterministic boundaries and evaluations."),
    AcademyCourse("linux", "Cybersecurity", "Linux + Networking Lab", "Beginner", "Processes, permissions, ports, DNS, TCP/IP, HTTP and logs", "Inspect a service in your own lab and explain its ports.", "Run and observe a local service.", "Diagnose a permission, port or DNS problem safely.", "Keep evidence and a reproducible lab notebook."),
    AcademyCourse("websec", "Cybersecurity", "Web Security Testing", "Intermediate", "Scope, authorization, threat modeling, evidence and remediation", "Write scope and trust boundaries before testing.", "Test an intentionally vulnerable local lab only.", "Document a finding with impact and remediation.", "Retest the fix and write a concise report."),
    AcademyCourse("system", "Architecture", "System Design", "Advanced", "Requirements, APIs, data flow, caching, queues and trade-offs", "Design a small service from requirements.", "Produce an architecture and failure-mode plan.", "Analyze a bottleneck or outage scenario.", "Defend trade-offs like a senior engineer."),

    // Automation agency track
    AcademyCourse("auto-foundation", "Automation Agency", "Automation Agency Foundations", "Beginner", "Agency model, automation vs AI, client problems, discovery and ROI", "Map one business process from trigger to outcome.", "Turn the map into a paid automation offer with a clear before/after.", "Find bottlenecks, duplicate work and failure points.", "Explain ROI, scope and the boundary between automation and manual work."),
    AcademyCourse("auto-api", "Automation Agency", "APIs, Webhooks + JSON", "Beginner", "HTTP requests, REST APIs, webhooks, JSON, headers, auth and retries", "Connect two test services with a webhook.", "Build a multi-step API workflow with validation and error paths.", "Diagnose 400, 401, 404, 429 and 500 failures from evidence.", "Review security, idempotency and retry behavior."),
    AcademyCourse("auto-n8n", "Automation Agency", "n8n Workflow Automation", "Beginner", "Triggers, nodes, expressions, branching, loops, credentials and executions", "Build a lead-capture to notification workflow.", "Create a reusable CRM automation with retries and logs.", "Debug a failed execution step-by-step.", "Review workflow readability, cost and reliability."),
    AcademyCourse("auto-make", "Automation Agency", "Make + Zapier Automation", "Beginner", "Scenarios, Zaps, filters, paths, schedulers and app integrations", "Automate form-to-spreadsheet-to-email follow-up.", "Package three repeatable workflows for a small business.", "Handle duplicate records, rate limits and missing fields.", "Compare tools by reliability, cost and client fit."),
    AcademyCourse("auto-sheets", "Automation Agency", "Google Workspace Automation", "Beginner", "Sheets, Forms, Drive, Gmail, Calendar and structured business data", "Build a lead tracker that automatically follows up.", "Create a lightweight operations system for a client.", "Fix broken mappings and inconsistent data.", "Review permissions and data hygiene."),
    AcademyCourse("auto-crm", "Automation Agency", "CRM + Lead Automation", "Intermediate", "Lead capture, qualification, routing, follow-up, pipeline stages and reminders", "Design a lead funnel with automatic routing.", "Build an end-to-end lead management workflow.", "Recover missed leads and failed notifications.", "Measure response time, conversion and automation ROI."),
    AcademyCourse("auto-whatsapp", "Automation Agency", "WhatsApp + Telegram Business Automation", "Intermediate", "Opt-in messaging, templates, bots, notifications, human handoff and state", "Design an opt-in customer notification flow.", "Build a support or order-status workflow with a human fallback.", "Debug state, delivery and webhook issues.", "Review consent, privacy and escalation rules."),
    AcademyCourse("auto-ai", "Automation Agency", "AI Agents + Tool Calling", "Intermediate", "LLM prompts, structured output, tools, memory boundaries and approvals", "Build an AI assistant that classifies incoming requests.", "Give it bounded tools such as CRM lookup and ticket creation.", "Handle hallucinations, invalid tool calls and timeouts.", "Define approval gates, audit logs and deterministic fallbacks."),
    AcademyCourse("auto-ops", "Automation Agency", "Automation Reliability + Monitoring", "Intermediate", "Logs, retries, queues, idempotency, alerts, secrets and incident response", "Add logging and an alert to an existing workflow.", "Build a production-style workflow with recovery paths.", "Simulate a failed API and recover without duplicates.", "Write a mini runbook and postmortem."),
    AcademyCourse("auto-sales", "Automation Agency", "Agency Sales + Client Delivery", "Intermediate", "Niche, offer, discovery call, proposal, onboarding, scope and reporting", "Write a discovery checklist for one niche.", "Create a demo, proposal and onboarding workflow.", "Handle scope creep and unclear requirements.", "Review the offer for measurable business outcomes."),
    AcademyCourse("auto-pricing", "Automation Agency", "Automation Pricing + Retainers", "Intermediate", "Setup fees, recurring support, usage costs, margins and value pricing", "Price one workflow using implementation and ongoing support.", "Build three packages with clear limits and outcomes.", "Identify underpriced support and hidden API costs.", "Defend pricing with ROI and service scope."),

    // Marketing AI agency track
    AcademyCourse("mkt-foundation", "Marketing AI Agency", "Marketing Fundamentals + Positioning", "Beginner", "ICP, positioning, customer pain, awareness, funnel and competitive research", "Choose one niche and write its top five customer pains.", "Create a focused agency positioning statement and offer.", "Diagnose weak positioning from low-quality leads.", "Review whether the offer is specific and measurable."),
    AcademyCourse("mkt-offer", "Marketing AI Agency", "Offer Design + Landing Pages", "Beginner", "Value proposition, proof, CTA, landing-page structure and conversion basics", "Audit a landing page using a simple conversion checklist.", "Build a high-converting service landing page.", "Find friction, unclear messaging and weak CTAs.", "Review copy, trust and mobile UX."),
    AcademyCourse("mkt-copy", "Marketing AI Agency", "AI Copywriting + Content Systems", "Beginner", "Research, hooks, briefs, brand voice, repurposing and editorial workflows", "Turn one customer pain into ten content angles.", "Build a repeatable weekly content engine with AI assistance.", "Fix generic, repetitive or unsupported AI copy.", "Review originality, accuracy and brand consistency."),
    AcademyCourse("mkt-seo", "Marketing AI Agency", "SEO + Search Console", "Intermediate", "Search intent, keyword research, on-page SEO, technical basics and measurement", "Map search intent to pages for a niche.", "Build and measure a small SEO content cluster.", "Diagnose indexing, metadata and intent mismatch issues.", "Review organic growth using evidence, not vanity metrics."),
    AcademyCourse("mkt-social", "Marketing AI Agency", "Social Media Growth Systems", "Beginner", "Content pillars, short-form video, distribution, community and analytics", "Create a 7-day content plan for one niche.", "Build a reusable content production and scheduling system.", "Diagnose low retention, weak hooks and inconsistent publishing.", "Review content against audience intent and metrics."),
    AcademyCourse("mkt-meta", "Marketing AI Agency", "Meta Ads + Lead Generation", "Intermediate", "Campaign structure, creatives, audiences, lead forms, tracking and testing", "Design a small test campaign with one hypothesis.", "Build a lead-generation system with tracking and follow-up.", "Diagnose poor CTR, CPL or lead quality.", "Review experiment design and attribution limits."),
    AcademyCourse("mkt-google", "Marketing AI Agency", "Google Ads + Intent Marketing", "Intermediate", "Search campaigns, keywords, match types, negatives, landing pages and conversion tracking", "Group keywords by commercial intent.", "Build a small search funnel with conversion tracking.", "Diagnose irrelevant traffic and poor conversion paths.", "Review search-term quality and experiment results."),
    AcademyCourse("mkt-analytics", "Marketing AI Agency", "GA4 + Marketing Analytics", "Intermediate", "Events, conversions, attribution, funnels, dashboards and decision metrics", "Define the events a lead-generation site should track.", "Create a simple marketing performance dashboard.", "Find broken events and misleading metrics.", "Explain which metrics actually drive decisions."),
    AcademyCourse("mkt-crm", "Marketing AI Agency", "Email + CRM Nurture Automation", "Intermediate", "Lead magnets, segmentation, sequences, lifecycle stages and personalization", "Design a 5-step nurture sequence for qualified leads.", "Connect lead capture, CRM stages and automated follow-up.", "Handle duplicate contacts and bad segmentation.", "Review consent, deliverability and message quality."),
    AcademyCourse("mkt-creative", "Marketing AI Agency", "AI Creative Production", "Intermediate", "Creative briefs, image/video workflows, variations, brand systems and QA", "Create a creative brief from one campaign objective.", "Build a repeatable AI-assisted creative pipeline.", "Catch visual, factual and brand inconsistencies.", "Review quality control and human approval points."),
    AcademyCourse("mkt-sales", "Marketing AI Agency", "Agency Sales + Client Reporting", "Intermediate", "Lead qualification, audits, proposals, onboarding, KPIs and monthly reviews", "Create a short audit that identifies three measurable opportunities.", "Build a proposal and reporting workflow for a client.", "Handle attribution disputes and missed targets.", "Review retention, outcomes and next-quarter experiments."),
    AcademyCourse("mkt-capstone", "Marketing AI Agency", "AI Marketing Agency Capstone", "Advanced", "Positioning, landing page, content, SEO, ads, analytics, CRM and automation", "Pick a niche and define one measurable growth goal.", "Build a complete agency demo system from lead capture to reporting.", "Run a simulated campaign issue and document the fix.", "Present the system like a real client engagement with evidence and next steps.")
)

private enum class JourneyPhase { LEARN, LAB, BUILD, DEBUG, REVIEW }

@Composable
fun PathPilotAppV4(viewModel: PathPilotViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<AcademyCourse?>(null) }
    BackHandler(enabled = selected != null) { selected = null }
    Surface(Modifier.fillMaxSize(), color = Color(0xFFF8FBFF)) {
        if (selected != null) {
            CourseJourney(selected!!, onBack = { selected = null })
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        val items = listOf(
                            Triple("Home", Icons.Default.Home, 0),
                            Triple("Learn", Icons.Default.Code, 1),
                            Triple("Agency", Icons.Default.Business, 2),
                            Triple("Mentor", Icons.Default.AutoAwesome, 3),
                            Triple("College", Icons.Default.School, 4)
                        )
                        items.forEach { item ->
                            NavigationBarItem(
                                selected = tab == item.third,
                                onClick = { tab = item.third },
                                icon = { Icon(item.second, contentDescription = item.first) },
                                label = { Text(item.first) }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        0 -> HomeScreen(onOpen = { selected = it }, onAgency = { tab = 2 })
                        1 -> LearnScreen(onOpen = { selected = it })
                        2 -> AgencyScreen(onOpen = { selected = it })
                        3 -> MentorScreen()
                        else -> CollegeScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(43.dp).clip(RoundedCornerShape(13.dp)).background(PPBlue), contentAlignment = Alignment.Center) {
            Text("➤", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row {
                Text("Path", fontSize = 23.sp, fontWeight = FontWeight.Black, color = PPNavy)
                Text("Pilot", fontSize = 23.sp, fontWeight = FontWeight.Black, color = PPBlue)
            }
            Text("AI Engineering + Agency Academy", fontSize = 10.sp, color = PPGray)
        }
        Box(Modifier.size(35.dp).clip(CircleShape).background(PPBlueSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = PPBlue)
        }
    }
}

@Composable
private fun HomeScreen(onOpen: (AcademyCourse) -> Unit, onAgency: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pathpilot_engine", Context.MODE_PRIVATE) }
    val completed = academyCourses.count { prefs.getBoolean("done_${it.id}", false) }
    val next = academyCourses.firstOrNull { !prefs.getBoolean("done_${it.id}", false) }
    LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Header() }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Text("Hi Gaurav 👋", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PPNavy)
                Text("Learn. Build. Automate. Market. Ship.", fontSize = 12.sp, color = PPGray)
            }
        }
        item {
            Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(PPBlueSoft)) {
                Column(Modifier.padding(20.dp)) {
                    Text("REAL-WORLD SKILLS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue)
                    Spacer(Modifier.height(6.dp))
                    Text("From coding to\nreal agency work.", fontSize = 27.sp, lineHeight = 31.sp, fontWeight = FontWeight.Black, color = PPNavy)
                    Spacer(Modifier.height(7.dp))
                    Text("Programming, web development, backend, AI, cybersecurity, automation agencies and marketing AI agencies.", fontSize = 12.sp, color = PPGray)
                    Spacer(Modifier.height(13.dp))
                    Button(onClick = { next?.let(onOpen) }, enabled = next != null, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text("Continue learning")
                    }
                }
            }
        }
        item {
            Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Academy progress", fontSize = 17.sp, fontWeight = FontWeight.Black, color = PPNavy, modifier = Modifier.weight(1f))
                        Text("$completed/${academyCourses.size}", color = PPBlue, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (academyCourses.isEmpty()) 0f else completed.toFloat() / academyCourses.size },
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                        color = PPBlue,
                        trackColor = Color(0xFFDDE8F5)
                    )
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MiniFeature(Modifier.weight(1f), Icons.Default.Code, "Coding", PPBlueSoft)
                MiniFeature(Modifier.weight(1f), Icons.Default.SettingsSuggest, "Automation", PPPurpleSoft)
                MiniFeature(Modifier.weight(1f), Icons.Default.Language, "Marketing", PPGreenSoft)
            }
        }
        item {
            Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Text("AGENCY TRACKS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue)
                    Spacer(Modifier.height(4.dp))
                    Text("Automation Agency + Marketing AI Agency", fontSize = 17.sp, fontWeight = FontWeight.Black, color = PPNavy)
                    Text("Learn tools, delivery, sales, pricing and client retention—not just buttons.", fontSize = 12.sp, color = PPGray)
                    Spacer(Modifier.height(9.dp))
                    OutlinedButton(onClick = onAgency, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("Open agency courses")
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
        item {
            if (next != null) {
                MissionCard(next, onOpen)
            }
        }
    }
}

@Composable
private fun MiniFeature(modifier: Modifier, icon: ImageVector, title: String, background: Color) {
    Card(modifier, shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(background)) {
        Column(Modifier.padding(11.dp)) {
            Icon(icon, contentDescription = null, tint = PPBlue)
            Spacer(Modifier.height(5.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PPNavy)
        }
    }
}

@Composable
private fun MissionCard(course: AcademyCourse, onOpen: (AcademyCourse) -> Unit) {
    Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(16.dp)) {
            Text("TODAY'S MISSION", fontSize = 9.sp, fontWeight = FontWeight.Black, color = PPBlue)
            Text(course.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PPNavy)
            Spacer(Modifier.height(5.dp))
            Text(course.mission, fontSize = 12.sp, color = PPGray)
            Spacer(Modifier.height(9.dp))
            Button(onClick = { onOpen(course) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Start mission") }
        }
    }
}

@Composable
private fun LearnScreen(onOpen: (AcademyCourse) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All", "Programming", "Web Development", "Backend", "Professional", "AI Development", "Cybersecurity", "Architecture", "Capstone")
    val filtered = academyCourses.filter { it.category in categories.drop(1) && (category == "All" || it.category == category) && (query.isBlank() || it.title.contains(query, true) || it.concept.contains(query, true)) }
    LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header() }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Text("Engineering Academy", fontSize = 26.sp, fontWeight = FontWeight.Black, color = PPNavy)
                Text("Languages → web → backend → AI → security → professional workflow.", fontSize = 12.sp, color = PPGray)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, placeholder = { Text("Python, React, SQL, Docker…") }, shape = RoundedCornerShape(14.dp))
            }
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                categories.forEach { c ->
                    Surface(Modifier.clickable { category = c }, shape = RoundedCornerShape(20.dp), color = if (category == c) PPBlue else Color.White) {
                        Text(c, Modifier.padding(horizontal = 13.dp, vertical = 8.dp), fontSize = 10.sp, color = if (category == c) Color.White else PPGray)
                    }
                }
            }
        }
        items(filtered) { CourseRow(it, onOpen) }
    }
}

@Composable
private fun AgencyScreen(onOpen: (AcademyCourse) -> Unit) {
    var selectedTrack by remember { mutableStateOf("All") }
    val tracks = listOf("All", "Automation Agency", "Marketing AI Agency")
    val courses = academyCourses.filter { it.category == "Automation Agency" || it.category == "Marketing AI Agency" }.filter { selectedTrack == "All" || it.category == selectedTrack }
    LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header() }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Text("Agency Academy", fontSize = 27.sp, fontWeight = FontWeight.Black, color = PPNavy)
                Text("Build an agency as an operator: acquire → automate → deliver → measure → retain.", fontSize = 12.sp, color = PPGray)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tracks.forEach { track ->
                        Surface(Modifier.clickable { selectedTrack = track }, shape = RoundedCornerShape(18.dp), color = if (selectedTrack == track) PPBlue else Color.White) {
                            Text(track, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 10.sp, color = if (selectedTrack == track) Color.White else PPGray)
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(PPPurpleSoft)) {
                Column(Modifier.padding(17.dp)) {
                    Text("AUTOMATION AGENCY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue)
                    Text("n8n • Make • APIs • Webhooks • CRM • WhatsApp • AI agents • monitoring • pricing", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PPNavy)
                    Spacer(Modifier.height(4.dp))
                    Text("The goal is not to learn a tool. The goal is to solve a business process reliably.", fontSize = 11.sp, color = PPGray)
                }
            }
        }
        item {
            Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(PPGreenSoft)) {
                Column(Modifier.padding(17.dp)) {
                    Text("MARKETING AI AGENCY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF168A55))
                    Text("Positioning • landing pages • content • SEO • Meta Ads • Google Ads • analytics • CRM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PPNavy)
                    Spacer(Modifier.height(4.dp))
                    Text("AI speeds up production; strategy, measurement and client outcomes stay human-led.", fontSize = 11.sp, color = PPGray)
                }
            }
        }
        items(courses) { CourseRow(it, onOpen) }
    }
}

@Composable
private fun CourseRow(course: AcademyCourse, onOpen: (AcademyCourse) -> Unit) {
    val icon = when (course.category) {
        "Automation Agency" -> Icons.Default.SettingsSuggest
        "Marketing AI Agency" -> Icons.Default.Language
        "Cybersecurity" -> Icons.Default.Security
        "Web Development" -> Icons.Default.Language
        else -> Icons.Default.MenuBook
    }
    Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth().clickable { onOpen(course) }, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(Color.White)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(45.dp).clip(RoundedCornerShape(13.dp)).background(PPBlueSoft), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = PPBlue) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${course.category} • ${course.level}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = PPBlue)
                Text(course.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PPNavy)
                Text(course.concept, fontSize = 11.sp, color = PPGray)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun CourseJourney(course: AcademyCourse, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pathpilot_engine", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf(JourneyPhase.LEARN) }
    var completed by remember { mutableStateOf(prefs.getBoolean("done_${course.id}", false)) }
    var answer by remember { mutableStateOf("") }
    var mentorReply by remember { mutableStateOf("") }
    val phaseText = when (phase) {
        JourneyPhase.LEARN -> course.concept
        JourneyPhase.LAB -> course.mission
        JourneyPhase.BUILD -> course.build
        JourneyPhase.DEBUG -> course.debug
        JourneyPhase.REVIEW -> course.review
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text(course.category, fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue)
                Text(course.title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = PPNavy)
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            JourneyPhase.values().forEach { p ->
                val active = phase == p
                Surface(Modifier.clickable { phase = p }, shape = RoundedCornerShape(18.dp), color = if (active) PPBlue else Color.White) {
                    Text(p.name, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 10.sp, color = if (active) Color.White else PPGray)
                }
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(PPBlueSoft)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("${phase.name} PHASE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue)
                        Spacer(Modifier.height(5.dp))
                        Text(phaseText, fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold, color = PPNavy)
                    }
                }
            }
            item {
                OutlinedTextField(answer, { answer = it }, Modifier.fillMaxWidth(), minLines = 4, label = { Text("Your work / notes / solution") }, shape = RoundedCornerShape(14.dp))
            }
            item {
                Button(onClick = {
                    scope.launch {
                        mentorReply = GeminiMentorService.askGeminiMentor(context, "Course: ${course.title}. Phase: ${phase.name}. Learner response: $answer. Give concise mentor feedback, identify one mistake or gap, then give the next action.", "PathPilot agency and engineering learner")
                    }
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Ask Gemini 3.8 Mentor")
                }
            }
            if (mentorReply.isNotBlank()) {
                item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White)) { Text(mentorReply, Modifier.padding(16.dp), fontSize = 12.sp, color = PPNavy) } }
            }
            item {
                Button(onClick = {
                    prefs.edit().putBoolean("done_${course.id}", true).apply()
                    completed = true
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (completed) "Course mission completed" else "Mark phase work complete")
                }
            }
        }
    }
}

@Composable
private fun MentorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("") }
    val prompts = listOf("Teach me APIs like a senior developer", "Give me an n8n automation challenge", "How do I price my first automation client?", "Build a marketing AI agency plan for a local business")
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header() }
        item { Text("AI Mentor", fontSize = 27.sp, fontWeight = FontWeight.Black, color = PPNavy) }
        item { Text("Teacher + senior developer + agency coach. Ask for reasoning, exercises, debugging and next actions.", fontSize = 12.sp, color = PPGray) }
        item {
            OutlinedTextField(question, { question = it }, Modifier.fillMaxWidth(), minLines = 4, label = { Text("Ask anything about coding or agency work") }, shape = RoundedCornerShape(14.dp))
        }
        item {
            Button(onClick = {
                scope.launch { reply = GeminiMentorService.askGeminiMentor(context, question, "Learner studying software engineering, automation agency and marketing AI agency") }
            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) { Text("Ask Gemini 3.8") }
        }
        if (reply.isNotBlank()) item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(Color.White)) { Text(reply, Modifier.padding(16.dp), fontSize = 12.sp, color = PPNavy) } }
        item { Text("Try these", fontWeight = FontWeight.Bold, color = PPNavy) }
        items(prompts) { prompt -> OutlinedButton(onClick = { question = prompt }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(prompt) } }
    }
}

@Composable
private fun CollegeScreen() {
    LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header() }
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Text("College Mode", fontSize = 27.sp, fontWeight = FontWeight.Black, color = PPNavy)
                Text("Use syllabus + PYQs + assignments + mistakes to rank what deserves study time.", fontSize = 12.sp, color = PPGray)
            }
        }
        item {
            Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(Modifier.padding(17.dp)) {
                    Text("PRIORITY ENGINE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue)
                    Text("Very High / High / Medium / Low", fontSize = 17.sp, fontWeight = FontWeight.Black, color = PPNavy)
                    Spacer(Modifier.height(5.dp))
                    Text("Rank questions from evidence such as PYQ repetition, syllabus weight, assignment overlap, marks pattern and your mistakes. Never promise an exact exam question.", fontSize = 12.sp, color = PPGray)
                }
            }
        }
        item { StudyCard("Today", "1 concept + 3 questions + 1 revision block", PPBlueSoft) }
        item { StudyCard("After college", "Review mistakes and convert one weak topic into a tiny practice task", PPGreenSoft) }
        item { StudyCard("Weekend", "PYQ analysis + timed practice + next-week plan", PPOrangeSoft) }
    }
}

@Composable
private fun StudyCard(title: String, body: String, bg: Color) {
    Card(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(bg)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = PPBlue)
            Text(body, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PPNavy)
        }
    }
}
