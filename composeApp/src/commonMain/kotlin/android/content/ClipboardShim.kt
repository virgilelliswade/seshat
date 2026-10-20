package android.content

class ClipData(val text: CharSequence) {
    companion object {
        fun newPlainText(label: CharSequence?, text: CharSequence?): ClipData {
            return ClipData(text ?: "")
        }
    }
}

class ClipboardManager {
    fun setPrimaryClip(clip: ClipData) {
        val str = clip.text.toString()
        try {
            val selection = java.awt.datatransfer.StringSelection(str)
            java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        } catch (_: Throwable) {
            // Non-desktop/headless fallback
        }
    }

    fun getText(): CharSequence? {
        return try {
            val clip = java.awt.Toolkit.getDefaultToolkit().systemClipboard
            clip.getData(java.awt.datatransfer.DataFlavor.stringFlavor) as? String
        } catch (_: Throwable) {
            null
        }
    }
}
