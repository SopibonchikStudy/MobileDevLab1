package com.example.a1lab.ui.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a1lab.R
import com.example.a1lab.ui.components.PunchlineTextField
import com.example.a1lab.ui.components.BrandHeader
import com.example.a1lab.ui.components.BrandedBackground
import com.example.a1lab.ui.components.FieldError
import com.example.a1lab.viewmodel.RegistrationUiState

@Composable
fun RegistrationScreen(
    state: RegistrationUiState,
    onNameChange: (String) -> Unit,
    onLoginChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmationChange: (String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    BrandedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrandHeader(compact = true)
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.registration_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.registration_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            PunchlineTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.display_name_label),
                isError = state.nameError != null,
                errorMessage = state.nameError,
                enabled = !state.isSubmitting
            )
            PunchlineTextField(
                value = state.login,
                onValueChange = onLoginChange,
                label = stringResource(R.string.login_label),
                isError = state.loginError != null,
                errorMessage = state.loginError,
                enabled = !state.isSubmitting
            )
            PunchlineTextField(
                value = state.age,
                onValueChange = onAgeChange,
                label = stringResource(R.string.age_label),
                numeric = true,
                isError = state.ageError != null,
                errorMessage = state.ageError,
                enabled = !state.isSubmitting
            )
            PunchlineTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.password_label),
                password = true,
                isError = state.passwordError != null,
                errorMessage = state.passwordError,
                enabled = !state.isSubmitting
            )
            PunchlineTextField(
                value = state.passwordConfirmation,
                onValueChange = onConfirmationChange,
                label = stringResource(R.string.confirm_password_label),
                password = true,
                isError = state.passwordConfirmationError != null,
                errorMessage = state.passwordConfirmationError,
                enabled = !state.isSubmitting
            )
            FieldError(state.formError)
            Button(
                onClick = onRegister,
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.register_action))
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), enabled = !state.isSubmitting) {
                Text(stringResource(R.string.back_action))
            }
            Text(
                text = stringResource(R.string.registration_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
