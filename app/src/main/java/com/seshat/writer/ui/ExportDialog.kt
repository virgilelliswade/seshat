package com.seshat.writer.ui

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.seshat.writer.export.FinalDraftSerializer
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.export.SavedScriptInfo
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.bundle.SeshatBundleManager
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.titlepage.ScreenplayDocumentStore
import com.seshat.writer.titlepage.TitlePageData
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExportDialog(
    viewModel: ScreenplayViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.value
    val titlePageData = ScreenplayDocumentStore.titlePage.value

    var selectedTab by remember { mutableIntStateOf(0) }
    var includeTitlePageInPdf by remember { mutableStateOf(true) }
    var includeSceneNumbersInPdf by remember { mutableStateOf(uiState.showSceneNumbers) }
    var includeNotesInPdf by remember { mutableStateOf(false) }
    var includeRevisionAsterisksInPdf by remember { mutableStateOf(true) }
    var printColoredPagesInPdf by remember { mutableStateOf(uiState.showColoredDraftTint) }
    var enableWatermark by remember { mutableStateOf(false) }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var watermarkOpacity by remember { mutableStateOf(0.25f) }
    var watermarkRecipient by remember { mutableStateOf("") }
    var enableCustomHeader by remember { mutableStateOf(false) }
    var customHeaderText by remember {
        mutableStateOf(titlePageData.title.ifBlank { "UNTITLED SCREENPLAY" }.uppercase(Locale.US))
    }
    var savedScripts by remember { mutableStateOf<List<SavedScriptInfo>>(emptyList()) }

    fun refreshSavedList() {
        savedScripts = ScriptFileManager.listSavedScripts(context)
    }

    LaunchedEffect(Unit) {
        refreshSavedList()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FILE & EXPORT",
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

                // Tabs: 0 -> Export, 1 -> Saved Scripts
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
                                "Export",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) ElectrumGold else ScreenplayTextMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            refreshSavedList()
                        },
                        text = {
                            Text(
                                "Library (${savedScripts.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) ElectrumGold else ScreenplayTextMuted
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Export Tab Content
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Current script info
                        Text(
                            text = "CURRENT SCRIPT: ${titlePageData.title.ifBlank { "UNTITLED SCREENPLAY" }}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ScreenplayTextMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 1. PDF Export Card
                        ExportActionCard(
                            title = "Industry PDF (US Letter)",
                            subtitle = "8.5\" × 11\" printable Hollywood standard PDF",
                            buttonText = "Export & Share PDF",
                            extraContent = {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { includeTitlePageInPdf = !includeTitlePageInPdf }
                                    ) {
                                        Checkbox(
                                            checked = includeTitlePageInPdf,
                                            onCheckedChange = { includeTitlePageInPdf = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ElectrumGold,
                                                checkmarkColor = ObsidianBlack
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Include Title Page",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { includeSceneNumbersInPdf = !includeSceneNumbersInPdf }
                                    ) {
                                        Checkbox(
                                            checked = includeSceneNumbersInPdf,
                                            onCheckedChange = { includeSceneNumbersInPdf = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ElectrumGold,
                                                checkmarkColor = ObsidianBlack
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Scene Numbers (Shooting Script)",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { includeNotesInPdf = !includeNotesInPdf }
                                    ) {
                                        Checkbox(
                                            checked = includeNotesInPdf,
                                            onCheckedChange = { includeNotesInPdf = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ElectrumGold,
                                                checkmarkColor = ObsidianBlack
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Include Writer's Notes in PDF",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                        )
                                    }

                                    // Revision Options
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { includeRevisionAsterisksInPdf = !includeRevisionAsterisksInPdf }
                                    ) {
                                        Checkbox(
                                            checked = includeRevisionAsterisksInPdf,
                                            onCheckedChange = { includeRevisionAsterisksInPdf = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ElectrumGold,
                                                checkmarkColor = ObsidianBlack
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Revision Asterisks (*) in Margin",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                        )
                                    }
                                    if (uiState.currentRevisionColor != com.seshat.writer.model.RevisionColor.WHITE) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { printColoredPagesInPdf = !printColoredPagesInPdf }
                                        ) {
                                            Checkbox(
                                                checked = printColoredPagesInPdf,
                                                onCheckedChange = { printColoredPagesInPdf = it },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = ElectrumGold,
                                                    checkmarkColor = ObsidianBlack
                                                ),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Colored Draft Pages (${uiState.currentRevisionColor.displayName})",
                                                style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                            )
                                        }
                                    }

                                    // Watermark Option
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { enableWatermark = !enableWatermark }
                                    ) {
                                        Checkbox(
                                            checked = enableWatermark,
                                            onCheckedChange = { enableWatermark = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ElectrumGold,
                                                checkmarkColor = ObsidianBlack
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Diagonal Watermark",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                        )
                                    }
                                    if (enableWatermark) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 30.dp, end = 4.dp, bottom = 4.dp)
                                        ) {
                                            BasicTextField(
                                                value = watermarkText,
                                                onValueChange = { watermarkText = it },
                                                textStyle = TextStyle(
                                                    color = ElectrumGold,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                cursorBrush = SolidColor(ElectrumGold),
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ObsidianBlack, RoundedCornerShape(4.dp))
                                                    .border(1.dp, ElectrumGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                val presets = listOf("CONFIDENTIAL", "FIRST DRAFT", "FOR REVIEW ONLY", "REVISED")
                                                for (preset in presets) {
                                                    Surface(
                                                        color = if (watermarkText.equals(preset, ignoreCase = true)) ElectrumGold.copy(alpha = 0.3f) else ObsidianSurfaceVariant,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(0.5.dp, ElectrumGold.copy(alpha = 0.4f)),
                                                        modifier = Modifier.clickable { watermarkText = preset }
                                                    ) {
                                                        Text(
                                                            text = preset,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                color = if (watermarkText.equals(preset, ignoreCase = true)) ElectrumGold else ScreenplayTextMuted
                                                            ),
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Recipient Name Field
                                            BasicTextField(
                                                value = watermarkRecipient,
                                                onValueChange = { watermarkRecipient = it },
                                                textStyle = TextStyle(
                                                    color = ScreenplayTextDark,
                                                    fontSize = 11.sp
                                                ),
                                                cursorBrush = SolidColor(ElectrumGold),
                                                singleLine = true,
                                                decorationBox = { inner ->
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(ObsidianBlack, RoundedCornerShape(4.dp))
                                                            .border(0.5.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                                    ) {
                                                        if (watermarkRecipient.isEmpty()) {
                                                            Text(
                                                                text = "Recipient Name (Optional, e.g. JANE DOE)",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 10.sp,
                                                                    color = ScreenplayTextMuted
                                                                )
                                                            )
                                                        }
                                                        inner()
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Opacity selector pills
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Opacity:",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        color = ScreenplayTextMuted
                                                    )
                                                )
                                                val opacityOptions = listOf(
                                                    Pair("Subtle 15%", 0.15f),
                                                    Pair("Standard 25%", 0.25f),
                                                    Pair("Bold 40%", 0.40f)
                                                )
                                                for ((lbl, opVal) in opacityOptions) {
                                                    Surface(
                                                        color = if (watermarkOpacity == opVal) ElectrumGold.copy(alpha = 0.3f) else ObsidianSurfaceVariant,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(0.5.dp, if (watermarkOpacity == opVal) ElectrumGold else ElectrumGold.copy(alpha = 0.3f)),
                                                        modifier = Modifier.clickable { watermarkOpacity = opVal }
                                                    ) {
                                                        Text(
                                                            text = lbl,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                fontWeight = if (watermarkOpacity == opVal) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (watermarkOpacity == opVal) ElectrumGold else ScreenplayTextMuted
                                                            ),
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Custom Running Header (Page 2+)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { enableCustomHeader = !enableCustomHeader }
                                    ) {
                                        Checkbox(
                                            checked = enableCustomHeader,
                                            onCheckedChange = { enableCustomHeader = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ElectrumGold,
                                                checkmarkColor = ObsidianBlack
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Custom Top Header (Page 2+)",
                                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark)
                                        )
                                    }
                                    if (enableCustomHeader) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 30.dp, end = 4.dp, bottom = 4.dp)
                                        ) {
                                            BasicTextField(
                                                value = customHeaderText,
                                                onValueChange = { customHeaderText = it },
                                                textStyle = TextStyle(
                                                    color = ElectrumGold,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                cursorBrush = SolidColor(ElectrumGold),
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ObsidianBlack, RoundedCornerShape(4.dp))
                                                    .border(1.dp, ElectrumGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                try {
                                    val finalWatermark = if (enableWatermark && watermarkText.isNotBlank()) watermarkText.trim() else null
                                    val finalRecipient = if (enableWatermark && watermarkRecipient.isNotBlank()) watermarkRecipient.trim() else null
                                    val finalHeader = if (enableCustomHeader && customHeaderText.isNotBlank()) customHeaderText.trim() else null
                                    val file = ScriptFileManager.exportPdf(
                                        context = context,
                                        blocks = uiState.blocks,
                                        titlePageData = titlePageData,
                                        includeTitlePage = includeTitlePageInPdf,
                                        showSceneNumbers = includeSceneNumbersInPdf,
                                        watermarkText = finalWatermark,
                                        watermarkOpacity = watermarkOpacity,
                                        watermarkRecipient = finalRecipient,
                                        headerText = finalHeader,
                                        includeNotes = includeNotesInPdf,
                                        revisionColor = uiState.currentRevisionColor,
                                        revisionDate = uiState.revisionDate,
                                        printRevisionAsterisks = includeRevisionAsterisksInPdf,
                                        printColoredPages = printColoredPagesInPdf
                                    )
                                    ScriptFileManager.shareFile(context, file, "application/pdf", "Share PDF")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 2. Final Draft .fdx Export Card
                        ExportActionCard(
                            title = "Final Draft (.fdx)",
                            subtitle = "Universal XML format for Final Draft 10–13",
                            buttonText = "Export .fdx",
                            onClick = {
                                try {
                                    val file = ScriptFileManager.exportFdx(
                                        context = context,
                                        blocks = uiState.blocks,
                                        titlePageData = titlePageData
                                    )
                                    ScriptFileManager.shareFile(context, file, "application/xml", "Share Final Draft Script")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 3. Fountain .fountain Export Card
                        ExportActionCard(
                            title = "Fountain (.fountain)",
                            subtitle = "Plain text screenwriting format for Highland & WriterDuet",
                            buttonText = "Export .fountain",
                            onClick = {
                                try {
                                    val file = ScriptFileManager.exportFountain(
                                        context = context,
                                        blocks = uiState.blocks,
                                        titlePageData = titlePageData
                                    )
                                    ScriptFileManager.shareFile(context, file, "text/plain", "Share Fountain Script")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 3b. Seshat Project Bundle (.seshat) Export Card
                        val isTv = uiState.projectFormat == ProjectFormat.TV_SERIES
                        val bundleTitle = if (isTv) "Seshat Show Project Bundle (.seshat)" else "Seshat Project Bundle (.seshat)"
                        val bundleSubtitle = if (isTv) {
                            "Complete TV Series package: All ${uiState.tvSeriesProject?.episodes?.size ?: 0} episodes, corkboards & shared series lore bible in one cloud-ready file"
                        } else {
                            "Self-contained project package: Screenplay, corkboard outline & lore bible for cloud backup and co-writing"
                        }

                        ExportActionCard(
                            title = bundleTitle,
                            subtitle = bundleSubtitle,
                            buttonText = "Export & Share .seshat Bundle",
                            onClick = {
                                try {
                                    val exported = if (isTv && uiState.tvSeriesProject != null) {
                                        SeshatBundleManager.exportTvSeriesBundle(context, uiState.tvSeriesProject!!)
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
                                        ScriptFileManager.shareFile(context, exported, "application/zip", "Share Seshat Bundle")
                                    } else {
                                        Toast.makeText(context, "Export error", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 4. Character Breakdown & Dialogue Report Card
                        ExportActionCard(
                            title = "Character Breakdown & Dialogue Report",
                            subtitle = "Casting & production PDF report with speeches and character stats",
                            buttonText = "Export & Share Report PDF",
                            onClick = {
                                try {
                                    val analytics = viewModel.getScriptAnalytics()
                                    val file = ScriptFileManager.exportCharacterReport(
                                        context = context,
                                        analytics = analytics,
                                        title = titlePageData.title.ifBlank { "Screenplay" }
                                    )
                                    ScriptFileManager.shareFile(context, file, "application/pdf", "Share Character Report")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 5. Location & Set Breakdown Report Card
                        ExportActionCard(
                            title = "Location & Set Breakdown Report",
                            subtitle = "Production set logistics, INT/EXT & Day/Night statistics, cast present",
                            buttonText = "Export Location Report PDF",
                            onClick = {
                                try {
                                    val locAnalytics = viewModel.getLocationAnalytics()
                                    val file = ScriptFileManager.exportLocationReport(
                                        context = context,
                                        analytics = locAnalytics,
                                        title = titlePageData.title.ifBlank { "Screenplay" }
                                    )
                                    ScriptFileManager.shareFile(context, file, "application/pdf", "Share Location Report")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 6. Cast-Scene Breakdown Matrix Card
                        ExportActionCard(
                            title = "Cast-Scene Breakdown Matrix (Call Sheet)",
                            subtitle = "Call sheet grid with scene-by-scene cast breakdown and attendance",
                            buttonText = "Export Cast Matrix PDF",
                            onClick = {
                                try {
                                    val matrix = viewModel.getCastSceneMatrix()
                                    val file = ScriptFileManager.exportCastMatrix(
                                        context = context,
                                        matrix = matrix,
                                        title = titlePageData.title.ifBlank { "Screenplay" }
                                    )
                                    ScriptFileManager.shareFile(context, file, "application/pdf", "Share Cast Matrix")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 4. Quick Save to Device Library
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val file = ScriptFileManager.saveScript(
                                            context = context,
                                            blocks = uiState.blocks,
                                            titlePageData = titlePageData,
                                            format = "fountain"
                                        )
                                        refreshSavedList()
                                        Toast.makeText(context, "Saved to library: ${file.name}", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Save error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .border(1.dp, ElectrumGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "💾 Save Current Script to Library",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectrumGold
                                ),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }
                    }
                } else {
                    // Library Tab Content
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = ObsidianBlack,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.newDocument()
                                        onDismiss()
                                        Toast.makeText(context, "New screenplay created", Toast.LENGTH_SHORT).show()
                                    }
                                    .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "➕ New Script",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectrumGold,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Text(
                                text = "Tap script to open",
                                style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (savedScripts.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                Text(
                                    text = "No saved scripts found in library.\nUse the Export tab to save the current script.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(260.dp)
                            ) {
                                items(savedScripts) { script ->
                                    val dateStr = remember(script.lastModified) {
                                        val fmt = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
                                        fmt.format(Date(script.lastModified))
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    val (blocks, titlePage) = if (script.file.extension == "fdx") {
                                                        val doc = FinalDraftSerializer.importFromFdx(script.file.readText())
                                                        Pair(doc.blocks, doc.titlePage)
                                                    } else {
                                                        val doc = FountainSerializer.importFromFountain(script.file.readText())
                                                        Pair(doc.blocks, doc.titlePage)
                                                    }
                                                    viewModel.loadDocument(blocks, titlePage)
                                                    onDismiss()
                                                    Toast.makeText(context, "Loaded: ${script.title}", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error loading: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = script.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        color = LightGold,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${script.fileName} • $dateStr",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        color = ScreenplayTextMuted
                                                    )
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    ScriptFileManager.deleteScript(script.file)
                                                    refreshSavedList()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFEF5350).copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
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
        }
    }
}

@Composable
private fun ExportActionCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBlack),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScreenplayTextDark
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScreenplayTextMuted
                        )
                    )
                }

                Surface(
                    color = ObsidianSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clickable(onClick = onClick)
                        .border(1.dp, ElectrumGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (extraContent != null) {
                Spacer(modifier = Modifier.height(6.dp))
                extraContent()
            }
        }
    }
}
