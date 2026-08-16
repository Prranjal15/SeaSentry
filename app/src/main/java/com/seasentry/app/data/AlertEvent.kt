package com.seasentry.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "alert_events")
data class AlertEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val tier: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
