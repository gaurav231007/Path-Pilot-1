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

data class MentorReport(val personalizedAnalysis:String,val biggestWeakness:String,val biggestStrength:String,val recommendedFocusArea:String,val studyStrategy:String,val improvementSuggestions:List<String>,val motivation:String,val explanationOfNextAction:String,val isAiGenerated:Boolean=true)

object GeminiMentorService {
    private const val PREFS_NAME="path_pilot_ai_prefs"
    private const val KEY_CUSTOM_GEMINI_API="custom_gemini_api_key"
    // Gemini 3.8 Flash is the current stable API model for complex coding/agentic work.
    private const val MODEL_NAME="gemini-3.8-flash"
    private const val BASE_URL="https://generativelanguage.googleapis.com/v1beta/models"
    private val httpClient by lazy{OkHttpClient.Builder().connectTimeout(30,TimeUnit.SECONDS).readTimeout(120,TimeUnit.SECONDS).writeTimeout(120,TimeUnit.SECONDS).build()}

    fun getEffectiveApiKey(context:Context?):String{
        if(context!=null){val k=context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).getString(KEY_CUSTOM_GEMINI_API,"")?.trim().orEmpty();if(k.isNotBlank())return k}
        val a=runCatching{BuildConfig.GEMINI_API_KEY.trim()}.getOrDefault("");if(a.isNotBlank()&&a!="MY_GEMINI_API_KEY")return a
        val b=runCatching{BuildConfig.PATH_PILOT_GEMINI_API_KEY.trim()}.getOrDefault("");if(b.isNotBlank()&&b!="MY_GEMINI_API_KEY")return b
        return ""
    }
    fun setCustomApiKey(context:Context,key:String){context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).edit().putString(KEY_CUSTOM_GEMINI_API,key.trim()).apply()}
    fun clearCustomApiKey(context:Context){context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).edit().remove(KEY_CUSTOM_GEMINI_API).apply()}
    fun isConfigured(context:Context?)=getEffectiveApiKey(context).isNotBlank()

    private fun requestBody(prompt:String,maxTokens:Int):String = JSONObject()
        .put("contents",JSONArray().put(JSONObject().put("parts",JSONArray().put(JSONObject().put("text",prompt)))))
        .put("generationConfig",JSONObject().put("maxOutputTokens",maxTokens))
        .toString()

    suspend fun testApiKeyConnection(keyToTest:String): Pair<Boolean, String> = withContext(Dispatchers.IO){
        val key=keyToTest.trim();if(key.isBlank())return@withContext false to "API key is empty."
        try{
            val request=Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$key").post(requestBody("Reply only OK.",20).toRequestBody("application/json".toMediaType())).build()
            httpClient.newCall(request).execute().use{response->val raw=response.body?.string().orEmpty();if(response.isSuccessful)true to "Gemini 3.8 Flash connected successfully." else false to "Gemini HTTP ${response.code}: ${extractError(raw)}"}
        }catch(e:Exception){false to "Network error: ${e.localizedMessage?:"Unknown error"}"}
    }

    private fun clean(s:String)=s.trim().take(8000)
    private fun extractError(raw:String)=runCatching{JSONObject(raw).optJSONObject("error")?.optString("message")}.getOrNull()?.takeIf{it.isNotBlank()}?:raw.take(500)

    suspend fun askGeminiMentor(context:Context?,question:String,studentContext:String)=withContext(Dispatchers.IO){
        val key=getEffectiveApiKey(context);if(key.isBlank())return@withContext "Gemini API key is not available in this build."
        val prompt="""You are Path-Pilot's personal engineering mentor. Teach programming languages, web development, backend engineering, professional software workflow, cybersecurity only in authorized labs, and college subjects. Teach by: concept -> tiny exercise -> real feature -> bug/debug -> review -> next action. Be practical, explain why, and give a concrete next task. Support beginners and adapt difficulty from learner context. Never guarantee exam questions and never help with unauthorized access.

Learner context: ${clean(studentContext)}
Question: ${clean(question)}"""
        try{
            val request=Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$key").post(requestBody(prompt,1600).toRequestBody("application/json".toMediaType())).build()
            httpClient.newCall(request).execute().use{response->val raw=response.body?.string().orEmpty();if(!response.isSuccessful)return@withContext "Gemini request failed (HTTP ${response.code}): ${extractError(raw)}";val text=runCatching{JSONObject(raw).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")}.getOrNull().orEmpty();if(text.isBlank())"Gemini returned no text. Please try again." else text.trim()}
        }catch(e:Exception){"Gemini network error: ${e.localizedMessage?:"Unknown error"}"}
    }

    suspend fun generateMentorGuidance(context:Context?=null,studentName:String,targetExam:String,readinessScore:Int,accuracy:Float,speedSec:Float,weakTopics:String,strongTopics:String):MentorReport{
        val text=askGeminiMentor(context,"Use my metrics to choose the next best learning action. Focus: $targetExam; readiness: $readinessScore; accuracy: $accuracy; speed: $speedSec; weak: $weakTopics; strong: $strongTopics","Learner: $studentName. Use evidence and practical next steps.")
        return MentorReport(text,weakTopics.ifBlank{"Not enough data"},strongTopics.ifBlank{"Active learning"},weakTopics.ifBlank{"Current mission"},"Learn -> build -> debug -> review -> repeat.",listOf("Complete one small task","Log mistakes","Review before starting tomorrow"),"Small consistent progress compounds.","Do the first concrete task from the response.",!text.startsWith("Gemini "))
    }
    suspend fun explainQuestionWithGemini(context:Context?,questionText:String,options:String,studentAnswer:String,correctAnswer:String,standardExplanation:String)=askGeminiMentor(context,"Explain this college question step by step. Question: $questionText Options: $options Student answer: $studentAnswer Correct answer: $correctAnswer Official explanation: $standardExplanation","The learner wants conceptual understanding, not just the answer.")
}
