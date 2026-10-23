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
    private const val MODEL_NAME = "gemini-3.8-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun getEffectiveApiKey(context: Context?): String {
        if (context != null) {
            val custom = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CUSTOM_GEMINI_API, "")?.trim().orEmpty()
            if (custom.isNotBlank()) return custom
        }
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") return buildKey
        val explicitKey = BuildConfig.PATH_PILOT_GEMINI_API_KEY.trim()
        if (explicitKey.isNotBlank() && explicitKey != "MY_GEMINI_API_KEY") return explicitKey
        return ""
    }

    fun setCustomApiKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_CUSTOM_GEMINI_API, key.trim()).apply()
    }

    fun clearCustomApiKey(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .remove(KEY_CUSTOM_GEMINI_API).apply()
    }

    fun isConfigured(context: Context?): Boolean = getEffectiveApiKey(context).isNotBlank()

    suspend fun testApiKeyConnection(keyToTest: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val key = keyToTest.trim()
        if (key.isBlank()) return@withContext false to "API key is empty."
        try {
            val body = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", "Reply only OK.")))
                }))
            }
            val request = Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$key")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (response.isSuccessful) true to "Gemini connected successfully."
                else false to "Gemini HTTP ${response.code}: ${extractError(raw)}"
            }
        } catch (e: Exception) {
            false to "Network error: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    private fun sanitizeInput(input: String): String {
        val patterns = listOf("ignore previous instructions", "system prompt", "jailbreak", "bypass safety", "reveal prompt")
        var result = input
        patterns.forEach { result = result.replace(Regex(it, RegexOption.IGNORE_CASE), "[sanitized]") }
        return result.trim().take(4000)
    }

    suspend fun askGeminiMentor(context: Context?, studentQuestion: String, studentContext: String): String = withContext(Dispatchers.IO) {
        val key = getEffectiveApiKey(context)
        if (key.isBlank()) return@withContext "Gemini is not configured in this build. Please add your Gemini API key in the app settings/status bar."

        val question = sanitizeInput(studentQuestion)
        val systemPrompt = """
            You are Path-Pilot's personal engineering mentor.
            Teach software engineering, backend development, professional development workflow,
            cybersecurity in authorized environments, and college subjects.
            Be practical: explain the concept, show a small example, then give the next action.
            Do not give instructions for unauthorized access or attacks.
            Keep the response under 220 words unless the learner asks for depth.
            Learner context: $studentContext
        """.trimIndent()

        try {
            val body = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", question)))
                }))
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("generationConfig", JSONObject().apply { put("temperature", 0.5) })
            }
            val request = Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$key")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext "Gemini request failed (HTTP ${response.code}). ${extractError(raw)}"
                val text = JSONObject(raw).optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                    ?.optString("text").orEmpty().trim()
                if (text.isNotBlank()) return@withContext text
                "Gemini returned no text. Please try again."
            }
        } catch (e: Exception) {
            "Gemini network error: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    suspend fun generateMentorGuidance(
        context: Context? = null, studentName: String, targetExam: String, readinessScore: Int,
        accuracy: Float, speedSec: Float, weakTopics: String, strongTopics: String
    ): MentorReport = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context)
        val cleanName = sanitizeInput(studentName)
        val weak = sanitizeInput(weakTopics)
        val strong = sanitizeInput(strongTopics)
        if (apiKey.isBlank()) return@withContext fallbackReport(cleanName, weak, strong, false)
        try {
            val prompt = "Learner: $cleanName\nFocus: ${sanitizeInput(targetExam)}\nReadiness: $readinessScore/100\nAccuracy: $accuracy%\nSpeed: $speedSec sec/question\nWeak: $weak\nStrong: $strong\nGive practical next steps. Return JSON with personalizedAnalysis, biggestWeakness, biggestStrength, recommendedFocusArea, studyStrategy, improvementSuggestions, motivation, explanationOfNextAction."
            val body = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().apply { put("parts", JSONArray().put(JSONObject().put("text", prompt))) }))
                put("generationConfig", JSONObject().apply { put("temperature", 0.4); put("responseMimeType", "application/json") })
            }
            val request = Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext fallbackReport(cleanName, weak, strong, false)
                val text = JSONObject(raw).optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")
                    ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
                val json = JSONObject(text)
                val suggestions = mutableListOf<String>()
                json.optJSONArray("improvementSuggestions")?.let { a -> for (i in 0 until a.length()) suggestions.add(a.optString(i)) }
                if (suggestions.isEmpty()) suggestions.addAll(listOf("Keep a daily mistake log", "Build one small feature", "Review weak topics with active recall"))
                MentorReport(
                    json.optString("personalizedAnalysis", "Keep building consistently."),
                    json.optString("biggestWeakness", weak.ifBlank { "Not enough evidence yet" }),
                    json.optString("biggestStrength", strong.ifBlank { "Consistency" }),
                    json.optString("recommendedFocusArea", weak.ifBlank { "Current learning mission" }),
                    json.optString("studyStrategy", "Learn, build, debug, review, repeat."),
                    suggestions.take(3), json.optString("motivation", "Small daily progress compounds."),
                    json.optString("explanationOfNextAction", "Focus on the weakest evidence-backed area first."), true
                )
            }
        } catch (_: Exception) {
            fallbackReport(cleanName, weak, strong, false)
        }
    }

    suspend fun explainQuestionWithGemini(context: Context?, questionText: String, options: String, studentAnswer: String, correctAnswer: String, standardExplanation: String): String =
        askGeminiMentor(context, "Explain this college question step by step. Question: $questionText Options: $options Student answer: $studentAnswer Correct answer: $correctAnswer Official explanation: $standardExplanation", "The learner wants conceptual understanding, not just the answer.")

    private fun extractError(raw: String): String = try {
        JSONObject(raw).optJSONObject("error")?.optString("message").orEmpty().ifBlank { raw.take(300) }
    } catch (_: Exception) { raw.take(300) }

    private fun fallbackReport(name: String, weak: String, strong: String, ai: Boolean) = MentorReport(
        "$name, keep the loop simple: learn one concept, build something tiny, debug it, and review the mistake.",
        weak.ifBlank { "Not enough data yet" }, strong.ifBlank { "You are actively learning" },
        weak.ifBlank { "Today's learning mission" }, "Concept → tiny exercise → real feature → bug → debugging → review.",
        listOf("Practice one small feature today", "Keep an error log", "Review yesterday's mistake before starting"),
        "Consistency beats random long sessions.", "Start with one small task you can finish today.", ai
    )
}
