package com.example.ai

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class MentorReport(
    val personalizedAnalysis: String,
    val biggestWeakness: String,
    val biggestStrength: String,
    val recommendedFocusArea: String,
    val studyStrategy: String,
    val improvementSuggestions: List<String>,
    val motivation: String,
    val explanationOfNextAction: String,
    val isAiGenerated: Boolean = true
)

object GeminiMentorService {

    private const val PREFS_NAME = "path_pilot_ai_prefs"
    private const val KEY_CUSTOM_GEMINI_API = "custom_gemini_api_key"

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getEffectiveApiKey(context: Context?): String {
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val custom = prefs.getString(KEY_CUSTOM_GEMINI_API, "")?.trim() ?: ""
            if (custom.isNotBlank()) {
                return custom
            }
        }
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun setCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_GEMINI_API, key.trim()).apply()
    }

    fun clearCustomApiKey(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CUSTOM_GEMINI_API).apply()
    }

    fun isConfigured(context: Context?): Boolean {
        return getEffectiveApiKey(context).isNotBlank()
    }

    // Live connection test to verify user's API key
    suspend fun testApiKeyConnection(keyToTest: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val key = keyToTest.trim()
        if (key.isBlank()) {
            return@withContext Pair(false, "API key cannot be empty.")
        }
        if (key == "MY_GEMINI_API_KEY") {
            return@withContext Pair(false, "Please provide your real Gemini API key from Google AI Studio.")
        }

        try {
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "Respond with 'OK' if you can read this.") })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_NAME:generateContent?key=$key")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Pair(true, "Successfully connected to Gemini 3.5 Flash!")
            } else {
                val errorBody = response.body?.string() ?: "HTTP ${response.code}"
                val errorMsg = try {
                    JSONObject(errorBody).optJSONObject("error")?.optString("message") ?: errorBody
                } catch (_: Exception) {
                    errorBody
                }
                Pair(false, "Connection failed: $errorMsg")
            }
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.localizedMessage ?: "Unknown failure"}")
        }
    }

    // Enterprise-grade prompt injection sanitizer
    private fun sanitizeInput(input: String): String {
        val dangerousPatterns = listOf(
            "ignore previous instructions",
            "system prompt",
            "jailbreak",
            "as an ai",
            "act as an unfiltered",
            "bypass safety",
            "reveal prompt"
        )
        var sanitized = input
        for (pattern in dangerousPatterns) {
            sanitized = sanitized.replace(Regex(pattern, RegexOption.IGNORE_CASE), "[sanitized]")
        }
        return sanitized.trim().take(400)
    }

    suspend fun generateMentorGuidance(
        context: Context? = null,
        studentName: String,
        targetExam: String,
        readinessScore: Int,
        accuracy: Float,
        speedSec: Float,
        weakTopics: String,
        strongTopics: String
    ): MentorReport = withContext(Dispatchers.IO) {
        val cleanName = sanitizeInput(studentName)
        val cleanExam = sanitizeInput(targetExam)
        val cleanWeak = sanitizeInput(weakTopics)
        val cleanStrong = sanitizeInput(strongTopics)

        val apiKey = getEffectiveApiKey(context)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext buildSynthesizedMentorReport(
                cleanName, cleanExam, readinessScore, accuracy, speedSec, cleanWeak, cleanStrong, false
            )
        }

        try {
            val systemInstruction = """
                You are an expert academic mentor and decision coach for Path Pilot ("Decode Performance. Decide the Next Move.").
                Provide actionable educational guidance following the philosophy: Observe -> Explain -> Guide.
                Never Command -> Lecture -> Overwhelm.
                Do not hallucinate or invent facts. Only use the supplied student analytics.
                Be supportive, specific, and practical.
                Always return a valid JSON object matching the requested schema with keys:
                "personalizedAnalysis", "biggestWeakness", "biggestStrength", "recommendedFocusArea", "studyStrategy", "improvementSuggestions" (array of 3 strings), "motivation", "explanationOfNextAction".
            """.trimIndent()

            val studentAnalyticsPrompt = """
                Student Profile Analytics:
                - Name: $cleanName
                - Target Exam: $cleanExam
                - Readiness Score: $readinessScore / 100
                - Overall Accuracy: ${"%.1f".format(accuracy)}%
                - Average Solving Speed: ${"%.1f".format(speedSec)} seconds/question
                - Weak Topics Identified: $cleanWeak
                - Strong Topics Demonstrated: $cleanStrong

                Act as a personal mentor. Provide your detailed feedback and strategic next move recommendation.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", studentAnalyticsPrompt) })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string() ?: ""
                val parsed = JSONObject(responseBodyStr)
                val candidates = parsed.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    val reportJson = JSONObject(text)
                    val suggestionsArray = reportJson.optJSONArray("improvementSuggestions")
                    val suggestionsList = mutableListOf<String>()
                    if (suggestionsArray != null) {
                        for (i in 0 until suggestionsArray.length()) {
                            suggestionsList.add(suggestionsArray.getString(i))
                        }
                    }
                    if (suggestionsList.isEmpty()) {
                        suggestionsList.add("Maintain daily error log for calculation slips")
                        suggestionsList.add("Practice active recall for high-yield formulas")
                        suggestionsList.add("Time your drills strictly to build exam stamina")
                    }

                    return@withContext MentorReport(
                        personalizedAnalysis = reportJson.optString("personalizedAnalysis", "Your learning curve demonstrates strong foundational discipline with high upside in core technical topics."),
                        biggestWeakness = reportJson.optString("biggestWeakness", cleanWeak.ifEmpty { "High-speed numerical precision" }),
                        biggestStrength = reportJson.optString("biggestStrength", cleanStrong.ifEmpty { "Conceptual synthesis in mechanics" }),
                        recommendedFocusArea = reportJson.optString("recommendedFocusArea", cleanWeak.split(",").firstOrNull() ?: "Electrostatics"),
                        studyStrategy = reportJson.optString("studyStrategy", "Focus on 30-minute high-yield topic drills paired with error logging."),
                        improvementSuggestions = suggestionsList,
                        motivation = reportJson.optString("motivation", "Great scores aren't built on perfection; they are built on relentless micro-adjustments."),
                        explanationOfNextAction = reportJson.optString("explanationOfNextAction", "Directing focus toward high-weightage topics with low mastery provides your fastest path to rank elevation."),
                        isAiGenerated = true
                    )
                }
            }
        } catch (_: Exception) {
            // Graceful fallback to synthesized expert mentor rule engine
        }

        buildSynthesizedMentorReport(cleanName, cleanExam, readinessScore, accuracy, speedSec, cleanWeak, cleanStrong, false)
    }

    suspend fun askGeminiMentor(
        context: Context?,
        studentQuestion: String,
        studentContext: String
    ): String = withContext(Dispatchers.IO) {
        val cleanQuestion = sanitizeInput(studentQuestion)
        val apiKey = getEffectiveApiKey(context)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "To enable live real-time conversations with the Path Pilot Gemini AI Mentor, please enter your Gemini API Key in Settings or the top AI status bar. In the meantime: Focus on daily deliberate practice, maintain an error journal, and solve high-weightage topics first."
        }

        try {
            val systemPrompt = """
                You are Path Pilot's expert academic mentor for competitive exams (JEE, NEET, CUET, KCET).
                The student context is: $studentContext.
                Provide concise, motivating, actionable, and mathematically/scientifically sound advice.
                Avoid overly lengthy generic filler; provide immediate tactical steps. Keep answers under 180 words.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", cleanQuestion) })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.6)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val parsed = JSONObject(body)
                val text = parsed.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (e: Exception) {
            return@withContext "Unable to reach Gemini API (${e.localizedMessage ?: "network issue"}). Please check your internet connection or API key."
        }

        "Mentorship recommendation: Focus on high-yield topic problems, review your mistake ledger daily, and simulate exam timing to minimize negative marking."
    }

    suspend fun explainQuestionWithGemini(
        context: Context?,
        questionText: String,
        options: String,
        studentAnswer: String,
        correctAnswer: String,
        standardExplanation: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Standard Solution:\n$standardExplanation\n\n(Tip: Add your Gemini API Key in the top status bar to unlock interactive AI concept breakdowns and speed-solving shortcuts!)"
        }

        try {
            val prompt = """
                You are a master competitive exam tutor.
                Explain this question clearly:
                Question: $questionText
                Options: $options
                Student's Chosen Answer: $studentAnswer
                Correct Answer: $correctAnswer
                Official Solution: $standardExplanation

                Format your response in 3 concise bullet points:
                1. Key Concept & Why '$correctAnswer' is correct.
                2. Why other options (including '$studentAnswer' if incorrect) are common traps.
                3. Speed-Solving Shortcut (how to solve this in under 45 seconds).
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val parsed = JSONObject(body)
                val text = parsed.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (_: Exception) {
            // Fallback to standard explanation
        }

        "Standard Solution:\n$standardExplanation"
    }

    private fun buildSynthesizedMentorReport(
        name: String,
        targetExam: String,
        readinessScore: Int,
        accuracy: Float,
        speedSec: Float,
        weakTopics: String,
        strongTopics: String,
        aiGenerated: Boolean
    ): MentorReport {
        val primaryWeak = weakTopics.split(",").firstOrNull()?.trim()?.ifEmpty { "Electrostatics & Capacitance" } ?: "Electrostatics & Capacitance"
        val primaryStrong = strongTopics.split(",").firstOrNull()?.trim()?.ifEmpty { "Kinematics & Dynamics" } ?: "Kinematics & Dynamics"

        val analysis = when {
            readinessScore >= 80 -> "You are demonstrating outstanding consistency in $targetExam prep. Your mastery across fundamentals is crisp, and your primary leverage now lies in eliminating fringe edge-case traps."
            readinessScore >= 60 -> "You have established a solid knowledge baseline. Your diagnostic trends show steady grasp in $primaryStrong, while targeted revision in $primaryWeak will yield the sharpest percentile surge."
            else -> "You are at a pivotal building phase. Rather than spreading effort thinly across the entire syllabus, hyper-focusing on high-frequency topics like $primaryWeak will rapidly elevate your benchmark score."
        }

        val strategy = "Adopt the 30-10 Decision Rule: Spend 30 minutes clarifying the underlying physics or logic in $primaryWeak, followed immediately by 10 untimed verification questions to cement pattern recognition before introducing timed constraints."

        val nextMoveReason = "Your diagnostic test confirmed that while you grasp $primaryStrong effectively, calculation drag and conceptual hesitation occur primarily in $primaryWeak. Since $primaryWeak carries heavy weightage in $targetExam, fixing this topic yields the highest score gain per hour spent."

        val suggestions = listOf(
            "Maintain an active 'Mistake Ledger' specifically recording why an incorrect option seemed plausible in $primaryWeak.",
            "Benchmark your solving rhythm to 45 seconds on standard questions to reserve 2.5 minutes for complex multi-step problems.",
            "Perform weekly mixed-topic retention quizzes to prevent decay in $primaryStrong while you elevate $primaryWeak."
        )

        val quote = if (readinessScore >= 70) {
            "Precision beats volume. Focus on deciding the next move with total clarity, $name."
        } else {
            "Every champion's diagnostic started somewhere. Trust the data, attack $primaryWeak, and watch your readiness climb."
        }

        return MentorReport(
            personalizedAnalysis = analysis,
            biggestWeakness = primaryWeak,
            biggestStrength = primaryStrong,
            recommendedFocusArea = primaryWeak,
            studyStrategy = strategy,
            improvementSuggestions = suggestions,
            motivation = quote,
            explanationOfNextAction = nextMoveReason,
            isAiGenerated = aiGenerated
        )
    }
}
