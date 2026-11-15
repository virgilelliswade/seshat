package com.seshat.writer.universe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ObsidianBlack
import com.seshat.writer.ui.theme.ObsidianSurface
import com.seshat.writer.ui.theme.ObsidianSurfaceVariant
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextDark
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private enum class Node3DCategory {
    ROOT_LORE,
    ERA_RING,
    PROJECT_NODE,
    CHARACTER_NODE
}

private data class TreeNode3D(
    val id: String,
    val title: String,
    val category: Node3DCategory,
    val x: Float,
    val y: Float,
    val z: Float,
    val radius: Float,
    val color: Color,
    val icon: String,
    val formatBadge: String = "",
    val assignmentIndex: Int? = null,
    val eraTag: String = "",
    val roleLabel: String = "",
    val meta: String = "",
    val starringChars: List<String> = emptyList()
)

private data class TreeLink3D(
    val fromId: String,
    val toId: String,
    val color: Color,
    val isDashed: Boolean = false,
    val strokeWidth: Float = 2f
)

private data class ProjectedNode(
    val node: TreeNode3D,
    val screenX: Float,
    val screenY: Float,
    val screenRadius: Float,
    val depth: Float,
    val alpha: Float
)

/**
 * Native in-app interactive 3D Universe Tree.
 * Renders spatial orbits, foundation lore roots, the chronological timeline trunk,
 * medium branches, and character constellations directly inside Seshat using Compose Canvas.
 */
@Composable
fun Universe3DTreeInAppDialog(
    universeTitle: String,
    bible: StoryBible,
    blendedProject: BlendedUniverseProject?,
    onEnterProject: ((Int) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var yaw by remember { mutableFloatStateOf(28f) }
    var pitch by remember { mutableFloatStateOf(18f) }
    var zoom by remember { mutableFloatStateOf(1.05f) }
    var isAutoOrbiting by remember { mutableStateOf(false) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var selectedEraFilter by remember { mutableStateOf("ALL") }

    // Auto-orbit animation loop
    LaunchedEffect(isAutoOrbiting) {
        while (isAutoOrbiting) {
            delay(24)
            yaw = (yaw + 0.35f) % 360f
        }
    }

    // Build the 3D Tree scene model from live universe data
    val (nodes, links) = remember(universeTitle, bible, blendedProject) {
        build3DTreeModel(universeTitle, bible, blendedProject)
    }

    val eras = remember(blendedProject) {
        blendedProject?.eras ?: emptyList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07090D))
        ) {
            val textMeasurer = rememberTextMeasurer()

            // 3D Rendering Canvas with gesture handling
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            yaw = (yaw + dragAmount.x * 0.45f) % 360f
                            pitch = (pitch - dragAmount.y * 0.35f).coerceIn(-80f, 80f)
                        }
                    }
                    .pointerInput(nodes) {
                        detectTapGestures { tapOffset ->
                            // Find clicked node in screen projection
                            val cx = size.width / 2f
                            val cy = size.height / 2f + 40f
                            val radYaw = (yaw * PI / 180.0).toFloat()
                            val radPitch = (pitch * PI / 180.0).toFloat()
                            val cosY = cos(radYaw)
                            val sinY = sin(radYaw)
                            val cosP = cos(radPitch)
                            val sinP = sin(radPitch)
                            val cameraDist = 800f

                            var closestNode: TreeNode3D? = null
                            var minDistance = Float.MAX_VALUE

                            for (node in nodes) {
                                if (selectedEraFilter != "ALL" && node.eraTag.isNotBlank() && node.eraTag != selectedEraFilter) {
                                    continue
                                }
                                val x1 = node.x * cosY + node.z * sinY
                                val z1 = -node.x * sinY + node.z * cosY
                                val y2 = node.y * cosP - z1 * sinP
                                val z2 = node.y * sinP + z1 * cosP

                                val pScale = (cameraDist / (cameraDist + z2)).coerceIn(0.2f, 3f)
                                val sx = cx + x1 * pScale * zoom
                                val sy = cy + y2 * pScale * zoom
                                val sRadius = (node.radius * pScale * zoom).coerceAtLeast(8f)

                                val dx = tapOffset.x - sx
                                val dy = tapOffset.y - sy
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist <= sRadius * 2.2f && dist < minDistance) {
                                    minDistance = dist
                                    closestNode = node
                                }
                            }

                            selectedNodeId = closestNode?.id
                        }
                    }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f + 40f

                val radYaw = (yaw * PI / 180.0).toFloat()
                val radPitch = (pitch * PI / 180.0).toFloat()
                val cosY = cos(radYaw)
                val sinY = sin(radYaw)
                val cosP = cos(radPitch)
                val sinP = sin(radPitch)
                val cameraDist = 800f

                // Draw background stellar grid / stars
                drawCircle(Color(0xFF101B2E), radius = size.minDimension * 0.48f, center = Offset(cx, cy), alpha = 0.35f)
                drawCircle(Color(0xFF0F172A), radius = size.minDimension * 0.32f, center = Offset(cx, cy), alpha = 0.25f)

                // Project all 3D nodes into screen coordinates
                val projectedMap = mutableMapOf<String, ProjectedNode>()
                val projectedList = mutableListOf<ProjectedNode>()

                for (node in nodes) {
                    val isFilteredOut = selectedEraFilter != "ALL" && node.eraTag.isNotBlank() && node.eraTag != selectedEraFilter
                    val nodeAlphaMultiplier = if (isFilteredOut) 0.18f else 1f

                    // 1. Rotate around Y (Yaw)
                    val x1 = node.x * cosY + node.z * sinY
                    val z1 = -node.x * sinY + node.z * cosY

                    // 2. Rotate around X (Pitch)
                    val y2 = node.y * cosP - z1 * sinP
                    val z2 = node.y * sinP + z1 * cosP

                    // 3. Perspective Projection
                    val pScale = (cameraDist / (cameraDist + z2)).coerceIn(0.2f, 3f)
                    val sx = cx + x1 * pScale * zoom
                    val sy = cy + y2 * pScale * zoom
                    val sRadius = (node.radius * pScale * zoom).coerceAtLeast(4f)
                    val depthAlpha = ((z2 + 500f) / 1000f).coerceIn(0.35f, 1f) * nodeAlphaMultiplier

                    val proj = ProjectedNode(
                        node = node,
                        screenX = sx,
                        screenY = sy,
                        screenRadius = sRadius,
                        depth = z2,
                        alpha = depthAlpha
                    )
                    projectedMap[node.id] = proj
                    projectedList.add(proj)
                }

                // Draw connecting links in 3D
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                for (link in links) {
                    val p1 = projectedMap[link.fromId] ?: continue
                    val p2 = projectedMap[link.toId] ?: continue
                    val avgAlpha = ((p1.alpha + p2.alpha) / 2f).coerceIn(0.1f, 0.9f)

                    drawLine(
                        color = link.color.copy(alpha = avgAlpha),
                        start = Offset(p1.screenX, p1.screenY),
                        end = Offset(p2.screenX, p2.screenY),
                        strokeWidth = (link.strokeWidth * zoom).coerceIn(1f, 5f),
                        cap = StrokeCap.Round,
                        pathEffect = if (link.isDashed) dashedEffect else null
                    )
                }

                // Sort nodes back-to-front by depth (z2 descending)
                projectedList.sortByDescending { it.depth }

                // Draw each projected node
                for (p in projectedList) {
                    val node = p.node
                    val isSelected = node.id == selectedNodeId

                    // Pulsing outer halo if selected
                    if (isSelected) {
                        drawCircle(
                            color = ElectrumGold.copy(alpha = 0.35f),
                            radius = p.screenRadius * 2.2f,
                            center = Offset(p.screenX, p.screenY)
                        )
                        drawCircle(
                            color = ElectrumGold,
                            radius = p.screenRadius * 1.5f,
                            center = Offset(p.screenX, p.screenY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                        )
                    }

                    // Node body
                    drawCircle(
                        color = node.color.copy(alpha = p.alpha),
                        radius = p.screenRadius,
                        center = Offset(p.screenX, p.screenY)
                    )

                    // Node border
                    drawCircle(
                        color = Color.White.copy(alpha = (p.alpha * 0.9f).coerceIn(0.2f, 1f)),
                        radius = p.screenRadius,
                        center = Offset(p.screenX, p.screenY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isSelected) 2.5f else 1.2f)
                    )

                    // Floating label text if node is close or prominent
                    if (p.depth < 300f || isSelected || node.category == Node3DCategory.PROJECT_NODE) {
                        val textToDraw = "${node.icon} ${node.title.take(24)}"
                        val measured = textMeasurer.measure(
                            text = textToDraw,
                            style = TextStyle(
                                color = if (isSelected) ElectrumGold else Color.White.copy(alpha = p.alpha),
                                fontSize = if (isSelected) 12.sp else 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = MainAppFontFamily
                            )
                        )
                        val textOffset = Offset(
                            p.screenX - (measured.size.width / 2f),
                            p.screenY + p.screenRadius + 4f
                        )
                        // Dark text background scrim
                        drawRect(
                            color = Color(0xCC050811),
                            topLeft = Offset(textOffset.x - 4f, textOffset.y - 1f),
                            size = androidx.compose.ui.geometry.Size(measured.size.width + 8f, measured.size.height + 2f)
                        )
                        drawText(
                            textLayoutResult = measured,
                            topLeft = textOffset
                        )
                    }
                }
            }

            // Top Bar HUD Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🪐 3D UNIVERSE TREE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ElectrumGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 15.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("IN-APP SPATIAL VIEW", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                        }
                    }
                    Text(
                        text = "$universeTitle • Drag to Orbit • Scroll/Buttons to Zoom • Tap Node to Inspect",
                        style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextMuted, fontSize = 11.5.sp)
                    )
                }

                // Controls & Close
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Auto-Orbit toggle
                    InAppHudPill(
                        label = if (isAutoOrbiting) "⏸ Pause Orbit" else "▶ Auto Orbit",
                        isActive = isAutoOrbiting,
                        onClick = { isAutoOrbiting = !isAutoOrbiting }
                    )

                    // Zoom Controls
                    InAppHudPill(
                        label = "🔍 -",
                        isActive = false,
                        onClick = { zoom = (zoom - 0.15f).coerceAtLeast(0.5f) }
                    )
                    InAppHudPill(
                        label = "🔍 +",
                        isActive = false,
                        onClick = { zoom = (zoom + 0.15f).coerceAtMost(2.5f) }
                    )
                    InAppHudPill(
                        label = "↺ Reset",
                        isActive = false,
                        onClick = {
                            yaw = 28f
                            pitch = 18f
                            zoom = 1.05f
                            selectedNodeId = null
                        }
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ObsidianSurface, RoundedCornerShape(0.dp))
                            .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close 3D View", tint = ScreenplayTextMuted)
                    }
                }
            }

            // Eras filter pills along top-center
            if (eras.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                        .horizontalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(0.dp))
                        .background(Color(0xCC0A0F1D))
                        .border(1.dp, ObsidianSurfaceVariant, RoundedCornerShape(0.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ERAS:", style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    InAppFilterChip(label = "ALL", isSelected = selectedEraFilter == "ALL", onClick = { selectedEraFilter = "ALL" })
                    for (era in eras) {
                        InAppFilterChip(
                            label = era.name,
                            isSelected = selectedEraFilter == era.name,
                            onClick = { selectedEraFilter = era.name }
                        )
                    }
                }
            }

            // Selected Node Inspector Card (Bottom-Right)
            val activeNode = nodes.find { it.id == selectedNodeId }
            AnimatedVisibility(
                visible = activeNode != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                if (activeNode != null) {
                    Surface(
                        color = Color(0xEE0D1322),
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(1.5.dp, ElectrumGold),
                        modifier = Modifier
                            .widthIn(max = 360.dp)
                            .shadow(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(activeNode.icon, fontSize = 20.sp)
                                    Text(
                                        text = activeNode.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = ElectrumGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                                IconButton(onClick = { selectedNodeId = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Inspector", tint = ScreenplayTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }

                            if (activeNode.formatBadge.isNotBlank() || activeNode.eraTag.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (activeNode.formatBadge.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(0.dp))
                                                .background(activeNode.color.copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(activeNode.formatBadge, color = activeNode.color, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    }
                                    if (activeNode.eraTag.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(0.dp))
                                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("⏳ ${activeNode.eraTag}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }

                            if (activeNode.roleLabel.isNotBlank()) {
                                Text(
                                    text = activeNode.roleLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(color = ScreenplayTextMuted, fontSize = 11.sp)
                                )
                            }

                            if (activeNode.meta.isNotBlank()) {
                                Text(
                                    text = activeNode.meta,
                                    style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontSize = 12.sp)
                                )
                            }

                            if (activeNode.starringChars.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "🎭 Starring Characters:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = LightGold, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                )
                                Text(
                                    text = activeNode.starringChars.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall.copy(color = ScreenplayTextDark, fontSize = 11.5.sp)
                                )
                            }

                            // Enter Project Button if this node is an assignment
                            if (activeNode.assignmentIndex != null && onEnterProject != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        onEnterProject(activeNode.assignmentIndex)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectrumGold),
                                    shape = RoundedCornerShape(0.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("🚀 Enter Project Editor", color = ObsidianBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
private fun InAppHudPill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) ElectrumGold else ObsidianSurface,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, if (isActive) ElectrumGold else ObsidianSurfaceVariant),
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isActive) ObsidianBlack else ScreenplayTextDark,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun InAppFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) ElectrumGold else Color.Transparent,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, if (isSelected) ElectrumGold else Color(0xFF334155)),
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) ObsidianBlack else ScreenplayTextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun build3DTreeModel(
    universeTitle: String,
    bible: StoryBible,
    blendedProject: BlendedUniverseProject?
): Pair<List<TreeNode3D>, List<TreeLink3D>> {
    val nodes = mutableListOf<TreeNode3D>()
    val links = mutableListOf<TreeLink3D>()

    // 1. Foundational Lore Roots at subterranean base (Y > 160f)
    val loreRoots = listOf(
        Triple("lore_cosmo", "Cosmology & Laws", "✨"),
        Triple("lore_anthro", "Anthropology & Factions", "🏛️"),
        Triple("lore_tech", "Technology & Systems", "⚡"),
        Triple("lore_chrono", "Chronology & Eras", "⏳")
    )
    val rootAngles = floatArrayOf(0f, 90f, 180f, 270f)
    for (i in loreRoots.indices) {
        val (id, title, icon) = loreRoots[i]
        val ang = (rootAngles[i] * PI / 180.0).toFloat()
        val rx = cos(ang) * 160f
        val rz = sin(ang) * 160f
        val ry = 220f
        nodes.add(
            TreeNode3D(
                id = id,
                title = title,
                category = Node3DCategory.ROOT_LORE,
                x = rx,
                y = ry,
                z = rz,
                radius = 16f,
                color = Color(0xFF8B5CF6),
                icon = icon,
                formatBadge = "LORE PILLAR",
                roleLabel = "Foundational Universe Lore",
                meta = "Shared universe canon rules and world-building foundations."
            )
        )
        // Root tendrils connecting to the trunk base
        links.add(TreeLink3D(fromId = id, toId = "trunk_base", color = Color(0xFF8B5CF6).copy(alpha = 0.5f), strokeWidth = 2f))
    }

    // 2. Trunk Base & Central Chronological Trunk Axis
    nodes.add(
        TreeNode3D(
            id = "trunk_base",
            title = universeTitle,
            category = Node3DCategory.ERA_RING,
            x = 0f,
            y = 120f,
            z = 0f,
            radius = 18f,
            color = ElectrumGold,
            icon = "🪐",
            formatBadge = "UNIVERSE ANCHOR",
            roleLabel = "Central World Tree Spine",
            meta = "The core origin and spine connecting all eras and medium branches."
        )
    )

    // 3. Chronological Eras along the vertical axis (from Y = 80f up to Y = -220f)
    val eras = blendedProject?.eras ?: emptyList()
    val eraCount = eras.size.coerceAtLeast(1)
    val eraStepY = 280f / (eraCount + 1).toFloat()

    var lastTrunkId = "trunk_base"

    for (eIdx in eras.indices) {
        val era = eras[eIdx]
        val ey = 100f - ((eIdx + 1) * eraStepY)
        val eraNodeId = "era_$eIdx"
        nodes.add(
            TreeNode3D(
                id = eraNodeId,
                title = era.name,
                category = Node3DCategory.ERA_RING,
                x = 0f,
                y = ey,
                z = 0f,
                radius = 12f,
                color = Color(0xFF38BDF8),
                icon = "⏳",
                formatBadge = "TIMELINE ERA",
                eraTag = era.name,
                roleLabel = "Chronological Milestone",
                meta = era.description.ifBlank { "Timeline epoch of the universe." }
            )
        )
        links.add(TreeLink3D(fromId = lastTrunkId, toId = eraNodeId, color = ElectrumGold, strokeWidth = 4f))
        lastTrunkId = eraNodeId
    }

    // 4. Project Branches (Sprouting from the trunk and eras)
    val assignments = blendedProject?.assignments ?: emptyList()

    for (aIdx in assignments.indices) {
        val a = assignments[aIdx]
        val pColor = when (a.format) {
            ProjectFormat.SCREENPLAY -> ElectrumGold
            ProjectFormat.TV_SERIES -> Color(0xFF10B981)
            ProjectFormat.NOVEL -> Color(0xFFF43F5E)
            ProjectFormat.COMIC_BOOK -> Color(0xFF06B6D4)
            ProjectFormat.STAGE_PLAY -> Color(0xFFA855F7)
        }
        val icon = when (a.format) {
            ProjectFormat.SCREENPLAY -> "🎬"
            ProjectFormat.TV_SERIES -> "📺"
            ProjectFormat.NOVEL -> "📖"
            ProjectFormat.COMIC_BOOK -> "💥"
            ProjectFormat.STAGE_PLAY -> "🎭"
        }

        // Calculate 3D branch position
        val radialDistance = if (a.branchRole == BranchRole.MAIN_SPINE) 130f else 220f
        val angleDeg = (aIdx * (360f / assignments.size.coerceAtLeast(1)) + (if (a.branchRole == BranchRole.MAIN_SPINE) 0f else 45f))
        val radAng = (angleDeg * PI / 180.0).toFloat()
        val bx = cos(radAng) * radialDistance
        val bz = sin(radAng) * radialDistance

        // Match height Y to the assignment's era or index
        val eraIndex = eras.indexOfFirst { it.name.equals(a.eraTag, ignoreCase = true) }
        val by = if (eraIndex >= 0) {
            100f - ((eraIndex + 1) * eraStepY) + ((aIdx % 3 - 1) * 22f)
        } else {
            70f - (aIdx * (240f / assignments.size.coerceAtLeast(1)))
        }

        val projectNodeId = "proj_${a.id}"
        val stats = if (a.format == ProjectFormat.NOVEL) {
            "${a.novelManuscript.chapters.size} chapters • ${a.novelManuscript.totalWordCount()} words"
        } else {
            "${a.blocks.size} blocks"
        }

        nodes.add(
            TreeNode3D(
                id = projectNodeId,
                title = a.title,
                category = Node3DCategory.PROJECT_NODE,
                x = bx,
                y = by,
                z = bz,
                radius = if (a.branchRole == BranchRole.MAIN_SPINE) 17f else 13f,
                color = pColor,
                icon = icon,
                formatBadge = a.mediumBadge(),
                assignmentIndex = aIdx,
                eraTag = a.eraTag,
                roleLabel = if (a.branchRole == BranchRole.MAIN_SPINE) "Main Canon Spine" else "Spinoff / Sidequest",
                meta = "$stats • ${a.projectNotes.ifBlank { "Project ready for writing." }}",
                starringChars = a.starringCharacterNames
            )
        )

        // Connect branch to parent spinoff or closest trunk node
        val parentNodeId = a.spinoffParentId?.let { pid -> "proj_$pid" } ?: run {
            if (eraIndex >= 0) "era_$eraIndex" else "trunk_base"
        }
        links.add(
            TreeLink3D(
                fromId = parentNodeId,
                toId = projectNodeId,
                color = pColor.copy(alpha = 0.8f),
                strokeWidth = if (a.branchRole == BranchRole.MAIN_SPINE) 3f else 1.8f
            )
        )

        // 5. Starring Characters orbiting this project
        val starring = a.starringCharacterNames
        for (cIdx in starring.indices) {
            val charName = starring[cIdx]
            val cAng = ((cIdx * 90f + aIdx * 30f) * PI / 180.0).toFloat()
            val cx = bx + cos(cAng) * 48f
            val cz = bz + sin(cAng) * 48f
            val cy = by + ((cIdx % 2 * 2 - 1) * 18f)
            val charNodeId = "char_${a.id}_$cIdx"
            nodes.add(
                TreeNode3D(
                    id = charNodeId,
                    title = charName,
                    category = Node3DCategory.CHARACTER_NODE,
                    x = cx,
                    y = cy,
                    z = cz,
                    radius = 8f,
                    color = Color(0xFFFBBF24),
                    icon = "🎭",
                    formatBadge = "CHARACTER",
                    eraTag = a.eraTag,
                    roleLabel = "Starring in ${a.title}",
                    meta = "Master universe character profile."
                )
            )
            links.add(
                TreeLink3D(
                    fromId = projectNodeId,
                    toId = charNodeId,
                    color = Color(0xFFFBBF24).copy(alpha = 0.6f),
                    isDashed = true,
                    strokeWidth = 1.2f
                )
            )
        }
    }

    return Pair(nodes, links)
}
