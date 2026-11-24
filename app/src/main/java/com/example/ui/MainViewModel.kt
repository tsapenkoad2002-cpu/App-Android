package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.OverlayPreferencesRepository
import com.example.data.ServiceStatusTracker
import com.example.model.ButtonSize
import com.example.model.OverlayMode
import com.example.model.OverlaySettings
import com.example.model.OverlayStyle
import com.example.model.ScrollMethod
import com.example.model.ScrollSpeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OverlayPreferencesRepository(application)

    val settings: StateFlow<OverlaySettings> = repository.settings
    val isServiceConnected: StateFlow<Boolean> = ServiceStatusTracker.isServiceConnected
    val isScrolling: StateFlow<Boolean> = ServiceStatusTracker.isScrolling

    private val _isAccessibilityEnabled = MutableStateFlow(
        ServiceStatusTracker.isAccessibilityEnabledInSystem(application)
    )
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _showDisclosureDialog = MutableStateFlow(false)
    val showDisclosureDialog: StateFlow<Boolean> = _showDisclosureDialog.asStateFlow()

    fun refreshAccessibilityStatus() {
        _isAccessibilityEnabled.value =
            ServiceStatusTracker.isAccessibilityEnabledInSystem(getApplication())
    }

    fun openDisclosureDialog() {
        _showDisclosureDialog.value = true
    }

    fun dismissDisclosureDialog() {
        _showDisclosureDialog.value = false
    }

    fun setOverlayEnabled(enabled: Boolean) {
        repository.setOverlayEnabled(enabled)
    }

    fun setOverlayMode(mode: OverlayMode) {
        repository.setOverlayMode(mode)
    }

    fun setButtonSize(size: ButtonSize) {
        repository.setButtonSize(size)
    }

    fun setOpacity(opacity: Float) {
        repository.setOpacity(opacity)
    }

    fun setStyle(style: OverlayStyle) {
        repository.setStyle(style)
    }

    fun setSnapToEdge(snap: Boolean) {
        repository.setSnapToEdge(snap)
    }

    fun setHapticFeedback(enabled: Boolean) {
        repository.setHapticFeedback(enabled)
    }

    fun setScrollMethod(method: ScrollMethod) {
        repository.setScrollMethod(method)
    }

    fun setScrollSpeed(speed: ScrollSpeed) {
        repository.setScrollSpeed(speed)
    }

    fun resetButtonPosition() {
        repository.resetButtonPosition()
    }

    override fun onCleared() {
        repository.cleanup()
        super.onCleared()
    }
}
