package com.seasentry.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_events ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEvent>>

    @Query("SELECT * FROM alert_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentAlerts(limit: Int): List<AlertEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEvent>): List<Long>

    @Query("DELETE FROM alert_events")
    suspend fun clearAllAlerts(): Int

    @Query("SELECT COUNT(*) FROM alert_events")
    suspend fun getAlertCount(): Int
}
