package com.seshat.writer.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
fun rememberAudioPermissionLauncher(onPermissionResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )
    return {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onPermissionResult(true)
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

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

    fun isMacBook(): Boolean = false
}
