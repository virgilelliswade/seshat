package com.seshat.writer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.input.TextFieldValue
import com.seshat.writer.model.InlineFormatSpan
import com.seshat.writer.model.InlineFormatType

/**
 * Controller connecting toolbar formatting buttons (Bold, Italic, Underline, Strikethrough)
 * to the currently active / focused text field in the editor canvas.
 * Directly toggles [InlineFormatSpan]s so raw text remains completely free of confusing markdown asterisks.
 */
class EditorFormattingController {
    private var activeBlockId: String? = null
    private var activeValueGetter: (() -> TextFieldValue)? = null
    private var activeValueSetter: ((TextFieldValue) -> Unit)? = null
    private var activeFormatsGetter: (() -> List<InlineFormatSpan>)? = null
    private var activeFormatsSetter: ((List<InlineFormatSpan>) -> Unit)? = null

    fun registerActiveField(
        blockId: String,
        getValue: () -> TextFieldValue,
        setValue: (TextFieldValue) -> Unit,
        getFormats: () -> List<InlineFormatSpan> = { emptyList() },
        setFormats: (List<InlineFormatSpan>) -> Unit = {}
    ) {
        activeBlockId = blockId
        activeValueGetter = getValue
        activeValueSetter = setValue
        activeFormatsGetter = getFormats
        activeFormatsSetter = setFormats
    }

    fun unregister(blockId: String) {
        // Retain the latest getter/setter so taps on Bottom HUD buttons immediately
        // following an IME or touch focus transfer can still format the intended block.
    }

    fun toggleBold(): Boolean {
        return toggleFormat(InlineFormatType.BOLD)
    }

    fun toggleItalic(): Boolean {
        return toggleFormat(InlineFormatType.ITALIC)
    }

    fun toggleUnderline(): Boolean {
        return toggleFormat(InlineFormatType.UNDERLINE)
    }

    fun toggleStrikethrough(): Boolean {
        return toggleFormat(InlineFormatType.STRIKETHROUGH)
    }

    private fun toggleFormat(type: InlineFormatType): Boolean {
        val valueGetter = activeValueGetter ?: return false
        val formatsGetter = activeFormatsGetter ?: return false
        val formatsSetter = activeFormatsSetter ?: return false

        val currentValue = valueGetter()
        val currentFormats = formatsGetter()

        val updatedFormats = RichTextHelper.toggleSpan(
            text = currentValue.text,
            formats = currentFormats,
            selection = currentValue.selection,
            type = type
        )

        formatsSetter(updatedFormats)
        return true
    }
}

val LocalEditorFormattingController = staticCompositionLocalOf { EditorFormattingController() }

@Composable
fun rememberEditorFormattingController(): EditorFormattingController {
    return remember { EditorFormattingController() }
}
