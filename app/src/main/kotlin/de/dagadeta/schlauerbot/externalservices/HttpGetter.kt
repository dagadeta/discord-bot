package de.dagadeta.schlauerbot.externalservices

import com.jayway.jsonpath.JsonPath
import de.dagadeta.schlauerbot.discord.Logging
import okhttp3.OkHttpClient
import okhttp3.Request

fun interface AnswerProvider {
    fun getAnswer(): String
}

abstract class HttpGetter(
    private val logger: Logging, private val url: String, val propertyName: String, val serviceName: String
) : AnswerProvider {
    private val client = OkHttpClient()

    override fun getAnswer(): String {
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
            return JsonPath.parse(responseBody).read<String>(propertyName).trim()
        }
    }
}
