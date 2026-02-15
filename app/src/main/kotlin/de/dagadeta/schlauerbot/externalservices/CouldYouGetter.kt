package de.dagadeta.schlauerbot.externalservices

import de.dagadeta.schlauerbot.discord.Logging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.springframework.stereotype.Service

open class HttpGetter(
    private val logger: Logging, private val url: String, val propertyName: String, val serviceName: String
) {
    private val client = OkHttpClient()

    fun getAnswer(): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                logger.log("Error with $serviceName API request: ${response.code}")
                return "Even the $serviceName API is down, I can't help you!"
            }

            val responseBody = response.body.string()
            val json = JSONObject(responseBody)
            return json.optString(propertyName, "").trim()
        }
    }
}

@Service
class CouldYouGetter(logger: Logging) : HttpGetter(
    logger = logger,
    url = "https://naas.isalman.dev/no",
    propertyName = "reason",
    serviceName = "No-as-a-Service",
)
