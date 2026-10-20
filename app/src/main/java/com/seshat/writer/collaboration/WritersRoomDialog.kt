package com.seshat.writer.collaboration

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.bible.StoryBeat
import com.seshat.writer.bundle.SeshatBundleManager
import com.seshat.writer.diff.DraftDiffEngine
import com.seshat.writer.importer.ScriptImporter
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import java.io.File
import kotlin.random.Random

data class WritersRoomPitch(
    val id: String = java.util.UUID.randomUUID().toString(),
    val author: String,
    val category: String, // Beat, Joke, Dialogue, Lore
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RoomMember(
    val name: String,
    val role: String,
    val isOnline: Boolean = true
)

@Composable
fun WritersRoomDialog(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Session code state
    var roomCode by remember {
        val rand = Random.nextInt(1000, 9999)
        mutableStateOf("SESHAT-$rand")
    }

    // Room members roster
    val members = remember {
        mutableStateListOf(
            RoomMember(uiState.author.ifBlank { "Showrunner (You)" }, "Head Writer / Showrunner", true),
            RoomMember("Co-Writer 1", "Staff Writer", true),
            RoomMember("Story Editor", "Script Coordinator", false)
        )
    }

    // Room pitches and live board
    val pitches = remember {
        mutableStateListOf(
            WritersRoomPitch(
                author = "Story Editor",
                category = "Beat",
                text = "What if the cold open ends on the discovery of the hidden documents?"
            ),
            WritersRoomPitch(
                author = "Staff Writer",
                category = "Joke",
                text = "Alt dialogue punch-up for the boardroom scene: 'We don't negotiate with reality.'"
            )
        )
    }

    var newPitchText by remember { mutableStateOf("") }
    var newPitchCategory by remember { mutableStateOf("Beat") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WRITERS' ROOM & COLLABORATION",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = ElectrumGold
                                )
                            )
                            Text(
                                text = "Live Co-Writing Session • Cloud Sync • Co-Writer Draft Merge",
                                style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ScreenplayTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: 0 -> Live Room, 1 -> Cloud / Drive Sync, 2 -> Co-Writer Merge
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ObsidianBlack,
                    contentColor = ElectrumGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectrumGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Live Room ($roomCode)",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) ElectrumGold else ScreenplayTextMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Google Drive / Cloud Bundles",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) ElectrumGold else ScreenplayTextMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Co-Writer Draft Merge",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) ElectrumGold else ScreenplayTextMuted
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> LiveRoomContent(
                        roomCode = roomCode,
                        members = members,
                        pitches = pitches,
                        newPitchText = newPitchText,
                        newPitchCategory = newPitchCategory,
                        onUpdateNewPitchText = { newPitchText = it },
                        onUpdateNewPitchCategory = { newPitchCategory = it },
                        onAddPitch = {
                            if (newPitchText.isNotBlank()) {
                                pitches.add(
                                    WritersRoomPitch(
                                        author = uiState.author.ifBlank { "You" },
                                        category = newPitchCategory,
                                        text = newPitchText.trim()
                                    )
                                )
                                newPitchText = ""
                            }
                        },
                        onPushPitchToBible = { pitch ->
                            val currentBible = uiState.storyBible
                            val newBeat = StoryBeat(
                                title = "[Room Pitch] ${pitch.category.uppercase()}",
                                summary = pitch.text,
                                dramaticShift = "+ / -"
                            )
                            val updatedBible = currentBible.copy(beats = currentBible.beats + newBeat)
                            viewModel.saveStoryBible(context, updatedBible)
                            Toast.makeText(context, "Added pitch to Lore Bible Beats!", Toast.LENGTH_SHORT).show()
                        },
                        onCopyRoomCode = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Room Code", roomCode))
                            Toast.makeText(context, "Copied Room Code: $roomCode", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> CloudSyncContent(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    2 -> CoWriterMergeContent(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveRoomContent(
    roomCode: String,
    members: List<RoomMember>,
    pitches: List<WritersRoomPitch>,
    newPitchText: String,
    newPitchCategory: String,
    onUpdateNewPitchText: (String) -> Unit,
    onUpdateNewPitchCategory: (String) -> Unit,
    onAddPitch: () -> Unit,
    onPushPitchToBible: (WritersRoomPitch) -> Unit,
    onCopyRoomCode: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Room Info & Roster (width: 260.dp)
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
        ) {
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ROOM PASSCODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScreenplayTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = roomCode,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectrumGold,
                                letterSpacing = 2.sp
                            )
                        )
                        IconButton(onClick = onCopyRoomCode, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Copy Code",
                                tint = ElectrumGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "Share this code with co-writers in your writers' room to pair sessions.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ScreenplayTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ROOM ROSTER (${members.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ScreenplayTextMuted
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(members) { m ->
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (m.isOnline) Color(0xFF10B981) else ScreenplayTextMuted)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = m.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = m.role,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ScreenplayTextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Communal Pitch & Punch-Up Board
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "COMMUNAL PITCH & PUNCH-UP BOARD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ScreenplayTextMuted
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pitches) { p ->
                    val badgeColor = when (p.category) {
                        "Joke" -> Color(0xFFEC4899)
                        "Lore" -> Color(0xFF8B5CF6)
                        "Dialogue" -> Color(0xFF3B82F6)
                        else -> ElectrumGold
                    }

                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = p.category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = p.author,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = ScreenplayTextMuted
                                        )
                                    )
                                }

                                Surface(
                                    color = ObsidianSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.clickable { onPushPitchToBible(p) }
                                ) {
                                    Text(
                                        text = "Push to Bible ➔",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ElectrumGold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = p.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add Pitch Input Bar
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Beat", "Joke", "Dialogue", "Lore").forEach { cat ->
                            val isSelected = newPitchCategory == cat
                            Surface(
                                color = if (isSelected) ElectrumGold else ObsidianSurfaceVariant,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable { onUpdateNewPitchCategory(cat) }
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ObsidianBlack else ScreenplayTextMuted,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newPitchText,
                            onValueChange = onUpdateNewPitchText,
                            placeholder = { Text("Pitch an idea, alternate joke, or beat...", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectrumGold,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onAddPitch,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Post Pitch", color = Color(0xFF0F1216), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudSyncContent(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState
) {
    val context = LocalContext.current
    var lastExportPath by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Surface(
            color = ObsidianBlack,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "☁️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "GOOGLE DRIVE & CLOUD SHARED BUNDLES",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                        )
                        Text(
                            text = "How writers' rooms collaborate using unified .seshat project packages",
                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A .seshat bundle packages the entire TV Show (all episodes, Fountain scripts, corkboards, and the persistent Series Lore Bible) into a single file.\n\n" +
                            "1. Save or sync the .seshat file inside your shared Google Drive, Dropbox, or OneDrive folder.\n" +
                            "2. Any room writer can import the latest .seshat project on Android, tablet, or laptop.\n" +
                            "3. When passes are returned, use the 'Co-Writer Draft Merge' tab to automatically highlight revisions with industry revision color tags.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val tvProject = uiState.tvSeriesProject
                            val exported = if (tvProject != null) {
                                SeshatBundleManager.exportTvSeriesBundle(context, tvProject)
                            } else {
                                SeshatBundleManager.exportScreenplayBundle(
                                    context = context,
                                    title = uiState.title,
                                    author = uiState.author,
                                    screenplayType = uiState.screenplayType,
                                    blocks = uiState.blocks,
                                    corkboard = uiState.corkboardDoc,
                                    storyBible = uiState.storyBible
                                )
                            }
                            if (exported != null) {
                                lastExportPath = exported.absolutePath
                                Toast.makeText(context, "Exported .seshat bundle successfully!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Export Project Bundle (.seshat)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (lastExportPath != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Saved to: $lastExportPath",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ElectrumGold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CoWriterMergeContent(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState
) {
    val context = LocalContext.current
    var coWriterStatus by remember { mutableStateOf<String?>(null) }
    var mergedScenesCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Surface(
            color = ObsidianBlack,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CO-WRITER PASS IMPORT & DRAFT DIFF MERGE",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "When a co-writer or staff writer emails or sends back an updated draft, Seshat compares their scenes side-by-side with your active episode and advances the production revision color (White ➔ Blue ➔ Pink ➔ Yellow).",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextMuted,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.openDraftDiff()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold)
                    ) {
                        Text("Launch Full Side-by-Side Diff Tool", fontWeight = FontWeight.Bold, color = ObsidianBlack)
                    }

                    OutlinedButton(
                        onClick = {
                            // Advance revision color for co-writer pass
                            val nextColor = when (uiState.currentRevisionColor) {
                                RevisionColor.WHITE -> RevisionColor.BLUE
                                RevisionColor.BLUE -> RevisionColor.PINK
                                RevisionColor.PINK -> RevisionColor.YELLOW
                                RevisionColor.YELLOW -> RevisionColor.GREEN
                                RevisionColor.GREEN -> RevisionColor.GOLDENROD
                                else -> RevisionColor.BUFF
                            }
                            viewModel.setCurrentRevisionColor(nextColor)
                            Toast.makeText(context, "Draft Revision set to: ${nextColor.displayName}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Bump to Next Revision Color", color = ElectrumGold)
                    }
                }
            }
        }
    }
}
