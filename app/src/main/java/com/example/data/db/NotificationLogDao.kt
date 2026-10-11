package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: NotificationLog): Long

    @Query("DELETE FROM notification_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM notification_logs WHERE alarmTriggered = 1")
    suspend fun getTriggeredCount(): Int
}
