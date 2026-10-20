package com.seshat.writer.dictation

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cross-platform voice dictation manager for screenplay input.
 */
class ScreenplayDictationManager(private val context: Context? = null) {

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _livePartialText = MutableStateFlow("")
    val livePartialText: StateFlow<String> = _livePartialText.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private val _dictationError = MutableStateFlow<String?>(null)
    val dictationError: StateFlow<String?> = _dictationError.asStateFlow()

    var onSpeechFinalized: ((String) -> Unit)? = null

    val isPermissionGranted: Boolean
        get() = true

    val isRecognitionAvailable: Boolean
        get() = true

    fun startListening() {
        _dictationError.value = null
        _livePartialText.value = ""
        _audioRmsLevel.value = 0.5f
        _isListening.value = true
    }

    fun stopListening() {
        _isListening.value = false
        _audioRmsLevel.value = 0f
        val text = _livePartialText.value.trim()
        if (text.isNotBlank()) {
            onSpeechFinalized?.invoke(text)
        }
    }

    fun cancelListening() {
        _isListening.value = false
        _audioRmsLevel.value = 0f
        _livePartialText.value = ""
        _dictationError.value = null
    }

    fun setSimulatedText(text: String) {
        _livePartialText.value = text
    }

    fun destroy() {
        cancelListening()
    }
}
