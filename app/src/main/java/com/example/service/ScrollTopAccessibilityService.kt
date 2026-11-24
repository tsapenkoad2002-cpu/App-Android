package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.res.Configuration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity
import com.example.data.OverlayPreferencesRepository
import com.example.data.ServiceStatusTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ScrollTopAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: OverlayPreferencesRepository
    private lateinit var overlayController: OverlayWindowController
    private lateinit var scrollEngine: ScrollEngine
    private var scrollJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        ServiceStatusTracker.setServiceConnected(true)

        repository = OverlayPreferencesRepository(this)
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        scrollEngine = ScrollEngine(this)

        overlayController = OverlayWindowController(
            context = this,
            windowManager = windowManager,
            onTriggerScrollTop = { handleTriggerScrollTop() },
            onLongPressOpenApp = { openSettingsApp() },
            onButtonPositionSaved = { x, y -> repository.saveButtonPosition(x, y) }
        )

        serviceScope.launch {
            repository.settings.collectLatest { settings ->
                overlayController.applySettings(settings)
            }
        }
    }

    private fun handleTriggerScrollTop() {
        // Tapping again while an auto-scroll burst is running cancels it immediately
        if (scrollJob?.isActive == true) {
            scrollJob?.cancel()
            scrollJob = null
            ServiceStatusTracker.setScrolling(false)
            overlayController.setScrollingState(false)
            return
        }

        val settingsSnapshot = repository.settings.value
        scrollJob = serviceScope.launch {
            ServiceStatusTracker.setScrolling(true)
            overlayController.setScrollingState(true)
            try {
                scrollEngine.scrollToTop(settingsSnapshot)
            } finally {
                ServiceStatusTracker.setScrolling(false)
                overlayController.setScrollingState(false)
                scrollJob = null
            }
        }
    }

    private fun openSettingsApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::overlayController.isInitialized && ::repository.isInitialized) {
            overlayController.applySettings(repository.settings.value)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No passive event logging or inspection — scrolling runs strictly on explicit user tap.
    }

    override fun onInterrupt() {
        scrollJob?.cancel()
        ServiceStatusTracker.setScrolling(false)
        if (::overlayController.isInitialized) {
            overlayController.setScrollingState(false)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        teardownService()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        teardownService()
        super.onDestroy()
    }

    private fun teardownService() {
        scrollJob?.cancel()
        ServiceStatusTracker.setScrolling(false)
        ServiceStatusTracker.setServiceConnected(false)
        if (::overlayController.isInitialized) {
            overlayController.removeAllOverlays()
        }
        if (::repository.isInitialized) {
            repository.cleanup()
        }
        serviceScope.cancel()
    }
}
