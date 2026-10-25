package com.example.a1lab.data

import android.content.Context
import com.example.a1lab.domain.BookCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class JsonBookRepository(context: Context) : BookRepository {
    private val appContext = context.applicationContext
    private val cacheFile = File(appContext.filesDir, "catalog_cache.json")
    private val initialization = appContext.getSharedPreferences("catalog_state", Context.MODE_PRIVATE)

    override suspend fun getBooks(): List<BookCard> = withContext(Dispatchers.IO) {
        if (!initialization.getBoolean(KEY_INITIALIZED, false) || !cacheFile.exists()) {
            if (!initialization.getBoolean(KEY_INITIALIZED, false)) {
                return@withContext refreshBooks()
            }
            return@withContext emptyList()
        }
        runCatching { readCache() }.getOrElse { emptyList() }
    }

    override suspend fun refreshBooks(): List<BookCard> = withContext(Dispatchers.IO) {
        val books = demoBooks
        writeAtomically(cacheFile, JSONObject().put("version", 1).put("books", BookJsonCodec.encode(books)).toString())
        initialization.edit().putBoolean(KEY_INITIALIZED, true).apply()
        books
    }

    override suspend fun replaceBooks(books: List<BookCard>) = withContext(Dispatchers.IO) {
        require(books.isNotEmpty()) { "Нельзя сохранить пустой каталог" }
        writeAtomically(cacheFile, JSONObject().put("version", 1).put("books", BookJsonCodec.encode(books)).toString())
        initialization.edit().putBoolean(KEY_INITIALIZED, true).apply()
    }

    override suspend fun clearCachedBooks() = withContext(Dispatchers.IO) {
        if (cacheFile.exists() && !cacheFile.delete()) {
            error("Не удалось очистить каталог")
        }
    }

    private fun readCache(): List<BookCard> {
        val root = JSONObject(cacheFile.readText())
        require(root.getInt("version") == 1)
        return BookJsonCodec.decode(root.getJSONArray("books"))
    }

    private fun writeAtomically(target: File, content: String) {
        val temporary = File(target.parentFile, "${target.name}.tmp")
        temporary.writeText(content)
        if (!target.exists()) {
            if (!temporary.renameTo(target)) error("Не удалось сохранить каталог")
            return
        }
        val previous = File(target.parentFile, "${target.name}.previous")
        if (previous.exists() && !previous.delete()) error("Не удалось обновить каталог")
        if (!target.renameTo(previous)) error("Не удалось обновить каталог")
        if (!temporary.renameTo(target)) {
            previous.renameTo(target)
            error("Не удалось сохранить каталог")
        }
        if (previous.exists()) previous.delete()
    }

    private companion object {
        const val KEY_INITIALIZED = "initialized"
    }
}
