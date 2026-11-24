package com.example.model

enum class OverlayMode(val id: String) {
    FLOATING_BUTTON("floating"),
    TOP_BAR("top_bar"),
    BOTH("both");

    companion object {
        fun fromId(id: String?): OverlayMode = entries.firstOrNull { it.id == id } ?: FLOATING_BUTTON
    }
}

enum class OverlayStyle(val id: String) {
    COBALT("cobalt"),
    OBSIDIAN("obsidian"),
    GLASS("glass"),
    MINIMAL("minimal");

    companion object {
        fun fromId(id: String?): OverlayStyle = entries.firstOrNull { it.id == id } ?: COBALT
    }
}

enum class ButtonSize(val id: String, val dpValue: Int, val topBarWidthDp: Int, val topBarHeightDp: Int) {
    COMPACT("compact", 44, 110, 26),
    MEDIUM("medium", 54, 148, 32),
    LARGE("large", 64, 192, 38);

    companion object {
        fun fromId(id: String?): ButtonSize = entries.firstOrNull { it.id == id } ?: MEDIUM
    }
}

enum class ScrollMethod(val id: String) {
    SMART("smart"),
    GESTURE("gesture"),
    ACCESSIBILITY_ONLY("api");

    companion object {
        fun fromId(id: String?): ScrollMethod = entries.firstOrNull { it.id == id } ?: SMART
    }
}

enum class ScrollSpeed(val id: String, val maxNodeSteps: Int, val gestureCount: Int, val gestureDurationMs: Long) {
    QUICK("quick", maxNodeSteps = 10, gestureCount = 3, gestureDurationMs = 95L),
    BALANCED("balanced", maxNodeSteps = 22, gestureCount = 5, gestureDurationMs = 85L),
    DEEP("deep", maxNodeSteps = 45, gestureCount = 9, gestureDurationMs = 75L);

    companion object {
        fun fromId(id: String?): ScrollSpeed = entries.firstOrNull { it.id == id } ?: BALANCED
    }
}

data class OverlaySettings(
    val overlayEnabled: Boolean = true,
    val overlayMode: OverlayMode = OverlayMode.FLOATING_BUTTON,
    val buttonSize: ButtonSize = ButtonSize.MEDIUM,
    val opacity: Float = 0.88f,
    val style: OverlayStyle = OverlayStyle.COBALT,
    val snapToEdge: Boolean = true,
    val hapticFeedback: Boolean = true,
    val scrollMethod: ScrollMethod = ScrollMethod.SMART,
    val scrollSpeed: ScrollSpeed = ScrollSpeed.BALANCED,
    val savedButtonX: Int = -1,
    val savedButtonY: Int = -1
)
