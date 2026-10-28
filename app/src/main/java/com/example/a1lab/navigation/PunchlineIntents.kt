package com.example.a1lab.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.example.a1lab.ContentActivity
import com.example.a1lab.RegistrationActivity
import com.example.a1lab.SettingsActivity
import com.example.a1lab.domain.AppSettings
import com.example.a1lab.domain.PunchlineState
import com.example.a1lab.domain.RegisteredAccount
import java.io.Serializable

object PunchlineIntents {

    fun openContent(context: Context, state: PunchlineState): Intent =
        Intent(context, ContentActivity::class.java).putExtra("content_state", state)

    fun openSettings(context: Context, state: PunchlineState): Intent =
        Intent(context, SettingsActivity::class.java).putExtra("content_state", state)

    fun openRegistration(context: Context, settings: AppSettings): Intent =
        Intent(context, RegistrationActivity::class.java).putExtra("settings", settings)

    fun registeredResult(account: RegisteredAccount): Intent =
        Intent().putExtra("registered_account", account)

    fun contentResult(state: PunchlineState): Intent =
        Intent().putExtra("content_state", state)

    fun accountFrom(intent: Intent?): RegisteredAccount? =
        intent?.serializableExtra("registered_account")

    fun contentFrom(intent: Intent?): PunchlineState? =
        intent?.serializableExtra("content_state")

    fun settingsFrom(intent: Intent?): AppSettings? =
        intent?.serializableExtra("settings")

    private inline fun <reified T : Serializable> Intent.serializableExtra(key: String): T? {
        val extras: Bundle = extras ?: return null
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getSerializable(key, T::class.java)
        } else {
            extras.getSerializable(key) as? T
        }
    }
}
