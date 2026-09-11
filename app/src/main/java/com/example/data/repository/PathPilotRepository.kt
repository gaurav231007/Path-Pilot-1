package com.example.data.repository

import com.example.data.dao.PathPilotDao
import com.example.data.entity.AttemptEntity
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.StudentDNAEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class PathPilotRepository(private val dao: PathPilotDao) {

    val allExams: Flow<List<ExamEntity>> = dao.getAllExams()
    val allQuestions: Flow<List<QuestionEntity>> = dao.getAllQuestions()
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAuditLogs()
    val totalAttemptsCount: Flow<Int> = dao.getTotalAttemptsCount()

    fun getUser(userId: Int): Flow<UserEntity?> = dao.getUserById(userId)
    fun getStudentDNA(userId: Int): Flow<StudentDNAEntity?> = dao.getDNAByUserId(userId)
    fun getAttempts(userId: Int): Flow<List<AttemptEntity>> = dao.getAttemptsByUser(userId)

    suspend fun getDiagnosticQuestions(examId: Int, limit: Int = 10): List<QuestionEntity> {
        return dao.getDiagnosticQuestions(examId, limit)
    }

    suspend fun getQuestionsForChapter(chapterId: Int, limit: Int = 5): List<QuestionEntity> {
        return dao.getQuestionsByChapter(chapterId, limit)
    }

    suspend fun updateTargetExam(userId: Int, examCode: String) {
        dao.updateUserTargetExam(userId, examCode)
    }

    suspend fun recordAttempt(
        userId: Int,
        questionId: Int,
        selectedAnswer: String,
        isCorrect: Boolean,
        timeTakenSec: Int
    ) {
        // 1. Insert attempt
        dao.insertAttempt(
            AttemptEntity(
                userId = userId,
                questionId = questionId,
                selectedAnswer = selectedAnswer,
                correct = isCorrect,
                timeTakenSec = timeTakenSec
            )
        )

        // 2. Dynamically recalculate Question difficulty
        val question = dao.getQuestionById(questionId)
        if (question != null) {
            val newTotal = question.totalAttempts + 1
            val newCorrect = question.correctAttempts + (if (isCorrect) 1 else 0)
            val correctPercent = (newCorrect.toFloat() / newTotal.toFloat()) * 100f
            val newDifficulty = when {
                correctPercent >= 72f -> "EASY"
                correctPercent >= 40f -> "MEDIUM"
                else -> "HARD"
            }
            dao.updateQuestionStats(
                questionId = questionId,
                isCorrect = if (isCorrect) 1 else 0,
                timeTaken = timeTakenSec,
                newDifficulty = newDifficulty
            )
        }
    }

    suspend fun updateStudentDNAAfterAssessment(
        userId: Int,
        completedAttempts: List<AttemptEntity>,
        questionsMap: Map<Int, QuestionEntity>,
        chaptersMap: Map<Int, ChapterEntity>
    ): StudentDNAEntity {
        val existingDNA = dao.getDNAByUserIdDirect(userId)

        val totalSessionQuestions = completedAttempts.size
        val correctSessionQuestions = completedAttempts.count { it.correct }
        val sessionAccuracy = if (totalSessionQuestions > 0) (correctSessionQuestions.toFloat() / totalSessionQuestions) * 100f else 0f
        val sessionAvgTime = if (totalSessionQuestions > 0) completedAttempts.map { it.timeTakenSec }.average().toFloat() else 40f

        val totalTests = (existingDNA?.totalTestsCompleted ?: 0) + 1
        val totalQuestions = (existingDNA?.totalQuestionsAnswered ?: 0) + totalSessionQuestions

        // Smooth moving average for accuracy and speed
        val updatedAccuracy = if (existingDNA != null && existingDNA.totalQuestionsAnswered > 0) {
            ((existingDNA.accuracy * existingDNA.totalQuestionsAnswered) + (sessionAccuracy * totalSessionQuestions)) / totalQuestions
        } else {
            sessionAccuracy
        }

        val updatedSpeed = if (existingDNA != null && existingDNA.totalQuestionsAnswered > 0) {
            ((existingDNA.speedSec * existingDNA.totalQuestionsAnswered) + (sessionAvgTime * totalSessionQuestions)) / totalQuestions
        } else {
            sessionAvgTime
        }

        // Track chapter performance in this session
        val chapterStats = mutableMapOf<String, Pair<Int, Int>>() // Name -> (correct, total)
        for (attempt in completedAttempts) {
            val q = questionsMap[attempt.questionId] ?: continue
            val ch = chaptersMap[q.chapterId]
            val chName = ch?.name ?: "General Topic"
            val prev = chapterStats[chName] ?: Pair(0, 0)
            chapterStats[chName] = Pair(prev.first + (if (attempt.correct) 1 else 0), prev.second + 1)
        }

        val weakSet = mutableSetOf<String>()
        val strongSet = mutableSetOf<String>()

        // Carry over prior weak/strong topics if not tested yet
        existingDNA?.weakTopics?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { weakSet.addAll(it) }
        existingDNA?.strongTopics?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { strongSet.addAll(it) }

        for ((chName, stats) in chapterStats) {
            val chAccuracy = (stats.first.toFloat() / stats.second.toFloat()) * 100f
            if (chAccuracy < 60f) {
                weakSet.add(chName)
                strongSet.remove(chName)
            } else if (chAccuracy >= 75f) {
                strongSet.add(chName)
                weakSet.remove(chName)
            }
        }

        // Calculate Readiness Score (0-100)
        // Factors:
        // - Accuracy score (up to 55 pts)
        // - Speed efficiency (benchmarked around 40-50 sec/question: up to 25 pts)
        // - Mastery balance (strong vs weak topics: up to 20 pts)
        val accuracyPts = (updatedAccuracy * 0.55f).coerceIn(0f, 55f)
        val speedFactor = ((90f - updatedSpeed.coerceIn(20f, 90f)) / 70f) * 25f
        val masteryBalancePts = if (strongSet.size + weakSet.size > 0) {
            (strongSet.size.toFloat() / (strongSet.size + weakSet.size).toFloat()) * 20f
        } else {
            10f
        }
        val calculatedReadiness = min(100, max(15, (accuracyPts + speedFactor + masteryBalancePts).roundToInt()))

        val updatedDNA = StudentDNAEntity(
            id = existingDNA?.id ?: 0,
            userId = userId,
            readinessScore = calculatedReadiness,
            accuracy = updatedAccuracy,
            speedSec = updatedSpeed,
            weakTopics = weakSet.take(4).joinToString(", "),
            strongTopics = strongSet.take(4).joinToString(", "),
            totalTestsCompleted = totalTests,
            totalQuestionsAnswered = totalQuestions,
            lastUpdated = System.currentTimeMillis()
        )

        dao.insertOrUpdateDNA(updatedDNA)
        return updatedDNA
    }

    // Admin operations
    suspend fun addQuestion(adminId: Int, question: QuestionEntity): Long {
        val id = dao.insertQuestion(question)
        dao.insertAuditLog(
            AuditLogEntity(
                adminId = adminId,
                action = "CREATE_QUESTION",
                targetType = "QUESTION",
                details = "Added question ID: $id on chapter ${question.chapterId} [${question.difficulty}]"
            )
        )
        return id
    }

    suspend fun updateQuestion(adminId: Int, question: QuestionEntity) {
        dao.updateQuestion(question)
        dao.insertAuditLog(
            AuditLogEntity(
                adminId = adminId,
                action = "UPDATE_QUESTION",
                targetType = "QUESTION",
                details = "Updated question ID: ${question.id}"
            )
        )
    }

    suspend fun deleteQuestion(adminId: Int, questionId: Int) {
        dao.deleteQuestionById(questionId)
        dao.insertAuditLog(
            AuditLogEntity(
                adminId = adminId,
                action = "DELETE_QUESTION",
                targetType = "QUESTION",
                details = "Deleted question ID: $questionId"
            )
        )
    }

    data class BulkImportResult(
        val totalProcessed: Int,
        val importedCount: Int,
        val duplicateCount: Int,
        val failedRowsCount: Int,
        val sampleImportedNames: List<String>
    )

    suspend fun bulkImportQuestions(
        adminId: Int,
        csvText: String,
        defaultExamId: Int,
        defaultSubjectId: Int,
        defaultChapterId: Int
    ): BulkImportResult {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var imported = 0
        var duplicates = 0
        var failed = 0
        val sampleNames = mutableListOf<String>()

        val existingQuestions = dao.getAllQuestions().firstOrNull() ?: emptyList()
        val existingTexts = existingQuestions.map { it.question.lowercase().trim() }.toSet()

        val batchToInsert = mutableListOf<QuestionEntity>()

        for (line in lines) {
            // Ignore header row
            if (line.startsWith("Question", ignoreCase = true) || line.startsWith("ID,", ignoreCase = true)) {
                continue
            }
            // Parse CSV fields (comma or pipe separated)
            val parts = if (line.contains("|")) line.split("|") else line.split(",")
            if (parts.size >= 6) {
                val qText = parts[0].trim().removeSurrounding("\"")
                val optA = parts.getOrNull(1)?.trim()?.removeSurrounding("\"") ?: "Option A"
                val optB = parts.getOrNull(2)?.trim()?.removeSurrounding("\"") ?: "Option B"
                val optC = parts.getOrNull(3)?.trim()?.removeSurrounding("\"") ?: "Option C"
                val optD = parts.getOrNull(4)?.trim()?.removeSurrounding("\"") ?: "Option D"
                val ans = parts.getOrNull(5)?.trim()?.removeSurrounding("\"")?.uppercase() ?: "A"
                val diff = parts.getOrNull(6)?.trim()?.removeSurrounding("\"")?.uppercase() ?: "MEDIUM"
                val expl = parts.getOrNull(7)?.trim()?.removeSurrounding("\"") ?: "Derived from standard syllabus guidelines."

                if (existingTexts.contains(qText.lowercase())) {
                    duplicates++
                } else {
                    val entity = QuestionEntity(
                        examId = defaultExamId,
                        subjectId = defaultSubjectId,
                        chapterId = defaultChapterId,
                        difficulty = if (diff in listOf("EASY", "MEDIUM", "HARD")) diff else "MEDIUM",
                        question = qText,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        optionD = optD,
                        correctAnswer = if (ans in listOf("A", "B", "C", "D")) ans else "A",
                        explanation = expl,
                        totalAttempts = 0,
                        correctAttempts = 0,
                        averageTimeSec = 45,
                        calculatedDifficulty = diff
                    )
                    batchToInsert.add(entity)
                    imported++
                    if (sampleNames.size < 3) sampleNames.add(qText)
                }
            } else {
                failed++
            }
        }

        if (batchToInsert.isNotEmpty()) {
            dao.insertQuestions(batchToInsert)
            dao.insertAuditLog(
                AuditLogEntity(
                    adminId = adminId,
                    action = "BULK_IMPORT_QUESTIONS",
                    targetType = "QUESTIONS_BATCH",
                    details = "Imported ${batchToInsert.size} questions, $duplicates duplicates skipped, $failed malformed rows."
                )
            )
        }

        return BulkImportResult(
            totalProcessed = lines.size,
            importedCount = imported,
            duplicateCount = duplicates,
            failedRowsCount = failed,
            sampleImportedNames = sampleNames
        )
    }
}
