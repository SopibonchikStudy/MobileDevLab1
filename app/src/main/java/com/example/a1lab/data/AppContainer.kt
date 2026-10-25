package com.example.a1lab.data

import android.content.Context
import com.example.a1lab.storage.PreferenceSettingsRepository
import com.example.a1lab.storage.PreferenceUserRepository

class AppContainer(context: Context) {
    val userRepository: UserRepository = PreferenceUserRepository(context)
    val settingsRepository: SettingsRepository = PreferenceSettingsRepository(context)
    val bookRepository: BookRepository = JsonBookRepository(context)
    val backupRepository: BackupRepository = JsonBackupRepository(context)
}
