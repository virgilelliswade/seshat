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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.ai.CoverageVerdict
import com.seshat.writer.ai.StudioCoverageReport
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.ui.theme.LightGold
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seshat.writer.analytics.CastSceneMatrix
import com.seshat.writer.analytics.CharacterBreakdown
import com.seshat.writer.analytics.LocationAnalytics
import com.seshat.writer.analytics.LocationBreakdown
import com.seshat.writer.analytics.SceneMatrixItem
import com.seshat.writer.analytics.ScreenplayAnalyticsEngine
import com.seshat.writer.analytics.SettingType
import com.seshat.writer.breakdown.ProductionBreakdownEngine
import com.seshat.writer.breakdown.ProductionBreakdownPdfExporter
import com.seshat.writer.breakdown.ScriptBreakdownSummary
import com.seshat.writer.breakdown.SceneBreakdown
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.titlepage.ScreenplayDocumentStore
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import java.util.Locale

enum class CharacterSortMode {
    WORDS,
    SCENES,
    ALPHABETICAL
}

@Composable
fun CharacterReportDialog(
    viewModel: ScreenplayViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val titlePageData = ScreenplayDocumentStore.titlePage.value
    val scriptTitle = titlePageData.title.ifBlank { "UNTITLED SCREENPLAY" }

    var selectedTab by remember { mutableIntStateOf(uiState.reportsSelectedTab) }
    val aiConfig = remember { AiPreferences.load(context) }
    val showCoverageTab = aiConfig.isEnabled && aiConfig.enableStudioCoverage

    val characterAnalytics = remember(uiState.blocks) {
        viewModel.getScriptAnalytics()
    }
    val locationAnalytics = remember(uiState.blocks) {
        viewModel.getLocationAnalytics()
    }
    val castMatrix = remember(uiState.blocks) {
        viewModel.getCastSceneMatrix()
    }
    val breakdownSummary = remember(uiState.blocks) {
        viewModel.getProductionBreakdown()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ObsidianSurface,
            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "📊 PRODUCTION REPORTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ElectrumGold
                            )
                        )
                        Text(
                            text = "Script: $scriptTitle",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ScreenplayTextMuted
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-tab Navigation Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ObsidianBlack,
                    contentColor = ElectrumGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectrumGold,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            viewModel.setReportsSelectedTab(0)
                        },
                        text = {
                            Text(
                                text = "👤 CHARACTERS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) ElectrumGold else ScreenplayTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.setReportsSelectedTab(1)
                        },
                        text = {
                            Text(
                                text = "📍 LOCATIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) ElectrumGold else ScreenplayTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            viewModel.setReportsSelectedTab(2)
                        },
                        text = {
                            Text(
                                text = "🎬 CAST MATRIX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 2) ElectrumGold else ScreenplayTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = {
                            selectedTab = 3
                            viewModel.setReportsSelectedTab(3)
                        },
                        text = {
                            Text(
                                text = "📋 AD BREAKDOWN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 3) ElectrumGold else ScreenplayTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    )
                    if (showCoverageTab) {
                        Tab(
                            selected = selectedTab == 4,
                            onClick = {
                                selectedTab = 4
                                viewModel.setReportsSelectedTab(4)
                            },
                            text = {
                                Text(
                                    text = "✨ COVERAGE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 4) ElectrumGold else ScreenplayTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> CharacterReportTabContent(
                        analytics = characterAnalytics,
                        scriptTitle = scriptTitle,
                        context = context
                    )
                    1 -> LocationReportTabContent(
                        analytics = locationAnalytics,
                        scriptTitle = scriptTitle,
                        context = context
                    )
                    2 -> CastMatrixTabContent(
                        matrix = castMatrix,
                        scriptTitle = scriptTitle,
                        context = context
                    )
                    3 -> ProductionBreakdownTabContent(
                        summary = breakdownSummary,
                        scriptTitle = scriptTitle,
                        context = context,
                        viewModel = viewModel
                    )
                    4 -> StudioCoverageTabContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        scriptTitle = scriptTitle,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterReportTabContent(
    analytics: com.seshat.writer.analytics.ScriptAnalytics,
    scriptTitle: String,
    context: Context
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(CharacterSortMode.WORDS) }
    var expandedCharacterNames by remember { mutableStateOf(setOf<String>()) }

    val filteredCharacters = remember(analytics, searchQuery, sortMode) {
        val list = if (searchQuery.isBlank()) {
            analytics.characterBreakdowns
        } else {
            val q = searchQuery.trim().uppercase(Locale.US)
            analytics.characterBreakdowns.filter { it.characterName.contains(q) }
        }

        when (sortMode) {
            CharacterSortMode.WORDS -> list.sortedByDescending { it.totalWords }
            CharacterSortMode.SCENES -> list.sortedByDescending { it.speakingSceneNumbers.size }
            CharacterSortMode.ALPHABETICAL -> list.sortedBy { it.characterName }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Analytics Summary Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricChip(label = "CHARACTERS", value = analytics.totalCharacters.toString(), modifier = Modifier.weight(1f))
            MetricChip(label = "SPEECHES", value = analytics.totalDialogueCues.toString(), modifier = Modifier.weight(1f))
            MetricChip(label = "WORDS", value = analytics.totalDialogueWords.toString(), modifier = Modifier.weight(1f))
            MetricChip(label = "% SPOKEN", value = String.format(Locale.US, "%.1f%%", analytics.percentageSpoken), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search & Sort Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 12.sp),
                cursorBrush = SolidColor(ElectrumGold),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(ObsidianBlack, RoundedCornerShape(6.dp))
                            .border(0.5.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Filter character...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ScreenplayTextMuted,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.weight(1f)
            )

            SortButton(
                text = "Words",
                isSelected = sortMode == CharacterSortMode.WORDS,
                onClick = { sortMode = CharacterSortMode.WORDS }
            )
            SortButton(
                text = "Scenes",
                isSelected = sortMode == CharacterSortMode.SCENES,
                onClick = { sortMode = CharacterSortMode.SCENES }
            )
            SortButton(
                text = "A-Z",
                isSelected = sortMode == CharacterSortMode.ALPHABETICAL,
                onClick = { sortMode = CharacterSortMode.ALPHABETICAL }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Copy Text & Export PDF
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val reportText = ScreenplayAnalyticsEngine.formatTextReport(analytics, scriptTitle)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Character Report", reportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Character Report copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = "📋 COPY REPORT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        try {
                            val pdfFile = ScriptFileManager.exportCharacterReport(context, analytics, scriptTitle)
                            Toast.makeText(context, "Report saved: ${pdfFile.name}", Toast.LENGTH_SHORT).show()
                            ScriptFileManager.shareFile(context, pdfFile, "application/pdf", "Share Character Report")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = "📄 EXPORT PDF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = ObsidianSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Character List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredCharacters.isEmpty()) {
                item {
                    Text(
                        text = "No characters found matching query.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted),
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            } else {
                itemsIndexed(filteredCharacters) { idx, char ->
                    val isExpanded = expandedCharacterNames.contains(char.characterName)
                    CharacterCard(
                        index = idx + 1,
                        char = char,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedCharacterNames = if (isExpanded) {
                                expandedCharacterNames - char.characterName
                            } else {
                                expandedCharacterNames + char.characterName
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationReportTabContent(
    analytics: LocationAnalytics,
    scriptTitle: String,
    context: Context
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterSetting by remember { mutableStateOf<SettingType?>(null) }
    var expandedLocations by remember { mutableStateOf(setOf<String>()) }

    val filteredBreakdowns = remember(analytics, searchQuery, selectedFilterSetting) {
        var list = analytics.locationBreakdowns
        if (selectedFilterSetting != null) {
            list = list.filter { it.primarySetting == selectedFilterSetting }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().uppercase(Locale.US)
            list = list.filter { it.locationName.contains(q) }
        }
        list
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Summary Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricChip(label = "UNIQUE SETS", value = analytics.totalUniqueLocations.toString(), modifier = Modifier.weight(1f))
            MetricChip(label = "TOTAL SCENES", value = analytics.totalScenes.toString(), modifier = Modifier.weight(1f))
            MetricChip(label = "INT / EXT", value = "${analytics.intScenesCount}/${analytics.extScenesCount}", modifier = Modifier.weight(1f))
            MetricChip(label = "DAY / NIGHT", value = "${analytics.dayScenesCount}/${analytics.nightScenesCount}", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 12.sp),
                cursorBrush = SolidColor(ElectrumGold),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(ObsidianBlack, RoundedCornerShape(6.dp))
                            .border(0.5.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Filter location or set...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ScreenplayTextMuted,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.weight(1f)
            )

            SortButton(
                text = "ALL",
                isSelected = selectedFilterSetting == null,
                onClick = { selectedFilterSetting = null }
            )
            SortButton(
                text = "INT.",
                isSelected = selectedFilterSetting == SettingType.INTERIOR,
                onClick = { selectedFilterSetting = SettingType.INTERIOR }
            )
            SortButton(
                text = "EXT.",
                isSelected = selectedFilterSetting == SettingType.EXTERIOR,
                onClick = { selectedFilterSetting = SettingType.EXTERIOR }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Copy Text & Export PDF
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val reportText = ScreenplayAnalyticsEngine.formatLocationReportText(analytics, scriptTitle)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Location Report", reportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Location Report copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = "📋 COPY REPORT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        try {
                            val pdfFile = ScriptFileManager.exportLocationReport(context, analytics, scriptTitle)
                            Toast.makeText(context, "Location Report saved: ${pdfFile.name}", Toast.LENGTH_SHORT).show()
                            ScriptFileManager.shareFile(context, pdfFile, "application/pdf", "Share Location Report")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = "📄 EXPORT PDF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = ObsidianSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Locations List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredBreakdowns.isEmpty()) {
                item {
                    Text(
                        text = "No locations or sets found matching filter.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted),
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            } else {
                itemsIndexed(filteredBreakdowns) { idx, loc ->
                    val isExpanded = expandedLocations.contains(loc.locationName)
                    LocationCard(
                        index = idx + 1,
                        loc = loc,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedLocations = if (isExpanded) {
                                expandedLocations - loc.locationName
                            } else {
                                expandedLocations + loc.locationName
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationCard(
    index: Int,
    loc: LocationBreakdown,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onToggleExpand() }
            .border(0.5.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$index.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScreenplayTextMuted
                        ),
                        modifier = Modifier.width(22.dp)
                    )
                    Text(
                        text = loc.locationName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ElectrumGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = loc.primarySetting.prefix,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectrumGold
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${loc.totalScenes} sc",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = ScreenplayTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Day: ${loc.dayScenes} • Night: ${loc.nightScenes} • Scenes: ${loc.sceneNumbers.take(6).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 11.sp
                    )
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = ObsidianSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (loc.uniqueCharacters.isNotEmpty()) {
                        Text(
                            text = "CAST PRESENT: ${loc.uniqueCharacters.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectrumGold,
                                fontSize = 9.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    loc.scenes.forEach { sc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scene ${sc.sceneNumber} • [Pg ${sc.pageNumber}] • ${sc.timeOfDay}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectrumGold.copy(alpha = 0.9f)
                                    )
                                )
                                Text(
                                    text = sc.sceneHeading,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = ScreenplayTextMuted
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (sc.speakingCharacters.isNotEmpty()) {
                                Text(
                                    text = sc.speakingCharacters.take(3).joinToString(", "),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = ScreenplayTextDark
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CastMatrixTabContent(
    matrix: CastSceneMatrix,
    scriptTitle: String,
    context: Context
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredScenes = remember(matrix, searchQuery) {
        if (searchQuery.isBlank()) {
            matrix.scenes
        } else {
            val q = searchQuery.trim().uppercase(Locale.US)
            matrix.scenes.filter { sc ->
                sc.sceneNumber.contains(q) ||
                sc.sceneHeading.uppercase(Locale.US).contains(q) ||
                sc.characters.any { it.contains(q) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Summary Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MetricChip(label = "CAST MEMBERS", value = matrix.allCharacters.size.toString(), modifier = Modifier.weight(1f))
            MetricChip(label = "TOTAL SCENES", value = matrix.scenes.size.toString(), modifier = Modifier.weight(1f))
            val omittedCount = matrix.scenes.count { it.isOmitted }
            val activeCount = matrix.scenes.size - omittedCount
            MetricChip(label = "ACTIVE SCENES", value = activeCount.toString(), modifier = Modifier.weight(1f))
            MetricChip(label = "OMITTED", value = omittedCount.toString(), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field
        BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            textStyle = TextStyle(color = ScreenplayTextDark, fontSize = 12.sp),
            cursorBrush = SolidColor(ElectrumGold),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianBlack, RoundedCornerShape(6.dp))
                        .border(0.5.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Filter scene or cast member...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScreenplayTextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Copy Text & Export PDF
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val reportText = ScreenplayAnalyticsEngine.formatCastMatrixText(matrix, scriptTitle)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Cast-Scene Matrix", reportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Cast Matrix copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = "📋 COPY MATRIX",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        try {
                            val pdfFile = ScriptFileManager.exportCastMatrix(context, matrix, scriptTitle)
                            Toast.makeText(context, "Cast Matrix saved: ${pdfFile.name}", Toast.LENGTH_SHORT).show()
                            ScriptFileManager.shareFile(context, pdfFile, "application/pdf", "Share Cast Matrix")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = "📄 EXPORT PDF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectrumGold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = ObsidianSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Call Sheet Grid List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (filteredScenes.isEmpty()) {
                item {
                    Text(
                        text = "No scenes or characters found matching query.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted),
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            } else {
                itemsIndexed(filteredScenes) { _, sc ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (sc.isOmitted) ObsidianSurfaceVariant.copy(alpha = 0.5f) else ObsidianBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Sc. ${sc.sceneNumber}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (sc.isOmitted) ScreenplayTextMuted else ElectrumGold,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.width(55.dp)
                                    )
                                    Text(
                                        text = sc.sceneHeading,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (sc.isOmitted) ScreenplayTextMuted else ScreenplayTextDark,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (sc.isOmitted) {
                                        Surface(
                                            color = Color(0xFFC0392B).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(3.dp),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = "OMITTED",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE74C3C)
                                                ),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "p.${sc.pageNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = ScreenplayTextMuted
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (sc.characters.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    sc.characters.forEach { charName ->
                                        Surface(
                                            color = ElectrumGold.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = charName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ElectrumGold
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = if (sc.isOmitted) "Scene omitted from shooting draft" else "[Silent / Non-speaking]",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = ScreenplayTextMuted
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

@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectrumGold,
                    fontSize = 13.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = ScreenplayTextMuted
                )
            )
        }
    }
}

@Composable
private fun SortButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .border(
                0.5.dp,
                if (isSelected) ElectrumGold else ElectrumGold.copy(alpha = 0.2f),
                RoundedCornerShape(4.dp)
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) ElectrumGold else ScreenplayTextMuted
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CharacterCard(
    index: Int,
    char: CharacterBreakdown,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onToggleExpand() }
            .border(0.5.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$index.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScreenplayTextMuted
                        ),
                        modifier = Modifier.width(22.dp)
                    )
                    Text(
                        text = char.characterName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${char.totalWords}w (${String.format(Locale.US, "%.1f%%", char.percentageOfDialogue)})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextDark,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = ScreenplayTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle stats: Scenes & Cues
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${char.totalSpeeches} dialogue cues in ${char.speakingSceneNumbers.size} scenes",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "Scenes: ${char.speakingSceneNumbers.take(6).joinToString(", ")}${if (char.speakingSceneNumbers.size > 6) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 11.sp
                    )
                )
            }

            // Expanded Speeches Drawer
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = ObsidianSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "DIALOGUE SPEECHES (${char.speeches.size}):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    char.speeches.forEachIndexed { _, speech ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Sc. ${speech.sceneNumber} • p. ${speech.pageNumber} • ${speech.sceneHeading}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = ElectrumGold.copy(alpha = 0.8f)
                                )
                            )
                            if (!speech.parenthetical.isNullOrBlank()) {
                                Text(
                                    text = "(${speech.parenthetical})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = ScreenplayTextMuted
                                    )
                                )
                            }
                            Text(
                                text = "\"${speech.dialogueText}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = ScreenplayTextDark
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
private fun StudioCoverageTabContent(
    viewModel: ScreenplayViewModel,
    uiState: ScreenplayUiState,
    scriptTitle: String,
    context: Context
) {
    val report = uiState.studioCoverageReport
    val isLoading = uiState.isStudioCoverageLoading

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (report == null) {
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨ HOLLYWOOD STUDIO READER COVERAGE",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Generate professional studio executive coverage: logline, narrative synopsis (Acts I-III), character arc evaluations, craft review, and Pass/Consider/Recommend verdict rating.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ScreenplayTextMuted,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ElectrumGold,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Reading screenplay and synthesizing coverage...",
                                style = MaterialTheme.typography.bodySmall.copy(color = ElectrumGold)
                            )
                        }
                    } else {
                        Surface(
                            color = ElectrumGold,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.generateStudioCoverage(context) }
                        ) {
                            Text(
                                text = "GENERATE STUDIO COVERAGE REPORT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                ),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Header summary bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val verdictBg = when (report.recommendation) {
                        CoverageVerdict.RECOMMEND -> Color(0xFF1B5E20)
                        CoverageVerdict.CONSIDER -> Color(0xFF4A3B00)
                        CoverageVerdict.PASS -> Color(0xFF4A1010)
                    }
                    val verdictColor = when (report.recommendation) {
                        CoverageVerdict.RECOMMEND -> Color(0xFF4CAF50)
                        CoverageVerdict.CONSIDER -> ElectrumGold
                        CoverageVerdict.PASS -> Color(0xFFE57373)
                    }
                    Surface(
                        color = verdictBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, verdictColor)
                    ) {
                        Text(
                            text = "VERDICT: ${report.recommendation.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = verdictColor
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = "Score: ${report.premiseScore}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScreenplayTextDark
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Export PDF
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, ElectrumGold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val file = viewModel.exportStudioCoveragePdf(context, report)
                                if (file != null) {
                                    Toast.makeText(context, "Saved coverage to: ${file.name}", Toast.LENGTH_LONG).show()
                                }
                            }
                    ) {
                        Text(
                            text = "📄 EXPORT PDF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectrumGold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Copy text
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Studio Coverage", report.formatPlainText()))
                                Toast.makeText(context, "Copied coverage to clipboard", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Text(
                            text = "📋 COPY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ScreenplayTextDark
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Re-generate
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !isLoading) {
                                viewModel.generateStudioCoverage(context)
                            }
                    ) {
                        Text(
                            text = if (isLoading) "..." else "🔄",
                            style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextDark),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Logline Card
            CoverageSectionCard(title = "1. LOGLINE", content = report.logline)

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Narrative Synopsis Card
            CoverageSectionCard(title = "2. NARRATIVE SYNOPSIS", content = report.synopsis)

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Character Arcs
            if (report.characterArcs.isNotEmpty()) {
                Surface(
                    color = ObsidianBlack,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "3. CHARACTER ARCS & CRUCIBLES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ElectrumGold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        for (arc in report.characterArcs) {
                            Text(
                                text = "• ${arc.name.uppercase(java.util.Locale.US)}: ${arc.arc}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ScreenplayTextDark,
                                    lineHeight = 18.sp
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 4. Craft Review Card
            Surface(
                color = ObsidianBlack,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "4. CRAFT EVALUATIONS & MARKETABILITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = ElectrumGold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DIALOGUE & SUBTEXT:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectrumGold)
                    )
                    Text(
                        text = report.dialogueReview,
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, lineHeight = 18.sp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "PACING & STRUCTURE:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectrumGold)
                    )
                    Text(
                        text = report.pacingReview,
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, lineHeight = 18.sp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "COMMERCIAL PROSPECTS:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectrumGold)
                    )
                    Text(
                        text = report.commercialProspects,
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, lineHeight = 18.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverageSectionCard(title: String, content: String) {
    Surface(
        color = ObsidianBlack,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ObsidianSurfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = LightGold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ScreenplayTextDark,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductionBreakdownTabContent(
    summary: ScriptBreakdownSummary,
    scriptTitle: String,
    context: Context,
    viewModel: ScreenplayViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp)
    ) {
        // Summary & Export Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "TOTAL: ${summary.totalScenes} SCENES  •  ${summary.totalPagesFormatted}  •  ${summary.uniqueCastCount} CAST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectrumGold,
                    letterSpacing = 0.5.sp
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = {
                        val report = ProductionBreakdownEngine.generateBreakdownTextReport(summary, scriptTitle)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AD Breakdown Report", report))
                        Toast.makeText(context, "Breakdown report copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Copy Report", style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val file = viewModel.exportProductionBreakdownPdf(context)
                        ScriptFileManager.shareFile(context, file, "application/pdf", "Assistant Director Breakdown")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Export AD PDF", style = MaterialTheme.typography.labelSmall.copy(color = ObsidianBlack, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Inventory KPI Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BreakdownKpiPill(label = "PROPS", count = summary.uniquePropsCount, color = Color(0xFFFFB74D), modifier = Modifier.weight(1f))
            BreakdownKpiPill(label = "WARDROBE", count = summary.uniqueWardrobeCount, color = Color(0xFFBA68C8), modifier = Modifier.weight(1f))
            BreakdownKpiPill(label = "VEHICLES", count = summary.uniqueVehiclesCount, color = Color(0xFF64B5F6), modifier = Modifier.weight(1f))
            BreakdownKpiPill(label = "STUNTS", count = summary.uniqueStuntsCount, color = Color(0xFFFF8A65), modifier = Modifier.weight(1f))
            BreakdownKpiPill(label = "SFX/VFX", count = summary.uniqueSfxCount, color = Color(0xFF4DD0E1), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scene Breakdown List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(summary.scenes) { _, scene ->
                SceneBreakdownCard(scene = scene)
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                MasterInventoryCard(summary = summary)
            }
        }
    }
}

@Composable
private fun BreakdownKpiPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Text(text = "$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 8.sp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SceneBreakdownCard(scene: SceneBreakdown) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ObsidianSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectrumGold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SCENE ${scene.sceneNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectrumGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scene.heading,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ScreenplayTextDark,
                            fontWeight = FontWeight.Bold,
                            fontFamily = ScreenplayCourierFontFamily,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${scene.pageCountEighths} (pg ${scene.startPage})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subline metadata
            Text(
                text = "${scene.intExt} ${scene.location}  •  ${scene.timeOfDay}  •  ${scene.scriptDay}",
                style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted.copy(alpha = 0.8f), fontSize = 10.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = ObsidianSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Cast Members
            if (scene.castMembers.isNotEmpty()) {
                CategoryTagRow(title = "CAST", items = scene.castMembers, tagColor = ElectrumGold)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Props
            if (scene.props.isNotEmpty()) {
                CategoryTagRow(title = "PROPS", items = scene.props, tagColor = Color(0xFFFFB74D))
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Wardrobe
            if (scene.wardrobe.isNotEmpty()) {
                CategoryTagRow(title = "WARDROBE", items = scene.wardrobe, tagColor = Color(0xFFBA68C8))
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Vehicles
            if (scene.vehicles.isNotEmpty()) {
                CategoryTagRow(title = "VEHICLES", items = scene.vehicles, tagColor = Color(0xFF64B5F6))
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Stunts
            if (scene.stunts.isNotEmpty()) {
                CategoryTagRow(title = "STUNTS", items = scene.stunts, tagColor = Color(0xFFFF8A65))
                Spacer(modifier = Modifier.height(6.dp))
            }

            // SFX / VFX
            if (scene.specialEffects.isNotEmpty()) {
                CategoryTagRow(title = "SFX/VFX", items = scene.specialEffects, tagColor = Color(0xFF4DD0E1))
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Sound
            if (scene.soundNotes.isNotEmpty()) {
                CategoryTagRow(title = "SOUND", items = scene.soundNotes, tagColor = Color(0xFF81C784))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryTagRow(title: String, items: List<String>, tagColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$title:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = tagColor,
                fontSize = 9.sp
            ),
            modifier = Modifier.width(68.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(tagColor.copy(alpha = 0.15f))
                        .border(0.5.dp, tagColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = tagColor,
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MasterInventoryCard(summary: ScriptBreakdownSummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📦 MASTER PRODUCTION ELEMENT INVENTORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectrumGold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (summary.allProps.isNotEmpty()) {
                CategoryTagRow(title = "ALL PROPS (${summary.allProps.size})", items = summary.allProps, tagColor = Color(0xFFFFB74D))
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (summary.allWardrobe.isNotEmpty()) {
                CategoryTagRow(title = "ALL WARDROBE (${summary.allWardrobe.size})", items = summary.allWardrobe, tagColor = Color(0xFFBA68C8))
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (summary.allVehicles.isNotEmpty()) {
                CategoryTagRow(title = "ALL VEHICLES (${summary.allVehicles.size})", items = summary.allVehicles, tagColor = Color(0xFF64B5F6))
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (summary.allStunts.isNotEmpty()) {
                CategoryTagRow(title = "ALL STUNTS (${summary.allStunts.size})", items = summary.allStunts, tagColor = Color(0xFFFF8A65))
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (summary.allSfx.isNotEmpty()) {
                CategoryTagRow(title = "ALL SFX (${summary.allSfx.size})", items = summary.allSfx, tagColor = Color(0xFF4DD0E1))
            }
        }
    }
}
