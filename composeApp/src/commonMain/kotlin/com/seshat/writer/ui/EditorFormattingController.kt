package com.seshat.writer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Controller connecting toolbar formatting buttons (Bold, Italic, Underline)
 * to the currently active / focused text field in the editor canvas.
 */
class EditorFormattingController {
    private var activeBlockId: String? = null
    private var activeValueGetter: (() -> TextFieldValue)? = null
    private var activeValueSetter: ((TextFieldValue) -> Unit)? = null

    fun registerActiveField(
        blockId: String,
        getValue: () -> TextFieldValue,
        setValue: (TextFieldValue) -> Unit
    ) {
        activeBlockId = blockId
        activeValueGetter = getValue
        activeValueSetter = setValue
    }

    fun unregister(blockId: String) {
        // Retain the latest getter/setter so taps on Bottom HUD buttons immediately
        // following an IME or touch focus transfer can still format the intended block.
    }

    fun toggleBold(): Boolean {
        return applyDelimiter("**")
    }

    fun toggleItalic(): Boolean {
        return applyDelimiter("*")
    }

    fun toggleUnderline(): Boolean {
        return applyDelimiter("_")
    }

    private fun applyDelimiter(delimiter: String): Boolean {
        val getter = activeValueGetter ?: return false
        val setter = activeValueSetter ?: return false
        val current = getter()
        val formatted = TextFormattingHelper.toggleFormatting(current, delimiter)
        setter(formatted)
        return true
    }
}

val LocalEditorFormattingController = staticCompositionLocalOf { EditorFormattingController() }

@Composable
fun rememberEditorFormattingController(): EditorFormattingController {
    return remember { EditorFormattingController() }
}
