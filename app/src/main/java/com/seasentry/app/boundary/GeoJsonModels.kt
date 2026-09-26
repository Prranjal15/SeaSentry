package com.seasentry.app.boundary

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class FeatureCollection(
    val type: String = "FeatureCollection",
    val features: List<Feature> = emptyList()
)

@Serializable
data class Feature(
    val type: String = "Feature",
    val id: String? = null,
    val geometry: Geometry,
    @SerialName("geometry_name")
    val geometryName: String? = null,
    val properties: Properties = Properties()
)

@Serializable(with = GeometrySerializer::class)
sealed class Geometry {
    abstract val type: String
    abstract fun toLatLngLines(): List<List<LatLng>>

    @Serializable
    @SerialName("LineString")
    data class LineString(
        override val type: String = "LineString",
        val coordinates: List<List<Double>>
    ) : Geometry() {
        override fun toLatLngLines(): List<List<LatLng>> {
            val line = coordinates.mapNotNull { coord ->
                if (coord.size >= 2) {
                    // GeoJSON coordinates are [longitude, latitude]
                    LatLng(latitude = coord[1], longitude = coord[0])
                } else null
            }
            return if (line.isNotEmpty()) listOf(line) else emptyList()
        }
    }

    @Serializable
    @SerialName("MultiLineString")
    data class MultiLineString(
        override val type: String = "MultiLineString",
        val coordinates: List<List<List<Double>>>
    ) : Geometry() {
        override fun toLatLngLines(): List<List<LatLng>> {
            return coordinates.mapNotNull { line ->
                val pts = line.mapNotNull { coord ->
                    if (coord.size >= 2) {
                        // GeoJSON coordinates are [longitude, latitude]
                        LatLng(latitude = coord[1], longitude = coord[0])
                    } else null
                }
                if (pts.isNotEmpty()) pts else null
            }
        }
    }
}

object GeometrySerializer : JsonContentPolymorphicSerializer<Geometry>(Geometry::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Geometry> {
        val type = element.jsonObject["type"]?.jsonPrimitive?.content
        return when (type) {
            "LineString" -> Geometry.LineString.serializer()
            "MultiLineString" -> Geometry.MultiLineString.serializer()
            else -> throw IllegalArgumentException("Unsupported GeoJSON geometry type: $type")
        }
    }
}

@Serializable
data class Properties(
    @SerialName("line_id")
    val lineId: Long? = null,
    @SerialName("line_name")
    val lineName: String? = null,
    @SerialName("line_type")
    val lineType: String? = null,
    @SerialName("mrgid_sov1")
    val mrgidSov1: Long? = null,
    @SerialName("mrgid_ter1")
    val mrgidTer1: Long? = null,
    val territory1: String? = null,
    val sovereign1: String? = null,
    @SerialName("mrgid_ter2")
    val mrgidTer2: Long? = null,
    val territory2: String? = null,
    @SerialName("mrgid_sov2")
    val mrgidSov2: Long? = null,
    val sovereign2: String? = null,
    @SerialName("mrgid_eez1")
    val mrgidEez1: Long? = null,
    val eez1: String? = null,
    @SerialName("mrgid_eez2")
    val mrgidEez2: Long? = null,
    val eez2: String? = null,
    val source1: String? = null,
    val url1: String? = null,
    val source2: String? = null,
    val url2: String? = null,
    val source3: String? = null,
    val url3: String? = null,
    val origin: String? = null,
    @SerialName("doc_date")
    val docDate: String? = null,
    @SerialName("mrgid_jreg")
    val mrgidJreg: Long? = null,
    @SerialName("joint_reg")
    val jointReg: String? = null,
    @SerialName("length_km")
    val lengthKm: Double? = null
)
