package com.seasentry.app.boundary

import android.content.Context
import kotlinx.serialization.json.Json

class BoundaryRepository(
    private val context: Context? = null,
    private val json: Json = defaultJson
) {
    companion object {
        const val DEFAULT_ASSET_NAME = "india_boundaries.geojson"

        val defaultJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    private var cachedBoundaries: List<List<LatLng>>? = null

    /**
     * Parses a GeoJSON string into boundary polylines (List of List<LatLng>),
     * filtering out features whose line_name contains "Maldives".
     */
    fun parseGeoJson(geoJsonString: String): List<List<LatLng>> {
        val featureCollection = json.decodeFromString<FeatureCollection>(geoJsonString)
        return featureCollection.features
            .filter { feature ->
                val lineName = feature.properties.lineName
                lineName == null || !lineName.contains("Maldives", ignoreCase = true)
            }
            .flatMap { it.geometry.toLatLngLines() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Parses a GeoJSON string into a List of Feature objects,
     * filtering out features whose line_name contains "Maldives".
     */
    fun parseFeatures(geoJsonString: String): List<Feature> {
        val featureCollection = json.decodeFromString<FeatureCollection>(geoJsonString)
        return featureCollection.features
            .filter { feature ->
                val lineName = feature.properties.lineName
                lineName == null || !lineName.contains("Maldives", ignoreCase = true)
            }
    }

    /**
     * Loads the boundary lines from assets, filters out Maldives features,
     * and returns the polylines as List<List<LatLng>>.
     */
    fun loadBoundaries(
        ctx: Context? = context,
        assetFileName: String = DEFAULT_ASSET_NAME
    ): List<List<LatLng>> {
        val appContext = checkNotNull(ctx) {
            "A non-null Context is required to load boundaries from assets."
        }
        val geoJsonString = appContext.assets.open(assetFileName).bufferedReader().use { it.readText() }
        val lines = parseGeoJson(geoJsonString)
        cachedBoundaries = lines
        return lines
    }

    /**
     * Returns cached boundary lines if loaded, otherwise loads them from assets.
     */
    fun getBoundaryLines(): List<List<LatLng>> {
        return cachedBoundaries ?: context?.let { loadBoundaries(it) } ?: emptyList()
    }
}
