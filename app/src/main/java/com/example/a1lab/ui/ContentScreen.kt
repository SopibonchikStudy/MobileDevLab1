package com.example.a1lab.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a1lab.R
import com.example.a1lab.domain.JokeCard
import com.example.a1lab.domain.JokeCategory
import com.example.a1lab.domain.JokeType
import com.example.a1lab.viewmodel.ContentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(
    state: ContentUiState,
    onSettings: () -> Unit,
    onRefresh: () -> Unit,
    onRestoreBackup: () -> Unit,
    onExit: () -> Unit,
    snackbarHost: @Composable () -> Unit
) {
    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.catalog_title), fontWeight = FontWeight.Bold) },
                actions = { TextButton(onClick = onSettings) { Text(stringResource(R.string.settings)) } }
            )
        }
    ) { padding ->
        when {
            state.jokes.isEmpty() -> EmptyContent(onRefresh, onRestoreBackup, onSettings, onExit, Modifier.fillMaxSize().padding(padding))
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                stringResource(R.string.joke_feed_greeting, state.userName.ifBlank { "друг" }),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Avatar(state.avatar, state.userName)
                            Text(stringResource(R.string.jokes_found, state.jokes.size))
                            OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.refresh_catalog))
                            }
                        }
                    }
                }
                items(state.jokes, key = { it.id }) { JokeItem(it) }
            }
        }
    }
}

@Composable
private fun EmptyContent(
    onRefresh: () -> Unit,
    onRestoreBackup: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier
) {
    Column(modifier.padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.empty_catalog_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.empty_catalog_message), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.try_again)) }
        OutlinedButton(onClick = onRestoreBackup, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.restore_backup)) }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.open_settings)) }
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Выйти") }
    }
}

@Composable
private fun JokeItem(joke: JokeCard) {
    val icon = when (joke.category) {
        JokeCategory.PROGRAMMING -> "</>"
        JokeCategory.PUN -> "?!"
        JokeCategory.SPOOKY -> "BOO"
        JokeCategory.CHRISTMAS -> "HO"
        else -> "HA"
    }
    val description = stringResource(R.string.joke_badge_description, joke.category.name)
    Card(Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(64.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center
            ) { Text(icon, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(joke.setup, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                joke.punchline?.takeIf { it.isNotBlank() }?.let { punchline ->
                    Text(punchline, style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    text = "${joke.category.label()} · ${joke.type.label()}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(stringResource(R.string.words_format, joke.wordCount), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun JokeCategory.label() = when (this) {
    JokeCategory.PROGRAMMING -> "Программирование"
    JokeCategory.MISC -> "Разное"
    JokeCategory.DARK -> "Чёрный юмор"
    JokeCategory.PUN -> "Каламбур"
    JokeCategory.SPOOKY -> "Хоррор"
    JokeCategory.CHRISTMAS -> "Новый год"
    JokeCategory.UNKNOWN -> "Без категории"
}

private fun JokeType.label() = when (this) {
    JokeType.SINGLE -> "Короткая"
    JokeType.TWOPART -> "Вопрос и ответ"
    JokeType.UNKNOWN -> "Неизвестный тип"
}
