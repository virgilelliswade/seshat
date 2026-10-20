package com.seshat.writer.ui.desktop

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.seshat.writer.ui.theme.neonBloom
import com.seshat.writer.ui.theme.opticalGlassBrush
import com.seshat.writer.ui.theme.prismaticGlassBorder
import com.seshat.writer.ui.theme.opticalPillBrush
import com.seshat.writer.ui.theme.smokedGlassBrush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seshat.writer.model.BlockType
import com.seshat.writer.screenplay.SceneItem
import com.seshat.writer.screenplay.ScreenplayUiState
import com.seshat.writer.screenplay.ScreenplayViewMode
import com.seshat.writer.ui.theme.CharacterGold
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LocalSeshatDarkTheme
import com.seshat.writer.ui.theme.MainAppFontFamily
import com.seshat.writer.ui.theme.ScreenplayCourierFontFamily
import com.seshat.writer.ui.theme.ScreenplayTextMuted
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.SluglineBlue
import com.seshat.writer.ui.theme.TransitionPurple

/**
 * Top Application Menu Bar for PC / Googlebook Studio Workstation.
 * Provides traditional desktop application menus: File, Edit, Format, Script, Production, Tools, View, Help.
 */
@Composable
fun DesktopMenuBar(
    uiState: ScreenplayUiState,
    onNewProject: () -> Unit,
    onOpenProject: () -> Unit,
    onSaveProject: () -> Unit,
    onExportProject: () -> Unit,
    onOpenTitlePage: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseToStartMenu: () -> Unit,
    onOpenFind: (Boolean) -> Unit,
    onRenameCharacter: () -> Unit,
    onChangeBlockType: (BlockType) -> Unit,
    onOpenStoryBible: () -> Unit,
    onOpenCorkboard: () -> Unit,
    onOpenTableRead: () -> Unit,
    onOpenScriptDoctor: () -> Unit,
    onOpenWritersRoom: () -> Unit,
    onToggleSceneNumbers: () -> Unit,
    onToggleWriterNotes: () -> Unit,
    onOpenRevisions: () -> Unit,
    onOpenDraftDiff: () -> Unit,
    onOpenReports: () -> Unit,
    onToggleDictation: () -> Unit,
    onToggleBinder: () -> Unit,
    onToggleInspector: () -> Unit,
    onToggleViewMode: () -> Unit,
    onToggleZenMode: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onToggleThemeMode: () -> Unit,
    onSwitchToTouchMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMenu by remember { mutableStateOf<String?>(null) }
    val isDark = LocalSeshatDarkTheme.current

    val barBg = opticalGlassBrush(glassOpacity = 0.85f)
    val borderCol = ScribeStarNeonRed.copy(alpha = 0.40f)
    val textCol = Color(0xFFE2E8F0)
    val menuDropBg = Color(0xF2080C14)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(barBg)
            .border(prismaticGlassBorder(width = 1.dp))
            .padding(horizontal = 8.dp)
    ) {
        // Left side: Brand mark + Desktop Menus
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Studio Brand Mark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clickable { onCloseToStartMenu() }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .neonBloom(
                            color = ScribeStarNeonRed,
                            cornerRadius = 4.dp,
                            glowRadius = 4.dp,
                            strokeWidth = 1.dp
                        )
                        .clip(CircleShape)
                        .background(ScribeStarNeonRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SESHAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 11.sp,
                        letterSpacing = 1.8.sp
                    )
                )
            }

            // Desktop Menus: File, Edit, Format, Script, Production, Tools, View, Help
            val menus = listOf("File", "Edit", "Format", "Script", "Production", "Tools", "View", "Help")
            menus.forEach { menuName ->
                Box {
                    DesktopMenuPill(
                        menuName = menuName,
                        isActive = activeMenu == menuName,
                        textCol = textCol,
                        onClick = { activeMenu = if (activeMenu == menuName) null else menuName }
                    )

                    DropdownMenu(
                        expanded = activeMenu == menuName,
                        onDismissRequest = { activeMenu = null },
                        modifier = Modifier
                            .background(menuDropBg)
                            .border(BorderStroke(1.dp, ScribeStarNeonRed.copy(alpha = 0.50f)), RoundedCornerShape(4.dp))
                    ) {
                        when (menuName) {
                            "File" -> {
                                DesktopMenuItem("New Project...", "Ctrl+N") { onNewProject(); activeMenu = null }
                                DesktopMenuItem("Open Project...", "Ctrl+O") { onOpenProject(); activeMenu = null }
                                DesktopMenuItem("Save", "Ctrl+S") { onSaveProject(); activeMenu = null }
                                HorizontalDivider(color = borderCol, modifier = Modifier.padding(vertical = 3.dp))
                                DesktopMenuItem("Title Page...", null) { onOpenTitlePage(); activeMenu = null }
                                DesktopMenuItem("Export PDF / FDX...", "Ctrl+E") { onExportProject(); activeMenu = null }
                                HorizontalDivider(color = borderCol, modifier = Modifier.padding(vertical = 3.dp))
                                DesktopMenuItem("Studio Settings...", null) { onOpenSettings(); activeMenu = null }
                                DesktopMenuItem("Close to Start Menu", null) { onCloseToStartMenu(); activeMenu = null }
                            }
                            "Edit" -> {
                                DesktopMenuItem("Find & Replace...", "Ctrl+F") { onOpenFind(false); activeMenu = null }
                                DesktopMenuItem("Replace in Script...", "Ctrl+H") { onOpenFind(true); activeMenu = null }
                                HorizontalDivider(color = borderCol, modifier = Modifier.padding(vertical = 3.dp))
                                DesktopMenuItem("Rename Character Globally...", null) { onRenameCharacter(); activeMenu = null }
                            }
                            "Format" -> {
                                when (uiState.projectFormat) {
                                    com.seshat.writer.library.ProjectFormat.STAGE_PLAY -> {
                                        DesktopMenuItem("Act / Scene", "Ctrl+1") { onChangeBlockType(BlockType.STAGE_ACT_SCENE); activeMenu = null }
                                        DesktopMenuItem("Stage Direction", "Ctrl+2") { onChangeBlockType(BlockType.STAGE_DIRECTION); activeMenu = null }
                                        DesktopMenuItem("Character", "Ctrl+3 / Tab") { onChangeBlockType(BlockType.CHARACTER); activeMenu = null }
                                        DesktopMenuItem("Parenthetical", "Ctrl+4") { onChangeBlockType(BlockType.PARENTHETICAL); activeMenu = null }
                                        DesktopMenuItem("Dialogue", "Ctrl+5") { onChangeBlockType(BlockType.DIALOGUE); activeMenu = null }
                                        DesktopMenuItem("Curtain / Blackout", "Ctrl+6") { onChangeBlockType(BlockType.STAGE_CURTAIN); activeMenu = null }
                                    }
                                    com.seshat.writer.library.ProjectFormat.COMIC_BOOK -> {
                                        DesktopMenuItem("Page Heading", "Ctrl+1") { onChangeBlockType(BlockType.COMIC_PAGE); activeMenu = null }
                                        DesktopMenuItem("Panel Heading", "Ctrl+2") { onChangeBlockType(BlockType.COMIC_PANEL); activeMenu = null }
                                        DesktopMenuItem("Art Direction", "Ctrl+3") { onChangeBlockType(BlockType.COMIC_ART_NOTE); activeMenu = null }
                                        DesktopMenuItem("Character", "Ctrl+4 / Tab") { onChangeBlockType(BlockType.CHARACTER); activeMenu = null }
                                        DesktopMenuItem("Speech Balloon", "Ctrl+5") { onChangeBlockType(BlockType.COMIC_BALLOON); activeMenu = null }
                                        DesktopMenuItem("Caption", "Ctrl+6") { onChangeBlockType(BlockType.COMIC_CAPTION); activeMenu = null }
                                        DesktopMenuItem("Sound Effect (SFX)", "Ctrl+7") { onChangeBlockType(BlockType.COMIC_SFX); activeMenu = null }
                                    }
                                    else -> {
                                        DesktopMenuItem("Scene Heading", "Ctrl+1") { onChangeBlockType(BlockType.SCENE_HEADING); activeMenu = null }
                                        DesktopMenuItem("Action", "Ctrl+2") { onChangeBlockType(BlockType.ACTION); activeMenu = null }
                                        DesktopMenuItem("Character", "Ctrl+3 / Tab") { onChangeBlockType(BlockType.CHARACTER); activeMenu = null }
                                        DesktopMenuItem("Parenthetical", "Ctrl+4") { onChangeBlockType(BlockType.PARENTHETICAL); activeMenu = null }
                                        DesktopMenuItem("Dialogue", "Ctrl+5") { onChangeBlockType(BlockType.DIALOGUE); activeMenu = null }
                                        DesktopMenuItem("Transition", "Ctrl+6") { onChangeBlockType(BlockType.TRANSITION); activeMenu = null }
                                    }
                                }
                            }
                            "Script" -> {
                                DesktopMenuItem("Story Bible & World Lore...", "Ctrl+B") { onOpenStoryBible(); activeMenu = null }
                                DesktopMenuItem("Corkboard Outline...", "Ctrl+O") { onOpenCorkboard(); activeMenu = null }
                                DesktopMenuItem("Script Doctor AI Diagnostics...", null) { onOpenScriptDoctor(); activeMenu = null }
                                DesktopMenuItem("Table Read Audio Player...", null) { onOpenTableRead(); activeMenu = null }
                                DesktopMenuItem("Writers' Room Collaboration...", null) { onOpenWritersRoom(); activeMenu = null }
                            }
                            "Production" -> {
                                DesktopMenuItem(
                                    if (uiState.showSceneNumbers) "✓ Scene Numbers (ON)" else "Scene Numbers (OFF)",
                                    null
                                ) { onToggleSceneNumbers(); activeMenu = null }
                                DesktopMenuItem(
                                    if (uiState.showWriterNotes) "✓ Writer's Notes (ON)" else "Writer's Notes (OFF)",
                                    null
                                ) { onToggleWriterNotes(); activeMenu = null }
                                HorizontalDivider(color = borderCol, modifier = Modifier.padding(vertical = 3.dp))
                                DesktopMenuItem("Production Revision Marks...", null) { onOpenRevisions(); activeMenu = null }
                                DesktopMenuItem("Compare Draft Revisions...", null) { onOpenDraftDiff(); activeMenu = null }
                                DesktopMenuItem("Character Dialogue Reports...", null) { onOpenReports(); activeMenu = null }
                            }
                            "Tools" -> {
                                DesktopMenuItem(
                                    if (uiState.isDictating) "🔴 Stop Voice Dictation" else "🎙️ Start Voice Dictation",
                                    "Ctrl+D / F10"
                                ) { onToggleDictation(); activeMenu = null }
                                DesktopMenuItem("Script Doctor Diagnostics...", null) { onOpenScriptDoctor(); activeMenu = null }
                            }
                            "View" -> {
                                DesktopMenuItem(
                                    if (uiState.isSidebarExpanded) "Hide Project Binder" else "Show Project Binder",
                                    "Ctrl+\\"
                                ) { onToggleBinder(); activeMenu = null }
                                DesktopMenuItem("Toggle Studio Inspector", "Ctrl+I") { onToggleInspector(); activeMenu = null }
                                HorizontalDivider(color = borderCol, modifier = Modifier.padding(vertical = 3.dp))
                                DesktopMenuItem(
                                    if (uiState.viewMode == ScreenplayViewMode.PAGE_VIEW) "✓ Physical Page View" else "Physical Page View",
                                    null
                                ) { if (uiState.viewMode != ScreenplayViewMode.PAGE_VIEW) onToggleViewMode(); activeMenu = null }
                                DesktopMenuItem(
                                    if (uiState.viewMode == ScreenplayViewMode.INLINE_VIEW) "✓ Continuous Flow View" else "Continuous Flow View",
                                    null
                                ) { if (uiState.viewMode != ScreenplayViewMode.INLINE_VIEW) onToggleViewMode(); activeMenu = null }
                                DesktopMenuItem(
                                    if (uiState.isZenMode) "Exit Zen Mode" else "Distraction-Free Zen Mode",
                                    "F11"
                                ) { onToggleZenMode(); activeMenu = null }
                                HorizontalDivider(color = borderCol, modifier = Modifier.padding(vertical = 3.dp))
                                DesktopMenuItem("Zoom In (+10%)", "Ctrl+=") { onZoomIn(); activeMenu = null }
                                DesktopMenuItem("Zoom Out (-10%)", "Ctrl+-") { onZoomOut(); activeMenu = null }
                                DesktopMenuItem("Reset Zoom (100%)", "Ctrl+0") { onResetZoom(); activeMenu = null }
                                HorizontalDivider(color = borderCol, modifier = Modifier.padding(vertical = 3.dp))
                                DesktopMenuItem("Switch to Touch App Mode", null) { onSwitchToTouchMode(); activeMenu = null }
                            }
                            "Help" -> {
                                DesktopMenuItem("Keyboard Shortcuts Guide", "F1") { onOpenSettings(); activeMenu = null }
                                DesktopMenuItem("About Seshat Writing Studio", null) { onOpenSettings(); activeMenu = null }
                            }
                        }
                    }
                }
            }
        }

        // Right side of Menu Bar: Project Title & State
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = uiState.title.ifBlank { "UNTITLED SCREENPLAY" }.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1
            )

            // Auto-save status pip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF131A26))
                    .border(BorderStroke(0.8.dp, borderCol), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Quick Theme Toggle
            Text(
                text = if (isDark) "🌙" else "☀️",
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .clickable { onToggleThemeMode() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun DesktopMenuItem(
    label: String,
    shortcut: String?,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                )
                if (shortcut != null) {
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = shortcut,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF64748B),
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
private fun DesktopMenuPill(
    menuName: String,
    isActive: Boolean,
    textCol: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isLit = isActive || isHovered

    Box(
        modifier = Modifier
            .then(
                if (isLit) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 4.dp,
                        glowRadius = 4.dp,
                        strokeWidth = 1.dp,
                        outerAlpha = 0.22f,
                        midAlpha = 0.45f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(4.dp))
            .background(opticalPillBrush(isLit = isLit, isAccent = isHovered))
            .border(
                prismaticGlassBorder(
                    width = if (isLit) 1.dp else 0.8.dp,
                    isLit = isLit
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = menuName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MainAppFontFamily,
                color = if (isLit) Color.White else textCol,
                fontWeight = if (isLit) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp
            )
        )
    }
}

/**
 * Studio Action Ribbon / Quick-Bar (36dp) sitting immediately below Menu Bar.
 */
@Composable
fun DesktopActionRibbon(
    uiState: ScreenplayUiState,
    onSaveProject: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenFind: () -> Unit,
    onToggleDictation: () -> Unit,
    onOpenScriptDoctor: () -> Unit,
    onOpenCorkboard: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onToggleViewMode: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    onToggleInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barBg = opticalGlassBrush(glassOpacity = 0.85f)
    val borderCol = ScribeStarNeonRed.copy(alpha = 0.40f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(barBg)
            .border(prismaticGlassBorder(width = 1.dp))
            .padding(horizontal = 8.dp)
    ) {
        // Left Tool Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            DesktopRibbonButton(icon = "📁", label = "Library", onClick = onOpenLibrary)
            DesktopRibbonButton(icon = "💾", label = "Save", onClick = onSaveProject, isAccent = true)
            DesktopRibbonButton(icon = "📤", label = "Export", onClick = onOpenExport)
            DesktopRibbonSeparator()
            DesktopRibbonButton(icon = "🔍", label = "Find", onClick = onOpenFind)
            DesktopRibbonButton(
                icon = if (uiState.isDictating) "🔴" else "🎙️",
                label = if (uiState.isDictating) "Recording" else "Dictate",
                onClick = onToggleDictation,
                isHighlight = uiState.isDictating
            )
            DesktopRibbonButton(icon = "🧠", label = "Doctor", onClick = onOpenScriptDoctor)
            DesktopRibbonSeparator()
            DesktopRibbonButton(icon = "📋", label = "Corkboard", onClick = onOpenCorkboard)
            DesktopRibbonButton(icon = "📖", label = "Bible", onClick = onOpenStoryBible)
        }

        // Center Breadcrumb
        val currentScene = uiState.scenes.getOrNull(uiState.selectedSceneIndex)
        val headingText = currentScene?.heading?.take(28) ?: "SCENE 1"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "ACT I",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            )
            Text(
                text = "  ›  ",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF475569), fontSize = 10.sp)
            )
            Text(
                text = headingText.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right View & Zoom Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // View Mode Glass Pill
            DesktopRibbonButton(
                icon = if (uiState.viewMode == ScreenplayViewMode.PAGE_VIEW) "📄" else "📜",
                label = if (uiState.viewMode == ScreenplayViewMode.PAGE_VIEW) "Page" else "Flow",
                onClick = onToggleViewMode
            )

            // Zoom Controls Glass Capsule
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(opticalPillBrush(isLit = false))
                    .border(prismaticGlassBorder(width = 0.8.dp), RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "–",
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable(onClick = onZoomOut)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = "${(uiState.zoomLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = Color.White,
                        fontSize = 10.5.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onResetZoom)
                        .padding(horizontal = 2.dp)
                )
                Text(
                    text = "+",
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable(onClick = onZoomIn)
                        .padding(horizontal = 4.dp)
                )
            }

            // Inspector Toggle Button
            DesktopRibbonButton(
                icon = "◨",
                label = "Inspector",
                onClick = onToggleInspector
            )
        }
    }
}

@Composable
private fun DesktopRibbonButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    isAccent: Boolean = false,
    isHighlight: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed || isHighlight

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 6.dp else if (isAccent) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "RibbonBtnGlow"
    )
    val outerAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.35f else if (isAccent) 0.18f else 0.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "RibbonBtnOuterAlpha"
    )
    val midAlpha by animateFloatAsState(
        targetValue = if (isLit) 0.65f else if (isAccent) 0.35f else 0.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "RibbonBtnMidAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "RibbonBtnScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isLit || isAccent) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 6.dp,
                        glowRadius = glowRadius,
                        strokeWidth = if (isLit) 1.4.dp else 1.dp,
                        outerAlpha = outerAlpha,
                        midAlpha = midAlpha,
                        coreAlpha = if (isLit) 1.0f else 0.75f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(6.dp))
            .background(
                opticalPillBrush(
                    isLit = isLit,
                    isPrimary = isHighlight,
                    isAccent = isAccent
                )
            )
            .border(
                prismaticGlassBorder(
                    width = if (isLit) 1.4.dp else 0.8.dp,
                    isLit = isLit
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isHighlight || isAccent || isLit) Color.White else Color(0xFFCBD5E1),
                    fontWeight = if (isHighlight || isAccent || isLit) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.5.sp
                )
            )
        }
    }
}

@Composable
private fun DesktopRibbonSeparator() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(16.dp)
            .background(Color(0xFF2D3748))
    )
}

/**
 * Docked Project Binder for Desktop Workstation.
 * High-density tree navigation with 1px precision border and active scene tally pip.
 */
@Composable
fun DesktopDockedBinder(
    uiState: ScreenplayUiState,
    onSceneClick: (SceneItem) -> Unit,
    onCloseBinder: () -> Unit,
    onToggleReorderMode: () -> Unit,
    onMoveSceneUp: (Int) -> Unit,
    onMoveSceneDown: (Int) -> Unit,
    onToggleOmitScene: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val binderBg = opticalGlassBrush(glassOpacity = 0.85f)
    val borderCol = ScribeStarNeonRed.copy(alpha = 0.40f)
    var searchQuery by remember { mutableStateOf("") }

    val filteredScenes = remember(uiState.scenes, searchQuery) {
        if (searchQuery.isBlank()) uiState.scenes
        else uiState.scenes.filter { it.heading.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(binderBg)
            .border(prismaticGlassBorder(width = 1.dp))
    ) {
        // Binder Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PROJECT BINDER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.2.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(ScribeStarNeonRed.copy(alpha = 0.20f))
                        .border(BorderStroke(0.8.dp, ScribeStarNeonRed.copy(alpha = 0.60f)), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${uiState.scenes.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = ScribeStarNeonRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (uiState.isReorderMode) "✓ Done" else "⇅",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        color = if (uiState.isReorderMode) ScribeStarNeonRed else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .clickable { onToggleReorderMode() }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Collapse Binder",
                    tint = Color(0xFF64748B),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onCloseBinder() }
                )
            }
        }

        HorizontalDivider(color = borderCol)

        // Scene Tree List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            itemsIndexed(filteredScenes) { idx, scene ->
                val isSelected = scene.index == uiState.selectedSceneIndex
                DesktopSceneBinderItem(
                    scene = scene,
                    isSelected = isSelected,
                    isReorderMode = uiState.isReorderMode,
                    canMoveUp = idx > 0,
                    canMoveDown = idx < filteredScenes.size - 1,
                    onSceneClick = { onSceneClick(scene) },
                    onMoveUp = { onMoveSceneUp(scene.index) },
                    onMoveDown = { onMoveSceneDown(scene.index) }
                )
            }
        }
    }
}

@Composable
private fun DesktopSceneBinderItem(
    scene: SceneItem,
    isSelected: Boolean,
    isReorderMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSceneClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isSelected || isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isSelected) 5.dp else if (isHovered) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "SceneGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .then(
                if (isLit) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 6.dp,
                        glowRadius = glowRadius,
                        strokeWidth = if (isSelected) 1.2.dp else 0.9.dp,
                        outerAlpha = if (isSelected) 0.32f else 0.16f,
                        midAlpha = if (isSelected) 0.60f else 0.32f,
                        coreAlpha = if (isSelected) 1.0f else 0.70f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(6.dp))
            .background(
                opticalPillBrush(
                    isLit = isSelected,
                    isAccent = isHovered
                )
            )
            .border(
                prismaticGlassBorder(
                    width = if (isSelected) 1.2.dp else 0.8.dp,
                    isLit = isLit
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSceneClick
            )
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Active Tally Indicator Pip
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (isSelected) ScribeStarNeonRed else ScribeStarNeonRed.copy(alpha = 0.25f))
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Scene Number Badge
            Text(
                text = "${scene.index + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = if (isSelected) ScribeStarNeonRed else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                ),
                modifier = Modifier.width(22.dp)
            )

            // Scene Heading
            Text(
                text = scene.heading,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = when {
                        scene.isOmitted -> Color(0xFF64748B)
                        isSelected -> Color.White
                        else -> Color(0xFFCBD5E1)
                    },
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (scene.isOmitted) {
                Text(
                    text = "OMIT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFEF4444),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (isReorderMode) {
                Row {
                    Text(
                        text = "▲",
                        fontSize = 9.sp,
                        color = if (canMoveUp) Color.White else Color(0xFF334155),
                        modifier = Modifier
                            .clickable(enabled = canMoveUp) { onMoveUp() }
                            .padding(horizontal = 3.dp)
                    )
                    Text(
                        text = "▼",
                        fontSize = 9.sp,
                        color = if (canMoveDown) Color.White else Color(0xFF334155),
                        modifier = Modifier
                            .clickable(enabled = canMoveDown) { onMoveDown() }
                            .padding(horizontal = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Collapsible Studio Inspector Pane on the right edge of Desktop Workstation.
 * Offers tabs for Scene Notes, Story Bible Snippets, and Script Doctor diagnostics.
 */
@Composable
fun DesktopInspectorPane(
    uiState: ScreenplayUiState,
    onCloseInspector: () -> Unit,
    onOpenStoryBible: () -> Unit,
    onOpenScriptDoctor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Scene, 1: Lore Bible, 2: Script Doctor
    val inspectorBg = opticalGlassBrush(glassOpacity = 0.85f)
    val borderCol = ScribeStarNeonRed.copy(alpha = 0.40f)

    val currentScene = uiState.scenes.getOrNull(uiState.selectedSceneIndex)

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(inspectorBg)
            .border(prismaticGlassBorder(width = 1.dp))
    ) {
        // Inspector Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "STUDIO INSPECTOR",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp
                )
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Inspector",
                tint = Color(0xFF94A3B8),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onCloseInspector() }
            )
        }

        // Inspector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val tabs = listOf("Scene", "Bible", "Doctor")
            tabs.forEachIndexed { i, tabLabel ->
                DesktopInspectorTabPill(
                    label = tabLabel,
                    isSelected = selectedTab == i,
                    onClick = { selectedTab = i },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(color = borderCol)

        // Inspector Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> { // Scene Tab
                    Text(
                        text = "ACTIVE SCENE METADATA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentScene?.heading ?: "No Scene Selected",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CHARACTERS IN SCENE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.characters.isEmpty()) "None detected" else uiState.characters.take(6).joinToString(", "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "WRITER'S NOTES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.notesCount > 0) "${uiState.notesCount} note(s) attached to script." else "No active notes on this scene.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                }
                1 -> { // Story Bible Tab
                    Text(
                        text = "STORY BIBLE QUICK-LOOK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Project: ${uiState.title}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quickly reference character arcs, location dossiers, and world rules while drafting.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DesktopGlassActionButton(
                        text = "📖 Open Full Story Bible",
                        onClick = onOpenStoryBible
                    )
                }
                2 -> { // Script Doctor Tab
                    Text(
                        text = "AI SCRIPT DOCTOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ScribeStarNeonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instant Scene Diagnostics",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Diagnose subtext, character voice pacing, dramatic irony, and tension curves for ${currentScene?.heading ?: "current scene"}.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = MainAppFontFamily,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DesktopGlassActionButton(
                        text = "🧠 Run Scene Analysis",
                        onClick = onOpenScriptDoctor,
                        isPrimary = true
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopInspectorTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isLit = isSelected || isHovered

    Box(
        modifier = modifier
            .then(
                if (isLit) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 5.dp,
                        glowRadius = 4.dp,
                        strokeWidth = 1.dp,
                        outerAlpha = if (isSelected) 0.32f else 0.16f,
                        midAlpha = if (isSelected) 0.60f else 0.32f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(5.dp))
            .background(
                opticalPillBrush(
                    isLit = isSelected,
                    isAccent = isHovered
                )
            )
            .border(
                prismaticGlassBorder(
                    width = if (isLit) 1.2.dp else 0.8.dp,
                    isLit = isLit
                ),
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MainAppFontFamily,
                color = if (isLit) Color.White else Color(0xFF94A3B8),
                fontWeight = if (isLit) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.5.sp
            )
        )
    }
}

@Composable
private fun DesktopGlassActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val glowRadius by animateDpAsState(
        targetValue = if (isLit) 6.dp else if (isPrimary) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "InspActionGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isLit || isPrimary) {
                    Modifier.neonBloom(
                        color = ScribeStarNeonRed,
                        cornerRadius = 6.dp,
                        glowRadius = glowRadius,
                        strokeWidth = if (isLit) 1.4.dp else 1.dp,
                        outerAlpha = if (isLit) 0.35f else 0.18f,
                        midAlpha = if (isLit) 0.65f else 0.35f
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(6.dp))
            .background(
                opticalPillBrush(
                    isLit = isLit,
                    isPrimary = isPrimary
                )
            )
            .border(
                prismaticGlassBorder(
                    width = if (isLit) 1.4.dp else 1.dp,
                    isLit = isLit
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MainAppFontFamily,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

/**
 * Flush Edge-to-Edge Desktop Status Bar (Bottom, 28dp).
 * Provides real-time screenwriter telemetry without taking hands off keyboard.
 */
@Composable
fun DesktopStatusBar(
    uiState: ScreenplayUiState,
    onSwitchToTouchMode: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barBg = opticalGlassBrush(glassOpacity = 0.85f)
    val borderCol = ScribeStarNeonRed.copy(alpha = 0.40f)
    val mutedText = Color(0xFF94A3B8)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(barBg)
            .border(prismaticGlassBorder(width = 1.dp))
            .padding(horizontal = 10.dp)
    ) {
        // Left telemetry
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PAGE ${uiState.pageCount}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
            Text(text = "•", color = mutedText, fontSize = 9.sp)
            Text(
                text = "SCENE ${uiState.selectedSceneIndex + 1} OF ${uiState.scenes.size.coerceAtLeast(1)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp
                )
            )
            Text(text = "•", color = mutedText, fontSize = 9.sp)
            Text(
                text = "COURIER PRIME 12PT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.5.sp
                )
            )
            Text(text = "•", color = mutedText, fontSize = 9.sp)
            val isMacBookDesk = remember { com.seshat.writer.util.DeviceEnvironment.isMacBook() }
            Surface(
                color = if (isMacBookDesk) Color(0x25E11D48) else Color(0x351E2836),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, if (isMacBookDesk) ScribeStarNeonRed.copy(alpha = 0.5f) else Color(0xFF64B5F6).copy(alpha = 0.5f))
            ) {
                Text(
                    text = if (isMacBookDesk) "🍎 MacBook • ⌘ Arrow Nav" else "💻 Keyboard Nav",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MainAppFontFamily,
                        fontSize = 9.5.sp,
                        color = if (isMacBookDesk) ScribeStarNeonRed else Color(0xFF64B5F6),
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        // Center telemetry
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${uiState.wordCount} WORDS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                )
            )
            Text(text = "•", color = mutedText, fontSize = 9.sp)
            Text(
                text = "~${uiState.pageCount} MIN RUNTIME",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MainAppFontFamily,
                    color = ScribeStarNeonRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }

        // Right telemetry
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Unified bottom-right mode switch toggle button
            WorkstationModeToggleButton(
                isDesktopMode = true,
                onToggle = onSwitchToTouchMode
            )
        }
    }
}

/**
 * Unified bottom-right mode toggle button.
 * Renders in the bottom-right corner of both PC Mode (DesktopStatusBar) and
 * Touch Mode (BottomHud / NovelBottomHud), providing a single consistent toggle.
 */
@Composable
fun WorkstationModeToggleButton(
    isDesktopMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val isPressed by interaction.collectIsPressedAsState()
    val isLit = isHovered || isPressed

    val text = if (isDesktopMode) "📱 Touch Mode" else "💻 PC Mode"
    val accentColor = if (isDesktopMode) ScribeStarNeonRed else Color(0xFF38BDF8)

    Box(
        modifier = modifier
            .then(
                if (isLit) {
                    Modifier.neonBloom(
                        color = accentColor,
                        cornerRadius = 4.dp,
                        glowRadius = 3.dp,
                        strokeWidth = 1.dp
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(4.dp))
            .background(opticalPillBrush(isLit = isLit, accentColor = accentColor))
            .border(
                prismaticGlassBorder(
                    width = 0.8.dp,
                    isLit = isLit,
                    accentColor = accentColor
                ),
                RoundedCornerShape(4.dp)
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MainAppFontFamily,
                color = if (isLit) Color.White else Color(0xFFCBD5E1),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
