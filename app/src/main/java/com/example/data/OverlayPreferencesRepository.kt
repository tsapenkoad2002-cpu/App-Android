package com.example.data

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityManager
import com.example.model.ButtonSize
import com.example.model.OverlayMode
import com.example.model.OverlaySettings
import com.example.model.OverlayStyle
import com.example.model.ScrollMethod
import com.example.model.ScrollSpeed
import com.example.service.ScrollTopAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ServiceStatusTracker {
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _isScrolling = MutableStateFlow(false)
    val isScrolling: StateFlow<Boolean> = _isScrolling.asStateFlow()

    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    fun setScrolling(scrolling: Boolean) {
        _isScrolling.value = scrolling
    }

    fun isAccessibilityEnabledInSystem(context: Context): Boolean {
        if (_isServiceConnected.value) return true
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedPackage = context.packageName
        val expectedClass = ScrollTopAccessibilityService::class.java.name
        return enabledServices.any { info ->
            val serviceInfo = info.resolveInfo?.serviceInfo ?: return@any false
            serviceInfo.packageName == expectedPackage && serviceInfo.name == expectedClass
        }
    }
}

class OverlayPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<OverlaySettings> = _settings.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settings.value = readSettings()
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun cleanup() {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun readSettings(): OverlaySettings {
        return OverlaySettings(
            overlayEnabled = prefs.getBoolean(KEY_OVERLAY_ENABLED, true),
            overlayMode = OverlayMode.fromId(prefs.getString(KEY_OVERLAY_MODE, null)),
            buttonSize = ButtonSize.fromId(prefs.getString(KEY_BUTTON_SIZE, null)),
            opacity = prefs.getFloat(KEY_OPACITY, 0.88f).coerceIn(0.15f, 1.0f),
            style = OverlayStyle.fromId(prefs.getString(KEY_STYLE, null)),
            snapToEdge = prefs.getBoolean(KEY_SNAP_TO_EDGE, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            scrollMethod = ScrollMethod.fromId(prefs.getString(KEY_SCROLL_METHOD, null)),
            scrollSpeed = ScrollSpeed.fromId(prefs.getString(KEY_SCROLL_SPEED, null)),
            savedButtonX = prefs.getInt(KEY_SAVED_X, -1),
            savedButtonY = prefs.getInt(KEY_SAVED_Y, -1)
        )
    }

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    }

    fun setOverlayMode(mode: OverlayMode) {
        prefs.edit().putString(KEY_OVERLAY_MODE, mode.id).apply()
    }

    fun setButtonSize(size: ButtonSize) {
        prefs.edit().putString(KEY_BUTTON_SIZE, size.id).apply()
    }

    fun setOpacity(opacity: Float) {
        prefs.edit().putFloat(KEY_OPACITY, opacity.coerceIn(0.15f, 1.0f)).apply()
    }

    fun setStyle(style: OverlayStyle) {
        prefs.edit().putString(KEY_STYLE, style.id).apply()
    }

    fun setSnapToEdge(snap: Boolean) {
        prefs.edit().putBoolean(KEY_SNAP_TO_EDGE, snap).apply()
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
    }

    fun setScrollMethod(method: ScrollMethod) {
        prefs.edit().putString(KEY_SCROLL_METHOD, method.id).apply()
    }

    fun setScrollSpeed(speed: ScrollSpeed) {
        prefs.edit().putString(KEY_SCROLL_SPEED, speed.id).apply()
    }

    fun saveButtonPosition(x: Int, y: Int) {
        prefs.edit()
            .putInt(KEY_SAVED_X, x)
            .putInt(KEY_SAVED_Y, y)
            .apply()
    }

    fun resetButtonPosition() {
        prefs.edit()
            .putInt(KEY_SAVED_X, -1)
            .putInt(KEY_SAVED_Y, -1)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "naverh_overlay_prefs"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        private const val KEY_OVERLAY_MODE = "overlay_mode"
        private const val KEY_BUTTON_SIZE = "button_size"
        private const val KEY_OPACITY = "opacity"
        private const val KEY_STYLE = "style"
        private const val KEY_SNAP_TO_EDGE = "snap_to_edge"
        private const val KEY_HAPTIC = "haptic"
        private const val KEY_SCROLL_METHOD = "scroll_method"
        private const val KEY_SCROLL_SPEED = "scroll_speed"
        private const val KEY_SAVED_X = "saved_x"
        private const val KEY_SAVED_Y = "saved_y"
    }
}
