package com.seshat.writer.diff

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock

enum class DiffType {
    UNCHANGED,
    ADDED,
    REMOVED,
    MODIFIED
}

data class BlockDiff(
    val type: DiffType,
    val blockA: ScreenplayBlock? = null,
    val blockB: ScreenplayBlock? = null,
    val oldText: String? = null,
    val newText: String? = null,
    val blockType: BlockType,
    val speaker: String? = null
)

data class SceneDiffStats(
    val addedBlocks: Int = 0,
    val removedBlocks: Int = 0,
    val modifiedBlocks: Int = 0,
    val unchangedBlocks: Int = 0
)

data class SceneDiff(
    val type: DiffType,
    val sceneNumberA: String? = null,
    val sceneNumberB: String? = null,
    val headingA: String? = null,
    val headingB: String? = null,
    val blockDiffs: List<BlockDiff> = emptyList(),
    val stats: SceneDiffStats = SceneDiffStats()
)

data class DraftDiffSummary(
    val totalScenesA: Int,
    val totalScenesB: Int,
    val scenesAdded: Int,
    val scenesRemoved: Int,
    val scenesModified: Int,
    val scenesUnchanged: Int,
    val blocksAdded: Int,
    val blocksRemoved: Int,
    val blocksModified: Int,
    val wordsDelta: Int,
    val hasDifferences: Boolean
)

data class DraftDiffResult(
    val titleA: String,
    val titleB: String,
    val authorA: String,
    val authorB: String,
    val draftTagA: String,
    val draftTagB: String,
    val sceneDiffs: List<SceneDiff>,
    val summary: DraftDiffSummary
)
