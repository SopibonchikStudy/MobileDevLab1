package com.example.a1lab.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a1lab.R
import com.example.a1lab.ui.theme.Amber
import com.example.a1lab.ui.theme.Emerald

@Composable
fun BrandedBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.background, colors.primaryContainer.copy(alpha = 0.45f), colors.background)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(5) { index ->
                val y = size.height * (0.2f + index * 0.16f)
                drawLine(
                    color = colors.onSurface.copy(alpha = 0.035f),
                    start = Offset(-size.width * 0.1f, y),
                    end = Offset(size.width * 1.1f, y + size.height * 0.08f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            drawCircle(
                color = colors.secondary.copy(alpha = 0.08f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.9f, size.height * 0.08f)
            )
        }
        content()
    }
}

@Composable
fun BooklineLogo(logoSize: Int = 72, modifier: Modifier = Modifier) {
    val bookColor = MaterialTheme.colorScheme.onPrimary
    val spineColor = MaterialTheme.colorScheme.primaryContainer
    val logoDescription = stringResource(R.string.bookline_logo_description)
    Surface(
        modifier = modifier.size(logoSize.dp),
        shape = RoundedCornerShape((logoSize * 0.3f).dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding((logoSize * 0.2f).dp)
                .semantics { contentDescription = logoDescription }
        ) {
            val center = size.width / 2
            val left = Path().apply {
                moveTo(center, size.height * 0.2f)
                quadraticTo(size.width * 0.18f, size.height * 0.08f, 0f, size.height * 0.28f)
                lineTo(0f, size.height * 0.82f)
                quadraticTo(size.width * 0.23f, size.height * 0.62f, center, size.height * 0.78f)
                close()
            }
            val right = Path().apply {
                moveTo(center, size.height * 0.2f)
                quadraticTo(size.width * 0.82f, size.height * 0.08f, size.width, size.height * 0.28f)
                lineTo(size.width, size.height * 0.82f)
                quadraticTo(size.width * 0.77f, size.height * 0.62f, center, size.height * 0.78f)
                close()
            }
            drawPath(left, color = bookColor)
            drawPath(right, color = bookColor)
            drawLine(spineColor, Offset(center, size.height * 0.22f), Offset(center, size.height * 0.74f), 2.dp.toPx())
            drawPath(
                Path().apply {
                    moveTo(center + size.width * 0.2f, size.height * 0.28f)
                    lineTo(center + size.width * 0.2f, size.height * 0.68f)
                },
                color = Amber,
                style = Stroke(width = 3.dp.toPx())
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
        BooklineLogo(logoSize = if (compact) 54 else 70)
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
    val safeText = remember(text) { initials.ifBlank { "B" } }
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                Brush.linearGradient(listOf(Emerald, MaterialTheme.colorScheme.secondary)),
                RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = safeText,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.34f).sp
        )
    }
}
