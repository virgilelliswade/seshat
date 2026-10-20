package com.seshat.writer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.corkboard.CorkboardTemplate
import com.seshat.writer.corkboard.CorkboardTemplateRegistry
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder

/**
 * Floating optical glass dialog allowing writers to choose storytelling beat sheet templates
 * (e.g. Save the Cat, Hero's Journey, 3-Act, Story Circle) for the Corkboard Outline.
 */
@Composable
fun CorkboardTemplateDialog(
    onSelectTemplate: (CorkboardTemplate) -> Unit,
    onClearBoard: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalSeshatDarkTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(ambientWorkspaceBrush(isDark))
                .border(prismaticGlassBorder(width = 1.2.dp), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📋 CORKBOARD BEAT TEMPLATES",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = MainAppFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Populate your outline swimlanes with structured beat cards to fill in without script clutter.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScreenplayTextMuted,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    // Floating Circular Dismiss Pill
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(opticalPillBrush())
                            .border(prismaticGlassBorder(width = 1.dp), CircleShape)
                            .size(38.dp)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(38.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ScribeStarNeonRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable List of Templates
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CorkboardTemplateRegistry.ALL_TEMPLATES, key = { it.id }) { template ->
                        TemplateCardItem(
                            template = template,
                            isDark = isDark,
                            onClick = {
                                onSelectTemplate(template)
                                onDismiss()
                            }
                        )
                    }

                    // Clear / Blank Corkboard option
                    item(key = "clear_blank") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) ObsidianSurfaceVariant.copy(alpha = 0.5f) else Color(0xFFE2E8F0),
                            border = BorderStroke(1.dp, ScreenplayTextMuted.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClearBoard()
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📄", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "BLANK CORKBOARD",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = MainAppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = ScreenplayTextMuted,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                    Text(
                                        text = "Clear all beat cards and outline freely from scratch.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ScreenplayTextMuted.copy(alpha = 0.8f),
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
    }
}

@Composable
private fun TemplateCardItem(
    template: CorkboardTemplate,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, template.badgeColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(opticalPillBrush())
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = template.badgeColor.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, template.badgeColor.copy(alpha = 0.6f)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = template.badgeIcon, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = template.name.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = MainAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            letterSpacing = 0.8.sp
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = template.badgeColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, template.badgeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${template.actCount} Acts • ${template.beats.size} Beats",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = template.badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = template.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ElectrumGold,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}
