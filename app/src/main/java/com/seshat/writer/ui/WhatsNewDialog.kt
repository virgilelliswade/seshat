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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.R
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.StudioMonoFontFamily

private data class WhatsNewHighlight(
    val badge: String,
    val tag: String,
    val title: String,
    val description: String,
    val accentColor: Color
)

private val v28Highlights = listOf(
    WhatsNewHighlight(
        badge = "🌌",
        tag = "CHARACTER STELLARIUM",
        title = "Character Builder 2.0 & Archetype Constellations",
        description = "Expanded character dossiers now track Age, Gender, Physical Description, Wants, Needs, and Shard of Glass—plus the interactive Character Stellarium where you combine 2–6 Archetype Stars (Light, Dual, or Shadow) across 8 domains into a glowing character constellation.",
        accentColor = ElectrumGold
    ),
    WhatsNewHighlight(
        badge = "☁️",
        tag = "CLOUD SYNC MIRROR",
        title = "Zero-Server iCloud, Google Drive & Dropbox Mirroring",
        description = "Choose any Cloud Sync Folder in Studio Settings. Active drafts stay fast and offline-safe locally, then mirror atomically (.tmp → .seshat) on Save or after 25s idle—plus a 1-tap 'Save Backup Copy' option for Android Google Drive.",
        accentColor = ScribeStarNeonRed
    ),
    WhatsNewHighlight(
        badge = "☀️",
        tag = "LIGHT MODE POLISH",
        title = "Crisp Sidebar Close Controls & 'SCENES' Header",
        description = "Fixed invisible white close ('X') icons and header labels in Light Mode across the Screenplay, Novel, TV, and Universe sidebars, and renamed 'BINDER ⌵' to 'SCENES ⌵' to match the top bar.",
        accentColor = ElectrumGold
    ),
    WhatsNewHighlight(
        badge = "🪐",
        tag = "UNIVERSE ENGINE",
        title = "Blended Universe & Expanded Worldbuilding",
        description = "Create A Universe now offers Cinematic, Television, Novel, and Blended Universes in both Standard and Writing Challenge modes. Blended Universes unite Film scripts, TV episodes, Novels, Comics, and Stage Plays under one Master Story Bible with an instant Assignment Switcher binder.",
        accentColor = ElectrumGold
    ),
    WhatsNewHighlight(
        badge = "🏆",
        tag = "WRITING CHALLENGES",
        title = "Writing Challenges & Interactive Rules Guide",
        description = "Genre Campaigns are now Writing Challenges (Screenplay Challenge branching to Film vs TV, Novel, Comic Book, Stage Play, and Universe Challenge). Hover or tap the Writing Challenges header (ⓘ) for the full breakdown of timed airlocks, 48h quotas, the AI Bouncer, and XP loot.",
        accentColor = ElectrumGold
    ),
    WhatsNewHighlight(
        badge = "🗝️",
        tag = "GUIDED AI BYOK",
        title = "Zero-Account AI Key Links & Ollama Quick-Setup",
        description = "Use your own AI key or local models with no cloud login required: 1-tap 'Get Free Key ↗' links for Gemini, OpenAI, and Anthropic, one-click 'Paste Key' with live prefix validation, and a guided Local AI (Ollama) setup card.",
        accentColor = ScribeStarNeonRed
    ),
    WhatsNewHighlight(
        badge = "🧰",
        tag = "WORKSPACE POLISH",
        title = "Collapsible Top Studio Tools Drawer",
        description = "Both Screenplay and Novel top bars now keep essentials upfront and group secondary tools (Doctor, Compile, View Mode, Zoom, Scene #s, Notes, Find, Sprint, Typewriter, Scraps) inside an expandable '🧰 Tools ▾' tray so unmaximized windows stay clean without horizontal scrolling.",
        accentColor = ScribeStarNeonRed
    ),
    WhatsNewHighlight(
        badge = "🎬",
        tag = "MENU REFINEMENT",
        title = "Streamlined Splash Navigation & Clean Library Exit",
        description = "Splash columns now follow Screenplay (Film vs TV) → Novel → Comic Book → Stage Play → Create A Universe / Universe Challenge, redundant 'Freeform Studio' copy is removed, and closing Open / Import with 'X' returns directly to Splash.",
        accentColor = ElectrumGold
    ),
    WhatsNewHighlight(
        badge = "✨",
        tag = "SPLASH SHOWCASE",
        title = "5-Second Goddess Reveal & Focus Blur",
        description = "Launch opens with a clean 5-second unobstructed showcase of the Goddess Seshat artwork (or tap anywhere to skip) before fading in the studio controls over a softly blurred backdrop.",
        accentColor = ScribeStarNeonRed
    )
)

/**
 * Release highlights modal for Seshat v2.8.
 */
@Composable
fun WhatsNewDialog(
    onDismiss: () -> Unit,
    onOpenAiSettings: (() -> Unit)? = null
) {
    val isDark = LocalSeshatDarkTheme.current
    val surfaceBg = if (isDark) ObsidianSurface else Color(0xFFF8FAFC)
    val cardBg = if (isDark) ObsidianBlack else Color.White
    val primaryText = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 320.dp, max = 580.dp)
                .heightIn(max = 700.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .clip(RoundedCornerShape(0.dp))
                .border(BorderStroke(1.2.dp, ElectrumGold.copy(alpha = 0.65f)), RoundedCornerShape(0.dp)),
            color = surfaceBg
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) ObsidianBlack else Color(0xFFE2E8F0).copy(alpha = 0.6f))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.seshat_emblem_logo),
                            contentDescription = "Seshat Emblem",
                            modifier = Modifier.size(34.dp)
                        )
                        Column {
                            Text(
                                text = "WHAT'S NEW IN SESHAT",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = MainAppFontFamily,
                                    color = ElectrumGold,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "RELEASE 2.9 • CLOUD SYNC, STELLARIUM & STUDIO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = StudioMonoFontFamily,
                                    color = secondaryText,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close What's New",
                            tint = secondaryText
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = ObsidianSurfaceVariant)

                // Scrollable Feature Cards
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    v28Highlights.forEach { item ->
                        WhatsNewFeatureCard(
                            item = item,
                            cardBg = cardBg,
                            primaryText = primaryText,
                            secondaryText = secondaryText
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = ObsidianSurfaceVariant)

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onOpenAiSettings != null) {
                        OutlinedButton(
                            onClick = onOpenAiSettings,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, ObsidianSurfaceVariant)
                        ) {
                            Text(
                                text = "🗝️ Configure AI Key",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = MainAppFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryText
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectrumGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = "Start Writing",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatsNewFeatureCard(
    item: WhatsNewHighlight,
    cardBg: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(0.dp))
            .border(BorderStroke(1.dp, item.accentColor.copy(alpha = 0.45f)), RoundedCornerShape(0.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = item.badge, fontSize = 16.sp)
            Surface(
                color = item.accentColor.copy(alpha = 0.16f),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(0.75.dp, item.accentColor.copy(alpha = 0.6f))
            ) {
                Text(
                    text = item.tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = StudioMonoFontFamily,
                        color = item.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.6.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = MainAppFontFamily,
                color = primaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        )
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = MainAppFontFamily,
                color = secondaryText,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        )
    }
}
