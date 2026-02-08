package de.dagadeta.schlauerbot.couldyou

import de.dagadeta.schlauerbot.discord.Logging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.springframework.stereotype.Service

fun interface CouldYouGetter {
    /**
     * Provides a random rejection reason
     *
     * @return random rejection reason
     */
    fun getRejectionReason(): String
}

@Service
class NoAsAServiceCouldYouGetter(var logger: Logging) : CouldYouGetter {
    private val client = OkHttpClient()

    override fun getRejectionReason(): String {
        val url = "https://naas.isalman.dev/no"

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                logger.log("Error with No-as-a-Service API request: ${response.code}")
                return "Even the No-as-a-Service API is down, I can't help you!"
            }

            val responseBody = response.body.string()
            val json = JSONObject(responseBody)
            val reason = json.optString("reason", "").trim()

            return reason
        }
    }
}