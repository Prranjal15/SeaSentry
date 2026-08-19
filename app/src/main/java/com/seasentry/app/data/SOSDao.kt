package com.seasentry.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SOSDao {
    @Query("SELECT * FROM sos_events ORDER BY timestamp DESC")
    fun getAllSOS(): Flow<List<SOSEvent>>

    @Query("SELECT * FROM sos_events WHERE id = :id LIMIT 1")
    suspend fun getSOSById(id: String): SOSEvent?

    @Query("SELECT * FROM sos_events WHERE status != 'RESOLVED' ORDER BY timestamp DESC")
    fun getActiveSOSEvents(): Flow<List<SOSEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSOS(sos: SOSEvent): Long

    @Query("UPDATE sos_events SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String): Int

    @Query("DELETE FROM sos_events")
    suspend fun clearAllSOS(): Int
}
