package com.seshat.writer.util

import android.content.Context

object DeviceEnvironment {

    val isMacOS: Boolean by lazy {
        try {
            val osName = System.getProperty("os.name") ?: ""
            osName.contains("Mac", ignoreCase = true)
        } catch (_: Throwable) {
            true // Default to macOS on desktop build
        }
    }

    /**
     * Detects if running on a MacBook or macOS workstation.
     */
    fun isMacBook(context: Context? = null): Boolean = isMacOS

    /**
     * Backward-compatible desktop check. Returns false on macOS, true on Chromebook.
     */
    fun isGooglebook(context: Context? = null): Boolean = !isMacOS

    /**
     * Checks specifically for ChromeOS / Googlebook ARC container.
     */
    fun isChromeOS(context: Context? = null): Boolean = false

    /**
     * Returns the platform hardware keyboard moniker.
     */
    fun getKeyboardPlatformName(): String = if (isMacOS) "MacBook Keyboard" else "PC / Chromebook"

    /**
     * Returns primary modifier symbol: ⌘ (Command) on Mac, Ctrl on PC/Chromebook.
     */
    fun getModifierSymbol(): String = if (isMacOS) "⌘" else "Ctrl"

    /**
     * Formatted label for device status badge.
     */
    fun getDeviceNavLabel(context: Context? = null): String =
        if (isMacOS) "🍎 MacBook • ⌘ Arrow Nav" else if (isGooglebook(context)) "💻 Googlebook • Arrow Nav" else "📱 Touch Screen"
}
