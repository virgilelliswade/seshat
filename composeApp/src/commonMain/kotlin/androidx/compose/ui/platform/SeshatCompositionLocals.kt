@file:JvmName("SeshatPlatformCompositionLocalsKt")
package androidx.compose.ui.platform

import androidx.compose.runtime.compositionLocalOf
import android.content.Context

val LocalContext = compositionLocalOf<Context> { Context() }

class Configuration(
    val screenWidthDp: Int = 1280,
    val screenHeightDp: Int = 800,
    val orientation: Int = 1
)

val LocalConfiguration = compositionLocalOf<Configuration> { Configuration() }
