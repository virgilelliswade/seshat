package com.seshat.writer.firstdraft

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.corkboard.SceneCard
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.neonBloom
import java.util.Calendar
import java.util.Locale

/**
 * Persistent top HUD strip for the "First Draft" Anti-Procrastination Engine.
 * Displays sequential stage progression, countdown timers, XP balance, 25m Sprint,
 * XP Shop, Duty Roster, Reference Drawer, and Daily Receipt Share.
 */
@Composable
fun FirstDraftControlStrip(
    state: FirstDraftState,
    currentWordCount: Int,
    onAdvanceStage: () -> Unit,
    onToggleSprint: () -> Unit,
    onOpenXpShop: () -> Unit,
    onOpenDutyRoster: () -> Unit,
    onToggleReferenceDrawer: () -> Unit,
    onOpenDailyReceipt: () -> Unit,
    onInsertTkToken: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isFirstDraftMode) return

    val nowMs = System.currentTimeMillis()
    val timerText = when (state.stage) {
        FirstDraftStage.SPARK -> "⚡ SPARK: ${FirstDraftController.formatDurationHhMmSs(state.sparkActiveRemainingMs)}"
        FirstDraftStage.BIBLE -> "📖 BIBLE AIRLOCK: ${FirstDraftController.formatDurationHhMmSs(state.stageDeadlineWallClockMs - nowMs)}"
        FirstDraftStage.OUTLINE -> "📌 OUTLINE AIRLOCK: ${FirstDraftController.formatDurationHhMmSs(state.stageDeadlineWallClockMs - nowMs)}"
        FirstDraftStage.DRAFT -> String.format(Locale.US, "⏳ DECAY CLOCK: %.1f HRS", state.hoursUntilDecay(nowMs))
        FirstDraftStage.STUDIO_UNLOCKED -> "🔓 STUDIO UNLOCKED"
    }

    val quotaRequired = state.survivalQuotaWords()
    val netQuotaWords = (currentWordCount - state.wordsAtQuotaWindowStart).coerceAtLeast(0)
    val canShareDaily = state.canShareDailyReceipt(nowMs)

    Surface(
        color = Color(0xEE080B12),
        border = BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.55f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Left: Stage Pipeline & Countdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FirstDraftStage.entries.forEach { st ->
                    val isCurrent = state.stage == st
                    val isPassed = st.ordinal < state.stage.ordinal
                    Surface(
                        color = when {
                            isCurrent -> ScribeStarNeonRed
                            isPassed -> Color(0xFF1E293B)
                            else -> Color(0xFF0F141E)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isCurrent -> Color.White
                                isPassed -> Color(0xFF10B981)
                                else -> Color(0xFF334155)
                            }
                        )
                    ) {
                        Text(
                            text = if (isPassed) "🔒 ${st.label}" else st.badge,
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = if (isCurrent) Color.White else if (isPassed) Color(0xFF10B981) else Color(0xFF64748B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Active Countdown Badge
                Surface(
                    color = Color(0xFF18080E),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ScribeStarNeonRed)
                ) {
                    Text(
                        text = timerText,
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (state.stage == FirstDraftStage.DRAFT) {
                    Surface(
                        color = Color(0xFF0E1B2A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text(
                            text = "QUOTA: $netQuotaWords / $quotaRequired WDS",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: XP Shop, 25m Sprint, [TK] Token, Reference Drawer, Duty Roster, Share, Advance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Active Pass Indicators
                if (state.airlockPassRemainingMs > 0L) {
                    Surface(
                        color = Color(0xFF064E3B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Text(
                            text = "🔓 BIBLE PASS: ${FirstDraftController.formatDurationHhMmSs(state.airlockPassRemainingMs)}",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (state.mulliganPassRemainingMs > 0L) {
                    Surface(
                        color = Color(0xFF3B0764),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFA855F7))
                    ) {
                        Text(
                            text = "⏪ MULLIGAN: ${FirstDraftController.formatDurationHhMmSs(state.mulliganPassRemainingMs)}",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFFD8B4FE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // XP Shop Pill
                Surface(
                    color = Color(0xFF1E1B0E),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ElectrumGold),
                    modifier = Modifier.clickable { onOpenXpShop() }
                ) {
                    Text(
                        text = "⚡ ${state.xpBalance} XP • SHOP",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = ElectrumGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // 25-Minute Sprint Button (Strictly in DRAFT)
                if (state.stage == FirstDraftStage.DRAFT) {
                    val sprintMin = (state.activeSprintRemainingMs / 60000L)
                    val sprintSec = (state.activeSprintRemainingMs % 60000L) / 1000L
                    Surface(
                        color = if (state.isSprintRunning) ScribeStarNeonRed else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ScribeStarNeonRed),
                        modifier = Modifier.clickable { onToggleSprint() }
                    ) {
                        Text(
                            text = if (state.isSprintRunning) {
                                String.format(Locale.US, "🏃 SPRINT %02d:%02d", sprintMin, sprintSec)
                            } else {
                                "🏃 25m Sprint (+1k XP)"
                            },
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // [TK] Token Hotkey Pill (Cmd+K / Ctrl+K)
                    Surface(
                        color = Color(0xFF2A0810),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ScribeStarNeonRed),
                        modifier = Modifier.clickable { onInsertTkToken() }
                    ) {
                        Text(
                            text = "[TK] ⌘K",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = ScribeStarNeonRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Split-Pane Read-Only Reference Drawer Toggle
                if (state.stage == FirstDraftStage.DRAFT || state.stage == FirstDraftStage.OUTLINE) {
                    Surface(
                        color = if (state.isReferenceDrawerOpen) ScribeStarNeonRed else Color(0xFF161E2E),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.clickable { onToggleReferenceDrawer() }
                    ) {
                        Text(
                            text = "📚 Read-Only Ref",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Day Job Duty Roster Button
                Surface(
                    color = Color(0xFF161E2E),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    modifier = Modifier.clickable { onOpenDutyRoster() }
                ) {
                    Text(
                        text = "🗓️ Duty Roster",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Daily Draft Receipt Share Button (Disabled until dailyQuotaMet & 24h cooldown)
                if (state.stage == FirstDraftStage.DRAFT) {
                    Surface(
                        color = if (canShareDaily) ScribeStarNeonRed else Color(0xFF111622),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (canShareDaily) Color.White else Color(0xFF334155)),
                        modifier = Modifier.clickable(enabled = canShareDaily) { onOpenDailyReceipt() }
                    ) {
                        Text(
                            text = if (canShareDaily) "📤 Share Receipt" else "🔒 Receipt (Quota/24h)",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = if (canShareDaily) Color.White else Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Manual One-Way Stage Airlock Button
                if (state.stage == FirstDraftStage.SPARK ||
                    state.stage == FirstDraftStage.BIBLE ||
                    state.stage == FirstDraftStage.OUTLINE
                ) {
                    val nextLabel = state.stage.nextStage().label
                    Surface(
                        color = ScribeStarNeonRed,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier.clickable { onAdvanceStage() }
                    ) {
                        Text(
                            text = "🔒 Seal & Enter $nextLabel ➔",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated SPARK Stage Screen enforcing active session countdown and logline/hook word caps:
 * - Feature Screenplay: Logline (Max 50 words), 30 minutes active timer.
 * - TV Series: Series Logline + Pilot Logline (Max 50 words each), 60 minutes active timer.
 * - Prose Novel: Premise/Hook (Max 100 words), 30 minutes active timer.
 */
@Composable
fun SparkStageEditorView(
    state: FirstDraftState,
    projectTitle: String,
    onUpdateLogline: (String) -> Unit,
    onUpdatePilotLogline: (String) -> Unit,
    onSealSparkAndAdvance: () -> Unit,
    onOpenDutyRoster: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTv = state.projectFormat == ProjectFormat.TV_SERIES ||
        state.screenplayType == ScreenplayType.TV_HOUR_DRAMA ||
        state.screenplayType == ScreenplayType.TV_HALF_HOUR_COMEDY
    val isNovel = state.projectFormat == ProjectFormat.NOVEL
    val maxWords = state.maxSparkWords()
    val primaryWordCount = FirstDraftController.countWords(state.sparkLogline)
    val pilotWordCount = FirstDraftController.countWords(state.sparkPilotLogline)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE607090E))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .neonBloom(
                    color = ScribeStarNeonRed,
                    cornerRadius = 24.dp,
                    glowRadius = 16.dp,
                    strokeWidth = 1.8.dp,
                    outerAlpha = 0.35f,
                    midAlpha = 0.65f
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0E131F))
                .border(BorderStroke(1.5.dp, ScribeStarNeonRed), RoundedCornerShape(24.dp))
                .padding(28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = ScribeStarNeonRed.copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ScribeStarNeonRed)
            ) {
                Text(
                    text = "⚡ STAGE 1 OF 4: SPARK AIRLOCK • ${FirstDraftController.formatDurationHhMmSs(state.sparkActiveRemainingMs)} REMAINING",
                    style = TextStyle(
                        fontFamily = MainAppFontFamily,
                        color = ScribeStarNeonRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = projectTitle.ifBlank { "UNTITLED PROJECT" }.uppercase(Locale.US),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    isTv -> "TV Series Rule: Define your Series Logline (Max 50 words) and Episode 101 Pilot Logline (Max 50 words) before the 60-minute active timer expires. Episodes 102+ are hard-locked until the Pilot reaches STUDIO_UNLOCKED."
                    isNovel -> "Novel Rule: Define your core Premise & Hook (Max 100 words) before the 30-minute active timer expires. Once sealed, your Spark becomes permanently Read-Only."
                    else -> "Feature Screenplay Rule: Define your core Logline (Max 50 words) before the 30-minute active timer expires. Once sealed, your Spark becomes permanently Read-Only."
                },
                style = TextStyle(
                    fontFamily = MainAppFontFamily,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Logline / Premise Field
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        isTv -> "SERIES OVERARCHING LOGLINE"
                        isNovel -> "NOVEL PREMISE & HOOK"
                        else -> "FEATURE FILM LOGLINE"
                    },
                    style = TextStyle(
                        fontFamily = MainAppFontFamily,
                        color = ElectrumGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "$primaryWordCount / $maxWords WORDS",
                    style = TextStyle(
                        fontFamily = MainAppFontFamily,
                        color = if (primaryWordCount >= maxWords) ScribeStarNeonRed else Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF07090E))
                    .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                if (state.sparkLogline.isEmpty()) {
                    Text(
                        text = "When an inciting event strikes, a flawed protagonist must pursue a high-stakes goal against an overwhelming antagonistic force...",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF475569),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                }
                BasicTextField(
                    value = state.sparkLogline,
                    onValueChange = { raw ->
                        if (!state.isSparkReadOnly) {
                            onUpdateLogline(FirstDraftController.trimToWordLimit(raw, maxWords))
                        }
                    },
                    readOnly = state.isSparkReadOnly,
                    textStyle = TextStyle(
                        fontFamily = MainAppFontFamily,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(ScribeStarNeonRed),
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Secondary Pilot Logline for TV Series
            if (isTv) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "EPISODE 101 (PILOT) LOGLINE",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = ElectrumGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "$pilotWordCount / 50 WORDS",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = if (pilotWordCount >= 50) ScribeStarNeonRed else Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF07090E))
                        .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    if (state.sparkPilotLogline.isEmpty()) {
                        Text(
                            text = "In the pilot episode, the protagonist is thrust into the central conflict of Episode 101...",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF475569),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                    BasicTextField(
                        value = state.sparkPilotLogline,
                        onValueChange = { raw ->
                            if (!state.isSparkReadOnly) {
                                onUpdatePilotLogline(FirstDraftController.trimToWordLimit(raw, 50))
                            }
                        },
                        readOnly = state.isSparkReadOnly,
                        textStyle = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(ScribeStarNeonRed),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFF161E2E),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    modifier = Modifier.clickable { onOpenDutyRoster() }
                ) {
                    Text(
                        text = "🗓️ Configure Day Job Roster",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                    )
                }

                Surface(
                    color = ScribeStarNeonRed,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSealSparkAndAdvance() }
                ) {
                    Text(
                        text = "🔒 SEAL SPARK (READ-ONLY) & ENTER BIBLE AIRLOCK ➔",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-screen Reverse-Lockout overlay displayed whenever a scheduled work/commute shift is active.
 */
@Composable
fun WorkShiftReverseLockoutOverlay(
    activeShift: WorkShiftBlock,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFA05070B))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .neonBloom(
                    color = ScribeStarNeonRed,
                    cornerRadius = 24.dp,
                    glowRadius = 20.dp,
                    strokeWidth = 2.dp,
                    outerAlpha = 0.45f,
                    midAlpha = 0.80f
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0C101A))
                .border(BorderStroke(2.dp, ScribeStarNeonRed), RoundedCornerShape(24.dp))
                .padding(36.dp)
        ) {
            Text(
                text = "🛑 REVERSE-LOCKOUT ENGAGED",
                style = TextStyle(
                    fontFamily = MainAppFontFamily,
                    color = ScribeStarNeonRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "WORK SHIFT ACTIVE - EDITOR LOCKED UNTIL ${activeShift.formattedEndTime()}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    lineHeight = 32.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "All wall-clock macro-timers (BIBLE, OUTLINE, and 48-Hour Decay) are paused during your scheduled ${activeShift.formattedDayName()} shift (${activeShift.formattedStartTime()} – ${activeShift.formattedEndTime()}). Focus on your day job and return ready to sprint.",
                style = TextStyle(
                    fontFamily = MainAppFontFamily,
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * The "Day Job" Duty Roster modal (`WorkShiftFreeze`).
 * Enforces Sunday Roster Lock (editable only on initial project creation or Sundays 08:00-20:00)
 * and a hard cap of 50 hours per 7-day rolling window.
 */
@Composable
fun WorkShiftRosterDialog(
    freeze: WorkShiftFreeze,
    onSaveAndLockRoster: (List<WorkShiftBlock>) -> Unit,
    onDismiss: () -> Unit
) {
    val nowMs = System.currentTimeMillis()
    val canEdit = freeze.canEditRoster(nowMs)
    var shifts by remember(freeze) { mutableStateOf(freeze.shifts) }
    var selectedDay by remember { mutableIntStateOf(Calendar.MONDAY) }
    var startHour by remember { mutableIntStateOf(9) }
    var endHour by remember { mutableIntStateOf(17) }

    val totalMinutes = shifts.sumOf { it.durationMinutes() }
    val totalHours = totalMinutes / 60f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD904060A))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0E131F))
                    .border(BorderStroke(1.5.dp, ScribeStarNeonRed), RoundedCornerShape(22.dp))
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "🗓️ THE 'DAY JOB' DUTY ROSTER (WorkShiftFreeze)",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = if (canEdit) {
                                "Editable Now (Initial Setup or Sunday 08:00–20:00 Window)"
                            } else {
                                "🔒 ROSTER LOCKED FOR 7 DAYS (Editable Sundays 08:00–20:00)"
                            },
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = if (canEdit) Color(0xFF10B981) else ScribeStarNeonRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 50-Hour Hard Cap Bar
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "WEEKLY SHIFT ALLOCATION (HARD CAP: 50.0 HRS)",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f / 50.0 HRS", totalHours),
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = if (totalMinutes >= WorkShiftFreeze.MAX_WEEKLY_MINUTES) ScribeStarNeonRed else ElectrumGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (totalMinutes.toFloat() / WorkShiftFreeze.MAX_WEEKLY_MINUTES).coerceIn(0f, 1f) },
                    color = ScribeStarNeonRed,
                    trackColor = Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (canEdit) {
                    // Add Shift Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF07090E))
                            .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val days = listOf(
                                Calendar.MONDAY to "MON",
                                Calendar.TUESDAY to "TUE",
                                Calendar.WEDNESDAY to "WED",
                                Calendar.THURSDAY to "THU",
                                Calendar.FRIDAY to "FRI",
                                Calendar.SATURDAY to "SAT",
                                Calendar.SUNDAY to "SUN"
                            )
                            days.forEach { (dConst, dLabel) ->
                                val isSel = selectedDay == dConst
                                Surface(
                                    color = if (isSel) ScribeStarNeonRed else Color(0xFF161E2E),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDay = dConst }
                                ) {
                                    Text(
                                        text = dLabel,
                                        style = TextStyle(
                                            fontFamily = MainAppFontFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Start: ", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Surface(
                                    color = Color(0xFF161E2E),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { startHour = (startHour + 1) % 24 }
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%02d:00", startHour),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("End: ", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Surface(
                                    color = Color(0xFF161E2E),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { endHour = (endHour + 1) % 24 }
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%02d:00", endHour),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Surface(
                                color = ElectrumGold,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    val candidate = WorkShiftBlock(
                                        dayOfWeek = selectedDay,
                                        startHour = startHour,
                                        startMinute = 0,
                                        endHour = endHour,
                                        endMinute = 0
                                    )
                                    val newTotal = totalMinutes + candidate.durationMinutes()
                                    if (candidate.durationMinutes() > 0 && newTotal <= WorkShiftFreeze.MAX_WEEKLY_MINUTES) {
                                        shifts = shifts + candidate
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Shift Block",
                                        style = TextStyle(
                                            fontFamily = MainAppFontFamily,
                                            color = ObsidianBlack,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Shift Blocks List
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(180.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (shifts.isEmpty()) {
                        Text(
                            text = "No weekly work/commute blocks scheduled.",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF64748B),
                                fontSize = 12.5.sp
                            ),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        shifts.forEach { block ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161E2E))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${block.formattedDayName()} • ${block.formattedStartTime()} – ${block.formattedEndTime()} (${String.format(Locale.US, "%.1f", block.durationMinutes() / 60f)}h)",
                                    style = TextStyle(
                                        fontFamily = MainAppFontFamily,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                                if (canEdit) {
                                    IconButton(
                                        onClick = { shifts = shifts.filterNot { it.id == block.id } },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ScribeStarNeonRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (canEdit) {
                    Surface(
                        color = ScribeStarNeonRed,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSaveAndLockRoster(shifts)
                                onDismiss()
                            }
                    ) {
                        Text(
                            text = "🔒 SAVE & LOCK SCHEDULE FOR 7 DAYS (isReadOnly = true)",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * XP Shop Modal allowing purchase of Airlock Pass (2,500 XP), Mulligan (5,000 XP), and Freeze Token (10,000 XP).
 */
@Composable
fun XpShopDialog(
    state: FirstDraftState,
    onPurchaseItem: (XpShopItem) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD904060A))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0E131F))
                    .border(BorderStroke(1.5.dp, ElectrumGold), RoundedCornerShape(22.dp))
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "⚡ FORWARD-MOMENTUM XP SHOP",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = ElectrumGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Balance: ${state.xpBalance} XP (Earned strictly in DRAFT: +10/line, +500/clean scene, +1,000/sprint)",
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.5.sp
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                XpShopItem.entries.forEach { item ->
                    val canAfford = state.xpBalance >= item.costXp
                    Surface(
                        color = Color(0xFF07090E),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (canAfford) ElectrumGold else Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = item.title,
                                    style = TextStyle(
                                        fontFamily = MainAppFontFamily,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.description,
                                    style = TextStyle(
                                        fontFamily = MainAppFontFamily,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                )
                            }

                            Surface(
                                color = if (canAfford) ElectrumGold else Color(0xFF1E293B),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable(enabled = canAfford) {
                                    onPurchaseItem(item)
                                }
                            ) {
                                Text(
                                    text = "${item.costXp} XP",
                                    style = TextStyle(
                                        fontFamily = MainAppFontFamily,
                                        color = if (canAfford) ObsidianBlack else Color(0xFF64748B),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Gemini AI Bouncer / Idle Instigator high-contrast bottom banner triggered after
 * 60 seconds of zero keystroke delta in DRAFT state.
 */
@Composable
fun IdleInstigatorBanner(
    promptText: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (promptText == null && !isLoading) return

    Surface(
        color = Color(0xFF28050C),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, ScribeStarNeonRed),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🚨 GEMINI AI BOUNCER — 60s IDLE INSTIGATOR",
                    style = TextStyle(
                        fontFamily = MainAppFontFamily,
                        color = ScribeStarNeonRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLoading) "Analyzing last 15 lines for immediate conflict escalation..." else (promptText ?: ""),
                    style = TextStyle(
                        fontFamily = MainAppFontFamily,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
            }
        }
    }
}

/**
 * Red diagonal watermark rendered across all views when isDecayed == true: "ABANDONED DRAFT".
 */
@Composable
fun AbandonedDraftWatermarkOverlay(
    isDecayed: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isDecayed) return
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "ABANDONED DRAFT",
            style = TextStyle(
                fontFamily = MainAppFontFamily,
                color = ScribeStarNeonRed.copy(alpha = 0.24f),
                fontWeight = FontWeight.Black,
                fontSize = 68.sp,
                letterSpacing = 6.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                rotationZ = -32f
            }
        )
    }
}

/**
 * Split-pane Read-Only Reference Drawer allowing users in DRAFT or OUTLINE state
 * to inspect locked SPARK, BIBLE, and OUTLINE models with all edit/add/delete actions disabled.
 */
@Composable
fun ReadOnlyReferenceDrawer(
    state: FirstDraftState,
    bible: StoryBible,
    sceneCards: List<SceneCard>,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(Color(0xF50B0E17))
            .border(BorderStroke(1.dp, Color(0xFF334155)))
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔒 AIRLOCK REFERENCE",
                style = TextStyle(
                    fontFamily = MainAppFontFamily,
                    color = ElectrumGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            )
            IconButton(onClick = onCloseDrawer, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Text(
            text = "Read-Only Inspection Mode (isReadOnly = true)",
            style = TextStyle(
                fontFamily = MainAppFontFamily,
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Locked SPARK
        Text(
            text = "⚡ SEALED SPARK LOGLINE",
            style = TextStyle(
                fontFamily = MainAppFontFamily,
                color = ScribeStarNeonRed,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = state.sparkLogline.ifBlank { "No Spark logline recorded." },
            style = TextStyle(
                fontFamily = MainAppFontFamily,
                color = Color.White,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        )
        if (state.sparkPilotLogline.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pilot Logline: ${state.sparkPilotLogline}",
                style = TextStyle(
                    fontFamily = MainAppFontFamily,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(14.dp))

        // 2. Locked BIBLE Characters & Lore Pillars
        Text(
            text = "📖 SEALED STORY BIBLE (${bible.characters.size} Characters)",
            style = TextStyle(
                fontFamily = MainAppFontFamily,
                color = ElectrumGold,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        bible.characters.forEach { ch ->
            Surface(
                color = Color(0xFF131926),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "${ch.name} (${ch.role})",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                    if (ch.goalAndStakes.isNotBlank() || ch.arcAndNeed.isNotBlank()) {
                        Text(
                            text = ch.goalAndStakes.ifBlank { ch.arcAndNeed },
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(14.dp))

        // 3. Locked OUTLINE Cards
        Text(
            text = "📌 SEALED CORKBOARD OUTLINE (${sceneCards.size} Cards)",
            style = TextStyle(
                fontFamily = MainAppFontFamily,
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        sceneCards.take(state.unlockedCorkboardSceneSlots).forEachIndexed { idx, card ->
            Surface(
                color = Color(0xFF131926),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "${idx + 1}. ${card.headingText}",
                        style = TextStyle(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                    if (card.summary.isNotBlank()) {
                        Text(
                            text = card.summary,
                            style = TextStyle(
                                fontFamily = MainAppFontFamily,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
