package com.seasentry.app

import com.seasentry.app.data.AlertEvent
import com.seasentry.app.data.SOSEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EntityTest {

    @Test
    fun testSOSEventCreation() {
        val now = System.currentTimeMillis()
        val sos = SOSEvent(
            senderId = "vessel-42",
            latitude = 18.9220,
            longitude = 72.8347,
            timestamp = now,
            message = "Engine failure near lighthouse",
            hopCount = 0,
            status = "PENDING"
        )

        assertNotNull(sos.id)
        assertEquals("vessel-42", sos.senderId)
        assertEquals(18.9220, sos.latitude, 0.0001)
        assertEquals(72.8347, sos.longitude, 0.0001)
        assertEquals(now, sos.timestamp)
        assertEquals("Engine failure near lighthouse", sos.message)
        assertEquals(0, sos.hopCount)
        assertEquals("PENDING", sos.status)
    }

    @Test
    fun testAlertEventCreation() {
        val now = System.currentTimeMillis()
        val alert = AlertEvent(
            tier = "CRITICAL",
            latitude = 18.9100,
            longitude = 72.8200,
            timestamp = now
        )

        assertNotNull(alert.id)
        assertEquals("CRITICAL", alert.tier)
        assertEquals(18.9100, alert.latitude, 0.0001)
        assertEquals(72.8200, alert.longitude, 0.0001)
        assertEquals(now, alert.timestamp)
    }
}
