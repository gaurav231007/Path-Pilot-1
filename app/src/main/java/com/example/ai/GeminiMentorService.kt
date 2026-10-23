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
    private const val MODEL_NAME="gemini-2.5-flash"
    private const val BASE_URL="https://generativelanguage.googleapis.com/v1beta/models"
    private val httpClient by lazy { OkHttpClient.Builder().connectTimeout(30,TimeUnit.SECONDS).readTimeout(90,TimeUnit.SECONDS).writeTimeout(90,TimeUnit.SECONDS).build() }

    fun getEffectiveApiKey(context:Context?):String {
        if(context!=null){ val k=context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).getString(KEY_CUSTOM_GEMINI_API,"")?.trim().orEmpty(); if(k.isNotBlank()) return k }
        val a=runCatching{BuildConfig.GEMINI_API_KEY.trim()}.getOrDefault(""); if(a.isNotBlank()&&a!="MY_GEMINI_API_KEY") return a
        val b=runCatching{BuildConfig.PATH_PILOT_GEMINI_API_KEY.trim()}.getOrDefault(""); if(b.isNotBlank()&&b!="MY_GEMINI_API_KEY") return b
        return ""
    }
    fun setCustomApiKey(context:Context,key:String){context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).edit().putString(KEY_CUSTOM_GEMINI_API,key.trim()).apply()}
    fun clearCustomApiKey(context:Context){context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).edit().remove(KEY_CUSTOM_GEMINI_API).apply()}
    fun isConfigured(context:Context?)=getEffectiveApiKey(context).isNotBlank()

    private fun clean(s:String)=s.trim().take(5000)
    private fun error(raw:String)=runCatching{JSONObject(raw).optJSONObject("error")?.optString("message")}.getOrNull()?.takeIf{it.isNotBlank()}?:raw.take(300)

    suspend fun askGeminiMentor(context:Context?,question:String,studentContext:String)=withContext(Dispatchers.IO){
        val key=getEffectiveApiKey(context); if(key.isBlank()) return@withContext "Gemini API key is not available in this build."
        val prompt="""You are Path-Pilot, a personal engineering mentor. Teach programming, backend engineering, professional software workflow, cybersecurity only in authorized labs, and college subjects. Follow this loop: concept -> tiny exercise -> real feature -> bug/debug -> review -> next action. Explain simply but professionally. Give practical examples and a concrete next task. Never claim an exam question is guaranteed and never help with unauthorized access.\n\nLearner context: ${clean(studentContext)}\n\nQuestion: ${clean(question)}"""
        try{
            val body=JSONObject().apply{put("contents",JSONArray().put(JSONObject().apply{put("parts",JSONArray().put(JSONObject().put("text",prompt)))}));put("generationConfig",JSONObject().apply{put("temperature",0.4);put("maxOutputTokens",1200)})}
            val req=Request.Builder().url("$BASE_URL/$MODEL_NAME:generateContent?key=$key").post(body.toString().toRequestBody("application/json".toMediaType())).build()
            httpClient.newCall(req).execute().use{r->val raw=r.body?.string().orEmpty();if(!r.isSuccessful)return@withContext "Gemini request failed (HTTP ${r.code}): ${error(raw)}";val text=runCatching{JSONObject(raw).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")}.getOrNull().orEmpty();if(text.isBlank())"Gemini returned no text. Please try again." else text.trim()}
        }catch(e:Exception){"Gemini network error: ${e.localizedMessage?:"Unknown error"}"}
    }

    suspend fun generateMentorGuidance(context:Context?=null,studentName:String,targetExam:String,readinessScore:Int,accuracy:Float,speedSec:Float,weakTopics:String,strongTopics:String):MentorReport=withContext(Dispatchers.IO){
        if(getEffectiveApiKey(context).isBlank()) return@withContext fallbackReport(studentName,weakTopics,strongTopics,false)
        val q="Learner $studentName. Current focus $targetExam. Readiness $readinessScore. Accuracy $accuracy. Speed $speedSec. Weak areas $weakTopics. Strong areas $strongTopics. Give practical engineering/college next steps."
        val text=askGeminiMentor(context,q,"Use the learner metrics as evidence; prioritize weak areas and concrete actions.")
        MentorReport(text,weakTopics.ifBlank{"Not enough data"},strongTopics.ifBlank{"Active learning"},weakTopics.ifBlank{"Current mission"},"Learn -> build -> debug -> review -> repeat.",listOf("Complete one small task","Log mistakes","Review before starting tomorrow"),"Small consistent progress compounds.","Do the first concrete task from the response.",!text.startsWith("Gemini "))
    }
    suspend fun explainQuestionWithGemini(context:Context?,questionText:String,options:String,studentAnswer:String,correctAnswer:String,standardExplanation:String)=askGeminiMentor(context,"Explain this college question step by step. Question: $questionText Options: $options Student answer: $studentAnswer Correct answer: $correctAnswer Official explanation: $standardExplanation","The learner wants conceptual understanding, not just the answer.")
    private fun fallbackReport(name:String,weak:String,strong:String,ai:Boolean)=MentorReport("$name, keep the loop simple: learn one concept, build something tiny, debug it, and review the mistake.",weak.ifBlank{"Not enough data yet"},strong.ifBlank{"You are actively learning"},weak.ifBlank{"Today's learning mission"},"Concept -> exercise -> feature -> bug -> debugging -> review.",listOf("Practice one small feature","Keep an error log","Review yesterday's mistake"),"Consistency beats random long sessions.","Start with one small task you can finish today.",ai)
}
