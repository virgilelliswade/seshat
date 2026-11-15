package com.seshat.writer.universe

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 4 Themed Universe Builder Modes for Seshat:
 * 1. Arboreal: World Tree / Yggdrasil / Nature (Mythic & Epic Worldbuilding)
 * 2. Stellar: Celestial Orrery / Astronomy (Hard Sci-Fi & Cosmic Space Opera)
 * 3. Deoxyribonuclear: The Genome Loom / DNA (Biopunk & The Stellar Codex Model)
 * 4. Temporal: The Chrono-Loom / MCU-ish (Multiverse, Timelines & Sagas)
 */
enum class UniverseTheme(
    val id: String,
    val displayName: String,
    val engineName: String,
    val foundationsLabel: String,
    val masterTimelineLabel: String,
    val erasLabel: String,
    val mainProjectsLabel: String,
    val spinoffsLabel: String,
    val microUnitsLabel: String,
    val deliverablesLabel: String,
    val archetypesLabel: String,
    val scrappedLabel: String,
    val icon: String
) {
    ARBOREAL(
        id = "ARBOREAL",
        displayName = "Arboreal (World Tree)",
        engineName = "The World Tree (Universe Viewer)",
        foundationsLabel = "Roots (Foundational Lore & Rules)",
        masterTimelineLabel = "The Trunk (Timeline Axis)",
        erasLabel = "Ages (Historical Eras)",
        mainProjectsLabel = "Main Branches (Core Projects)",
        spinoffsLabel = "Offshoots (Spinoffs & Sidequests)",
        microUnitsLabel = "Leaves (Scenes & Chapters)",
        deliverablesLabel = "The Harvest (Deliverables: PDF/FDX)",
        archetypesLabel = "The Pantheon (Character Archetypes)",
        scrappedLabel = "Pruned Branches (Non-Canon)",
        icon = "🌲"
    ),
    STELLAR(
        id = "STELLAR",
        displayName = "Stellar (Celestial Orrery)",
        engineName = "The Celestial Orrery (Universe Viewer)",
        foundationsLabel = "Universal Constants (Foundational Lore & Rules)",
        masterTimelineLabel = "The Prime Meridian (Timeline Axis)",
        erasLabel = "Epochs (Historical Eras)",
        mainProjectsLabel = "Major Vectors (Core Projects)",
        spinoffsLabel = "Orbital Offshoots (Spinoffs & Sidequests)",
        microUnitsLabel = "Luminaries (Scenes & Chapters)",
        deliverablesLabel = "Zenith Artifacts (Deliverables: PDF/FDX)",
        archetypesLabel = "The Stellarium (Character Archetypes)",
        scrappedLabel = "Dark Nebula (Non-Canon)",
        icon = "🔭"
    ),
    DEOXYRIBONUCLEAR(
        id = "DEOXYRIBONUCLEAR",
        displayName = "Deoxyribonuclear (DNA / Stellar Codex)",
        engineName = "The Genome Loom (Universe Viewer)",
        foundationsLabel = "Base Pairs (Foundational Lore & Rules)",
        masterTimelineLabel = "The Chromosome Axis (Timeline Axis)",
        erasLabel = "Helical Cycles (Historical Eras)",
        mainProjectsLabel = "Canon Strands (Core Projects)",
        spinoffsLabel = "Splices (Spinoffs & Sidequests)",
        microUnitsLabel = "Codons (Scenes & Chapters)",
        deliverablesLabel = "Expressed Phenotypes (Deliverables: PDF/FDX)",
        archetypesLabel = "Gene Lineages (Character Archetypes)",
        scrappedLabel = "Silenced Introns (Non-Canon)",
        icon = "🧬"
    ),
    TEMPORAL(
        id = "TEMPORAL",
        displayName = "Temporal (Chrono-Loom / Multiverse)",
        engineName = "The Chrono-Loom (Universe Viewer)",
        foundationsLabel = "Absolute Points (Foundational Lore & Rules)",
        masterTimelineLabel = "The Sacred Timeline (Timeline Axis)",
        erasLabel = "Phases & Sagas (Historical Eras)",
        mainProjectsLabel = "Spine Realities (Core Projects)",
        spinoffsLabel = "Nexus Branches (Spinoffs & Sidequests)",
        microUnitsLabel = "Nexus Moments (Scenes & Chapters)",
        deliverablesLabel = "Nexus Artifacts (Deliverables: PDF/FDX)",
        archetypesLabel = "Anchor Beings (Character Archetypes)",
        scrappedLabel = "Pruned Timelines (Non-Canon)",
        icon = "⏳"
    ),
    MYTHOLOGICAL(
        id = "MYTHOLOGICAL",
        displayName = "Mythological (Classical Epics)",
        engineName = "The Loom of Destiny (Universe Viewer)",
        foundationsLabel = "Creation Myths (Foundational Lore & Rules)",
        masterTimelineLabel = "The Thread of Fate (Timeline Axis)",
        erasLabel = "Mythic Ages (Historical Eras)",
        mainProjectsLabel = "Great Epics (Core Projects)",
        spinoffsLabel = "Folklore & Quests (Spinoffs & Sidequests)",
        microUnitsLabel = "Cantos & Labors (Scenes & Chapters)",
        deliverablesLabel = "The Sacred Codex (Deliverables: PDF/FDX)",
        archetypesLabel = "The Pantheon (Character Archetypes)",
        scrappedLabel = "The Underworld (Non-Canon & Lost Lore)",
        icon = "🏛️"
    );

    companion object {
        fun fromId(id: String?): UniverseTheme =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ARBOREAL
    }
}

/**
 * The relationship of a project branch within the universe tree.
 */
enum class BranchRole(
    val label: String,
    val icon: String,
    val description: String
) {
    MAIN_SPINE(
        label = "Main Canon Spine",
        icon = "⭐",
        description = "Anchors directly to the central timeline trunk (Flagship works)."
    ),
    DIRECT_SEQUEL(
        label = "Direct Sequel / Next Arc",
        icon = "➡️",
        description = "Extends directly forward from a preceding parent project."
    ),
    SPINOFF_SIDEQUEST(
        label = "Sidequest / Spinoff",
        icon = "🔀",
        description = "Shoots out as a curved branch off an existing parent project."
    ),
    PREQUEL_ORIGIN(
        label = "Prequel / Origin Story",
        icon = "⏪",
        description = "Limbs backward from a parent work into an earlier historical era."
    ),
    PARALLEL_COMPANION(
        label = "Parallel Companion Work",
        icon = "📖",
        description = "Runs side-by-side with another work in a different medium."
    );

    companion object {
        fun fromName(name: String?): BranchRole =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MAIN_SPINE
    }
}

/**
 * Historical era along the chronological trunk.
 */
data class UniverseTimelineEra(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val timeSpan: String = "",
    val description: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("timeSpan", timeSpan)
        put("description", description)
    }

    companion object {
        fun fromJson(json: JSONObject): UniverseTimelineEra = UniverseTimelineEra(
            id = json.optString("id", UUID.randomUUID().toString()),
            name = json.optString("name", ""),
            timeSpan = json.optString("timeSpan", ""),
            description = json.optString("description", "")
        )
    }
}
