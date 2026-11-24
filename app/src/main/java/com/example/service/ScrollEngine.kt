package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.example.model.OverlaySettings
import com.example.model.ScrollMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.ArrayDeque
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

class ScrollEngine(private val service: AccessibilityService) {

    suspend fun scrollToTop(settings: OverlaySettings) {
        val speed = settings.scrollSpeed
        when (settings.scrollMethod) {
            ScrollMethod.ACCESSIBILITY_ONLY -> {
                performNodeScrollToTop(speed.maxNodeSteps)
            }
            ScrollMethod.GESTURE -> {
                performGestureScrollToTop(speed.gestureCount, speed.gestureDurationMs)
            }
            ScrollMethod.SMART -> {
                val result = performNodeScrollToTop(speed.maxNodeSteps)
                // If no vertical scrollable node responded (common in browsers/WebViews/custom engines),
                // fall back to smooth downward fling gestures so content scrolls to the very top.
                if (!result.foundVerticalTarget) {
                    performGestureScrollToTop(speed.gestureCount, speed.gestureDurationMs)
                }
            }
        }
    }

    private data class NodeScrollResult(
        val foundVerticalTarget: Boolean,
        val didScroll: Boolean
    )

    private suspend fun performNodeScrollToTop(maxSteps: Int): NodeScrollResult {
        val target = findBestVerticalScrollableNode() ?: return NodeScrollResult(
            foundVerticalTarget = false,
            didScroll = false
        )

        var didScroll = false

        // 1. Try instant jump-to-position if supported (e.g. RecyclerView / ListView / Compose LazyList)
        val supportsScrollToPos = target.actionList.any {
            it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.id
        }
        if (supportsScrollToPos) {
            val args = Bundle().apply {
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, 0)
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_COLUMN_INT, 0)
            }
            if (target.performAction(
                    AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.id,
                    args
                )
            ) {
                didScroll = true
                delay(40L)
                target.refresh()
            }
        }

        // 2. Stepwise scroll upward using ACTION_SCROLL_UP or ACTION_SCROLL_BACKWARD
        val scrollActionId = chooseVerticalUpActionId(target)
        var consecutiveFailures = 0
        for (step in 0 until maxSteps) {
            if (!coroutineContext.isActive) break
            val scrolled = target.performAction(scrollActionId)
            if (scrolled) {
                didScroll = true
                consecutiveFailures = 0
                delay(32L)
                target.refresh()
            } else {
                consecutiveFailures++
                // Sometimes a second refresh or fallback to ACTION_SCROLL_BACKWARD succeeds
                if (scrollActionId != AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD &&
                    consecutiveFailures == 1
                ) {
                    val fallbackScrolled = target.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                    if (fallbackScrolled) {
                        didScroll = true
                        consecutiveFailures = 0
                        delay(32L)
                        target.refresh()
                        continue
                    }
                }
                break
            }
        }

        return NodeScrollResult(foundVerticalTarget = true, didScroll = didScroll)
    }

    private fun chooseVerticalUpActionId(node: AccessibilityNodeInfo): Int {
        val hasScrollUp = node.actionList.any {
            it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id
        }
        return if (hasScrollUp) {
            AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }
    }

    private fun findBestVerticalScrollableNode(): AccessibilityNodeInfo? {
        val (screenW, screenH) = getScreenDimensions()
        val roots = collectActiveAppRoots()
        var bestNode: AccessibilityNodeInfo? = null
        var bestScore = -1L

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        roots.forEach { queue.add(it) }

        val bounds = Rect()
        var visitedCount = 0
        val maxVisit = 400

        while (queue.isNotEmpty() && visitedCount < maxVisit) {
            val node = queue.removeFirst()
            visitedCount++

            if (!node.isVisibleToUser) continue

            val actions = node.actionList
            val hasUp = actions.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id }
            val hasDown = actions.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id }
            val hasLeft = actions.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id }
            val hasRight = actions.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id }
            val hasBackward = actions.any { it.id == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD }
            val hasForward = actions.any { it.id == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD }
            val hasScrollToPos = actions.any {
                it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.id
            }

            val isExplicitHorizontalOnly = (hasLeft || hasRight) && !hasUp && !hasDown
            val isExplicitVertical = hasUp || hasDown
            val isGenericScrollable = (node.isScrollable || hasBackward || hasForward || hasScrollToPos) &&
                !isExplicitHorizontalOnly

            if (isExplicitVertical || isGenericScrollable) {
                node.getBoundsInScreen(bounds)
                val w = bounds.width().coerceAtLeast(0)
                val h = bounds.height().coerceAtLeast(0)
                if (w > 40 && h > 80) {
                    var score = w.toLong() * h.toLong()
                    // Strongly prefer tall containers (main feed/webview) over small horizontal bars
                    if (h >= screenH * 0.35f) score *= 3L
                    if (isExplicitVertical) score *= 2L
                    if (hasUp || hasBackward) score += 500_000L
                    if (w > h * 2 && !isExplicitVertical) {
                        // Wide shallow strip is likely a horizontal pager/carousel
                        score /= 8L
                    }
                    if (score > bestScore) {
                        bestScore = score
                        bestNode = node
                    }
                }
            }

            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                queue.add(child)
            }
        }

        // Also check screenW just to keep bounds check clean if needed
        if (bestNode == null && screenW > 0) return null
        return bestNode
    }

    private fun collectActiveAppRoots(): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        try {
            val windows = service.windows
            if (!windows.isNullOrEmpty()) {
                val appWindows = windows
                    .filter { win ->
                        win.type != AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY &&
                            win.type != AccessibilityWindowInfo.TYPE_SYSTEM
                    }
                    .sortedByDescending { if (it.isActive) 1 else 0 }

                for (win in appWindows) {
                    win.root?.let { result.add(it) }
                }
            }
        } catch (_: Exception) {
            // Fallback below
        }

        if (result.isEmpty()) {
            service.rootInActiveWindow?.let { result.add(it) }
        }
        return result
    }

    private suspend fun performGestureScrollToTop(flingCount: Int, durationMs: Long) {
        val (screenW, screenH) = getScreenDimensions()
        val x = screenW * 0.5f
        // Start comfortably below browser URL bars & status bar, end near bottom
        val startY = screenH * 0.30f
        val endY = screenH * 0.86f

        for (i in 0 until flingCount) {
            if (!coroutineContext.isActive) break
            val dispatched = dispatchSingleSwipe(x, startY, x, endY, durationMs)
            if (!dispatched) break
            delay(95L)
        }
    }

    private suspend fun dispatchSingleSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long
    ): Boolean = suspendCancellableCoroutine { cont ->
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                if (cont.isActive) cont.resume(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                if (cont.isActive) cont.resume(false)
            }
        }

        val accepted = service.dispatchGesture(gesture, callback, null)
        if (!accepted && cont.isActive) {
            cont.resume(false)
        }
    }

    private fun getScreenDimensions(): Pair<Int, Int> {
        val wm = service.getSystemService(AccessibilityService.WINDOW_SERVICE) as? WindowManager
        return if (wm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val b = wm.currentWindowMetrics.bounds
                b.width() to b.height()
            } else {
                @Suppress("DEPRECATION")
                val metrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getRealMetrics(metrics)
                metrics.widthPixels to metrics.heightPixels
            }
        } else {
            1080 to 2400
        }
    }
}
