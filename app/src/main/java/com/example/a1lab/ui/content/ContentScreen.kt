package com.example.a1lab.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a1lab.R
import com.example.a1lab.domain.JokeCard
import com.example.a1lab.domain.JokeCategory
import com.example.a1lab.domain.JokeType
import com.example.a1lab.ui.components.InitialsBadge
import com.example.a1lab.ui.theme.Amber
import com.example.a1lab.ui.theme.Terracotta
import com.example.a1lab.viewmodel.ContentUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(
    state: ContentUiState,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onRestoreBackup: () -> Unit,
    snackbarHost: @Composable () -> Unit
) {
    val settingsDescription = stringResource(R.string.open_settings_description)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.catalog_title), fontWeight = FontWeight.Bold)
                        Text(
                            text = stringResource(R.string.brand_tagline),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(width = 64.dp, height = 48.dp)
                            .semantics { contentDescription = settingsDescription }
                    ) {
                        Text("⚙", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.jokes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.loading_jokes), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            !state.errorMessage.isNullOrBlank() && state.jokes.isEmpty() -> {
                ErrorState(
                    message = state.errorMessage.orEmpty(),
                    onRefresh = onRefresh,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            state.jokes.isEmpty() -> {
                EmptyState(
                    backupAvailable = state.backupAvailable,
                    onRestoreBackup = onRestoreBackup,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        HeaderPanel(state, onRefresh)
                    }
                    items(state.jokes, key = { it.id }) { joke ->
                        JokeCardItem(joke)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderPanel(state: ContentUiState, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InitialsBadge(state.userName.ifBlank { "Punchline" }, size = 52)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.joke_feed_greeting, state.userName.ifBlank { "стендапер" }),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.jokes_found, state.jokes.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onRefresh, enabled = !state.isLoading) {
                Text("↻", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun JokeCardItem(joke: JokeCard) {
    val accentColor = when (joke.category) {
        JokeCategory.PROGRAMMING -> MaterialTheme.colorScheme.primary
        JokeCategory.PUN -> Amber
        JokeCategory.SPOOKY -> Terracotta
        else -> MaterialTheme.colorScheme.secondary
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            JokeBadge(joke)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = joke.setup,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
                if (!joke.punchline.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.punchline_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = joke.punchline,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(categoryText(joke.category)) })
                    Text(typeText(joke.type), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.words_format, joke.wordCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(stringResource(R.string.safe_badge), style = MaterialTheme.typography.labelMedium, color = accentColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun JokeBadge(joke: JokeCard) {
    val emoji = when (joke.category) {
        JokeCategory.PROGRAMMING -> "</>"
        JokeCategory.PUN -> "?!"
        JokeCategory.SPOOKY -> "BOO"
        JokeCategory.CHRISTMAS -> "HO"
        JokeCategory.DARK -> "..."
        else -> "HA"
    }
    val badgeDescription = stringResource(R.string.joke_badge_description, categoryText(joke.category))
    Box(
        modifier = Modifier
            .size(width = 68.dp, height = 88.dp)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
                ),
                RoundedCornerShape(14.dp)
            )
            .semantics { contentDescription = badgeDescription },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("JOKE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun categoryText(category: JokeCategory): String = stringResource(
    when (category) {
        JokeCategory.PROGRAMMING -> R.string.category_programming
        JokeCategory.MISC -> R.string.category_misc
        JokeCategory.DARK -> R.string.category_dark
        JokeCategory.PUN -> R.string.category_pun
        JokeCategory.SPOOKY -> R.string.category_spooky
        JokeCategory.CHRISTMAS -> R.string.category_christmas
        JokeCategory.UNKNOWN -> R.string.category_unknown
    }
)

@Composable
private fun typeText(type: JokeType): String = stringResource(
    when (type) {
        JokeType.SINGLE -> R.string.type_single
        JokeType.TWOPART -> R.string.type_twopart
        JokeType.UNKNOWN -> R.string.type_unknown
    }
)

