package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiMentorService
import com.example.ai.MentorReport
import com.example.data.db.AppDatabase
import com.example.data.db.DatabaseInitializer
import com.example.data.entity.AttemptEntity
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.StudentDNAEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.UserEntity
import com.example.data.remote.SupabaseService
import com.example.data.remote.SupabaseSyncStatus
import com.example.data.repository.PathPilotRepository
import com.example.engine.NextBestAction
import com.example.engine.NextBestActionEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Screen navigation enum
enum class ScreenState {
    DASHBOARD,
    DIAGNOSTIC_ASSESSMENT,
    ASSESSMENT_RESULTS,
    AI_MENTOR,
    CAREER_DISCOVERY,
    ADMIN_PANEL
}

data class TestSessionState(
    val examId: Int = 1,
    val examName: String = "JEE Main",
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, String> = emptyMap(), // questionId -> "A"|"B"|"C"|"D"
    val questionTimes: Map<Int, Int> = emptyMap(), // questionId -> seconds
    val currentQuestionSeconds: Int = 0,
    val totalElapsedSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val accuracy: Float = 0f
)

class PathPilotViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = PathPilotRepository(db.pathPilotDao())

    // Active User
    private val _currentUserId = MutableStateFlow(1)
    val currentUserId: StateFlow<Int> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = repository.getUser(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allExams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<QuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAttemptsCount: StateFlow<Int> = repository.totalAttemptsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val studentDNA: StateFlow<StudentDNAEntity?> = repository.getStudentDNA(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentAttempts: StateFlow<List<AttemptEntity>> = repository.getAttempts(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current screen navigation
    private val _currentScreen = MutableStateFlow(ScreenState.DASHBOARD)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    // Test Taking Session State
    private val _testSession = MutableStateFlow(TestSessionState())
    val testSession: StateFlow<TestSessionState> = _testSession.asStateFlow()

    // AI Mentor Report State
    private val _mentorReport = MutableStateFlow<MentorReport?>(null)
    val mentorReport: StateFlow<MentorReport?> = _mentorReport.asStateFlow()

    private val _isGeneratingMentor = MutableStateFlow(false)
    val isGeneratingMentor: StateFlow<Boolean> = _isGeneratingMentor.asStateFlow()

    // Gemini API State
    private val _isGeminiConfigured = MutableStateFlow(GeminiMentorService.isConfigured(application))
    val isGeminiConfigured: StateFlow<Boolean> = _isGeminiConfigured.asStateFlow()

    private val _mentorAnswer = MutableStateFlow<String?>(null)
    val mentorAnswer: StateFlow<String?> = _mentorAnswer.asStateFlow()

    private val _isAskingMentor = MutableStateFlow(false)
    val isAskingMentor: StateFlow<Boolean> = _isAskingMentor.asStateFlow()

    private val _aiExplanations = MutableStateFlow<Map<Int, String>>(emptyMap())
    val aiExplanations: StateFlow<Map<Int, String>> = _aiExplanations.asStateFlow()

    private val _explainingQuestionId = MutableStateFlow<Int?>(null)
    val explainingQuestionId: StateFlow<Int?> = _explainingQuestionId.asStateFlow()

    // Next Best Action computed
    private val _nextBestAction = MutableStateFlow<NextBestAction?>(null)
    val nextBestAction: StateFlow<NextBestAction?> = _nextBestAction.asStateFlow()

    // Admin state
    private val _adminNotification = MutableStateFlow<String?>(null)
    val adminNotification: StateFlow<String?> = _adminNotification.asStateFlow()

    // Supabase Cloud Sync state
    private val _supabaseStatus = MutableStateFlow<SupabaseSyncStatus>(
        if (SupabaseService.isConfigured()) SupabaseSyncStatus.Success("Configured with ${SupabaseService.getSupabaseUrl()}")
        else SupabaseSyncStatus.Unconfigured
    )
    val supabaseStatus: StateFlow<SupabaseSyncStatus> = _supabaseStatus.asStateFlow()

    private var testTimerJob: Job? = null

    init {
        viewModelScope.launch {
            // Seed database if needed
            DatabaseInitializer.seedInitialData(db.pathPilotDao())
            // Recompute initial Next Best Action
            refreshNextBestAction()
        }
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun switchUserRole(role: String) {
        viewModelScope.launch {
            if (role == "ADMIN") {
                _currentUserId.value = 2
                _currentScreen.value = ScreenState.ADMIN_PANEL
            } else {
                _currentUserId.value = 1
                _currentScreen.value = ScreenState.DASHBOARD
            }
        }
    }

    fun selectTargetExam(examCode: String) {
        viewModelScope.launch {
            repository.updateTargetExam(_currentUserId.value, examCode)
            refreshNextBestAction()
        }
    }

    fun refreshNextBestAction() {
        viewModelScope.launch {
            val dna = repository.getStudentDNA(_currentUserId.value).firstOrNull()
            val chapters = allChapters.value.ifEmpty { db.pathPilotDao().getAllChapters().firstOrNull() ?: emptyList() }
            val subjects = db.pathPilotDao().getAllSubjects().firstOrNull() ?: emptyList()

            val action = NextBestActionEngine.computeNextBestAction(dna, chapters, subjects)
            _nextBestAction.value = action
        }
    }

    // Diagnostic Assessment Flow
    fun startDiagnosticAssessment(examId: Int = 1, chapterId: Int? = null) {
        viewModelScope.launch {
            val questions = if (chapterId != null) {
                repository.getQuestionsForChapter(chapterId, 6)
            } else {
                repository.getDiagnosticQuestions(examId, 8)
            }

            if (questions.isEmpty()) {
                // Fallback to all questions
                val fallbackList = allQuestions.value.take(6)
                initTestSession(examId, fallbackList)
            } else {
                initTestSession(examId, questions)
            }
        }
    }

    private fun initTestSession(examId: Int, questions: List<QuestionEntity>) {
        val exam = allExams.value.firstOrNull { it.id == examId }
        _testSession.value = TestSessionState(
            examId = examId,
            examName = exam?.name ?: "Diagnostic Assessment",
            questions = questions,
            currentIndex = 0,
            selectedAnswers = emptyMap(),
            questionTimes = emptyMap(),
            currentQuestionSeconds = 0,
            totalElapsedSeconds = 0,
            isCompleted = false
        )
        _currentScreen.value = ScreenState.DIAGNOSTIC_ASSESSMENT
        startTestTimer()
    }

    private fun startTestTimer() {
        testTimerJob?.cancel()
        testTimerJob = viewModelScope.launch {
            while (!_testSession.value.isCompleted) {
                delay(1000)
                _testSession.value = _testSession.value.copy(
                    currentQuestionSeconds = _testSession.value.currentQuestionSeconds + 1,
                    totalElapsedSeconds = _testSession.value.totalElapsedSeconds + 1
                )
            }
        }
    }

    fun selectAnswer(answer: String) {
        val session = _testSession.value
        val currentQ = session.questions.getOrNull(session.currentIndex) ?: return

        val updatedAnswers = session.selectedAnswers.toMutableMap()
        updatedAnswers[currentQ.id] = answer

        val updatedTimes = session.questionTimes.toMutableMap()
        val prevTime = updatedTimes[currentQ.id] ?: 0
        updatedTimes[currentQ.id] = prevTime + session.currentQuestionSeconds

        _testSession.value = session.copy(
            selectedAnswers = updatedAnswers,
            questionTimes = updatedTimes,
            currentQuestionSeconds = 0
        )
    }

    fun navigateQuestion(index: Int) {
        val session = _testSession.value
        if (index in session.questions.indices) {
            val currentQ = session.questions.getOrNull(session.currentIndex)
            val updatedTimes = session.questionTimes.toMutableMap()
            if (currentQ != null) {
                val prev = updatedTimes[currentQ.id] ?: 0
                updatedTimes[currentQ.id] = prev + session.currentQuestionSeconds
            }
            _testSession.value = session.copy(
                currentIndex = index,
                questionTimes = updatedTimes,
                currentQuestionSeconds = 0
            )
        }
    }

    fun submitAssessment() {
        testTimerJob?.cancel()
        viewModelScope.launch {
            val session = _testSession.value
            val questions = session.questions
            val answers = session.selectedAnswers

            var correct = 0
            val completedAttempts = mutableListOf<AttemptEntity>()
            val questionsMap = questions.associateBy { it.id }
            val chaptersMap = allChapters.value.associateBy { it.id }

            for (q in questions) {
                val chosen = answers[q.id] ?: ""
                val isCorrect = chosen.equals(q.correctAnswer, ignoreCase = true)
                if (isCorrect) correct++

                val time = (session.questionTimes[q.id] ?: 40).coerceAtLeast(5)
                repository.recordAttempt(
                    userId = _currentUserId.value,
                    questionId = q.id,
                    selectedAnswer = chosen,
                    isCorrect = isCorrect,
                    timeTakenSec = time
                )
                completedAttempts.add(
                    AttemptEntity(
                        userId = _currentUserId.value,
                        questionId = q.id,
                        selectedAnswer = chosen,
                        correct = isCorrect,
                        timeTakenSec = time
                    )
                )
            }

            val accuracy = if (questions.isNotEmpty()) (correct.toFloat() / questions.size) * 100f else 0f
            val score = correct * 4 - (questions.size - correct) // Standard JEE marking +4, -1

            _testSession.value = session.copy(
                isCompleted = true,
                score = score,
                correctCount = correct,
                totalCount = questions.size,
                accuracy = accuracy
            )

            // Update Student DNA
            val updatedDNA = repository.updateStudentDNAAfterAssessment(
                userId = _currentUserId.value,
                completedAttempts = completedAttempts,
                questionsMap = questionsMap,
                chaptersMap = chaptersMap
            )

            // Recompute Next Best Action
            refreshNextBestAction()

            // Auto trigger AI mentor update
            generateMentorGuidance(updatedDNA)

            // Asynchronously sync Student DNA to Supabase Cloud if configured
            if (SupabaseService.isConfigured()) {
                launch {
                    val synced = SupabaseService.syncStudentDNA(updatedDNA)
                    if (synced) {
                        _supabaseStatus.value = SupabaseSyncStatus.Success("Student DNA synced to Supabase at ${System.currentTimeMillis()}")
                    }
                }
            }

            _currentScreen.value = ScreenState.ASSESSMENT_RESULTS
        }
    }

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseStatus.value = SupabaseSyncStatus.Syncing
            val result = SupabaseService.testConnection()
            _supabaseStatus.value = result
        }
    }

    fun getEffectiveGeminiApiKey(): String {
        return GeminiMentorService.getEffectiveApiKey(getApplication())
    }

    fun saveGeminiApiKey(key: String) {
        GeminiMentorService.setCustomApiKey(getApplication(), key)
        _isGeminiConfigured.value = GeminiMentorService.isConfigured(getApplication())
        generateMentorGuidance()
    }

    fun clearGeminiApiKey() {
        GeminiMentorService.clearCustomApiKey(getApplication())
        _isGeminiConfigured.value = GeminiMentorService.isConfigured(getApplication())
        generateMentorGuidance()
    }

    fun testGeminiApiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = GeminiMentorService.testApiKeyConnection(key)
            onResult(result.first, result.second)
        }
    }

    fun askMentorQuestion(question: String) {
        viewModelScope.launch {
            _isAskingMentor.value = true
            val dna = studentDNA.value
            val user = currentUser.value
            val context = "Student: ${user?.name ?: "Student"}, Exam: ${user?.targetExam ?: "JEE"}, Readiness: ${dna?.readinessScore ?: 70}%, Weak Topics: ${dna?.weakTopics ?: "Electrostatics & Capacitance"}"
            val response = GeminiMentorService.askGeminiMentor(getApplication(), question, context)
            _mentorAnswer.value = response
            _isAskingMentor.value = false
        }
    }

    fun clearMentorAnswer() {
        _mentorAnswer.value = null
    }

    fun explainQuestionWithAI(questionId: Int) {
        viewModelScope.launch {
            val question = _testSession.value.questions.find { it.id == questionId } ?: return@launch
            _explainingQuestionId.value = questionId
            val studentAns = _testSession.value.selectedAnswers[questionId] ?: "Unanswered"
            val optionsStr = "A: ${question.optionA}, B: ${question.optionB}, C: ${question.optionC}, D: ${question.optionD}"
            val explanation = GeminiMentorService.explainQuestionWithGemini(
                context = getApplication(),
                questionText = question.question,
                options = optionsStr,
                studentAnswer = studentAns,
                correctAnswer = question.correctAnswer,
                standardExplanation = question.explanation
            )
            _aiExplanations.value = _aiExplanations.value + (questionId to explanation)
            _explainingQuestionId.value = null
        }
    }

    fun generateMentorGuidance(overrideDNA: StudentDNAEntity? = null) {
        viewModelScope.launch {
            _isGeneratingMentor.value = true
            val user = currentUser.value
            val dna = overrideDNA ?: studentDNA.value ?: repository.getStudentDNA(_currentUserId.value).firstOrNull()

            val report = GeminiMentorService.generateMentorGuidance(
                context = getApplication(),
                studentName = user?.name ?: "Student",
                targetExam = user?.targetExam ?: "JEE",
                readinessScore = dna?.readinessScore ?: 70,
                accuracy = dna?.accuracy ?: 68f,
                speedSec = dna?.speedSec ?: 44f,
                weakTopics = dna?.weakTopics ?: "Electrostatics & Capacitance",
                strongTopics = dna?.strongTopics ?: "Kinematics & Dynamics"
            )

            _mentorReport.value = report
            _isGeneratingMentor.value = false
        }
    }

    // Admin actions
    fun addQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.addQuestion(_currentUserId.value, question)
            _adminNotification.value = "Successfully published new question!"
            delay(3000)
            _adminNotification.value = null
        }
    }

    fun updateQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(_currentUserId.value, question)
            _adminNotification.value = "Updated question #${question.id}"
            delay(3000)
            _adminNotification.value = null
        }
    }

    fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            repository.deleteQuestion(_currentUserId.value, questionId)
            _adminNotification.value = "Deleted question #$questionId"
            delay(3000)
            _adminNotification.value = null
        }
    }

    fun bulkImportCsv(csvText: String, defaultExamId: Int = 1, defaultSubjectId: Int = 1, defaultChapterId: Int = 1) {
        viewModelScope.launch {
            val result = repository.bulkImportQuestions(
                adminId = _currentUserId.value,
                csvText = csvText,
                defaultExamId = defaultExamId,
                defaultSubjectId = defaultSubjectId,
                defaultChapterId = defaultChapterId
            )
            _adminNotification.value = "Import summary: ${result.importedCount} imported, ${result.duplicateCount} duplicates skipped, ${result.failedRowsCount} failed."
            delay(5000)
            _adminNotification.value = null
        }
    }
}
