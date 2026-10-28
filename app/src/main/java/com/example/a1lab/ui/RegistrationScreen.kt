package com.example.a1lab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a1lab.R
import com.example.a1lab.viewmodel.RegistrationUiState

@Composable
fun RegistrationScreen(
    state: RegistrationUiState,
    onName: (String) -> Unit,
    onLogin: (String) -> Unit,
    onAge: (String) -> Unit,
    onPassword: (String) -> Unit,
    onConfirmation: (String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    Page {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Brand()
            Text(stringResource(R.string.registration_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.registration_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Field(state.name, onName, stringResource(R.string.display_name_label), state.nameError)
            Field(state.login, onLogin, stringResource(R.string.login_label), state.loginError)
            Field(state.age, onAge, stringResource(R.string.age_label), state.ageError, number = true)
            Field(state.password, onPassword, stringResource(R.string.password_label), state.passwordError, password = true)
            Field(state.confirmation, onConfirmation, stringResource(R.string.confirm_password_label), state.confirmationError, password = true)
            state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.register_action))
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.back_action))
            }
        }
    }
}
