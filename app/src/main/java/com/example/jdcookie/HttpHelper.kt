package com.example.jdcookie

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.lang.reflect.Type

object HttpHelper {
    val client = OkHttpClient()
    val gson = Gson()
    val JSON = "application/json; charset=utf-8".toMediaType()

    suspend inline fun <reified T> get(url: String, headers: Map<String, String> = emptyMap()): T =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .apply {
                    headers.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()
            execute(request)
        }

    suspend inline fun <reified T> post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): T = withContext(
        Dispatchers.IO
    ) {
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(JSON))
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        execute(request)

    }

    suspend inline fun <reified T> put(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): T = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .put(body.toRequestBody(JSON))
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()
        execute(request)
    }

    inline fun <reified T> execute(request: Request): T {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}: ${response.message}")
            val bodyStr = response.body?.string() ?: throw IOException("Empty response body")
            val type: Type = object : TypeToken<T>() {}.type
            return gson.fromJson(bodyStr, type)
        }
    }
}