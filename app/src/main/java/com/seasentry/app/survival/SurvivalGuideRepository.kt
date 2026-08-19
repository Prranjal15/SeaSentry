package com.seasentry.app.survival

object SurvivalGuideRepository {

    val GUIDES = listOf(
        SurvivalGuideItem(
            id = "guide-imbl",
            title = "IMBL Border Emergency & Evasion",
            subtitle = "Immediate 180° turn and border de-escalation protocol",
            category = SurvivalCategory.NAVIGATION,
            urgency = "CRITICAL",
            summary = "Step-by-step procedures when your vessel breaches or approaches an International Maritime Boundary Line (IMBL).",
            actionChecklist = listOf(
                "Immediately disengage autopilot and execute a 180° South turn back into authorized waters.",
                "Cut fishing gear lines or retrieve drift nets immediately if safe to do so.",
                "Broadcast current GPS coordinates and vessel ID on VHF Channel 16 to maritime patrol.",
                "Illuminate all navigation running lights and hoist vessel identification flag.",
                "Maintain logbook entry of exact time, coordinates, and evasive action taken."
            ),
            proTip = "Never shut off NavIC transponders or AIS when near international border zones.",
            vhfChannel = "VHF Ch 16",
            radioScript = "MAYDAY / PAN-PAN: Sector Coast Guard, this is Vessel [ID], Latitude [Lat], Longitude [Lon], executing emergency 180° turn south towards Indian territorial waters, over."
        ),
        SurvivalGuideItem(
            id = "guide-engine",
            title = "Engine Failure in Open Sea",
            subtitle = "Drift control, sea anchor deployment & restart diagnostics",
            category = SurvivalCategory.PROPULSION,
            urgency = "HIGH",
            summary = "Protocols for sudden total power loss, main engine stall, or propeller fouling.",
            actionChecklist = listOf(
                "Deploy sea anchor (drogue) from the bow to keep the vessel head-to-sea and minimize drift rate.",
                "Inspect fuel water-separator filters and verify diesel fuel shut-off valve is open.",
                "Check propeller shaft for entanglement in discarded fishing nets or ropes.",
                "Activate vessel NavIC beacon and monitor drift vector on SeaSentry Hub.",
                "Prepare auxiliary battery bank and purge air from diesel injection lines."
            ),
            proTip = "Deploying a sea anchor reduces leeway drift by up to 70%, preventing rapid drift across boundaries.",
            vhfChannel = "VHF Ch 16 / DSC Ch 70",
            radioScript = "PAN-PAN, PAN-PAN, PAN-PAN: All stations, this is Fishing Vessel [ID], dead in water at position [Lat, Lon] due to engine failure, requesting tow assistance, over."
        ),
        SurvivalGuideItem(
            id = "guide-mob",
            title = "Man Overboard (MOB) Rescue Protocol",
            subtitle = "Williamson Turn, marker release & retrieval procedure",
            category = SurvivalCategory.EMERGENCY,
            urgency = "CRITICAL",
            summary = "Emergency rescue sequence when a crew member falls into the sea.",
            actionChecklist = listOf(
                "Shout 'MAN OVERBOARD' immediately and designate one lookout to NEVER take eyes off the person in water.",
                "Throw lifebuoy with attached Danbuoy marker and strobe light towards the victim immediately.",
                "Press the MOB button on NavIC GPS / SeaSentry Hub to pin the drop coordinate.",
                "Execute a Williamson Turn: Rudder hard over toward the victim's side, shift opposite rudder when 60° off course.",
                "Approach the person from downwind / leeward side to provide a calm sea shelter.",
                "Deploy rescue scrambling net or boarding ladder and secure victim with safety harness."
            ),
            proTip = "Keep visual contact uninterrupted. In 2-meter swells, an unspotted person is lost in under 60 seconds.",
            vhfChannel = "VHF Ch 16",
            radioScript = "MAYDAY, MAYDAY, MAYDAY: All vessels, Man Overboard in position [Lat, Lon], person in orange life vest, all vessels in vicinity please assist lookout, over."
        ),
        SurvivalGuideItem(
            id = "guide-weather",
            title = "Severe Weather & Gale Storm Defense",
            subtitle = "Vessel securing, storm tactics & heave-to maneuvering",
            category = SurvivalCategory.WEATHER,
            urgency = "HIGH",
            summary = "Survival guidelines for sudden cyclones, squalls, and gale-force wave states in the open sea.",
            actionChecklist = listOf(
                "Dog down and secure all deck hatches, watertight doors, and companionways.",
                "Start bilge pumps and verify all automatic float switches are active.",
                "Lash down all loose fishing crates, drums, and heavy deck gear securely.",
                "Maneuver into 'Heave-To' configuration: keep bow angled 30°-45° into oncoming swells at low engine RPM.",
                "Order all crew to don Type-1 SOLAS approved life jackets with distress whistles and lights."
            ),
            proTip = "Never take steep breaking seas on the beam (broadside). Always keep the bow angled into the swells.",
            vhfChannel = "VHF Ch 16 / Weather Fax",
            radioScript = "SECURITE, SECURITE, SECURITE: SeaSentry coastal stations, reporting severe squall with wind speeds exceeding 45 kts at [Lat, Lon]."
        ),
        SurvivalGuideItem(
            id = "guide-collision",
            title = "Collision Risk & COLREGS Evasion",
            subtitle = "Emergency evasion, acoustic fog signals & radar monitoring",
            category = SurvivalCategory.NAVIGATION,
            urgency = "HIGH",
            summary = "Evasive maneuvers to avoid close-quarters collisions with commercial cargo ships and trawlers.",
            actionChecklist = listOf(
                "Check compass bearing of approaching vessel. If bearing does NOT change significantly, risk of collision EXISTS.",
                "Sound 5 rapid short blasts (• • • • •) on vessel horn/whistle indicating danger / doubt of intention.",
                "Alter course boldly and early to STARBOARD (Right) as per COLREGS Rule 14 & 16.",
                "Flash high-power searchlight or shine deck floodlight onto sails/hull to ensure visual identification.",
                "Contact oncoming vessel on VHF Ch 16 with your position and intended evasive turn."
            ),
            proTip = "Large commercial container vessels require 2 to 3 nautical miles to alter course. Take evasive action early.",
            vhfChannel = "VHF Ch 16 / Ch 13 (Bridge-to-Bridge)",
            radioScript = "Vessel approaching on my port bow at 14 knots, this is Fishing Vessel SeaSentry on your starboard, I am altering course to starboard, please confirm."
        ),
        SurvivalGuideItem(
            id = "guide-fire",
            title = "Fire Onboard Marine Vessel",
            subtitle = "Engine room fuel cut-off, Class B suppression & evacuation",
            category = SurvivalCategory.EMERGENCY,
            urgency = "CRITICAL",
            summary = "Immediate firefighting protocol for electrical, engine oil, and galley fires at sea.",
            actionChecklist = listOf(
                "Sound continuous emergency fire alarm throughout all vessel compartments.",
                "Pull emergency remote fuel shutoff valve to starve engine room fuel supply.",
                "Isolate electrical battery banks and switch off master DC breaker panels.",
                "Discharge Dry Powder / CO2 fire extinguisher at the base of the fire in sweeping motion.",
                "Seal all air vents and companionways to starve the fire of oxygen.",
                "Prepare life raft and distress grab-bag on aft deck in case abandonment is mandated."
            ),
            proTip = "Never throw water onto engine oil or diesel fires; water will vaporize violently and spread burning liquid.",
            vhfChannel = "VHF Ch 16",
            radioScript = "MAYDAY, MAYDAY, MAYDAY: All stations, Fishing Vessel [ID], fire in engine compartment at [Lat, Lon], 5 persons on board, preparing life raft, over."
        ),
        SurvivalGuideItem(
            id = "guide-sos",
            title = "SOS Mayday & GMDSS Transmission",
            subtitle = "Standard international distress broadcast sequence",
            category = SurvivalCategory.RESCUE,
            urgency = "CRITICAL",
            summary = "Exact voice format and EPIRB / SeaSentry digital beacon activation instructions.",
            actionChecklist = listOf(
                "Press and hold 'SEND SOS' on SeaSentry Hub to initiate digital mesh broadcast.",
                "Set marine VHF radio to Channel 16 at HIGH POWER (25 Watts).",
                "Speak slowly and clearly: State 'MAYDAY' three times followed by vessel name.",
                "State vessel GPS latitude and longitude coordinates accurately.",
                "State nature of distress, number of persons on board (POB), and seaworthiness of vessel.",
                "Activate 406 MHz EPIRB and prepare red hand flares when rescue craft is within visual range."
            ),
            proTip = "Keep transmitting every 3 minutes until an official Coast Guard or Naval station acknowledges your distress call.",
            vhfChannel = "VHF Ch 16 / 2182 kHz",
            radioScript = "MAYDAY, MAYDAY, MAYDAY. This is Vessel [Name/ID]. Position: [Latitude, Longitude]. Nature of Distress: Vessel taking on water / sinking. 6 souls onboard. Life raft deployed. OVER."
        ),
        SurvivalGuideItem(
            id = "guide-hypothermia",
            title = "Cold Water Survival & Hypothermia",
            subtitle = "H.E.L.P. posture, thermal retention & rewarming",
            category = SurvivalCategory.RESCUE,
            urgency = "MEDIUM",
            summary = "Body heat preservation strategies during immersion in cold open waters.",
            actionChecklist = listOf(
                "Adopt H.E.L.P. (Heat Escape Lessening Posture): Cross arms over chest and draw knees up tight to stomach.",
                "If multiple crew members are in water, form a tight circular 'Huddle' with chests pressed together.",
                "Keep head and neck out of the water; over 40% of body heat is lost from the scalp.",
                "Do NOT attempt to swim unless a floating rescue craft is within 50 meters.",
                "Once rescued: Remove wet clothing, wrap in dry blankets / foil thermal bivy, and rewarm torso first."
            ),
            proTip = "Never rub frostbitten or hypothermic limbs vigorously as it forces cold acidic blood back into the heart.",
            vhfChannel = "VHF Ch 16",
            radioScript = null
        )
    )

    fun getGuidesByCategory(category: SurvivalCategory): List<SurvivalGuideItem> {
        return if (category == SurvivalCategory.ALL) {
            GUIDES
        } else {
            GUIDES.filter { it.category == category }
        }
    }

    fun searchGuides(query: String): List<SurvivalGuideItem> {
        if (query.isBlank()) return GUIDES
        val q = query.trim().lowercase()
        return GUIDES.filter {
            it.title.lowercase().contains(q) ||
            it.subtitle.lowercase().contains(q) ||
            it.summary.lowercase().contains(q) ||
            it.actionChecklist.any { step -> step.lowercase().contains(q) }
        }
    }
}
