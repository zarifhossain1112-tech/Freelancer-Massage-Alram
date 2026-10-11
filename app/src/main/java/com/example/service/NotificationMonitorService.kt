package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.db.GigAlarmDatabase
import com.example.data.model.AppSettingsManager
import com.example.data.model.NotificationLog
import com.example.ui.alarm.AlarmAlertActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationMonitorService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        const val CHANNEL_ID = "gigalarm_active_alert_channel"
        const val NOTIFICATION_ID = 9911
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return

        // Ignore our own notifications
        if (pkgName == packageName) return

        // Ignore ongoing notifications like media players or permanent service badges
        if (sbn.isOngoing) return

        scope.launch {
            processIncomingNotification(sbn)
        }
    }

    private suspend fun processIncomingNotification(sbn: StatusBarNotification) {
        val context = applicationContext
        val pkgName = sbn.packageName
        val db = GigAlarmDatabase.getDatabase(context)
        val settingsManager = AppSettingsManager(context)
        val settings = settingsManager.getSettings()

        // Check if master alarm is turned on
        if (!settings.masterEnabled) return

        // Check if this package is monitored
        val monitoredApp = db.monitoredAppDao().getAppByPackage(pkgName) ?: return

        // If app monitoring is toggled off, ignore
        if (!monitoredApp.isEnabled) return

        // Extract Title and Text
        val extras: Bundle? = sbn.notification.extras
        val title = extras?.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
            ?: extras?.getString(android.app.Notification.EXTRA_TITLE)
            ?: monitoredApp.appName
        val text = extras?.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras?.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
            ?: ""

        val combinedContent = "$title $text".lowercase()

        // Evaluate keywords
        val keywordsList = monitoredApp.keywords
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        val isMatched: Boolean
        val matchReason: String

        if (keywordsList.isEmpty()) {
            isMatched = true
            matchReason = "All notifications mode"
        } else {
            val matchedKeyword = keywordsList.firstOrNull { kw -> combinedContent.contains(kw) }
            if (matchedKeyword != null) {
                isMatched = true
                matchReason = "Matched keyword: $matchedKeyword"
            } else {
                isMatched = false
                matchReason = "No keyword matched (${keywordsList.joinToString(", ")})"
            }
        }

        // Record to DB
        db.notificationLogDao().insert(
            NotificationLog(
                packageName = pkgName,
                appName = monitoredApp.appName,
                title = title,
                text = text,
                timestamp = System.currentTimeMillis(),
                alarmTriggered = isMatched,
                triggerReason = matchReason
            )
        )

        if (isMatched) {
            // Update last triggered timestamp
            db.monitoredAppDao().updateLastTriggered(pkgName, System.currentTimeMillis())

            // Start Loud Alarm
            val alarmData = ActiveAlarmData(
                packageName = pkgName,
                appName = monitoredApp.appName,
                title = title,
                message = text
            )

            AlarmSoundPlayer.startAlarm(
                context = context,
                alarmData = alarmData,
                settings = settings,
                toneOverride = monitoredApp.customSound,
                volumeOverride = if (monitoredApp.customVolume > 0) monitoredApp.customVolume else null,
                durationOverride = if (monitoredApp.customDurationSeconds >= 0) monitoredApp.customDurationSeconds else null,
                vibrationOverride = if (monitoredApp.customVibration != "DEFAULT") monitoredApp.customVibration else null
            )

            // Trigger Full Screen Alert Activity
            triggerAlarmAlert(context, alarmData)
        }
    }

    private fun triggerAlarmAlert(context: Context, alarmData: ActiveAlarmData) {
        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra(AlarmAlertActivity.EXTRA_PACKAGE_NAME, alarmData.packageName)
            putExtra(AlarmAlertActivity.EXTRA_APP_NAME, alarmData.appName)
            putExtra(AlarmAlertActivity.EXTRA_TITLE, alarmData.title)
            putExtra(AlarmAlertActivity.EXTRA_MESSAGE, alarmData.message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 ${alarmData.appName} Alert!")
            .setContentText(alarmData.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${alarmData.title}\n${alarmData.message}"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Launch Activity directly as well
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
