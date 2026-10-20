package com.seshat.writer.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

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
    val isTtsReady: Boolean = false
)

class TableReadManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _state = MutableStateFlow(TableReadState())
    val state: StateFlow<TableReadState> = _state.asStateFlow()

    private var activeBlocks: List<ScreenplayBlock> = emptyList()
    private var characterPitchMap: MutableMap<String, Float> = mutableMapOf()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
                _state.value = _state.value.copy(isTtsReady = true)
                setupProgressListener()
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                scope.launch(Dispatchers.Main) {
                    if (_state.value.isPlaying && !_state.value.isPaused) {
                        advanceToNextBlock()
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                scope.launch(Dispatchers.Main) {
                    if (_state.value.isPlaying) {
                        advanceToNextBlock()
                    }
                }
            }
        })
    }

    fun startReading(
        blocks: List<ScreenplayBlock>,
        startIndex: Int = 0,
        mode: TableReadMode = TableReadMode.FULL_TABLE_READ,
        userRole: String? = null,
        speechRate: Float = 1.0f,
        announceCharacterNames: Boolean = true
    ) {
        if (!isInitialized || tts == null) return

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
            announceCharacterNames = announceCharacterNames
        )

        speakCurrentBlock()
    }

    fun pause() {
        tts?.stop()
        _state.value = _state.value.copy(isPlaying = false, isPaused = true)
    }

    fun resume() {
        if (_state.value.isPaused) {
            _state.value = _state.value.copy(isPlaying = true, isPaused = false)
            speakCurrentBlock()
        }
    }

    fun stop() {
        tts?.stop()
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
        tts?.stop()
        val nextIndex = (_state.value.currentBlockIndex + 1).coerceAtMost(activeBlocks.size - 1)
        _state.value = _state.value.copy(currentBlockIndex = nextIndex, isWaitingForUser = false)
        if (_state.value.isPlaying || _state.value.isPaused) {
            speakCurrentBlock()
        }
    }

    fun skipPrevious() {
        if (activeBlocks.isEmpty()) return
        tts?.stop()
        val prevIndex = (_state.value.currentBlockIndex - 1).coerceAtLeast(0)
        _state.value = _state.value.copy(currentBlockIndex = prevIndex, isWaitingForUser = false)
        if (_state.value.isPlaying || _state.value.isPaused) {
            speakCurrentBlock()
        }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        _state.value = _state.value.copy(speechRate = clamped)
        tts?.setSpeechRate(clamped)
    }

    fun userFinishedTurn() {
        if (_state.value.isWaitingForUser) {
            _state.value = _state.value.copy(isWaitingForUser = false)
            advanceToNextBlock()
        }
    }

    private fun speakCurrentBlock() {
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
            // It's the user's turn! Pause and wait for the writer to speak.
            _state.value = _state.value.copy(
                isWaitingForUser = true,
                currentSpeakingBlockId = block.id,
                currentSpeakingCharacter = cleanSpeaker,
                currentSpeakingText = block.text
            )
            return
        }

        val textToSpeak = buildSpokenText(block, cleanSpeaker, _state.value.announceCharacterNames)
        val pitch = if (cleanSpeaker != null) characterPitchMap[cleanSpeaker] ?: 1.0f else 1.0f

        _state.value = _state.value.copy(
            isWaitingForUser = false,
            currentSpeakingBlockId = block.id,
            currentSpeakingCharacter = cleanSpeaker,
            currentSpeakingText = block.text
        )

        tts?.setSpeechRate(_state.value.speechRate)
        tts?.setPitch(pitch)

        val utteranceId = "block_${block.id}_$idx"
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
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
            BlockType.PARENTHETICAL -> "" // Skip parentheticals or speak subtly
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
        characterPitchMap.clear()
        val characters = blocks
            .filter { it.type == BlockType.CHARACTER }
            .map { it.text.uppercase(Locale.US).trim() }
            .distinct()

        val pitchPills = listOf(0.85f, 1.15f, 0.95f, 1.25f, 0.75f, 1.05f)
        for ((idx, char) in characters.withIndex()) {
            val assignedPitch = pitchPills[idx % pitchPills.size]
            characterPitchMap[char] = assignedPitch
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
