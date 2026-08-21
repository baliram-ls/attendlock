package com.example

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TimetableAnalyzer {

    private val model = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel("gemini-3.6-flash")

    suspend fun analyzeTimetable(
        context: Context,
        imageUri: Uri
    ): String = withContext(Dispatchers.IO) {

        val contentResolver = context.contentResolver

        val bytes = contentResolver
            .openInputStream(imageUri)
            ?.use { it.readBytes() }
            ?: throw Exception("Unable to read timetable image")

        val mimeType = contentResolver.getType(imageUri)
            ?: "image/jpeg"

        val prompt = content {
            inlineData(bytes, mimeType)

            text(
                """
                You are a timetable extraction assistant for an Android
                college attendance app called AttendMate.

                Analyze the timetable image carefully.

                Extract every class visible in the timetable.

                For each class identify:
                - day
                - subject name
                - start time
                - end time
                - room number if visible
                - faculty name if visible

                Return ONLY valid JSON.

                Use this exact structure:

                {
                  "classes": [
                    {
                      "day": "Monday",
                      "subject": "Data Structures",
                      "startTime": "09:00",
                      "endTime": "10:00",
                      "room": "",
                      "faculty": ""
                    }
                  ]
                }

                Important rules:
                1. Do not invent information.
                2. If room or faculty is not visible, use an empty string.
                3. Preserve the subject names exactly as shown when possible.
                4. Convert times to 24-hour format.
                5. Include all days and all periods visible.
                6. Return JSON only. No markdown.
                """.trimIndent()
            )
        }

        val response = model.generateContent(prompt)

        response.text ?: throw Exception("Gemini returned an empty response")
    }
}
