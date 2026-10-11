package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MonitoredApp
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredAppDao {
    @Query("SELECT * FROM monitored_apps ORDER BY isEnabled DESC, appName ASC")
    fun getAllApps(): Flow<List<MonitoredApp>>

    @Query("SELECT * FROM monitored_apps")
    suspend fun getAllAppsSync(): List<MonitoredApp>

    @Query("SELECT * FROM monitored_apps WHERE packageName = :pkg LIMIT 1")
    suspend fun getAppByPackage(pkg: String): MonitoredApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: MonitoredApp)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(apps: List<MonitoredApp>)

    @Update
    suspend fun update(app: MonitoredApp)

    @Delete
    suspend fun delete(app: MonitoredApp)

    @Query("UPDATE monitored_apps SET isEnabled = :enabled WHERE packageName = :pkg")
    suspend fun setEnabled(pkg: String, enabled: Boolean)

    @Query("UPDATE monitored_apps SET lastTriggeredAt = :time WHERE packageName = :pkg")
    suspend fun updateLastTriggered(pkg: String, time: Long)

    @Query("SELECT COUNT(*) FROM monitored_apps")
    suspend fun getCount(): Int
}
