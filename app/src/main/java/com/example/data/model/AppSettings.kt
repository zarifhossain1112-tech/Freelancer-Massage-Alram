package com.example.data.model

import android.content.Context
import android.content.SharedPreferences

data class AppSettings(
    val masterEnabled: Boolean = true,
    val alarmSound: String = "SIREN", // SIREN, RADAR, PULSE, CHIME, SYSTEM_ALARM
    val alarmVolume: Int = 100, // 0 to 100
    val overrideSilentMode: Boolean = true,
    val vibrationPattern: String = "PULSE", // CONTINUOUS, PULSE, SOS, OFF
    val ringDurationSeconds: Int = 60, // 15, 30, 60, 120, 0 (continuous)
    val language: String = "bn" // "bn" or "en"
)

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gigalarm_prefs", Context.MODE_PRIVATE)

    fun getSettings(): AppSettings {
        return AppSettings(
            masterEnabled = prefs.getBoolean("master_enabled", true),
            alarmSound = prefs.getString("alarm_sound", "SIREN") ?: "SIREN",
            alarmVolume = prefs.getInt("alarm_volume", 100),
            overrideSilentMode = prefs.getBoolean("override_silent", true),
            vibrationPattern = prefs.getString("vibration_pattern", "PULSE") ?: "PULSE",
            ringDurationSeconds = prefs.getInt("ring_duration", 60),
            language = prefs.getString("language", "bn") ?: "bn"
        )
    }

    fun updateSettings(settings: AppSettings) {
        prefs.edit()
            .putBoolean("master_enabled", settings.masterEnabled)
            .putString("alarm_sound", settings.alarmSound)
            .putInt("alarm_volume", settings.alarmVolume)
            .putBoolean("override_silent", settings.overrideSilentMode)
            .putString("vibration_pattern", settings.vibrationPattern)
            .putInt("ring_duration", settings.ringDurationSeconds)
            .putString("language", settings.language)
            .apply()
    }
}
