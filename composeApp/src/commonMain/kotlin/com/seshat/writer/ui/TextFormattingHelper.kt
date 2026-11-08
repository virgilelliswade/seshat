package com.seshat.writer.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Utility helper for toggling inline markdown formatting (Bold `**`, Italic `*`, Underline `_`)
 * on selected text or at cursor positions.
 */
object TextFormattingHelper {

    /**
     * Toggles the given markdown delimiter around the current selection or at the cursor.
     * - If text is selected and already wrapped by [delimiter], the delimiters are stripped (toggled off).
     * - If text is selected and not wrapped, [delimiter] is wrapped around the selected text.
     * - If cursor is collapsed and directly inside [delimiter]...[delimiter], the delimiters are removed.
     * - If cursor is collapsed and outside, [delimiter]...[delimiter] is inserted with the cursor placed in between.
     */
    fun toggleFormatting(
        value: TextFieldValue,
        delimiter: String
    ): TextFieldValue {
        val text = value.text
        val selection = value.selection
        val dLen = delimiter.length

        // Case 1: Text is selected
        if (!selection.collapsed) {
            val start = selection.min
            val end = selection.max
            val selectedText = text.substring(start, end)

            // 1a. Selected text itself is wrapped in delimiter (e.g. "**hello**")
            if (selectedText.length >= dLen * 2 &&
                selectedText.startsWith(delimiter) &&
                selectedText.endsWith(delimiter)
            ) {
                val unwrapped = selectedText.substring(dLen, selectedText.length - dLen)
                val newText = text.substring(0, start) + unwrapped + text.substring(end)
                return TextFieldValue(
                    text = newText,
                    selection = TextRange(start, start + unwrapped.length)
                )
            }

            // 1b. Selection is bounded by delimiter immediately outside (e.g. "**" + "hello" + "**")
            if (start >= dLen && end + dLen <= text.length &&
                text.substring(start - dLen, start) == delimiter &&
                text.substring(end, end + dLen) == delimiter
            ) {
                val newText = text.substring(0, start - dLen) + selectedText + text.substring(end + dLen)
                return TextFieldValue(
                    text = newText,
                    selection = TextRange(start - dLen, end - dLen)
                )
            }

            // 1c. Selection is not wrapped -> Wrap it!
            val wrapped = "$delimiter$selectedText$delimiter"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            return TextFieldValue(
                text = newText,
                selection = TextRange(start + dLen, start + dLen + selectedText.length)
            )
        }

        // Case 2: Cursor collapsed (no selection)
        val cursor = selection.start

        // 2a. Cursor is directly inside the delimiters (e.g. "**|**")
        if (cursor >= dLen && cursor + dLen <= text.length &&
            text.substring(cursor - dLen, cursor) == delimiter &&
            text.substring(cursor, cursor + dLen) == delimiter
        ) {
            val newText = text.substring(0, cursor - dLen) + text.substring(cursor + dLen)
            return TextFieldValue(
                text = newText,
                selection = TextRange(cursor - dLen)
            )
        }

        // 2b. Insert delimiters and place cursor inside
        val newText = text.substring(0, cursor) + delimiter + delimiter + text.substring(cursor)
        return TextFieldValue(
            text = newText,
            selection = TextRange(cursor + dLen)
        )
    }
}
