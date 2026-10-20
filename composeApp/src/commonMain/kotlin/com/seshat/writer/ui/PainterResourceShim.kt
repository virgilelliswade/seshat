package com.seshat.writer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.seshat.writer.R

@Composable
fun painterResource(id: Int): Painter {
    val path = when (id) {
        R.drawable.seshat_emblem_logo -> "seshat_emblem_logo.png"
        R.drawable.seshat_neon_splash -> "seshat_neon_splash.jpg"
        R.drawable.seshat_logo -> "seshat_logo.jpg"
        R.drawable.seshat_quill_icon -> "seshat_quill_icon.png"
        R.drawable.seshat_quill_star_medallion -> "seshat_quill_star_medallion.png"
        R.drawable.scribestar_media_logo -> "scribestar_media_logo.jpg"
        R.drawable.seshat_studio_splash -> "seshat_studio_splash.jpg"
        R.drawable.seshat_light_splash -> "seshat_light_splash.png"
        R.drawable.ic_launcher_foreground -> "ic_launcher_foreground.png"
        else -> "seshat_emblem_logo.png"
    }
    return androidx.compose.ui.res.painterResource(path)
}
