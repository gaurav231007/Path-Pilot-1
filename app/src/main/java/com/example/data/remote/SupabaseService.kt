package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.entity.AttemptEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.StudentDNAEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class SupabaseSyncStatus {
    object Unconfigured : SupabaseSyncStatus()
    object Syncing : SupabaseSyncStatus()
    data class Success(val message: String) : SupabaseSyncStatus()
    data class Error(val error: String) : SupabaseSyncStatus()
}

object SupabaseService {

    private const val TAG = "SupabaseService"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun isConfigured(): Boolean {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        return url.isNotBlank() && !url.contains("your-project") && key.isNotBlank() && !key.contains("your-anon-key")
    }

    fun getSupabaseUrl(): String {
        return try {
            val field = BuildConfig::class.java.getField("SUPABASE_URL")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getSupabaseKey(): String {
        return try {
            val field = BuildConfig::class.java.getField("SUPABASE_ANON_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Test live connectivity with Supabase project
     */
    suspend fun testConnection(): SupabaseSyncStatus = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext SupabaseSyncStatus.Unconfigured
        }

        val url = getSupabaseUrl().trimEnd('/')
        val key = getSupabaseKey()

        try {
            val request = Request.Builder()
                .url("$url/rest/v1/")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful || response.code == 200 || response.code == 404) {
                SupabaseSyncStatus.Success("Successfully connected to Supabase endpoint ($url)")
            } else {
                SupabaseSyncStatus.Error("Supabase responded with code ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection error", e)
            SupabaseSyncStatus.Error("Network error connecting to Supabase: ${e.localizedMessage}")
        }
    }

    /**
     * Sync Student DNA to Supabase cloud table `student_dna`
     */
    suspend fun syncStudentDNA(dna: StudentDNAEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false

        val url = getSupabaseUrl().trimEnd('/')
        val key = getSupabaseKey()

        try {
            val payload = JSONObject().apply {
                put("user_id", dna.userId)
                put("readiness_score", dna.readinessScore)
                put("accuracy", dna.accuracy.toDouble())
                put("speed_sec", dna.speedSec.toDouble())
                put("weak_topics", dna.weakTopics)
                put("strong_topics", dna.strongTopics)
                put("total_tests", dna.totalTestsCompleted)
                put("total_questions", dna.totalQuestionsAnswered)
                put("last_updated", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url("$url/rest/v1/student_dna?on_conflict=user_id")
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing student DNA", e)
            false
        }
    }

    /**
     * SQL schema instructions for Supabase SQL Editor
     */
    fun getSupabaseSqlSchema(): String {
        return """
-- Path Pilot Supabase PostgreSQL Schema
-- Execute in Supabase SQL Editor (https://app.supabase.com/project/_/sql)

CREATE TABLE IF NOT EXISTS public.users (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    role TEXT DEFAULT 'STUDENT',
    target_exam TEXT DEFAULT 'JEE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.student_dna (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    readiness_score INT NOT NULL,
    accuracy DOUBLE PRECISION NOT NULL,
    speed_sec DOUBLE PRECISION NOT NULL,
    weak_topics TEXT,
    strong_topics TEXT,
    total_tests INT DEFAULT 0,
    total_questions INT DEFAULT 0,
    last_updated BIGINT
);

CREATE TABLE IF NOT EXISTS public.questions (
    id SERIAL PRIMARY KEY,
    exam_code TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    question TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS public.attempts (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    time_taken_sec INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.student_dna ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.questions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow anonymous read questions" ON public.questions FOR SELECT USING (true);
CREATE POLICY "Allow authenticated read/write dna" ON public.student_dna FOR ALL USING (true);
        """.trimIndent()
    }
}
