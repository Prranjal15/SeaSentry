package com.seasentry.app

import com.seasentry.app.boundary.BoundaryRepository
import com.seasentry.app.boundary.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BoundaryRepositoryTest {

    @Test
    fun testParseActualIndiaBoundariesAssetFile() {
        val assetFile = File("src/main/assets/india_boundaries.geojson")
        assertTrue("Asset file must exist", assetFile.exists())

        val geoJsonString = assetFile.readText()
        val repository = BoundaryRepository()
        val features = repository.parseFeatures(geoJsonString)
        val polylines = repository.parseGeoJson(geoJsonString)

        // Verify total features after filtering out Maldives (original has 18, 2 are Maldives)
        assertEquals(16, features.size)

        // Ensure NO feature contains Maldives
        for (feature in features) {
            val name = feature.properties.lineName ?: ""
            assertFalse("Should not contain Maldives: $name", name.contains("Maldives", ignoreCase = true))
        }

        // Verify polylines are extracted
        assertTrue("Should have polylines", polylines.isNotEmpty())

        // Verify total points count across all polylines (29,091 points)
        val totalPoints = polylines.sumOf { it.size }
        assertEquals(29091, totalPoints)

        // Verify coordinate bounds are realistic for Indian maritime boundaries
        for (line in polylines) {
            for (pt in line) {
                // Latitudes in the Indian Ocean / Arabian Sea / Bay of Bengal region: ~4°N to ~25°N
                assertTrue("Latitude ${pt.latitude} out of expected range", pt.latitude in 0.0..35.0)
                // Longitudes: ~60°E to ~100°E
                assertTrue("Longitude ${pt.longitude} out of expected range", pt.longitude in 50.0..105.0)
            }
        }
    }
}
