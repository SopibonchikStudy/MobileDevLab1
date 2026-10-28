package com.example.a1lab.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a1lab.R
import com.example.a1lab.viewmodel.StartUiState

@Composable
fun StartScreen(
    state: StartUiState,
    onLogin: (String) -> Unit,
    onPassword: (String) -> Unit,
    onSignIn: () -> Unit,
    onRegister: () -> Unit
) {
    Page {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Brand()
            Text(stringResource(R.string.brand_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (state.returning) {
                            stringResource(R.string.welcome_back, state.displayName.ifBlank { state.login })
                        } else {
                            stringResource(R.string.first_visit_title)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.returning) Avatar(state.avatar, state.displayName)
                    Text(
                        stringResource(if (state.returning) R.string.welcome_back_subtitle else R.string.first_visit_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Field(state.login, onLogin, stringResource(R.string.login_label), state.loginError)
                    Field(state.password, onPassword, stringResource(R.string.password_label), state.passwordError, password = true)
                    state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(if (state.returning) R.string.continue_action else R.string.sign_in))
                    }
                    OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.create_account))
                    }
                }
            }
            Text(stringResource(R.string.local_storage_note), style = MaterialTheme.typography.bodySmall)
        }
    }
}
