package com.example.a1lab.data

import android.content.Context
import com.example.a1lab.domain.JokeCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class RemoteJokeRepository(context: Context) : JokeRepository {
    private val appContext = context.applicationContext
    private val cacheFile = File(appContext.filesDir, CACHE_FILE_NAME)
    private val initialization = appContext.getSharedPreferences("joke_cache_state", Context.MODE_PRIVATE)

    override suspend fun getJokes(): List<JokeCard> = withContext(Dispatchers.IO) {
        val initialized = initialization.getBoolean(KEY_INITIALIZED, false)
        if (!initialized && cacheFile.exists()) {
            return@withContext readCache()
        }
        if (!initialized) return@withContext refreshJokes()
        if (!cacheFile.exists()) return@withContext emptyList()
        readCache()
    }

    override suspend fun refreshJokes(): List<JokeCard> = withContext(Dispatchers.IO) {
        var collected = emptyList<JokeCard>()
        repeat(JokeFeedPolicy.MAX_BATCHES) {
            if (collected.size >= JokeFeedPolicy.MIN_JOKES) return@repeat
            collected = JokeFeedPolicy.merge(collected, fetchBatch(JokeFeedPolicy.BATCH_SIZE))
        }
        require(collected.size >= JokeFeedPolicy.MIN_JOKES) {
            "JokeAPI вернул меньше ${JokeFeedPolicy.MIN_JOKES} уникальных шуток. Попробуйте обновить ещё раз"
        }
        writeAtomically(
            cacheFile,
            JSONObject()
                .put("version", CACHE_VERSION)
                .put("source", API_SOURCE)
                .put("jokes", JokeJsonCodec.encode(collected))
                .toString()
        )
        initialization.edit().putBoolean(KEY_INITIALIZED, true).apply()
        collected
    }

    override suspend fun replaceJokes(jokes: List<JokeCard>) = withContext(Dispatchers.IO) {
        require(jokes.isNotEmpty()) { "Нельзя сохранить пустую коллекцию шуток" }
        writeAtomically(
            cacheFile,
            JSONObject()
                .put("version", CACHE_VERSION)
                .put("source", API_SOURCE)
                .put("jokes", JokeJsonCodec.encode(jokes))
                .toString()
        )
        initialization.edit().putBoolean(KEY_INITIALIZED, true).apply()
    }

    override suspend fun clearCachedJokes() = withContext(Dispatchers.IO) {
        if ((cacheFile.exists() && !cacheFile.delete()) || !initialization.edit().putBoolean(KEY_INITIALIZED, true).commit()) {
            error("Не удалось очистить коллекцию шуток")
        }
    }

    private fun fetchBatch(amount: Int): List<JokeCard> {
        val connection = (URL("$API_BASE?type=twopart&amount=$amount&safe-mode&lang=en").openConnection()
            as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Punchline-Android-Lab/1.0")
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) {
                error("JokeAPI недоступен (HTTP $status). Попробуйте позже")
            }
            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            return JokeJsonCodec.decodeApiResponse(JSONObject(response))
        } finally {
            connection.disconnect()
        }
    }

    private fun readCache(): List<JokeCard> {
        val root = JSONObject(cacheFile.readText())
        require(root.getInt("version") == CACHE_VERSION) { "Неподдерживаемая версия кэша шуток" }
        return JokeJsonCodec.decode(root.getJSONArray("jokes"))
    }

    private fun writeAtomically(target: File, content: String) {
        val temporary = File(target.parentFile, "${target.name}.tmp")
        temporary.writeText(content)
        if (!target.exists()) {
            if (!temporary.renameTo(target)) error("Не удалось сохранить шутки")
            return
        }
        val previous = File(target.parentFile, "${target.name}.previous")
        if (previous.exists() && !previous.delete()) error("Не удалось обновить шутки")
        if (!target.renameTo(previous)) error("Не удалось обновить шутки")
        if (!temporary.renameTo(target)) {
            previous.renameTo(target)
            error("Не удалось сохранить шутки")
        }
        if (previous.exists()) previous.delete()
    }

    private companion object {
        const val API_BASE = "https://v2.jokeapi.dev/joke/Any"
        const val API_SOURCE = "https://jokeapi.dev/"
        const val CACHE_FILE_NAME = "jokes_cache.json"
        const val CACHE_VERSION = 2
        const val KEY_INITIALIZED = "initialized"
    }
}
