package com.example.service

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.example.R
import com.example.model.OverlayMode
import com.example.model.OverlaySettings
import com.example.model.OverlayStyle
import kotlin.math.abs

class OverlayWindowController(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onTriggerScrollTop: () -> Unit,
    private val onLongPressOpenApp: () -> Unit,
    private val onButtonPositionSaved: (Int, Int) -> Unit
) {
    private var floatingContainer: FrameLayout? = null
    private var floatingParams: WindowManager.LayoutParams? = null
    private var floatingGlyphView: ScrollTopGlyphView? = null

    private var topBarContainer: FrameLayout? = null
    private var topBarParams: WindowManager.LayoutParams? = null
    private var topBarGlyphView: TopBarPillView? = null

    private var currentSettings: OverlaySettings = OverlaySettings()
    private var isScrollingAnimActive: Boolean = false
    private var edgeAnimator: ValueAnimator? = null

    fun applySettings(settings: OverlaySettings) {
        currentSettings = settings
        if (!settings.overlayEnabled) {
            removeAllOverlays()
            return
        }

        val showButton = settings.overlayMode == OverlayMode.FLOATING_BUTTON ||
            settings.overlayMode == OverlayMode.BOTH
        val showTopBar = settings.overlayMode == OverlayMode.TOP_BAR ||
            settings.overlayMode == OverlayMode.BOTH

        if (showButton) {
            ensureFloatingButton(settings)
        } else {
            removeFloatingButton()
        }

        if (showTopBar) {
            ensureTopBar(settings)
        } else {
            removeTopBar()
        }
    }

    fun setScrollingState(scrolling: Boolean) {
        isScrollingAnimActive = scrolling
        floatingGlyphView?.setActiveScrolling(scrolling)
        topBarGlyphView?.setActiveScrolling(scrolling)
    }

    fun removeAllOverlays() {
        removeFloatingButton()
        removeTopBar()
    }

    private fun ensureFloatingButton(settings: OverlaySettings) {
        val sizePx = dpToPx(settings.buttonSize.dpValue)
        val (screenW, screenH) = getScreenSize()

        if (floatingContainer == null) {
            val container = FrameLayout(context).apply {
                contentDescription = context.getString(R.string.cd_floating_overlay)
                elevation = dpToPx(8).toFloat()
            }
            val glyph = ScrollTopGlyphView(context)
            container.addView(
                glyph,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            val initialX = if (settings.savedButtonX >= 0) {
                settings.savedButtonX.coerceIn(0, (screenW - sizePx).coerceAtLeast(0))
            } else {
                (screenW - sizePx - dpToPx(16)).coerceAtLeast(0)
            }
            val initialY = if (settings.savedButtonY >= 0) {
                settings.savedButtonY.coerceIn(dpToPx(48), (screenH - sizePx - dpToPx(48)).coerceAtLeast(dpToPx(48)))
            } else {
                (screenH * 0.62f).toInt()
            }

            val params = WindowManager.LayoutParams(
                sizePx,
                sizePx,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = initialX
                y = initialY
            }

            attachFloatingTouchListener(container, params)

            try {
                windowManager.addView(container, params)
                floatingContainer = container
                floatingParams = params
                floatingGlyphView = glyph
            } catch (_: Exception) {
                return
            }
        } else {
            val params = floatingParams ?: return
            params.width = sizePx
            params.height = sizePx
            params.x = params.x.coerceIn(0, (screenW - sizePx).coerceAtLeast(0))
            params.y = params.y.coerceIn(dpToPx(24), (screenH - sizePx - dpToPx(24)).coerceAtLeast(dpToPx(24)))
            try {
                windowManager.updateViewLayout(floatingContainer, params)
            } catch (_: Exception) {
            }
        }

        styleFloatingButton(settings)
    }

    private fun styleFloatingButton(settings: OverlaySettings) {
        val container = floatingContainer ?: return
        val glyph = floatingGlyphView ?: return
        val (bgColor, strokeColor, fgColor) = resolvePalette(settings.style)

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(bgColor)
            setStroke(dpToPx(1).coerceAtLeast(2), strokeColor)
        }
        container.background = bg
        container.alpha = settings.opacity
        glyph.setForegroundColor(fgColor)
    }

    private fun ensureTopBar(settings: OverlaySettings) {
        val widthPx = dpToPx(settings.buttonSize.topBarWidthDp)
        val heightPx = dpToPx(settings.buttonSize.topBarHeightDp)

        if (topBarContainer == null) {
            val container = FrameLayout(context).apply {
                contentDescription = context.getString(R.string.cd_top_bar_overlay)
            }
            val pill = TopBarPillView(context)
            val pillParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(4)
                leftMargin = dpToPx(6)
                rightMargin = dpToPx(6)
            }
            container.addView(pill, pillParams)

            val params = WindowManager.LayoutParams(
                widthPx,
                heightPx,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                x = 0
                y = dpToPx(2)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            container.setOnClickListener {
                if (currentSettings.hapticFeedback) {
                    container.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                animateTapPulse(container)
                onTriggerScrollTop()
            }
            container.setOnLongClickListener {
                if (currentSettings.hapticFeedback) {
                    container.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
                onLongPressOpenApp()
                true
            }

            try {
                windowManager.addView(container, params)
                topBarContainer = container
                topBarParams = params
                topBarGlyphView = pill
            } catch (_: Exception) {
                return
            }
        } else {
            val params = topBarParams ?: return
            params.width = widthPx
            params.height = heightPx
            try {
                windowManager.updateViewLayout(topBarContainer, params)
            } catch (_: Exception) {
            }
        }

        styleTopBar(settings)
    }

    private fun styleTopBar(settings: OverlaySettings) {
        val container = topBarContainer ?: return
        val pill = topBarGlyphView ?: return
        val (bgColor, strokeColor, fgColor) = resolvePalette(settings.style)
        pill.setColors(bgColor, strokeColor, fgColor)
        container.alpha = settings.opacity
    }

    private fun attachFloatingTouchListener(view: View, params: WindowManager.LayoutParams) {
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        var longPressTriggered = false

        val longPressRunnable = Runnable {
            if (!isDragging) {
                longPressTriggered = true
                if (currentSettings.hapticFeedback) {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
                onLongPressOpenApp()
            }
        }

        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    edgeAnimator?.cancel()
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    longPressTriggered = false
                    v.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(90L).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                        isDragging = true
                        v.removeCallbacks(longPressRunnable)
                    }
                    if (isDragging) {
                        val (screenW, screenH) = getScreenSize()
                        params.x = (initialX + dx.toInt()).coerceIn(0, (screenW - v.width).coerceAtLeast(0))
                        params.y = (initialY + dy.toInt()).coerceIn(dpToPx(20), (screenH - v.height - dpToPx(20)).coerceAtLeast(dpToPx(20)))
                        try {
                            windowManager.updateViewLayout(v, params)
                        } catch (_: Exception) {
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.removeCallbacks(longPressRunnable)
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
                    if (isDragging) {
                        if (currentSettings.snapToEdge) {
                            snapButtonToNearestEdge(v, params)
                        } else {
                            onButtonPositionSaved(params.x, params.y)
                        }
                    } else if (!longPressTriggered && event.actionMasked == MotionEvent.ACTION_UP) {
                        if (currentSettings.hapticFeedback) {
                            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        animateTapPulse(v)
                        onTriggerScrollTop()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snapButtonToNearestEdge(view: View, params: WindowManager.LayoutParams) {
        val (screenW, _) = getScreenSize()
        val margin = dpToPx(12)
        val center = params.x + view.width / 2
        val targetX = if (center < screenW / 2) {
            margin
        } else {
            (screenW - view.width - margin).coerceAtLeast(margin)
        }

        edgeAnimator?.cancel()
        edgeAnimator = ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                params.x = anim.animatedValue as Int
                try {
                    if (floatingContainer != null) {
                        windowManager.updateViewLayout(view, params)
                    }
                } catch (_: Exception) {
                }
            }
            start()
        }
        onButtonPositionSaved(targetX, params.y)
    }

    private fun animateTapPulse(view: View) {
        view.animate()
            .scaleX(1.12f)
            .scaleY(1.12f)
            .setDuration(100L)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(150L).start()
            }
            .start()
    }

    private fun removeFloatingButton() {
        edgeAnimator?.cancel()
        floatingContainer?.let { view ->
            try {
                windowManager.removeViewImmediate(view)
            } catch (_: Exception) {
            }
        }
        floatingContainer = null
        floatingParams = null
        floatingGlyphView = null
    }

    private fun removeTopBar() {
        topBarContainer?.let { view ->
            try {
                windowManager.removeViewImmediate(view)
            } catch (_: Exception) {
            }
        }
        topBarContainer = null
        topBarParams = null
        topBarGlyphView = null
    }

    private fun resolvePalette(style: OverlayStyle): Triple<Int, Int, Int> {
        return when (style) {
            OverlayStyle.COBALT -> Triple(
                Color.parseColor("#E61D4ED8"),
                Color.parseColor("#6693C5FD"),
                Color.WHITE
            )
            OverlayStyle.OBSIDIAN -> Triple(
                Color.parseColor("#EB0F172A"),
                Color.parseColor("#5594A3B8"),
                Color.parseColor("#F8FAFC")
            )
            OverlayStyle.GLASS -> Triple(
                Color.parseColor("#CCFFFFFF"),
                Color.parseColor("#88CBD5E1"),
                Color.parseColor("#0F172A")
            )
            OverlayStyle.MINIMAL -> Triple(
                Color.parseColor("#330F172A"),
                Color.parseColor("#44FFFFFF"),
                Color.parseColor("#CCFFFFFF")
            )
        }
    }

    private fun getScreenSize(): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val b = windowManager.currentWindowMetrics.bounds
            b.width() to b.height()
        } else {
            @Suppress("DEPRECATION")
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            metrics.widthPixels to metrics.heightPixels
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}

private class ScrollTopGlyphView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
    }
    private val arrowPath = Path()
    private var activeScrolling = false

    fun setForegroundColor(color: Int) {
        paint.color = color
        ringPaint.color = Color.argb(120, Color.red(color), Color.green(color), Color.blue(color))
        invalidate()
    }

    fun setActiveScrolling(active: Boolean) {
        activeScrolling = active
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val stroke = (w * 0.085f).coerceAtLeast(4f)
        paint.strokeWidth = stroke

        // Top horizontal bar ("to top" ceiling line)
        val barY = h * 0.30f
        val barHalfW = w * 0.20f
        canvas.drawLine(w / 2f - barHalfW, barY, w / 2f + barHalfW, barY, paint)

        // Upward chevron + shaft
        val arrowTopY = h * 0.43f
        val arrowBottomY = h * 0.72f
        val chevronWing = w * 0.17f

        arrowPath.reset()
        arrowPath.moveTo(w / 2f - chevronWing, arrowTopY + chevronWing * 0.95f)
        arrowPath.lineTo(w / 2f, arrowTopY)
        arrowPath.lineTo(w / 2f + chevronWing, arrowTopY + chevronWing * 0.95f)
        canvas.drawPath(arrowPath, paint)
        canvas.drawLine(w / 2f, arrowTopY + stroke * 0.25f, w / 2f, arrowBottomY, paint)

        if (activeScrolling) {
            ringPaint.strokeWidth = stroke * 0.7f
            val inset = stroke * 1.4f
            canvas.drawArc(
                RectF(inset, inset, w - inset, h - inset),
                -90f,
                270f,
                false,
                ringPaint
            )
        }
    }
}

private class TopBarPillView(context: Context) : View(context) {
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val rect = RectF()
    private val chevronPath = Path()
    private var activeScrolling = false

    fun setColors(bgColor: Int, strokeColor: Int, fgColor: Int) {
        bgPaint.color = bgColor
        strokePaint.color = strokeColor
        glyphPaint.color = fgColor
        invalidate()
    }

    fun setActiveScrolling(active: Boolean) {
        activeScrolling = active
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val strokeW = 2.5f
        strokePaint.strokeWidth = strokeW
        rect.set(strokeW, strokeW, w - strokeW, h - strokeW)
        val radius = h / 2f
        canvas.drawRoundRect(rect, radius, radius, bgPaint)
        canvas.drawRoundRect(rect, radius, radius, strokePaint)

        // Centered compact "to-top" chevron + top bar
        val glyphStroke = (h * 0.14f).coerceAtLeast(3f)
        glyphPaint.strokeWidth = glyphStroke
        val cx = w / 2f
        val barY = h * 0.34f
        val barHalf = h * 0.28f
        canvas.drawLine(cx - barHalf, barY, cx + barHalf, barY, glyphPaint)

        val peakY = h * 0.48f
        val wingX = h * 0.26f
        val wingY = h * 0.72f
        chevronPath.reset()
        chevronPath.moveTo(cx - wingX, wingY)
        chevronPath.lineTo(cx, peakY)
        chevronPath.lineTo(cx + wingX, wingY)
        canvas.drawPath(chevronPath, glyphPaint)

        if (activeScrolling) {
            val dotRadius = h * 0.12f
            val prevStyle = glyphPaint.style
            glyphPaint.style = Paint.Style.FILL
            canvas.drawCircle(cx + h * 0.85f, h / 2f, dotRadius, glyphPaint)
            canvas.drawCircle(cx - h * 0.85f, h / 2f, dotRadius, glyphPaint)
            glyphPaint.style = prevStyle
        }
    }
}
