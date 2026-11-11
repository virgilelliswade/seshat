package com.seshat.writer.universe

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.floatingGlassIsland
import com.seshat.writer.ui.theme.opticalGlassBrush
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder

/**
 * Binder pane for Blended Universe projects.
 * Houses multi-medium Writing Assignments (Film, TV, Novel, Comic Book, Stage Play)
 * all sharing a single Master Story Bible.
 */
@Composable
fun UniverseBinderPane(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    universeProject: BlendedUniverseProject,
    onCloseBinder: () -> Unit,
    onOpenStoryBible: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalSeshatDarkTheme.current
    var showAddAssignmentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(ObsidianSurface)
            .padding(14.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🪐 UNIVERSE BINDER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = ScribeStarNeonRed
                    )
                )
                Text(
                    text = "${universeProject.assignments.size} Writing Assignments · Shared Lore",
                    style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                )
            }
            IconButton(onClick = onCloseBinder, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Universe Binder",
                    tint = ScreenplayTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Universe Name Card
        Surface(
            color = ObsidianBlack,
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = universeProject.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ScreenplayTextDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Blended Multi-Medium Universe",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Shared Master Story Bible Jump Button
        Surface(
            color = ObsidianSurfaceVariant,
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(0.dp))
                .clickable { onOpenStoryBible() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📖", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MASTER LORE BIBLE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ScribeStarNeonRed
                            )
                        )
                        Text(
                            text = "${uiState.storyBible.characters.size} Characters · ${uiState.storyBible.locations.size} Locations",
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
                    tint = ScribeStarNeonRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Assignments List Header + "+ New Assignment" CTA
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "WRITING ASSIGNMENTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = ScreenplayTextMuted
                )
            )

            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(0.dp))
                    .clip(RoundedCornerShape(0.dp))
                    .background(opticalPillBrush(isPrimary = true, isDark = isDark))
                    .border(prismaticGlassBorder(width = 1.dp, isLit = true, isDark = isDark), RoundedCornerShape(0.dp))
                    .clickable { showAddAssignmentDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Writing Assignment",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEW ASSIGNMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(universeProject.assignments) { idx, assignment ->
                val isActive = idx == universeProject.activeAssignmentIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isActive) 8.dp else 4.dp, RoundedCornerShape(0.dp))
                        .clip(RoundedCornerShape(0.dp))
                        .background(
                            if (isActive) opticalPillBrush(isLit = true, isDark = isDark)
                            else opticalGlassBrush(glassOpacity = if (isDark) 0.85f else 0.92f)
                        )
                        .border(
                            prismaticGlassBorder(
                                width = if (isActive) 1.5.dp else 1.dp,
                                isLit = isActive,
                                isDark = isDark
                            ),
                            RoundedCornerShape(0.dp)
                        )
                        .clickable { viewModel.selectBlendedAssignment(context, idx) }
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = if (isActive) Color.White.copy(alpha = 0.18f) else ObsidianSurfaceVariant,
                                shape = RoundedCornerShape(0.dp)
                            ) {
                                Text(
                                    text = assignment.mediumBadge(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else ScreenplayTextMuted,
                                        fontSize = 9.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = assignment.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isActive) Color.White else ScreenplayTextDark,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddAssignmentDialog) {
        NewBlendedAssignmentDialog(
            onDismiss = { showAddAssignmentDialog = false },
            onCreateAssignment = { title, format, screenplayType ->
                showAddAssignmentDialog = false
                viewModel.addBlendedAssignment(context, title, format, screenplayType)
            }
        )
    }
}

@Composable
fun NewBlendedAssignmentDialog(
    onDismiss: () -> Unit,
    onCreateAssignment: (String, ProjectFormat, ScreenplayType) -> Unit
) {
    val isDark = LocalSeshatDarkTheme.current
    var titleInput by remember { mutableStateOf("") }
    var chosenFormat by remember { mutableStateOf(ProjectFormat.SCREENPLAY) }
    var chosenScreenplayType by remember { mutableStateOf(ScreenplayType.FEATURE) }

    val mediumChoices = listOf(
        Triple("🎬 Film (Feature)", ProjectFormat.SCREENPLAY, ScreenplayType.FEATURE),
        Triple("📺 TV Episode / Pilot", ProjectFormat.TV_SERIES, ScreenplayType.TV_HOUR_DRAMA),
        Triple("📖 Novel / Book", ProjectFormat.NOVEL, ScreenplayType.FEATURE),
        Triple("💥 Comic Book Issue", ProjectFormat.COMIC_BOOK, ScreenplayType.FEATURE),
        Triple("🎭 Stage Play", ProjectFormat.STAGE_PLAY, ScreenplayType.FEATURE)
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .floatingGlassIsland(isDark = isDark, elevation = 18.dp, isHighlighted = true)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "🪐 NEW BLENDED UNIVERSE ASSIGNMENT",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ScribeStarNeonRed,
                        letterSpacing = 0.6.sp
                    )
                )
                Text(
                    text = "Pick any medium for this installment—it shares your Master Story Bible automatically.",
                    style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                )

                // Medium Choice Buttons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    mediumChoices.forEach { (label, fmt, sType) ->
                        val isSelected = chosenFormat == fmt
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(0.dp))
                                .background(
                                    if (isSelected) opticalPillBrush(isPrimary = true, isDark = isDark)
                                    else opticalGlassBrush(glassOpacity = if (isDark) 0.84f else 0.92f)
                                )
                                .border(
                                    prismaticGlassBorder(width = 1.dp, isLit = isSelected, isDark = isDark),
                                    RoundedCornerShape(0.dp)
                                )
                                .clickable {
                                    chosenFormat = fmt
                                    chosenScreenplayType = sType
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else ScreenplayTextDark
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Assignment Title", color = ScreenplayTextMuted) },
                    placeholder = { Text("e.g. Issue #1 / Episode 101 / Book 2", color = ScreenplayTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScribeStarNeonRed,
                        unfocusedBorderColor = ObsidianSurfaceVariant,
                        focusedTextColor = ScreenplayTextDark,
                        unfocusedTextColor = ScreenplayTextDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Cancel", color = ScreenplayTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTitle = titleInput.trim().ifBlank {
                                when (chosenFormat) {
                                    ProjectFormat.SCREENPLAY -> "Film Feature"
                                    ProjectFormat.TV_SERIES -> "TV Pilot"
                                    ProjectFormat.NOVEL -> "Novel Volume"
                                    ProjectFormat.COMIC_BOOK -> "Comic Issue"
                                    ProjectFormat.STAGE_PLAY -> "Stage Play"
                                }
                            }
                            onCreateAssignment(finalTitle, chosenFormat, chosenScreenplayType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Create Assignment", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
