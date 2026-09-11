package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "STUDENT", "ADMIN", "SUPER_ADMIN"
    val targetExam: String, // "JEE", "NEET", "UPSC", "SSC", "BANKING"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String, // "JEE", "NEET", "UPSC", "SSC"
    val name: String,
    val description: String,
    val totalMarks: Int = 300,
    val targetDurationMins: Int = 180
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int,
    val name: String,
    val weightagePercent: Int
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val name: String,
    val weightage: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val highPriority: Boolean = true,
    val estimatedMinsToMaster: Int = 30
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int,
    val subjectId: Int,
    val chapterId: Int,
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String,
    // Dynamic difficulty metrics
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0,
    val averageTimeSec: Int = 45,
    val calculatedDifficulty: String = "MEDIUM" // Recalculated by system
)

@Entity(tableName = "attempts")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val questionId: Int,
    val selectedAnswer: String,
    val correct: Boolean,
    val timeTakenSec: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_dna")
data class StudentDNAEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val readinessScore: Int, // 0 - 100
    val accuracy: Float, // 0 - 100%
    val speedSec: Float, // Avg seconds per question
    val weakTopics: String, // JSON or comma-separated: "Electrostatics, Organic Reaction Mechanisms"
    val strongTopics: String, // "Kinematics, Thermodynamics"
    val totalTestsCompleted: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val adminId: Int,
    val action: String,
    val targetType: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
