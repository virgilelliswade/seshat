package com.seshat.writer.firstdraft

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Full-screen 2.2-second procedural Genre Campaign Ignition Transition Overlay.
 * Plays when the writer launches a Gamified Genre Campaign, and can be skipped anytime by clicking.
 */
@Composable
fun GenreIgnitionTransitionOverlay(
    genre: CampaignGenre,
    projectTitle: String,
    durationMs: Long = 2200L,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember(genre) { mutableStateOf(0f) }
    var elapsedMs by remember(genre) { mutableStateOf(0L) }
    var hasCompleted by remember(genre) { mutableStateOf(false) }

    LaunchedEffect(genre) {
        val startTime = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val elapsed = (now - startTime).coerceAtLeast(0L)
            elapsedMs = elapsed
            progress = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            if (elapsed >= durationMs) {
                break
            }
        }
        if (!hasCompleted) {
            hasCompleted = true
            onFinished()
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    val ignitionHeadline = when (genre) {
        CampaignGenre.HORROR -> "🩸 SURVIVAL MODE ENGAGED"
        CampaignGenre.SCIFI -> "🚀 HYPERDRIVE PROTOCOL ONLINE"
        CampaignGenre.FANTASY -> "⚔️ QUEST ACCEPTED: THE PROPHECY"
        CampaignGenre.THRILLER -> "🕵️ HEIST CLOCK RUNNING // EYES ONLY"
        CampaignGenre.ACTION -> "💥 OVERDRIVE ENGAGED — 9,000 RPM"
        CampaignGenre.COMEDY -> "🎤 LIVE ON AIR — YOU'RE UP!"
        CampaignGenre.DRAMA -> "🏆 VIRTUOSO MODE — QUIET ON SET"
        CampaignGenre.MYSTERY -> "🔍 CASE FILE #01: CRIME SCENE ACTIVE"
    }

    val ignitionSubtitle = when (genre) {
        CampaignGenre.HORROR -> "\"Heartbeat detected. Lock your Logline & Victim in 30 minutes before the flatline hits...\""
        CampaignGenre.SCIFI -> "\"Matrix telemetry locked. Engaging FTL starfield jump to Stage 1 Ignition...\""
        CampaignGenre.FANTASY -> "\"Arcane runes ignited. Forge your prophecy before the dungeon gate seals...\""
        CampaignGenre.THRILLER -> "\"Searchlights sweeping. Connect the red strings and lock your alibi...\""
        CampaignGenre.ACTION -> "\"Tachometer at redline! Cut the brakes and blast into the first act...\""
        CampaignGenre.COMEDY -> "\"Two spotlights on center stage. The mic is hot—land the premise!\""
        CampaignGenre.DRAMA -> "\"Velvet curtains parting. Step into the golden light and write from the heart.\""
        CampaignGenre.MYSTERY -> "\"Flashbulbs popping in the fog. Scan the fingerprints before the trail goes cold.\""
    }

    val fxDescription = when (genre) {
        CampaignGenre.HORROR -> "FX: Arterial EKG Spike & Viscous Crimson Drip"
        CampaignGenre.SCIFI -> "FX: Matrix Cyber-Rain → FTL Starfield Warp"
        CampaignGenre.FANTASY -> "FX: Rotating Arcane Rune Sigil & Rising Forge Embers"
        CampaignGenre.THRILLER -> "FX: Noir Venetian Blinds, Searchlight & Red String Web"
        CampaignGenre.ACTION -> "FX: RPM Tachometer Redline, Shockwave & Nitro Sparks"
        CampaignGenre.COMEDY -> "FX: Dual Sweeping Stage Spotlights & Neon ON AIR Frame"
        CampaignGenre.DRAMA -> "FX: Parting Velvet Proscenium Curtains & 35mm Gold Bokeh"
        CampaignGenre.MYSTERY -> "FX: Forensic Flashbulbs, Crime Tape & Magnifying Reticle"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!hasCompleted) {
                        hasCompleted = true
                        onFinished()
                    }
                }
            )
    ) {
        // 1. Full-screen Procedural Genre Canvas
        GenreTransitionCanvas(
            genre = genre,
            timeMs = elapsedMs,
            progress = progress,
            isAmbient = false,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top HUD Bar (Genre Badge + Currency + Skip Pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xCC050811),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.2.dp, ElectrumGold.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = genre.modeTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = MainAppFontFamily,
                            color = ElectrumGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    color = Color(0xB30F172A),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "⚡ ${genre.currencyName}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Surface(
                color = Color(0xCC450E1A),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.75f))
            ) {
                val secs = String.format(Locale.US, "%.1fs / %.1fs", elapsedMs / 1000f, durationMs / 1000f)
                Text(
                    text = "⚡ IGNITION ($secs) • CLICK TO SKIP ⏭",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color(0xFFFECDD3),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // 3. Center Cinematic Title Card Overlay
        Surface(
            color = Color(0xBF050811),
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(1.5.dp, ElectrumGold.copy(alpha = 0.75f)),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .widthIn(max = 620.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayProject = projectTitle.trim().ifBlank { "UNTITLED CAMPAIGN" }.uppercase(Locale.US)
                Text(
                    text = "$displayProject • STAGE 1 OF 4: ${genre.stage1Title.uppercase(Locale.US)} (30:00)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = ScribeStarNeonRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ignitionHeadline,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = ignitionSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color(0xFFFDE68A),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(0.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0.03f, 1f))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ScribeStarNeonRed, ElectrumGold, ScribeStarNeonRed)
                                )
                            )
                    )
                }
            }
        }

        // 4. Bottom Stage Roadmap Strip
        Surface(
            color = Color(0xCC050811),
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1️⃣ ${genre.stage1Title} (30m)  →  2️⃣ ${genre.stage2Title} (48h)  →  3️⃣ ${genre.stage3Title} (24h)  →  4️⃣ ${genre.stage4Title}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = fxDescription,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = ElectrumGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

/**
 * Ambient looping background canvas for the Live Campaign Briefing card in the Genre Selector.
 */
@Composable
fun AmbientGenrePreviewCanvas(
    genre: CampaignGenre,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AmbientGenreTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AmbientGenrePhase"
    )

    val timeMs = (phase * 4000f).toLong()
    GenreTransitionCanvas(
        genre = genre,
        timeMs = timeMs,
        progress = phase,
        isAmbient = true,
        modifier = modifier
    )
}

@Composable
fun GenreTransitionCanvas(
    genre: CampaignGenre,
    timeMs: Long,
    progress: Float,
    isAmbient: Boolean,
    modifier: Modifier = Modifier
) {
    val alphaMul = if (isAmbient) 0.42f else 1f
    Canvas(modifier = modifier) {
        when (genre) {
            CampaignGenre.HORROR -> drawHorrorTransition(timeMs, progress, alphaMul)
            CampaignGenre.SCIFI -> drawSciFiTransition(timeMs, progress, alphaMul)
            CampaignGenre.FANTASY -> drawFantasyTransition(timeMs, progress, alphaMul)
            CampaignGenre.THRILLER -> drawThrillerTransition(timeMs, progress, alphaMul)
            CampaignGenre.ACTION -> drawActionTransition(timeMs, progress, alphaMul)
            CampaignGenre.COMEDY -> drawComedyTransition(timeMs, progress, alphaMul)
            CampaignGenre.DRAMA -> drawDramaTransition(timeMs, progress, alphaMul)
            CampaignGenre.MYSTERY -> drawMysteryTransition(timeMs, progress, alphaMul)
        }
    }
}

/**
 * 1. HORROR: Viscous Crimson Blood Drips & Jagged Arterial EKG Heartbeat Spike
 */
private fun DrawScope.drawHorrorTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF24040B).copy(alpha = alphaMul),
                Color(0xFF050102).copy(alpha = alphaMul)
            ),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = max(w, h) * 0.7f
        )
    )

    // 24 Viscous crimson drips from top edge
    val dripCount = 24
    val dripColor = Color(0xFFBE123C).copy(alpha = 0.72f * alphaMul)
    for (i in 0 until dripCount) {
        val x = ((i + 0.5f) / dripCount.toFloat()) * w
        val speed = 1.2f + (i % 5) * 0.55f
        val maxLen = 50f + (i * 37 % 140)
        val radius = 3.5f + (i % 4) * 1.6f
        val cycleH = h + maxLen
        val currentLen = ((timeMs * 0.14f * speed + i * 43f) % cycleH).coerceAtLeast(16f)

        drawRoundRect(
            color = dripColor,
            topLeft = Offset(x - radius, 0f),
            size = Size(radius * 2f, min(h, currentLen)),
            cornerRadius = CornerRadius(radius, radius)
        )
        drawCircle(
            color = dripColor,
            radius = radius * 1.45f,
            center = Offset(x, min(h, currentLen))
        )
    }

    // Jagged EKG Heartbeat trace across center
    val ekgPath = Path()
    val midY = h * 0.5f
    val step = 8f
    var x = 0f
    var first = true
    val scroll = (timeMs * 0.32f).toInt()
    while (x <= w) {
        val cycle = (x.toInt() + scroll) % 200
        val yOffset = when {
            cycle in 75..85 -> -h * 0.16f
            cycle in 86..98 -> h * 0.14f
            cycle in 99..112 -> -h * 0.08f
            else -> 0f
        }
        val y = midY + yOffset
        if (first) {
            ekgPath.moveTo(x, y)
            first = false
        } else {
            ekgPath.lineTo(x, y)
        }
        x += step
    }

    drawPath(
        path = ekgPath,
        color = Color(0xFFFF1E42).copy(alpha = 0.35f * alphaMul),
        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = ekgPath,
        color = Color(0xFFFF1E42).copy(alpha = 0.95f * alphaMul),
        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * 2. SCI-FI: Cascading Cyan Matrix Telemetry Streams & FTL Starfield Warp Jump
 */
private fun DrawScope.drawSciFiTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(color = Color(0xFF030816).copy(alpha = alphaMul))

    // Cascading Matrix digital streams
    val colCount = 36
    for (i in 0 until colCount) {
        val x = ((i + 0.5f) / colCount.toFloat()) * w
        val speed = 1.5f + (i % 6) * 0.65f
        val headY = ((timeMs * 0.18f * speed + i * 67f) % (h + 120f))
        for (seg in 0 until 6) {
            val segY = headY - seg * 14f
            if (segY in 0f..h) {
                val segColor = if (seg == 0) {
                    Color(0xFFA5F3FC).copy(alpha = 0.9f * alphaMul)
                } else {
                    Color(0xFF06B6D4).copy(alpha = (0.72f - seg * 0.11f).coerceAtLeast(0.1f) * alphaMul)
                }
                drawRoundRect(
                    color = segColor,
                    topLeft = Offset(x - 2.5f, segY),
                    size = Size(5f, 9f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
    }

    // Radial FTL Warp Star-Streaks
    val cx = w * 0.5f
    val cy = h * 0.5f
    val maxR = max(w, h) * 0.6f
    val starCount = 64
    for (i in 0 until starCount) {
        val angle = (i * 137.5f) * (PI.toFloat() / 180f)
        val speed = 2f + (i % 5) * 1.1f
        val dist = ((timeMs * 0.16f * speed + i * 29f) % maxR) + 18f
        val streakLen = dist * (0.22f + progress * 0.18f)
        val x1 = cx + cos(angle) * dist
        val y1 = cy + sin(angle) * dist
        val x2 = cx + cos(angle) * (dist + streakLen)
        val y2 = cy + sin(angle) * (dist + streakLen)
        drawLine(
            color = Color(0xFF38BDF8).copy(alpha = 0.85f * alphaMul),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 3. FANTASY: Rotating Golden Arcane Hexagram Rune Circle & Rising Forge Embers
 */
private fun DrawScope.drawFantasyTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2A1235).copy(alpha = alphaMul),
                Color(0xFF09040E).copy(alpha = alphaMul)
            ),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = max(w, h) * 0.65f
        )
    )

    val cx = w * 0.5f
    val cy = h * 0.5f
    val ringR = min(w, h) * 0.34f
    val innerR = ringR * 0.82f
    val runeColor = Color(0xFFFBBF24).copy(alpha = 0.72f * alphaMul)

    // Rotating arcane rings & hexagram triangles
    val rotDeg = (timeMs * 0.045f) % 360f
    rotate(degrees = rotDeg, pivot = Offset(cx, cy)) {
        drawCircle(
            color = runeColor,
            radius = ringR,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f)
        )
        drawCircle(
            color = runeColor,
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.8f)
        )

        for (t in 0..1) {
            val offsetAngle = t * (PI.toFloat() / 3f)
            val triPath = Path()
            for (v in 0..2) {
                val ang = offsetAngle + v * (2f * PI.toFloat() / 3f)
                val vx = cx + cos(ang) * innerR
                val vy = cy + sin(ang) * innerR
                if (v == 0) triPath.moveTo(vx, vy) else triPath.lineTo(vx, vy)
            }
            triPath.close()
            drawPath(
                path = triPath,
                color = runeColor,
                style = Stroke(width = 2.2f, join = StrokeJoin.Round)
            )
        }
    }

    // Rising molten forge embers
    val emberCount = 42
    for (i in 0 until emberCount) {
        val baseX = ((i * 73 % 100) / 100f) * w
        val sway = sin(timeMs * 0.003f + i) * 24f
        val x = (baseX + sway).coerceIn(0f, w)
        val speed = 1.2f + (i % 4) * 0.6f
        val y = h - ((timeMs * 0.09f * speed + i * 31f) % h)
        val radius = 2.2f + (i % 3) * 1.5f
        val emberColor = if (i % 2 == 0) Color(0xFFF59E0B) else Color(0xFFFDE047)
        drawCircle(
            color = emberColor.copy(alpha = 0.82f * alphaMul),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

/**
 * 4. THRILLER / NOIR: Venetian Blind Slats, Helicopter Searchlight & Red String Web
 */
private fun DrawScope.drawThrillerTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(color = Color(0xFF090D16).copy(alpha = alphaMul))

    // Venetian blind shadow slats
    val slatSpacing = 28f
    var sy = 0f
    while (sy < h) {
        drawRect(
            color = Color.White.copy(alpha = 0.04f * alphaMul),
            topLeft = Offset(0f, sy),
            size = Size(w, 11f)
        )
        sy += slatSpacing
    }

    // Sweeping Helicopter Searchlight Cone
    val sweepX = w * 0.5f + sin(timeMs * 0.0025f) * (w * 0.35f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFEF08A).copy(alpha = 0.28f * alphaMul),
                Color.Transparent
            ),
            center = Offset(sweepX, h * 0.5f),
            radius = min(w, h) * 0.55f
        ),
        radius = min(w, h) * 0.55f,
        center = Offset(sweepX, h * 0.5f)
    )

    // Red Conspiracy String Board nodes
    val pins = listOf(
        Offset(w * 0.16f, h * 0.22f),
        Offset(w * 0.34f, h * 0.17f),
        Offset(w * 0.58f, h * 0.26f),
        Offset(w * 0.82f, h * 0.20f),
        Offset(w * 0.23f, h * 0.76f),
        Offset(w * 0.51f, h * 0.78f),
        Offset(w * 0.77f, h * 0.72f)
    )

    for (i in pins.indices) {
        val p1 = pins[i]
        val p2 = pins[(i + 2) % pins.size]
        drawLine(
            color = Color(0xFFEF4444).copy(alpha = 0.82f * alphaMul),
            start = p1,
            end = p2,
            strokeWidth = 2.4f
        )
    }

    pins.forEach { pt ->
        drawCircle(
            color = Color(0xFFFDE047).copy(alpha = 0.9f * alphaMul),
            radius = 5.5f,
            center = pt
        )
    }
}

/**
 * 5. ACTION: Tachometer RPM Redline Arc, Expanding Shockwave & Nitro Welding Sparks
 */
private fun DrawScope.drawActionTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(color = Color(0xFF140703).copy(alpha = alphaMul))

    val cx = w * 0.5f
    val cy = h * 0.5f
    val maxWaveR = min(w, h) * 0.52f
    val waveR = ((timeMs * 0.28f) % maxWaveR).coerceAtLeast(20f)
    val waveAlpha = ((1f - waveR / maxWaveR) * 0.85f * alphaMul).coerceIn(0f, 1f)

    // Shockwave ring
    drawCircle(
        color = Color(0xFFF97316).copy(alpha = waveAlpha),
        radius = waveR,
        center = Offset(cx, cy),
        style = Stroke(width = 4f)
    )

    // Tachometer Redline Arc
    val arcR = min(w, h) * 0.36f
    drawArc(
        color = Color(0xFFDC2626).copy(alpha = 0.9f * alphaMul),
        startAngle = 195f,
        sweepAngle = 150f * progress.coerceIn(0.2f, 1f),
        useCenter = false,
        topLeft = Offset(cx - arcR, cy - arcR),
        size = Size(arcR * 2f, arcR * 2f),
        style = Stroke(width = 10f, cap = StrokeCap.Round)
    )

    // High-velocity Nitro Sparks
    val sparkCount = 48
    for (i in 0 until sparkCount) {
        val angle = (i * 7.5f) * (PI.toFloat() / 180f)
        val t = ((timeMs * 0.004f + i * 0.07f) % 1f)
        val dist = t * maxWaveR * 1.1f
        val sx = cx + cos(angle) * dist
        val sy = cy + sin(angle) * dist
        val sparkColor = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFFFB923C)
        drawCircle(
            color = sparkColor.copy(alpha = (1f - t) * 0.9f * alphaMul),
            radius = 3f,
            center = Offset(sx, sy)
        )
    }
}

/**
 * 6. COMEDY: Dual Sweeping Stage Spotlights & Neon Comedy Club Brick Frame
 */
private fun DrawScope.drawComedyTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(color = Color(0xFF160712).copy(alpha = alphaMul))

    // Brick wall horizontal lines
    var by = 0f
    while (by < h) {
        drawLine(
            color = Color(0xFFF472B6).copy(alpha = 0.09f * alphaMul),
            start = Offset(0f, by),
            end = Offset(w, by),
            strokeWidth = 1f
        )
        by += 24f
    }

    // Dual sweeping follow-spotlights
    val offset = sin(timeMs * 0.003f) * (w * 0.18f)
    val spot1X = w * 0.5f + offset
    val spot2X = w * 0.5f - offset
    val spotR = min(w, h) * 0.44f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFEC4899).copy(alpha = 0.36f * alphaMul), Color.Transparent),
            center = Offset(spot1X, h * 0.5f),
            radius = spotR
        ),
        radius = spotR,
        center = Offset(spot1X, h * 0.5f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFACC15).copy(alpha = 0.32f * alphaMul), Color.Transparent),
            center = Offset(spot2X, h * 0.5f),
            radius = spotR
        ),
        radius = spotR,
        center = Offset(spot2X, h * 0.5f)
    )

    // Flickering Neon Marquee Frame
    val flicker = if (sin(timeMs * 0.03f) > -0.85f) 1f else 0.35f
    drawRoundRect(
        color = Color(0xFFF43F5E).copy(alpha = 0.82f * flicker * alphaMul),
        topLeft = Offset(w * 0.10f, h * 0.12f),
        size = Size(w * 0.80f, h * 0.76f),
        cornerRadius = CornerRadius(18f, 18f),
        style = Stroke(width = 3.5f)
    )
}

/**
 * 7. DRAMA / ROMANCE: Parting Velvet Proscenium Curtains & 35mm Golden Bokeh Orbs
 */
private fun DrawScope.drawDramaTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1C1204).copy(alpha = alphaMul),
                Color(0xFF090502).copy(alpha = alphaMul)
            )
        )
    )

    // Warm 35mm Golden Bokeh Orbs
    val bokehCount = 24
    for (i in 0 until bokehCount) {
        val bx = (((i * 89 % 100) / 100f) * w + sin(timeMs * 0.0012f + i) * 35f).coerceIn(0f, w)
        val by = h - ((timeMs * 0.03f + i * 47f) % h)
        val br = 12f + (i % 5) * 8f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFDE047).copy(alpha = 0.36f * alphaMul),
                    Color.Transparent
                ),
                center = Offset(bx, by),
                radius = br
            ),
            radius = br,
            center = Offset(bx, by)
        )
    }

    // Left & Right Velvet Curtains parting outward
    val openRatio = (progress * 1.3f).coerceIn(0f, 1f)
    val curtainW = (w * 0.42f) * (1f - openRatio * 0.72f)
    val velvetColor = Color(0xFF881337).copy(alpha = 0.85f * alphaMul)
    val goldTrim = Color(0xFFFBBF24).copy(alpha = 0.9f * alphaMul)

    drawRect(
        color = velvetColor,
        topLeft = Offset(0f, 0f),
        size = Size(curtainW, h)
    )
    drawRect(
        color = velvetColor,
        topLeft = Offset(w - curtainW, 0f),
        size = Size(curtainW, h)
    )
    drawLine(
        color = goldTrim,
        start = Offset(curtainW, 0f),
        end = Offset(curtainW, h),
        strokeWidth = 3f
    )
    drawLine(
        color = goldTrim,
        start = Offset(w - curtainW, 0f),
        end = Offset(w - curtainW, h),
        strokeWidth = 3f
    )
}

/**
 * 8. MYSTERY: Concentric Fingerprint Whorl, Crime Tape, Magnifying Reticle & Flashbulbs
 */
private fun DrawScope.drawMysteryTransition(timeMs: Long, progress: Float, alphaMul: Float) {
    val w = size.width
    val h = size.height

    drawRect(color = Color(0xFF041311).copy(alpha = alphaMul))

    val cx = w * 0.5f
    val cy = h * 0.5f
    val maxR = min(w, h) * 0.38f

    // Fingerprint whorl rings
    var r = 18f
    while (r <= maxR) {
        drawArc(
            color = Color(0xFF10B981).copy(alpha = 0.28f * alphaMul),
            startAngle = 15f,
            sweepAngle = 310f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.85f, cy - r * 1.12f),
            size = Size(r * 1.7f, r * 2.24f),
            style = Stroke(width = 2f)
        )
        r += 11f
    }

    // Diagonal Crime Scene Tape stripes
    rotate(degrees = -9f, pivot = Offset(cx, cy)) {
        drawRect(
            color = Color(0xFFEAB308).copy(alpha = 0.78f * alphaMul),
            topLeft = Offset(-w * 0.2f, cy - h * 0.34f),
            size = Size(w * 1.4f, 18f)
        )
        drawRect(
            color = Color(0xFFEAB308).copy(alpha = 0.78f * alphaMul),
            topLeft = Offset(-w * 0.2f, cy + h * 0.30f),
            size = Size(w * 1.4f, 18f)
        )
    }

    // Scanning Magnifying Reticle
    val scanX = cx + cos(timeMs * 0.0025f) * (w * 0.16f)
    val scanY = cy + sin(timeMs * 0.0035f) * (h * 0.14f)
    drawCircle(
        color = Color(0xFF34D399).copy(alpha = 0.9f * alphaMul),
        radius = min(w, h) * 0.12f,
        center = Offset(scanX, scanY),
        style = Stroke(width = 3f)
    )

    // Periodic forensic camera flashbulb pop
    val flash = ((sin(timeMs * 0.009f) - 0.92f) * 4.5f).coerceIn(0f, 0.38f)
    if (flash > 0f) {
        drawRect(color = Color.White.copy(alpha = flash * alphaMul))
    }
}
