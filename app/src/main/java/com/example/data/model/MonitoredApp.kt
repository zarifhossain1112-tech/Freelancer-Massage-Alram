package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_apps")
data class MonitoredApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val keywords: String = "", // comma-separated, empty means any notification triggers alarm
    val isPreset: Boolean = false,
    val customSound: String = "DEFAULT", // "DEFAULT", "SIREN", "RADAR", "PULSE", "CHIME", "SYSTEM_ALARM"
    val customVolume: Int = -1, // -1 means follow global setting, or 10..100% intensity
    val customDurationSeconds: Int = -1, // -1 means follow global setting, or 15..300 seconds (0 for infinite)
    val customVibration: String = "DEFAULT", // "DEFAULT", "PULSE", "RAPID", "CONTINUOUS", "SOS", "HEARTBEAT", "OFF"
    val lastTriggeredAt: Long = 0L
)
