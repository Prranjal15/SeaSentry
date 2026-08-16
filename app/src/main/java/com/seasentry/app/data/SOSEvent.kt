package com.seasentry.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sos_events")
data class SOSEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val message: String,
    val hopCount: Int,
    val status: String
)
