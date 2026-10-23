package com.example.ai

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object StudyMaterialService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val MAX_FILE_BYTES = 6L * 1024L * 1024L
    private const val MAX_FILES = 4

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    suspend fun analyze(context: Context, uris: List<Uri>): String = withContext(Dispatchers.IO) {
        val key = GeminiMentorService.getEffectiveApiKey(context)
        if (key.isBlank()) return@withContext "Gemini API is not configured. Add the API key first."
        if (uris.isEmpty()) return@withContext "Please select at least one syllabus, PYQ, assignment or notes file."

        val parts = JSONArray()
        parts.put(JSONObject().apply {
            put("text", """
                You are Path-Pilot's college exam intelligence engine.
                Analyze the supplied study material for the student's current college preparation.
                Identify syllabus topics, extract PYQ patterns, connect assignments/teacher material to topics,
                and rank likely-to-be-important practice areas.
                NEVER claim a question is guaranteed to appear or say 99% exact prediction.
                Use Very High, High, Medium, Low priority only when evidence supports it.
                Return a concise, student-friendly report with:
                1) Subjects/topics found
                2) Repeated PYQ patterns
                3) Very High / High priority questions or topics with reasons
                4) Medium / Low priority topics
                5) A 7-day revision order
                6) Missing information or unreadable material, if any
                Distinguish clearly between evidence from the files and your inference.
            """.trimIndent())
        })

        uris.take(MAX_FILES).forEach { uri ->
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val name = getDisplayName(context, uri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@forEach
            if (bytes.size.toLong() > MAX_FILE_BYTES) return@forEach

            val supported = mime == "application/pdf" || mime.startsWith("image/") || mime.startsWith("text/")
            if (!supported) return@forEach

            parts.put(JSONObject().apply {
                put("text", "FILE: $name | MIME: $mime")
            })
            parts.put(JSONObject().apply {
                put("inline_data", JSONObject().apply {
                    put("mime_type", mime)
                    put("data", Base64.encodeToString(bytes, Base64.NO_WRAP))
                })
            })
        }

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply { put("parts", parts) })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
            })
        }

        try {
            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_NAME:generateContent?key=$key")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = try { JSONObject(raw).optJSONObject("error")?.optString("message") } catch (_: Exception) { null }
                    return@withContext "Material analysis failed: ${message ?: "HTTP ${response.code}"}"
                }
                val text = JSONObject(raw)
                    .optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")
                    ?.optJSONObject(0)?.optString("text")
                return@withContext text?.trim().orEmpty().ifBlank { "I couldn't extract a useful analysis from these files." }
            }
        } catch (e: Exception) {
            "Material analysis failed: ${e.localizedMessage ?: "network error"}"
        }
    }

    private fun getDisplayName(context: Context, uri: Uri): String {
        var name = "Study material"
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) name = cursor.getString(0)
        }
        return name
    }
}
