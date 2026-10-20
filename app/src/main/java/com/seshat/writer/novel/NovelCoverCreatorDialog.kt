package com.seshat.writer.novel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.seshat.writer.ai.AiClient
import com.seshat.writer.ai.AiPreferences
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class CoverPreviewMode {
    FRONT_COVER,
    FULL_DUST_JACKET
}

@Composable
fun NovelCoverCreatorDialog(
    manuscript: NovelManuscript,
    onSaveDesign: (NovelCoverDesign) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiConfig = remember { AiPreferences.load(context) }

    var previewMode by remember { mutableStateOf(CoverPreviewMode.FRONT_COVER) }
    var design by remember {
        mutableStateOf(
            manuscript.coverDesign.copy(
                title = manuscript.coverDesign.title.ifBlank { manuscript.title },
                subtitle = manuscript.coverDesign.subtitle.ifBlank { manuscript.subtitle },
                author = manuscript.coverDesign.author.ifBlank { manuscript.author.ifBlank { "Author Name" } }
            )
        )
    }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingCopy by remember { mutableStateOf(false) }
    var isGeneratingArt by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var showLegalDisclaimer by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val ext = when {
                        mimeType.contains("png") -> "png"
                        mimeType.contains("webp") -> "webp"
                        else -> "jpg"
                    }
                    val destFile = File(coversDir, "imported_cover_${System.currentTimeMillis()}.$ext")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (destFile.exists() && destFile.length() > 0) {
                            design = design.copy(customArtworkPath = destFile.absolutePath)
                            Toast.makeText(context, "Cover image imported successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to read selected image file", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error importing image: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val pageCount = manuscript.estimatedPages()
    val spineWidthInches = design.calculateSpineWidthInches(pageCount)

    // Render Preview whenever design or preview mode changes
    LaunchedEffect(design, previewMode) {
        withContext(Dispatchers.Default) {
            val bmp = when (previewMode) {
                CoverPreviewMode.FRONT_COVER -> {
                    NovelCoverGenerator.renderFrontCoverBitmap(
                        manuscript = manuscript,
                        design = design,
                        width = 800,
                        height = 1280
                    )
                }
                CoverPreviewMode.FULL_DUST_JACKET -> {
                    // Render dust jacket preview
                    val flapW = design.flapWidthInches
                    val trimW = design.trimWidthInches
                    val trimH = design.trimHeightInches
                    val totalWInches = (flapW * 2) + (trimW * 2) + spineWidthInches + 0.25f
                    val totalHInches = trimH + 0.25f

                    val scale = 65f // points/pixels per inch for live preview
                    val wPx = Math.round(totalWInches * scale)
                    val hPx = Math.round(totalHInches * scale)

                    val bmp = Bitmap.createBitmap(wPx, hPx, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bmp)
                    NovelCoverGenerator.renderDustJacketCanvas(
                        canvas = canvas,
                        width = wPx.toFloat(),
                        height = hPx.toFloat(),
                        manuscript = manuscript,
                        design = design,
                        pageCount = pageCount,
                        spineWidthInches = spineWidthInches,
                        flapWidthInches = flapW,
                        trimWidthInches = trimW,
                        bleedInches = 0.125f
                    )
                    bmp
                }
            }
            previewBitmap = bmp
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.96f)
                .clip(RoundedCornerShape(16.dp)),
            color = ObsidianBlack,
            border = BorderStroke(1.5.dp, ElectrumGold.copy(alpha = 0.7f)),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // 1. TOP BAR
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
                                text = "COVER STUDIO",
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
                            text = "DUST JACKET & EBOOK COVER CREATOR",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = ElectrumGold
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Preview Mode Switcher
                        Surface(
                            color = if (previewMode == CoverPreviewMode.FRONT_COVER) ElectrumGold.copy(alpha = 0.25f) else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (previewMode == CoverPreviewMode.FRONT_COVER) ElectrumGold else Color.Transparent),
                            modifier = Modifier.clickable { previewMode = CoverPreviewMode.FRONT_COVER }
                        ) {
                            Text(
                                text = "📱 Front Cover",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (previewMode == CoverPreviewMode.FRONT_COVER) ElectrumGold else ScreenplayTextMuted
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            color = if (previewMode == CoverPreviewMode.FULL_DUST_JACKET) ElectrumGold.copy(alpha = 0.25f) else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (previewMode == CoverPreviewMode.FULL_DUST_JACKET) ElectrumGold else Color.Transparent),
                            modifier = Modifier.clickable { previewMode = CoverPreviewMode.FULL_DUST_JACKET }
                        ) {
                            Text(
                                text = "📖 Dust Jacket Wrap",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (previewMode == CoverPreviewMode.FULL_DUST_JACKET) ElectrumGold else ScreenplayTextMuted
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ScreenplayTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = ObsidianSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // 2. MAIN TWO-COLUMN BODY
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT COLUMN: Live Canvas Preview
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .background(ObsidianSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (previewMode == CoverPreviewMode.FRONT_COVER) "EBOOK FRONT COVER (1:1.6)" else "HARDCOVER DUST JACKET (5-PANEL WRAP)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectrumGold,
                                    letterSpacing = 1.sp
                                )
                            )

                            Text(
                                text = "Spine: ${"%.3f".format(spineWidthInches)}\" (~$pageCount pgs)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ScreenplayTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color(0xFF07090C), RoundedCornerShape(8.dp))
                                .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap!!.asImageBitmap(),
                                    contentDescription = "Cover Preview",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                CircularProgressIndicator(color = ElectrumGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Print Dimensions Summary Banner
                        Surface(
                            color = ObsidianBlack,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📐 Wrap: ${"%.2f".format(design.calculateTotalWrapWidthInches(pageCount))}\" × 9.25\" (KDP / IngramSpark)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.5.sp)
                                )
                                Text(
                                    text = design.themePreset.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // RIGHT COLUMN: Controls, AI Tools, and Field Editors
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .background(ObsidianSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // --- AI MAGIC SECTION ---
                            item {
                                Surface(
                                    color = ObsidianBlack,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "✨ AI & CUSTOM ARTWORK STUDIO",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = ElectrumGold,
                                                    letterSpacing = 1.sp
                                                )
                                            )
                                            Text(
                                                text = aiConfig.provider.displayName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = ScreenplayTextMuted,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // AI Copy Generator Button
                                            Button(
                                                onClick = {
                                                    isGeneratingCopy = true
                                                    scope.launch {
                                                        val sample = manuscript.chapters.firstOrNull()?.paragraphs
                                                            ?.filter { it.type == NovelParagraphType.NARRATIVE }
                                                            ?.joinToString(" ") { it.text } ?: ""
                                                        val res = AiClient.generateCoverCopy(
                                                            config = aiConfig,
                                                            title = design.title.ifBlank { manuscript.title },
                                                            subtitle = design.subtitle.ifBlank { manuscript.subtitle },
                                                            author = design.author.ifBlank { manuscript.author },
                                                            genre = manuscript.genre,
                                                            synopsis = manuscript.chapters.firstOrNull()?.synopsis ?: "",
                                                            storySample = sample
                                                        )
                                                        isGeneratingCopy = false
                                                        res.onSuccess { copyRes ->
                                                            design = design.copy(
                                                                tagline = copyRes.tagline,
                                                                backCoverBlurb = copyRes.backCoverBlurb,
                                                                praiseQuote = copyRes.praiseQuote,
                                                                frontFlapTeaser = copyRes.frontFlapTeaser,
                                                                backFlapAuthorBio = copyRes.backFlapAuthorBio,
                                                                aiArtPrompt = copyRes.artPrompt
                                                            )
                                                            Toast.makeText(context, "Generated full dust jacket copy with Gemini!", Toast.LENGTH_SHORT).show()
                                                        }.onFailure { err ->
                                                            Toast.makeText(context, "AI Copy Error: ${err.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color(0xFF0F1216)),
                                                shape = RoundedCornerShape(6.dp),
                                                enabled = !isGeneratingCopy,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (isGeneratingCopy) {
                                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF0F1216), strokeWidth = 2.dp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Generating...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                } else {
                                                    Text("✨ AI Blurb & Flaps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            // AI Art Generator Button
                                            Button(
                                                onClick = {
                                                    if (design.aiArtPrompt.isBlank()) {
                                                        Toast.makeText(context, "Generate copy first or enter an Art Prompt below.", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    isGeneratingArt = true
                                                    scope.launch {
                                                        val artFile = File(context.filesDir, "cover_art_${System.currentTimeMillis()}.jpg")
                                                        val res = AiClient.generateCoverArtWithAi(
                                                            config = aiConfig,
                                                            prompt = design.aiArtPrompt,
                                                            outputFile = artFile
                                                        )
                                                        isGeneratingArt = false
                                                        res.onSuccess { file ->
                                                            design = design.copy(customArtworkPath = file.absolutePath)
                                                            Toast.makeText(context, "Generated AI Cover Art!", Toast.LENGTH_SHORT).show()
                                                        }.onFailure { err ->
                                                            Toast.makeText(context, "AI Art Error: ${err.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                                                shape = RoundedCornerShape(6.dp),
                                                enabled = !isGeneratingArt,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (isGeneratingArt) {
                                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Rendering...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                } else {
                                                    Text("🎨 Generate AI Art", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Custom Artwork Import & Management Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { showLegalDisclaimer = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF1E293B),
                                                    contentColor = ElectrumGold
                                                ),
                                                border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.6f)),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("🖼️ Import Custom Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (!design.customArtworkPath.isNullOrBlank()) {
                                                OutlinedButton(
                                                    onClick = {
                                                        design = design.copy(customArtworkPath = null)
                                                        Toast.makeText(context, "Custom artwork removed", Toast.LENGTH_SHORT).show()
                                                    },
                                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = Color(0xFFEF4444),
                                                        containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                                                    ),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove Artwork",
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Clear Art", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        if (!design.customArtworkPath.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                color = ObsidianSurface,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, ElectrumGold.copy(alpha = 0.35f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "✓ Artwork Loaded: ${File(design.customArtworkPath!!).name}",
                                                        style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold, fontSize = 10.5.sp),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // --- THEME PRESETS ---
                            item {
                                Text(
                                    text = "AESTHETIC THEME PRESET",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectrumGold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CoverThemePreset.values().forEach { preset ->
                                        val isSel = design.themePreset == preset
                                        Surface(
                                            color = Color(android.graphics.Color.parseColor(preset.bgHex)),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.5.dp, if (isSel) ElectrumGold else ObsidianSurfaceVariant),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { design = design.copy(themePreset = preset) }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(android.graphics.Color.parseColor(preset.accentHex)))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // --- PAPER STOCK (SPINE WIDTH) ---
                            item {
                                Text(
                                    text = "PAPER STOCK (SPINE WIDTH CALCULATION)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectrumGold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PaperType.values().forEach { paper ->
                                        val isSel = design.paperType == paper
                                        Surface(
                                            color = if (isSel) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, if (isSel) ElectrumGold else ObsidianSurfaceVariant),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { design = design.copy(paperType = paper) }
                                        ) {
                                            Text(
                                                text = when (paper) {
                                                    PaperType.WHITE_50LB -> "⚪ 50lb White"
                                                    PaperType.CREAM_55LB -> "📜 55lb Cream"
                                                    PaperType.COLOR_70LB -> "🎨 70lb Color"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSel) ElectrumGold else ScreenplayTextMuted,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // --- COVER FIELDS EDITORS ---
                            item {
                                CoverTextField(
                                    label = "BOOK TITLE",
                                    value = design.title,
                                    onValueChange = { design = design.copy(title = it) }
                                )
                            }

                            item {
                                CoverTextField(
                                    label = "SUBTITLE / SERIES TITLE",
                                    value = design.subtitle,
                                    onValueChange = { design = design.copy(subtitle = it) }
                                )
                            }

                            item {
                                CoverTextField(
                                    label = "AUTHOR BYLINE",
                                    value = design.author,
                                    onValueChange = { design = design.copy(author = it) }
                                )
                            }

                            item {
                                CoverTextField(
                                    label = "MARKETING TAGLINE",
                                    value = design.tagline,
                                    onValueChange = { design = design.copy(tagline = it) }
                                )
                            }

                            item {
                                CoverTextField(
                                    label = "BACK COVER BLURB",
                                    value = design.backCoverBlurb,
                                    singleLine = false,
                                    onValueChange = { design = design.copy(backCoverBlurb = it) }
                                )
                            }

                            item {
                                CoverTextField(
                                    label = "EDITORIAL PRAISE QUOTE",
                                    value = design.praiseQuote,
                                    onValueChange = { design = design.copy(praiseQuote = it) }
                                )
                            }

                            item {
                                CoverTextField(
                                    label = "INSIDE FRONT FLAP TEASER",
                                    value = design.frontFlapTeaser,
                                    singleLine = false,
                                    onValueChange = { design = design.copy(frontFlapTeaser = it) }
                                )
                            }

                            item {
                                CoverTextField(
                                    label = "BACK FLAP AUTHOR BIO",
                                    value = design.backFlapAuthorBio,
                                    singleLine = false,
                                    onValueChange = { design = design.copy(backFlapAuthorBio = it) }
                                )
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CoverTextField(
                                            label = "ISBN / BARCODE",
                                            value = design.isbn,
                                            onValueChange = { design = design.copy(isbn = it) }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        CoverTextField(
                                            label = "PRICE",
                                            value = design.priceText,
                                            onValueChange = { design = design.copy(priceText = it) }
                                        )
                                    }
                                }
                            }

                            item {
                                CoverTextField(
                                    label = "AI ART PROMPT (FOR GEMINI / IMAGEN)",
                                    value = design.aiArtPrompt,
                                    singleLine = false,
                                    onValueChange = { design = design.copy(aiArtPrompt = it) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ObsidianSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // 3. BOTTOM ACTIONS (Save & Exports)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, ScreenplayTextMuted.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenplayTextMuted)
                    ) {
                        Text("Cancel")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Export Front Cover PNG Button
                        OutlinedButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val exportDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "covers")
                                    exportDir.mkdirs()
                                    val safe = manuscript.title.replace(Regex("[^a-zA-Z0-9]"), "_")
                                    val file = File(exportDir, "${safe}_front_cover.png")
                                    val bmp = NovelCoverGenerator.renderFrontCoverBitmap(manuscript, design, 1600, 2560)
                                    FileOutputStream(file).use { out ->
                                        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                                    }
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Exported Cover to ${file.name}", Toast.LENGTH_SHORT).show()
                                        shareFile(context, file, "image/png", "Share Front Cover Image")
                                    }
                                }
                            },
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectrumGold)
                        ) {
                            Text("📤 Export Cover (PNG)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Export Dust Jacket PDF Button
                        OutlinedButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val exportDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "covers")
                                    exportDir.mkdirs()
                                    val safe = manuscript.title.replace(Regex("[^a-zA-Z0-9]"), "_")
                                    val file = File(exportDir, "${safe}_dust_jacket_wrap.pdf")
                                    val success = NovelCoverGenerator.exportDustJacketPdf(context, manuscript, design, file)
                                    withContext(Dispatchers.Main) {
                                        if (success) {
                                            Toast.makeText(context, "Exported Dust Jacket to ${file.name}", Toast.LENGTH_SHORT).show()
                                            shareFile(context, file, "application/pdf", "Share Dust Jacket Wrap PDF")
                                        } else {
                                            Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectrumGold)
                        ) {
                            Text("📑 Export Dust Jacket (PDF)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Save & Apply Button
                        Button(
                            onClick = {
                                onSaveDesign(design)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color(0xFF0F1216)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVE & APPLY TO NOVEL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLegalDisclaimer) {
        AlertDialog(
            onDismissRequest = { showLegalDisclaimer = false },
            containerColor = ObsidianSurface,
            titleContentColor = ElectrumGold,
            textContentColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚠️ Copyright & Usage Notice",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectrumGold
                        )
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Before selecting and importing an image for your book cover or dust jacket, please review the required agreement:",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 12.sp)
                    )
                    Surface(
                        color = ObsidianBlack,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "1. Permission & Ownership:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectrumGold
                                )
                            )
                            Text(
                                text = "You confirm and warrant that you own or have obtained all necessary licenses, permissions, and rights to use any imported image in book covers, dust jackets, and commercial or personal publications.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.5.sp)
                            )
                            HorizontalDivider(color = ObsidianSurfaceVariant)
                            Text(
                                text = "2. ScribeStar Media LLC Non-Liability:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectrumGold
                                )
                            )
                            Text(
                                text = "You expressly agree that ScribeStar Media LLC is NOT responsible or liable for any copyright infringement, trademark violation, intellectual property dispute, or damages arising from any images or content you import, generate, or publish.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.5.sp)
                            )
                        }
                    }
                    Text(
                        text = "By clicking 'I Agree & Import', you confirm your consent to these terms and will proceed to choose an image from your device.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLegalDisclaimer = false
                        imagePickerLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectrumGold,
                        contentColor = Color(0xFF0F1216)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("I Agree & Import", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLegalDisclaimer = false },
                    border = BorderStroke(1.dp, ScreenplayTextMuted.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenplayTextMuted),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun CoverTextField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = ScreenplayTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = TextStyle(color = Color.White, fontSize = 12.5.sp),
            cursorBrush = SolidColor(ElectrumGold),
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianBlack, RoundedCornerShape(6.dp))
                .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = if (singleLine) 7.dp else 10.dp)
        )
    }
}

private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    } catch (_: Exception) {}
}
