package com.example.a1lab.data

import android.content.Context
import com.example.a1lab.domain.JokeCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class JsonBackupRepository(context: Context) : BackupRepository {
    private val backupDirectory = File(context.applicationContext.filesDir, "backup")
    private val backupFile = File(backupDirectory, FILE_NAME)

    override suspend fun createBackup(jokes: List<JokeCard>): BackupInfo = withContext(Dispatchers.IO) {
        if (jokes.isEmpty()) error("Нельзя создать копию пустой коллекции шуток")
        if (!backupDirectory.exists() && !backupDirectory.mkdirs()) {
            error("Не удалось создать папку резервной копии")
        }
        val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            .apply { timeZone = TimeZone.getDefault() }
            .format(Date())
        val json = JSONObject()
            .put("version", FORMAT_VERSION)
            .put("createdAt", createdAt)
            .put("jokes", JokeJsonCodec.encode(jokes))
            .toString()
        val temporary = File(backupDirectory, "$FILE_NAME.tmp")
        temporary.writeText(json)
        readBackupFile(temporary)
        replaceFileAtomically(temporary, backupFile)
        BackupInfo(createdAt, jokes.size)
    }

    override suspend fun restoreBackup(): List<JokeCard> = withContext(Dispatchers.IO) {
        if (!backupFile.exists()) error("Резервная копия не найдена")
        readBackupFile(backupFile).jokes
    }

    override suspend fun deleteBackup() = withContext(Dispatchers.IO) {
        if (backupFile.exists() && !backupFile.delete()) error("Не удалось удалить резервную копию")
    }

    override suspend fun getBackupInfo(): BackupInfo? = withContext(Dispatchers.IO) {
        if (!backupFile.exists()) return@withContext null
        runCatching { readBackupFile(backupFile) }
            .map { BackupInfo(it.createdAt, it.jokes.size) }
            .getOrNull()
    }

    private fun readBackupFile(file: File): BackupContent {
        val root = JSONObject(file.readText())
        require(root.getInt("version") == FORMAT_VERSION) { "Неподдерживаемая версия копии" }
        val jokes = JokeJsonCodec.decode(root.getJSONArray("jokes"))
        require(jokes.isNotEmpty()) { "Копия пуста" }
        require(jokes.map { it.id }.distinct().size == jokes.size) { "Повторяющиеся шутки" }
        return BackupContent(root.getString("createdAt"), jokes)
    }

    private fun replaceFileAtomically(temporary: File, target: File) {
        if (!target.exists()) {
            if (!temporary.renameTo(target)) error("Не удалось сохранить резервную копию")
            return
        }
        val previous = File(backupDirectory, "$FILE_NAME.previous")
        if (previous.exists() && !previous.delete()) error("Не удалось обновить резервную копию")
        if (!target.renameTo(previous)) error("Не удалось обновить резервную копию")
        if (!temporary.renameTo(target)) {
            previous.renameTo(target)
            error("Не удалось сохранить резервную копию")
        }
        if (previous.exists()) previous.delete()
    }

    private data class BackupContent(val createdAt: String, val jokes: List<JokeCard>)

    private companion object {
        const val FILE_NAME = "jokes_backup.json"
        const val FORMAT_VERSION = 2
    }
}
