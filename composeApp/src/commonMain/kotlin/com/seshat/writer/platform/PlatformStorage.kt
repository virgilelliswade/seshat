package com.seshat.writer.platform

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

interface PlatformStorage {
    fun getFilesDir(): File
    fun getCacheDir(): File
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getFloat(key: String, defaultValue: Float = 0f): Float
    fun putFloat(key: String, value: Float)
    fun getInt(key: String, defaultValue: Int = 0): Int
    fun putInt(key: String, value: Int)
}

/**
 * File-backed properties storage for Desktop (macOS / Linux / Windows).
 * Persists settings directly in ~/Documents/Seshat Writing Studio/settings.properties.
 */
class DesktopPlatformStorage : PlatformStorage {
    private val internalBaseDir: File by lazy {
        val userHome = System.getProperty("user.home") ?: "."
        val dir = File(userHome, "Documents/Seshat Writing Studio")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val internalCacheDir: File by lazy {
        val tmp = System.getProperty("java.io.tmpdir") ?: "."
        val dir = File(tmp, "seshat_cache")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val propsFile: File by lazy {
        File(internalBaseDir, "settings.properties")
    }

    private val properties: Properties = Properties()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        try {
            if (propsFile.exists() && propsFile.canRead()) {
                FileInputStream(propsFile).use { properties.load(it) }
            }
        } catch (_: Throwable) {}
    }

    private fun saveProperties() {
        try {
            FileOutputStream(propsFile).use { properties.store(it, "Seshat Studio Settings") }
        } catch (_: Throwable) {}
    }

    override fun getFilesDir(): File = internalBaseDir

    override fun getCacheDir(): File = internalCacheDir

    override fun getString(key: String, defaultValue: String): String {
        return properties.getProperty(key, defaultValue)
    }

    override fun putString(key: String, value: String) {
        properties.setProperty(key, value)
        saveProperties()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val v = properties.getProperty(key) ?: return defaultValue
        return v.toBooleanStrictOrNull() ?: defaultValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        val v = properties.getProperty(key) ?: return defaultValue
        return v.toFloatOrNull() ?: defaultValue
    }

    override fun putFloat(key: String, value: Float) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val v = properties.getProperty(key) ?: return defaultValue
        return v.toIntOrNull() ?: defaultValue
    }

    override fun putInt(key: String, value: Int) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }
}

/**
 * Global application platform registry providing uniform file storage and toast services.
 */
object AppPlatform {
    var storage: PlatformStorage = DesktopPlatformStorage()
    var toastHandler: ((String) -> Unit)? = null

    val isMac: Boolean
        get() = System.getProperty("os.name")?.lowercase()?.contains("mac") == true

    fun showToast(message: String) {
        val handler = toastHandler
        if (handler != null) {
            handler.invoke(message)
        } else {
            println("[Seshat Toast] $message")
        }
    }
}
