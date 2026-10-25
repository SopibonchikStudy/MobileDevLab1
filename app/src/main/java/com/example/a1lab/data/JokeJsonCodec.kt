package com.example.a1lab.data

import com.example.a1lab.domain.JokeCard
import com.example.a1lab.domain.JokeCategory
import com.example.a1lab.domain.JokeType
import org.json.JSONArray
import org.json.JSONObject

internal object JokeJsonCodec {
    fun encode(jokes: List<JokeCard>): JSONArray = JSONArray().apply {
        jokes.forEach { joke ->
            put(JSONObject().apply {
                put("id", joke.id)
                put("category", joke.category.name)
                put("type", joke.type.name)
                put("setup", joke.setup)
                put("punchline", joke.punchline ?: JSONObject.NULL)
                put("safe", joke.safe)
                put("language", joke.language)
                put("wordCount", joke.wordCount)
            })
        }
    }

    fun decode(array: JSONArray): List<JokeCard> = buildList {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val joke = JokeCard(
                id = item.getInt("id"),
                category = enumValueOrDefault(item.optString("category"), JokeCategory.UNKNOWN),
                type = enumValueOrDefault(item.optString("type"), JokeType.UNKNOWN),
                setup = item.getString("setup"),
                punchline = item.opt("punchline").takeUnless { it == null || it == JSONObject.NULL }?.toString(),
                safe = item.optBoolean("safe", true),
                language = item.optString("language", "en"),
                wordCount = item.optInt("wordCount", item.optString("setup").wordCount())
            )
            require(joke.id > 0 && joke.setup.isNotBlank()) { "Некорректная шутка в JSON" }
            add(joke)
        }
    }

    fun decodeApiResponse(json: JSONObject): List<JokeCard> {
        require(!json.optBoolean("error", false)) {
            json.optString("message").ifBlank { "JokeAPI вернул ошибку" }
        }
        val jokes = json.optJSONArray("jokes")
        if (jokes != null) return decodeApiJokes(jokes)
        return decodeApiJokes(JSONArray().put(json))
    }

    private fun decodeApiJokes(array: JSONArray): List<JokeCard> = buildList {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val apiType = item.optString("type")
            val setup = when (apiType) {
                "twopart" -> item.optString("setup")
                "single" -> item.optString("joke")
                else -> item.optString("setup", item.optString("joke"))
            }.trim()
            val punchline = when (apiType) {
                "twopart" -> item.optString("delivery").trim().ifBlank { null }
                else -> null
            }
            val joke = JokeCard(
                id = item.getInt("id"),
                category = parseCategory(item.optString("category")),
                type = parseType(apiType),
                setup = setup,
                punchline = punchline,
                safe = item.optBoolean("safe", true),
                language = item.optString("lang", "en"),
                wordCount = "$setup ${punchline.orEmpty()}".wordCount()
            )
            require(joke.id > 0 && joke.setup.isNotBlank()) { "JokeAPI вернул шутку без текста" }
            add(joke)
        }
    }

    private fun parseCategory(value: String): JokeCategory = when (value.lowercase()) {
        "programming" -> JokeCategory.PROGRAMMING
        "misc" -> JokeCategory.MISC
        "dark" -> JokeCategory.DARK
        "pun" -> JokeCategory.PUN
        "spooky" -> JokeCategory.SPOOKY
        "christmas" -> JokeCategory.CHRISTMAS
        else -> JokeCategory.UNKNOWN
    }

    private fun parseType(value: String): JokeType = when (value.lowercase()) {
        "single" -> JokeType.SINGLE
        "twopart" -> JokeType.TWOPART
        else -> JokeType.UNKNOWN
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(default)

    private fun String.wordCount(): Int = trim().split(Regex("\\s+")).count { it.isNotBlank() }
}
