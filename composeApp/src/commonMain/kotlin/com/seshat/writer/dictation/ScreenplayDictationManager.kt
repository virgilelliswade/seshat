package com.seshat.writer.dictation

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine
import kotlin.concurrent.thread
import kotlin.math.sqrt

/**
 * Cross-platform macOS Desktop voice dictation manager with real microphone capture,
 * live RMS waveform metering, WAV buffering (for Gemini Multimodal STT when configured),
 * and full compatibility with macOS system Dictation (⌘D / Fn Fn) into the live text box.
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

    private var audioLine: TargetDataLine? = null
    private var captureThread: Thread? = null
    private val pcmBuffer = ByteArrayOutputStream()
    private val pcmLock = Any()

    val isPermissionGranted: Boolean
        get() = true

    val isRecognitionAvailable: Boolean
        get() = true

    fun startListening() {
        stopAudioLineOnly()
        synchronized(pcmLock) { pcmBuffer.reset() }
        _dictationError.value = null
        _audioRmsLevel.value = 0f
        _isListening.value = true

        captureThread = thread(isDaemon = true, name = "SeshatDesktopMicCapture") {
            try {
                val format = AudioFormat(16000f, 16, 1, true, false)
                val line = AudioSystem.getTargetDataLine(format)
                line.open(format)
                line.start()
                audioLine = line

                val buf = ByteArray(1024)
                while (_isListening.value && line.isOpen) {
                    val read = line.read(buf, 0, buf.size)
                    if (read > 0) {
                        synchronized(pcmLock) {
                            // Cap buffer at ~60 seconds of 16kHz 16-bit mono audio (~1.9 MB)
                            if (pcmBuffer.size() < 1_920_000) {
                                pcmBuffer.write(buf, 0, read)
                            }
                        }
                        // Compute real normalized RMS amplitude for waveform bars
                        var sumSquares = 0.0
                        val sampleCount = read / 2
                        var i = 0
                        while (i + 1 < read) {
                            val sample = ((buf[i + 1].toInt() shl 8) or (buf[i].toInt() and 0xFF)).toShort().toDouble()
                            sumSquares += sample * sample
                            i += 2
                        }
                        val rms = if (sampleCount > 0) sqrt(sumSquares / sampleCount) else 0.0
                        val normalized = (rms / 6500.0).toFloat().coerceIn(0f, 1f)
                        _audioRmsLevel.value = normalized
                    }
                }
            } catch (_: Throwable) {
                // Keep text-box / OS Dictation (Fn Fn / ⌘D) functional even if hardware mic line is unavailable
                _audioRmsLevel.value = 0f
            } finally {
                stopAudioLineOnly()
            }
        }
    }

    fun consumeRecordedWavBytes(): ByteArray? {
        val rawPcm = synchronized(pcmLock) {
            val bytes = pcmBuffer.toByteArray()
            pcmBuffer.reset()
            bytes
        }
        if (rawPcm.size < 1600) return null
        return try {
            val format = AudioFormat(16000f, 16, 1, true, false)
            val bais = ByteArrayInputStream(rawPcm)
            val ais = AudioInputStream(bais, format, (rawPcm.size / 2).toLong())
            val wavOut = ByteArrayOutputStream()
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavOut)
            wavOut.toByteArray()
        } catch (_: Throwable) {
            null
        }
    }

    private fun stopAudioLineOnly() {
        try {
            audioLine?.stop()
        } catch (_: Throwable) {}
        try {
            audioLine?.close()
        } catch (_: Throwable) {}
        audioLine = null
    }

    fun stopListening() {
        _isListening.value = false
        _audioRmsLevel.value = 0f
        stopAudioLineOnly()
        val text = _livePartialText.value.trim()
        if (text.isNotBlank()) {
            onSpeechFinalized?.invoke(text)
        }
    }

    fun cancelListening() {
        _isListening.value = false
        _audioRmsLevel.value = 0f
        stopAudioLineOnly()
        synchronized(pcmLock) { pcmBuffer.reset() }
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
