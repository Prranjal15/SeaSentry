package com.seasentry.app.survival

enum class SurvivalCategory(val displayName: String) {
    ALL("All Guides"),
    EMERGENCY("Critical Emergencies"),
    PROPULSION("Engine & Hull"),
    WEATHER("Severe Weather"),
    NAVIGATION("Border & Navigation"),
    RESCUE("Search & Rescue")
}

data class SurvivalGuideItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: SurvivalCategory,
    val urgency: String,
    val summary: String,
    val actionChecklist: List<String>,
    val proTip: String,
    val vhfChannel: String = "VHF Ch 16 (156.8 MHz)",
    val radioScript: String? = null
)
