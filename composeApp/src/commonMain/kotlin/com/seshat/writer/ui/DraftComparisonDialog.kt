package com.seshat.writer.ui

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.diff.BlockDiff
import com.seshat.writer.diff.DiffType
import com.seshat.writer.diff.DraftComparisonPdfExporter
import com.seshat.writer.diff.DraftDiffEngine
import com.seshat.writer.diff.DraftDiffResult
import com.seshat.writer.diff.SceneDiff
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.importer.ScriptImporter
import com.seshat.writer.library.ScriptLibraryManager
import com.seshat.writer.library.ScriptProject
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.titlepage.ScreenplayDocumentStore
import com.seshat.writer.titlepage.TitlePageData
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DraftComparisonDialog(
    viewModel: ScreenplayViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val titlePageData = ScreenplayDocumentStore.titlePage.value

    val savedProjects = remember { ScriptLibraryManager.listProjects(context, uiState.title) }

    // Draft Selection State
    var draftAName by remember { mutableStateOf("Current Script (${uiState.title.ifBlank { "Untitled" }})") }
    var draftBName by remember {
        mutableStateOf(
            savedProjects.firstOrNull { !it.isActive }?.title?.let { "$it (Saved)" }
                ?: "Current Script (${uiState.title.ifBlank { "Untitled" }})"
        )
    }

    var blocksA by remember { mutableStateOf(uiState.blocks) }
    var titleA by remember { mutableStateOf(titlePageData) }

    var blocksB by remember {
        val otherProject = savedProjects.firstOrNull { !it.isActive }
        if (otherProject != null) {
            val imp = ScriptImporter.importFromFile(otherProject.file)
            mutableStateOf(imp.document?.blocks ?: uiState.blocks)
        } else {
            mutableStateOf(uiState.blocks)
        }
    }
    var titleB by remember {
        val otherProject = savedProjects.firstOrNull { !it.isActive }
        if (otherProject != null) {
            val imp = ScriptImporter.importFromFile(otherProject.file)
            mutableStateOf(imp.document?.titlePage ?: titlePageData)
        } else {
            mutableStateOf(titlePageData)
        }
    }

    var menuAExpanded by remember { mutableStateOf(false) }
    var menuBExpanded by remember { mutableStateOf(false) }

    // View Options
    var filterTab by remember { mutableIntStateOf(0) } // 0: ALL, 1: CHANGES ONLY, 2: ADDED, 3: MODIFIED
    var isSideBySideView by remember { mutableStateOf(false) }

    // Compute Diff
    val diffResult = remember(blocksA, titleA, blocksB, titleB) {
        DraftDiffEngine.compareDrafts(
            blocksA = blocksA,
            titlePageA = titleA,
            blocksB = blocksB,
            titlePageB = titleB
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ObsidianSurface,
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📑 DRAFT REVISION COMPARISON",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = ElectrumGold
                                )
                            )
                        }
                        Text(
                            text = "Visual line diff, scene alteration tracking & change report",
                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                val report = DraftDiffEngine.generateChangeReportText(diffResult)
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Draft Revision Report", report))
                                Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Copy Report", style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val exportsDir = ScriptFileManager.getExportsDirectory(context)
                                val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                val file = File(exportsDir, "Revision_Diff_Report_$stamp.pdf")
                                DraftComparisonPdfExporter.exportDiffReportToFile(diffResult, file, context)
                                ScriptFileManager.shareFile(context, file, "application/pdf", "Draft Comparison Report")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Export PDF", tint = Color(0xFF0F1216), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF0F1216), fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ScreenplayTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Draft Selector Bar
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
                            .padding(12.dp)
                    ) {
                        // Draft A Selector
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text("BASELINE DRAFT (A)", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 10.sp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { menuAExpanded = true }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = draftAName,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = ElectrumGold)
                                }
                            }

                            DropdownMenu(
                                expanded = menuAExpanded,
                                onDismissRequest = { menuAExpanded = false },
                                modifier = Modifier.background(ObsidianSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Current Script (${uiState.title.ifBlank { "Untitled" }})", color = ScreenplayTextDark) },
                                    onClick = {
                                        draftAName = "Current Script (${uiState.title.ifBlank { "Untitled" }})"
                                        blocksA = uiState.blocks
                                        titleA = titlePageData
                                        menuAExpanded = false
                                    }
                                )
                                savedProjects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text("${proj.title} (${proj.format})", color = ScreenplayTextDark) },
                                        onClick = {
                                            draftAName = "${proj.title} (Saved)"
                                            val imp = ScriptImporter.importFromFile(proj.file)
                                            blocksA = imp.document?.blocks ?: uiState.blocks
                                            titleA = imp.document?.titlePage ?: titlePageData
                                            menuAExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Swap Button
                        IconButton(
                            onClick = {
                                val tmpName = draftAName
                                draftAName = draftBName
                                draftBName = tmpName

                                val tmpBlocks = blocksA
                                blocksA = blocksB
                                blocksB = tmpBlocks

                                val tmpTitle = titleA
                                titleA = titleB
                                titleB = tmpTitle
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("⇄", style = MaterialTheme.typography.titleMedium.copy(color = ElectrumGold, fontWeight = FontWeight.Bold))
                        }

                        // Draft B Selector
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text("REVISED DRAFT (B)", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 10.sp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { menuBExpanded = true }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = draftBName,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextDark, fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = ElectrumGold)
                                }
                            }

                            DropdownMenu(
                                expanded = menuBExpanded,
                                onDismissRequest = { menuBExpanded = false },
                                modifier = Modifier.background(ObsidianSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Current Script (${uiState.title.ifBlank { "Untitled" }})", color = ScreenplayTextDark) },
                                    onClick = {
                                        draftBName = "Current Script (${uiState.title.ifBlank { "Untitled" }})"
                                        blocksB = uiState.blocks
                                        titleB = titlePageData
                                        menuBExpanded = false
                                    }
                                )
                                savedProjects.forEach { proj ->
                                    DropdownMenuItem(
                                        text = { Text("${proj.title} (${proj.format})", color = ScreenplayTextDark) },
                                        onClick = {
                                            draftBName = "${proj.title} (Saved)"
                                            val imp = ScriptImporter.importFromFile(proj.file)
                                            blocksB = imp.document?.blocks ?: uiState.blocks
                                            titleB = imp.document?.titlePage ?: titlePageData
                                            menuBExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // KPI Banner
                val sum = diffResult.summary
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    KpiChip(label = "ADDED SCENES", value = "+${sum.scenesAdded}", color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                    KpiChip(label = "REMOVED SCENES", value = "-${sum.scenesRemoved}", color = Color(0xFFE57373), modifier = Modifier.weight(1f))
                    KpiChip(label = "MODIFIED SCENES", value = "~${sum.scenesModified}", color = ElectrumGold, modifier = Modifier.weight(1f))
                    KpiChip(label = "BLOCK EDITS", value = "+${sum.blocksAdded} / -${sum.blocksRemoved}", color = LightGold, modifier = Modifier.weight(1f))
                    KpiChip(label = "NET WORDS", value = "${if (sum.wordsDelta >= 0) "+" else ""}${sum.wordsDelta}", color = ScreenplayTextDark, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter & Mode Toggle Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = filterTab,
                        containerColor = ObsidianBlack,
                        contentColor = ElectrumGold,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[filterTab]),
                                color = ElectrumGold,
                                height = 2.dp
                            )
                        },
                        modifier = Modifier
                            .width(420.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        Tab(selected = filterTab == 0, onClick = { filterTab = 0 }, text = { Text("ALL (${diffResult.sceneDiffs.size})", fontSize = 11.sp) })
                        Tab(selected = filterTab == 1, onClick = { filterTab = 1 }, text = { Text("CHANGES (${sum.scenesAdded + sum.scenesRemoved + sum.scenesModified})", fontSize = 11.sp) })
                        Tab(selected = filterTab == 2, onClick = { filterTab = 2 }, text = { Text("ADDED (${sum.scenesAdded})", fontSize = 11.sp) })
                        Tab(selected = filterTab == 3, onClick = { filterTab = 3 }, text = { Text("MODIFIED (${sum.scenesModified})", fontSize = 11.sp) })
                    }

                    // View Mode Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianBlack)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (!isSideBySideView) ElectrumGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { isSideBySideView = false }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Unified Diff", style = MaterialTheme.typography.labelSmall.copy(color = if (!isSideBySideView) ElectrumGold else ScreenplayTextMuted, fontSize = 11.sp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSideBySideView) ElectrumGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { isSideBySideView = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Side-by-Side", style = MaterialTheme.typography.labelSmall.copy(color = if (isSideBySideView) ElectrumGold else ScreenplayTextMuted, fontSize = 11.sp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filtered Scene Diff List
                val filteredScenes = remember(diffResult, filterTab) {
                    when (filterTab) {
                        1 -> diffResult.sceneDiffs.filter { it.type != DiffType.UNCHANGED }
                        2 -> diffResult.sceneDiffs.filter { it.type == DiffType.ADDED }
                        3 -> diffResult.sceneDiffs.filter { it.type == DiffType.MODIFIED }
                        else -> diffResult.sceneDiffs
                    }
                }

                if (filteredScenes.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Text("No differences found for this filter.", style = MaterialTheme.typography.bodyMedium.copy(color = ScreenplayTextMuted))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        items(filteredScenes) { sceneDiff ->
                            SceneDiffCard(sceneDiff = sceneDiff, isSideBySide = isSideBySideView)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 9.sp))
        }
    }
}

@Composable
private fun SceneDiffCard(sceneDiff: SceneDiff, isSideBySide: Boolean) {
    var isExpanded by remember { mutableStateOf(sceneDiff.type != DiffType.UNCHANGED) }

    val badgeColor = when (sceneDiff.type) {
        DiffType.ADDED -> Color(0xFF4CAF50)
        DiffType.REMOVED -> Color(0xFFE57373)
        DiffType.MODIFIED -> ElectrumGold
        DiffType.UNCHANGED -> ScreenplayTextMuted
    }
    val badgeText = when (sceneDiff.type) {
        DiffType.ADDED -> "ADDED"
        DiffType.REMOVED -> "REMOVED"
        DiffType.MODIFIED -> "MODIFIED"
        DiffType.UNCHANGED -> "UNCHANGED"
    }

    val heading = sceneDiff.headingB ?: sceneDiff.headingA ?: "UNTITLED SCENE"
    val num = sceneDiff.sceneNumberB ?: sceneDiff.sceneNumberA

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = badgeText, style = MaterialTheme.typography.labelSmall.copy(color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (num != null) "#$num $heading" else heading,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ScreenplayTextDark,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ScreenplayCourierFontFamily
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sceneDiff.type == DiffType.MODIFIED) {
                        Text(
                            text = "+${sceneDiff.stats.addedBlocks} / -${sceneDiff.stats.removedBlocks} / ~${sceneDiff.stats.modifiedBlocks}",
                            style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = ScreenplayTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(color = ObsidianSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSideBySide) {
                        SideBySideBlockView(sceneDiff.blockDiffs)
                    } else {
                        UnifiedBlockView(sceneDiff.blockDiffs)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnifiedBlockView(diffs: List<BlockDiff>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        diffs.forEach { diff ->
            when (diff.type) {
                DiffType.UNCHANGED -> {
                    Text(
                        text = diff.newText ?: diff.oldText ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ScreenplayTextMuted.copy(alpha = 0.7f),
                            fontFamily = ScreenplayCourierFontFamily,
                            fontSize = 11.sp
                        )
                    )
                }
                DiffType.ADDED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                            .padding(6.dp)
                    ) {
                        Column {
                            val spk = if (diff.speaker != null) " (${diff.speaker})" else ""
                            Text(
                                text = "+ [${diff.blockType.name}$spk]",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            )
                            Text(
                                text = diff.newText ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontFamily = ScreenplayCourierFontFamily, fontSize = 11.sp)
                            )
                        }
                    }
                }
                DiffType.REMOVED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE57373).copy(alpha = 0.15f))
                            .padding(6.dp)
                    ) {
                        Column {
                            val spk = if (diff.speaker != null) " (${diff.speaker})" else ""
                            Text(
                                text = "- [${diff.blockType.name}$spk]",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFE57373), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            )
                            Text(
                                text = diff.oldText ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE57373),
                                    textDecoration = TextDecoration.LineThrough,
                                    fontFamily = ScreenplayCourierFontFamily,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
                DiffType.MODIFIED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectrumGold.copy(alpha = 0.1f))
                            .padding(6.dp)
                    ) {
                        Column {
                            val spk = if (diff.speaker != null) " (${diff.speaker})" else ""
                            Text(
                                text = "~ [${diff.blockType.name}$spk]",
                                style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            )
                            Text(
                                text = "OLD: ${diff.oldText ?: ""}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE57373).copy(alpha = 0.8f),
                                    textDecoration = TextDecoration.LineThrough,
                                    fontFamily = ScreenplayCourierFontFamily,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "NEW: ${diff.newText ?: ""}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = ScreenplayCourierFontFamily,
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

@Composable
private fun SideBySideBlockView(diffs: List<BlockDiff>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        diffs.forEach { diff ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Column: Draft A
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (diff.type) {
                                DiffType.REMOVED -> Color(0xFFE57373).copy(alpha = 0.15f)
                                DiffType.MODIFIED -> Color(0xFFE57373).copy(alpha = 0.08f)
                                else -> ObsidianSurfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                        .padding(6.dp)
                ) {
                    Text(
                        text = diff.oldText ?: "—",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (diff.type == DiffType.REMOVED) Color(0xFFE57373) else ScreenplayTextMuted,
                            textDecoration = if (diff.type == DiffType.REMOVED) TextDecoration.LineThrough else null,
                            fontFamily = ScreenplayCourierFontFamily,
                            fontSize = 11.sp
                        )
                    )
                }

                // Right Column: Draft B
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (diff.type) {
                                DiffType.ADDED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                DiffType.MODIFIED -> Color(0xFF4CAF50).copy(alpha = 0.08f)
                                else -> ObsidianSurfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                        .padding(6.dp)
                ) {
                    Text(
                        text = diff.newText ?: "—",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (diff.type == DiffType.ADDED) Color(0xFF4CAF50) else ScreenplayTextDark,
                            fontWeight = if (diff.type == DiffType.ADDED || diff.type == DiffType.MODIFIED) FontWeight.Medium else FontWeight.Normal,
                            fontFamily = ScreenplayCourierFontFamily,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
