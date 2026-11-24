package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.ControlScreen
import com.example.ui.screens.TestFeedScreen
import com.example.ui.theme.NaverhTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NaverhTheme {
                NaverhRootApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaverhRootApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val isServiceConnected by viewModel.isServiceConnected.collectAsStateWithLifecycle()
    val showDisclosure by viewModel.showDisclosureDialog.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Refresh service state immediately whenever user returns from System Accessibility Settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshAccessibilityStatus()
    }

    // BackHandler returns from Test tab to Control tab cleanly
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val openAccessibilitySettings = {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val openBrowserSample: () -> Unit = {
        val uri = Uri.parse("https://ru.wikipedia.org/wiki/Android")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDisclosureDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.AccessibilityNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = stringResource(R.string.disclosure_title))
            },
            text = {
                Text(
                    text = stringResource(R.string.disclosure_body),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissDisclosureDialog()
                        openAccessibilitySettings()
                    },
                    modifier = Modifier.testTag("disclosure_confirm_button")
                ) {
                    Text(stringResource(R.string.disclosure_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDisclosureDialog() },
                    modifier = Modifier.testTag("disclosure_cancel_button")
                ) {
                    Text(stringResource(R.string.disclosure_cancel))
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Rounded.Tune else Icons.Outlined.Tune,
                                    contentDescription = stringResource(R.string.tab_control)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_control)) },
                            modifier = Modifier.testTag("nav_tab_control")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Rounded.FormatListNumbered else Icons.Outlined.FormatListNumbered,
                                    contentDescription = stringResource(R.string.tab_test)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_test)) },
                            modifier = Modifier.testTag("nav_tab_test")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        NavigationRailItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Rounded.Tune else Icons.Outlined.Tune,
                                    contentDescription = stringResource(R.string.tab_control)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_control)) },
                            modifier = Modifier.testTag("rail_tab_control")
                        )
                        NavigationRailItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Rounded.FormatListNumbered else Icons.Outlined.FormatListNumbered,
                                    contentDescription = stringResource(R.string.tab_test)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_test)) },
                            modifier = Modifier.testTag("rail_tab_test")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier.widthIn(max = 680.dp)
                    when (selectedTab) {
                        0 -> ControlScreen(
                            settings = settings,
                            isAccessibilityEnabled = isAccessibilityEnabled,
                            isServiceConnected = isServiceConnected,
                            onRequestEnableService = { viewModel.openDisclosureDialog() },
                            onOpenAccessibilitySettings = openAccessibilitySettings,
                            onToggleOverlayEnabled = { viewModel.setOverlayEnabled(it) },
                            onSelectMode = { viewModel.setOverlayMode(it) },
                            onSelectSize = { viewModel.setButtonSize(it) },
                            onChangeOpacity = { viewModel.setOpacity(it) },
                            onSelectStyle = { viewModel.setStyle(it) },
                            onToggleSnapToEdge = { viewModel.setSnapToEdge(it) },
                            onToggleHaptic = { viewModel.setHapticFeedback(it) },
                            onSelectScrollMethod = { viewModel.setScrollMethod(it) },
                            onSelectScrollSpeed = { viewModel.setScrollSpeed(it) },
                            onResetPosition = { viewModel.resetButtonPosition() },
                            modifier = contentModifier
                        )
                        1 -> TestFeedScreen(
                            onOpenBrowserSample = openBrowserSample,
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }
}
