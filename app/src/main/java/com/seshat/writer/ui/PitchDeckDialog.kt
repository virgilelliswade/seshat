package com.seshat.writer.ui

import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.pitch.PitchDeckDocument
import com.seshat.writer.pitch.PitchDeckExporters
import com.seshat.writer.pitch.PitchDeckRepository
import com.seshat.writer.pitch.PitchDeckTheme
import com.seshat.writer.pitch.PitchSlide
import com.seshat.writer.pitch.PitchSlideLayout
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.StudioMonoFontFamily
import com.seshat.writer.ui.theme.ambientWorkspaceBrush
import com.seshat.writer.ui.theme.floatingGlassIsland
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

@Composable
fun PitchDeckDialog(
    projectTitle: String,
    author: String,
    storyBible: StoryBible,
    corkboard: CorkboardDocument,
    blocks: List<ScreenplayBlock>,
    onDismiss: () -> Unit,
    onOpenAiSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isCompactScreen = configuration.screenWidthDp < 820
    val isDark = LocalSeshatDarkTheme.current

    var deck by remember(projectTitle) {
        mutableStateOf(
            PitchDeckRepository.loadDeck(
                context = context,
                title = projectTitle.ifBlank { "UNTITLED PROJECT" },
                author = author,
                bible = storyBible,
                corkboard = corkboard
            )
        )
    }

    // 0 = 45-Second Elevator Pitch, 1 = 16:9 Presentation Deck
    var activeMode by remember { mutableIntStateOf(1) }
    var selectedSlideIdx by remember { mutableIntStateOf(0) }
    var isAiBusy by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // 45-second rehearsal timer
    var timerSecondsLeft by remember { mutableIntStateOf(45) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timerSecondsLeft > 0) {
            delay(1000L)
            timerSecondsLeft--
        }
        if (timerSecondsLeft == 0) {
            isTimerRunning = false
        }
    }

    fun saveDeckUpdate(updated: PitchDeckDocument) {
        deck = updated
        PitchDeckRepository.saveDeck(context, updated)
    }

    val corkboardSnippet = remember(corkboard) {
        corkboard.acts.joinToString("\n") { act ->
            val cards = corkboard.cards
                .filter { it.actIndex == act.actIndex }
                .take(5)
                .joinToString("; ") { "${it.title}: ${it.summary}" }
            "${act.title}: $cards"
        }
    }
    val scriptSnippet = remember(blocks) {
        blocks.take(40).joinToString("\n") { it.text }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ambientWorkspaceBrush(isDark))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Studio Header Island
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .floatingGlassIsland(isDark = isDark, elevation = 14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(0.dp))
                                    .background(ObsidianBlack)
                                    .border(BorderStroke(1.dp, ObsidianSurfaceVariant), RoundedCornerShape(0.dp))
                                    .clickable(onClick = onDismiss)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Pitch Deck Creator",
                                    tint = if (isDark) Color.White else Color(0xFF0F172A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "🎤 PITCH DECK CREATOR • ${deck.projectTitle.uppercase()}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = MainAppFontFamily,
                                        color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "LMU Pitch Night Studio • Story Bible & Corkboard Connected",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Mode Toggle + Exports
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("⚡ 45s Elevator Pitch" to 0, "📊 16:9 Slide Deck (${deck.slides.size})" to 1).forEach { (label, modeIdx) ->
                                val selected = activeMode == modeIdx
                                Surface(
                                    color = if (selected) ScribeStarNeonRed.copy(alpha = 0.22f) else ObsidianBlack,
                                    shape = RoundedCornerShape(0.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(0.dp))
                                        .clickable { activeMode = modeIdx }
                                        .border(1.dp, if (selected) ScribeStarNeonRed else ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (selected) (if (isDark) Color.White else Color(0xFF991B1B)) else ScreenplayTextMuted,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            if (activeMode == 0) {
                                Surface(
                                    color = ObsidianBlack,
                                    shape = RoundedCornerShape(0.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(0.dp))
                                        .clickable {
                                            val pdf = PitchDeckExporters.exportElevatorSheetPdf(context, deck)
                                            ScriptFileManager.shareFile(context, pdf, "application/pdf", "Share 45s Elevator Pitch")
                                            statusMessage = "✅ Exported 45s Pitch PDF: ${pdf.name}"
                                        }
                                        .border(1.dp, ElectrumGold, RoundedCornerShape(0.dp))
                                ) {
                                    Text(
                                        text = "📄 Export Sheet PDF",
                                        style = MaterialTheme.typography.labelSmall.copy(color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    color = ObsidianBlack,
                                    shape = RoundedCornerShape(0.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(0.dp))
                                        .clickable {
                                            val pdf = PitchDeckExporters.exportDeckPdf(context, deck)
                                            ScriptFileManager.shareFile(context, pdf, "application/pdf", "Share Pitch Deck PDF")
                                            statusMessage = "✅ Exported Deck PDF: ${pdf.name}"
                                        }
                                        .border(1.dp, ElectrumGold, RoundedCornerShape(0.dp))
                                ) {
                                    Text(
                                        text = "📄 PDF",
                                        style = MaterialTheme.typography.labelSmall.copy(color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    color = ElectrumGold,
                                    shape = RoundedCornerShape(0.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(0.dp))
                                        .clickable {
                                            val pptx = PitchDeckExporters.exportDeckPptx(context, deck)
                                            ScriptFileManager.shareFile(
                                                context,
                                                pptx,
                                                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                                "Share PowerPoint / Google Slides / Keynote (.pptx)"
                                            )
                                            statusMessage = "✅ Exported .PPTX (PowerPoint / Slides / Keynote): ${pptx.name}"
                                        }
                                ) {
                                    Text(
                                        text = "📊 Export .PPTX",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (statusMessage != null) {
                        HorizontalDivider(color = ObsidianSurfaceVariant, thickness = 1.dp)
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }
                }

                // Body
                if (activeMode == 0) {
                    ElevatorPitchWorkspace(
                        deck = deck,
                        timerSecondsLeft = timerSecondsLeft,
                        isTimerRunning = isTimerRunning,
                        isAiBusy = isAiBusy,
                        isDark = isDark,
                        onUpdateDeck = ::saveDeckUpdate,
                        onStartPauseTimer = { isTimerRunning = !isTimerRunning },
                        onResetTimer = {
                            isTimerRunning = false
                            timerSecondsLeft = 45
                        },
                        onAiDraftElevator = {
                            val config = AiPreferences.load(context)
                            if (!config.isConfigured) {
                                Toast.makeText(context, "Configure an AI key in Settings first.", Toast.LENGTH_SHORT).show()
                                onOpenAiSettings()
                            } else {
                                isAiBusy = true
                                scope.launch {
                                    val prompt = """
                                        Write a Hollywood 45-second Pitch Night sheet in JSON for "${deck.projectTitle}".
                                        Return ONLY valid JSON with keys: "logline" (1 punchy sentence), "passion" (1 personal paragraph on Why Me/Why Now), "pitch" (spoken 45-second elevator pitch ~145 words).
                                    """.trimIndent()
                                    val res = AiClient.generatePitchContent(
                                        config = config,
                                        taskPrompt = prompt,
                                        storyBibleContext = storyBible.toAiContextSnippet(),
                                        corkboardContext = corkboardSnippet,
                                        scriptContext = scriptSnippet
                                    )
                                    isAiBusy = false
                                    res.onSuccess { raw ->
                                        val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                                        try {
                                            val j = JSONObject(cleaned)
                                            saveDeckUpdate(
                                                deck.copy(
                                                    elevatorPitch = deck.elevatorPitch.copy(
                                                        logline = j.optString("logline", deck.elevatorPitch.logline),
                                                        passionWhyMe = j.optString("passion", deck.elevatorPitch.passionWhyMe),
                                                        spokenPitch = j.optString("pitch", deck.elevatorPitch.spokenPitch)
                                                    )
                                                )
                                            )
                                            statusMessage = "✨ AI drafted your 45s Elevator Pitch from your Story Bible & Corkboard!"
                                        } catch (_: Exception) {
                                            saveDeckUpdate(
                                                deck.copy(elevatorPitch = deck.elevatorPitch.copy(spokenPitch = raw))
                                            )
                                            statusMessage = "✨ AI generated pitch copy into Spoken Pitch."
                                        }
                                    }.onFailure { err ->
                                        statusMessage = "❌ AI Error: ${err.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                } else {
                    PresentationDeckWorkspace(
                        deck = deck,
                        selectedSlideIdx = selectedSlideIdx.coerceIn(0, (deck.slides.size - 1).coerceAtLeast(0)),
                        isCompactScreen = isCompactScreen,
                        isAiBusy = isAiBusy,
                        isDark = isDark,
                        onSelectSlide = { selectedSlideIdx = it },
                        onUpdateDeck = ::saveDeckUpdate,
                        onAiBuildFullDeck = {
                            val config = AiPreferences.load(context)
                            if (!config.isConfigured) {
                                Toast.makeText(context, "Configure an AI key in Settings first.", Toast.LENGTH_SHORT).show()
                                onOpenAiSettings()
                            } else {
                                isAiBusy = true
                                scope.launch {
                                    val prompt = """
                                        Build an 8-slide Hollywood Pitch Night presentation JSON array for "${deck.projectTitle}".
                                        Follow this arc: 1) Cold Open Hook, 2) World & Stakes, 3) Title/Tone Card, 4) Characters Spotlight, 5) Turning Point, 6) Central Dilemma, 7) Why Me? Why Now? & Comps, 8) Closing Questions?.
                                        Return ONLY a JSON array of objects with keys: "title", "subtitle", "body", "speakerNotes".
                                    """.trimIndent()
                                    val res = AiClient.generatePitchContent(
                                        config = config,
                                        taskPrompt = prompt,
                                        storyBibleContext = storyBible.toAiContextSnippet(),
                                        corkboardContext = corkboardSnippet,
                                        scriptContext = scriptSnippet
                                    )
                                    isAiBusy = false
                                    res.onSuccess { raw ->
                                        val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                                        try {
                                            val arr = JSONArray(cleaned)
                                            val layouts = PitchSlideLayout.entries
                                            val generated = mutableListOf<PitchSlide>()
                                            for (i in 0 until arr.length()) {
                                                val obj = arr.optJSONObject(i) ?: continue
                                                generated.add(
                                                    PitchSlide(
                                                        title = obj.optString("title", "SLIDE ${i + 1}"),
                                                        subtitle = obj.optString("subtitle", ""),
                                                        body = obj.optString("body", ""),
                                                        layout = layouts[i.coerceIn(0, layouts.lastIndex)],
                                                        speakerNotes = obj.optString("speakerNotes", "")
                                                    )
                                                )
                                            }
                                            if (generated.isNotEmpty()) {
                                                selectedSlideIdx = 0
                                                saveDeckUpdate(deck.copy(slides = generated))
                                                statusMessage = "✨ AI built a ${generated.size}-slide Pitch Deck from your Bible & Corkboard!"
                                            }
                                        } catch (_: Exception) {
                                            statusMessage = "❌ AI returned non-JSON output; try polishing individual slides."
                                        }
                                    }.onFailure { err ->
                                        statusMessage = "❌ AI Error: ${err.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ElevatorPitchWorkspace(
    deck: PitchDeckDocument,
    timerSecondsLeft: Int,
    isTimerRunning: Boolean,
    isAiBusy: Boolean,
    isDark: Boolean,
    onUpdateDeck: (PitchDeckDocument) -> Unit,
    onStartPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onAiDraftElevator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elev = deck.elevatorPitch
    val estSecs = elev.estimatedSeconds()
    val words = elev.wordCount()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rehearsal Timer & AI Draft Bar
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⏱️ 45-SECOND REHEARSAL CLOCK: 00:${String.format(Locale.US, "%02d", timerSecondsLeft)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = StudioMonoFontFamily,
                            color = if (timerSecondsLeft <= 10) ScribeStarNeonRed else (if (isDark) ElectrumGold else Color(0xFF991B1B)),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Current Draft: $words words (~${estSecs}s spoken @ 140 WPM · Ideal: 100–145 words)",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onStartPauseTimer,
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.dp, ElectrumGold)
                    ) {
                        Text(if (isTimerRunning) "⏸ Pause" else "▶ Start 45s", color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onResetTimer,
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.dp, ObsidianSurfaceVariant)
                    ) {
                        Text("↺ Reset", color = ScreenplayTextDark)
                    }
                    Button(
                        onClick = onAiDraftElevator,
                        enabled = !isAiBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = ScribeStarNeonRed, contentColor = Color.White),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        if (isAiBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(if (isAiBusy) "Drafting..." else "✨ AI Draft Pitch", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        OutlinedTextField(
            value = elev.authorAndCourse,
            onValueChange = { onUpdateDeck(deck.copy(elevatorPitch = elev.copy(authorAndCourse = it))) },
            label = { Text("Writer / Studio / Course Header", color = ScreenplayTextMuted) },
            placeholder = { Text("e.g. Virgil Ellis Wade / SCWR 685", color = ScreenplayTextMuted) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextDark),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = elev.logline,
            onValueChange = { onUpdateDeck(deck.copy(elevatorPitch = elev.copy(logline = it))) },
            label = { Text("Logline (1 Crisp Sentence)", color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) },
            placeholder = { Text("When [inciting incident], a [protagonist] must [goal] before [stakes]...", color = ScreenplayTextMuted) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextDark),
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = elev.passionWhyMe,
            onValueChange = { onUpdateDeck(deck.copy(elevatorPitch = elev.copy(passionWhyMe = it))) },
            label = { Text("Passion — Why Me? Why This Story?", color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold) },
            placeholder = { Text("Your personal connection to the world, theme, and why you are the writer for it...", color = ScreenplayTextMuted) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextDark),
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = elev.spokenPitch,
            onValueChange = { onUpdateDeck(deck.copy(elevatorPitch = elev.copy(spokenPitch = it))) },
            label = { Text("45-Second Spoken Elevator Pitch", color = ScribeStarNeonRed, fontWeight = FontWeight.Bold) },
            placeholder = { Text("Hook the producer into the world, introduce your hero's dilemma, and end on the central hook...", color = ScreenplayTextMuted) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextDark),
            minLines = 7,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PresentationDeckWorkspace(
    deck: PitchDeckDocument,
    selectedSlideIdx: Int,
    isCompactScreen: Boolean,
    isAiBusy: Boolean,
    isDark: Boolean,
    onSelectSlide: (Int) -> Unit,
    onUpdateDeck: (PitchDeckDocument) -> Unit,
    onAiBuildFullDeck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSlide = deck.slides.getOrNull(selectedSlideIdx)
    val t = deck.theme

    Column(modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Theme Picker + AI Build Row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "THEME:",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = StudioMonoFontFamily, color = ScreenplayTextMuted, fontWeight = FontWeight.Bold)
            )
            PitchDeckTheme.entries.forEach { themeOption ->
                val isSelected = deck.theme == themeOption
                Surface(
                    color = Color(themeOption.bgHex),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(0.dp))
                        .clickable { onUpdateDeck(deck.copy(theme = themeOption)) }
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(themeOption.accentHex) else ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                ) {
                    Text(
                        text = themeOption.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(themeOption.accentHex), fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAiBuildFullDeck,
                enabled = !isAiBusy,
                colors = ButtonDefaults.buttonColors(containerColor = ScribeStarNeonRed, contentColor = Color.White),
                shape = RoundedCornerShape(0.dp)
            ) {
                if (isAiBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(if (isAiBusy) "Generating..." else "✨ AI Build Full Deck", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        // Slide Rail + Live 16:9 Canvas + Inspector
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Slide List
            Column(
                modifier = Modifier
                    .width(if (isCompactScreen) 140.dp else 210.dp)
                    .fillMaxHeight()
                    .background(ObsidianSurface, RoundedCornerShape(0.dp))
                    .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        val newSlide = PitchSlide(title = "NEW SLIDE", subtitle = "SECTION", body = "Key points...", layout = PitchSlideLayout.WORLD_STAKES)
                        val nextSlides = deck.slides + newSlide
                        onSelectSlide(nextSlides.lastIndex)
                        onUpdateDeck(deck.copy(slides = nextSlides))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Slide", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    deck.slides.forEachIndexed { idx, slide ->
                        val isSelected = idx == selectedSlideIdx
                        Surface(
                            color = if (isSelected) ScribeStarNeonRed.copy(alpha = 0.2f) else ObsidianBlack,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, if (isSelected) ScribeStarNeonRed else ObsidianSurfaceVariant),
                            modifier = Modifier.fillMaxWidth().clickable { onSelectSlide(idx) }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = String.format(Locale.US, "%02d • %s", idx + 1, slide.title.ifBlank { "Untitled" }),
                                        style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = slide.layout.label,
                                        style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 9.5.sp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Center Preview + Editor
            if (activeSlide != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Live 16:9 Slide Preview Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color(t.bgHex))
                            .border(1.5.dp, Color(t.accentHex), RoundedCornerShape(0.dp))
                    ) {
                        // Right accent bar matching LMU Cyberpunk deck style
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(14.dp)
                                .background(Color(t.accentHex))
                        )
                        Column(
                            modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 34.dp, top = 18.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                HorizontalDivider(color = Color(t.mutedHex).copy(alpha = 0.55f), thickness = 1.dp)
                                if (activeSlide.subtitle.isNotBlank()) {
                                    Text(
                                        text = activeSlide.subtitle.uppercase(Locale.US),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = StudioMonoFontFamily,
                                            color = Color(t.mutedHex),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Text(
                                    text = activeSlide.title.ifBlank { deck.projectTitle },
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = MainAppFontFamily,
                                        color = Color(t.accentHex),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = activeSlide.body,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(t.textHex),
                                        lineHeight = 20.sp
                                    ),
                                    maxLines = 7,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column {
                                HorizontalDivider(color = Color(t.mutedHex).copy(alpha = 0.4f), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%02d", selectedSlideIdx + 1),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = StudioMonoFontFamily, color = Color(t.mutedHex))
                                )
                            }
                        }
                    }

                    // Slide Controls (Move Up/Down, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EDIT SLIDE ${selectedSlideIdx + 1} (${activeSlide.layout.label})",
                            style = MaterialTheme.typography.labelSmall.copy(color = if (isDark) ElectrumGold else Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                enabled = selectedSlideIdx > 0,
                                onClick = {
                                    val list = deck.slides.toMutableList()
                                    val cur = list[selectedSlideIdx]
                                    list[selectedSlideIdx] = list[selectedSlideIdx - 1]
                                    list[selectedSlideIdx - 1] = cur
                                    onSelectSlide(selectedSlideIdx - 1)
                                    onUpdateDeck(deck.copy(slides = list))
                                }
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Slide Up", tint = ScreenplayTextDark)
                            }
                            IconButton(
                                enabled = selectedSlideIdx < deck.slides.lastIndex,
                                onClick = {
                                    val list = deck.slides.toMutableList()
                                    val cur = list[selectedSlideIdx]
                                    list[selectedSlideIdx] = list[selectedSlideIdx + 1]
                                    list[selectedSlideIdx + 1] = cur
                                    onSelectSlide(selectedSlideIdx + 1)
                                    onUpdateDeck(deck.copy(slides = list))
                                }
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Slide Down", tint = ScreenplayTextDark)
                            }
                            IconButton(
                                enabled = deck.slides.size > 1,
                                onClick = {
                                    val list = deck.slides.filterIndexed { i, _ -> i != selectedSlideIdx }
                                    onSelectSlide((selectedSlideIdx - 1).coerceAtLeast(0))
                                    onUpdateDeck(deck.copy(slides = list))
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Slide", tint = Color(0xFFEF5350))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = activeSlide.title,
                        onValueChange = { v ->
                            val list = deck.slides.toMutableList()
                            list[selectedSlideIdx] = activeSlide.copy(title = v)
                            onUpdateDeck(deck.copy(slides = list))
                        },
                        label = { Text("Slide Title", color = ScreenplayTextMuted) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextDark),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = activeSlide.subtitle,
                        onValueChange = { v ->
                            val list = deck.slides.toMutableList()
                            list[selectedSlideIdx] = activeSlide.copy(subtitle = v)
                            onUpdateDeck(deck.copy(slides = list))
                        },
                        label = { Text("Subtitle / Section Tag", color = ScreenplayTextMuted) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = activeSlide.body,
                        onValueChange = { v ->
                            val list = deck.slides.toMutableList()
                            list[selectedSlideIdx] = activeSlide.copy(body = v)
                            onUpdateDeck(deck.copy(slides = list))
                        },
                        label = { Text("Slide Copy / Bullets", color = ScreenplayTextMuted) },
                        minLines = 4,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = activeSlide.speakerNotes,
                        onValueChange = { v ->
                            val list = deck.slides.toMutableList()
                            list[selectedSlideIdx] = activeSlide.copy(speakerNotes = v)
                            onUpdateDeck(deck.copy(slides = list))
                        },
                        label = { Text("Presenter / Pitch Night Notes", color = ScreenplayTextMuted) },
                        minLines = 2,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
