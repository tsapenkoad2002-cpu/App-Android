package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ButtonSize
import com.example.model.OverlayMode
import com.example.model.OverlaySettings
import com.example.model.OverlayStyle
import kotlinx.coroutines.launch

@Composable
fun OverlayPreviewCard(
    settings: OverlaySettings,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollOffsetAnim = remember { Animatable(0f) }

    val triggerDemoScroll: () -> Unit = {
        scope.launch {
            scrollOffsetAnim.snapTo(42f)
            scrollOffsetAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
            )
        }
    }

    val (bgColor, strokeColor, fgColor) = when (settings.style) {
        OverlayStyle.COBALT -> Triple(Color(0xFF1D4ED8), Color(0xFF93C5FD), Color.White)
        OverlayStyle.OBSIDIAN -> Triple(Color(0xFF0F172A), Color(0xFF64748B), Color(0xFFF8FAFC))
        OverlayStyle.GLASS -> Triple(Color(0xFFFFFFFF), Color(0xFFCBD5E1), Color(0xFF0F172A))
        OverlayStyle.MINIMAL -> Triple(Color(0x660F172A), Color(0x66FFFFFF), Color.White)
    }

    val fabSizeDp = when (settings.buttonSize) {
        ButtonSize.COMPACT -> 36.dp
        ButtonSize.MEDIUM -> 44.dp
        ButtonSize.LARGE -> 52.dp
    }

    val topBarWidthDp = when (settings.buttonSize) {
        ButtonSize.COMPACT -> 84.dp
        ButtonSize.MEDIUM -> 112.dp
        ButtonSize.LARGE -> 144.dp
    }

    val topBarHeightDp = when (settings.buttonSize) {
        ButtonSize.COMPACT -> 18.dp
        ButtonSize.MEDIUM -> 22.dp
        ButtonSize.LARGE -> 26.dp
    }

    val showTopBar = settings.overlayMode == OverlayMode.TOP_BAR ||
        settings.overlayMode == OverlayMode.BOTH
    val showFloatingBtn = settings.overlayMode == OverlayMode.FLOATING_BUTTON ||
        settings.overlayMode == OverlayMode.BOTH

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.preview_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(settings.opacity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stylized mini phone viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                // Simulated scrollable page skeleton
                val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                val accentLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 26.dp)
                        .offset(y = (-scrollOffsetAnim.value).dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(accentLineColor)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(lineColor)
                            )
                            Box(
                                modifier = Modifier
                                    .width(76.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(lineColor)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(lineColor)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(lineColor)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.64f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(lineColor)
                    )
                }

                // Subtle simulated status bar dots on left & right
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .align(Alignment.TopCenter)
                ) {
                    val dotColor = Color.Gray.copy(alpha = 0.35f)
                    drawCircle(dotColor, radius = 3.dp.toPx(), center = Offset(24.dp.toPx(), 11.dp.toPx()))
                    drawCircle(dotColor, radius = 3.dp.toPx(), center = Offset(34.dp.toPx(), 11.dp.toPx()))
                    drawCircle(dotColor, radius = 3.dp.toPx(), center = Offset(size.width - 24.dp.toPx(), 11.dp.toPx()))
                }

                // Top Bar (iPhone-style) preview
                androidx.compose.animation.AnimatedVisibility(
                    visible = showTopBar,
                    enter = fadeIn() + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(topBarWidthDp)
                            .height(topBarHeightDp)
                            .alpha(settings.opacity)
                            .clip(CircleShape)
                            .background(bgColor)
                            .border(1.dp, strokeColor.copy(alpha = 0.7f), CircleShape)
                            .clickable(onClick = triggerDemoScroll)
                            .testTag("preview_top_bar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerticalAlignTop,
                            contentDescription = stringResource(R.string.preview_hint_top),
                            tint = fgColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Floating Button preview
                androidx.compose.animation.AnimatedVisibility(
                    visible = showFloatingBtn,
                    enter = fadeIn() + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(fabSizeDp)
                            .alpha(settings.opacity)
                            .clip(CircleShape)
                            .background(bgColor)
                            .border(1.2.dp, strokeColor.copy(alpha = 0.7f), CircleShape)
                            .clickable(onClick = triggerDemoScroll)
                            .testTag("preview_floating_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerticalAlignTop,
                            contentDescription = stringResource(R.string.preview_hint_fab),
                            tint = fgColor,
                            modifier = Modifier.size(fabSizeDp * 0.48f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.hint_gestures_footer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
