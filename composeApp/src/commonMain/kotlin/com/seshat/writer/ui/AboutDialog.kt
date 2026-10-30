package com.seshat.writer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.R
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextMuted

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(460.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)), RoundedCornerShape(12.dp)),
            color = ObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.seshat_emblem_logo),
                    contentDescription = "Seshat Emblem",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SESHAT WRITING STUDIO",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ElectrumGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )

                Text(
                    text = "Version 2.0.0 • Studio & First Draft Engine",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianBlack, RoundedCornerShape(8.dp))
                        .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Engineered for professional screenwriters, novelists, and showrunners.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 12.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Hollywood-Standard Courier Pagination Engine\n• Final Draft (.fdx) & Fountain Bi-Directional Export\n• Native macOS Speech Synthesis Table Read\n• AI Script Doctor & Coverage Studio\n• Novel Compiler & Story World Binder",
                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp, lineHeight = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Created by ScribeStar Media, LLC\nAll Rights Reserved © 2026",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextMuted.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
