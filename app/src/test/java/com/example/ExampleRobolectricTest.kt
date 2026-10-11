package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppSettings
import com.example.data.model.CustomSound
import com.example.data.model.MonitoredApp
import com.example.data.model.PredefinedSoundsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GigAlarm", appName)
    }

    @Test
    fun `test default app settings and monitored app models`() {
        val defaultSettings = AppSettings()
        assertTrue(defaultSettings.masterEnabled)
        assertEquals(100, defaultSettings.alarmVolume)
        assertEquals("SIREN", defaultSettings.alarmSound)
        assertEquals("PULSE", defaultSettings.vibrationPattern)

        val app = MonitoredApp(
            packageName = "com.fiverr.fiverr",
            appName = "Fiverr",
            isEnabled = true,
            keywords = "order, message",
            customVolume = 90,
            customDurationSeconds = 60,
            customVibration = "RAPID"
        )
        assertNotNull(app)
        assertEquals("com.fiverr.fiverr", app.packageName)
        assertEquals(90, app.customVolume)
        assertEquals(60, app.customDurationSeconds)
        assertEquals("RAPID", app.customVibration)
    }

    @Test
    fun `test predefined sounds and custom sound model`() {
        assertTrue(PredefinedSoundsData.SOUNDS.isNotEmpty())
        val siren = PredefinedSoundsData.SOUNDS.first { it.key == "SIREN" }
        assertNotNull(siren)

        val customSound = CustomSound(
            id = "test-uuid-123",
            name = "My Custom Sound",
            filePath = "/data/data/com.example/files/alarm_sounds/sound_123.mp3",
            fileSize = 1024L
        )
        assertEquals("test-uuid-123", customSound.id)
        assertEquals("My Custom Sound", customSound.name)
    }
}
