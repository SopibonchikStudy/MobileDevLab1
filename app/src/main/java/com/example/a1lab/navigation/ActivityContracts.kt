package com.example.a1lab.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.a1lab.RegistrationActivity
import com.example.a1lab.SettingsActivity

object RegistrationContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(context, RegistrationActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        intent?.getStringExtra(RegistrationActivity.EXTRA_LOGIN)
            ?.takeIf { resultCode == Activity.RESULT_OK }
}

data class SettingsResult(
    val login: String,
    val profileChanged: Boolean
)

object SettingsContract : ActivityResultContract<String, SettingsResult?>() {
    override fun createIntent(context: Context, input: String): Intent =
        Intent(context, SettingsActivity::class.java)
            .putExtra(SettingsActivity.EXTRA_LOGIN, input)

    override fun parseResult(resultCode: Int, intent: Intent?): SettingsResult? =
        if (resultCode == Activity.RESULT_OK && intent != null) {
            SettingsResult(
                login = intent.getStringExtra(SettingsActivity.EXTRA_LOGIN).orEmpty(),
                profileChanged = intent.getBooleanExtra(SettingsActivity.EXTRA_PROFILE_CHANGED, false)
            )
        } else {
            null
        }
}
