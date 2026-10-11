package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CustomSound
import com.example.data.model.MonitoredApp
import com.example.data.model.NotificationLog

@Database(
    entities = [MonitoredApp::class, NotificationLog::class, CustomSound::class],
    version = 3,
    exportSchema = false
)
abstract class GigAlarmDatabase : RoomDatabase() {
    abstract fun monitoredAppDao(): MonitoredAppDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun customSoundDao(): CustomSoundDao

    companion object {
        @Volatile
        private var INSTANCE: GigAlarmDatabase? = null

        fun getDatabase(context: Context): GigAlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GigAlarmDatabase::class.java,
                    "gigalarm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
