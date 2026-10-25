package com.example.a1lab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a1lab.R

@Composable
fun BrandedBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { content() }
}

@Composable
fun PunchlineLogo(logoSize: Int = 72, modifier: Modifier = Modifier) {
    val logoDescription = stringResource(R.string.punchline_logo_description)
    Surface(
        modifier = modifier.size(logoSize.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .semantics { contentDescription = logoDescription }
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "P",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = (logoSize * 0.42f).sp
            )
        }
    }
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PunchlineLogo(logoSize = if (compact) 54 else 70)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.brand_name),
                style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InitialsBadge(text: String, modifier: Modifier = Modifier, size: Int = 48) {
    val initials = text.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase()
    val safeText = remember(text) { initials.ifBlank { "P" } }
    Box(
        modifier = modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = safeText,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.34f).sp
        )
    }
}
