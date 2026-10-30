package com.seshat.writer.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextMuted

@Composable
fun ShortcutsDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(560.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)), RoundedCornerShape(12.dp)),
            color = ObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KEYBOARD SHORTCUTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ElectrumGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Text(
                            text = "macOS Workstation Reference",
                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ShortcutSection("Screenplay Block Formatting", listOf(
                        "⌘1" to "Scene Heading (Slugline)",
                        "⌘2" to "Action",
                        "⌘3" to "Character Name",
                        "⌘4" to "Parenthetical",
                        "⌘5" to "Dialogue",
                        "⌘6" to "Transition",
                        "Tab" to "Cycle block types intelligently",
                        "Enter" to "Contextual next block creation"
                    ))

                    ShortcutSection("File & Project", listOf(
                        "⌘S" to "Save Project to Local Storage",
                        "⌘E" to "Export Script (PDF / FDX / Fountain)",
                        "⌘O" to "Open Script Library",
                        "⌘N" to "New Project / Start Menu",
                        "⇧⌘T" to "Title Page Setup"
                    ))

                    ShortcutSection("Studio & Creative Tools", listOf(
                        "⌘B" to "Open Story Bible & World Building",
                        "⇧⌘O" to "Toggle Corkboard / Index Cards",
                        "⌘J" to "AI Script Doctor & Analysis",
                        "⇧⌘R" to "Table Read (macOS Voice Synthesis)",
                        "⌘D / F10" to "Voice Dictation Mode",
                        "⌘F" to "Find in Script",
                        "⌥⌘F" to "Find & Replace"
                    ))

                    ShortcutSection("MacBook Keyboard Navigation", listOf(
                        "⌘ ↑" to "Jump to Beginning of Script",
                        "⌘ ↓" to "Jump to End of Script",
                        "⌥ ↑" to "Previous Scene Heading",
                        "⌥ ↓" to "Next Scene Heading",
                        "Fn ↑ / Page Up" to "Page Up (~10 blocks)",
                        "Fn ↓ / Page Down" to "Page Down (~10 blocks)",
                        "↑ / ↓" to "Navigate between blocks & lines",
                        "← / →" to "Navigate across characters / blocks",
                        "⇧ ↑ / ⇧ ↓" to "Expand block selection range"
                    ))

                    ShortcutSection("View & Zoom", listOf(
                        "⌘ +" to "Zoom In Canvas",
                        "⌘ -" to "Zoom Out Canvas",
                        "⌘ 0" to "Reset Zoom to 100%",
                        "F11" to "Toggle Zen Mode (Distraction-Free)"
                    ))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ShortcutSection(title: String, shortcuts: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = ElectrumGold.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianBlack, RoundedCornerShape(8.dp))
                .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                shortcuts.forEach { (key, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 12.sp)
                        )
                        Box(
                            modifier = Modifier
                                .background(ObsidianSurfaceVariant, RoundedCornerShape(4.dp))
                                .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectrumGold,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
