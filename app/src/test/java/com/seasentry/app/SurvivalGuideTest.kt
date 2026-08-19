package com.seasentry.app

import com.seasentry.app.survival.SurvivalCategory
import com.seasentry.app.survival.SurvivalGuideRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SurvivalGuideTest {

    @Test
    fun testSurvivalGuidesAvailabilityOffline() {
        val guides = SurvivalGuideRepository.GUIDES
        assertEquals(8, guides.size)

        val imblGuide = guides.find { it.id == "guide-imbl" }
        assertNotNull(imblGuide)
        assertEquals("CRITICAL", imblGuide?.urgency)
        assertTrue(imblGuide?.actionChecklist?.isNotEmpty() == true)

        val engineGuide = guides.find { it.id == "guide-engine" }
        assertNotNull(engineGuide)
        assertEquals(SurvivalCategory.PROPULSION, engineGuide?.category)
    }

    @Test
    fun testSurvivalGuideSearch() {
        val mobResults = SurvivalGuideRepository.searchGuides("Overboard")
        assertEquals(1, mobResults.size)
        assertEquals("guide-mob", mobResults.first().id)

        val emptyResults = SurvivalGuideRepository.searchGuides("NonExistentEmergencyTermXYZ")
        assertTrue(emptyResults.isEmpty())
    }

    @Test
    fun testSurvivalGuideCategoryFiltering() {
        val emergencyGuides = SurvivalGuideRepository.getGuidesByCategory(SurvivalCategory.EMERGENCY)
        assertTrue(emergencyGuides.isNotEmpty())
        assertTrue(emergencyGuides.all { it.category == SurvivalCategory.EMERGENCY })
    }
}
