package com.seasentry.app

import com.seasentry.app.navarea.NavAreaRepository
import com.seasentry.app.navarea.NavigationalWarning
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NavAreaRepositoryTest {

    private lateinit var repository: NavAreaRepository

    @Before
    fun setUp() {
        repository = NavAreaRepository()
    }

    @Test
    fun testInitialStateIsEmpty() {
        val initialWarnings = repository.warnings.value
        assertTrue("NavAreaRepository warnings should start empty", initialWarnings.isEmpty())
    }

    @Test
    fun testInjectSimulatedWarningAddsRealisticWarning() {
        repository.injectSimulatedWarning()

        val warnings = repository.warnings.value
        assertEquals(1, warnings.size)

        val warning = warnings.first()
        assertNotNull(warning.id)
        assertTrue(warning.title.isNotBlank())
        assertTrue(warning.text.isNotBlank())
        assertTrue(warning.area.contains("NAVAREA VIII"))
        assertEquals("NAVAREA VIII Coordinator - India", warning.authority)
        assertTrue("Timestamp should be populated", warning.issuedAt > 0)

        // Lat/Lon should be in vicinity of the Indian Coast / Mumbai navigation corridor
        assertTrue("Latitude should be between 18.0 and 20.0", warning.latitude in 18.0..20.0)
        assertTrue("Longitude should be between 72.0 and 74.0", warning.longitude in 72.0..74.0)
    }

    @Test
    fun testMultipleInjectionsAndClear() {
        repository.injectSimulatedWarning()
        repository.injectSimulatedWarning()

        val list = repository.warnings.value
        assertEquals(2, list.size)

        val firstId = list[0].id
        repository.removeWarning(firstId)
        assertEquals(1, repository.warnings.value.size)

        repository.clearWarnings()
        assertTrue(repository.warnings.value.isEmpty())
    }

    @Test
    fun testNavigationalWarningModel() {
        val now = System.currentTimeMillis()
        val model = NavigationalWarning(
            id = "NAV-TEST-01",
            title = "Debris Notice",
            text = "Floating net hazard",
            area = "NAVAREA VIII",
            latitude = 18.93,
            longitude = 72.83,
            authority = "NAVAREA VIII Coordinator - India",
            issuedAt = now
        )

        assertEquals("NAV-TEST-01", model.id)
        assertEquals("Debris Notice", model.title)
        assertEquals("Floating net hazard", model.text)
        assertEquals("NAVAREA VIII", model.area)
        assertEquals(18.93, model.latitude, 0.001)
        assertEquals(72.83, model.longitude, 0.001)
        assertEquals("NAVAREA VIII Coordinator - India", model.authority)
        assertEquals(now, model.issuedAt)
    }
}
