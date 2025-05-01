package de.dagadeta.schlauerbot

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

interface WordChecker {
    /**
     * Checks, if a word exists in the dictionary.
     *
     * @param word the word to check
     * @return true if the word exists, false otherwise
     */
    fun isValidWord(word: String): Boolean
}

class WiktionaryWordChecker(private val language: String, var logger: Logging) : WordChecker {
    private val client = OkHttpClient()

    override fun isValidWord(word: String): Boolean {
        val url = "https://$language.wiktionary.org/w/api.php?action=query&titles=${word.trim()}&format=json"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                logger.log("Error with Wiktionary API request: ${response.code}")
                return false
            }

            val responseBody = response.body?.string() ?: return false
            val json = JSONObject(responseBody)
            val pages = json.getJSONObject("query").getJSONObject("pages")
            val pageKeys = pages.keys()

            if (pageKeys.hasNext()) return pageKeys.next() != "-1"
        }

        return false
    }
}