package com.seshat.writer.ai

import android.content.Context
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.platform.AppPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class TableReadMode {
    FULL_TABLE_READ,
    REHEARSAL_SPARRING
}

data class TableReadState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isWaitingForUser: Boolean = false,
    val mode: TableReadMode = TableReadMode.FULL_TABLE_READ,
    val userRoleCharacter: String? = null,
    val currentBlockIndex: Int = 0,
    val totalPlayableBlocks: Int = 0,
    val currentSpeakingBlockId: String? = null,
    val currentSpeakingCharacter: String? = null,
    val currentSpeakingText: String = "",
    val speechRate: Float = 1.0f,
    val announceCharacterNames: Boolean = true,
    val isTtsReady: Boolean = true
)

/**
 * Cross-platform Table Read audio engine.
 * On macOS / Desktop, leverages native macOS speech synthesis (`say` command)
 * with multi-character voice casting.
 */
class TableReadManager(
    private val context: Context? = null,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(TableReadState())
    val state: StateFlow<TableReadState> = _state.asStateFlow()

    private var activeBlocks: List<ScreenplayBlock> = emptyList()
    private val characterVoiceMap: MutableMap<String, String> = mutableMapOf()
    private var speechJob: Job? = null
    private var activeProcess: Process? = null

    // Default cast of macOS voices for distinct character dramatization
    private val macVoicePool = listOf("Samantha", "Daniel", "Karen", "Alex", "Fred", "Victoria", "Moira", "Fiona", "Tessa")

    fun startReading(
        blocks: List<ScreenplayBlock>,
        startIndex: Int = 0,
        mode: TableReadMode = TableReadMode.FULL_TABLE_READ,
        userRole: String? = null,
        speechRate: Float = 1.0f,
        announceCharacterNames: Boolean = true
    ) {
        activeBlocks = blocks.filter { !it.isOmitted && it.text.isNotBlank() }
        if (activeBlocks.isEmpty()) return

        val safeStart = startIndex.coerceIn(0, activeBlocks.size - 1)
        buildCharacterVoiceMap(activeBlocks)

        _state.value = _state.value.copy(
            isPlaying = true,
            isPaused = false,
            isWaitingForUser = false,
            mode = mode,
            userRoleCharacter = userRole?.trim()?.uppercase(Locale.US),
            currentBlockIndex = safeStart,
            totalPlayableBlocks = activeBlocks.size,
            speechRate = speechRate,
            announceCharacterNames = announceCharacterNames,
            isTtsReady = true
        )

        speakCurrentBlock()
    }

    fun pause() {
        stopSpeechProcess()
        _state.value = _state.value.copy(isPlaying = false, isPaused = true)
    }

    fun resume() {
        if (_state.value.isPaused) {
            _state.value = _state.value.copy(isPlaying = true, isPaused = false)
            speakCurrentBlock()
        }
    }

    fun stop() {
        stopSpeechProcess()
        _state.value = _state.value.copy(
            isPlaying = false,
            isPaused = false,
            isWaitingForUser = false,
            currentSpeakingBlockId = null,
            currentSpeakingCharacter = null,
            currentSpeakingText = ""
        )
    }

    fun skipNext() {
        if (activeBlocks.isEmpty()) return
        stopSpeechProcess()
        val nextIndex = (_state.value.currentBlockIndex + 1).coerceAtMost(activeBlocks.size - 1)
        _state.value = _state.value.copy(currentBlockIndex = nextIndex, isWaitingForUser = false)
        if (_state.value.isPlaying || _state.value.isPaused) {
            speakCurrentBlock()
        }
    }

    fun skipPrevious() {
        if (activeBlocks.isEmpty()) return
        stopSpeechProcess()
        val prevIndex = (_state.value.currentBlockIndex - 1).coerceAtLeast(0)
        _state.value = _state.value.copy(currentBlockIndex = prevIndex, isWaitingForUser = false)
        if (_state.value.isPlaying || _state.value.isPaused) {
            speakCurrentBlock()
        }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        _state.value = _state.value.copy(speechRate = clamped)
    }

    fun userFinishedTurn() {
        if (_state.value.isWaitingForUser) {
            _state.value = _state.value.copy(isWaitingForUser = false)
            advanceToNextBlock()
        }
    }

    private fun stopSpeechProcess() {
        speechJob?.cancel()
        speechJob = null
        try {
            activeProcess?.destroyForcibly()
            activeProcess = null
        } catch (_: Throwable) {}
    }

    private fun speakCurrentBlock() {
        stopSpeechProcess()

        val idx = _state.value.currentBlockIndex
        if (idx !in activeBlocks.indices) {
            stop()
            return
        }

        val block = activeBlocks[idx]
        val speaker = findSpeakerForBlock(idx)
        val cleanSpeaker = speaker?.uppercase(Locale.US)?.trim()

        // Check Rehearsal / Sparring Mode
        val isUserTurn = _state.value.mode == TableReadMode.REHEARSAL_SPARRING &&
                !_state.value.userRoleCharacter.isNullOrBlank() &&
                cleanSpeaker != null &&
                cleanSpeaker.startsWith(_state.value.userRoleCharacter!!)

        if (isUserTurn && block.type == BlockType.DIALOGUE) {
            _state.value = _state.value.copy(
                isWaitingForUser = true,
                currentSpeakingBlockId = block.id,
                currentSpeakingCharacter = cleanSpeaker,
                currentSpeakingText = block.text
            )
            return
        }

        val textToSpeak = buildSpokenText(block, cleanSpeaker, _state.value.announceCharacterNames)
        val voice = cleanSpeaker?.let { characterVoiceMap[it] } ?: "Alex"

        _state.value = _state.value.copy(
            isWaitingForUser = false,
            currentSpeakingBlockId = block.id,
            currentSpeakingCharacter = cleanSpeaker,
            currentSpeakingText = block.text
        )

        speechJob = scope.launch(Dispatchers.IO) {
            if (AppPlatform.isMac && textToSpeak.isNotBlank()) {
                try {
                    val rate = (185 * _state.value.speechRate).toInt().coerceIn(100, 350)
                    val proc = ProcessBuilder("say", "-v", voice, "-r", rate.toString(), textToSpeak).start()
                    activeProcess = proc
                    proc.waitFor()
                } catch (_: Throwable) {
                    delay((textToSpeak.length * 50L).coerceIn(400L, 5000L))
                }
            } else if (textToSpeak.isNotBlank()) {
                delay((textToSpeak.length * 50L).coerceIn(400L, 5000L))
            } else {
                delay(200L)
            }

            withContext(Dispatchers.Main) {
                if (_state.value.isPlaying && !_state.value.isPaused) {
                    advanceToNextBlock()
                }
            }
        }
    }

    private fun advanceToNextBlock() {
        val nextIdx = _state.value.currentBlockIndex + 1
        if (nextIdx < activeBlocks.size) {
            _state.value = _state.value.copy(currentBlockIndex = nextIdx)
            speakCurrentBlock()
        } else {
            stop()
        }
    }

    private fun buildSpokenText(
        block: ScreenplayBlock,
        speaker: String?,
        announceSpeaker: Boolean
    ): String {
        return when (block.type) {
            BlockType.SCENE_HEADING, BlockType.STAGE_ACT_SCENE, BlockType.COMIC_PAGE, BlockType.COMIC_PANEL -> block.text
            BlockType.CHARACTER -> if (announceSpeaker) "${block.text}." else ""
            BlockType.PARENTHETICAL -> ""
            BlockType.DIALOGUE, BlockType.COMIC_BALLOON -> block.text
            BlockType.ACTION, BlockType.STAGE_DIRECTION, BlockType.COMIC_ART_NOTE, BlockType.COMIC_CAPTION, BlockType.COMIC_SFX -> block.text
            BlockType.TRANSITION, BlockType.STAGE_CURTAIN -> block.text
        }
    }

    private fun findSpeakerForBlock(index: Int): String? {
        val b = activeBlocks[index]
        if (b.type == BlockType.CHARACTER) return b.text
        if (b.type == BlockType.DIALOGUE || b.type == BlockType.COMIC_BALLOON || b.type == BlockType.PARENTHETICAL) {
            for (i in (index - 1) downTo 0) {
                val candidate = activeBlocks[i]
                if (candidate.type == BlockType.CHARACTER) return candidate.text
                if (candidate.type == BlockType.SCENE_HEADING || candidate.type == BlockType.STAGE_ACT_SCENE || candidate.type == BlockType.COMIC_PAGE) break
            }
        }
        return null
    }

    private fun buildCharacterVoiceMap(blocks: List<ScreenplayBlock>) {
        characterVoiceMap.clear()
        val characters = blocks
            .filter { it.type == BlockType.CHARACTER }
            .map { it.text.uppercase(Locale.US).trim() }
            .distinct()

        for ((idx, char) in characters.withIndex()) {
            val assignedVoice = macVoicePool[idx % macVoicePool.size]
            characterVoiceMap[char] = assignedVoice
        }
    }

    fun release() {
        stop()
    }
}
