package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CustomSound
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSoundDao {
    @Query("SELECT * FROM custom_sounds ORDER BY createdAt DESC")
    fun getAllCustomSounds(): Flow<List<CustomSound>>

    @Query("SELECT * FROM custom_sounds WHERE id = :id LIMIT 1")
    suspend fun getSoundById(id: String): CustomSound?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sound: CustomSound)

    @Delete
    suspend fun delete(sound: CustomSound)

    @Query("SELECT COUNT(*) FROM custom_sounds")
    suspend fun getCount(): Int
}
