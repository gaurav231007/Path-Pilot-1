package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandIndigo
import com.example.ui.viewmodel.PathPilotViewModel

data class CareerTrack(
    val title: String,
    val examCode: String,
    val examName: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val topRoles: List<String>,
    val averagePackage: String,
    val eligibility: String,
    val strategicRoadmap: String,
    val faqs: List<Pair<String, String>>
)

@Composable
fun CareerDiscoveryScreen(
    viewModel: PathPilotViewModel,
    onBack: () -> Unit,
    onStartExamDiagnostic: (examId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("ALL") }
    var expandedTrackTitle by remember { mutableStateOf<String?>("Software & AI Systems Engineering") }

    val careerTracks = listOf(
        CareerTrack(
            title = "Software & AI Systems Engineering",
            examCode = "JEE",
            examName = "JEE Main & Advanced",
            icon = Icons.Default.Code,
            description = "Entry into India's premier Indian Institutes of Technology (IITs) and National Institutes of Technology (NITs).",
            topRoles = listOf("AI Research Engineer", "Systems Architect", "Product Engineer", "Quantitative Developer"),
            averagePackage = "₹18 - ₹45 LPA",
            eligibility = "Class 12 with Physics, Chemistry, and Mathematics (minimum 75% aggregate or top 20 percentile).",
            strategicRoadmap = "Master high-weightage Calculus and Electrostatics early. Maintain 50+ timed numerical problems daily. Calibrate Student DNA through weekly full diagnostics.",
            faqs = listOf(
                "How many attempts are permitted for JEE Advanced?" to "A candidate can attempt JEE Advanced maximum 2 times in consecutive years.",
                "What is the weightage of Class 11 vs 12?" to "Roughly 45% Class 11 concepts and 55% Class 12 concepts."
            )
        ),
        CareerTrack(
            title = "Medical & Clinical Healthcare",
            examCode = "NEET",
            examName = "NEET UG",
            icon = Icons.Default.LocalHospital,
            description = "National gateway for MBBS, BDS, and allied medicine at AIIMS, JIPMER, and premier medical colleges.",
            topRoles = listOf("General Surgeon", "Cardiologist", "Medical Researcher", "Radiologist"),
            averagePackage = "₹12 - ₹35 LPA",
            eligibility = "Class 12 with Physics, Chemistry, Biology/Biotechnology (minimum 50% marks for general category).",
            strategicRoadmap = "Attain 95%+ accuracy in Biology NCERT lines. Solve Physics and Chemistry numericals within 50 seconds per question.",
            faqs = listOf(
                "Is there an upper age limit for NEET?" to "There is currently no upper age limit for NEET UG aspirants.",
                "What is the total mark benchmark for top AIIMS seats?" to "A score of 680+ out of 720 is typical for prime institution admission."
            )
        ),
        CareerTrack(
            title = "Civil Administration & Public Policy",
            examCode = "UPSC",
            examName = "UPSC Civil Services (CSE)",
            icon = Icons.Default.Policy,
            description = "India's highest civil administrative leadership positions leading national governance and diplomacy.",
            topRoles = listOf("District Magistrate (IAS)", "Police Commissioner (IPS)", "Diplomat (IFS)", "Secretary to Govt"),
            averagePackage = "Apex Central Pay Scale + Governance Leadership",
            eligibility = "Graduate degree in any discipline from a recognized university. Minimum age 21 years.",
            strategicRoadmap = "Develop structured analytical writing. Master Indian Polity, Modern History, and Macroeconomics with active news-linkage.",
            faqs = listOf(
                "How many stages are there in UPSC CSE?" to "Three stages: Prelims (Objective), Mains (9 Written papers), and the Personality Test (Interview).",
                "How does Path Pilot help UPSC aspirants?" to "By evaluating conceptual speed in GS Prelims papers and generating Next Best Actions for weak subject modules."
            )
        ),
        CareerTrack(
            title = "Central Government Executive Operations",
            examCode = "SSC",
            examName = "SSC CGL / CHSL",
            icon = Icons.Default.BusinessCenter,
            description = "Staff Selection Commission executive positions in ministries, audit departments, and enforcement directorates.",
            topRoles = listOf("Income Tax Inspector", "Customs Examiner", "Central Excise Inspector", "Assistant Audit Officer"),
            averagePackage = "₹8 - ₹18 LPA + Central Benefits",
            eligibility = "Bachelor's degree for CGL; 12th standard for CHSL examinations.",
            strategicRoadmap = "Eliminate calculation drag in Quantitative Aptitude. Target 25-second solving pace on Logical Reasoning modules.",
            faqs = listOf(
                "What is the examination pattern?" to "Tier I (Computer-based Objective) followed by Tier II domain papers."
            )
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("career_discovery_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Career & Exam Discovery",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Career Goal → Exam Discovery → Strategic Roadmap",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Info Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Decide Your Career Direction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aligning your daily practice with high-impact career milestones ensures every study hour yields tangible advancement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Career Tracks List
        items(careerTracks.size) { index ->
            val track = careerTracks[index]
            val isExpanded = expandedTrackTitle == track.title

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedTrackTitle = if (isExpanded) null else track.title
                    }
                    .testTag("career_track_${track.examCode}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = track.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Exam: ${track.examName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = { expandedTrackTitle = if (isExpanded) null else track.title }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = track.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Compensation Range: ${track.averagePackage}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandEmerald
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            // Top Roles
                            Text(
                                text = "Key Roles & Outcomes",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track.topRoles.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Eligibility
                            Text(
                                text = "Eligibility Criteria",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track.eligibility,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Strategic Roadmap
                            Text(
                                text = "Strategic Preparation Roadmap",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track.strategicRoadmap,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // FAQs
                            Text(
                                text = "Frequently Asked Questions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            track.faqs.forEach { (q, a) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Q: $q",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "A: $a",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Set as Target Exam CTA
                            Button(
                                onClick = {
                                    viewModel.selectTargetExam(track.examCode)
                                    val examId = when (track.examCode) {
                                        "JEE" -> 1
                                        "NEET" -> 2
                                        "UPSC" -> 3
                                        "SSC" -> 4
                                        else -> 1
                                    }
                                    onStartExamDiagnostic(examId)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                            ) {
                                Text("Select ${track.examCode} & Launch Diagnostic")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
