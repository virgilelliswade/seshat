package com.seshat.writer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.bible.CharacterDossier
import com.seshat.writer.bible.StarPolarity
import com.seshat.writer.bible.StellariumCatalog
import com.seshat.writer.bible.StellariumDomain
import com.seshat.writer.bible.StellariumStarSelection
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.StudioMonoFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Compact inline Character Stellarium preview + launch button inside each Character Dossier card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterStellariumSummaryCard(
    character: CharacterDossier,
    onUpdate: (CharacterDossier) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var showStellariumDialog by remember { mutableStateOf(false) }
    val stars = character.stellariumStars

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) ObsidianBlack else Color(0xFFF1F5F9), RoundedCornerShape(0.dp))
            .border(BorderStroke(1.dp, ElectrumGold.copy(alpha = 0.45f)), RoundedCornerShape(0.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "✨", fontSize = 14.sp)
                Column {
                    Text(
                        text = "CHARACTER STELLARIUM (${stars.size} STARS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = StudioMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) ElectrumGold else Color(0xFF991B1B),
                            fontSize = 10.5.sp,
                            letterSpacing = 0.6.sp
                        )
                    )
                    Text(
                        text = if (stars.isEmpty()) "Combine 2–6 contrasting archetype stars into a constellation"
                        else StellariumCatalog.buildConstellationSummary(stars),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDark) ScreenplayTextMuted else Color(0xFF475569),
                            fontSize = 10.5.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = if (isDark) ObsidianSurface else Color.White,
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(0.dp))
                        .clickable {
                            val drawn = StellariumCatalog.drawContrastingStars(count = 3)
                            onUpdate(character.copy(stellariumStars = drawn))
                        }
                        .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                ) {
                    Text(
                        text = "🎲 Draw 3",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isDark) ElectrumGold else Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = ScribeStarNeonRed.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(0.dp))
                        .clickable { showStellariumDialog = true }
                        .border(1.dp, ScribeStarNeonRed, RoundedCornerShape(0.dp))
                ) {
                    Text(
                        text = "🔭 Stellarium",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isDark) Color.White else Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (stars.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StellariumSkyCanvas(
                    stars = stars,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(0.dp))
                        .clickable { showStellariumDialog = true }
                )

                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    stars.forEach { star ->
                        val domainColor = Color(star.domain.hexColor)
                        Surface(
                            color = domainColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(1.dp, domainColor.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "${star.domain.icon} ${star.name} ${star.polarity.badge}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStellariumDialog) {
        CharacterStellariumDialog(
            character = character,
            onUpdate = onUpdate,
            onDismiss = { showStellariumDialog = false }
        )
    }
}

/**
 * Interactive observatory sky canvas plotting the 8 Archetype Domains and connecting
 * the character's selected stars into a glowing constellation.
 */
@Composable
fun StellariumSkyCanvas(
    stars: List<StellariumStarSelection>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF10192D), Color(0xFF050811))
                )
            )
            .border(1.dp, ElectrumGold.copy(alpha = 0.45f), RoundedCornerShape(0.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = minOf(cx, cy) * 0.90f

            // Concentric celestial rings
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = maxR,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = ElectrumGold.copy(alpha = 0.18f),
                radius = maxR * 0.62f,
                center = Offset(cx, cy),
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = maxR * 0.30f,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )

            // 8 Domain spokes + perimeter anchor dots
            val domains = StellariumDomain.entries
            domains.forEachIndexed { idx, domain ->
                val angleRad = (-90.0 + idx * (360.0 / domains.size)) * PI / 180.0
                val px = cx + (maxR * cos(angleRad)).toFloat()
                val py = cy + (maxR * sin(angleRad)).toFloat()
                drawLine(
                    color = Color(domain.hexColor).copy(alpha = 0.16f),
                    start = Offset(cx, cy),
                    end = Offset(px, py),
                    strokeWidth = 1f
                )
                drawCircle(
                    color = Color(domain.hexColor).copy(alpha = 0.70f),
                    radius = 2.6f,
                    center = Offset(px, py)
                )
            }

            // Plot selected character stars at domain angle + polarity radius
            val domainIndexCounts = mutableMapOf<StellariumDomain, Int>()
            val points = stars.map { sel ->
                val dIdx = sel.domain.ordinal
                val countInDomain = domainIndexCounts.getOrDefault(sel.domain, 0)
                domainIndexCounts[sel.domain] = countInDomain + 1

                val baseAngleDeg = -90.0 + dIdx * (360.0 / domains.size) + (countInDomain * 11.0 - (if (countInDomain > 0) 5.5 else 0.0))
                val angleRad = baseAngleDeg * PI / 180.0
                val radiusFraction = when (sel.polarity) {
                    StarPolarity.POSITIVE -> 0.74f
                    StarPolarity.DUAL -> 0.56f
                    StarPolarity.SHADOW -> 0.38f
                } - (countInDomain * 0.06f)

                val r = maxR * radiusFraction.coerceIn(0.24f, 0.85f)
                val x = cx + (r * cos(angleRad)).toFloat()
                val y = cy + (r * sin(angleRad)).toFloat()
                Offset(x, y) to Color(sel.domain.hexColor)
            }

            // Constellation lines
            if (points.size >= 2) {
                for (i in 0 until points.lastIndex) {
                    drawLine(
                        color = ElectrumGold.copy(alpha = 0.75f),
                        start = points[i].first,
                        end = points[i + 1].first,
                        strokeWidth = 2.2f
                    )
                }
                if (points.size >= 3) {
                    drawLine(
                        color = ElectrumGold.copy(alpha = 0.35f),
                        start = points.last().first,
                        end = points.first().first,
                        strokeWidth = 1.4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                    )
                }
            }

            // Glowing star nodes
            points.forEach { (pt, color) ->
                drawCircle(color = color.copy(alpha = 0.28f), radius = 10f, center = pt)
                drawCircle(color = color, radius = 5.5f, center = pt)
                drawCircle(color = Color.White, radius = 2.2f, center = pt)
            }
        }
    }
}

/**
 * Full-featured Character Stellarium Observatory Dialog.
 * Writers combine 2–6 Archetype Stars (Light, Dual, or Shadow) across 8 domains.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterStellariumDialog(
    character: CharacterDossier,
    onUpdate: (CharacterDossier) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDomainFilter by remember { mutableStateOf<StellariumDomain?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var customStarName by remember { mutableStateOf("") }
    var customStarDomain by remember { mutableStateOf(StellariumDomain.WISDOM_TRUTH) }

    val stars = character.stellariumStars
    val filteredCatalog = remember(selectedDomainFilter, searchQuery) {
        StellariumCatalog.stars.filter { star ->
            (selectedDomainFilter == null || star.domain == selectedDomainFilter) &&
                (searchQuery.isBlank() || star.name.contains(searchQuery, ignoreCase = true) ||
                    star.positiveTrait.contains(searchQuery, ignoreCase = true) ||
                    star.shadowTrait.contains(searchQuery, ignoreCase = true))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 980.dp)
                .heightIn(max = 760.dp)
                .clip(RoundedCornerShape(0.dp))
                .border(BorderStroke(1.2.dp, ElectrumGold.copy(alpha = 0.7f)), RoundedCornerShape(0.dp)),
            color = ObsidianSurface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianBlack)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "✨ CHARACTER STELLARIUM • ${character.name.ifBlank { "UNNAMED CHARACTER" }}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MainAppFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = ElectrumGold,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = "Combine 2–6 Archetype Stars (Light, Dual, or Shadow) into a unique character constellation",
                            style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ObsidianSurface,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable {
                                    val drawn = StellariumCatalog.drawContrastingStars(count = 3)
                                    onUpdate(character.copy(stellariumStars = drawn))
                                }
                                .border(1.dp, ElectrumGold, RoundedCornerShape(0.dp))
                        ) {
                            Text(
                                text = "🎲 Shuffle 3 Stars",
                                style = MaterialTheme.typography.labelSmall.copy(color = ElectrumGold, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Stellarium", tint = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = ObsidianSurfaceVariant, thickness = 1.dp)

                // Main Content: Constellation Ring + Pinned Stars (Top) + Deck Browser (Bottom)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Constellation Observatory Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StellariumSkyCanvas(
                                stars = stars,
                                modifier = Modifier.size(190.dp)
                            )
                            Text(
                                text = "${stars.size} / 6 Recommended Stars",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = StudioMonoFontFamily,
                                    color = if (stars.size in 2..6) Color(0xFF10B981) else ElectrumGold,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PINNED CONSTELLATION STARS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = StudioMonoFontFamily,
                                    color = ElectrumGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            if (stars.isEmpty()) {
                                Surface(
                                    color = ObsidianBlack,
                                    shape = RoundedCornerShape(0.dp),
                                    border = BorderStroke(1.dp, ObsidianSurfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No stars pinned yet. Pick 2–6 archetype stars from the deck below or click '🎲 Shuffle 3 Stars' for instant chemistry.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                stars.forEachIndexed { idx, sel ->
                                    val catalogStar = StellariumCatalog.findById(sel.archetypeId)
                                    val domainColor = Color(sel.domain.hexColor)
                                    Surface(
                                        color = ObsidianBlack,
                                        shape = RoundedCornerShape(0.dp),
                                        border = BorderStroke(1.dp, domainColor.copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${sel.domain.icon} ${sel.name.uppercase()} • ${sel.domain.label}",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = domainColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    StarPolarity.entries.forEach { pol ->
                                                        val isActive = sel.polarity == pol
                                                        Surface(
                                                            color = if (isActive) domainColor.copy(alpha = 0.25f) else ObsidianSurface,
                                                            shape = RoundedCornerShape(0.dp),
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(0.dp))
                                                                .clickable {
                                                                    val updated = stars.toMutableList()
                                                                    updated[idx] = sel.copy(polarity = pol)
                                                                    onUpdate(character.copy(stellariumStars = updated))
                                                                }
                                                                .border(
                                                                    1.dp,
                                                                    if (isActive) domainColor else ObsidianSurfaceVariant,
                                                                    RoundedCornerShape(0.dp)
                                                                )
                                                        ) {
                                                            Text(
                                                                text = "${pol.badge} ${pol.label}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    color = if (isActive) Color.White else ScreenplayTextMuted,
                                                                    fontSize = 9.5.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                ),
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            onUpdate(character.copy(stellariumStars = stars.filterNot { it.id == sel.id }))
                                                        },
                                                        modifier = Modifier.size(22.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Remove Star", tint = ScribeStarNeonRed, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                            if (catalogStar != null) {
                                                if (sel.polarity == StarPolarity.POSITIVE || sel.polarity == StarPolarity.DUAL) {
                                                    Text(
                                                        text = "✦ Light: ${catalogStar.positiveTrait}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.5.sp)
                                                    )
                                                }
                                                if (sel.polarity == StarPolarity.SHADOW || sel.polarity == StarPolarity.DUAL) {
                                                    Text(
                                                        text = "🌑 Shadow: ${catalogStar.shadowTrait}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFCA5A5), fontSize = 10.5.sp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = character.stellariumNotes,
                                onValueChange = { onUpdate(character.copy(stellariumNotes = it)) },
                                label = { Text("Constellation Conflict & Chemistry Notes", color = ElectrumGold, fontSize = 10.sp) },
                                placeholder = { Text("How do these stars clash or create irony in this character?", color = ScreenplayTextMuted, fontSize = 11.sp) },
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }
                    }

                    HorizontalDivider(color = ObsidianSurfaceVariant, thickness = 1.dp)

                    // Archetype Deck Browser
                    Text(
                        text = "ARCHETYPE STAR DECK (CLICK ANY STAR TO PIN / UNPIN)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = StudioMonoFontFamily,
                            color = ElectrumGold,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Domain Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val allSelected = selectedDomainFilter == null
                        Surface(
                            color = if (allSelected) ElectrumGold.copy(alpha = 0.2f) else ObsidianBlack,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable { selectedDomainFilter = null }
                                .border(1.dp, if (allSelected) ElectrumGold else ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                        ) {
                            Text(
                                text = "All Domains (${StellariumCatalog.stars.size})",
                                style = MaterialTheme.typography.labelSmall.copy(color = if (allSelected) ElectrumGold else ScreenplayTextMuted, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                        StellariumDomain.entries.forEach { domain ->
                            val isSelected = selectedDomainFilter == domain
                            val dColor = Color(domain.hexColor)
                            Surface(
                                color = if (isSelected) dColor.copy(alpha = 0.22f) else ObsidianBlack,
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(0.dp))
                                    .clickable { selectedDomainFilter = domain }
                                    .border(1.dp, if (isSelected) dColor else ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                            ) {
                                Text(
                                    text = "${domain.icon} ${domain.shortLabel}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = if (isSelected) Color.White else ScreenplayTextMuted, fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Search + Custom Star Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search stars (e.g. Rebel, Seeker, Vampire)...", color = ScreenplayTextMuted, fontSize = 11.sp) },
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customStarName,
                            onValueChange = { customStarName = it },
                            placeholder = { Text("Add custom star...", color = ScreenplayTextMuted, fontSize = 11.sp) },
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark),
                            singleLine = true,
                            modifier = Modifier.weight(0.75f)
                        )
                        Button(
                            onClick = {
                                val trimmed = customStarName.trim()
                                if (trimmed.isNotEmpty()) {
                                    val domainToUse = selectedDomainFilter ?: customStarDomain
                                    val newStar = StellariumStarSelection(
                                        name = trimmed,
                                        domain = domainToUse,
                                        polarity = StarPolarity.DUAL
                                    )
                                    onUpdate(character.copy(stellariumStars = stars + newStar))
                                    customStarName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text("+ Star", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Deck Cards Grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredCatalog.forEach { star ->
                            val isPinned = stars.any { it.archetypeId == star.id }
                            val dColor = Color(star.domain.hexColor)
                            Surface(
                                color = if (isPinned) dColor.copy(alpha = 0.22f) else ObsidianBlack,
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier
                                    .width(215.dp)
                                    .clip(RoundedCornerShape(0.dp))
                                    .clickable {
                                        val updated = if (isPinned) {
                                            stars.filterNot { it.archetypeId == star.id }
                                        } else {
                                            stars + StellariumStarSelection.fromCatalogStar(star, StarPolarity.DUAL)
                                        }
                                        onUpdate(character.copy(stellariumStars = updated))
                                    }
                                    .border(
                                        width = if (isPinned) 1.5.dp else 1.dp,
                                        color = if (isPinned) dColor else ObsidianSurfaceVariant,
                                        shape = RoundedCornerShape(0.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${star.domain.icon} ${star.name}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = if (isPinned) Color.White else dColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        if (isPinned) {
                                            Text("✓ PINNED", color = ElectrumGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "✦ ${star.positiveTrait}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.sp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "🌑 ${star.shadowTrait}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFCA5A5), fontSize = 10.sp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = ObsidianSurfaceVariant, thickness = 1.dp)

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
