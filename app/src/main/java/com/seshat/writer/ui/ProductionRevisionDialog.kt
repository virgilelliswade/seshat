package com.seshat.writer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductionRevisionDialog(
    viewModel: ScreenplayViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var revisionDateText by remember { mutableStateOf(uiState.revisionDate) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ObsidianSurface,
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(uiState.currentRevisionColor.composeColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎬 PRODUCTION REVISIONS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ElectrumGold
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ScreenplayTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Revision Tracking Row
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Track Script Revisions",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isRevisionTrackingEnabled) ElectrumGold else ScreenplayTextDark
                                )
                            )
                            Text(
                                text = if (uiState.isRevisionTrackingEnabled) "Edits are automatically stamped with ${uiState.currentRevisionColor.draftTag} & marginal asterisks (*)" else "Revision tracking is inactive",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = ScreenplayTextMuted
                                )
                            )
                        }

                        Switch(
                            checked = uiState.isRevisionTrackingEnabled,
                            onCheckedChange = { viewModel.setRevisionTrackingEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElectrumGold,
                                checkedTrackColor = ElectrumGold.copy(alpha = 0.4f),
                                uncheckedThumbColor = ScreenplayTextMuted,
                                uncheckedTrackColor = ObsidianSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hollywood Standard Draft Color Selector
                Text(
                    text = "HOLLYWOOD DRAFT COLOR PALETTE:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = ElectrumGold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RevisionColor.entries.forEach { revColor ->
                        val isSelected = uiState.currentRevisionColor == revColor
                        Surface(
                            color = if (isSelected) revColor.composeColor.copy(alpha = 0.25f) else ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.setCurrentRevisionColor(revColor)
                                }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) revColor.composeColor else ObsidianSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(revColor.composeColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = revColor.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) revColor.composeColor else ScreenplayTextDark
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Revision Date & Colored Page Tint Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Revision Date",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ScreenplayTextMuted
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicTextField(
                            value = revisionDateText,
                            onValueChange = {
                                revisionDateText = it
                                viewModel.setRevisionDate(it)
                            },
                            textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 12.sp),
                            cursorBrush = SolidColor(ElectrumGold),
                            singleLine = true,
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ObsidianBlack, RoundedCornerShape(6.dp))
                                        .border(0.5.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    inner()
                                }
                            }
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleColoredDraftTint() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = uiState.showColoredDraftTint,
                                onCheckedChange = { viewModel.setColoredDraftTint(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ElectrumGold,
                                    uncheckedColor = ScreenplayTextMuted
                                )
                            )
                            Text(
                                text = "Tint Canvas Pages",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (uiState.showColoredDraftTint) ElectrumGold else ScreenplayTextDark
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ObsidianSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Scene Numbering & Continuity (Shooting Script Mode)
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔒 Lock Scene Numbers",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isSceneNumberingLocked) ElectrumGold else ScreenplayTextDark
                                    )
                                )
                                Text(
                                    text = if (uiState.isSceneNumberingLocked) "Shooting script mode: scene numbers are frozen. New scenes receive 12A, 12B or A1." else "Spec mode: scenes are numbered dynamically 1..N.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = ScreenplayTextMuted
                                    )
                                )
                            }

                            Switch(
                                checked = uiState.isSceneNumberingLocked,
                                onCheckedChange = { locked ->
                                    if (locked) {
                                        viewModel.lockSceneNumbers()
                                    } else {
                                        viewModel.unlockSceneNumbers()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ElectrumGold,
                                    checkedTrackColor = ElectrumGold.copy(alpha = 0.4f),
                                    uncheckedThumbColor = ScreenplayTextMuted,
                                    uncheckedTrackColor = ObsidianSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Continuity: ${if (uiState.isSceneNumberingLocked) "LOCKED (${uiState.lockedSceneNumbers.size} scenes)" else "DYNAMIC (Sequential)"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = if (uiState.isSceneNumberingLocked) ElectrumGold else ScreenplayTextMuted
                                )
                            )

                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        viewModel.renumberAllScenes()
                                    }
                                    .border(0.5.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = "Renumber All Scenes (1..N)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectrumGold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ObsidianSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // Stats & Batch Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Revised Blocks: ${uiState.revisedBlocksCount} of ${uiState.blocks.size}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.revisedBlocksCount > 0) ElectrumGold else ScreenplayTextMuted
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.markAllBlocksRevised(true)
                                }
                                .border(0.5.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = "Mark All Revised",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = ElectrumGold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.markAllBlocksRevised(false)
                                }
                                .border(0.5.dp, ScreenplayTextMuted.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = "Clear Marks",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = ScreenplayTextMuted
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
