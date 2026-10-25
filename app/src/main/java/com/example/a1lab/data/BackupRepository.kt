package com.example.a1lab.data

import com.example.a1lab.domain.BookCard

data class BackupInfo(
    val createdAt: String,
    val bookCount: Int
)

interface BackupRepository {
    suspend fun createBackup(books: List<BookCard>): BackupInfo
    suspend fun restoreBackup(): List<BookCard>
    suspend fun deleteBackup()
    suspend fun getBackupInfo(): BackupInfo?
}
