package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AttemptEntity
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.StudentDNAEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PathPilotDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE users SET targetExam = :targetExam WHERE id = :userId")
    suspend fun updateUserTargetExam(userId: Int, targetExam: String)

    @Query("SELECT * FROM exams ORDER BY id ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE code = :code LIMIT 1")
    suspend fun getExamByCode(code: String): ExamEntity?

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Int): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Query("SELECT * FROM subjects WHERE examId = :examId ORDER BY id ASC")
    fun getSubjectsByExam(examId: Int): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getChaptersBySubject(subjectId: Int): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: Int): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY id ASC")
    fun getQuestionsByExam(examId: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getDiagnosticQuestions(examId: Int, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByChapter(chapterId: Int, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Int): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Int)

    @Query("UPDATE questions SET totalAttempts = totalAttempts + 1, correctAttempts = correctAttempts + :isCorrect, averageTimeSec = (averageTimeSec + :timeTaken) / 2, calculatedDifficulty = :newDifficulty WHERE id = :questionId")
    suspend fun updateQuestionStats(questionId: Int, isCorrect: Int, timeTaken: Int, newDifficulty: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AttemptEntity): Long

    @Query("SELECT * FROM attempts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAttemptsByUser(userId: Int): Flow<List<AttemptEntity>>

    @Query("SELECT COUNT(*) FROM attempts")
    fun getTotalAttemptsCount(): Flow<Int>

    @Query("DELETE FROM attempts")
    suspend fun deleteAllAttempts()

    @Query("SELECT * FROM student_dna WHERE userId = :userId LIMIT 1")
    fun getDNAByUserId(userId: Int): Flow<StudentDNAEntity?>

    @Query("SELECT * FROM student_dna WHERE userId = :userId LIMIT 1")
    suspend fun getDNAByUserIdDirect(userId: Int): StudentDNAEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDNA(dna: StudentDNAEntity): Long

    @Query("DELETE FROM student_dna")
    suspend fun deleteAllStudentDNA()

    @Query("UPDATE questions SET totalAttempts = 0, correctAttempts = 0, averageTimeSec = 45, calculatedDifficulty = 'MEDIUM'")
    suspend fun resetQuestionLearningStats()

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAllAuditLogs()
}
