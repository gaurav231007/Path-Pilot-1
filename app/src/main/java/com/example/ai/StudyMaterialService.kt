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
    private const val MODEL_NAME = "gemini-3.8-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val MAX_FILE_BYTES = 6L * 1024L * 1024L
    private const val MAX_FILES = 4
    private val client by lazy { OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(90, TimeUnit.SECONDS).writeTimeout(90, TimeUnit.SECONDS).build() }

    suspend fun analyze(context: Context, uris: List<Uri>): String = withContext(Dispatchers.IO) {
        val key = GeminiMentorService.getEffectiveApiKey(context)
        if (key.isBlank()) return@withContext "Gemini API is not configured."
        if (uris.isEmpty()) return@withContext "Please select at least one study file."
        val parts = JSONArray().put(JSONObject().apply {
            put("text", """
                You are Path-Pilot's college study intelligence engine.
                Analyze the supplied syllabus, PYQs, assignments and teacher material.
                Extract topics, repeated PYQ patterns, important practice areas and a 7-day revision order.
                Rank Very High, High, Medium or Low only from evidence. Never guarantee an exact exam question.
                Clearly separate evidence found in files from inference and mention unreadable/missing material.
            """.trimIndent())
        })
        uris.take(MAX_FILES).forEach { uri ->
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val name = getDisplayName(context, uri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@forEach
            if (bytes.size.toLong() > MAX_FILE_BYTES) return@forEach
            val supported = mime == "application/pdf" || mime.startsWith("image/") || mime.startsWith("text/")
            if (!supported) return@forEach
            parts.put(JSONObject().put("text", "FILE: $name | MIME: $mime"))
            parts.put(JSONObject().put("inline_data", JSONObject().apply {
                put("mime_type", mime)
                put("data", Base64.encodeToString(bytes, Base64.NO_WRAP))
            }))
        }
        val body = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", parts)))
            put("generationConfig", JSONObject().put("temperature", 0.2))
        }
        try {
            val request = Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$key")
                .post(body.toString().toRequestBody("application/json".toMediaType())).build()
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val msg = try { JSONObject(raw).optJSONObject("error")?.optString("message") } catch (_: Exception) { null }
                    return@withContext "Material analysis failed (HTTP ${response.code}): ${msg ?: raw.take(300)}"
                }
                val text = JSONObject(raw).optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")
                    ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty().trim()
                return@withContext text.ifBlank { "I couldn't extract a useful analysis from these files." }
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
