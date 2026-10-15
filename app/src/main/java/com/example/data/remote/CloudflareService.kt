package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * CloudflareService handles secure communication with our Cloudflare Worker.
 * This is used for generating Agora RTC tokens securely and potentially for 
 * fast signaling in the future.
 */
object CloudflareService {
    private const val TAG = "CloudflareService"
    
    // The official Cloudflare Worker URL provided by the user
    private var WORKER_BASE_URL = "https://group.cybenode-site.workers.dev"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Updates the worker URL dynamically if needed (e.g. from Firebase config)
     */
    fun setWorkerUrl(url: String) {
        if (url.isNotBlank()) {
            WORKER_BASE_URL = url.removeSuffix("/")
        }
    }

    /**
     * Fetches a fresh Agora RTC Token from the Cloudflare Worker.
     * This ensures the App Certificate is never exposed in the client code.
     */
    suspend fun fetchAgoraToken(channelName: String, uid: Int): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$WORKER_BASE_URL/token?channel=$channelName&uid=$uid"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            
            if (response.isSuccessful && !body.isNullOrEmpty()) {
                val json = JSONObject(body)
                val token = json.optString("token")
                if (token.isNotEmpty()) {
                    Log.d(TAG, "Successfully fetched Agora token from CF Worker for $channelName")
                    return@withContext token
                } else {
                    Log.e(TAG, "CF Worker returned success but token is empty: $body")
                }
            } else {
                Log.e(TAG, "Failed to fetch token from CF: ${response.code} (URL: $url) Body: $body")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error fetching token from Cloudflare", e)
        }
        null
    }
}
