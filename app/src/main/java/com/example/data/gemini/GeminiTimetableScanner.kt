package com.example.data.gemini

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ScannedClassItem(
    val dayOfWeek: Int, // 1=Mon .. 7=Sun
    val dayName: String,
    val subjectName: String,
    val subjectCode: String = "",
    val teacher: String = "",
    val room: String = "",
    val startTime: String = "09:00",
    val endTime: String = "10:00"
)

object GeminiTimetableScanner {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize if too huge to keep latency low
        val scaled = if (width > 1600 || height > 1600) {
            val scale = 1600f / maxOf(width, height)
            Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
        } else {
            this
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun scanTimetableImage(
        bitmap: Bitmap,
        userCustomApiKey: String? = null
    ): Result<List<ScannedClassItem>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = when {
                !userCustomApiKey.isNullOrBlank() -> userCustomApiKey.trim()
                BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
                else -> ""
            }

            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Gemini API key is not configured. Please add your API key in Settings > Timetable OCR & Gemini API Key or in AI Studio Secrets."
                    )
                )
            }

            val base64Image = bitmap.toBase64()

            val prompt = """
                Analyze this timetable / schedule image. Extract all scheduled classes/lectures for each day of the week.
                Return ONLY a valid JSON array of objects with no Markdown formatting or backticks around it.
                Each object must have these exact fields:
                - "dayOfWeek": integer (1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for Thursday, 5 for Friday, 6 for Saturday, 7 for Sunday)
                - "dayName": string (e.g. "Monday", "Tuesday")
                - "subjectName": string (name of the subject/course)
                - "subjectCode": string (e.g. "CS301", "BCS101", or empty string if not found)
                - "teacher": string (faculty or professor name, or empty string)
                - "room": string (classroom or lab number, or empty string)
                - "startTime": string in 24-hour HH:mm format (e.g. "09:00", "14:30")
                - "endTime": string in 24-hour HH:mm format (e.g. "10:00", "15:30")

                Example response format:
                [
                  {"dayOfWeek": 1, "dayName": "Monday", "subjectName": "Data Structures", "subjectCode": "CS301", "teacher": "Dr. Sharma", "room": "Room 302", "startTime": "09:00", "endTime": "10:00"},
                  {"dayOfWeek": 1, "dayName": "Monday", "subjectName": "Operating Systems", "subjectCode": "CS302", "teacher": "Prof. Rao", "room": "Lab 1", "startTime": "10:15", "endTime": "11:15"}
                ]
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errorObj = JSONObject(responseBody).optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP error ${response.code}"
                } catch (e: Exception) {
                    "HTTP error ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception("Gemini API Error: $errorMsg"))
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isBlank()) {
                return@withContext Result.failure(Exception("No data extracted from timetable image."))
            }

            val cleanedJson = text.trim().let {
                if (it.startsWith("```json")) it.removePrefix("```json").removeSuffix("```").trim()
                else if (it.startsWith("```")) it.removePrefix("```").removeSuffix("```").trim()
                else it
            }

            val items = mutableListOf<ScannedClassItem>()
            val jsonArray = JSONArray(cleanedJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val dayOfWeek = obj.optInt("dayOfWeek", 1)
                val dayName = obj.optString("dayName", "Monday")
                val subjectName = obj.optString("subjectName", "Subject")
                val subjectCode = obj.optString("subjectCode", "")
                val teacher = obj.optString("teacher", "")
                val room = obj.optString("room", "")
                val startTime = obj.optString("startTime", "09:00")
                val endTime = obj.optString("endTime", "10:00")

                if (subjectName.isNotBlank()) {
                    items.add(
                        ScannedClassItem(
                            dayOfWeek = if (dayOfWeek in 1..7) dayOfWeek else 1,
                            dayName = dayName,
                            subjectName = subjectName,
                            subjectCode = subjectCode,
                            teacher = teacher,
                            room = room,
                            startTime = startTime,
                            endTime = endTime
                        )
                    )
                }
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
