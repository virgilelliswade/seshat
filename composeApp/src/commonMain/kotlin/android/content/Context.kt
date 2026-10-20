package android.content

import java.io.File

open class Context {
    open val filesDir: File
        get() = com.seshat.writer.platform.AppPlatform.storage.getFilesDir()

    open val applicationContext: Context get() = this

    open val cacheDir: File
        get() = com.seshat.writer.platform.AppPlatform.storage.getCacheDir()

    open fun getExternalFilesDir(type: String?): File? = filesDir

    open val contentResolver: ContentResolver get() = ContentResolver()

    open fun getSystemService(name: String): Any? {
        if (name == CLIPBOARD_SERVICE) {
            return ClipboardManager()
        }
        return null
    }

    companion object {
        const val MODE_PRIVATE = 0
        const val CLIPBOARD_SERVICE = "clipboard"
    }
}

class ContentResolver {
    fun openInputStream(uri: android.net.Uri): java.io.InputStream? {
        val path = uri.path ?: return null
        val f = java.io.File(path)
        return if (f.exists()) java.io.FileInputStream(f) else null
    }
}
