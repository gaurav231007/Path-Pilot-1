package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.AppDatabase
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AssessmentResultsScreen
import com.example.ui.screens.DeveloperMentorScreen
import com.example.ui.screens.DiagnosticAssessmentScreen
import com.example.ui.screens.LegalTrustDialog
import com.example.ui.screens.MentorGuidanceScreen
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PathPilotViewModel
import com.example.ui.viewmodel.ScreenState
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: PathPilotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PathPilotApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathPilotApp(viewModel: PathPilotViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    var showLegalDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // One-time clean slate for the new Developer Mentor experience.
    // Course/question content stays; old attempts, DNA, audit history and adaptive stats do not.
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("path_pilot_migration", 0)
        if (!prefs.getBoolean("developer_mentor_v1_reset_done", false)) {
            delay(1200)
            val dao = AppDatabase.getInstance(context).pathPilotDao()
            dao.deleteAllAttempts()
            dao.deleteAllStudentDNA()
            dao.deleteAllAuditLogs()
            dao.resetQuestionLearningStats()
            prefs.edit().putBoolean("developer_mentor_v1_reset_done", true).apply()
        }
    }

    if (showLegalDialog) {
        LegalTrustDialog(onDismiss = { showLegalDialog = false })
    }

    val isTakingAssessment = currentScreen == ScreenState.DIAGNOSTIC_ASSESSMENT
    val isAdminMode = currentScreen == ScreenState.ADMIN_PANEL
    val isMentor = currentScreen == ScreenState.AI_MENTOR

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isTakingAssessment) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isMentor) Icons.Default.Psychology else Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isMentor) "AI Mentor" else "Path Pilot",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showLegalDialog = true },
                            modifier = Modifier.testTag("legal_trust_button")
                        ) {
                            Text("i", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(
                            onClick = {
                                if (isAdminMode) viewModel.switchUserRole("STUDENT")
                                else viewModel.switchUserRole("ADMIN")
                            },
                            modifier = Modifier.testTag("role_switcher_button")
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) Icons.Default.School else Icons.Default.AdminPanelSettings,
                                contentDescription = "Switch role",
                                tint = if (isAdminMode) BrandIndigo else MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (!isTakingAssessment && !isAdminMode) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = !isMentor,
                        onClick = { viewModel.navigateTo(ScreenState.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Learn") },
                        label = { Text("Learn") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_learn")
                    )

                    NavigationBarItem(
                        selected = isMentor,
                        onClick = { viewModel.navigateTo(ScreenState.AI_MENTOR) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Mentor") },
                        label = { Text("Mentor") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_mentor")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenState.DASHBOARD -> DeveloperMentorScreen(
                    viewModel = viewModel,
                    onOpenMentor = { viewModel.navigateTo(ScreenState.AI_MENTOR) }
                )

                ScreenState.DIAGNOSTIC_ASSESSMENT -> DiagnosticAssessmentScreen(
                    viewModel = viewModel,
                    onCancel = { viewModel.navigateTo(ScreenState.DASHBOARD) }
                )

                ScreenState.ASSESSMENT_RESULTS -> AssessmentResultsScreen(
                    viewModel = viewModel,
                    onReturnToDashboard = { viewModel.navigateTo(ScreenState.DASHBOARD) },
                    onConsultMentor = { viewModel.navigateTo(ScreenState.AI_MENTOR) }
                )

                ScreenState.AI_MENTOR -> MentorGuidanceScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenState.DASHBOARD) },
                    onStartRecommendedDrill = {
                        val action = viewModel.nextBestAction.value
                        viewModel.startDiagnosticAssessment(examId = 1, chapterId = action?.chapterId)
                    }
                )

                ScreenState.CAREER_DISCOVERY -> DeveloperMentorScreen(
                    viewModel = viewModel,
                    onOpenMentor = { viewModel.navigateTo(ScreenState.AI_MENTOR) }
                )

                ScreenState.ADMIN_PANEL -> AdminPanelScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.switchUserRole("STUDENT") }
                )
            }
        }
    }
}
