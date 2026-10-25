package com.example.a1lab.data

import com.example.a1lab.domain.JokeCard

data class BackupInfo(
    val createdAt: String,
    val jokeCount: Int
)

interface BackupRepository {
    suspend fun createBackup(jokes: List<JokeCard>): BackupInfo
    suspend fun restoreBackup(): List<JokeCard>
    suspend fun deleteBackup()
    suspend fun getBackupInfo(): BackupInfo?
}
