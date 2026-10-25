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
fun PunchlineLogo(logoSize: Int = 72, modifier: Modifier = Modifier) {
    val markColor = MaterialTheme.colorScheme.onPrimary
    val smileColor = MaterialTheme.colorScheme.primaryContainer
    val logoDescription = stringResource(R.string.punchline_logo_description)
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
            drawCircle(markColor, radius = size.minDimension * 0.43f, center = Offset(size.width / 2, size.height * 0.48f))
            drawArc(
                color = smileColor,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(size.width * 0.16f, size.height * 0.2f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.68f, size.height * 0.56f),
                style = Stroke(width = 5.dp.toPx())
            )
            drawCircle(Amber, radius = size.minDimension * 0.055f, center = Offset(size.width * 0.3f, size.height * 0.47f))
            drawCircle(Amber, radius = size.minDimension * 0.055f, center = Offset(size.width * 0.7f, size.height * 0.47f))
            drawArc(
                color = Amber,
                startAngle = 18f,
                sweepAngle = 144f,
                useCenter = false,
                topLeft = Offset(size.width * 0.26f, size.height * 0.42f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.48f, size.height * 0.34f),
                style = Stroke(width = 4.dp.toPx())
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
