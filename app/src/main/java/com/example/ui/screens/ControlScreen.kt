package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PanToolAlt
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwipeDown
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ButtonSize
import com.example.model.OverlayMode
import com.example.model.OverlaySettings
import com.example.model.OverlayStyle
import com.example.model.ScrollMethod
import com.example.model.ScrollSpeed
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldActive
import kotlin.math.roundToInt

@Composable
fun ControlScreen(
    settings: OverlaySettings,
    isAccessibilityEnabled: Boolean,
    isServiceConnected: Boolean,
    onRequestEnableService: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleOverlayEnabled: (Boolean) -> Unit,
    onSelectMode: (OverlayMode) -> Unit,
    onSelectSize: (ButtonSize) -> Unit,
    onChangeOpacity: (Float) -> Unit,
    onSelectStyle: (OverlayStyle) -> Unit,
    onToggleSnapToEdge: (Boolean) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onSelectScrollMethod: (ScrollMethod) -> Unit,
    onSelectScrollSpeed: (ScrollSpeed) -> Unit,
    onResetPosition: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActiveOverlay = isAccessibilityEnabled && settings.overlayEnabled

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Status Card
        item {
            HeroStatusCard(
                isAccessibilityEnabled = isAccessibilityEnabled,
                isServiceConnected = isServiceConnected,
                isOverlayEnabled = settings.overlayEnabled,
                isActiveOverlay = isActiveOverlay,
                onRequestEnableService = onRequestEnableService,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onToggleOverlayEnabled = onToggleOverlayEnabled
            )
        }

        // 2. Overlay Mode Selection
        item {
            SectionTitle(title = stringResource(R.string.section_overlay_mode))
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeOptionCard(
                    title = stringResource(R.string.mode_floating_title),
                    subtitle = stringResource(R.string.mode_floating_desc),
                    icon = Icons.Rounded.TouchApp,
                    selected = settings.overlayMode == OverlayMode.FLOATING_BUTTON,
                    testTag = "mode_floating_card",
                    onClick = { onSelectMode(OverlayMode.FLOATING_BUTTON) }
                )
                ModeOptionCard(
                    title = stringResource(R.string.mode_top_bar_title),
                    subtitle = stringResource(R.string.mode_top_bar_desc),
                    icon = Icons.Rounded.Dock,
                    selected = settings.overlayMode == OverlayMode.TOP_BAR,
                    testTag = "mode_top_bar_card",
                    onClick = { onSelectMode(OverlayMode.TOP_BAR) }
                )
                ModeOptionCard(
                    title = stringResource(R.string.mode_both_title),
                    subtitle = stringResource(R.string.mode_both_desc),
                    icon = Icons.Rounded.Layers,
                    selected = settings.overlayMode == OverlayMode.BOTH,
                    testTag = "mode_both_card",
                    onClick = { onSelectMode(OverlayMode.BOTH) }
                )
            }
        }

        // 3. Live Interactive Preview Card
        item {
            OverlayPreviewCard(settings = settings)
        }

        // 4. Visual Appearance Card
        item {
            SectionTitle(title = stringResource(R.string.section_appearance))
            Spacer(modifier = Modifier.height(8.dp))
            AppearanceSettingsCard(
                settings = settings,
                onSelectSize = onSelectSize,
                onChangeOpacity = onChangeOpacity,
                onSelectStyle = onSelectStyle,
                onToggleSnapToEdge = onToggleSnapToEdge,
                onToggleHaptic = onToggleHaptic,
                onResetPosition = onResetPosition
            )
        }

        // 5. Scroll Behavior Card
        item {
            SectionTitle(title = stringResource(R.string.section_scroll_behavior))
            Spacer(modifier = Modifier.height(8.dp))
            ScrollBehaviorCard(
                settings = settings,
                onSelectScrollMethod = onSelectScrollMethod,
                onSelectScrollSpeed = onSelectScrollSpeed
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HeroStatusCard(
    isAccessibilityEnabled: Boolean,
    isServiceConnected: Boolean,
    isOverlayEnabled: Boolean,
    isActiveOverlay: Boolean,
    onRequestEnableService: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleOverlayEnabled: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActiveOverlay) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            isActiveOverlay -> EmeraldActive
            isAccessibilityEnabled -> MaterialTheme.colorScheme.secondary
            else -> AmberWarning
        },
        label = "status_color"
    )

    val statusLabel = when {
        isActiveOverlay -> stringResource(R.string.status_active)
        isAccessibilityEnabled -> stringResource(R.string.status_paused)
        else -> stringResource(R.string.status_permission_needed)
    }

    val statusDescription = when {
        isActiveOverlay -> stringResource(R.string.status_desc_active)
        isAccessibilityEnabled -> stringResource(R.string.status_desc_paused)
        else -> stringResource(R.string.status_desc_permission)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.14f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isActiveOverlay -> Icons.Rounded.VerticalAlignTop
                                isAccessibilityEnabled -> Icons.Rounded.PauseCircle
                                else -> Icons.Rounded.WarningAmber
                            },
                            contentDescription = stringResource(R.string.cd_status_icon),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.hero_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = statusDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isAccessibilityEnabled && !isServiceConnected) {
                    Button(
                        onClick = onRequestEnableService,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("enable_service_button"),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessibilityNew,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.action_enable_service),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                MaterialTheme.shapes.medium
                            )
                            .clickable { onToggleOverlayEnabled(!isOverlayEnabled) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (isOverlayEnabled) EmeraldActive else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.overlay_toggle_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isOverlayEnabled,
                            onCheckedChange = onToggleOverlayEnabled,
                            modifier = Modifier.testTag("overlay_master_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenAccessibilitySettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("open_accessibility_settings_button"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.action_open_settings))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(width = if (selected) 1.5.dp else 1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_mode_icon),
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppearanceSettingsCard(
    settings: OverlaySettings,
    onSelectSize: (ButtonSize) -> Unit,
    onChangeOpacity: (Float) -> Unit,
    onSelectStyle: (OverlayStyle) -> Unit,
    onToggleSnapToEdge: (Boolean) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onResetPosition: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Size selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_button_size),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizes = listOf(
                        ButtonSize.COMPACT to stringResource(R.string.size_compact),
                        ButtonSize.MEDIUM to stringResource(R.string.size_medium),
                        ButtonSize.LARGE to stringResource(R.string.size_large)
                    )
                    sizes.forEach { (size, label) ->
                        FilterChip(
                            selected = settings.buttonSize == size,
                            onClick = { onSelectSize(size) },
                            label = { Text(label) },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("size_chip_${size.id}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Style selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_theme_style),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val styles = listOf(
                        Triple(OverlayStyle.COBALT, stringResource(R.string.style_cobalt), Color(0xFF1D4ED8)),
                        Triple(OverlayStyle.OBSIDIAN, stringResource(R.string.style_obsidian), Color(0xFF0F172A)),
                        Triple(OverlayStyle.GLASS, stringResource(R.string.style_glass), Color(0xFFE2E8F0)),
                        Triple(OverlayStyle.MINIMAL, stringResource(R.string.style_minimal), Color(0xFF64748B))
                    )
                    styles.forEach { (style, label, dotColor) ->
                        val isSelected = settings.style == style
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { onSelectStyle(style) }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("style_option_${style.id}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Opacity slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_opacity),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${(settings.opacity * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = settings.opacity,
                    onValueChange = onChangeOpacity,
                    valueRange = 0.2f..1.0f,
                    modifier = Modifier.testTag("opacity_slider")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Snap to edge toggle
            SettingToggleRow(
                title = stringResource(R.string.label_snap_edge),
                subtitle = stringResource(R.string.desc_snap_edge),
                icon = Icons.Rounded.PanToolAlt,
                checked = settings.snapToEdge,
                testTag = "snap_edge_switch",
                onCheckedChange = onToggleSnapToEdge
            )

            // Haptic toggle
            SettingToggleRow(
                title = stringResource(R.string.label_haptic),
                subtitle = stringResource(R.string.desc_haptic),
                icon = Icons.Rounded.Vibration,
                checked = settings.hapticFeedback,
                testTag = "haptic_switch",
                onCheckedChange = onToggleHaptic
            )

            if (settings.overlayMode != OverlayMode.TOP_BAR) {
                OutlinedButton(
                    onClick = onResetPosition,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("reset_position_button"),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.label_reset_position))
                }
            }
        }
    }
}

@Composable
private fun ScrollBehaviorCard(
    settings: OverlaySettings,
    onSelectScrollMethod: (ScrollMethod) -> Unit,
    onSelectScrollSpeed: (ScrollSpeed) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.label_scroll_method),
                style = MaterialTheme.typography.titleSmall
            )

            val methods = listOf(
                Triple(
                    ScrollMethod.SMART,
                    stringResource(R.string.method_smart_title),
                    stringResource(R.string.method_smart_desc)
                ),
                Triple(
                    ScrollMethod.GESTURE,
                    stringResource(R.string.method_gesture_title),
                    stringResource(R.string.method_gesture_desc)
                ),
                Triple(
                    ScrollMethod.ACCESSIBILITY_ONLY,
                    stringResource(R.string.method_api_title),
                    stringResource(R.string.method_api_desc)
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.forEach { (method, title, desc) ->
                    val selected = settings.scrollMethod == method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { onSelectScrollMethod(method) }
                            .padding(12.dp)
                            .testTag("scroll_method_${method.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = when (method) {
                                ScrollMethod.SMART -> Icons.Rounded.AutoAwesome
                                ScrollMethod.GESTURE -> Icons.Rounded.SwipeDown
                                ScrollMethod.ACCESSIBILITY_ONLY -> Icons.Rounded.VerticalAlignTop
                            },
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.label_scroll_speed),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val speeds = listOf(
                        ScrollSpeed.QUICK to stringResource(R.string.speed_quick),
                        ScrollSpeed.BALANCED to stringResource(R.string.speed_balanced),
                        ScrollSpeed.DEEP to stringResource(R.string.speed_deep)
                    )
                    speeds.forEach { (speed, label) ->
                        FilterChip(
                            selected = settings.scrollSpeed == speed,
                            onClick = { onSelectScrollSpeed(speed) },
                            label = { Text(label, maxLines = 1) },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("speed_chip_${speed.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
