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
    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val MAX_FILE_BYTES = 6L * 1024L * 1024L
    private val client by lazy { OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).build() }

    suspend fun analyze(context: Context, uris: List<Uri>): String = withContext(Dispatchers.IO) {
        val key = GeminiMentorService.getEffectiveApiKey(context)
        if (key.isBlank()) return@withContext "Gemini API key is not available in this build."
        if (uris.isEmpty()) return@withContext "Please select at least one study file."

        val parts = JSONArray().put(JSONObject().put("text", "Analyze these college study materials for Path-Pilot. Extract syllabus topics, repeated PYQ patterns, assignment/teacher-material overlap, and evidence-based practice priorities. Rank Very High, High, Medium, Low. Give reasons and a 7-day revision order. Clearly distinguish evidence from inference. Never guarantee an exact exam question or claim 99% accuracy."))
        uris.take(4).forEach { uri ->
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@forEach
            if (bytes.size.toLong() > MAX_FILE_BYTES) return@forEach
            if (mime != "application/pdf" && !mime.startsWith("image/") && !mime.startsWith("text/")) return@forEach
            parts.put(JSONObject().put("text", "FILE: ${getDisplayName(context, uri)}"))
            parts.put(JSONObject().put("inline_data", JSONObject().put("mime_type", mime).put("data", Base64.encodeToString(bytes, Base64.NO_WRAP))))
        }
        val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", parts))).put("generationConfig", JSONObject().put("temperature", 0.2).put("maxOutputTokens", 1800))
        try {
            val request = Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$key").post(body.toString().toRequestBody("application/json".toMediaType())).build()
            client.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = runCatching { JSONObject(raw).optJSONObject("error")?.optString("message") }.getOrNull().orEmpty()
                    return@withContext "Material analysis failed (HTTP ${response.code}): ${message.ifBlank { raw.take(200) }}"
                }
                runCatching { JSONObject(raw).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text") }.getOrNull()?.trim()?.takeIf { it.isNotBlank() } ?: "I couldn't extract useful analysis from these files."
            }
        } catch (e: Exception) {
            "Material analysis failed: ${e.localizedMessage ?: "network error"}"
        }
    }

    private fun getDisplayName(context: Context, uri: Uri): String {
        var name = "Study material"
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) name = it.getString(0) }
        return name
    }
}
