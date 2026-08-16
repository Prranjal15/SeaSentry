package com.seasentry.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SOSEvent::class,
        AlertEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // DAOs will be added here in subsequent modules
}
