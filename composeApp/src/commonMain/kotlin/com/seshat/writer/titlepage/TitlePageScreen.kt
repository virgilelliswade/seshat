package com.seshat.writer.titlepage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.DeskBackground
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily

@Composable
fun TitlePageScreen(
    viewModel: TitlePageViewModel,
    onCloseWindow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titlePageState by viewModel.titlePageState.collectAsState()
    val scrollState = rememberScrollState()
    val chipScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeskBackground)
    ) {
        // Desktop Top Toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Title Page",
                    tint = ElectrumGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TITLE PAGE CREATOR",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = ElectrumGold
                        )
                    )
                    Text(
                        text = "US Letter 8.5\" × 11\" • Hollywood Courier Standard",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextMuted
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Done / Close Window Button
                Surface(
                    color = ObsidianSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clickable(onClick = onCloseWindow)
                        .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ElectrumGold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Window",
                            tint = ElectrumGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Quick Preset Action Chips Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianBlack)
                .horizontalScroll(chipScrollState)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "PRESETS:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ScreenplayTextMuted
                )
            )

            PresetChip(label = "written by", onClick = { viewModel.updateWrittenByLabel("written by") })
            PresetChip(label = "by", onClick = { viewModel.updateWrittenByLabel("by") })
            PresetChip(label = "Screenplay by", onClick = { viewModel.updateWrittenByLabel("Screenplay by") })
            PresetChip(label = "Story by", onClick = { viewModel.updateWrittenByLabel("Story by") })

            Spacer(modifier = Modifier.width(4.dp))
            PresetChip(
                label = "+ Today's Date",
                icon = Icons.Default.DateRange,
                onClick = { viewModel.insertCurrentDateToDraft() }
            )
            PresetChip(label = "First Draft", onClick = { viewModel.setDraftPreset("FIRST DRAFT") })
            PresetChip(label = "Shooting Script", onClick = { viewModel.setDraftPreset("SHOOTING SCRIPT") })
            PresetChip(label = "Revised Draft", onClick = { viewModel.setDraftPreset("REVISED DRAFT") })
        }

        // Paper Workspace (Scrollable US Letter canvas)
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF7)),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier
                    .widthIn(min = 640.dp, max = 740.dp)
                    .fillMaxWidth(0.95f)
                    .defaultMinSize(minHeight = 920.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(3.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 52.dp, vertical = 48.dp)
                ) {
                    // Top spacing (pushes Title ~3.5 inches down)
                    Spacer(modifier = Modifier.height(160.dp))

                    // Title Field (Centered, Bold, All-Caps Courier)
                    TitlePaperField(
                        value = titlePageState.title,
                        placeholder = "SCRIPT TITLE",
                        textStyle = TextStyle(
                            fontFamily = ScreenplayCourierFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF111111),
                            letterSpacing = 1.sp
                        ),
                        onValueChange = { viewModel.updateTitle(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // "written by" / "by" Field
                    TitlePaperField(
                        value = titlePageState.writtenByLabel,
                        placeholder = "written by",
                        textStyle = TextStyle(
                            fontFamily = ScreenplayCourierFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF2B2B2B)
                        ),
                        onValueChange = { viewModel.updateWrittenByLabel(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Author Field
                    TitlePaperField(
                        value = titlePageState.author,
                        placeholder = "Author Name",
                        textStyle = TextStyle(
                            fontFamily = ScreenplayCourierFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF111111)
                        ),
                        onValueChange = { viewModel.updateAuthor(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Optional Source Material Field ("Based on...")
                    TitlePaperField(
                        value = titlePageState.sourceMaterial,
                        placeholder = "(Optional: Based on...)",
                        textStyle = TextStyle(
                            fontFamily = ScreenplayCourierFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF555555)
                        ),
                        onValueChange = { viewModel.updateSourceMaterial(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Flexible middle spacing pushing footer to bottom of US Letter page
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(120.dp))

                    // Bottom Row: Contact Info (Left) & Draft/Revision Info (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Contact Info (Bottom Left)
                        Column(modifier = Modifier.width(260.dp)) {
                            Text(
                                text = "CONTACT",
                                style = TextStyle(
                                    fontFamily = ScreenplayCourierFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF888888),
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TitlePaperField(
                                value = titlePageState.contactInfo,
                                placeholder = "Writer / Representative\nAddress\nPhone / Email\nWGA Registration #",
                                singleLine = false,
                                textStyle = TextStyle(
                                    fontFamily = ScreenplayCourierFontFamily,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.Start,
                                    color = Color(0xFF222222)
                                ),
                                onValueChange = { viewModel.updateContactInfo(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Draft & Date Info (Bottom Right)
                        Column(
                            modifier = Modifier.width(260.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "DRAFT / REVISION",
                                style = TextStyle(
                                    fontFamily = ScreenplayCourierFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF888888),
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.End
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TitlePaperField(
                                value = titlePageState.draftInfo,
                                placeholder = "Draft Date\nCurrent Revision\nDraft Status",
                                singleLine = false,
                                textStyle = TextStyle(
                                    fontFamily = ScreenplayCourierFontFamily,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.End,
                                    color = Color(0xFF222222)
                                ),
                                onValueChange = { viewModel.updateDraftInfo(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TitlePaperField(
    value: String,
    placeholder: String,
    textStyle: TextStyle,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = singleLine,
        cursorBrush = SolidColor(ElectrumGold),
        modifier = modifier
            .background(Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = when (textStyle.textAlign) {
                    TextAlign.Center -> Alignment.Center
                    TextAlign.End -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(
                            color = Color(0xFFB0B0A8)
                        ),
                        textAlign = textStyle.textAlign
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        color = ObsidianSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LightGold,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = ScreenplayTextDark
                )
            )
        }
    }
}
