package com.seshat.writer.breakdown

data class SceneBreakdown(
    val sceneIndex: Int,
    val sceneNumber: String,
    val heading: String,
    val intExt: String,
    val location: String,
    val timeOfDay: String,
    val pageCountEighths: String,
    val pageEighthsFraction: Int,
    val startPage: Int,
    val castMembers: List<String>,
    val backgroundExtras: List<String> = emptyList(),
    val props: List<String> = emptyList(),
    val wardrobe: List<String> = emptyList(),
    val vehicles: List<String> = emptyList(),
    val stunts: List<String> = emptyList(),
    val specialEffects: List<String> = emptyList(),
    val visualEffects: List<String> = emptyList(),
    val soundNotes: List<String> = emptyList(),
    val specialEquipment: List<String> = emptyList(),
    val scriptDay: String = "DAY 1",
    val notes: String = ""
)

data class ScriptBreakdownSummary(
    val totalScenes: Int,
    val totalEighths: Int,
    val totalPagesFormatted: String,
    val uniqueCastCount: Int,
    val uniquePropsCount: Int,
    val uniqueVehiclesCount: Int,
    val uniqueStuntsCount: Int,
    val uniqueSfxCount: Int,
    val uniqueWardrobeCount: Int,
    val allProps: List<String>,
    val allVehicles: List<String>,
    val allStunts: List<String>,
    val allSfx: List<String>,
    val allWardrobe: List<String>,
    val scenes: List<SceneBreakdown>
)
