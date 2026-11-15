package com.seshat.writer.universe

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Standard terminology for the Living Universe and World Tree architecture:
 * 1. Foundational Lore & Rules (World rules, characters, locations)
 * 2. Timeline Axis & Historical Eras (Chronological spine)
 * 3. Core Projects & Spinoffs (Medium-agnostic story branches)
 * 4. Scenes & Chapters (Micro narrative units)
 * 5. Deliverables (PDF/FDX exports)
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
    STANDARD(
        id = "STANDARD",
        displayName = "Story Universe",
        engineName = "Universe Hub",
        foundationsLabel = "Foundational Lore & Rules",
        masterTimelineLabel = "Timeline Axis",
        erasLabel = "Historical Eras",
        mainProjectsLabel = "Core Projects",
        spinoffsLabel = "Spinoffs & Sidequests",
        microUnitsLabel = "Scenes & Chapters",
        deliverablesLabel = "Deliverables (PDF/FDX)",
        archetypesLabel = "Character Archetypes",
        scrappedLabel = "Non-Canon / Scrapped",
        icon = "🪐"
    ),
    ARBOREAL(
        id = "ARBOREAL",
        displayName = "Story Universe",
        engineName = "Universe Hub",
        foundationsLabel = "Foundational Lore & Rules",
        masterTimelineLabel = "Timeline Axis",
        erasLabel = "Historical Eras",
        mainProjectsLabel = "Core Projects",
        spinoffsLabel = "Spinoffs & Sidequests",
        microUnitsLabel = "Scenes & Chapters",
        deliverablesLabel = "Deliverables (PDF/FDX)",
        archetypesLabel = "Character Archetypes",
        scrappedLabel = "Non-Canon / Scrapped",
        icon = "🪐"
    ),
    STELLAR(
        id = "STELLAR",
        displayName = "Story Universe",
        engineName = "Universe Hub",
        foundationsLabel = "Foundational Lore & Rules",
        masterTimelineLabel = "Timeline Axis",
        erasLabel = "Historical Eras",
        mainProjectsLabel = "Core Projects",
        spinoffsLabel = "Spinoffs & Sidequests",
        microUnitsLabel = "Scenes & Chapters",
        deliverablesLabel = "Deliverables (PDF/FDX)",
        archetypesLabel = "Character Archetypes",
        scrappedLabel = "Non-Canon / Scrapped",
        icon = "🪐"
    ),
    DEOXYRIBONUCLEAR(
        id = "DEOXYRIBONUCLEAR",
        displayName = "Story Universe",
        engineName = "Universe Hub",
        foundationsLabel = "Foundational Lore & Rules",
        masterTimelineLabel = "Timeline Axis",
        erasLabel = "Historical Eras",
        mainProjectsLabel = "Core Projects",
        spinoffsLabel = "Spinoffs & Sidequests",
        microUnitsLabel = "Scenes & Chapters",
        deliverablesLabel = "Deliverables (PDF/FDX)",
        archetypesLabel = "Character Archetypes",
        scrappedLabel = "Non-Canon / Scrapped",
        icon = "🪐"
    ),
    TEMPORAL(
        id = "TEMPORAL",
        displayName = "Story Universe",
        engineName = "Universe Hub",
        foundationsLabel = "Foundational Lore & Rules",
        masterTimelineLabel = "Timeline Axis",
        erasLabel = "Historical Eras",
        mainProjectsLabel = "Core Projects",
        spinoffsLabel = "Spinoffs & Sidequests",
        microUnitsLabel = "Scenes & Chapters",
        deliverablesLabel = "Deliverables (PDF/FDX)",
        archetypesLabel = "Character Archetypes",
        scrappedLabel = "Non-Canon / Scrapped",
        icon = "🪐"
    ),
    MYTHOLOGICAL(
        id = "MYTHOLOGICAL",
        displayName = "Story Universe",
        engineName = "Universe Hub",
        foundationsLabel = "Foundational Lore & Rules",
        masterTimelineLabel = "Timeline Axis",
        erasLabel = "Historical Eras",
        mainProjectsLabel = "Core Projects",
        spinoffsLabel = "Spinoffs & Sidequests",
        microUnitsLabel = "Scenes & Chapters",
        deliverablesLabel = "Deliverables (PDF/FDX)",
        archetypesLabel = "Character Archetypes",
        scrappedLabel = "Non-Canon / Scrapped",
        icon = "🪐"
    );

    companion object {
        fun fromId(id: String?): UniverseTheme =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: STANDARD
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
