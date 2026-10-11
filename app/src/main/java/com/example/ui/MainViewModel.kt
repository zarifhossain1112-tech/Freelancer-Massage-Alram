package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.GigAlarmDatabase
import com.example.data.model.AppSettings
import com.example.data.model.AppSettingsManager
import com.example.data.model.CustomSound
import com.example.data.model.MonitoredApp
import com.example.data.model.NotificationLog
import com.example.data.util.AppScannerHelper
import com.example.data.util.CustomSoundManager
import com.example.data.util.InstalledAppItem
import com.example.service.ActiveAlarmData
import com.example.service.AlarmSoundPlayer
import com.example.service.NotificationPermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val database: GigAlarmDatabase,
    private val settingsManager: AppSettingsManager
) : ViewModel() {

    val monitoredApps: StateFlow<List<MonitoredApp>> = database.monitoredAppDao()
        .getAllApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationLogs: StateFlow<List<NotificationLog>> = database.notificationLogDao()
        .getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customSounds: StateFlow<List<CustomSound>> = database.customSoundDao()
        .getAllCustomSounds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _appSettings = MutableStateFlow(settingsManager.getSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    val isRinging: StateFlow<Boolean> = AlarmSoundPlayer.isRinging

    fun checkPermission(context: Context) {
        _isPermissionGranted.value = NotificationPermissionHelper.isNotificationAccessGranted(context)
    }

    fun toggleMaster(enabled: Boolean) {
        val updated = _appSettings.value.copy(masterEnabled = enabled)
        _appSettings.value = updated
        settingsManager.updateSettings(updated)
    }

    fun toggleApp(pkg: String, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            database.monitoredAppDao().setEnabled(pkg, isEnabled)
        }
    }

    fun updateApp(app: MonitoredApp) {
        viewModelScope.launch(Dispatchers.IO) {
            database.monitoredAppDao().update(app)
        }
    }

    fun updateKeywords(pkg: String, keywords: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = database.monitoredAppDao().getAppByPackage(pkg)
            if (app != null) {
                database.monitoredAppDao().update(app.copy(keywords = keywords))
            }
        }
    }

    fun addApp(
        pkg: String,
        appName: String,
        keywords: String = "",
        customSound: String = "DEFAULT",
        customVolume: Int = -1,
        customDurationSeconds: Int = -1,
        customVibration: String = "DEFAULT"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            database.monitoredAppDao().insert(
                MonitoredApp(
                    packageName = pkg,
                    appName = appName,
                    isEnabled = true,
                    keywords = keywords,
                    isPreset = false,
                    customSound = customSound,
                    customVolume = customVolume,
                    customDurationSeconds = customDurationSeconds,
                    customVibration = customVibration
                )
            )
        }
    }

    fun deleteApp(app: MonitoredApp) {
        viewModelScope.launch(Dispatchers.IO) {
            database.monitoredAppDao().delete(app)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        _appSettings.value = newSettings
        settingsManager.updateSettings(newSettings)
    }

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            val currentMonitored = monitoredApps.value.map { it.packageName }.toSet()
            val apps = AppScannerHelper.getInstalledLauncherApps(context, currentMonitored)
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun importCustomAudio(context: Context, uri: Uri, name: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val sound = CustomSoundManager.importAudioFile(context, uri, name)
            if (sound != null) {
                // Auto select as alarm sound if user uploaded it
                updateSettings(_appSettings.value.copy(alarmSound = "CUSTOM:${sound.id}"))
            }
        }
    }

    fun deleteCustomSound(context: Context, sound: CustomSound) {
        viewModelScope.launch(Dispatchers.IO) {
            CustomSoundManager.deleteCustomSound(context, sound)
            if (_appSettings.value.alarmSound == "CUSTOM:${sound.id}") {
                updateSettings(_appSettings.value.copy(alarmSound = "SIREN"))
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            database.notificationLogDao().clearAll()
        }
    }

    fun triggerTestAlarm(
        context: Context,
        app: MonitoredApp? = null
    ) {
        val appName = app?.appName ?: "Fiverr"
        val pkgName = app?.packageName ?: "com.fiverr.fiverr"
        val title = "নতুন অর্ডার: $500 ডলারের প্রজেক্ট!"
        val message = "বায়ার @client_steve আপনাকে একটি নতুন মেসেজ দিয়েছেন।"

        val alarmData = ActiveAlarmData(
            packageName = pkgName,
            appName = appName,
            title = title,
            message = message
        )
        AlarmSoundPlayer.startAlarm(
            context = context,
            alarmData = alarmData,
            settings = _appSettings.value,
            toneOverride = if (app?.customSound != null && app.customSound != "DEFAULT") app.customSound else null,
            volumeOverride = if (app != null && app.customVolume > 0) app.customVolume else null,
            durationOverride = if (app != null && app.customDurationSeconds >= 0) app.customDurationSeconds else null,
            vibrationOverride = if (app != null && app.customVibration != "DEFAULT") app.customVibration else null
        )
    }

    fun testVibration(context: Context, pattern: String) {
        AlarmSoundPlayer.testVibrationPattern(context, pattern)
    }

    fun stopAlarm(context: Context) {
        AlarmSoundPlayer.stopAlarm(context)
    }
}
