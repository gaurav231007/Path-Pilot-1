package com.example.engine

import com.example.data.entity.ChapterEntity
import com.example.data.entity.StudentDNAEntity
import com.example.data.entity.SubjectEntity

data class NextBestAction(
    val topic: String,
    val subjectName: String,
    val timeRequiredMins: Int,
    val questionsRequired: Int,
    val priority: String, // "CRITICAL", "HIGH", "STRATEGIC"
    val expectedScoreBoost: String,
    val weightageInfo: String,
    val observation: String,
    val explanation: String,
    val guidance: String,
    val chapterId: Int
)

object NextBestActionEngine {

    fun computeNextBestAction(
        dna: StudentDNAEntity?,
        chapters: List<ChapterEntity>,
        subjects: List<SubjectEntity>
    ): NextBestAction {
        val subjectMap = subjects.associateBy { it.id }

        // Find weak topics from DNA
        val weakTopicNames = dna?.weakTopics?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

        // Match weak topics with chapters that have HIGH weightage
        val matchedChapter = chapters.firstOrNull { ch ->
            ch.weightage == "HIGH" && weakTopicNames.any { weak -> ch.name.contains(weak, ignoreCase = true) || weak.contains(ch.name, ignoreCase = true) }
        } ?: chapters.firstOrNull { ch ->
            weakTopicNames.any { weak -> ch.name.contains(weak, ignoreCase = true) }
        } ?: chapters.firstOrNull { it.weightage == "HIGH" } ?: chapters.firstOrNull()

        val selectedTopic = matchedChapter?.name ?: "Electrostatics & Capacitance"
        val subjectName = subjectMap[matchedChapter?.subjectId]?.name ?: "Physics"
        val accuracy = dna?.accuracy ?: 65f
        val speed = dna?.speedSec ?: 45f

        val timeMins = matchedChapter?.estimatedMinsToMaster ?: 30
        val questionsCount = if (accuracy < 50f) 12 else 8

        val (observation, explanation, guidance, boost) = when {
            accuracy < 50f -> Quad(
                "Your latest diagnostic assessment indicates repeated stumbling blocks in fundamental relations within $selectedTopic.",
                "Since $selectedTopic accounts for approximately 10-14% of the $subjectName section weightage, resolving foundational gaps here offers the highest immediate scoring dividend.",
                "Spend $timeMins minutes studying core concept summaries and formula derivations, followed by $questionsCount targeted practice problems.",
                "+12 to +16 Marks"
            )
            speed > 60f -> Quad(
                "Your accuracy in $selectedTopic is respectable, but your average time ($speed s) is slowing down your overall paper pacing.",
                "In high-speed competitive examinations, saving 25 seconds per question in $selectedTopic creates crucial buffer time for complex numerical sections.",
                "Practice $questionsCount timed drill questions in $selectedTopic, aiming for under 40 seconds per question with shortcut elimination techniques.",
                "+8 to +12 Marks"
            )
            else -> Quad(
                "Your recent assessment shows steady grasp across baseline concepts, with minor calculation slips in advanced scenarios of $selectedTopic.",
                "Given that $selectedTopic carries critical weightage and frequent multi-concept questions, mastering tricky edge cases will push your percentile into the top bracket.",
                "Review the high-yield trick problem patterns for $timeMins minutes, then challenge yourself with $questionsCount hard-level multi-step questions.",
                "+6 to +10 Marks"
            )
        }

        return NextBestAction(
            topic = selectedTopic,
            subjectName = subjectName,
            timeRequiredMins = timeMins,
            questionsRequired = questionsCount,
            priority = if (matchedChapter?.weightage == "HIGH") "CRITICAL" else "HIGH",
            expectedScoreBoost = boost,
            weightageInfo = "${matchedChapter?.weightage ?: "HIGH"} Exam Weightage (~12%)",
            observation = observation,
            explanation = explanation,
            guidance = guidance,
            chapterId = matchedChapter?.id ?: 1
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
