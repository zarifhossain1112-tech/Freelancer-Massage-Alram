package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.db.GigAlarmDatabase
import com.example.data.util.PresetAppsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GigAlarmApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createChannels()
        initDefaultApps()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "gigalarm_active_alert_channel",
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableVibration(true)
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun initDefaultApps() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = GigAlarmDatabase.getDatabase(this@GigAlarmApp).monitoredAppDao()
            if (dao.getCount() == 0) {
                dao.insertAll(PresetAppsData.getDefaultMonitoredApps())
            }
        }
    }
}
