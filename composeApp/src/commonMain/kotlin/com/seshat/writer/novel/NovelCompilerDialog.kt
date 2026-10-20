package com.seshat.writer.novel

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextMuted

@Composable
fun NovelCompilerDialog(
    manuscript: NovelManuscript,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(CompilerFormat.EPUB) }
    var selectedChapterIds by remember {
        mutableStateOf(manuscript.chapters.filterNot { it.isOmitted }.map { it.id }.toSet())
    }
    // Front Matter State
    var includeTitlePage by remember { mutableStateOf(manuscript.frontMatter.includeTitlePage) }
    var includeToc by remember { mutableStateOf(manuscript.frontMatter.includeTableOfContents) }
    var includeCopyrightPage by remember { mutableStateOf(manuscript.frontMatter.includeCopyrightPage) }
    var copyrightYear by remember { mutableStateOf(manuscript.frontMatter.copyrightYear) }
    var copyrightHolder by remember { mutableStateOf(manuscript.frontMatter.copyrightHolder.ifBlank { manuscript.author }) }
    var publisherName by remember { mutableStateOf(manuscript.frontMatter.publisherName) }
    var fictionDisclaimer by remember { mutableStateOf(manuscript.frontMatter.fictionDisclaimer) }
    var allRightsReserved by remember { mutableStateOf(manuscript.frontMatter.allRightsReserved) }
    var editionNotice by remember { mutableStateOf(manuscript.frontMatter.editionNotice) }
    var includeDedication by remember { mutableStateOf(manuscript.frontMatter.includeDedication) }
    var dedicationText by remember { mutableStateOf(manuscript.frontMatter.dedicationText) }
    var includeEpigraph by remember { mutableStateOf(manuscript.frontMatter.includeEpigraph) }
    var epigraphText by remember { mutableStateOf(manuscript.frontMatter.epigraphText) }
    var epigraphSource by remember { mutableStateOf(manuscript.frontMatter.epigraphSource) }

    // Back Matter State
    var includeAuthorBio by remember { mutableStateOf(manuscript.backMatter.includeAuthorBio) }
    var authorBioText by remember { mutableStateOf(manuscript.backMatter.authorBioText) }
    var includeAlsoByAuthor by remember { mutableStateOf(manuscript.backMatter.includeAlsoByAuthor) }
    var alsoByAuthorTitles by remember { mutableStateOf(manuscript.backMatter.alsoByAuthorTitles) }
    var includeNewsletterCta by remember { mutableStateOf(manuscript.backMatter.includeNewsletterCta) }
    var newsletterCtaText by remember { mutableStateOf(manuscript.backMatter.newsletterCtaText) }
    var newsletterLink by remember { mutableStateOf(manuscript.backMatter.newsletterLink) }
    var includeAcknowledgments by remember { mutableStateOf(manuscript.backMatter.includeAcknowledgments) }
    var acknowledgmentsText by remember { mutableStateOf(manuscript.backMatter.acknowledgmentsText) }

    // Scene Break Style
    var selectedSceneBreakStyle by remember { mutableStateOf(manuscript.sceneBreakStyle) }

    // Settings Sub-tab: 0 = Front Matter, 1 = Back Matter, 2 = Fleurons
    var activeSettingsTab by remember { mutableStateOf(0) }
    var isCoverCreatorOpen by remember { mutableStateOf(false) }

    val includedChapters = manuscript.chapters.filter { selectedChapterIds.contains(it.id) }
    val totalWords = includedChapters.sumOf { it.wordCount() }
    val estPages = (totalWords / 250).coerceAtLeast(1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            color = ObsidianBlack,
            border = BorderStroke(1.5.dp, ElectrumGold.copy(alpha = 0.6f)),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ElectrumGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "SCRIVENER COMPILER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectrumGold,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "COMPILE MANUSCRIPT",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = ElectrumGold
                            )
                        )
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
                HorizontalDivider(color = ObsidianSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Format Selection Row
                Text(
                    text = "TARGET FORMAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ScreenplayTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompilerFormat.values().forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        Surface(
                            color = if (isSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ElectrumGold else ObsidianSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFormat = fmt }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (fmt) {
                                        CompilerFormat.EPUB -> "📱 EPUB eBook"
                                        CompilerFormat.PDF_MANUSCRIPT -> "📑 PDF Manuscript"
                                        CompilerFormat.SHUNN_MANUSCRIPT -> "📄 Shunn Format"
                                        CompilerFormat.MARKDOWN -> "📝 Markdown"
                                        CompilerFormat.HTML -> "🌐 Web HTML"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ElectrumGold else ScreenplayTextMuted
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Two-column layout: Left Options, Right Chapter Checklist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT COLUMN: Front matter, Back matter & Fleurons
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .background(ObsidianSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        // Sub-tabs: Front Matter | Back Matter | Fleurons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Front Matter", "Back Matter", "Fleurons").forEachIndexed { tabIdx, tabTitle ->
                                val isTabSelected = activeSettingsTab == tabIdx
                                Surface(
                                    color = if (isTabSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isTabSelected) ElectrumGold else ObsidianSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeSettingsTab = tabIdx }
                                ) {
                                    Text(
                                        text = tabTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isTabSelected) ElectrumGold else ScreenplayTextMuted,
                                            fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable Settings Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            when (activeSettingsTab) {
                                0 -> {
                                    // FRONT MATTER CONTROLS
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeTitlePage,
                                            onCheckedChange = { includeTitlePage = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Include Title Page", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeToc,
                                            onCheckedChange = { includeToc = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Table of Contents (TOC)", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeCopyrightPage,
                                            onCheckedChange = { includeCopyrightPage = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Copyright & Legal Page", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    if (includeCopyrightPage) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(ObsidianBlack, RoundedCornerShape(8.dp))
                                                .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text("Year & Imprint:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                BasicTextField(
                                                    value = copyrightYear,
                                                    onValueChange = { copyrightYear = it },
                                                    textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                                        .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                        .padding(6.dp)
                                                )
                                                BasicTextField(
                                                    value = publisherName,
                                                    onValueChange = { publisherName = it },
                                                    textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                                    modifier = Modifier
                                                        .weight(2f)
                                                        .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                                        .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                        .padding(6.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Copyright Holder:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                                            BasicTextField(
                                                value = copyrightHolder,
                                                onValueChange = { copyrightHolder = it },
                                                textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                                    .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .padding(6.dp)
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = fictionDisclaimer,
                                                    onCheckedChange = { fictionDisclaimer = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = ElectrumGold),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Include Standard Fiction Disclaimer", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // Dedication
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeDedication,
                                            onCheckedChange = { includeDedication = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Dedication", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    if (includeDedication) {
                                        BasicTextField(
                                            value = dedicationText,
                                            onValueChange = { dedicationText = it },
                                            textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                            cursorBrush = SolidColor(ElectrumGold),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(ObsidianBlack, RoundedCornerShape(6.dp))
                                                .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // Epigraph
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeEpigraph,
                                            onCheckedChange = { includeEpigraph = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Epigraph (Opening Quote)", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    if (includeEpigraph) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(ObsidianBlack, RoundedCornerShape(8.dp))
                                                .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text("Quote:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                                            BasicTextField(
                                                value = epigraphText,
                                                onValueChange = { epigraphText = it },
                                                textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                                    .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .padding(6.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Attribution / Source:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                                            BasicTextField(
                                                value = epigraphSource,
                                                onValueChange = { epigraphSource = it },
                                                textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                                    .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .padding(6.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                1 -> {
                                    // BACK MATTER CONTROLS
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeAuthorBio,
                                            onCheckedChange = { includeAuthorBio = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "About the Author", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    if (includeAuthorBio) {
                                        BasicTextField(
                                            value = authorBioText,
                                            onValueChange = { authorBioText = it },
                                            textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                            cursorBrush = SolidColor(ElectrumGold),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(ObsidianBlack, RoundedCornerShape(6.dp))
                                                .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeAlsoByAuthor,
                                            onCheckedChange = { includeAlsoByAuthor = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Also by This Author", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    if (includeAlsoByAuthor) {
                                        BasicTextField(
                                            value = alsoByAuthorTitles,
                                            onValueChange = { alsoByAuthorTitles = it },
                                            textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                            cursorBrush = SolidColor(ElectrumGold),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(ObsidianBlack, RoundedCornerShape(6.dp))
                                                .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeNewsletterCta,
                                            onCheckedChange = { includeNewsletterCta = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "VIP Newsletter / Reader CTA", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    if (includeNewsletterCta) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(ObsidianBlack, RoundedCornerShape(8.dp))
                                                .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text("Call to Action Pitch:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                                            BasicTextField(
                                                value = newsletterCtaText,
                                                onValueChange = { newsletterCtaText = it },
                                                textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                                    .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .padding(6.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Signup Link (URL):", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted))
                                            BasicTextField(
                                                value = newsletterLink,
                                                onValueChange = { newsletterLink = it },
                                                textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(ObsidianSurface, RoundedCornerShape(4.dp))
                                                    .border(1.dp, ElectrumGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                    .padding(6.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = includeAcknowledgments,
                                            onCheckedChange = { includeAcknowledgments = it },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Acknowledgments", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White))
                                    }

                                    if (includeAcknowledgments) {
                                        BasicTextField(
                                            value = acknowledgmentsText,
                                            onValueChange = { acknowledgmentsText = it },
                                            textStyle = TextStyle(color = ElectrumGold, fontSize = 12.sp),
                                            cursorBrush = SolidColor(ElectrumGold),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(ObsidianBlack, RoundedCornerShape(6.dp))
                                                .border(1.dp, ElectrumGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                .padding(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                2 -> {
                                    // FLEURONS / SCENE BREAK ORNAMENTS
                                    Text(
                                        text = "CHOOSE SCENE BREAK FLEURON",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ScreenplayTextMuted,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    SceneBreakStyle.values().forEach { style ->
                                        val isSelected = selectedSceneBreakStyle == style
                                        Surface(
                                            color = if (isSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) ElectrumGold else ObsidianSurfaceVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clickable { selectedSceneBreakStyle = style }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = style.displayName,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            color = if (isSelected) ElectrumGold else Color.White,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        )
                                                    )
                                                }
                                                Text(
                                                    text = if (style == SceneBreakStyle.BLANK_SPACE) "[ 1 Line ]" else style.symbol,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = ElectrumGold,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Open Cover Studio Button
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCoverCreatorOpen = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎨", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Cover & Dust Jacket Studio",
                                        style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Design eBook cover or hardcover jacket",
                                        style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 9.5.sp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Summary Box
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "MANUSCRIPT METRICS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectrumGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${includedChapters.size} Chapters • ~$totalWords Words",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "~$estPages Book Pages • ~${(totalWords / 225)} min read",
                                    style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted)
                                )
                            }
                        }
                    }

                    // RIGHT COLUMN: Scrivener Chapter Checklist
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .background(ObsidianSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BINDER CHAPTERS (${selectedChapterIds.size}/${manuscript.chapters.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectrumGold,
                                    letterSpacing = 1.sp
                                )
                            )

                            Text(
                                text = "Select All",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectrumGold,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.clickable {
                                    selectedChapterIds = manuscript.chapters.map { it.id }.toSet()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(manuscript.chapters) { ch ->
                                val isChecked = selectedChapterIds.contains(ch.id)
                                Surface(
                                    color = ObsidianBlack,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChecked) ElectrumGold.copy(alpha = 0.5f) else ObsidianSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedChapterIds = if (isChecked) {
                                                selectedChapterIds - ch.id
                                            } else {
                                                selectedChapterIds + ch.id
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { check ->
                                                selectedChapterIds = if (check) {
                                                    selectedChapterIds + ch.id
                                                } else {
                                                    selectedChapterIds - ch.id
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectrumGold),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ch.title,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Text(
                                                text = "${ch.wordCount()} words • ${ch.status.label}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = ScreenplayTextMuted,
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

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, ScreenplayTextMuted.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenplayTextMuted)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val options = CompilerOptions(
                                format = selectedFormat,
                                includeTitlePage = includeTitlePage,
                                includeTableOfContents = includeToc,
                                includeCopyrightPage = includeCopyrightPage,
                                includeDedication = includeDedication,
                                dedicationText = dedicationText,
                                includeEpigraph = includeEpigraph,
                                epigraphText = epigraphText,
                                epigraphSource = epigraphSource,
                                includeAuthorBio = includeAuthorBio,
                                authorBioText = authorBioText,
                                includeAlsoByAuthor = includeAlsoByAuthor,
                                alsoByAuthorTitles = alsoByAuthorTitles,
                                includeNewsletterCta = includeNewsletterCta,
                                newsletterCtaText = newsletterCtaText,
                                newsletterLink = newsletterLink,
                                includeAcknowledgments = includeAcknowledgments,
                                acknowledgmentsText = acknowledgmentsText,
                                sceneBreakStyle = selectedSceneBreakStyle,
                                sceneBreakSymbol = selectedSceneBreakStyle.symbol,
                                selectedChapterIds = selectedChapterIds
                            )
                            val updatedManuscript = manuscript.copy(
                                frontMatter = NovelFrontMatter(
                                    includeTitlePage = includeTitlePage,
                                    includeCopyrightPage = includeCopyrightPage,
                                    copyrightYear = copyrightYear,
                                    copyrightHolder = copyrightHolder,
                                    publisherName = publisherName,
                                    fictionDisclaimer = fictionDisclaimer,
                                    allRightsReserved = allRightsReserved,
                                    editionNotice = editionNotice,
                                    includeDedication = includeDedication,
                                    dedicationText = dedicationText,
                                    includeEpigraph = includeEpigraph,
                                    epigraphText = epigraphText,
                                    epigraphSource = epigraphSource,
                                    includeTableOfContents = includeToc
                                ),
                                backMatter = NovelBackMatter(
                                    includeAuthorBio = includeAuthorBio,
                                    authorBioText = authorBioText,
                                    includeAlsoByAuthor = includeAlsoByAuthor,
                                    alsoByAuthorTitles = alsoByAuthorTitles,
                                    includeNewsletterCta = includeNewsletterCta,
                                    newsletterCtaText = newsletterCtaText,
                                    newsletterLink = newsletterLink,
                                    includeAcknowledgments = includeAcknowledgments,
                                    acknowledgmentsText = acknowledgmentsText
                                ),
                                sceneBreakStyle = selectedSceneBreakStyle
                            )
                            NovelRepository.saveManuscript(context, updatedManuscript)
                            val file = NovelRepository.compileAndExport(context, updatedManuscript, options)
                            if (file != null && file.exists()) {
                                Toast.makeText(context, "Compiled ${options.format.name} to ${file.name}", Toast.LENGTH_LONG).show()

                                // Open share sheet
                                try {
                                    if (java.awt.Desktop.isDesktopSupported()) {
                                        java.awt.Desktop.getDesktop().open(file)
                                    }
                                } catch (_: Exception) {}

                                onDismiss()
                            } else {
                                Toast.makeText(context, "Compilation failed.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color(0xFF0F1216)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COMPILE & EXPORT (${selectedFormat.extension.uppercase()})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (isCoverCreatorOpen) {
        NovelCoverCreatorDialog(
            manuscript = manuscript,
            onSaveDesign = { updatedDesign ->
                val updated = manuscript.copy(coverDesign = updatedDesign)
                NovelRepository.saveManuscript(context, updated)
            },
            onDismiss = { isCoverCreatorOpen = false }
        )
    }
}
