package com.seshat.writer.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build

object DeviceEnvironment {

    /**
     * Detects if the app is running in ChromeOS / Googlebook environment
     * or on a device with a full physical hardware keyboard.
     */
    fun isGooglebook(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            val isChromeOS = (pm?.hasSystemFeature("org.chromium.arc") == true) ||
                    (pm?.hasSystemFeature("org.chromium.arc.device_management") == true) ||
                    Build.BRAND?.equals("chromium", ignoreCase = true) == true ||
                    Build.DEVICE?.contains("cheets", ignoreCase = true) == true ||
                    (Build.MANUFACTURER?.equals("Google", ignoreCase = true) == true && Build.PRODUCT?.contains("arc", ignoreCase = true) == true)

            val keyboard = context.resources?.configuration?.keyboard
            val hasPhysicalKeyboard = keyboard == Configuration.KEYBOARD_QWERTY

            isChromeOS || hasPhysicalKeyboard
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks specifically for ChromeOS / Googlebook ARC container.
     */
    fun isChromeOS(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            (pm?.hasSystemFeature("org.chromium.arc") == true) ||
                    (pm?.hasSystemFeature("org.chromium.arc.device_management") == true) ||
                    Build.BRAND?.equals("chromium", ignoreCase = true) == true ||
                    Build.DEVICE?.contains("cheets", ignoreCase = true) == true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Human-readable device recognition label for status telemetry.
     */
    fun getDeviceLabel(context: Context): String {
        return if (isGooglebook(context)) "💻 Googlebook • Arrow Nav" else "📱 Touch Screen"
    }

    fun getDeviceNavLabel(context: Context): String = getDeviceLabel(context)
}
