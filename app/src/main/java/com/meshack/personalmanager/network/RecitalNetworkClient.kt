package com.meshack.personalmanager.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.meshack.personalmanager.data.local.entities.Recital
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecitalNetworkClient @Inject constructor() {

    private val client = OkHttpClient()

    suspend fun fetchVerseOfDay(): List<Recital> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("https://bible-api.com/?random=verse&translation=kjv").build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val body = resp.body?.string() ?: return@withContext emptyList()
                val obj = JSONObject(body)
                val text = obj.optString("text", "").trim()
                if (text.isNotBlank()) listOf(Recital(text = text, source = com.meshack.personalmanager.domain.RecitalSource.VERSE_API)) else emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
