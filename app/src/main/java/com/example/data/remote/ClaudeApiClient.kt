package com.example.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ClaudeApiClient(private val baseUrl: String = "https://cc.freemodel.dev") {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun streamChat(apiKey: String, model: String, messages: List<com.example.domain.model.Message>): Flow<String> = flow {
        val jsonArray = JSONArray()
        for (m in messages) {
            val obj = JSONObject()
            obj.put("role", m.role)
            
            if (m.imageBase64 != null) {
                val contentArray = JSONArray()
                val imageObj = JSONObject().apply {
                    put("type", "image")
                    put("source", JSONObject().apply {
                        put("type", "base64")
                        put("media_type", "image/jpeg")
                        put("data", m.imageBase64)
                    })
                }
                val textObj = JSONObject().apply {
                    put("type", "text")
                    put("text", m.content)
                }
                contentArray.put(imageObj)
                contentArray.put(textObj)
                obj.put("content", contentArray)
            } else {
                obj.put("content", m.content)
            }
            
            jsonArray.put(obj)
        }

        val requestBodyJson = JSONObject()
        requestBodyJson.put("model", model)
        requestBodyJson.put("max_tokens", 4096)
        requestBodyJson.put("messages", jsonArray)
        requestBodyJson.put("stream", true)

        val request = Request.Builder()
            .url("$baseUrl/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .addHeader("accept", "text/event-stream")
            .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()
        
        // Also try OpenAI format if it's a proxy that expects /v1/chat/completions? 
        // We will stick to Anthropic official /v1/messages since user specified "Claude api".

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            throw IOException("Unexpected code $response: $errorBody")
        }

        response.body?.let { body ->
            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            try {
                var line = reader.readLine()
                while (line != null) {
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ").trim()
                        if (data != "[DONE]") {
                            try {
                                val parse = JSONObject(data)
                                if (parse.has("type")) {
                                    val type = parse.getString("type")
                                    if (type == "content_block_delta" || type == "content_block") {
                                        val delta = parse.optJSONObject("delta")
                                        if (delta != null && delta.has("text")) {
                                            emit(delta.getString("text"))
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // ignore parse errors for partial chunks
                            }
                        }
                    }
                    line = reader.readLine()
                }
            } finally {
                withContext(Dispatchers.IO) {
                    body.close()
                }
            }
        }
    }
}
