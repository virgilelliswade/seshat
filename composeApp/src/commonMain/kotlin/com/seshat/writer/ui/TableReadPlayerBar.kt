package com.seshat.writer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ai.TableReadManager
import com.seshat.writer.ai.TableReadMode
import com.seshat.writer.ui.theme.DarkGold
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted

@Composable
fun TableReadPlayerBar(
    tableReadManager: TableReadManager,
    characters: List<String>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by tableReadManager.state.collectAsState()
    var isRoleMenuOpen by remember { mutableStateOf(false) }

    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElectrumGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Rehearsal Mode Banner when it's the writer's turn
            AnimatedVisibility(visible = state.isWaitingForUser) {
                Surface(
                    color = ElectrumGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, ElectrumGold, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎭 YOUR TURN TO PERFORM! (Role: ${state.userRoleCharacter})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LightGold
                                )
                            )
                            Text(
                                text = "\"${state.currentSpeakingText}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = ScreenplayTextDark,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            color = ElectrumGold,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { tableReadManager.userFinishedTurn() }
                        ) {
                            Text(
                                text = "DONE / NEXT LINE ⏭️",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Main Player Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎙️",
                        fontSize = 20.sp
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TABLE READ: ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = ElectrumGold
                                )
                            )
                            Text(
                                text = if (state.currentSpeakingCharacter != null) {
                                    "${state.currentSpeakingCharacter}"
                                } else {
                                    "Narrator"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LightGold
                                )
                            )
                            Text(
                                text = " (${state.currentBlockIndex + 1}/${state.totalPlayableBlocks})",
                                style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                            )
                        }
                        Text(
                            text = state.currentSpeakingText.ifBlank { "Ready to read." },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScreenplayTextDark,
                                fontFamily = FontFamily.Monospace
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Middle: Playback controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Previous
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { tableReadManager.skipPrevious() }
                            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "⏮️",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            fontSize = 14.sp
                        )
                    }

                    // Play / Pause
                    Surface(
                        color = ElectrumGold,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                if (state.isPlaying) {
                                    tableReadManager.pause()
                                } else {
                                    tableReadManager.resume()
                                }
                            }
                    ) {
                        Text(
                            text = if (state.isPlaying) "⏸️ PAUSE" else "▶️ PLAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    // Next
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { tableReadManager.skipNext() }
                            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "⏭️",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right: Speed, Mode and Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mode dropdown button
                    Box {
                        Surface(
                            color = if (state.mode == TableReadMode.REHEARSAL_SPARRING) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isRoleMenuOpen = true }
                                .border(
                                    1.dp,
                                    if (state.mode == TableReadMode.REHEARSAL_SPARRING) ElectrumGold else ObsidianSurfaceVariant,
                                    RoundedCornerShape(6.dp)
                                )
                        ) {
                            Text(
                                text = if (state.mode == TableReadMode.REHEARSAL_SPARRING) {
                                    "🎭 Role: ${state.userRoleCharacter ?: "Select"}"
                                } else {
                                    "👥 Full Table Read"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (state.mode == TableReadMode.REHEARSAL_SPARRING) ElectrumGold else ScreenplayTextDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isRoleMenuOpen,
                            onDismissRequest = { isRoleMenuOpen = false },
                            modifier = Modifier.background(ObsidianSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("👥 Full Table Read (All Characters)", color = ScreenplayTextDark) },
                                onClick = {
                                    tableReadManager.startReading(
                                        blocks = emptyList(), // keeps active
                                        mode = TableReadMode.FULL_TABLE_READ
                                    )
                                    isRoleMenuOpen = false
                                }
                            )
                            if (characters.isNotEmpty()) {
                                for (char in characters) {
                                    DropdownMenuItem(
                                        text = { Text("🎭 Rehearse as $char", color = ElectrumGold) },
                                        onClick = {
                                            tableReadManager.startReading(
                                                blocks = emptyList(),
                                                mode = TableReadMode.REHEARSAL_SPARRING,
                                                userRole = char
                                            )
                                            isRoleMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Speed pills
                    val speeds = listOf(0.85f, 1.0f, 1.25f)
                    for (sp in speeds) {
                        val isSel = state.speechRate == sp
                        Surface(
                            color = if (isSel) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { tableReadManager.setSpeechRate(sp) }
                                .border(1.dp, if (isSel) ElectrumGold else ObsidianSurfaceVariant, RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = "${sp}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = if (isSel) ElectrumGold else ScreenplayTextMuted
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(onClick = {
                        tableReadManager.stop()
                        onClose()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Table Read",
                            tint = ScreenplayTextMuted
                        )
                    }
                }
            }
        }
    }
}
