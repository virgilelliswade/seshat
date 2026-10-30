package com.seshat.writer.tv

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seshat.writer.bundle.SeshatBundleManager
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted

/**
 * Series Binder / Episode Deck pane for Television Series Projects.
 * Provides Scrivener-style episode management:
 * - Switching active episode
 * - Reordering episodes in the season
 * - Adding new episodes with tailored act structures
 * - Quick 1-tap jump to the unified Series Lore Bible
 * - Launching Writers' Room collaboration & bundle exports
 */
@Composable
fun TvEpisodeBinderPane(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    seriesProject: TvSeriesProject,
    onCloseBinder: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onOpenWritersRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddEpisodeDialog by remember { mutableStateOf(false) }
    var episodeToDeleteIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .background(ObsidianSurface)
            .padding(14.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = ElectrumGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SERIES BINDER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = ElectrumGold
                        )
                    )
                    Text(
                        text = "${seriesProject.episodes.size} Episodes • ${if (seriesProject.format == ScreenplayType.TV_HALF_HOUR_COMEDY) "Comedy" else "Drama"}",
                        style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                    )
                }
            }

            IconButton(onClick = onCloseBinder, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Episode Binder",
                    tint = ScreenplayTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Series Title & Showrunner Card
        Surface(
            color = ObsidianBlack,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = seriesProject.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (seriesProject.showrunner.isNotBlank()) {
                    Text(
                        text = "Creator / Showrunner: ${seriesProject.showrunner}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ScreenplayTextMuted,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Persistent Series Lore Bible Action Button
        Surface(
            color = ObsidianSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onOpenStoryBible() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📖",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SERIES LORE BIBLE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA78BFA)
                            )
                        )
                        Text(
                            text = "${uiState.storyBible.characters.size} Dossiers • ${uiState.storyBible.locations.size} Locations",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScreenplayTextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val isRuleZeroLocked = uiState.firstDraftState.isArcadeLocked && !uiState.firstDraftState.tvEpisode101Unlocked

        // Episode List Header + Add Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isRuleZeroLocked) "EPISODE DECK (🔒 RULE ZERO)" else "EPISODE DECK",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isRuleZeroLocked) Color(0xFFFF1E42) else ScreenplayTextMuted
                )
            )

            Surface(
                color = if (isRuleZeroLocked) ObsidianSurfaceVariant else ElectrumGold,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (isRuleZeroLocked) {
                            Toast.makeText(
                                context,
                                "🔒 Rule Zero: Complete Pilot (Ep 101) to STUDIO_UNLOCKED before adding Episode 102+!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            showAddEpisodeDialog = true
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Episode",
                        tint = if (isRuleZeroLocked) ScreenplayTextMuted else ObsidianBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRuleZeroLocked) "🔒 EP 102+" else "NEW EPISODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isRuleZeroLocked) ScreenplayTextMuted else ObsidianBlack,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable Episode Deck Cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(seriesProject.episodes) { index, ep ->
                val isEpLockedByRuleZero = isRuleZeroLocked && index > 0
                val isActive = index == seriesProject.activeEpisodeIndex
                val borderColor = if (isEpLockedByRuleZero) Color(0xFF475569) else if (isActive) ElectrumGold else ObsidianSurfaceVariant
                val containerColor = if (isEpLockedByRuleZero) Color(0xFF090D14) else if (isActive) ElectrumGold.copy(alpha = 0.12f) else ObsidianBlack

                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(if (isActive) 1.5.dp else 1.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (isEpLockedByRuleZero) {
                                Toast.makeText(
                                    context,
                                    "🔒 Rule Zero: Complete Episode 101 (Pilot) to unlock Episode ${ep.episodeNumber}!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                viewModel.selectTvEpisode(context, index)
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isActive) ElectrumGold else ObsidianSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "EP ${ep.episodeNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color(0xFF0F1216) else ElectrumGold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ep.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isActive) ElectrumGold else ScreenplayTextDark
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            // Revision Color Dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(ep.revisionColor.primaryColorHex))
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Stats: Scenes & Pages & Corkboard Outline
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${ep.sceneCount} scenes • ${ep.pageCount} pgs • ${ep.corkboard.actCount} acts",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ScreenplayTextMuted,
                                    fontSize = 11.sp
                                )
                            )

                            // Episode Management Arrows / Delete
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index > 0) {
                                    IconButton(
                                        onClick = { viewModel.reorderTvEpisodes(context, index, index - 1) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = ScreenplayTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                if (index < seriesProject.episodes.size - 1) {
                                    IconButton(
                                        onClick = { viewModel.reorderTvEpisodes(context, index, index + 1) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = ScreenplayTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                if (seriesProject.episodes.size > 1) {
                                    IconButton(
                                        onClick = { episodeToDeleteIndex = index },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Episode",
                                            tint = ScreenplayTextMuted.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = ObsidianSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Collaboration & Export Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onOpenWritersRoom() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(text = "👥", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WRITERS' ROOM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val exported = SeshatBundleManager.exportTvSeriesBundle(context, seriesProject)
                        if (exported != null) {
                            Toast.makeText(context, "Exported: ${exported.name}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(text = "📦", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ".SESHAT BUNDLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }

    // Add Episode Dialog
    if (showAddEpisodeDialog) {
        val nextNumber = remember(seriesProject.episodes) {
            val lastEpNum = seriesProject.episodes.lastOrNull()?.episodeNumber?.toIntOrNull()
            if (lastEpNum != null) (lastEpNum + 1).toString() else "${seriesProject.episodes.size + 101}"
        }
        var epNumInput by remember { mutableStateOf(nextNumber) }
        var epTitleInput by remember { mutableStateOf("Episode ${seriesProject.episodes.size + 1}") }
        var writerInput by remember { mutableStateOf(seriesProject.showrunner) }

        Dialog(onDismissRequest = { showAddEpisodeDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ADD NEW EPISODE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Adds a new episode with tailored act outline to '${seriesProject.title}'",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = epNumInput,
                        onValueChange = { epNumInput = it },
                        label = { Text("Episode Number (e.g. 102)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectrumGold,
                            unfocusedBorderColor = ObsidianSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = epTitleInput,
                        onValueChange = { epTitleInput = it },
                        label = { Text("Episode Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectrumGold,
                            unfocusedBorderColor = ObsidianSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = writerInput,
                        onValueChange = { writerInput = it },
                        label = { Text("Writer Credit") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectrumGold,
                            unfocusedBorderColor = ObsidianSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showAddEpisodeDialog = false }) {
                            Text("Cancel", color = ScreenplayTextMuted)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (epTitleInput.isNotBlank()) {
                                    viewModel.addTvEpisode(
                                        context = context,
                                        episodeNumber = epNumInput.ifBlank { "102" },
                                        episodeTitle = epTitleInput.trim(),
                                        writerCredit = writerInput.trim()
                                    )
                                    showAddEpisodeDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold)
                        ) {
                            Text("Create Episode", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (episodeToDeleteIndex != null) {
        val targetIdx = episodeToDeleteIndex!!
        val ep = seriesProject.episodes.getOrNull(targetIdx)
        if (ep != null) {
            Dialog(onDismissRequest = { episodeToDeleteIndex = null }) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "DELETE EPISODE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Are you sure you want to delete Ep ${ep.episodeNumber}: '${ep.title}'? This action cannot be undone.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = { episodeToDeleteIndex = null }) {
                                Text("Cancel", color = ScreenplayTextMuted)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    viewModel.deleteTvEpisode(context, targetIdx)
                                    episodeToDeleteIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
