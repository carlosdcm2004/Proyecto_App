package com.example.proyecto_app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object OpenAIClient {

    private const val BASE_URL = "https://api.openai.com/v1/chat/completions"

    // Cliente HTTP
    private val client = OkHttpClient()

    // Función suspendida: se llama desde una coroutine
    suspend fun describeObject(label: String): String = withContext(Dispatchers.IO) {
        try {
            // Construimos el JSON del body
            val messages = JSONArray().apply {
                put(
                    JSONObject()
                        .put("role", "system")
                        .put(
                            "content",
                            "Eres un asistente que genera DESCRIPCIONES MUY BREVES en español " +
                                    "sobre objetos físicos. Máximo dos frases."
                        )
                )
                put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "content",
                            "Describe brevemente qué es y para qué sirve el objeto \"$label\"."
                        )
                )
            }

            val json = JSONObject().apply {
                put("model", "gpt-4o-mini") // puedes cambiar a otro modelo si quieres
                put("messages", messages)
                put("max_tokens", 60)
                put("temperature", 0.7)
            }

            val mediaType = "application/json".toMediaType()
            val body = json.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(BASE_URL)
                .header("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                .header("Content-Type", "application/json")
                .post(body)
                .build()


            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "No pude generar la descripción (error ${response.code})"
                }

                val responseBody = response.body?.string() ?: return@withContext "Respuesta vacía"
                val jsonResp = JSONObject(responseBody)
                val content = jsonResp
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                content.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Ocurrió un error al pedir la descripción."
        }
    }
}
