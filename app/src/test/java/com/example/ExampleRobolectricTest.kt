package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.OverlayPreferencesRepository
import com.example.model.ButtonSize
import com.example.model.OverlayMode
import com.example.model.OverlayStyle
import com.example.model.ScrollMethod
import com.example.model.ScrollSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Наверх", appName)
  }

  @Test
  fun `overlay preferences repository persists updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = OverlayPreferencesRepository(context)

    repo.setOverlayEnabled(false)
    repo.setOverlayMode(OverlayMode.BOTH)
    repo.setButtonSize(ButtonSize.LARGE)
    repo.setOpacity(0.55f)
    repo.setStyle(OverlayStyle.OBSIDIAN)
    repo.setScrollMethod(ScrollMethod.GESTURE)
    repo.setScrollSpeed(ScrollSpeed.DEEP)

    val updated = repo.settings.value
    assertFalse(updated.overlayEnabled)
    assertEquals(OverlayMode.BOTH, updated.overlayMode)
    assertEquals(ButtonSize.LARGE, updated.buttonSize)
    assertEquals(0.55f, updated.opacity, 0.01f)
    assertEquals(OverlayStyle.OBSIDIAN, updated.style)
    assertEquals(ScrollMethod.GESTURE, updated.scrollMethod)
    assertEquals(ScrollSpeed.DEEP, updated.scrollSpeed)

    repo.cleanup()
  }
}
